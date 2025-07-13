package io.github.manamiproject.modb.core.httpclient

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern
import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.httpclient.HttpProtocol.HTTP_1_1
import io.github.manamiproject.modb.test.*
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.Protocol.HTTP_2
import okhttp3.internal.http.HTTP_PERM_REDIRECT
import okio.Timeout
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.SocketTimeoutException
import java.net.URI
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.test.Test


internal class DefaultHttpClientKtTest : MockServerTestCase<WireMockServer> by WireMockServerCreator() {

    @Nested
    inner class ConstructorTests {

        @Test
        fun `throws exception if the list of http protocol versions is empty`() {
            // when
            val result = assertThrows<IllegalArgumentException> {
                DefaultHttpClient(
                    protocols = mutableListOf(),
                    isTestContext = true,
                )
            }

            // then
            assertThat(result).hasMessage("Requires at least one http protocol version.")
        }
    }

    @Nested
    inner class GetTests {

        @Test
        fun `successful retrieve response`() {
            runBlocking {
                // given
                val path = "anime/1535"
                val httpResponseCode = 200
                val body = "Successful"

                serverInstance.stubFor(
                    get(urlPathEqualTo("/$path")).willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(httpResponseCode)
                                .withBody(body)
                    )
                )

                val url = URI("http://localhost:$port/$path").toURL()
                val client = DefaultHttpClient(
                    isTestContext = true,
                )

                // when
                val result = client.get(url)

                // then
                assertThat(result.code).isEqualTo(httpResponseCode)
                assertThat(result.bodyAsString()).isEqualTo(body)
            }
        }

        @Test
        fun `receive an error`() {
            runBlocking {
                // given
                val path = "anime/1535"
                val httpResponseCode = 400
                val body = "Bad request"

                serverInstance.stubFor(
                    get(urlPathEqualTo("/$path")).willReturn(
                        aResponse()
                            .withHeader("Content-Type", "text/plain")
                            .withStatus(httpResponseCode)
                            .withBody(body)
                    )
                )

                val url = URI("http://localhost:$port/$path").toURL()
                val client = DefaultHttpClient(
                    isTestContext = true,
                )

                // when
                val result = client.get(url)

                // then
                assertThat(result.code).isEqualTo(httpResponseCode)
                assertThat(result.bodyAsString()).isEqualTo(body)
            }
        }

        @Test
        fun `headers can be overridden - override User-Agent`() {
            runBlocking {
                // given
                val header = mapOf(
                    "User-Agent" to listOf("Test-Agent")
                )

                serverInstance.stubFor(
                    get(urlPathEqualTo("/test")).willReturn(
                        aResponse()
                            .withStatus(200)
                    )
                )

                val client = DefaultHttpClient(
                    isTestContext = true,
                )

                // when
                client.get(URI("http://localhost:$port/test").toURL(), header)

                // then
                serverInstance.verify(
                getRequestedFor(urlEqualTo("/test"))
                    .withHeader("User-Agent", equalTo("Test-Agent"))
                )
            }
        }

        @Test
        fun `add additional header to GET request`() {
            runBlocking {
                // given
                val header = mapOf(
                    "Additional-Header" to listOf("Some value")
                )

                serverInstance.stubFor(
                    get(urlPathEqualTo("/test")).willReturn(
                        aResponse()
                            .withStatus(200)
                    )
                )

                val client = DefaultHttpClient(
                    isTestContext = true,
                )

                // when
                client.get(
                    url = URI("http://localhost:$port/test").toURL(),
                    headers = header,
                )

                // then
                serverInstance.verify(
                    getRequestedFor(urlEqualTo("/test"))
                        .withHeader("Additional-Header", equalTo("Some value"))
                )
            }
        }

        @Test
        fun `multiple values of a header are joined by a comma`() {
            runBlocking {
                // given
                serverInstance.stubFor(
                    get(urlPathEqualTo("/test")).willReturn(
                        aResponse()
                            .withStatus(200)
                    )
                )

                val headers = mapOf("multi-value-key" to listOf("value1", "value2"))

                val client = DefaultHttpClient(
                    isTestContext = true,
                )

                // when
                client.get(
                    url = URI("http://localhost:$port/test").toURL(),
                    headers = headers,
                )

                // then
                serverInstance.verify(
                    getRequestedFor(urlEqualTo("/test"))
                        .withHeader("multi-value-key", equalTo("value1,value2"))
                )
            }
        }

        @ParameterizedTest
        @ValueSource(ints = [500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511, 520, 521, 522, 523, 524, 525, 526, 527, 529, 530, 561, 598, 599, 425, 429])
        fun `performs retry for a variety of status codes`(value: Int) {

            runBlocking {
                // given
                val path = "anime/1535"

                serverInstance.stubFor(
                    get(urlPathEqualTo("/$path"))
                        .inScenario("Fail until last retry")
                        .whenScenarioStateIs(STARTED)
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(value)
                                .withBody(EMPTY)
                        )
                        .willSetStateTo("Retry")
                )
                serverInstance.stubFor(
                    get(urlPathEqualTo("/$path"))
                        .inScenario("Fail until last retry")
                        .whenScenarioStateIs("Retry")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(200)
                                .withBody(EMPTY)
                        )
                )

                val url = URI("http://localhost:$port/$path").toURL()

                val client = DefaultHttpClient(
                    isTestContext = true,
                )

                // when
                val result = client.get(url)

                // then
                assertThat(result.code).isEqualTo(200)
            }
        }

        @Test
        fun `status code - initial execution fails, all retries fail except the last retry which is successful`() {
            runBlocking {
                // given
                val path = "anime/1535"

                serverInstance.stubFor(
                    get(urlPathEqualTo("/$path"))
                        .inScenario("Fail until last retry")
                        .whenScenarioStateIs(STARTED)
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(429)
                                .withBody(EMPTY)
                        )
                        .willSetStateTo("Retry 1")
                )
                serverInstance.stubFor(
                    get(urlPathEqualTo("/$path"))
                        .inScenario("Fail until last retry")
                        .whenScenarioStateIs("Retry 1")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(429)
                                .withBody(EMPTY)
                        )
                        .willSetStateTo("Retry 2")
                )
                serverInstance.stubFor(
                    get(urlPathEqualTo("/$path"))
                        .inScenario("Fail until last retry")
                        .whenScenarioStateIs("Retry 2")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(429)
                                .withBody(EMPTY)
                        )
                        .willSetStateTo("Retry 3")
                )
                serverInstance.stubFor(
                    get(urlPathEqualTo("/$path"))
                        .inScenario("Fail until last retry")
                        .whenScenarioStateIs("Retry 3")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(429)
                                .withBody(EMPTY)
                        )
                        .willSetStateTo("Retry 4")
                )
                serverInstance.stubFor(
                    get(urlPathEqualTo("/$path"))
                        .inScenario("Fail until last retry")
                        .whenScenarioStateIs("Retry 4")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(429)
                                .withBody(EMPTY)
                        )
                        .willSetStateTo("Retry 5")
                )
                serverInstance.stubFor(
                    get(urlPathEqualTo("/$path"))
                        .inScenario("Fail until last retry")
                        .whenScenarioStateIs("Retry 5")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(200)
                                .withBody(EMPTY)
                        )
                )

                val url = URI("http://localhost:$port/$path").toURL()

                val client = DefaultHttpClient(
                    isTestContext = true,
                )

                // when
                val result = client.get(url)

                // then
                assertThat(result.code).isEqualTo(200)
            }
        }

        @Test
        fun `status code - throws exception, because it still fails after max attempts retries`() {
            runBlocking {
                // given
                val path = "anime/1535"

                serverInstance.stubFor(
                    get(urlPathEqualTo("/$path")).willReturn(
                        aResponse()
                            .withHeader("Content-Type", "text/plain")
                            .withStatus(429)
                            .withBody(EMPTY)
                    )
                )

                val url = URI("http://localhost:$port/$path").toURL()
                val client = DefaultHttpClient(
                    isTestContext = true,
                )

                // when
                val result = exceptionExpected<FailedAfterRetryException> {
                    client.get(url)
                }

                // then
                assertThat(result).hasMessage("Execution failed despite [5] retry attempts. Last invocation of [GET http://localhost:$port/anime/1535] returned http status code [429]")
                assertThat(result.cause).hasNoCause()
            }
        }

        @Test
        fun `throwable - exception without a retry case is thrown as-is`() {
            runBlocking {
                // given
                val url = URI("http://localhost:$port/test").toURL()
                val exception = UnknownHostException("invoked by test")

                val testOkHttpClient = Call.Factory { throw exception }

                val client = DefaultHttpClient(
                    isTestContext = true,
                    okhttpClient = testOkHttpClient,
                )

                // when
                val result = exceptionExpected<UnknownHostException> {
                    client.get(
                        url = url,
                    )
                }

                assertThat(result).hasMessage("invoked by test")
            }
        }

        @Test
        fun `throwable - performs a retry for a SocketTimeoutException`() {
            runBlocking {
                // given
                val path = "anime/1535"

                serverInstance.stubFor(
                    get(urlPathEqualTo("/$path"))
                        .inScenario("SocketTimeoutException triggers retry")
                        .whenScenarioStateIs(STARTED)
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(200)
                                .withFixedDelay(6000)
                        )
                        .willSetStateTo("Retry")
                )
                serverInstance.stubFor(
                    get(urlPathEqualTo("/$path"))
                        .inScenario("SocketTimeoutException triggers retry")
                        .whenScenarioStateIs("Retry")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(200)
                                .withBody("Success")
                        )
                )

                val url = URI("http://localhost:$port/$path").toURL()
                val client = DefaultHttpClient(
                    isTestContext = true,
                    okhttpClient = OkHttpClient.Builder()
                        .retryOnConnectionFailure(true)
                        .connectTimeout(1L, SECONDS)
                        .readTimeout(5L, SECONDS)
                        .build(),
                )

                // when
                val result = client.get(url)

                // then
                assertThat(result.code).isEqualTo(200)
                assertThat(result.bodyAsString()).isEqualTo("Success")
            }
        }

        @Test
        fun `throwable - re-throws SocketTimeoutException after maximum of attempts has been reached`() {
            runBlocking {
                serverInstance.stubFor(
                    get(urlPathEqualTo("/test")).willReturn(
                        aResponse()
                            .withStatus(200)
                            .withFixedDelay(3000)
                    )
                )

                val client = DefaultHttpClient(
                    isTestContext = true,
                    okhttpClient = OkHttpClient().newBuilder()
                        .writeTimeout(1500, MILLISECONDS)
                        .readTimeout(1500, MILLISECONDS)
                        .build(),
                )

                // when
                val result = exceptionExpected<SocketTimeoutException> {
                    client.get(
                        url = URI("http://localhost:$port/test").toURL(),
                    )
                }

                // then
                assertThat(result.message).isIn(
                    "Read timed out",
                    "timeout",
                )
            }
        }

        @Test
        fun `status code 103 - leads to removing HTTP 2 from protocols and performs a retry`() {
            runBlocking {
                // given
                val url = URI("http://localhost:$port/anime/1535").toURL()

                val testCall103 = object : Call {
                    override fun cancel() = shouldNotBeInvoked()
                    override fun clone(): Call = shouldNotBeInvoked()
                    override fun enqueue(responseCallback: Callback) = shouldNotBeInvoked()
                    override fun isCanceled(): Boolean = shouldNotBeInvoked()
                    override fun isExecuted(): Boolean = shouldNotBeInvoked()
                    override fun request(): Request = shouldNotBeInvoked()
                    override fun timeout(): Timeout = shouldNotBeInvoked()
                    override fun execute(): Response = Response.Builder()
                        .protocol(HTTP_2)
                        .message(EMPTY)
                        .request(
                            Request.Builder()
                                .url(url)
                                .build()
                        )
                        .code(103)
                        .build()
                }

                val testCall200 = object : Call {
                    override fun cancel() = shouldNotBeInvoked()
                    override fun clone(): Call = shouldNotBeInvoked()
                    override fun enqueue(responseCallback: Callback) = shouldNotBeInvoked()
                    override fun isCanceled(): Boolean = shouldNotBeInvoked()
                    override fun isExecuted(): Boolean = shouldNotBeInvoked()
                    override fun request(): Request = shouldNotBeInvoked()
                    override fun timeout(): Timeout = shouldNotBeInvoked()
                    override fun execute(): Response = Response.Builder()
                        .protocol(Protocol.HTTP_1_1)
                        .message(EMPTY)
                        .request(
                            Request.Builder()
                                .url(url)
                                .build()
                        )
                        .code(200)
                        .build()
                }

                var currentAttempt = 0
                val testOkHttpClient = Call.Factory {
                    currentAttempt++
                    when (currentAttempt) {
                        1 -> testCall103
                        else -> testCall200
                    }
                }

                val client = DefaultHttpClient(
                    okhttpClient = testOkHttpClient,
                    isTestContext = true,
                )

                // when
                val result = client.get(url)

                // then
                assertThat(result.code).isEqualTo(200)
                val protocols = client.javaClass.getDeclaredField("protocols")
                protocols.isAccessible = true
                val value = protocols.get(client) as (ArrayList<*>)
                assertThat(value).containsExactlyInAnyOrder(HTTP_1_1)
            }
        }

        @Test
        fun `executes higher order functions before and after retry`() {
            runBlocking {
                // given
                val path = "anime/1535"

                serverInstance.stubFor(
                    get(urlPathEqualTo("/$path"))
                        .inScenario("single retry")
                        .whenScenarioStateIs(STARTED)
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(429)
                        )
                        .willSetStateTo("Retry")
                )
                serverInstance.stubFor(
                    get(urlPathEqualTo("/$path"))
                        .inScenario("single retry")
                        .whenScenarioStateIs("Retry")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(200)
                                .withBody("Success")
                        )
                )

                val url = URI("http://localhost:$port/$path").toURL()

                var executeBeforeHasBeenInvoked = false
                val testExecuteBefore = { executeBeforeHasBeenInvoked = true }

                var executeAfterHasBeenInvoked = false
                val testExecuteAfter = { executeAfterHasBeenInvoked = true }

                val client = DefaultHttpClient(
                    isTestContext = true,
                    retryBehavior = RetryBehavior().addCases(
                        HttpResponseRetryCase(
                            executeBefore = testExecuteBefore,
                            executeAfter = testExecuteAfter,
                        ) { it.isNotOk() },
                    ),
                )

                // when
                client.get(url)

                // then
                assertThat(executeBeforeHasBeenInvoked).isTrue()
                assertThat(executeAfterHasBeenInvoked).isTrue()
            }
        }
    }

    @Nested
    inner class PostTests {

        @Test
        fun `successfully retrieve response`() {
            runBlocking {
                // given
                val path = "graphql"
                val httpResponseCode = 200
                val body = "{ \"key\": \"some-value\" }"

                serverInstance.stubFor(
                    post(urlPathEqualTo("/$path")).willReturn(
                        aResponse()
                            .withStatus(httpResponseCode)
                            .withBody("{{request.body}}")
                    )
                )

                val url = URI("http://localhost:$port/$path").toURL()
                val client = DefaultHttpClient(
                    isTestContext = true,
                )

                // when
                val result = client.post(
                    url = url,
                    headers = mapOf("test-header" to listOf("headervalue")),
                    requestBody = RequestBody(
                        mediaType = APPLICATION_JSON,
                        body = body,
                    ),
                )

                // then
                assertThat(result.code).isEqualTo(httpResponseCode)
                assertThat(result.bodyAsString()).isEqualTo(body)
            }
        }

        @Test
        fun `receive an error`() {
            runBlocking {
                // given
                val path = "anime/1535"
                val httpResponseCode = 400
                val body = "Bad request"

                serverInstance.stubFor(
                    post(urlPathEqualTo("/$path")).willReturn(
                        aResponse()
                            .withStatus(httpResponseCode)
                            .withBody(body)
                    )
                )

                val url = URI("http://localhost:$port/$path").toURL()
                val client = DefaultHttpClient(
                    isTestContext = true,
                )

                // when
                val result = client.post(
                    url = url,
                    headers = mapOf("test-header" to listOf("headervalue")),
                    requestBody = RequestBody(
                        mediaType = APPLICATION_JSON,
                        body = body,
                    )
                )

                // then
                assertThat(result.code).isEqualTo(httpResponseCode)
                assertThat(result.bodyAsString()).isEqualTo(body)
            }
        }

        @Test
        fun `headers can be overridden - override User-Agent`() {
            runBlocking {
                // given
                val header = mapOf(
                    "User-Agent" to listOf("Test-Agent")
                )

                serverInstance.stubFor(
                    post(urlPathEqualTo("/test")).willReturn(
                        aResponse()
                            .withStatus(200)
                    )
                )

                val client = DefaultHttpClient(
                    isTestContext = true,
                )

                // when
                client.post(
                    url = URI("http://localhost:$port/test").toURL(),
                    headers = header,
                    requestBody = RequestBody(
                        mediaType = APPLICATION_JSON,
                        body = "{ \"key\": \"some-value\" }"
                    )
                )

                // then
                serverInstance.verify(
                    postRequestedFor(urlEqualTo("/test"))
                        .withHeader("User-Agent", equalTo("Test-Agent"))
                )
            }
        }

        @Test
        fun `send accept encoding gzip for POST requests`() {
            runBlocking {
                // given
                serverInstance.stubFor(
                    post(urlPathEqualTo("/test")).willReturn(
                        aResponse()
                            .withStatus(200)
                    )
                )

                val client = DefaultHttpClient(
                    isTestContext = true,
                )

                // when
                client.post(
                    url = URI("http://localhost:$port/test").toURL(),
                    requestBody = RequestBody(
                        mediaType = APPLICATION_JSON,
                        body = "{ \"id\": 1 }"
                    )
                )

                // then
                serverInstance.verify(
                    postRequestedFor(urlEqualTo("/test"))
                        .withHeader("accept-encoding", equalTo("gzip"))
                )
            }
        }

        @Test
        fun `headers can be overridden - override content-type`() {
            runBlocking {
                // given
                serverInstance.stubFor(
                    post(urlPathEqualTo("/test")).willReturn(
                        aResponse()
                            .withStatus(200)
                    )
                )

                val client = DefaultHttpClient(
                    isTestContext = true,
                )

                // when
                client.post(
                    url = URI("http://localhost:$port/test").toURL(),
                    requestBody = RequestBody(
                        mediaType = "application/xml",
                        body = "<element>content<element>"
                    )
                )

                // then
                serverInstance.verify(
                    postRequestedFor(urlEqualTo("/test"))
                        .withHeader("content-type", equalTo("application/xml"))
                )
            }
        }

        @Test
        fun `add additional header to POST request`() {
            runBlocking {
                // given
                val header = mapOf(
                    "Additional-Header" to listOf("Some value")
                )

                serverInstance.stubFor(
                    post(urlPathEqualTo("/test")).willReturn(
                        aResponse()
                            .withStatus(200)
                    )
                )

                val client = DefaultHttpClient(
                    isTestContext = true,
                )

                // when
                client.post(
                    url = URI("http://localhost:$port/test").toURL(),
                    headers = header,
                    requestBody = RequestBody(
                        mediaType = APPLICATION_JSON,
                        body = "{ \"id\": 1 }"
                    )
                )

                // then
                serverInstance.verify(
                    postRequestedFor(urlEqualTo("/test"))
                        .withHeader("Additional-Header", equalTo("Some value"))
                )
            }
        }

        @Test
        fun `multiple values of a header are joined by a comma`() {
            runBlocking {
                // given
                serverInstance.stubFor(
                    post(urlPathEqualTo("/test")).willReturn(
                        aResponse()
                            .withStatus(200)
                    )
                )

                val headers = mapOf("multi-value-key" to listOf("value1", "value2"))

                val client = DefaultHttpClient(
                    isTestContext = true,
                )

                // when
                client.post(
                    url = URI("http://localhost:$port/test").toURL(),
                    requestBody = RequestBody(APPLICATION_JSON, "{ \"property\": \"value\" }"),
                    headers = headers,
                )

                // then
                serverInstance.verify(
                    postRequestedFor(urlEqualTo("/test"))
                        .withHeader("multi-value-key", equalTo("value1,value2"))
                )
            }
        }

        @Test
        fun `body is sent correctly for POST request`() {
            runBlocking {
                // given
                serverInstance.stubFor(
                    post(urlPathEqualTo("/test")).willReturn(
                        aResponse()
                            .withStatus(200)
                    )
                )

                val client = DefaultHttpClient(
                    isTestContext = true,
                )

                // when
                client.post(
                    url = URI("http://localhost:$port/test").toURL(),
                    requestBody = RequestBody(
                        mediaType = APPLICATION_JSON,
                        body = "{ \"property\": \"value\" }"
                    )
                )

                // then
                serverInstance.verify(
                    postRequestedFor(urlEqualTo("/test"))
                        .withRequestBody(EqualToJsonPattern("{ \"property\": \"value\" }", true, true))
                )
            }
        }

        @Test
        fun `throws exception if media type of the request body is blank`() {
            // given
            val client = DefaultHttpClient(
                isTestContext = true,
            )
            val requestBody = RequestBody(
                mediaType = EMPTY,
                body = "{ \"property\": \"value\" }",
            )

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                client.post(
                    url = URI("http://localhost:$port/test").toURL(),
                    requestBody = requestBody,
                )
            }

            // then
            assertThat(result).hasMessage("MediaType must not be blank.")
        }

        @Test
        fun `throws exception if the body of the request body is blank`() {
            // given
            val client = DefaultHttpClient(
                isTestContext = true,
            )
            val requestBody = RequestBody(
                mediaType = APPLICATION_JSON,
                body = EMPTY,
            )

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                client.post(
                    url = URI("http://localhost:$port/test").toURL(),
                    requestBody = requestBody,
                )
            }

            // then
            assertThat(result).hasMessage("The request's body must not be blank.")
        }

        @ParameterizedTest
        @ValueSource(ints = [500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511, 520, 521, 522, 523, 524, 525, 526, 527, 529, 530, 561, 598, 599, 425, 429])
        fun `performs retry for a variety of status codes`(value: Int) {
            runBlocking {
                // given
                val path = "graphql"
                val body = "{ \"key\": \"some-value\" }"

                serverInstance.stubFor(
                    post(urlPathEqualTo("/$path"))
                        .inScenario("Fail until last retry")
                        .whenScenarioStateIs(STARTED)
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(value)
                                .withBody(EMPTY)
                        )
                        .willSetStateTo("Retry")
                )
                serverInstance.stubFor(
                    post(urlPathEqualTo("/$path"))
                        .inScenario("Fail until last retry")
                        .whenScenarioStateIs("Retry")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(200)
                                .withBody(EMPTY)
                        )
                )

                val url = URI("http://localhost:$port/$path").toURL()
                val client = DefaultHttpClient(
                    isTestContext = true,
                )

                // when
                val result = client.post(
                    url = url,
                    headers = mapOf("test-header" to listOf("headervalue")),
                    requestBody = RequestBody(
                        mediaType = APPLICATION_JSON,
                        body = body,
                    )
                )

                // then
                assertThat(result.code).isEqualTo(200)
            }
        }

        @Test
        fun `status code - initial execution fails, all retries fail except the last retry which is successful`() {
            runBlocking {
                // given
                val path = "graphql"
                val body = "{ \"key\": \"some-value\" }"

                serverInstance.stubFor(
                    post(urlPathEqualTo("/$path"))
                        .inScenario("Fail until last retry")
                        .whenScenarioStateIs(STARTED)
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(429)
                                .withBody(EMPTY)
                        )
                        .willSetStateTo("Retry 1")
                )
                serverInstance.stubFor(
                    post(urlPathEqualTo("/$path"))
                        .inScenario("Fail until last retry")
                        .whenScenarioStateIs("Retry 1")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(429)
                                .withBody(EMPTY)
                        )
                        .willSetStateTo("Retry 2")
                )
                serverInstance.stubFor(
                    post(urlPathEqualTo("/$path"))
                        .inScenario("Fail until last retry")
                        .whenScenarioStateIs("Retry 2")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(429)
                                .withBody(EMPTY)
                        )
                        .willSetStateTo("Retry 3")
                )
                serverInstance.stubFor(
                    post(urlPathEqualTo("/$path"))
                        .inScenario("Fail until last retry")
                        .whenScenarioStateIs("Retry 3")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(429)
                                .withBody(EMPTY)
                        )
                        .willSetStateTo("Retry 4")
                )
                serverInstance.stubFor(
                    post(urlPathEqualTo("/$path"))
                        .inScenario("Fail until last retry")
                        .whenScenarioStateIs("Retry 4")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(429)
                                .withBody(EMPTY)
                        )
                        .willSetStateTo("Retry 5")
                )
                serverInstance.stubFor(
                    post(urlPathEqualTo("/$path"))
                        .inScenario("Fail until last retry")
                        .whenScenarioStateIs("Retry 5")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(200)
                                .withBody(EMPTY)
                        )
                )

                val url = URI("http://localhost:$port/$path").toURL()
                val client = DefaultHttpClient(
                    isTestContext = true,
                )

                // when
                val result = client.post(
                    url = url,
                    headers = mapOf("test-header" to listOf("headervalue")),
                    requestBody = RequestBody(
                        mediaType = APPLICATION_JSON,
                        body = body,
                    )
                )

                // then
                assertThat(result.code).isEqualTo(200)
            }
        }

        @Test
        fun `status code - throws exception, because it still fails after max attempts retries`() {
            runBlocking {
                // given
                val path = "graphql"
                val body = "{ \"key\": \"some-value\" }"

                serverInstance.stubFor(
                    post(urlPathEqualTo("/$path")).willReturn(
                        aResponse()
                            .withHeader("Content-Type", "text/plain")
                            .withStatus(429)
                            .withBody(EMPTY)
                    )
                )

                val url = URI("http://localhost:$port/$path").toURL()
                val client = DefaultHttpClient(
                    isTestContext = true,
                )

                // when
                val result = exceptionExpected<FailedAfterRetryException> {
                    client.post(
                        url = url,
                        headers = mapOf("test-header" to listOf("headervalue")),
                        requestBody = RequestBody(
                            mediaType = APPLICATION_JSON,
                            body = body,
                        )
                    )
                }

                // then
                assertThat(result).hasMessage("Execution failed despite [5] retry attempts. Last invocation of [POST http://localhost:$port/graphql] returned http status code [429]")
                assertThat(result.cause).hasNoCause()
            }
        }

        @Test
        fun `throwable - exception without a retry case is thrown as-is`() {
            runBlocking {
                // given
                val url = URI("http://localhost:$port/test").toURL()
                val exception = UnknownHostException("invoked by test")
                val body = "{ \"key\": \"some-value\" }"

                val testOkHttpClient = Call.Factory { throw exception }
                val client = DefaultHttpClient(
                    isTestContext = true,
                    okhttpClient = testOkHttpClient,
                )

                // when
                val result = exceptionExpected<UnknownHostException> {
                    client.post(
                        url = url,
                        headers = mapOf("test-header" to listOf("headervalue")),
                        requestBody = RequestBody(
                            mediaType = APPLICATION_JSON,
                            body = body,
                        ),
                    )
                }

                assertThat(result).hasMessage("invoked by test")
            }
        }

        @Test
        fun `throwable - performs a retry for a SocketTimeoutException`() {
            runBlocking {
                // given
                val path = "anime/1535"

                serverInstance.stubFor(
                    post(urlPathEqualTo("/$path"))
                        .inScenario("SocketTimeoutException triggers retry")
                        .whenScenarioStateIs(STARTED)
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(200)
                                .withFixedDelay(10000)
                        )
                        .willSetStateTo("Retry")
                )
                serverInstance.stubFor(
                    post(urlPathEqualTo("/$path"))
                        .inScenario("SocketTimeoutException triggers retry")
                        .whenScenarioStateIs("Retry")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(200)
                                .withBody("Success")
                        )
                )

                val url = URI("http://localhost:$port/$path").toURL()
                val body = "{ \"key\": \"some-value\" }"
                val client = DefaultHttpClient(
                    isTestContext = true,
                    okhttpClient = OkHttpClient.Builder()
                        .retryOnConnectionFailure(true)
                        .connectTimeout(1L, SECONDS)
                        .readTimeout(5L, SECONDS)
                        .build(),
                )

                // when
                val result = client.post(
                    url = url,
                    headers = mapOf("test-header" to listOf("headervalue")),
                    requestBody = RequestBody(
                        mediaType = APPLICATION_JSON,
                        body = body,
                    ),
                )

                // then
                assertThat(result.code).isEqualTo(200)
                assertThat(result.bodyAsString()).isEqualTo("Success")
            }
        }

        @Test
        fun `throwable - re-throws SocketTimeoutException after maximum of attempts has been reached`() {
            runBlocking {
                // given
                val path = "anime/1535"

                serverInstance.stubFor(
                    post(urlPathEqualTo("/$path")).willReturn(
                        aResponse()
                            .withStatus(200)
                            .withFixedDelay(3000)
                    )
                )

                val url = URI("http://localhost:$port/$path").toURL()
                val body = "{ \"key\": \"some-value\" }"
                val client = DefaultHttpClient(
                    isTestContext = true,
                    okhttpClient = OkHttpClient().newBuilder()
                        .writeTimeout(1500, MILLISECONDS)
                        .readTimeout(1500, MILLISECONDS)
                        .build(),
                )

                // when
                val result = exceptionExpected<SocketTimeoutException> {
                    client.post(
                        url = url,
                        headers = mapOf("test-header" to listOf("headervalue")),
                        requestBody = RequestBody(
                            mediaType = APPLICATION_JSON,
                            body = body,
                        ),
                    )
                }

                // then
                assertThat(result.message).isIn(
                    "Read timed out",
                    "timeout",
                )
            }
        }

        @Test
        fun `status code 103 - leads to removing HTTP 2 from protocols and performs a retry`() {
            runBlocking {
                // given
                val body = "{ \"key\": \"some-value\" }"
                val url = URI("http://localhost:$port/graphql").toURL()

                val testCall103 = object : Call {
                    override fun cancel() = shouldNotBeInvoked()
                    override fun clone(): Call = shouldNotBeInvoked()
                    override fun enqueue(responseCallback: Callback) = shouldNotBeInvoked()
                    override fun isCanceled(): Boolean = shouldNotBeInvoked()
                    override fun isExecuted(): Boolean = shouldNotBeInvoked()
                    override fun request(): Request = shouldNotBeInvoked()
                    override fun timeout(): Timeout = shouldNotBeInvoked()
                    override fun execute(): Response = Response.Builder()
                        .protocol(HTTP_2)
                        .message(EMPTY)
                        .request(
                            Request.Builder()
                                .url(url)
                                .build()
                        )
                        .code(103)
                        .build()
                }

                val testCall200 = object : Call {
                    override fun cancel() = shouldNotBeInvoked()
                    override fun clone(): Call = shouldNotBeInvoked()
                    override fun enqueue(responseCallback: Callback) = shouldNotBeInvoked()
                    override fun isCanceled(): Boolean = shouldNotBeInvoked()
                    override fun isExecuted(): Boolean = shouldNotBeInvoked()
                    override fun request(): Request = shouldNotBeInvoked()
                    override fun timeout(): Timeout = shouldNotBeInvoked()
                    override fun execute(): Response = Response.Builder()
                        .protocol(Protocol.HTTP_1_1)
                        .message(EMPTY)
                        .request(
                            Request.Builder()
                                .url(url)
                                .build()
                        )
                        .code(200)
                        .build()
                }

                var currentAttempt = 0
                val testOkHttpClient = Call.Factory {
                    currentAttempt++
                    when (currentAttempt) {
                        1 -> testCall103
                        else -> testCall200
                    }
                }

                val client = DefaultHttpClient(
                    okhttpClient = testOkHttpClient,
                    isTestContext = true,
                )

                // when
                val result = client.post(
                    url = url,
                    headers = mapOf("test-header" to listOf("headervalue")),
                    requestBody = RequestBody(
                        mediaType = APPLICATION_JSON,
                        body = body,
                    )
                )

                // then
                assertThat(result.code).isEqualTo(200)
                val protocols = client.javaClass.getDeclaredField("protocols")
                protocols.isAccessible = true
                val value = protocols.get(client) as (ArrayList<*>)
                assertThat(value).containsExactlyInAnyOrder(HTTP_1_1)
            }
        }

        @Test
        fun `executes higher order functions before and after retry`() {
            runBlocking {
                // given
                val path = "anime/1535"

                serverInstance.stubFor(
                    post(urlPathEqualTo("/$path"))
                        .inScenario("single retry")
                        .whenScenarioStateIs(STARTED)
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(429)
                        )
                        .willSetStateTo("Retry")
                )
                serverInstance.stubFor(
                    post(urlPathEqualTo("/$path"))
                        .inScenario("single retry")
                        .whenScenarioStateIs("Retry")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(200)
                                .withBody("Success")
                        )
                )

                val url = URI("http://localhost:$port/$path").toURL()
                val body = "{ \"key\": \"some-value\" }"

                var executeBeforeHasBeenInvoked = false
                val testExecuteBefore = { executeBeforeHasBeenInvoked = true }

                var executeAfterHasBeenInvoked = false
                val testExecuteAfter = { executeAfterHasBeenInvoked = true }

                val client = DefaultHttpClient(
                    isTestContext = true,
                    retryBehavior = RetryBehavior().addCases(
                        HttpResponseRetryCase(
                            executeBefore = testExecuteBefore,
                            executeAfter = testExecuteAfter,
                        ) { it.isNotOk() },
                    ),
                )

                // when
                client.post(
                    url = url,
                    headers = mapOf("test-header" to listOf("headervalue")),
                    requestBody = RequestBody(
                        mediaType = APPLICATION_JSON,
                        body = body,
                    ),
                )

                // then
                assertThat(executeBeforeHasBeenInvoked).isTrue()
                assertThat(executeAfterHasBeenInvoked).isTrue()
            }
        }
    }

    @Nested
    inner class CustomRedirectTests {

        @Test
        fun `successful retrieve response`() {
            runBlocking {
                // given
                val path = "anime/1535"
                val httpResponseCode = 200
                val body = "Successful"

                serverInstance.stubFor(
                    get(urlPathEqualTo("/$path")).willReturn(
                        aResponse()
                            .withHeader("Content-Type", "text/plain")
                            .withStatus(httpResponseCode)
                            .withBody(body)
                    )
                )

                val url = URI("http://localhost:$port/$path").toURL()
                val client = DefaultHttpClient(
                    useCustomRedirectInterceptor = true,
                    isTestContext = true,
                )

                // when
                val result = client.get(url)

                // then
                assertThat(result.code).isEqualTo(httpResponseCode)
                assertThat(result.bodyAsString()).isEqualTo(body)
            }
        }

        @ParameterizedTest
        @ValueSource(ints = [
            300,
            301,
            302,
            303,
            307,
            308,
        ])
        fun `uses custom redirect interceptor in case the initial response returns a respective status code`(value: Int) {
            runBlocking {
                // given
                val url = URI("http://localhost:$port/file.txt").toURL()

                serverInstance.stubFor(
                    get(urlPathEqualTo("/file.txt"))
                        .inScenario("Client follows redirect")
                        .whenScenarioStateIs(STARTED)
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withHeader("location", "http://localhost:$port/something/else/here.txt")
                                .withStatus(value)
                        )
                        .willSetStateTo("Redirect")
                )

                serverInstance.stubFor(
                    get(urlPathEqualTo("/something/else/here.txt"))
                        .inScenario("Client follows redirect")
                        .whenScenarioStateIs("Redirect")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(200)
                                .withBody("Success")
                        )
                )

                val client = DefaultHttpClient(
                    useCustomRedirectInterceptor = true,
                    isTestContext = true,
                )

                // when
                val result = client.get(url)

                // then
                assertThat(result.code).isEqualTo(200)
                assertThat(result.bodyAsString()).isEqualTo("Success")
            }
        }

        @ParameterizedTest
        @ValueSource(ints = [
            300,
            301,
            302,
            303,
            307,
            308,
        ])
        fun `correctly sets passes host header and all x- prefixed headers`(value: Int) {
            runBlocking {
                // given
                val url = URI("http://localhost:$port/file.txt").toURL()

                serverInstance.stubFor(
                    get(urlPathEqualTo("/file.txt"))
                        .inScenario("Client follows redirect")
                        .whenScenarioStateIs(STARTED)
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withHeader("location", "http://localhost:$port/something/else/here.txt")
                                .withHeader("x-test-1", "1")
                                .withHeader("x-some-more", "false")
                                .withStatus(value)
                        )
                        .willSetStateTo("Redirect")
                )

                serverInstance.stubFor(
                    get(urlPathEqualTo("/something/else/here.txt"))
                        .inScenario("Client follows redirect")
                        .withHeader("host", matching("localhost"))
                        .withHeader("x-test-1", matching("1"))
                        .withHeader("x-some-more", matching("false"))
                        .whenScenarioStateIs("Redirect")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withStatus(200)
                                .withBody("Success")
                        )
                )

                val client = DefaultHttpClient(
                    useCustomRedirectInterceptor = true,
                    isTestContext = true,
                )

                // when
                val result = client.get(
                    url = url,
                    headers = mapOf("host" to listOf("example.org")),
                )

                // then
                assertThat(result.code).isEqualTo(200)
                assertThat(result.bodyAsString()).isEqualTo("Success")
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = DefaultHttpClient.instance

                // when
                val result = DefaultHttpClient.instance

                // then
                assertThat(result).isExactlyInstanceOf(DefaultHttpClient::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}