package io.github.manamiproject.modb.anidb

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.github.manamiproject.modb.anidb.AnidbDownloader.Companion.ANIDB_PENDING_FILE_INDICATOR
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

internal class AnidbDownloaderTest : MockServerTestCase<WireMockServer> by WireMockServerCreator() {

    @Nested
    inner class DownloadTests {

        @Test
        fun `successfully download an anime`() {
            runBlocking {
                // given
                val id = "11376"

                val testConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "localhost"
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}:$port/anime/$id")
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                serverInstance.stubFor(
                    get(urlPathEqualTo("/anime/$id")).willReturn(
                        aResponse()
                            .withHeader("Content-Type", "text/html")
                            .withStatus(200)
                            .withBody("<html></html>")
                    )
                )

                val downloader = AnidbDownloader(testConfig)

                // when
                val result = downloader.download(id) {
                    shouldNotBeInvoked()
                }

                // then
                assertThat(result).isEqualTo("<html></html>")
            }
        }

        @Test
        fun `crawler is detected during download of anime and shows anti leech page`() {
            // given
            val id  = "11376"

            val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = "localhost"
                override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}:$port/anime/$id")
                override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
            }

            val responseBodyAntiLeech = loadTestResource<String>("AnidbDownloaderTest/anti-leech_page.html")

            serverInstance.stubFor(
                get(urlPathEqualTo("/anime/$id")).willReturn(
                    aResponse()
                        .withHeader("Content-Type", "text/html")
                        .withStatus(200)
                        .withBody(responseBodyAntiLeech)
                )
            )

            val downloader = AnidbDownloader(testConfig)

            // when
            val result = exceptionExpected<RuntimeException> {
                downloader.download(id) { shouldNotBeInvoked() }
            }

            // then
            assertThat(result).hasMessage("Crawler has been detected")
        }

        @Test
        fun `crawler is detected during download of anime and shows nginx error page`() {
            // given
            val id  = "11376"

            val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = "localhost"
                override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}:$port/anime/$id")
                override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
            }

            val responseBodyAntiLeech = loadTestResource<String>("AnidbDownloaderTest/nginx_error_page.html")

            serverInstance.stubFor(
                get(urlPathEqualTo("/anime/$id")).willReturn(
                    aResponse()
                        .withHeader("Content-Type", "text/html")
                        .withStatus(200)
                        .withBody(responseBodyAntiLeech)
                )
            )

            val downloader = AnidbDownloader(testConfig)

            // when
            val result = exceptionExpected<RuntimeException> {
                downloader.download(id) { shouldNotBeInvoked() }
            }

            // then
            assertThat(result).hasMessage("Crawler has been detected")
        }

        @Test
        fun `throws an exception due to an unknown http response code while trying to download an anime entry`() {
            // given
            val id = "11376"

            val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = "localhost"
                override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}:$port/anime/$id")
                override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
            }

            serverInstance.stubFor(
                get(urlPathEqualTo("/anime/$id")).willReturn(
                    aResponse()
                        .withHeader("Content-Type", "text/html")
                        .withStatus(402)
                        .withBody("<html></html>")
                )
            )

            val downloader = AnidbDownloader(testConfig)

            // when
            val result = exceptionExpected<IllegalStateException> {
                downloader.download(id) { shouldNotBeInvoked() }
            }

            // then
            assertThat(result).hasMessage("Unexpected response code [anidbId=$id], [responseCode=402]")
        }

        @Test
        fun `invoke onDeadEntry if the response body indicates a hentai`() {
            runBlocking {
                // given
                val id = "11376"
                var deadEntry = EMPTY

                val testConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "localhost"
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}:$port/anime/$id")
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val responseBody = loadTestResource<String>("AnidbDownloaderTest/hentai.html")

                serverInstance.stubFor(
                    get(urlPathEqualTo("/anime/$id")).willReturn(
                        aResponse()
                            .withHeader("Content-Type", "text/html")
                            .withStatus(200)
                            .withBody(responseBody)
                    )
                )

                val downloader = AnidbDownloader(testConfig)

                // when
                val result = downloader.download(id) {
                    deadEntry = it
                }

                // then
                assertThat(result).isEmpty()
                assertThat(deadEntry).isEqualTo(id)
            }
        }

        @Test
        fun `invoke onDeadEntry if the response body indicates a deleted entry`() {
            runBlocking {
                // given
                val id = "11376"
                var deadEntry = EMPTY

                val testConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "localhost"
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}:$port/anime/$id")
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val responseBody = loadTestResource<String>("AnidbDownloaderTest/deleted_entry.html")

                serverInstance.stubFor(
                    get(urlPathEqualTo("/anime/$id")).willReturn(
                        aResponse()
                            .withHeader("Content-Type", "text/html")
                            .withStatus(404)
                            .withBody(responseBody)
                    )
                )

                val downloader = AnidbDownloader(testConfig)

                // when
                val result = downloader.download(id) {
                    deadEntry = it
                }

                // then
                assertThat(result).isEmpty()
                assertThat(deadEntry).isEqualTo(id)
            }
        }

        @Test
        fun `won't invoke onDeadEntry and returns a static string if the response body indicates that the addition of the anime is pending`() {
            runBlocking {
                // given
                val id = "11376"

                val testConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "localhost"
                    override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}:$port/anime/$id")
                    override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
                }

                val responseBody = loadTestResource<String>("AnidbDownloaderTest/addition_pending.html")

                serverInstance.stubFor(
                    get(urlPathEqualTo("/anime/$id")).willReturn(
                        aResponse()
                            .withHeader("Content-Type", "text/html")
                            .withStatus(200)
                            .withBody(responseBody)
                    )
                )

                val downloader = AnidbDownloader(testConfig)

                // when
                val result = downloader.download(id) {
                    shouldNotBeInvoked()
                }

                // then
                assertThat(result).isEqualTo(ANIDB_PENDING_FILE_INDICATOR)
            }
        }

        @Test
        fun `throws an exception if the response body is empty`() {
            // given
            val id = 1535

            val testAnidbConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = "localhost"
                override fun buildAnimeLink(id: AnimeId): URI = AnidbConfig.buildAnimeLink(id)
                override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}:$port/anime/$id")
                override fun fileSuffix(): FileSuffix = AnidbConfig.fileSuffix()
            }

            serverInstance.stubFor(
                get(urlPathEqualTo("/anime/$id")).willReturn(
                    aResponse()
                        .withHeader("Content-Type", "text/html")
                        .withStatus(200)
                        .withBody(EMPTY)
                )
            )

            val downloader = AnidbDownloader(testAnidbConfig)

            // when
            val result = exceptionExpected<IllegalStateException> {
                downloader.download(id.toAnimeId()) { shouldNotBeInvoked() }
            }

            // then
            assertThat(result).hasMessage("Response body was blank for [anidbId=1535] with response code [200]")
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = AnidbDownloader.instance

                // when
                val result = AnidbDownloader.instance

                // then
                assertThat(result).isExactlyInstanceOf(AnidbDownloader::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}