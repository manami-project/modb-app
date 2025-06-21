package io.github.manamiproject.modb.app.crawlers.anidb

import io.github.manamiproject.modb.anidb.CrawlerDetectedException
import io.github.manamiproject.modb.app.TestHttpClient
import io.github.manamiproject.modb.app.TestNetworkController
import io.github.manamiproject.modb.app.network.NetworkController
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
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

internal class AnidbHighestIdDetectorTest {

    @Nested
    inner class DetectHighestIdTests {

        @Test
        fun `correctly extracts highest id`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object : MetaDataProviderConfig by AnidbHighestIdDetectorConfig {
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:8080/highest-id")
                }

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = loadTestResource<ByteArray>("crawler/anidb/AnidbHighestIdDetectorTest/latest-anime.html"),
                    )
                }

                val anidbHighestIdDetector = AnidbHighestIdDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                    networkController = TestNetworkController,
                )

                // when
                val result = anidbHighestIdDetector.detectHighestId()

                // then
                assertThat(result).isEqualTo(18872)
            }
        }

        @Test
        fun `throws exception, because the element couldn't be found`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object : MetaDataProviderConfig by AnidbHighestIdDetectorConfig {
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:8080/highest-id")
                }

                val content = """
                    <html>
                    </head>
                    </body>
                    </html>
                """.trimIndent()

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = content,
                    )
                }

                val anidbHighestIdDetector = AnidbHighestIdDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                    networkController = TestNetworkController,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    anidbHighestIdDetector.detectHighestId()
                }

                // then
                assertThat(result).hasMessage("Unable to find highestId.")
            }
        }

        @Test
        fun `throws exception if value found is not an integer`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object : MetaDataProviderConfig by AnidbHighestIdDetectorConfig {
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:8080/highest-id")
                }

                val content = """
                    <html>
                    </head>
                    <body>
                        <div class="latest_anime">
                            <table class="animelist">
                                <td class="name"><a href="other">other</a></td>
                            </table>
                        </div>
                    </body>
                    </html>
                """.trimIndent()

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = content,
                    )
                }

                val anidbHighestIdDetector = AnidbHighestIdDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                    networkController = TestNetworkController,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    anidbHighestIdDetector.detectHighestId()
                }

                // then
                assertThat(result).hasMessage("Unable to extract highest id.")
            }
        }

        @Test
        fun `initiates a restart of the network controller if a ConnectException is thrown`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object : MetaDataProviderConfig by AnidbHighestIdDetectorConfig {
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:8080/highest-id")
                }

                var hasBeenInvoked = false
                val testNetworkController = object: NetworkController by TestNetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> {
                        hasBeenInvoked = true
                        return async { true }
                    }
                }

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        return if (hasBeenInvoked) {
                            HttpResponse(
                                code = 200,
                                body = loadTestResource<ByteArray>("crawler/anidb/AnidbHighestIdDetectorTest/latest-anime.html"),
                            )
                        } else {
                            throw ConnectException()
                        }
                    }
                }

                val anidbHighestIdDetector = AnidbHighestIdDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                    networkController = testNetworkController,
                )

                // when
                val result = anidbHighestIdDetector.detectHighestId()

                // then
                assertThat(hasBeenInvoked).isTrue()
                assertThat(result).isEqualTo(18872)
            }
        }

        @Test
        fun `initiates a restart of the network controller if a UnknownHostException is thrown`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object : MetaDataProviderConfig by AnidbHighestIdDetectorConfig {
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:8080/highest-id")
                }

                var hasBeenInvoked = false
                val testNetworkController = object: NetworkController by TestNetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> {
                        hasBeenInvoked = true
                        return async { true }
                    }
                }

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        return if (hasBeenInvoked) {
                            HttpResponse(
                                code = 200,
                                body = loadTestResource<ByteArray>("crawler/anidb/AnidbHighestIdDetectorTest/latest-anime.html"),
                            )
                        } else {
                            throw UnknownHostException()
                        }
                    }
                }

                val anidbHighestIdDetector = AnidbHighestIdDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                    networkController = testNetworkController,
                )

                // when
                val result = anidbHighestIdDetector.detectHighestId()

                // then
                assertThat(hasBeenInvoked).isTrue()
                assertThat(result).isEqualTo(18872)
            }
        }

        @Test
        fun `initiates a restart of the network controller if a NoRouteToHostException is thrown`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object : MetaDataProviderConfig by AnidbHighestIdDetectorConfig {
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:8080/highest-id")
                }

                var hasBeenInvoked = false
                val testNetworkController = object: NetworkController by TestNetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> {
                        hasBeenInvoked = true
                        return async { true }
                    }
                }

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        return if (hasBeenInvoked) {
                            HttpResponse(
                                code = 200,
                                body = loadTestResource<ByteArray>("crawler/anidb/AnidbHighestIdDetectorTest/latest-anime.html"),
                            )
                        } else {
                            throw NoRouteToHostException()
                        }
                    }
                }

                val anidbHighestIdDetector = AnidbHighestIdDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                    networkController = testNetworkController,
                )

                // when
                val result = anidbHighestIdDetector.detectHighestId()

                // then
                assertThat(hasBeenInvoked).isTrue()
                assertThat(result).isEqualTo(18872)
            }
        }

        @Test
        fun `initiates a restart of the network controller if a CrawlerDetectedException is thrown`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object : MetaDataProviderConfig by AnidbHighestIdDetectorConfig {
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:8080/highest-id")
                }

                var hasBeenInvoked = false
                val testNetworkController = object: NetworkController by TestNetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> {
                        hasBeenInvoked = true
                        return async { true }
                    }
                }

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        return if (hasBeenInvoked) {
                            HttpResponse(
                                code = 200,
                                body = loadTestResource<ByteArray>("crawler/anidb/AnidbHighestIdDetectorTest/latest-anime.html"),
                            )
                        } else {
                            throw CrawlerDetectedException
                        }
                    }
                }

                val anidbHighestIdDetector = AnidbHighestIdDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                    networkController = testNetworkController,
                )

                // when
                val result = anidbHighestIdDetector.detectHighestId()

                // then
                assertThat(hasBeenInvoked).isTrue()
                assertThat(result).isEqualTo(18872)
            }
        }

        @Test
        fun `throws an exception if a restart of the network controller didn't help`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object : MetaDataProviderConfig by AnidbHighestIdDetectorConfig {
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:8080/highest-id")
                }

                var hasBeenInvoked = false
                val testNetworkController = object: NetworkController by TestNetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> {
                        hasBeenInvoked = true
                        return async { true }
                    }
                }

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        throw NoRouteToHostException("junit test")
                    }
                }

                val anidbHighestIdDetector = AnidbHighestIdDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                    networkController = testNetworkController,
                )

                // when
                val result = exceptionExpected<NoRouteToHostException> {
                    anidbHighestIdDetector.detectHighestId()
                }

                // then
                assertThat(hasBeenInvoked).isTrue()
                assertThat(result).hasMessage("junit test")
            }
        }

        @Test
        fun `directly throws exception if it's not one of the cases that restart the network controller`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object : MetaDataProviderConfig by AnidbHighestIdDetectorConfig {
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:8080/highest-id")
                }

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        throw NullPointerException("junit test")
                    }
                }

                val anidbHighestIdDetector = AnidbHighestIdDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                    networkController = TestNetworkController,
                )

                // when
                val result = exceptionExpected<NullPointerException> {
                    anidbHighestIdDetector.detectHighestId()
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
                val previous = AnidbHighestIdDetector.instance

                // when
                val result = AnidbHighestIdDetector.instance

                // then
                assertThat(result).isExactlyInstanceOf(AnidbHighestIdDetector::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}