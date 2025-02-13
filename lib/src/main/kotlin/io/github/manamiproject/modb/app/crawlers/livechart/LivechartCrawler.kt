package io.github.manamiproject.modb.app.crawlers.livechart

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.convfiles.AlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.convfiles.DefaultAlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.crawlers.*
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
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.anime.AnimeSeason
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.UNDEFINED
import io.github.manamiproject.modb.core.anime.YEAR_OF_THE_FIRST_ANIME
import io.github.manamiproject.modb.core.anime.Year
import io.github.manamiproject.modb.core.random
import io.github.manamiproject.modb.livechart.LivechartConfig
import io.github.manamiproject.modb.livechart.LivechartDownloader
import kotlinx.coroutines.delay
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration

/**
 * Implementation of [Crawler] for `livechart.me`.
 * Uses [DownloadControlStateScheduler] to to download all anime scheduled for re-download and
 * [PaginationIdRangeSelector] for downloading new entries.
 * Includes a hard coded random waiting time to reduce pressure on the meta data provider.
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property metaDataProviderConfig Configuration for a specific meta data provider.
 * @property downloadControlStateScheduler Allows to check which anime are scheduled for re-download and which are not.
 * @property deadEntriesAccessor Access to dead entries files.
 * @property lastPageMemorizer Access to the last which has been crawled.
 * @property newestYearDetector Allows to find the newest year available.
 * @property paginationIdRangeSelector Creates a list of anime IDs found on pages of the meta data provider.
 * @property alreadyDownloadedIdsFinder Fetches all IDs which have already been downloaded.
 * @property downloader Downloader for a specific meta data provider.
 */
class LivechartCrawler(
    private val appConfig: Config = AppConfig.instance,
    private val metaDataProviderConfig: MetaDataProviderConfig = LivechartConfig,
    private val downloadControlStateScheduler: DownloadControlStateScheduler = DefaultDownloadControlStateScheduler.instance,
    private val deadEntriesAccessor: DeadEntriesAccessor = DefaultDeadEntriesAccessor.instance,
    private val lastPageMemorizer: LastPageMemorizer<String> = StringBasedLastPageMemorizer(metaDataProviderConfig = metaDataProviderConfig),
    private val newestYearDetector: HighestIdDetector = LivechartNewestYearDetector.instance,
    private val paginationIdRangeSelector: PaginationIdRangeSelector<String> = LivechartPaginationIdRangeSelector.instance,
    private val alreadyDownloadedIdsFinder: AlreadyDownloadedIdsFinder = DefaultAlreadyDownloadedIdsFinder.instance,
    private val downloader: Downloader = LivechartDownloader(httpClient = SuspendableHttpClient()),
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

        val newestYear = newestYearDetector.detectHighestId()
        val lastPage = lastPageMemorizer.retrieveLastPage()
        val entriesNotScheduledForCurrentWeek = downloadControlStateScheduler.findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig)
        var pages = createListOfPages(newestYear)

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

    private fun createListOfPages(newestYear: Year): List<String> {
        return (YEAR_OF_THE_FIRST_ANIME..newestYear).flatMap { year ->
            AnimeSeason.Season.entries
                .filterNot { season -> season == UNDEFINED }
                .map { season -> season.toString().lowercase() }
                .map { season -> "$season-$year" }
        }
            .toMutableList()
            .apply { add("tba") }
            .sorted()
    }

    private suspend fun startDownload(idDownloadList: List<String>) = repeat(idDownloadList.size) { index ->
        val animeId = idDownloadList[index]
        val file = appConfig.workingDir(metaDataProviderConfig).resolve("$animeId.${metaDataProviderConfig.fileSuffix()}")

        wait()

        log.debug { "Downloading ${index+1}/${idDownloadList.size}: [livechartId=$animeId]" }

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
            delay(random(2000, 3500).toDuration(MILLISECONDS))
        }
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [LivechartCrawler]
         * @since 1.0.0
         */
        val instance: LivechartCrawler by lazy { LivechartCrawler() }
    }
}