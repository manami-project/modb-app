package io.github.manamiproject.modb.myanimelist

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
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

internal class MyanimelistDownloaderTest : MockServerTestCase<WireMockServer> by WireMockServerCreator() {

    @Nested
    inner class WaitAndRetryTests {

        @Test
        fun `response is 403 which indicates that a crawler has been detected - therefore pause and retry`() {
            runBlocking {
                // given
                val id = 1535

                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun hostname(): Hostname = "localhost"
                    override fun buildAnimeLink(id: AnimeId): URI = URI("http://localhost:$port/anime/$id")
                    override fun buildDataDownloadLink(id: String): URI = buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val responseBody = "<html><head/><body>Data</body></html>"

                serverInstance.stubFor(
                    get(urlPathEqualTo("/anime/$id"))
                        .inScenario("pause and retry")
                        .whenScenarioStateIs(STARTED)
                        .willSetStateTo("successful retrieval")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/html")
                                .withStatus(403)
                                .withBody("<html></html>")
                        )
                )

                serverInstance.stubFor(
                    get(urlPathEqualTo("/anime/$id"))
                        .inScenario("pause and retry")
                        .whenScenarioStateIs("successful retrieval")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/html")
                                .withStatus(200)
                                .withBody(responseBody)
                        )
                )

                val downloader = MyanimelistDownloader(testConfig)

                // when
                val result = downloader.download(id = id.toAnimeId(), onDeadEntry = { shouldNotBeInvoked() })

                // then
                assertThat(result).isEqualTo(responseBody)
            }
        }

        @Test
        fun `falsely returning 404 - therefore pause and retry - successfully retrieve anime after retry`() {
            runBlocking {
                // given
                val id = 1535

                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun hostname(): Hostname = "localhost"
                    override fun buildAnimeLink(id: AnimeId): URI = URI("http://localhost:$port/anime/$id")
                    override fun buildDataDownloadLink(id: String): URI = buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val responseBody = "<html><head/><body></body></html>"

                serverInstance.stubFor(
                    get(urlPathEqualTo("/anime/$id"))
                        .inScenario("pause and retry")
                        .whenScenarioStateIs(STARTED)
                        .willSetStateTo("successful retrieval")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/html")
                                .withStatus(404)
                                .withBody("<html><head><title>404 Not Found - MyAnimeList.net</title><body><p>Death Note was not found on this server.</p></body></html>")
                        )
                )

                serverInstance.stubFor(
                    get(urlPathEqualTo("/anime/$id"))
                        .inScenario("pause and retry")
                        .whenScenarioStateIs("successful retrieval")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/html")
                                .withStatus(200)
                                .withBody(responseBody)
                        )
                )

                val downloader = MyanimelistDownloader(testConfig)

                // when
                val result = downloader.download(id = id.toAnimeId(), onDeadEntry = { shouldNotBeInvoked() })

                // then
                assertThat(result).isEqualTo(responseBody)
            }
        }

        @Test
        fun `response is 429 'too many connections' - therefore has to pause and retry to download`() {
            runBlocking {
                // given
                val id = 1535

                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun hostname(): Hostname = "localhost"
                    override fun buildAnimeLink(id: AnimeId): URI = URI("http://localhost:$port/anime/$id")
                    override fun buildDataDownloadLink(id: String): URI = buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val responseBody = "<html><head/><body></body></html>"

                serverInstance.stubFor(
                    get(urlPathEqualTo("/anime/$id"))
                        .inScenario("pause and retry")
                        .whenScenarioStateIs(STARTED)
                        .willSetStateTo("successful retrieval")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/html")
                                .withStatus(429)
                                .withBody("<html></html>")
                        )
                )

                serverInstance.stubFor(
                    get(urlPathEqualTo("/anime/$id"))
                        .inScenario("pause and retry")
                        .whenScenarioStateIs("successful retrieval")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/html")
                                .withStatus(200)
                                .withBody(responseBody)
                        )
                )

                val downloader = MyanimelistDownloader(testConfig)

                // when
                val result = downloader.download(id = id.toAnimeId(), onDeadEntry = { shouldNotBeInvoked() })

                // then
                assertThat(result).isEqualTo(responseBody)
            }
        }

        @Test
        fun `response is 500 'internal server error' - therefore has to pause and retry to download`() {
            runBlocking {
                // given
                val id = 1535

                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun hostname(): Hostname = "localhost"
                    override fun buildAnimeLink(id: AnimeId): URI = URI("http://localhost:$port/anime/$id")
                    override fun buildDataDownloadLink(id: String): URI = buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val responseBody = "<html><head/><body></body></html>"

                serverInstance.stubFor(
                    get(urlPathEqualTo("/anime/$id"))
                        .inScenario("pause and retry")
                        .whenScenarioStateIs(STARTED)
                        .willSetStateTo("successful retrieval")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/html")
                                .withStatus(500)
                                .withBody("<html></html>")
                        )
                )

                serverInstance.stubFor(
                    get(urlPathEqualTo("/anime/$id"))
                        .inScenario("pause and retry")
                        .whenScenarioStateIs("successful retrieval")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/html")
                                .withStatus(200)
                                .withBody(responseBody)
                        )
                )

                val downloader = MyanimelistDownloader(testConfig)

                // when
                val result = downloader.download(id = id.toAnimeId(), onDeadEntry = { shouldNotBeInvoked() })

                // then
                assertThat(result).isEqualTo(responseBody)
            }
        }

        @Test
        fun `response is 504 'gateway timeout' - therefore has to pause and retry to download`() {
            runBlocking {
                // given
                val id = 1535

                val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun hostname(): Hostname = "localhost"
                    override fun buildAnimeLink(id: AnimeId): URI = URI("http://localhost:$port/anime/$id")
                    override fun buildDataDownloadLink(id: String): URI = buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
                }

                val responseBody = "<html><head/><body></body></html>"

                serverInstance.stubFor(
                    get(urlPathEqualTo("/anime/$id"))
                        .inScenario("pause and retry")
                        .whenScenarioStateIs(STARTED)
                        .willSetStateTo("successful retrieval")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/html")
                                .withStatus(504)
                                .withBody("<html></html>")
                        )
                )

                serverInstance.stubFor(
                    get(urlPathEqualTo("/anime/$id"))
                        .inScenario("pause and retry")
                        .whenScenarioStateIs("successful retrieval")
                        .willReturn(
                            aResponse()
                                .withHeader("Content-Type", "text/html")
                                .withStatus(200)
                                .withBody(responseBody)
                        )
                )

                val downloader = MyanimelistDownloader(testConfig)

                // when
                val result = downloader.download(id = id.toAnimeId(), onDeadEntry = { shouldNotBeInvoked() })

                // then
                assertThat(result).isEqualTo(responseBody)
            }
        }
    }

    @Nested
    inner class UnknownCaseTests {

        @Test
        fun `unhandled response code`() {
            // given
            val id = 1535

            val testConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
                override fun hostname(): Hostname = "localhost"
                override fun buildAnimeLink(id: AnimeId): URI = URI("http://localhost:$port/anime/$id")
                override fun buildDataDownloadLink(id: String): URI = buildAnimeLink(id)
                override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
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

            val downloader = MyanimelistDownloader(testConfig)

            // when
            val result = exceptionExpected<IllegalStateException> {
                downloader.download(id = id.toAnimeId(), onDeadEntry = { shouldNotBeInvoked() })
            }

            // then
            assertThat(result).hasMessage("Unable to determine the correct case for [myanimelistId=$id], [responseCode=400]")
        }

        @Test
        fun `responding 404 - unknown case`() {
            // given
            val id = 1535

            val testConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
                override fun hostname(): Hostname = "localhost"
                override fun buildAnimeLink(id: AnimeId): URI = URI("http://localhost:$port/anime/$id")
                override fun buildDataDownloadLink(id: String): URI = buildAnimeLink(id)
                override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
            }

            serverInstance.stubFor(
                get(urlPathEqualTo("/anime/$id"))
                    .willReturn(
                        aResponse()
                            .withHeader("Content-Type", "text/html")
                            .withStatus(404)
                            .withBody("<html><head/><body></body></html>")
                    )
            )

            val downloader = MyanimelistDownloader(testConfig)

            // when
            val result = exceptionExpected<IllegalStateException> {
                downloader.download(id = id.toAnimeId(), onDeadEntry = { shouldNotBeInvoked() })
            }

            // then
            assertThat(result).hasMessage("Unknown 404 case for [myanimelistId=$id]")
        }
    }

    @Test
    fun `successfully load an entry`() {
        runBlocking {
            // given
            val id = 1535

            val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                override fun hostname(): Hostname = "localhost"
                override fun buildAnimeLink(id: AnimeId): URI = URI("http://localhost:$port/anime/$id")
                override fun buildDataDownloadLink(id: String): URI = buildAnimeLink(id)
                override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
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

            val downloader = MyanimelistDownloader(testConfig)

            // when
            val result = downloader.download(id = id.toAnimeId(), onDeadEntry = { shouldNotBeInvoked() })

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

            val testConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                override fun hostname(): Hostname = "localhost"
                override fun buildAnimeLink(id: AnimeId): URI = URI("http://localhost:$port/anime/$id")
                override fun buildDataDownloadLink(id: String): URI = buildAnimeLink(id)
                override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
            }

            serverInstance.stubFor(
                get(urlPathEqualTo("/anime/$id"))
                    .willReturn(
                        aResponse()
                            .withHeader("Content-Type", "text/html")
                            .withStatus(404)
                            .withBody("<html><head><title>404 Not Found - MyAnimeList.net</title><body></body></html>")
                    )
            )

            val downloader = MyanimelistDownloader(testConfig)

            // when
            val result = downloader.download(id = id.toAnimeId(), onDeadEntry = { hasDeadEntryBeenInvoked = true })

            // then
            assertThat(hasDeadEntryBeenInvoked).isTrue()
            assertThat(result).isBlank()
        }
    }

    @Test
    fun `throws an exception if the response body is empty`() {
        // given
        val id = 1535

        val testConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
            override fun hostname(): Hostname = "localhost"
            override fun buildAnimeLink(id: AnimeId): URI = MyanimelistConfig.buildAnimeLink(id)
            override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}:$port/anime/$id")
            override fun fileSuffix(): FileSuffix = MyanimelistConfig.fileSuffix()
        }

        serverInstance.stubFor(
            get(urlPathEqualTo("/anime/$id")).willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/html")
                    .withStatus(200)
                    .withBody(EMPTY)
            )
        )

        val downloader = MyanimelistDownloader(testConfig)

        // when
        val result = exceptionExpected<IllegalStateException> {
            downloader.download(id.toAnimeId()) { shouldNotBeInvoked() }
        }

        // then
        assertThat(result).hasMessage("Response body was blank for [myanimelistId=1535] with response code [200]")
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = MyanimelistDownloader.instance

                // when
                val result = MyanimelistDownloader.instance

                // then
                assertThat(result).isExactlyInstanceOf(MyanimelistDownloader::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}
