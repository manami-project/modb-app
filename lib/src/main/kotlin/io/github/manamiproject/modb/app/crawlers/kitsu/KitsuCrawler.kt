package io.github.manamiproject.modb.app.crawlers.kitsu

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.crawlers.Crawler
import io.github.manamiproject.modb.app.crawlers.IdRangeSelector
import io.github.manamiproject.modb.app.crawlers.IntegerBasedIdRangeSelector
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.dataset.DefaultDeadEntriesAccessor
import io.github.manamiproject.modb.app.network.SuspendableHttpClient
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.excludeFromTestContext
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.random
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.kitsu.KitsuDownloader
import kotlinx.coroutines.delay
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration

/**
 * Implementation of [Crawler] for `kitsu.app`.
 * Uses [IntegerBasedIdRangeSelector] to determine which data to download.
 * Includes a hard coded random waiting time to reduce pressure on the meta data provider.
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property metaDataProviderConfig Configuration for a specific meta data provider.
 * @property deadEntriesAccess Access to dead entries files.
 * @property idRangeSelector Delivers the IDs to download.
 * @property downloader Downloader for a specific meta data provider.
 */
class KitsuCrawler @KoverIgnore constructor(
    private val appConfig: Config = AppConfig.instance,
    private val metaDataProviderConfig: MetaDataProviderConfig = KitsuConfig,
    private val deadEntriesAccess: DeadEntriesAccessor = DefaultDeadEntriesAccessor.instance,
    private val idRangeSelector: IdRangeSelector<Int> = IntegerBasedIdRangeSelector(
        metaDataProviderConfig = metaDataProviderConfig,
        highestIdDetector = KitsuHighestIdDetector.instance,
    ),
    private val downloader: Downloader = KitsuDownloader(
        metaDataProviderConfig = metaDataProviderConfig,
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

        log.debug { "Downloading ${index+1}/${idDownloadList.size}: [kitsutId=$animeId]" }

        val response = downloader.download(animeId.toString()) {
            deadEntriesAccess.addDeadEntry(animeId.toString(), metaDataProviderConfig)
        }

        if (response.neitherNullNorBlank()) {
            response.writeToFile(file, true)
        }
    }

    @KoverIgnore
    private suspend fun wait() {
        excludeFromTestContext(metaDataProviderConfig) {
            delay(random(1200, 1500).toDuration(MILLISECONDS))
        }
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [KitsuCrawler]
         * @since 1.0.0
         */
        val instance: KitsuCrawler by lazy { KitsuCrawler() }
    }
}