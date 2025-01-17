package io.github.manamiproject.modb.core.httpclient

import java.net.URL

/**
 * Defines a HTTP client.
 * @since 9.0.0
 */
public interface HttpClient {

    /**
     * Performs a HTTP POST request with a [RequestBody].
     * @since 9.0.0
     * @param url The actual [URL] that you want to call.
     * @param requestBody Contains the the payload as well as the media type which will be automatically applied as content-type header.
     * @param headers Optional header parameters. Entries of this map will either add or override header parameters.
     * The key is the name of the header parameter. The value is a list of values for the respective parameters. The values will be joined together.
     * **Default** is an empty [Map].
     * @return The server's response.
     */
    public suspend fun post(url: URL, requestBody: RequestBody, headers: Map<String, Collection<String>> = emptyMap()): HttpResponse

    /**
     * Performs a HTTP GET request.
     * @since 9.0.0
     * @param url The actual URL that you want to call.
     * @param headers Optional header parameters. Entries of this map will either add or override header parameters.
     * The key is the name of the header parameter. The value is a list of values for the respective parameters. The values will be joined together.
     * **Default** is an empty [Map].
     * @return The server's response.
     */
    public suspend fun get(url: URL, headers: Map<String, Collection<String>> = emptyMap()): HttpResponse
}