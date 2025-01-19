package io.github.manamiproject.modb.animeplanet

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.toAnimeId
import io.github.manamiproject.modb.core.httpclient.APPLICATION_JSON
import io.github.manamiproject.modb.test.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI
import kotlin.test.Test

internal class AnimePlanetDownloaderTest : MockServerTestCase<WireMockServer> by WireMockServerCreator() {

    @Nested
    inner class DownloadTests {

        @Test
        fun `successfully download an anime`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun hostname(): Hostname = "localhost"
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:$port/anime/$id")
                }

                val id = "black-clover"

                serverInstance.stubFor(
                    get(urlPathEqualTo("/anime/$id")).willReturn(
                        aResponse()
                            .withHeader("Content-Type", APPLICATION_JSON)
                            .withStatus(200)
                            .withBody("<html>content for $id</html>")
                    )
                )

                val downloader = AnimePlanetDownloader(testConfig)

                // when
                val result = downloader.download(id) {
                    shouldNotBeInvoked()
                }

                // then
                assertThat(result).isEqualTo("<html>content for black-clover</html>")
            }
        }

        @Test
        fun `unhandled response code throws exception`() {
            // given
            val testConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
                override fun hostname(): Hostname = "localhost"
                override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:$port/anime/$id")
                override fun isTestContext(): Boolean = true
            }

            val id = "black-clover"

            serverInstance.stubFor(
                get(urlPathEqualTo("/anime/$id")).willReturn(
                    aResponse()
                        .withHeader("Content-Type", "text/plain")
                        .withStatus(400)
                        .withBody("Internal Server Error")
                )
            )

            val downloader = AnimePlanetDownloader(testConfig)

            // when
            val result = exceptionExpected<IllegalStateException> {
                downloader.download(id) {
                    shouldNotBeInvoked()
                }
            }

            // then
            assertThat(result).hasMessage("Unable to determine the correct case for [animePlanetId=$id], [responseCode=400]")
        }

        @Test
        fun `throws an exception if the response body is empty`() {
            // given
            val id = 1535

            val testAnidbConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
                override fun hostname(): Hostname = "localhost"
                override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}:$port/anime/$id")
                override fun fileSuffix(): FileSuffix = AnimePlanetConfig.fileSuffix()
            }

            serverInstance.stubFor(
                get(urlPathEqualTo("/anime/$id")).willReturn(
                    aResponse()
                        .withHeader("Content-Type", "text/html")
                        .withStatus(200)
                        .withBody(EMPTY)
                )
            )

            val downloader = AnimePlanetDownloader(testAnidbConfig)

            // when
            val result = exceptionExpected<IllegalStateException> {
                downloader.download(id.toAnimeId()) { shouldNotBeInvoked() }
            }

            // then
            assertThat(result).hasMessage("Response body was blank for [animePlanetId=1535] with response code [200]")
        }

        @ParameterizedTest
        @ValueSource(ints = [429, 500, 502, 521, 525])
        fun `pause and retry on response code`(responseCode: Int) {
            runBlocking {
                // given
                val id = 1535

                val testAnimePlanetConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun hostname(): Hostname = "localhost"
                    override fun buildAnimeLink(id: AnimeId): URI = AnimePlanetConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:$port/anime/$id")
                    override fun fileSuffix(): FileSuffix = AnimePlanetConfig.fileSuffix()
                }

                serverInstance.stubFor(
                    get(urlPathEqualTo("/anime/$id"))
                        .inScenario("pause and retry")
                        .whenScenarioStateIs(STARTED)
                        .willSetStateTo("successful retrieval")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/html")
                                .withStatus(responseCode)
                                .withBody("<html></html>")
                        )
                )

                val responseBody = "<html><head/><body></body></html>"

                serverInstance.stubFor(
                    get(urlPathEqualTo("/anime/$id"))
                        .inScenario("pause and retry")
                        .whenScenarioStateIs("successful retrieval")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", APPLICATION_JSON)
                                .withStatus(200)
                                .withBody(responseBody)
                        )
                )

                val downloader = AnimePlanetDownloader(testAnimePlanetConfig)

                // when
                val result = downloader.download(id.toString()) {
                    shouldNotBeInvoked()
                }

                // then
                assertThat(result).isEqualTo(responseBody)
            }
        }

        @Test
        fun `invoke lambda on finding a dead entry`() {
            runBlocking {
                // given
                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun hostname(): Hostname = "localhost"
                    override fun buildDataDownloadLink(id: String): URI = URI("http://localhost:$port/anime/$id")
                }

                val id = "black-clover"

                serverInstance.stubFor(
                    get(urlPathEqualTo("/anime/$id")).willReturn(
                        aResponse()
                            .withHeader("Content-Type", APPLICATION_JSON)
                            .withStatus(200)
                            .withBody(loadTestResource<String>("AnimePlanetDownloaderTest/dead-entry.html"))
                    )
                )

                val downloader = AnimePlanetDownloader(testConfig)

                var deadEntryHasBeenInvoked = false

                // when
                val result = downloader.download(id) {
                    deadEntryHasBeenInvoked = true
                }

                // then
                assertThat(result).isEmpty()
                assertThat(deadEntryHasBeenInvoked).isTrue()
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = AnimePlanetDownloader.instance

                // when
                val result = AnimePlanetDownloader.instance

                // then
                assertThat(result).isExactlyInstanceOf(AnimePlanetDownloader::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}