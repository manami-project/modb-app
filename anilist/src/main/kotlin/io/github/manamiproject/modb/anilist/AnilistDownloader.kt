package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.httpclient.APPLICATION_JSON
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.RequestBody
import io.github.manamiproject.modb.core.loadResource
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.runBlocking

/**
 * Downloads anime data from anilist.co
 * @since 1.0.0
 * @param metaDataProviderConfig Configuration for downloading data.
 * @param httpClient To actually download the anime data.
 */
public class AnilistDownloader(
    private val metaDataProviderConfig: MetaDataProviderConfig = AnilistConfig,
    private val httpClient: HttpClient = AnilistHttpClient(),
) : Downloader {

    private val requestBody: String by lazy { runBlocking { loadResource("anime_download_request.graphql") } }

    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
        log.debug { "Downloading [anilistId=$id]" }

        val requestBody = RequestBody(
            mediaType = APPLICATION_JSON,
            body = requestBody.replace("<<ANIME_ID>>", id)
        )

        val requestHeaders = AnilistHeaderCreator.createAnilistHeaders(
            requestBody = requestBody,
            referer = metaDataProviderConfig.buildAnimeLink(id).toURL(),
        )

        val response = httpClient.post(
            url = metaDataProviderConfig.buildDataDownloadLink(id).toURL(),
            headers = requestHeaders,
            requestBody = requestBody,
        )

        check(response.bodyAsText.neitherNullNorBlank()) { "Response body was blank for [anilistId=$id] with response code [${response.code}]" }

        return when(response.code) {
            200 -> response.bodyAsText
            404 -> {
                onDeadEntry.invoke(id)
                EMPTY
            }
            else -> throw IllegalStateException("Unable to determine the correct case for [anilistId=$id], [responseCode=${response.code}]")
        }
    }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [AnilistDownloader]
         * @since 1.0.0
         */
        public val instance: AnilistDownloader by lazy { AnilistDownloader() }
    }
}