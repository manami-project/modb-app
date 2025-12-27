package io.github.manamiproject.modb.app.crawlers.kitsu

import io.github.manamiproject.modb.app.TestHttpClient
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import java.net.URL
import kotlin.test.Test

internal class KitsuHighestIdDetectorTest {

    @Nested
    inner class DetectHighestIdTests {

        @Test
        fun `correctly extracts highest id`() {
            runTest {
                // given
                val testMetaDataProviderConfig = object : MetaDataProviderConfig by KitsuHighestIdDetectorConfig {
                    override fun hostname(): Hostname = "localhost"
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:8080/highest-id")
                }

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = loadTestResource<ByteArray>("crawler/kitsu/KitsuHighestIdDetectorTest/just_added.json"),
                    )
                }

                val kitsuHighestIdDetector = KitsuHighestIdDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                )

                // when
                val result = kitsuHighestIdDetector.detectHighestId()

                // then
                assertThat(result).isEqualTo(49212)
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = KitsuHighestIdDetector.instance

                // when
                val result = KitsuHighestIdDetector.instance

                // then
                assertThat(result).isExactlyInstanceOf(KitsuHighestIdDetector::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}