package io.github.manamiproject.modb.app.crawlers.animeplanet

import io.github.manamiproject.modb.app.TestHttpClient
import io.github.manamiproject.modb.app.crawlers.myanimelist.MyanimelistHighestIdDetectorConfig
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

internal class AnimePlanetLastPageDetectorTest {

    @Nested
    inner class DetectHighestIdTests {

        @Test
        fun `correctly extracts last page`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object : MetaDataProviderConfig by MyanimelistHighestIdDetectorConfig {
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:8080/highest-id")
                }

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = loadTestResource("crawler/animeplanet/AnimePlanetLastPageDetectorTest/all.html"),
                    )
                }

                val animePlanetLastPageDetector = AnimePlanetLastPageDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                )

                // when
                val result = animePlanetLastPageDetector.detectHighestId()

                // then
                assertThat(result).isEqualTo(690)
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
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = content.toByteArray(),
                    )
                }

                val animePlanetLastPageDetector = AnimePlanetLastPageDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    animePlanetLastPageDetector.detectHighestId()
                }

                // then
                assertThat(result).hasMessage("Couldn't find lastPage.")
            }
        }

        @Test
        fun `throws exception if value found is not an integer`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object : MetaDataProviderConfig by MyanimelistHighestIdDetectorConfig {
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:8080/highest-id")
                }

                val content = """
                    <html>
                    </head>
                    <body>
                        <div class="pagination aligncenter"]><ul><li><a href="/other">other</a></li></u></div>
                    </body>
                    </html>
                """.trimIndent()

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = content.toByteArray(),
                    )
                }

                val animePlanetLastPageDetector = AnimePlanetLastPageDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    animePlanetLastPageDetector.detectHighestId()
                }

                // then
                assertThat(result).hasMessage("Couldn't extract the last page.")
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = AnimePlanetLastPageDetector.instance

                // when
                val result = AnimePlanetLastPageDetector.instance

                // then
                assertThat(result).isExactlyInstanceOf(AnimePlanetLastPageDetector::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}