package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_NETWORK
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.extensions.remove
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.XmlDataExtractor
import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.withContext

private const val CSRF_TOKEN_PREFIX = "window.al_token"

/**
 * Retrieves a valid token.
 * @since 1.0.0
 * @param metaDataProviderConfig Configuration for retrieving the token.
 * @param httpClient To download the site from which the token will be extracted
 */
public class AnilistDefaultTokenRetriever(
    private val metaDataProviderConfig: MetaDataProviderConfig = AnilistDefaultTokenRetrieverConfig,
    private val httpClient: HttpClient = DefaultHttpClient(isTestContext=metaDataProviderConfig.isTestContext()),
    private val extractor: DataExtractor = XmlDataExtractor,
): AnilistTokenRetriever {

    override suspend fun retrieveToken(): AnilistToken = withContext(LIMITED_NETWORK) {
        log.info { "Fetching token for anilist." }

        val response = httpClient.get(
            url = metaDataProviderConfig.buildDataDownloadLink().toURL(),
        )

        val cookie = extractCookie(response)
        val csrfToken = extractCsrfToken(response)

        return@withContext AnilistToken(
            cookie = cookie,
            csrfToken = csrfToken
        )
    }

    private fun extractCookie(response: HttpResponse): String {
        return response.headers["set-cookie"]
            ?.map { it.split(';') }
            ?.map { it.first() }
            ?.sorted()
            ?.joinToString("; ") ?: throw IllegalStateException("Unable to extract cookie.")
    }

    private suspend fun extractCsrfToken(response: HttpResponse): String = withContext(LIMITED_CPU) {
        require(response.bodyAsText.neitherNullNorBlank()) { "Response body must not be empty" }

        val data = extractor.extract(response.bodyAsText, selection = mapOf(
            "script" to "//script[contains(node(), $CSRF_TOKEN_PREFIX)]/node()"
        ))

        return@withContext if (data.notFound("script")) {
            throw IllegalStateException("Unable to extract CSRF token.")
        } else {
            data.listNotNull<String>("script")
                .first { it.startsWith(CSRF_TOKEN_PREFIX) }
                .remove("$CSRF_TOKEN_PREFIX = \"")
                .remove("\";")
                .trim()
        }
    }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [AnilistDefaultTokenRetriever]
         * @since 6.1.0
         */
        public val instance: AnilistDefaultTokenRetriever by lazy { AnilistDefaultTokenRetriever() }
    }
}