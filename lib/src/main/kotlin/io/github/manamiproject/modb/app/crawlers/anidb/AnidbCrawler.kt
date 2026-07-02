package io.github.manamiproject.modb.app.crawlers.anidb

import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anidb.AnidbDownloader
import io.github.manamiproject.modb.anidb.AnidbEntryStatus.*
import io.github.manamiproject.modb.anidb.AnidbResponseStatusChecker
import io.github.manamiproject.modb.anidb.AnidbWebViewConfig
import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.crawlers.Crawler
import io.github.manamiproject.modb.app.crawlers.IdRangeSelector
import io.github.manamiproject.modb.app.crawlers.IntegerBasedIdRangeSelector
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.dataset.DefaultDeadEntriesAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.app.network.LinuxNetworkController
import io.github.manamiproject.modb.app.network.NetworkController
import io.github.manamiproject.modb.app.network.SuspendableHttpClient
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.excludeFromTestContext
import io.github.manamiproject.modb.core.extensions.*
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
 * Implementation of [Crawler] for `anidb.net`.
 * Uses [IntegerBasedIdRangeSelector] to determine which data to download.
 * Includes a hard coded random waiting time to reduce pressure on the metadata provider.
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property metaDataProviderConfig [MetaDataProviderConfig] for downloading data via REST-API.
 * @property webViewMetaDataProviderConfig [MetaDataProviderConfig] for downloading data via web view (HTML content).
 * @property deadEntriesAccess Access to dead entries files.
 * @property downloadControlStateAccessor Access to DCS files.
 * @property idRangeSelector Delivers the IDs to download.
 * @property httpClient To actually download the anime data.
 * @property apiDownloader Downloader for XML API content.
 * @property webViewDownloader Downloader for HTML content.
 * @property networkController Access to the network controller which allows to perform a restart.
 */
class AnidbCrawler(
    private val appConfig: Config = AppConfig.instance,
    private val metaDataProviderConfig: MetaDataProviderConfig = AnidbConfig,
    private val webViewMetaDataProviderConfig: MetaDataProviderConfig = AnidbWebViewConfig,
    private val deadEntriesAccess: DeadEntriesAccessor = DefaultDeadEntriesAccessor.instance,
    private val downloadControlStateAccessor: DownloadControlStateAccessor = DefaultDownloadControlStateAccessor.instance,
    private val idRangeSelector: IdRangeSelector<Int> = IntegerBasedIdRangeSelector(
        metaDataProviderConfig = metaDataProviderConfig,
        highestIdDetector = AnidbHighestIdDetector.instance,
    ),
    private val httpClient: HttpClient = SuspendableHttpClient(),
    private val apiDownloader: Downloader = AnidbDownloader(
        metaDataProviderConfig = metaDataProviderConfig,
        httpClient = httpClient,
    ),
    private val webViewDownloader: Downloader = AnidbDownloader(
        metaDataProviderConfig = webViewMetaDataProviderConfig,
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

        val idDownloadList = idRangeSelector.idDownloadList().toMutableList()

        if (idDownloadList.isNotEmpty()) {
            appConfig.workingDir(metaDataProviderConfig).listRegularFiles("*.${ANIDB_PENDING_FILE_SUFFIX}").forEach {
                idDownloadList.remove(it.fileName().remove(".${ANIDB_PENDING_FILE_SUFFIX}").toInt())
            }

            when {
                idDownloadList.isEmpty() -> log.info { "No IDs left for [${metaDataProviderConfig.hostname()}] crawler to download." }
                else -> startDownload(idDownloadList)
            }
        }

        log.info { "Finished crawling data for [${metaDataProviderConfig.hostname()}]." }
    }

    private suspend fun startDownload(idDownloadList: List<Int>) = repeat(idDownloadList.size) { index ->
        val animeId = idDownloadList[index]

        wait()

        log.debug { "Downloading ${index+1}/${idDownloadList.size}: [anidbId=$animeId]" }

        val responseBody = apiDownloader.download(animeId.toString()) {
            throw IllegalStateException("Should not happen [anidbId=$animeId].")
        }

        val responseCheckerResult = AnidbResponseStatusChecker(
            responseBody = responseBody,
            metaDataProviderConfig = metaDataProviderConfig,
        ).checkStatus()

        if (responseCheckerResult == EXISTS) {
            val file = appConfig.workingDir(metaDataProviderConfig).resolve("$animeId.${metaDataProviderConfig.fileSuffix()}")
            responseBody.writeToFile(file, true)
        }

        if (responseCheckerResult == NOT_FOUND) {
            log.debug { "The [anidbId=$animeId] returned 'not found'. Checking if the entry is pending addition or deleted." }
            handleNotFound(animeId)
        }
    }

    private suspend fun handleNotFound(animeId: Int) {
        if (downloadControlStateAccessor.dcsEntryExists(metaDataProviderConfig, animeId.toAnimeId())) {
            log.debug { "A DCS entry for ID [anidbId=$animeId] already exists. Therefore we know that the 'not found' response indicates a deleted entry." }
            deadEntriesAccess.addDeadEntry(animeId.toAnimeId(), metaDataProviderConfig)
            return
        }

        log.debug { "Downloading anime [anidbId=$animeId] via web view to determine if it is pending addition or if it has been deleted." }

        val responseBody = webViewDownloader.download(animeId.toString()) {
            deadEntriesAccess.addDeadEntry(it, metaDataProviderConfig)
        }

        if (responseBody.eitherNullOrBlank()) return

        val responseCheckerResult = AnidbResponseStatusChecker(
            responseBody = responseBody,
            metaDataProviderConfig = webViewMetaDataProviderConfig,
        ).checkStatus()

        when (responseCheckerResult) {
            ADDITION_PENDING -> {
                val pendingFile = appConfig.workingDir(metaDataProviderConfig).resolve("$animeId.${ANIDB_PENDING_FILE_SUFFIX}")
                ANIDB_PENDING_FILE_INDICATOR.writeToFile(pendingFile, true)
            }
            else -> throw IllegalStateException("Type result [${responseCheckerResult}] after web view download for id [anidbId=$animeId].")
        }
    }

    @KoverIgnore
    private suspend fun wait() {
        excludeFromTestContext(metaDataProviderConfig) {
            delay(random(2000, 2500).toDuration(MILLISECONDS))
        }
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [AnidbCrawler]
         * @since 1.0.0
         */
        val instance: AnidbCrawler by lazy { AnidbCrawler() }

        /**
         * Suffix for pending files which have been downloaded, but cannot be converted.
         * @since 7.0.0
         */
        const val ANIDB_PENDING_FILE_SUFFIX: String = "pending"

        /**
         * Indicator for pending files which have been downloaded, but cannot be converted.
         * @since 8.0.0
         */
        const val ANIDB_PENDING_FILE_INDICATOR: String = ">pending<"
    }
}