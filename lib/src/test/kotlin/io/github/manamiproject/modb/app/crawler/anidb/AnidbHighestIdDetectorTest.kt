package io.github.manamiproject.modb.app.crawler.anidb

import io.github.manamiproject.modb.app.TestHttpClient
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import java.net.URL
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
                        body = loadTestResource<String>("crawler/anidb/AnidbHighestIdDetectorTest/latest-anime.html").toByteArray(),
                    )
                }

                val anidbHighestIdDetector = AnidbHighestIdDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
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
                        body = content.toByteArray(),
                    )
                }

                val anidbHighestIdDetector = AnidbHighestIdDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
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
                        body = content.toByteArray(),
                    )
                }

                val anidbHighestIdDetector = AnidbHighestIdDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    anidbHighestIdDetector.detectHighestId()
                }

                // then
                assertThat(result).hasMessage("Unable to extract highest id.")
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
                assertThat(result===previous).isTrue()
            }
        }
    }
}