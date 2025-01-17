package io.github.manamiproject.modb.livechart

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.toAnimeId
import io.github.manamiproject.modb.test.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.Test

internal class LivechartDownloaderTest : MockServerTestCase<WireMockServer> by WireMockServerCreator() {

    @Test
    fun `successfully load an entry`() {
        runBlocking {
            // given
            val id = 1535

            val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                override fun hostname(): Hostname = "localhost"
                override fun buildAnimeLink(id: AnimeId): URI = URI("http://localhost:$port/anime/$id")
                override fun buildDataDownloadLink(id: String): URI = buildAnimeLink(id)
                override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
            }

            val responseBody = "<html><head/><body></body></html>"

            serverInstance.stubFor(
                get(urlPathEqualTo("/anime/$id")).willReturn(
                    aResponse()
                        .withHeader("Content-Type", "text/html")
                        .withStatus(200)
                        .withBody(responseBody)
                )
            )

            val liveChartDownloader = LivechartDownloader(testLivechartConfig)

            // when
            val result = liveChartDownloader.download(id = id.toAnimeId(), onDeadEntry = { shouldNotBeInvoked() })

            // then
            assertThat(result).isEqualTo(responseBody)
        }
    }

    @Test
    fun `responding 404 indicating dead entry - add to dead entry list and return empty string`() {
        runBlocking {
            // given
            val id = 1535
            var hasDeadEntryBeenInvoked = false

            val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                override fun hostname(): Hostname = "localhost"
                override fun buildAnimeLink(id: AnimeId): URI = URI("http://localhost:$port/anime/$id")
                override fun buildDataDownloadLink(id: String): URI = buildAnimeLink(id)
                override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
            }

            serverInstance.stubFor(
                get(urlPathEqualTo("/anime/$id"))
                    .willReturn(
                        aResponse()
                            .withHeader("Content-Type", "text/html")
                            .withStatus(404)
                            .withBody("<html><head><title>Page Not Found | LiveChart.me</title><body></body></html>")
                    )
            )

            val livechartDownloader = LivechartDownloader(testLivechartConfig)

            // when
            val result =
                livechartDownloader.download(id = id.toAnimeId(), onDeadEntry = { hasDeadEntryBeenInvoked = true })

            // then
            assertThat(hasDeadEntryBeenInvoked).isTrue()
            assertThat(result).isBlank()
        }
    }

    @Test
    fun `excluded from database page - add to dead entry list and return empty string`() {
        runBlocking {
            // given
            val id = 1535
            var hasDeadEntryBeenInvoked = false

            val testLivechartConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                override fun hostname(): Hostname = "localhost"
                override fun buildAnimeLink(id: AnimeId): URI = URI("http://localhost:$port/anime/$id")
                override fun buildDataDownloadLink(id: String): URI = buildAnimeLink(id)
                override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
            }

            serverInstance.stubFor(
                get(urlPathEqualTo("/anime/$id"))
                    .willReturn(
                        aResponse()
                            .withHeader("Content-Type", "text/html")
                            .withStatus(200)
                            .withBody(loadTestResource<String>("downloader_tests/excluded_from_database.html"))
                    )
            )

            val livechartDownloader = LivechartDownloader(testLivechartConfig)

            // when
            val result = livechartDownloader.download(id = id.toAnimeId(), onDeadEntry = { hasDeadEntryBeenInvoked = true })

            // then
            assertThat(hasDeadEntryBeenInvoked).isTrue()
            assertThat(result).isBlank()
        }
    }

    @Test
    fun `throws an exception if the response body is empty`() {
        // given
        val id = 1535

        val testLivechartConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
            override fun hostname(): Hostname = "localhost"
            override fun buildAnimeLink(id: AnimeId): URI = LivechartConfig.buildAnimeLink(id)
            override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}:$port/anime/$id")
            override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
        }

        serverInstance.stubFor(
            get(urlPathEqualTo("/anime/$id")).willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/html")
                    .withStatus(200)
                    .withBody(EMPTY)
            )
        )

        val livechartDownloader = LivechartDownloader(testLivechartConfig)

        // when
        val result = exceptionExpected<IllegalStateException> {
            livechartDownloader.download(id.toAnimeId()) { shouldNotBeInvoked() }
        }

        // then
        assertThat(result).hasMessage("Response body was blank for [livechartId=1535] with response code [200]")
    }

    @Test
    fun `unhandled response code`() {
        // given
        val id = 1535

        val testLivechartConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
            override fun hostname(): Hostname = "localhost"
            override fun buildAnimeLink(id: AnimeId): URI = URI("http://localhost:$port/anime/$id")
            override fun buildDataDownloadLink(id: String): URI = buildAnimeLink(id)
            override fun fileSuffix(): FileSuffix = LivechartConfig.fileSuffix()
        }

        serverInstance.stubFor(
            get(urlPathEqualTo("/anime/$id"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "text/html")
                        .withStatus(400)
                        .withBody("<html></html>")
                )
        )

        val livechartDownloader = LivechartDownloader(testLivechartConfig)

        // when
        val result = exceptionExpected<IllegalStateException> {
            livechartDownloader.download(id = id.toAnimeId(), onDeadEntry = { shouldNotBeInvoked() })
        }

        // then
        assertThat(result).hasMessage("Unable to determine the correct case for [livechartId=$id], [responseCode=400]")
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = LivechartDownloader.instance

            // when
            val result = LivechartDownloader.instance

            // then
            assertThat(result).isExactlyInstanceOf(LivechartDownloader::class.java)
            assertThat(result===previous).isTrue()
        }
    }
}