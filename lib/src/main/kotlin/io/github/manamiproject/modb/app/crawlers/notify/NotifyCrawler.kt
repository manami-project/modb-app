package io.github.manamiproject.modb.app.crawlers.notify

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.crawlers.Crawler
import io.github.manamiproject.modb.app.crawlers.IdRangeSelector
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.app.network.SuspendableHttpClient
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.excludeFromTestContext
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.random
import io.github.manamiproject.modb.notify.NotifyDownloader
import kotlinx.coroutines.delay
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration

/**
 * Implementation of [Crawler] for `notify.moe`.
 * Uses [NotifyIdRangeSelector] to determine which data to download.
 * Includes a hard coded random waiting time to reduce pressure on the meta data provider.
 * @since 1.0.0
 */
class NotifyCrawler(
    private val appConfig: Config = AppConfig.instance,
    private val metaDataProviderConfig: MetaDataProviderConfig,
    private val idRangeSelector: IdRangeSelector<String> = NotifyIdRangeSelector(metaDataProviderConfig = metaDataProviderConfig),
    private val downloader: Downloader = NotifyDownloader(metaDataProviderConfig = metaDataProviderConfig, httpClient = SuspendableHttpClient()),
    private val downloadControlStateAccessor: DownloadControlStateAccessor = DefaultDownloadControlStateAccessor.instance,
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

    private suspend fun startDownload(idDownloadList: List<String>) = repeat(idDownloadList.size) { index ->
        val animeId = idDownloadList[index]
        val file = appConfig.workingDir(metaDataProviderConfig).resolve("$animeId.${metaDataProviderConfig.fileSuffix()}")

        log.debug { "Downloading ${index+1}/${idDownloadList.size}: [notifyId=$animeId]" }

        wait()

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

    private companion object {
        private val log by LoggerDelegate()
    }
}