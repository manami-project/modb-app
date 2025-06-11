package io.github.manamiproject.modb.app.crawlers.animenewsnetwork

import io.github.manamiproject.AnimenewsnetworkConfig
import io.github.manamiproject.AnimenewsnetworkDownloader
import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.convfiles.AlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.convfiles.DefaultAlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.crawlers.Crawler
import io.github.manamiproject.modb.app.crawlers.LastPageMemorizer
import io.github.manamiproject.modb.app.crawlers.PaginationIdRangeSelector
import io.github.manamiproject.modb.app.crawlers.StringBasedLastPageMemorizer
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.dataset.DefaultDeadEntriesAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateScheduler
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateScheduler
import io.github.manamiproject.modb.app.network.SuspendableHttpClient
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.excludeFromTestContext
import io.github.manamiproject.modb.core.extensions.createShuffledList
import io.github.manamiproject.modb.core.extensions.eitherNullOrBlank
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.random
import kotlinx.coroutines.delay
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration

/**
 * Implementation of [Crawler] for `animenewsnetwork.com`.
 * Uses [DownloadControlStateScheduler] to to download all anime scheduled for re-download and
 * [PaginationIdRangeSelector] for downloading new entries.
 * Includes a hard coded random waiting time to reduce pressure on the meta data provider.
 * @since 1.7.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property metaDataProviderConfig Configuration for a specific meta data provider.
 * @property downloadControlStateScheduler Allows to check which anime are scheduled for re-download and which are not.
 * @property deadEntriesAccessor Access to dead entries files.
 * @property lastPageMemorizer Access to the last which has been crawled.
 * @property paginationIdRangeSelector Creates a list of anime IDs found on pages of the meta data provider.
 * @property alreadyDownloadedIdsFinder Fetches all IDs which have already been downloaded.
 * @property downloader Downloader for a specific meta data provider.
 */
class AnimenewsnetworkCrawler(
    private val appConfig: Config = AppConfig.instance,
    private val metaDataProviderConfig: MetaDataProviderConfig = AnimenewsnetworkConfig,
    private val downloadControlStateScheduler: DownloadControlStateScheduler = DefaultDownloadControlStateScheduler.instance,
    private val deadEntriesAccessor: DeadEntriesAccessor = DefaultDeadEntriesAccessor.instance,
    private val lastPageMemorizer: LastPageMemorizer<String> = StringBasedLastPageMemorizer(metaDataProviderConfig = metaDataProviderConfig),
    private val paginationIdRangeSelector: PaginationIdRangeSelector<String> = AnimenewsnetworkPaginationIdRangeSelector.instance,
    private val alreadyDownloadedIdsFinder: AlreadyDownloadedIdsFinder = DefaultAlreadyDownloadedIdsFinder.instance,
    private val downloader: Downloader = AnimenewsnetworkDownloader(
        httpClient = SuspendableHttpClient(),
    ),
): Crawler {

    override suspend fun start() {
        log.info { "Starting crawler for [${metaDataProviderConfig.hostname()}]." }

        downloadEntriesScheduledForCurrentWeek()
        wait()
        downloadEntriesUsingPagination()

        log.info { "Finished crawling data for [${metaDataProviderConfig.hostname()}]." }
    }

    private suspend fun downloadEntriesScheduledForCurrentWeek() {
        log.info { "Downloading [${metaDataProviderConfig.hostname()}] entries scheduled for the current week." }

        val ids = downloadControlStateScheduler.findEntriesScheduledForCurrentWeek(metaDataProviderConfig) - alreadyDownloadedIdsFinder.alreadyDownloadedIds(metaDataProviderConfig)
        startDownload(ids.toList().createShuffledList())

        log.info { "Finished downloading [${metaDataProviderConfig.hostname()}] entries scheduled for the current week." }
    }

    private suspend fun downloadEntriesUsingPagination() {
        log.info { "Downloading [${metaDataProviderConfig.hostname()}] entries using pagination." }

        val lastPage = lastPageMemorizer.retrieveLastPage()
        val entriesNotScheduledForCurrentWeek = downloadControlStateScheduler.findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig)
        var pages = listOf("9", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z")

        check(lastPage.eitherNullOrBlank() || pages.contains(lastPage)) { "Invalid last page: [$lastPage]." }

        if (lastPage.neitherNullNorBlank()) {
            pages = pages.dropWhile { it != lastPage }.drop(1)
        }

        wait()

        pages.forEach { page ->
            val currentList = paginationIdRangeSelector.idDownloadList(page) - entriesNotScheduledForCurrentWeek - alreadyDownloadedIdsFinder.alreadyDownloadedIds(metaDataProviderConfig)
            wait()
            startDownload(currentList)
            lastPageMemorizer.memorizeLastPage(page)
        }

        log.info { "Finished downloading [${metaDataProviderConfig.hostname()}] entries using pagination." }
    }
    
    private suspend fun startDownload(idDownloadList: List<String>) = repeat(idDownloadList.size) { index ->
        val animeId = idDownloadList[index]
        val file = appConfig.workingDir(metaDataProviderConfig).resolve("$animeId.${metaDataProviderConfig.fileSuffix()}")

        wait()

        log.debug { "Downloading ${index+1}/${idDownloadList.size}: [animenewsnetworkId=$animeId]" }

        val response = downloader.download(animeId) {
            deadEntriesAccessor.addDeadEntry(it, metaDataProviderConfig)
        }

        if (response.neitherNullNorBlank()) {
            response.writeToFile(file, true)
        }
    }

    @KoverIgnore
    private suspend fun wait() {
        excludeFromTestContext(metaDataProviderConfig) {
            delay(random(1000, 1500).toDuration(MILLISECONDS))
        }
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [AnimenewsnetworkCrawler]
         * @since 1.0.0
         */
        val instance: AnimenewsnetworkCrawler by lazy { AnimenewsnetworkCrawler() }
    }
}