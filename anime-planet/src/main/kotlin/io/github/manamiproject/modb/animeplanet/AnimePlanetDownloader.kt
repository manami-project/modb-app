package io.github.manamiproject.modb.animeplanet

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_NETWORK
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.withContext

/**
 * Downloads anime data from anime-planet.com
 * @since 1.0.0
 * @param metaDataProviderConfig Configuration for downloading data.
 * @param httpClient To actually download the anime data.
 */
public class AnimePlanetDownloader(
    private val metaDataProviderConfig: MetaDataProviderConfig = AnimePlanetConfig,
    private val httpClient: HttpClient = DefaultHttpClient(isTestContext = metaDataProviderConfig.isTestContext()),
) : Downloader {

    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String = withContext(LIMITED_NETWORK) {
        log.debug { "Downloading [animePlanetId=$id]" }

        val response = httpClient.get(
            url = metaDataProviderConfig.buildDataDownloadLink(id).toURL(),
            headers = mapOf("host" to listOf("www.${metaDataProviderConfig.hostname()}")),
        )

        check(response.bodyAsText.neitherNullNorBlank()) { "Response body was blank for [animePlanetId=$id] with response code [${response.code}]" }

        if (response.bodyAsText.contains("You searched for") && response.bodyAsText.contains("...but we couldn't find anything.")) {
            onDeadEntry.invoke(id)
            return@withContext EMPTY
        }

        return@withContext when(response.code) {
            200 -> response.bodyAsText
            else -> throw IllegalStateException("Unable to determine the correct case for [animePlanetId=$id], [responseCode=${response.code}]")
        }
    }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [AnimePlanetDownloader]
         * @since 1.0.0
         */
        public val instance: AnimePlanetDownloader by lazy { AnimePlanetDownloader() }
    }
}