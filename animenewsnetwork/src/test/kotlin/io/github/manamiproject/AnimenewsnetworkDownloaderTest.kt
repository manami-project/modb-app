package io.github.manamiproject

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.toAnimeId
import io.github.manamiproject.modb.test.MockServerTestCase
import io.github.manamiproject.modb.test.WireMockServerCreator
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class AnimenewsnetworkDownloaderTest : MockServerTestCase<WireMockServer> by WireMockServerCreator() {

    @Test
    fun `successfully download an anime`() {
        runTest {
            // given
            val id = "11376"

            val testConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = "localhost"
                override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}:$port/anime/$id")
                override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
            }

            serverInstance.stubFor(
                get(urlPathEqualTo("/anime/$id")).willReturn(
                    aResponse()
                        .withHeader("Content-Type", "text/html")
                        .withStatus(200)
                        .withBody("<html></html>")
                )
            )

            val downloader = AnimenewsnetworkDownloader(testConfig)

            // when
            val result = downloader.download(id) {
                shouldNotBeInvoked()
            }

            // then
            assertThat(result).isEqualTo("<html></html>")
        }
    }

    @Test
    fun `throws an exception due to an unknown http response code while trying to download an anime entry`() {
        // given
        val id = "11376"

        val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
            override fun hostname(): Hostname = "localhost"
            override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
            override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}:$port/anime/$id")
            override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
        }

        serverInstance.stubFor(
            get(urlPathEqualTo("/anime/$id")).willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/html")
                    .withStatus(402)
                    .withBody("<html></html>")
            )
        )

        val downloader = AnimenewsnetworkDownloader(testConfig)

        // when
        val result = exceptionExpected<IllegalStateException> {
            downloader.download(id) { shouldNotBeInvoked() }
        }

        // then
        assertThat(result).hasMessage("Unexpected response code [animenewsnetworkId=$id], [responseCode=402]")
    }

    @Test
    fun `throws an exception if the response body is empty`() {
        // given
        val id = 1535

        val testAnidbConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
            override fun hostname(): Hostname = "localhost"
            override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
            override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}:$port/anime/$id")
            override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
        }

        serverInstance.stubFor(
            get(urlPathEqualTo("/anime/$id")).willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/html")
                    .withStatus(200)
                    .withBody(EMPTY)
            )
        )

        val downloader = AnimenewsnetworkDownloader(testAnidbConfig)

        // when
        val result = exceptionExpected<IllegalStateException> {
            downloader.download(id.toAnimeId()) { shouldNotBeInvoked() }
        }

        // then
        assertThat(result).hasMessage("Response body was blank for [animenewsnetworkId=1535] with response code [200]")
    }

    @Test
    fun `invokes onDeadEntry for response code 404 and returns empty string`() {
        runTest {
            // given
            val id = "11376"
            var deadEntry = EMPTY

            val testConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = "localhost"
                override fun buildAnimeLink(id: AnimeId): URI = AnimenewsnetworkConfig.buildAnimeLink(id)
                override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}:$port/anime/$id")
                override fun fileSuffix(): FileSuffix = AnimenewsnetworkConfig.fileSuffix()
            }

            serverInstance.stubFor(
                get(urlPathEqualTo("/anime/$id")).willReturn(
                    aResponse()
                        .withHeader("Content-Type", "text/html")
                        .withStatus(404)
                        .withBody("<html></html>")
                )
            )

            val downloader = AnimenewsnetworkDownloader(testConfig)

            // when
            val result = downloader.download(id) {
                deadEntry = it
            }

            // then
            assertThat(result).isEmpty()
            assertThat(deadEntry).isEqualTo(id)
        }
    }
}