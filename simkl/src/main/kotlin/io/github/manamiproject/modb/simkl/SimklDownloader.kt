package io.github.manamiproject.modb.simkl

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.logging.LoggerDelegate

public class SimklDownloader(
    private val metaDataProviderConfig: MetaDataProviderConfig = SimklConfig,
    private val httpClient: HttpClient = DefaultHttpClient(isTestContext = metaDataProviderConfig.isTestContext()),
): Downloader {

    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
        log.debug { "Downloading [simklId=$id]" }

        val response = httpClient.get(metaDataProviderConfig.buildDataDownloadLink(id).toURL())

        check(response.bodyAsText.neitherNullNorBlank()) { "Response body was blank for [simklId=$id] with response code [${response.code}]" }

        return when(response.code) {
            200 -> response.bodyAsText
            else -> throw IllegalStateException("Unable to determine the correct case for [simklId=$id], [responseCode=${response.code}]")
        }
    }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [SimklDownloader]
         * @since 1.0.0
         */
        public val instance: SimklDownloader by lazy { SimklDownloader() }
    }
}