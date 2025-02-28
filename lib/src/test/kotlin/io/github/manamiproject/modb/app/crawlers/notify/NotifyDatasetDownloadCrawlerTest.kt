package io.github.manamiproject.modb.app.crawlers.notify

import io.github.manamiproject.modb.app.*
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DOWNLOAD_CONTROL_STATE_FILE_SUFFIX
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import java.net.URL
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.test.Test

internal class NotifyDatasetDownloadCrawlerTest {

    @Test
    fun `throws exception if response code is not OK`() {
        // given
        val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
            override fun hostname(): Hostname = NotifyDatasetDownloaderConfig.hostname()
            override fun buildDataDownloadLink(id: String): URI = NotifyDatasetDownloaderConfig.buildDataDownloadLink(id)
        }

        val testHttpClient = object: HttpClient by TestHttpClient {
            override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                return HttpResponse(
                    code = 500,
                    body = EMPTY.toByteArray(),
                )
            }
        }

        val crawler = NotifyDatasetDownloadCrawler(
            appConfig = TestAppConfig,
            metaDataProviderConfig = testNotifyConfig,
            httpClient = testHttpClient,
            downloadControlStateAccessor = TestDownloadControlStateAccessor,
            deadEntriesAccessor = TestDeadEntriesAccessor,
        )

        // when
        val result = exceptionExpected<IllegalStateException> {
            crawler.start()
        }

        //then
        assertThat(result).hasMessage("Unhandled response code [500].")
    }

    @Test
    fun `correctly create conv files`() {
        // given
        tempDirectory {
            val testAppConfig = object: Config by TestAppConfig {
                override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
            }

            val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = NotifyDatasetDownloaderConfig.hostname()
                override fun buildDataDownloadLink(id: String): URI = NotifyDatasetDownloaderConfig.buildDataDownloadLink(id)
                override fun fileSuffix(): FileSuffix = NotifyDatasetDownloaderConfig.fileSuffix()
            }

            val dcsDir = tempDir.resolve("dcs").createDirectory()
            val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = dcsDir
            }

            val testHttpClient = object: HttpClient by TestHttpClient {
                override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                    return HttpResponse(
                        code = 200,
                        body = loadTestResource("crawler/notify/NotifyDatasetDownloadCrawlerTest/example-dataset.txt"),
                    )
                }
            }

            val expectedFiles = setOf(
                tempDir.resolve("iEOy6VGHg.${testNotifyConfig.fileSuffix()}"),
                tempDir.resolve("_0f7tKmig.${testNotifyConfig.fileSuffix()}"),
                tempDir.resolve("0kLGhFimR.${testNotifyConfig.fileSuffix()}"),
                tempDir.resolve("1vie5FmmR.${testNotifyConfig.fileSuffix()}"),
            )

            val crawler = NotifyDatasetDownloadCrawler(
                appConfig = testAppConfig,
                metaDataProviderConfig = testNotifyConfig,
                httpClient = testHttpClient,
                downloadControlStateAccessor = testDownloadControlStateAccessor,
                deadEntriesAccessor = TestDeadEntriesAccessor,
            )

            // when
            crawler.start()

            //then
            expectedFiles.forEach {
                assertThat(it).exists()
                val content = it.readFile()
                assertThat(content).startsWith("{")
                assertThat(content).contains(it.fileName().remove(".${testNotifyConfig.fileSuffix()}"))
                assertThat(content).endsWith("}")
            }
        }
    }

    @Test
    fun `correctly identify dead entry`() {
        // given
        tempDirectory {
            val testAppConfig = object: Config by TestAppConfig {
                override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
            }

            val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = NotifyDatasetDownloaderConfig.hostname()
                override fun buildDataDownloadLink(id: String): URI = NotifyDatasetDownloaderConfig.buildDataDownloadLink(id)
                override fun fileSuffix(): FileSuffix = NotifyDatasetDownloaderConfig.fileSuffix()
            }

            val testHttpClient = object: HttpClient by TestHttpClient {
                override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                    return HttpResponse(
                        code = 200,
                        body = loadTestResource("crawler/notify/NotifyDatasetDownloadCrawlerTest/example-dataset.txt"),
                    )
                }
            }

            val dcsDir = tempDir.resolve("dcs").createDirectory()
            dcsDir.resolve("iEOy6VGHg.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
            dcsDir.resolve("_0f7tKmig.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
            dcsDir.resolve("to-be-removed.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
            dcsDir.resolve("0kLGhFimR.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
            dcsDir.resolve("1vie5FmmR.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

            val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = dcsDir
            }

            val invocations = mutableListOf<AnimeId>()
            val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                override suspend fun addDeadEntry(animeId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig) {
                    invocations.add(animeId)
                }
            }

            val expectedFiles = setOf(
                tempDir.resolve("iEOy6VGHg.${testNotifyConfig.fileSuffix()}"),
                tempDir.resolve("_0f7tKmig.${testNotifyConfig.fileSuffix()}"),
                tempDir.resolve("0kLGhFimR.${testNotifyConfig.fileSuffix()}"),
                tempDir.resolve("1vie5FmmR.${testNotifyConfig.fileSuffix()}"),
            )

            val crawler = NotifyDatasetDownloadCrawler(
                appConfig = testAppConfig,
                metaDataProviderConfig = testNotifyConfig,
                httpClient = testHttpClient,
                downloadControlStateAccessor = testDownloadControlStateAccessor,
                deadEntriesAccessor = testDeadEntriesAccessor,
            )

            // when
            crawler.start()

            //then
            assertThat(invocations).containsExactly(
                "to-be-removed",
            )
            expectedFiles.forEach {
                assertThat(it).exists()
                val content = it.readFile()
                assertThat(content).startsWith("{")
                assertThat(content).contains(it.fileName().remove(".${testNotifyConfig.fileSuffix()}"))
                assertThat(content).endsWith("}")
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = NotifyDatasetDownloadCrawler.instance

                // when
                val result = NotifyDatasetDownloadCrawler.instance

                // then
                assertThat(result).isExactlyInstanceOf(NotifyDatasetDownloadCrawler::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}