package io.github.manamiproject.modb.app.crawler.livechart

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

internal class LivechartNewestYearDetectorTest {

    @Nested
    inner class DetectHighestIdTests {

        @Test
        fun `correctly extracts highest id`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by LivechartNewestYearDetectorConfig {
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:8080/highest-id")
                }

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        return HttpResponse(
                            code = 200,
                            body = loadTestResource<String>("crawler/livechart/LivechartNewestYearDetectorTest/charts.html").toByteArray(),
                        )
                    }
                }

                val livechartHighestIdDetector = LivechartNewestYearDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                )

                // when
                val result = livechartHighestIdDetector.detectHighestId()

                // then
                assertThat(result).isEqualTo(2026)
            }
        }

        @Test
        fun `throws exception, because the element couldn't be found`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by LivechartNewestYearDetectorConfig {
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:8080/highest-id")
                }

                val content = """
                    <html>
                    </head>
                    </body>
                    </html>
                """.trimIndent()

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        return HttpResponse(
                            code = 200,
                            body = content.toByteArray(),
                        )
                    }
                }

                val livechartNewestYearDetector = LivechartNewestYearDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    livechartNewestYearDetector.detectHighestId()
                }

                // then
                assertThat(result).hasMessage("Unable to extract headers.")
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = LivechartNewestYearDetector.instance

                // when
                val result = LivechartNewestYearDetector.instance

                // then
                assertThat(result).isExactlyInstanceOf(LivechartNewestYearDetector::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}