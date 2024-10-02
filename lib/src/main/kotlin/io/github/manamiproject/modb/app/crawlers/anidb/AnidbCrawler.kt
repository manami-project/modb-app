package io.github.manamiproject.modb.app.crawlers.anidb

import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anidb.AnidbDownloader
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
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.extensions.writeToFile
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
 * @property downloader Downloader for a specific meta data provider.
 */
class AnidbCrawler(
    private val appConfig: Config = AppConfig.instance,
    private val metaDataProviderConfig: MetaDataProviderConfig = AnidbConfig,
    private val deadEntriesAccess: DeadEntriesAccessor = DefaultDeadEntriesAccessor.instance,
    private val networkController: NetworkController = LinuxNetworkController.instance,
    private val idRangeSelector: IdRangeSelector<Int> = IntegerBasedIdRangeSelector(
        metaDataProviderConfig = metaDataProviderConfig,
        highestIdDetector = AnidbHighestIdDetector.instance,
    ),
    private val downloader: Downloader = AnidbDownloader(
        httpClient = SuspendableHttpClient(),
    ),
): Crawler {

    override suspend fun start() {
        log.info { "Starting crawler for [${metaDataProviderConfig.hostname()}]." }

        val idDownloadList = idRangeSelector.idDownloadList()

        when {
            idDownloadList.isEmpty() -> log.info { "No IDs left for [${metaDataProviderConfig.hostname()}] crawler to download." }
            else -> startDownload(idDownloadList)
        }

        log.info { "Finished crawling data for [${metaDataProviderConfig.hostname()}]." }
    }

    private suspend fun startDownload(idDownloadList: List<Int>) = repeat(idDownloadList.size) { index ->
        val animeId = idDownloadList[index]
        val file = appConfig.workingDir(metaDataProviderConfig).resolve("$animeId.${metaDataProviderConfig.fileSuffix()}")

        wait()

        log.debug { "Downloading ${index+1}/${idDownloadList.size}: [anidbId=$animeId]" }

        val response = try {
            downloader.download(animeId.toString()) {
                deadEntriesAccess.addDeadEntry(it, metaDataProviderConfig)
            }
        } catch (e: Throwable) {
            when(e) {
                is CrawlerDetectedException,
                is ConnectException,
                is UnknownHostException,
                is NoRouteToHostException -> {
                    networkController.restartAsync().await()
                    downloader.download(animeId.toString()) {
                        deadEntriesAccess.addDeadEntry(it, metaDataProviderConfig)
                    }
                }
                else -> throw e
            }
        }

        if (response.neitherNullNorBlank()) {
            response.writeToFile(file, true)
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
    }
}