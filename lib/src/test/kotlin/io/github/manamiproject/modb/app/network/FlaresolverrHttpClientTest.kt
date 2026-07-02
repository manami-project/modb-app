package io.github.manamiproject.modb.app.network

import io.github.manamiproject.modb.app.TestHttpClient
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.httpclient.HttpResponseRetryCase
import io.github.manamiproject.modb.core.httpclient.RequestBody
import io.github.manamiproject.modb.core.httpclient.RetryCase
import io.github.manamiproject.modb.core.httpclient.ThrowableRetryCase
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import java.net.URL
import kotlin.test.Test


internal class FlaresolverrHttpClientTest {

    @Nested
    inner class GetTests {

        @Test
        fun `correctly creates get request`() {
            runTest {
                // given
                var invokedUrl = EMPTY
                var invokedRequestBody = EMPTY
                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun post(url: URL, requestBody: RequestBody, headers: Map<String, Collection<String>>): HttpResponse {
                        invokedUrl = url.toString()
                        invokedRequestBody = requestBody.toString()
                        return HttpResponse(
                            code = 200,
                            body = "{}".toByteArray(),
                        )
                    }
                }
                val flaresolverrHttpClient = FlaresolverrHttpClient(httpClient = testHttpClient)

                // when
                flaresolverrHttpClient.get(URI("http://example.org").toURL())

                // then
                assertThat(invokedUrl).isEqualTo("http://localhost:8191/v1")
                assertThat(invokedRequestBody).isEqualTo("""
                    RequestBody(mediaType=application/json, body={
                      "cmd": "request.get",
                      "url": "http://example.org",
                      "maxTimeout": 60000
                    })
                """.trimIndent())
            }
        }
    }

    @Nested
    inner class PostTests {

        @Test
        fun `correctly creates post request`() {
            runTest {
                // given
                var invokedUrl = EMPTY
                var invokedRequestBody = EMPTY
                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun post(url: URL, requestBody: RequestBody, headers: Map<String, Collection<String>>): HttpResponse {
                        invokedUrl = url.toString()
                        invokedRequestBody = requestBody.toString()
                        return HttpResponse(
                            code = 200,
                            body = "{}".toByteArray(),
                        )
                    }
                }
                val flaresolverrHttpClient = FlaresolverrHttpClient(httpClient = testHttpClient)

                // when
                flaresolverrHttpClient.post(
                    url = URI("http://example.org").toURL(),
                    requestBody = RequestBody(
                        mediaType = "application/x-www-form-urlencoded",
                        body = "a=b&c=d"
                    ),
                )

                // then
                assertThat(invokedUrl).isEqualTo("http://localhost:8191/v1")
                assertThat(invokedRequestBody).isEqualTo("""
                    RequestBody(mediaType=application/x-www-form-urlencoded, body={
                      "cmd": "request.post",
                      "url": "http://example.org",
                      "maxTimeout": 60000,
                      "postData": "a=b&c=d"
                    })
                """.trimIndent())
            }
        }
    }

    @Nested
    inner class AddRetryCasesTests {

        @Test
        fun `adding a retry cases just delegates it to the internal HttpClient`() {
            // given
            val invocations = mutableListOf<RetryCase>()
            val testHttpClient = object : HttpClient by TestHttpClient {
                override fun addRetryCases(vararg retryCases: RetryCase): HttpClient {
                    invocations.addAll(retryCases)
                    return this
                }
            }
            val flaresolverrHttpClient = FlaresolverrHttpClient(httpClient = testHttpClient)

            val r1 = HttpResponseRetryCase { true }
            val r2 = ThrowableRetryCase { true }

            // when
            flaresolverrHttpClient.addRetryCases(r1, r2)

            // then
            assertThat(invocations).containsExactlyInAnyOrder(r1, r2)
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = FlaresolverrHttpClient.instance

                // when
                val result = FlaresolverrHttpClient.instance

                // then
                assertThat(result).isExactlyInstanceOf(FlaresolverrHttpClient::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}