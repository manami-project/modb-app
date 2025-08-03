package io.github.manamiproject.modb.app.crawlers.anisearch

import io.github.manamiproject.modb.app.TestDataExtractor
import io.github.manamiproject.modb.app.TestHttpClient
import io.github.manamiproject.modb.app.TestMetaDataProviderConfig
import io.github.manamiproject.modb.app.TestNetworkController
import io.github.manamiproject.modb.app.crawlers.myanimelist.MyanimelistHighestIdDetectorConfig
import io.github.manamiproject.modb.app.network.NetworkController
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.httpclient.RetryCase
import io.github.manamiproject.modb.core.httpclient.ThrowableRetryCase
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.*
import kotlin.test.Test

internal class AnisearchLastPageDetectorTest {

    @Nested
    inner class ConstructorTests {

        @Test
        fun `adds RetryCases for ConnectException, UnknownHostException and NoRouteToHostException with restarting the NetworkController`() {
            runBlocking {
                // given
                val cases = mutableListOf<RetryCase>()
                val testHttpClient = object : HttpClient by TestHttpClient {
                    override fun addRetryCases(vararg retryCases: RetryCase): HttpClient {
                        cases.addAll(retryCases)
                        return this
                    }
                }

                var restartInvocations = 0
                val testNetworkController = object: NetworkController by TestNetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> {
                        restartInvocations++
                        return runBlocking { async { true } }
                    }
                }

                // when
                AnisearchLastPageDetector(
                    metaDataProviderConfig = TestMetaDataProviderConfig,
                    httpClient = testHttpClient,
                    networkController = testNetworkController,
                    extractor = TestDataExtractor,
                )

                // then
                assertThat(cases).hasSize(3)

                val connectException = cases.find { (it as ThrowableRetryCase).retryIf(ConnectException()) }
                assertThat(connectException).isNotNull()
                connectException!!.executeBefore.invoke()
                assertThat(restartInvocations).isEqualTo(1)

                val unknownHostException = cases.find { (it as ThrowableRetryCase).retryIf(UnknownHostException()) }
                assertThat(unknownHostException).isNotNull()
                unknownHostException!!.executeBefore.invoke()
                assertThat(restartInvocations).isEqualTo(2)

                val noRouteToHostException = cases.find { (it as ThrowableRetryCase).retryIf(NoRouteToHostException()) }
                assertThat(noRouteToHostException).isNotNull()
                noRouteToHostException!!.executeBefore.invoke()
                assertThat(restartInvocations).isEqualTo(3)
            }
        }
    }

    @Nested
    inner class DetectHighestIdTests {

        @Test
        fun `correctly extracts last page`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object : MetaDataProviderConfig by AnisearchLastPageDetectorConfig {
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:8080/highest-id")
                }

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override fun addRetryCases(vararg retryCases: RetryCase): HttpClient = this
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = loadTestResource<ByteArray>("crawler/anisearch/AnisearchLastPageDetectorTest/page-1.html"),
                    )
                }

                val anisearchLastPageDetector = AnisearchLastPageDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                    networkController = TestNetworkController,
                )

                // when
                val result = anisearchLastPageDetector.detectHighestId()

                // then
                assertThat(result).isEqualTo(190)
            }
        }

        @Test
        fun `throws exception, because the element couldn't be found`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object : MetaDataProviderConfig by MyanimelistHighestIdDetectorConfig {
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:8080/highest-id")
                }

                val content = """
                    <html>
                    </head>
                    </body>
                    </html>
                """.trimIndent()

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override fun addRetryCases(vararg retryCases: RetryCase): HttpClient = this
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = content,
                    )
                }

                val anisearchLastPageDetector = AnisearchLastPageDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                    networkController = TestNetworkController,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    anisearchLastPageDetector.detectHighestId()
                }

                // then
                assertThat(result).hasMessage("Unable to extract lastPageNavEntry.")
            }
        }

        @Test
        fun `directly throws exception if it's not one of the cases that restart the network controller`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object : MetaDataProviderConfig by MyanimelistHighestIdDetectorConfig {
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:8080/highest-id")
                }

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override fun addRetryCases(vararg retryCases: RetryCase): HttpClient = this
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        throw NullPointerException("junit test")
                    }
                }

                val anisearchLastPageDetector = AnisearchLastPageDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                    networkController = TestNetworkController,
                )

                // when
                val result = exceptionExpected<NullPointerException> {
                    anisearchLastPageDetector.detectHighestId()
                }

                // then
                assertThat(result).hasMessage("junit test")
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = AnisearchLastPageDetector.instance

                // when
                val result = AnisearchLastPageDetector.instance

                // then
                assertThat(result).isExactlyInstanceOf(AnisearchLastPageDetector::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}