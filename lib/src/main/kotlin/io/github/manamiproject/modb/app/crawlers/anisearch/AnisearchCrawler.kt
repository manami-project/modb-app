package io.github.manamiproject.modb.app.crawlers.anisearch

import io.github.manamiproject.modb.anisearch.AnisearchConfig
import io.github.manamiproject.modb.anisearch.AnisearchDownloader
import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.convfiles.AlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.convfiles.DefaultAlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.crawlers.*
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateScheduler
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateScheduler
import io.github.manamiproject.modb.app.network.LinuxNetworkController
import io.github.manamiproject.modb.app.network.NetworkController
import io.github.manamiproject.modb.app.network.SuspendableHttpClient
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.excludeFromTestContext
import io.github.manamiproject.modb.core.extensions.createShuffledList
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.ThrowableRetryCase
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.random
import kotlinx.coroutines.delay
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.UnknownHostException
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration

/**
 * Implementation of [Crawler] for `anisearch.com`.
 * Uses [DownloadControlStateScheduler] to download all anime scheduled for re-download and
 * [PaginationIdRangeSelector] for downloading new entries.
 * Includes a hard coded random waiting time to reduce pressure on the metadata provider.
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property metaDataProviderConfig Configuration for a specific metadata provider.
 * @property downloadControlStateScheduler Allows to check which anime are scheduled for re-download and which are not.
 * @property downloadControlStateAccessor Access to DCS files.
 * @property lastPageMemorizer Access to the last which has been crawled.
 * @property lastPageDetector Allows to identify the last page of a metadata provider.
 * @property paginationIdRangeSelector Creates a list of anime IDs found on pages of the metadata provider.
 * @property alreadyDownloadedIdsFinder Fetches all IDs which have already been downloaded.
 * @property httpClient To actually download the anime data.
 * @property downloader Downloader for a specific metadata provider.
 * @property networkController Access to the network controller which allows to perform a restart.
 */
class AnisearchCrawler(
    private val appConfig: Config = AppConfig.instance,
    private val metaDataProviderConfig: MetaDataProviderConfig,
    private val downloadControlStateScheduler: DownloadControlStateScheduler = DefaultDownloadControlStateScheduler.instance,
    private val downloadControlStateAccessor: DownloadControlStateAccessor = DefaultDownloadControlStateAccessor.instance,
    private val lastPageMemorizer: LastPageMemorizer<Int> = IntegerBasedLastPageMemorizer(metaDataProviderConfig = metaDataProviderConfig),
    private val lastPageDetector: HighestIdDetector = AnisearchLastPageDetector.instance,
    private val paginationIdRangeSelector: PaginationIdRangeSelector<Int> = AnisearchPaginationIdRangeSelector(metaDataProviderConfig = metaDataProviderConfig),
    private val alreadyDownloadedIdsFinder: AlreadyDownloadedIdsFinder = DefaultAlreadyDownloadedIdsFinder.instance,
    private val httpClient: HttpClient = SuspendableHttpClient(),
    private val downloader: Downloader = AnisearchDownloader(
        metaDataProviderConfig = metaDataProviderConfig,
        httpClient = httpClient,
    ),
    private val networkController: NetworkController = LinuxNetworkController.instance,
): Crawler {

    init {
        val restart = suspend { networkController.restartAsync().join() }
        httpClient.addRetryCases(ThrowableRetryCase(executeBefore = restart) { it is ConnectException})
        httpClient.addRetryCases(ThrowableRetryCase(executeBefore = restart) { it is UnknownHostException})
        httpClient.addRetryCases(ThrowableRetryCase(executeBefore = restart) { it is NoRouteToHostException})
    }

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
            wait()
            startDownload(currentList)
            lastPageMemorizer.memorizeLastPage(currentPage)
        }

        log.info { "Finished downloading [${metaDataProviderConfig.hostname()}] entries using pagination." }
    }

    private suspend fun startDownload(idDownloadList: List<String>) = repeat(idDownloadList.size) { index ->
        val animeId = idDownloadList[index]
        val file = appConfig.workingDir(metaDataProviderConfig).resolve("$animeId.${metaDataProviderConfig.fileSuffix()}")

        wait()

        log.debug { "Downloading ${index+1}/${idDownloadList.size}: [anisearchId=$animeId]" }

        val response = download(animeId)

        if (response.neitherNullNorBlank()) {
            response.writeToFile(file, true)
        }
    }

    private suspend fun download(animeId: AnimeId): String {
        return downloader.download(animeId) {
            if (metaDataProviderConfig is AnisearchConfig) {
                downloadControlStateAccessor.removeDeadEntry(metaDataProviderConfig, animeId)
            }
        }
    }

    @KoverIgnore
    private suspend fun wait() {
        excludeFromTestContext(metaDataProviderConfig) {
            delay(random(2000, 3000).toDuration(MILLISECONDS))
        }
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}