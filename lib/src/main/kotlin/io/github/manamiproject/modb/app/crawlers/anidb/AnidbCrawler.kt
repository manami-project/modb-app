package io.github.manamiproject.modb.app.crawlers.anidb

import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anidb.AnidbDownloader
import io.github.manamiproject.modb.anidb.AnidbDownloader.Companion.ANIDB_PENDING_FILE_INDICATOR
import io.github.manamiproject.modb.anidb.CrawlerDetectedException
import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.crawlers.Crawler
import io.github.manamiproject.modb.app.crawlers.IdRangeSelector
import io.github.manamiproject.modb.app.crawlers.IntegerBasedIdRangeSelector
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.dataset.DefaultDeadEntriesAccessor
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
 * Includes a hard coded random waiting time to reduce pressure on the meta data provider.
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property metaDataProviderConfig Configuration for a specific meta data provider.
 * @property deadEntriesAccess Access to dead entries files.
 * @property idRangeSelector Delivers the IDs to download.
 * @property httpClient To actually download the anime data.
 * @property downloader Downloader for a specific meta data provider.
 */
class AnidbCrawler(
    private val appConfig: Config = AppConfig.instance,
    private val metaDataProviderConfig: MetaDataProviderConfig = AnidbConfig,
    private val deadEntriesAccess: DeadEntriesAccessor = DefaultDeadEntriesAccessor.instance,
    private val idRangeSelector: IdRangeSelector<Int> = IntegerBasedIdRangeSelector(
        metaDataProviderConfig = metaDataProviderConfig,
        highestIdDetector = AnidbHighestIdDetector.instance,
    ),
    private val httpClient: HttpClient = SuspendableHttpClient(),
    private val downloader: Downloader = AnidbDownloader(
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
        httpClient.addRetryCases(ThrowableRetryCase(executeBefore = restart) { it is CrawlerDetectedException})
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

        val response = downloader.download(animeId.toString()) {
            deadEntriesAccess.addDeadEntry(it, metaDataProviderConfig)
        }

        when {
            response.neitherNullNorBlank() && response != ANIDB_PENDING_FILE_INDICATOR -> {
                val file = appConfig.workingDir(metaDataProviderConfig).resolve("$animeId.${metaDataProviderConfig.fileSuffix()}")
                response.writeToFile(file, true)
            }
            response == ANIDB_PENDING_FILE_INDICATOR -> {
                val pendingFile = appConfig.workingDir(metaDataProviderConfig).resolve("$animeId.${ANIDB_PENDING_FILE_SUFFIX}")
                response.writeToFile(pendingFile, true)
            }
        }
    }

    @KoverIgnore
    private suspend fun wait() {
        excludeFromTestContext(metaDataProviderConfig) {
            delay(random(1500, 2500).toDuration(MILLISECONDS))
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
    }
}