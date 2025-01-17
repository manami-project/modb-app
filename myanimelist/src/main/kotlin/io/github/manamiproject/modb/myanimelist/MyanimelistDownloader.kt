package io.github.manamiproject.modb.myanimelist

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.ConfigRegistry
import io.github.manamiproject.modb.core.config.DefaultConfigRegistry
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.httpclient.*
import io.github.manamiproject.modb.core.httpclient.BrowserType.MOBILE
import io.github.manamiproject.modb.core.logging.LoggerDelegate

/**
 * Downloads anime data from myanimelist.net
 * @since 1.0.0
 * @param metaDataProviderConfig Configuration for downloading data.
 * @param httpClient To actually download the anime data.
 */
public class MyanimelistDownloader(
    private val metaDataProviderConfig: MetaDataProviderConfig = MyanimelistConfig,
    private val configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
    private val headerCreator: HeaderCreator = DefaultHeaderCreator(configRegistry = configRegistry),
    private val httpClient: HttpClient = DefaultHttpClient(isTestContext = metaDataProviderConfig.isTestContext()).apply {
        retryBehavior.addCases(
            RetryCase { it.code == 403 },
            RetryCase { it.code == 404 && it.bodyAsText.contains("was not found on this server.</p>") },
        )
    },
) : Downloader {

    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
        log.debug { "Downloading [myanimelistId=$id]" }

        val url = metaDataProviderConfig.buildDataDownloadLink(id).toURL()
        val response = httpClient.get(
            url = url,
            headers = headerCreator.createHeadersFor(
                url = url,
                browserType = MOBILE,
            ),
        )

        check(response.bodyAsText.neitherNullNorBlank()) { "Response body was blank for [myanimelistId=$id] with response code [${response.code}]" }

        return when(response.code) {
            200 -> response.bodyAsText
            404 -> checkDeadEntry(id, onDeadEntry, response.bodyAsText)
            else -> throw IllegalStateException("Unable to determine the correct case for [myanimelistId=$id], [responseCode=${response.code}]")
        }
    }

    private suspend fun checkDeadEntry(myanimelistId: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit, responseBody: String): String {
        return when {
            responseBody.contains("<title>404 Not Found - MyAnimeList.net") -> {
                onDeadEntry.invoke(myanimelistId)
                EMPTY
            }
            else -> throw IllegalStateException("Unknown 404 case for [myanimelistId=$myanimelistId]")
        }
    }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [MyanimelistDownloader]
         * @since 1.0.0
         */
        public val instance: MyanimelistDownloader by lazy { MyanimelistDownloader() }
    }
}