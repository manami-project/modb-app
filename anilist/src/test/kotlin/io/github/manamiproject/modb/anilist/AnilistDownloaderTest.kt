package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.httpclient.*
import io.github.manamiproject.modb.test.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import java.net.URI
import java.net.URL


internal class AnilistDownloaderTest {

    @Nested
    inner class DownloadTests {

        @Test
        fun `throws exception if the repsonse body is blank`() {
            runBlocking {
                // given
                val testAnilistConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun hostname(): Hostname = "localhost"
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}/graphql")
                    override fun fileSuffix(): FileSuffix = AnilistConfig.fileSuffix()
                }

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun post(
                        url: URL,
                        requestBody: RequestBody,
                        headers: Map<String, Collection<String>>
                    ): HttpResponse = HttpResponse(
                        code = 200,
                        body = EMPTY.toByteArray(),
                    )
                }

                val downloader = AnilistDownloader(
                    metaDataProviderConfig = testAnilistConfig,
                    httpClient = testHttpClient,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    downloader.download("123") { shouldNotBeInvoked() }
                }

                // then
                assertThat(result).hasMessage("Response body was blank for [anilistId=123] with response code [200]")
            }
        }

        @Test
        fun `successfully return http response`() {
            runBlocking {
                // given
                val testAnilistConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun hostname(): Hostname = "localhost"
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}/graphql")
                    override fun fileSuffix(): FileSuffix = AnilistConfig.fileSuffix()
                }

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun post(
                        url: URL,
                        requestBody: RequestBody,
                        headers: Map<String, Collection<String>>
                    ): HttpResponse = HttpResponse(
                        code = 200,
                        body = "success".toByteArray(),
                    )
                }

                val downloader = AnilistDownloader(
                    metaDataProviderConfig = testAnilistConfig,
                    httpClient = testHttpClient,
                )

                // when
                val result = downloader.download("123") { shouldNotBeInvoked() }

                // then
                assertThat(result).isEqualTo("success")
            }
        }

        @Test
        fun `creates dead entry on http response code 404`() {
            runBlocking {
                // given
                val testAnilistConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun hostname(): Hostname = "localhost"
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}/graphql")
                    override fun fileSuffix(): FileSuffix = AnilistConfig.fileSuffix()
                }

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun post(
                        url: URL,
                        requestBody: RequestBody,
                        headers: Map<String, Collection<String>>
                    ): HttpResponse = HttpResponse(
                        code = 404,
                        body = "not found".toByteArray(),
                    )
                }

                val downloader = AnilistDownloader(
                    metaDataProviderConfig = testAnilistConfig,
                    httpClient = testHttpClient,
                )

                var deadEntryInvocation = EMPTY

                // when
                downloader.download("123") {
                    deadEntryInvocation = "$it added to dead entry"
                }

                // then
                assertThat(deadEntryInvocation).isEqualTo("123 added to dead entry")
            }
        }

        @Test
        fun `throws exception in case of an unhandled http response code`() {
            runBlocking {
                // given
                val testAnilistConfig = object : MetaDataProviderConfig by MetaDataProviderTestConfig {
                    override fun hostname(): Hostname = "localhost"
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                    override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}/graphql")
                    override fun fileSuffix(): FileSuffix = AnilistConfig.fileSuffix()
                }

                val testHttpClient = object : HttpClient by TestHttpClient {
                    override suspend fun post(
                        url: URL,
                        requestBody: RequestBody,
                        headers: Map<String, Collection<String>>
                    ): HttpResponse = HttpResponse(
                        code = 400,
                        body = "error".toByteArray(),
                    )
                }

                val downloader = AnilistDownloader(
                    metaDataProviderConfig = testAnilistConfig,
                    httpClient = testHttpClient,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    downloader.download("123") { shouldNotBeInvoked() }
                }

                // then
                assertThat(result).hasMessage("Unable to determine the correct case for [anilistId=123], [responseCode=400]")
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = AnilistDownloader.instance

                // when
                val result = AnilistDownloader.instance

                // then
                assertThat(result).isExactlyInstanceOf(AnilistDownloader::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}