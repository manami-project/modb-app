package io.github.manamiproject.modb.app.crawlers.animeplanet

import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import io.github.manamiproject.modb.animeplanet.AnimePlanetDownloader
import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.convfiles.AlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.convfiles.DefaultAlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.crawlers.*
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateScheduler
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateScheduler
import io.github.manamiproject.modb.app.network.SuspendableHttpClient
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.excludeFromTestContext
import io.github.manamiproject.modb.core.extensions.createShuffledList
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.random
import kotlinx.coroutines.delay
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration

/**
 * Implementation of [Crawler] for `anime-planet.com`.
 * Uses [DownloadControlStateScheduler] to to download all anime scheduled for re-download and
 * [PaginationIdRangeSelector] for downloading new entries.
 * Includes a hard coded random waiting time to reduce pressure on the meta data provider.
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property metaDataProviderConfig Configuration for a specific meta data provider.
 * @property downloadControlStateScheduler Allows to check which anime are scheduled for re-download and which are not.
 * @property downloadControlStateAccessor Access to DCS files.
 * @property lastPageMemorizer Access to the last which has been crawled.
 * @property lastPageDetector Allows to identify the last page of a meta data provider.
 * @property paginationIdRangeSelector Creates a list of anime IDs found on pages of the meta data provider.
 * @property alreadyDownloadedIdsFinder Fetches all IDs which have already been downloaded.
 * @property downloader Downloader for a specific meta data provider.
 */
class AnimePlanetCrawler(
    private val appConfig: Config = AppConfig.instance,
    private val metaDataProviderConfig: MetaDataProviderConfig = AnimePlanetConfig,
    private val downloadControlStateScheduler: DownloadControlStateScheduler = DefaultDownloadControlStateScheduler.instance,
    private val downloadControlStateAccessor: DownloadControlStateAccessor = DefaultDownloadControlStateAccessor.instance,
    private val lastPageMemorizer: LastPageMemorizer<Int> = IntegerBasedLastPageMemorizer(metaDataProviderConfig = metaDataProviderConfig),
    private val lastPageDetector: HighestIdDetector = AnimePlanetLastPageDetector.instance,
    private val paginationIdRangeSelector: PaginationIdRangeSelector<Int> = AnimePlanetPaginationIdRangeSelector.instance,
    private val alreadyDownloadedIdsFinder: AlreadyDownloadedIdsFinder = DefaultAlreadyDownloadedIdsFinder.instance,
    private val downloader: Downloader = AnimePlanetDownloader(httpClient = SuspendableHttpClient()),
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

        val firstPage = lastPageMemorizer.retrieveLastPage()
        val lastPage = lastPageDetector.detectHighestId()
        val entriesNotScheduledForCurrentWeek = downloadControlStateScheduler.findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig)

        wait()

        for (currentPage in firstPage..lastPage) {
            val currentList = paginationIdRangeSelector.idDownloadList(currentPage) - entriesNotScheduledForCurrentWeek - alreadyDownloadedIdsFinder.alreadyDownloadedIds(metaDataProviderConfig)
            startDownload(currentList)
            lastPageMemorizer.memorizeLastPage(currentPage)
        }

        log.info { "Finished downloading [${metaDataProviderConfig.hostname()}] entries using pagination." }
    }

    private suspend fun startDownload(idDownloadList: List<String>) = repeat(idDownloadList.size) { index ->
        val animeId = idDownloadList[index]
        val file = appConfig.workingDir(metaDataProviderConfig).resolve("$animeId.${metaDataProviderConfig.fileSuffix()}")

        wait()

        log.debug { "Downloading ${index+1}/${idDownloadList.size}: [animePlanetId=$animeId]" }

        val response = downloader.download(animeId) {
            downloadControlStateAccessor.removeDeadEntry(metaDataProviderConfig, animeId)
        }

        if (response.neitherNullNorBlank()) {
            response.writeToFile(file, true)
        }
    }

    @KoverIgnore
    private suspend fun wait() {
        excludeFromTestContext(metaDataProviderConfig) {
            delay(random(1000, 1200).toDuration(MILLISECONDS))
        }
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [AnimePlanetCrawler]
         * @since 1.0.0
         */
        val instance: AnimePlanetCrawler by lazy { AnimePlanetCrawler() }
    }
}