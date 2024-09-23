package io.github.manamiproject.modb.app.crawler.myanimelist

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

internal class MyanimelistHighestIdDetectorTest {

    @Nested
    inner class DetectHighestIdTests {

        @Test
        fun `correctly extracts highest id`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object : MetaDataProviderConfig by MyanimelistHighestIdDetectorConfig {
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:8080/highest-id")
                }

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = loadTestResource("crawler/myanimelist/MyanimelistHighestIdDetectorTest/just_added.html"),
                    )
                }

                val myanimelistHighestIdDetector = MyanimelistHighestIdDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                )

                // when
                val result = myanimelistHighestIdDetector.detectHighestId()

                // then
                assertThat(result).isEqualTo(59892)
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

                val myanimelistHighestIdDetector = MyanimelistHighestIdDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    myanimelistHighestIdDetector.detectHighestId()
                }

                // then
                assertThat(result).hasMessage("Unable to extract urlOfTheMostRecentAddition.")
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
                        <a class="hoverinfo_trigger" href="/other/">link</a>
                    </body>
                    </html>
                """.trimIndent()

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = content.toByteArray(),
                    )
                }

                val myanimelistHighestIdDetector = MyanimelistHighestIdDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    myanimelistHighestIdDetector.detectHighestId()
                }

                // then
                assertThat(result).hasMessage("Unable to detect highest id for myanimelist.")
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = MyanimelistHighestIdDetector.instance

                // when
                val result = MyanimelistHighestIdDetector.instance

                // then
                assertThat(result).isExactlyInstanceOf(MyanimelistHighestIdDetector::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}