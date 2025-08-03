package io.github.manamiproject.modb.kitsu

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_NETWORK
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.JsonDataExtractor
import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponseRetryCase
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.withContext

/**
 * Downloads anime data from kitsu.app
 * @since 1.0.0
 * @property metaDataProviderConfig Configuration for downloading data.
 * @property httpClient To actually download the anime data.
 */
public class KitsuDownloader(
    private val metaDataProviderConfig: MetaDataProviderConfig = KitsuConfig,
    private val httpClient: HttpClient = DefaultHttpClient(isTestContext = metaDataProviderConfig.isTestContext()).apply {
        addRetryCases(HttpResponseRetryCase { it.code == 400 })
    },
    private val extractor: DataExtractor = JsonDataExtractor,
) : Downloader {

    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String = withContext(LIMITED_NETWORK) {
        log.debug { "Downloading [kitsuId=$id]" }

        val response = httpClient.get(
            url = metaDataProviderConfig.buildDataDownloadLink(id).toURL(),
        )
        val responseBody = response.bodyAsString()

        check(responseBody.neitherNullNorBlank()) { "Response body was blank for [kitsuId=$id] with response code [${response.code}]" }

        return@withContext when(response.code) {
            200 -> {
                val data = extractor.extract(responseBody, mapOf(
                    "entries" to "$.meta.count",
                ))
                val entries = data.intOrDefault("entries")
                when (entries) {
                    0 -> {
                        onDeadEntry.invoke(id)
                        EMPTY
                    }
                    1 -> {
                        responseBody
                    }
                    else -> throw IllegalStateException("Anime with id [${id}] returned [$entries] entries.")
                }
            }
            404 -> {
                onDeadEntry.invoke(id)
                EMPTY
            }
            else -> throw IllegalStateException("Unable to determine the correct case for [kitsutId=$id], [responseCode=${response.code}]")
        }
    }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [KitsuDownloader]
         * @since 7.0.0
         */
        public val instance: KitsuDownloader by lazy { KitsuDownloader() }
    }
}