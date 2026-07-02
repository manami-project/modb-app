package io.github.manamiproject.modb.app.network

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.httpclient.APPLICATION_JSON
import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.httpclient.RequestBody
import io.github.manamiproject.modb.core.httpclient.RetryCase
import io.github.manamiproject.modb.core.json.Json
import java.net.URI
import java.net.URL

internal class FlaresolverrHttpClient(
    private val appConfig: Config = AppConfig.instance,
    private val httpClient: HttpClient = DefaultHttpClient(isTestContext = appConfig.isTestContext()),
): HttpClient {

    override suspend fun post(
        url: URL,
        requestBody: RequestBody,
        headers: Map<String, Collection<String>>,
    ): HttpResponse {
        val postResponse = httpClient.post(
            url = URI("http://localhost:8191/v1").toURL(),
            requestBody = RequestBody(
                mediaType = "application/x-www-form-urlencoded",
                body = """
                        {
                          "cmd": "request.post",
                          "url": "$url",
                          "maxTimeout": 60000,
                          "postData": "${requestBody.body}"
                        }
                    """.trimIndent(),
            ),
        )
        val content = Json.parseJson<FlaresolverrResponse>(postResponse.bodyAsStream())!!
        return HttpResponse(
            code = content.solution.status,
            body = content.solution.response,
        )
    }

    override suspend fun get(
        url: URL,
        headers: Map<String, Collection<String>>,
    ): HttpResponse {
        val postResponse = httpClient.post(
            url = URI("http://localhost:8191/v1").toURL(),
            requestBody = RequestBody(
                mediaType = APPLICATION_JSON,
                body = """
                        {
                          "cmd": "request.get",
                          "url": "$url",
                          "maxTimeout": 60000
                        }
                    """.trimIndent(),
            ),
        )
        val content = Json.parseJson<FlaresolverrResponse>(postResponse.bodyAsStream())!!
        return HttpResponse(
            code = content.solution.status,
            body = content.solution.response,
        )
    }

    override fun addRetryCases(vararg retryCases: RetryCase): HttpClient {
        httpClient.addRetryCases(*retryCases)
        return this
    }

    companion object {
        /**
         * Singleton of [FlaresolverrHttpClient]
         * @since 1.0.0
         */
        val instance: FlaresolverrHttpClient by lazy { FlaresolverrHttpClient() }
    }
}

private data class FlaresolverrResponse(
    val solution: FlaresolverrResponseSolution = FlaresolverrResponseSolution(),
)

private data class FlaresolverrResponseSolution(
    val status: Int = 100,
    val response: String = EMPTY,
)