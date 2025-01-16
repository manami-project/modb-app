package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.httpclient.RequestBody
import java.net.URI
import java.net.URL

/**
 * Creates all necessary header parameter for an anilist GraphQL request.
 * @since 1.0.0
 */
public object AnilistHeaderCreator {

    /**
     * Creates all headers necessary for an anilist GraphQL request.
     * @since 1.0.0
     * @param requestBody The request body being sent. It's necessary for content-length parameter.
     * @param referer The referer [URL]. **Default**: _https://anilist.co_
     * @return A [Map] of header parameter. Key is the name of the parameter itself and value is a list of values for this specific header parameter.
     */
    public fun createAnilistHeaders(requestBody: RequestBody, referer: URL = URI("https://anilist.co").toURL()): Map<String, List<String>> {
        val url = "https://anilist.co"

        return mapOf(
            "authority" to listOf(url),
            "method" to listOf("POST"),
            "path" to listOf("/graphql"),
            "scheme" to listOf("https"),
            "accept" to listOf("*/*"),
            "accept-language" to listOf("en-US;q=0.9", "en;q=0.8"),
            "content-length" to listOf(requestBody.body.length.toString()),
            "content-type" to listOf(requestBody.mediaType),
            "origin" to listOf(url),
            "schema" to listOf("default"),
            "cookie" to listOf(AnilistDefaultTokenRepository.token.cookie),
            "x-csrf-token" to listOf(AnilistDefaultTokenRepository.token.csrfToken),
            "referer" to listOf(referer.toString())
        )
    }
}