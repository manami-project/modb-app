package io.github.manamiproject.modb.app.crawlers.notify

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.crawlers.Crawler
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.dataset.DefaultDeadEntriesAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DOWNLOAD_CONTROL_STATE_FILE_SUFFIX
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.app.network.SuspendableHttpClient
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.logging.LoggerDelegate


/**
 * Implementation of [Crawler] for `notify.moe`.
 * Downloads the whole dataset and creates conv files.
 * @since 1.2.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property metaDataProviderConfig Configuration for a specific meta data provider.
 * @property httpClient Implementation of [HttpClient] which is used to download the whole dataset.
 * @property downloadControlStateAccessor Access to DCS files.
 * @property deadEntriesAccessor Access to dead entries files.
 */
class NotifyDatasetDownloadCrawler(
    private val appConfig: Config = AppConfig.instance,
    private val metaDataProviderConfig: MetaDataProviderConfig = NotifyDatasetDownloaderConfig,
    private val httpClient: HttpClient = SuspendableHttpClient.instance,
    private val downloadControlStateAccessor: DownloadControlStateAccessor = DefaultDownloadControlStateAccessor.instance,
    private val deadEntriesAccessor: DeadEntriesAccessor = DefaultDeadEntriesAccessor.instance,
): Crawler {

    override suspend fun start() {
        log.info { "Starting crawler for [${metaDataProviderConfig.hostname()}]." }

        val response = httpClient.get(metaDataProviderConfig.buildDataDownloadLink().toURL())

        if (!response.isOk()) {
            throw IllegalStateException("Unhandled response code [${response.code}].")
        }

        val idList = response.bodyAsText.split("\n")
            .chunked(2) {
                it.first() to it.last()
            }
            .filter { it.first.neitherNullNorBlank() && it.second.neitherNullNorBlank() }
            .map { (key, content) ->
                content.writeToFile(appConfig.workingDir(metaDataProviderConfig).resolve("$key.${metaDataProviderConfig.fileSuffix()}"))
                key
            }
            .toHashSet()

        val dcsFileIds = downloadControlStateAccessor.downloadControlStateDirectory(metaDataProviderConfig)
            .listRegularFiles("*.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")
            .map { it.fileName() }
            .map { it.remove(".$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX") }
            .toHashSet()

        (dcsFileIds - idList).forEach {
            deadEntriesAccessor.addDeadEntry(it, metaDataProviderConfig)
        }

        log.info { "Finished crawling data for [${metaDataProviderConfig.hostname()}]." }
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [NotifyDatasetDownloadCrawler]
         * @since 1.2.0
         */
        val instance: NotifyDatasetDownloadCrawler by lazy { NotifyDatasetDownloadCrawler() }
    }
}