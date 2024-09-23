package io.github.manamiproject.modb.app.crawler.anilist

import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.app.TestHttpClient
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.httpclient.RequestBody
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import java.net.URL
import kotlin.test.Test

internal class AnilistHighestIdDetectorTest {

    @Nested
    inner class DetectHighestIdTests {

        @Test
        fun `correctly extracts highest id`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object : MetaDataProviderConfig by AnilistConfig {
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:8080/highest-id")
                }

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun post(url: URL, requestBody: RequestBody, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = loadTestResource("crawler/anilist/AnilistHighestIdDetectorTest/highest_id.json"),
                    )
                }

                val anilistHighestIdDetector = AnilistHighestIdDetector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                )

                // when
                val result = anilistHighestIdDetector.detectHighestId()

                // then
                assertThat(result).isEqualTo(181776)
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = AnilistHighestIdDetector.instance

                // when
                val result = AnilistHighestIdDetector.instance

                // then
                assertThat(result).isExactlyInstanceOf(AnilistHighestIdDetector::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}