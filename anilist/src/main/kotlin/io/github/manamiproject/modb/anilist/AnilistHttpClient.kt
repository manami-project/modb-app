package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.httpclient.RequestBody
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import java.net.URL

public class AnilistHttpClient(
    private val delegate: HttpClient = DefaultHttpClient(),
    private val anilistTokenRetriever: AnilistTokenRetriever = AnilistDefaultTokenRetriever.instance,
    private val anilistTokenRepository: AnilistTokenRepository = AnilistDefaultTokenRepository,
): HttpClient by delegate {

    override suspend fun post(url: URL, requestBody: RequestBody, headers: Map<String, Collection<String>>): HttpResponse {
        val initialResponse = delegate.post(url, requestBody, headers)

        if (initialResponse.code != 403) {
            return initialResponse
        }

        return delegate.post(url, requestBody, renewTokenInHeaders(headers))
    }

    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
        val initialResponse = delegate.get(url, headers)

        if (initialResponse.code != 403) {
            return initialResponse
        }

        return delegate.get(url, renewTokenInHeaders(headers))
    }

    private suspend fun renewTokenInHeaders(headers: Map<String, Collection<String>>): Map<String, Collection<String>> {
        log.warn { "Anilist responds with 403. Refreshing token." }
        anilistTokenRepository.token = anilistTokenRetriever.retrieveToken()
        val modifiedHeaders = HashMap(headers).apply {
            put("cookie", listOf(AnilistDefaultTokenRepository.token.cookie))
            put("x-csrf-token", listOf(AnilistDefaultTokenRepository.token.csrfToken))
        }
        log.info { "Token has been renewed" }

        return modifiedHeaders
    }


    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [AnilistHttpClient]
         * @since 1.0.0
         */
        public val instance: AnilistHttpClient by lazy { AnilistHttpClient() }
    }
}