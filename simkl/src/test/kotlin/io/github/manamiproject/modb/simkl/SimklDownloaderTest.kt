package io.github.manamiproject.modb.simkl

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.toAnimeId
import io.github.manamiproject.modb.core.httpclient.APPLICATION_JSON
import io.github.manamiproject.modb.test.MockServerTestCase
import io.github.manamiproject.modb.test.WireMockServerCreator
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.Test

internal class SimklDownloaderTest : MockServerTestCase<WireMockServer> by WireMockServerCreator() {

    @Test
    fun `successfully download an anime`() {
        runBlocking {
            // given
            val testConfig = object : MetaDataProviderConfig by SimklConfig {
                override fun hostname(): Hostname = "localhost"
                override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:$port/anime/$id")
            }

            val id = "drmaMJIZg"

            serverInstance.stubFor(
                get(urlPathEqualTo("/anime/$id")).willReturn(
                    aResponse()
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withStatus(200)
                        .withBody("<html></head><body>success</body></html>")
                )
            )

            val downloader = SimklDownloader(testConfig)

            // when
            val result = downloader.download(id) {
                shouldNotBeInvoked()
            }

            // then
            assertThat(result).isEqualTo("<html></head><body>success</body></html>")
        }
    }

    @Test
    fun `correctly identify dead entries`() {
        runBlocking {
            // given
            val testConfig = object : MetaDataProviderConfig by SimklConfig {
                override fun hostname(): Hostname = "localhost"
                override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:$port/anime/$id")
            }

            val id = "drmaMJIZg"

            serverInstance.stubFor(
                get(urlPathEqualTo("/anime/$id")).willReturn(
                    aResponse()
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withStatus(200)
                        .withBody("""<html><head><meta property="og:title" content="Simkl - Watch and Track Movies, Anime, TV Shows" /></head><body>success</body></html>""")
                )
            )

            var success = false
            val downloader = SimklDownloader(testConfig)

            // when
            val result = downloader.download(id)  {
                success = true
            }

            // then
            assertThat(success).isTrue()
            assertThat(result).isEmpty()
        }
    }

    @Test
    fun `unhandled response code throws exception`() {
        // given
        val testConfig = object: MetaDataProviderConfig by SimklConfig {
            override fun hostname(): Hostname = "localhost"
            override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:$port/anime/$id")
        }

        val id = "r0iztKiiR"

        serverInstance.stubFor(
            get(urlPathEqualTo("/anime/$id")).willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/plain")
                    .withStatus(400)
                    .withBody("Internal Server Error")
            )
        )

        val downloader = SimklDownloader(testConfig)

        // when
        val result = exceptionExpected<IllegalStateException> {
            downloader.download(id) {
                shouldNotBeInvoked()
            }
        }

        // then
        assertThat(result).hasMessage("Unable to determine the correct case for [simklId=$id], [responseCode=400]")
    }

    @Test
    fun `throws an exception if the response body is empty`() {
        // given
        val id = 1535

        val testAnidbConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
            override fun hostname(): Hostname = "localhost"
            override fun buildAnimeLink(id: AnimeId): URI = SimklConfig.buildAnimeLink(id)
            override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}:$port/anime/$id")
            override fun fileSuffix(): FileSuffix = SimklConfig.fileSuffix()
        }

        serverInstance.stubFor(
            get(urlPathEqualTo("/anime/$id")).willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/html")
                    .withStatus(200)
                    .withBody(EMPTY)
            )
        )

        val downloader = SimklDownloader(testAnidbConfig)

        // when
        val result = exceptionExpected<IllegalStateException> {
            downloader.download(id.toAnimeId()) { shouldNotBeInvoked() }
        }

        // then
        assertThat(result).hasMessage("Response body was blank for [simklId=1535] with response code [200]")
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = SimklDownloader.instance

            // when
            val result = SimklDownloader.instance

            // then
            assertThat(result).isExactlyInstanceOf(SimklDownloader::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}