package io.github.manamiproject.modb.app.network

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestHttpClient
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.httpclient.RequestBody
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import java.net.URL
import kotlin.test.Test

internal class SuspendableHttpClientTest {

    @Nested
    inner class GetTests {

        @Test
        fun `suspend GET call if network controller is inactive`() {
            runBlocking {
                // given
                val testAppConfig = object : Config by TestAppConfig {
                    override fun isTestContext(): Boolean = true
                }

                var delegationInvoked = false
                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun get(
                        url: URL,
                        headers: Map<String, Collection<String>>,
                    ): HttpResponse {
                        delegationInvoked = true
                        return HttpResponse(200, EMPTY.toByteArray())
                    }
                }

                var isNetworkActiveInvocations = 0
                val testNetWorkController = object : NetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> = shouldNotBeInvoked()
                    override fun isNetworkActive(): Boolean {
                        isNetworkActiveInvocations++
                        return isNetworkActiveInvocations == 4
                    }
                }

                val suspendableHttpClient = SuspendableHttpClient(
                    appConfig = testAppConfig,
                    httpClient = testHttpClient,
                    networkController = testNetWorkController,
                )

                // when
                suspendableHttpClient.get(URI("http://localhost").toURL())

                // then
                assertThat(delegationInvoked).isTrue()
                assertThat(isNetworkActiveInvocations).isEqualTo(4)
            }
        }
    }

    @Nested
    inner class PostTests {

        @Test
        fun `suspend POST call if network controller is inactive`() {
            runBlocking {
                // given
                val testAppConfig = object : Config by TestAppConfig {
                    override fun isTestContext(): Boolean = true
                }

                var delegationInvoked = false
                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun post(
                        url: URL,
                        requestBody: RequestBody,
                        headers: Map<String, Collection<String>>,
                    ): HttpResponse {
                        delegationInvoked = true
                        return HttpResponse(200, EMPTY.toByteArray())
                    }
                }

                var isNetworkActiveInvocations = 0
                val testNetWorkController = object : NetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> = shouldNotBeInvoked()
                    override fun isNetworkActive(): Boolean {
                        isNetworkActiveInvocations++
                        return isNetworkActiveInvocations == 4
                    }
                }

                val suspendableHttpClient = SuspendableHttpClient(
                    appConfig = testAppConfig,
                    httpClient = testHttpClient,
                    networkController = testNetWorkController,
                )

                // when
                suspendableHttpClient.post(URI("http://localhost").toURL(), RequestBody(EMPTY, EMPTY))

                // then
                assertThat(delegationInvoked).isTrue()
                assertThat(isNetworkActiveInvocations).isEqualTo(4)
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = SuspendableHttpClient.instance

                // when
                val result = SuspendableHttpClient.instance

                // then
                assertThat(result).isExactlyInstanceOf(SuspendableHttpClient::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}