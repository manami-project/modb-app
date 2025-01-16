package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.httpclient.RequestBody
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import java.net.URL
import kotlin.test.Test

internal class AnilistHttpClientTest {

    @Nested
    inner class GetTests {

        @Test
        fun `successfully loads an entry and retrieves a token upon first call`() {
            runBlocking {
                // given
                var delegateCallTimes = 0
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        delegateCallTimes++
                        return when (delegateCallTimes) {
                            1 -> HttpResponse(
                                code = 403,
                                body = "token outdated".toByteArray(),
                            )
                            else -> HttpResponse(
                                code = 200,
                                body = "success".toByteArray(),
                            )
                        }
                    }
                }

                var tokenHasBeenRetrieved = false
                val testAnilistDefaultTokenRetriever = object : AnilistTokenRetriever {
                    override suspend fun retrieveToken(): AnilistToken {
                        tokenHasBeenRetrieved = true
                        return AnilistToken(
                            cookie = "cookie-value",
                            csrfToken = "csrf-value",
                        )
                    }
                }

                var tokenHasBeenSet = false
                val testAnilistDefaultTokenRepository = object: AnilistTokenRepository {
                    override var token: AnilistToken
                        get() = AnilistToken(
                            cookie = "cookie-value",
                            csrfToken = "csrf-value",
                        )
                        set(_) { tokenHasBeenSet = true }

                }

                val client = AnilistHttpClient(
                    delegate = testHttpClient,
                    anilistTokenRetriever = testAnilistDefaultTokenRetriever,
                    anilistTokenRepository = testAnilistDefaultTokenRepository,
                )

                // when
                val result = client.get(url = URI("http://localhost").toURL())

                // then
                assertThat(tokenHasBeenRetrieved).isTrue()
                assertThat(tokenHasBeenSet).isTrue()
                assertThat(delegateCallTimes).isEqualTo(2)
                assertThat(result.bodyAsText).isEqualTo("success")
                assertThat(result.code).isEqualTo(200)
            }
        }

        @Test
        fun `updates token if http response code is 403`() {
            runBlocking {
                // given
                var delegateCallTimes = 0
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        delegateCallTimes++
                        return when (delegateCallTimes) {
                            1 -> HttpResponse(
                                code = 403,
                                body = "token outdated".toByteArray(),
                            )
                            else -> HttpResponse(
                                code = 200,
                                body = "success".toByteArray(),
                            )
                        }
                    }
                }

                var tokenHasBeenRetrieved = false
                val testAnilistDefaultTokenRetriever = object : AnilistTokenRetriever {
                    override suspend fun retrieveToken(): AnilistToken {
                        tokenHasBeenRetrieved = true
                        return AnilistToken(
                            cookie = "updated-cookie-value",
                            csrfToken = "updated-csrf-value",
                        )
                    }
                }

                val testAnilistDefaultTokenRepository = object: AnilistTokenRepository {
                    private var currentToken = AnilistToken("initial-cookie-value", "initial-csrf-token")

                    override var token: AnilistToken
                        get() = currentToken
                        set(value) { currentToken = value }

                }

                val client = AnilistHttpClient(
                    delegate = testHttpClient,
                    anilistTokenRetriever = testAnilistDefaultTokenRetriever,
                    anilistTokenRepository = testAnilistDefaultTokenRepository,
                )

                // when
                val result = client.get(url = URI("http://localhost").toURL())

                // then
                assertThat(tokenHasBeenRetrieved).isTrue()
                assertThat(delegateCallTimes).isEqualTo(2)
                assertThat(result.bodyAsText).isEqualTo("success")
                assertThat(result.code).isEqualTo(200)
                assertThat(testAnilistDefaultTokenRepository.token.cookie).isEqualTo("updated-cookie-value")
                assertThat(testAnilistDefaultTokenRepository.token.csrfToken).isEqualTo("updated-csrf-value")
            }
        }

        @Test
        fun `doesn't trigger token renewal if status code is 200`() {
            runBlocking {
                // given
                var delegateCallTimes = 0
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        delegateCallTimes++
                        return HttpResponse(
                            code = 200,
                            body = "success".toByteArray(),
                        )
                    }
                }

                val testAnilistDefaultTokenRetriever = object : AnilistTokenRetriever {
                    override suspend fun retrieveToken(): AnilistToken = shouldNotBeInvoked()
                }

                val testAnilistDefaultTokenRepository = object: AnilistTokenRepository {
                    override var token: AnilistToken
                        get() = AnilistToken(
                            cookie = "cookie-value",
                            csrfToken = "csrf-value",
                        )
                        set(_) { shouldNotBeInvoked() }

                }

                val client = AnilistHttpClient(
                    delegate = testHttpClient,
                    anilistTokenRetriever = testAnilistDefaultTokenRetriever,
                    anilistTokenRepository = testAnilistDefaultTokenRepository,
                )

                // when
                val result = client.get(url = URI("http://localhost").toURL())

                // then
                assertThat(delegateCallTimes).isEqualTo(1)
                assertThat(result.bodyAsText).isEqualTo("success")
                assertThat(result.code).isEqualTo(200)
            }
        }
    }

    @Nested
    inner class PostTests {

        @Test
        fun `successfully loads an entry and retrieves a token upon first call`() {
            runBlocking {
                // given
                var delegateCallTimes = 0
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun post(
                        url: URL,
                        requestBody: RequestBody,
                        headers: Map<String, Collection<String>>
                    ): HttpResponse {
                        delegateCallTimes++
                        return when (delegateCallTimes) {
                            1 -> HttpResponse(
                                code = 403,
                                body = "token outdated".toByteArray(),
                            )
                            else -> HttpResponse(
                                code = 200,
                                body = "success".toByteArray(),
                            )
                        }
                    }
                }

                var tokenHasBeenRetrieved = false
                val testAnilistDefaultTokenRetriever = object : AnilistTokenRetriever {
                    override suspend fun retrieveToken(): AnilistToken {
                        tokenHasBeenRetrieved = true
                        return AnilistToken(
                            cookie = "cookie-value",
                            csrfToken = "csrf-value",
                        )
                    }
                }

                var tokenHasBeenSet = false
                val testAnilistDefaultTokenRepository = object: AnilistTokenRepository {
                    override var token: AnilistToken
                        get() = AnilistToken(
                            cookie = "cookie-value",
                            csrfToken = "csrf-value",
                        )
                        set(_) { tokenHasBeenSet = true }

                }

                val client = AnilistHttpClient(
                    delegate = testHttpClient,
                    anilistTokenRetriever = testAnilistDefaultTokenRetriever,
                    anilistTokenRepository = testAnilistDefaultTokenRepository,
                )

                // when
                val result = client.post(url = URI("http://localhost").toURL(), requestBody = RequestBody(EMPTY, EMPTY))

                // then
                assertThat(tokenHasBeenRetrieved).isTrue()
                assertThat(tokenHasBeenSet).isTrue()
                assertThat(delegateCallTimes).isEqualTo(2)
                assertThat(result.bodyAsText).isEqualTo("success")
                assertThat(result.code).isEqualTo(200)
            }
        }

        @Test
        fun `updates token if http response code is 403`() {
            runBlocking {
                // given
                var delegateCallTimes = 0
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun post(
                        url: URL,
                        requestBody: RequestBody,
                        headers: Map<String, Collection<String>>
                    ): HttpResponse {
                        delegateCallTimes++
                        return when (delegateCallTimes) {
                            1 -> HttpResponse(
                                code = 403,
                                body = "token outdated".toByteArray(),
                            )
                            else -> HttpResponse(
                                code = 200,
                                body = "success".toByteArray(),
                            )
                        }
                    }
                }

                var tokenHasBeenRetrieved = false
                val testAnilistDefaultTokenRetriever = object : AnilistTokenRetriever {
                    override suspend fun retrieveToken(): AnilistToken {
                        tokenHasBeenRetrieved = true
                        return AnilistToken(
                            cookie = "updated-cookie-value",
                            csrfToken = "updated-csrf-value",
                        )
                    }
                }

                val testAnilistDefaultTokenRepository = object: AnilistTokenRepository {
                    private var currentToken = AnilistToken("initial-cookie-value", "initial-csrf-token")

                    override var token: AnilistToken
                        get() = currentToken
                        set(value) { currentToken = value }

                }

                val client = AnilistHttpClient(
                    delegate = testHttpClient,
                    anilistTokenRetriever = testAnilistDefaultTokenRetriever,
                    anilistTokenRepository = testAnilistDefaultTokenRepository,
                )

                // when
                val result = client.post(url = URI("http://localhost").toURL(), requestBody = RequestBody(EMPTY, EMPTY))

                // then
                assertThat(tokenHasBeenRetrieved).isTrue()
                assertThat(delegateCallTimes).isEqualTo(2)
                assertThat(result.bodyAsText).isEqualTo("success")
                assertThat(result.code).isEqualTo(200)
                assertThat(testAnilistDefaultTokenRepository.token.cookie).isEqualTo("updated-cookie-value")
                assertThat(testAnilistDefaultTokenRepository.token.csrfToken).isEqualTo("updated-csrf-value")
            }
        }

        @Test
        fun `doesn't trigger token renewal if status code is 200`() {
            runBlocking {
                // given
                var delegateCallTimes = 0
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun post(
                        url: URL,
                        requestBody: RequestBody,
                        headers: Map<String, Collection<String>>
                    ): HttpResponse {
                        delegateCallTimes++
                        return HttpResponse(
                            code = 200,
                            body = "success".toByteArray(),
                        )
                    }
                }

                val testAnilistDefaultTokenRetriever = object : AnilistTokenRetriever {
                    override suspend fun retrieveToken(): AnilistToken = shouldNotBeInvoked()
                }

                val testAnilistDefaultTokenRepository = object: AnilistTokenRepository {
                    override var token: AnilistToken
                        get() = AnilistToken(
                            cookie = "cookie-value",
                            csrfToken = "csrf-value",
                        )
                        set(_) { shouldNotBeInvoked() }

                }

                val client = AnilistHttpClient(
                    delegate = testHttpClient,
                    anilistTokenRetriever = testAnilistDefaultTokenRetriever,
                    anilistTokenRepository = testAnilistDefaultTokenRepository,
                )

                // when
                val result = client.post(url = URI("http://localhost").toURL(), requestBody = RequestBody(EMPTY, EMPTY))

                // then
                assertThat(delegateCallTimes).isEqualTo(1)
                assertThat(result.bodyAsText).isEqualTo("success")
                assertThat(result.code).isEqualTo(200)
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = AnilistHttpClient.instance

                // when
                val result = AnilistHttpClient.instance

                // then
                assertThat(result).isExactlyInstanceOf(AnilistHttpClient::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}
