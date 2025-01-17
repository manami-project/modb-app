package io.github.manamiproject.modb.notify

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
import kotlin.test.Test
import java.net.URI

internal class NotifyDownloaderTest : MockServerTestCase<WireMockServer> by WireMockServerCreator() {

    @Test
    fun `successfully download an anime`() {
        runBlocking {
            // given
            val testConfig = object : MetaDataProviderConfig by NotifyConfig {
                override fun hostname(): Hostname = "localhost"
                override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:$port/anime/$id")
            }

            val id = "drmaMJIZg"

            serverInstance.stubFor(
                get(urlPathEqualTo("/anime/$id")).willReturn(
                    aResponse()
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withStatus(200)
                        .withBody("{ \"notifyId\": $id }")
                )
            )

            val downloader = NotifyDownloader(testConfig)

            // when
            val result = downloader.download(id) {
                shouldNotBeInvoked()
            }

            // then
            assertThat(result).isEqualTo("{ \"notifyId\": drmaMJIZg }")
        }
    }

    @Test
    fun `invoke lambda in case of a dead entry`() {
        runBlocking {
            // given
            val testConfig = object : MetaDataProviderConfig by NotifyConfig {
                override fun hostname(): Hostname = "localhost"
                override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:$port/anime/$id")
            }

            val id = "drmaMJIZg"

            serverInstance.stubFor(
                get(urlPathEqualTo("/anime/$id")).willReturn(
                    aResponse()
                        .withHeader("Content-Type", "plain/text")
                        .withStatus(404)
                        .withBody("Not found: Key not found:")
                )
            )

            val downloader = NotifyDownloader(testConfig)

            var deadEntriesHasBeenInvoked = false

            // when
            downloader.download(id) {
                deadEntriesHasBeenInvoked = true
            }

            // then
            assertThat(deadEntriesHasBeenInvoked).isTrue()
        }
    }

    @Test
    fun `unhandled response code throws exception`() {
        // given
        val testConfig = object: MetaDataProviderConfig by NotifyConfig {
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

        val downloader = NotifyDownloader(testConfig)

        // when
        val result = exceptionExpected<IllegalStateException> {
            downloader.download(id) {
                shouldNotBeInvoked()
            }
        }

        // then
        assertThat(result).hasMessage("Unable to determine the correct case for [notifyId=$id], [responseCode=400]")
    }

    @Test
    fun `throws an exception if the response body is empty`() {
        // given
        val id = 1535

        val testAnidbConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
            override fun hostname(): Hostname = "localhost"
            override fun buildAnimeLink(id: AnimeId): URI = NotifyConfig.buildAnimeLink(id)
            override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}:$port/anime/$id")
            override fun fileSuffix(): FileSuffix = NotifyConfig.fileSuffix()
        }

        serverInstance.stubFor(
            get(urlPathEqualTo("/anime/$id")).willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/html")
                    .withStatus(200)
                    .withBody(EMPTY)
            )
        )

        val downloader = NotifyDownloader(testAnidbConfig)

        // when
        val result = exceptionExpected<IllegalStateException> {
            downloader.download(id.toAnimeId()) { shouldNotBeInvoked() }
        }

        // then
        assertThat(result).hasMessage("Response body was blank for [notifyId=1535] with response code [200]")
    }
}