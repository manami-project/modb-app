package io.github.manamiproject.modb.app.crawlers.anilist

import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.anilist.AnilistHeaderCreator
import io.github.manamiproject.modb.anilist.AnilistHttpClient
import io.github.manamiproject.modb.app.crawlers.HighestIdDetector
import io.github.manamiproject.modb.app.extensions.checkedBody
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.httpclient.APPLICATION_JSON
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.RequestBody
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.loadResource
import io.github.manamiproject.modb.core.logging.LoggerDelegate

/**
 * Detects the highest anime id based on the page showing latest additions.
 * @since 1.0.0
 * @property metaDataProviderConfig Configuration for a specific meta data provider.
 * @property httpClient Implementation of [HttpClient] which is used to retrieve the highest id.
 */
class AnilistHighestIdDetector(
    private val metaDataProviderConfig: MetaDataProviderConfig = AnilistConfig,
    private val httpClient: HttpClient = AnilistHttpClient.instance
): HighestIdDetector {

    override suspend fun detectHighestId(): Int {
        log.info { "Fetching highest id for [${metaDataProviderConfig.hostname()}]." }

        val requestUrl = metaDataProviderConfig.buildDataDownloadLink()
        val body = loadResource("crawler/anilist/AnilistHighestIdDetector/highest_id_request_body.graphql")
        val requestBody = RequestBody(
            mediaType = APPLICATION_JSON,
            body = body,
        )

        val response = httpClient.post(
            url = requestUrl.toURL(),
            headers = AnilistHeaderCreator.createAnilistHeaders(
                requestBody = requestBody,
                referer = requestUrl.toURL(),
            ),
            requestBody = requestBody,
        ).checkedBody(this::class)

        val anilistResponse = Json.parseJson<AnilistResponse>(response)!!

        return anilistResponse.data.Page.media.first().id
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [AnilistHighestIdDetector]
         * @since 1.0.0
         */
        val instance: AnilistHighestIdDetector by lazy { AnilistHighestIdDetector() }
    }
}

private data class AnilistResponse(
    val data: AnilistResponseData
)

private data class AnilistResponseData(
    val Page: AnilistResponsePage
)

private data class AnilistResponsePage(
    val media: List<PageEntry>
)

private data class PageEntry(
    val id: Int
)