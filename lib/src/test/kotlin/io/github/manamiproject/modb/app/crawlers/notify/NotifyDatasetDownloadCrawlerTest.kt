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
import io.github.manamiproject.modb.test.shouldNotBeInvoked
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
    fun `throws exception if response code for anime download is not OK`() {
        // given
        val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
            override fun hostname(): Hostname = NotifyAnimeDatasetDownloaderConfig.hostname()
            override fun buildDataDownloadLink(id: String): URI = NotifyAnimeDatasetDownloaderConfig.buildDataDownloadLink(id)
        }

        val testHttpClient = object: HttpClient by TestHttpClient {
            override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                return HttpResponse(
                    code = 500,
                    body = EMPTY,
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
        assertThat(result).hasMessage("Unhandled response code [500] when downloading anime data.")
    }

    @Test
    fun `throws exception if response code for relations download is not OK`() {
        // given
        tempDirectory {
            val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = NotifyAnimeDatasetDownloaderConfig.hostname()
                override fun buildDataDownloadLink(id: String): URI = NotifyAnimeDatasetDownloaderConfig.buildDataDownloadLink(id)
                override fun fileSuffix(): FileSuffix = NotifyAnimeDatasetDownloaderConfig.fileSuffix()
            }

            val testNotifyRelationsConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = NotifyRelationsDatasetDownloaderConfig.hostname()
                override fun buildDataDownloadLink(id: String): URI = NotifyRelationsDatasetDownloaderConfig.buildDataDownloadLink(id)
                override fun fileSuffix(): FileSuffix = NotifyRelationsDatasetDownloaderConfig.fileSuffix()
            }

            val testWorkingDir = tempDir.resolve("notify.moe").createDirectory()
            val testRelationsWorkingDir = tempDir.resolve("notify.moe-relations").createDirectory()
            val testAppConfig = object: Config by TestAppConfig {
                override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = when (metaDataProviderConfig.buildDataDownloadLink()) {
                    NotifyAnimeDatasetDownloaderConfig.buildDataDownloadLink() -> testWorkingDir
                    NotifyRelationsDatasetDownloaderConfig.buildDataDownloadLink() -> testRelationsWorkingDir
                    else -> shouldNotBeInvoked()
                }
            }

            val testHttpClient = object: HttpClient by TestHttpClient {
                override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = when (url) {
                    NotifyAnimeDatasetDownloaderConfig.buildDataDownloadLink().toURL() -> HttpResponse(
                        code = 200,
                        body = loadTestResource<ByteArray>("crawler/notify/NotifyDatasetDownloadCrawlerTest/example-anime-dataset.txt"),
                    )
                    else -> HttpResponse(
                        code = 500,
                        body = "error",
                    )
                }
            }

            val dcsDir = tempDir.resolve("dcs").createDirectory()
            val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = dcsDir
            }

            val crawler = NotifyDatasetDownloadCrawler(
                appConfig = testAppConfig,
                metaDataProviderConfig = testNotifyConfig,
                relationsMetaDataProviderConfig = testNotifyRelationsConfig,
                httpClient = testHttpClient,
                downloadControlStateAccessor = testDownloadControlStateAccessor,
                deadEntriesAccessor = TestDeadEntriesAccessor,
            )

            // when
            val result = exceptionExpected<IllegalStateException> {
                crawler.start()
            }

            //then
            assertThat(result).hasMessage("Unhandled response code [500] when downloading relations.")
        }
    }

    @Test
    fun `correctly create raw files`() {
        // given
        tempDirectory {
            val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = NotifyAnimeDatasetDownloaderConfig.hostname()
                override fun buildDataDownloadLink(id: String): URI = NotifyAnimeDatasetDownloaderConfig.buildDataDownloadLink(id)
                override fun fileSuffix(): FileSuffix = NotifyAnimeDatasetDownloaderConfig.fileSuffix()
            }

            val testNotifyRelationsConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = NotifyRelationsDatasetDownloaderConfig.hostname()
                override fun buildDataDownloadLink(id: String): URI = NotifyRelationsDatasetDownloaderConfig.buildDataDownloadLink(id)
                override fun fileSuffix(): FileSuffix = NotifyRelationsDatasetDownloaderConfig.fileSuffix()
            }

            val testWorkingDir = tempDir.resolve("notify.moe").createDirectory()
            val testRelationsWorkingDir = tempDir.resolve("notify.moe-relations").createDirectory()
            val testAppConfig = object: Config by TestAppConfig {
                override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = when (metaDataProviderConfig.buildDataDownloadLink()) {
                    NotifyAnimeDatasetDownloaderConfig.buildDataDownloadLink() -> testWorkingDir
                    NotifyRelationsDatasetDownloaderConfig.buildDataDownloadLink() -> testRelationsWorkingDir
                    else -> shouldNotBeInvoked()
                }
            }

            val testHttpClient = object: HttpClient by TestHttpClient {
                override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                    val body: ByteArray = when (url) {
                        NotifyAnimeDatasetDownloaderConfig.buildDataDownloadLink().toURL() -> loadTestResource("crawler/notify/NotifyDatasetDownloadCrawlerTest/example-anime-dataset.txt")
                        NotifyRelationsDatasetDownloaderConfig.buildDataDownloadLink().toURL() -> loadTestResource("crawler/notify/NotifyDatasetDownloadCrawlerTest/example-relations-dataset.txt")
                        else -> shouldNotBeInvoked()
                    }

                    return HttpResponse(
                        code = 200,
                        body = body,
                    )
                }
            }

            val dcsDir = tempDir.resolve("dcs").createDirectory()
            val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = dcsDir
            }

            val crawler = NotifyDatasetDownloadCrawler(
                appConfig = testAppConfig,
                metaDataProviderConfig = testNotifyConfig,
                relationsMetaDataProviderConfig = testNotifyRelationsConfig,
                httpClient = testHttpClient,
                downloadControlStateAccessor = testDownloadControlStateAccessor,
                deadEntriesAccessor = TestDeadEntriesAccessor,
            )

            val expectedFiles = setOf(
                testWorkingDir.resolve("iEOy6VGHg.${testNotifyConfig.fileSuffix()}"),
                testWorkingDir.resolve("_0f7tKmig.${testNotifyConfig.fileSuffix()}"),
                testWorkingDir.resolve("0kLGhFimR.${testNotifyConfig.fileSuffix()}"),
                testWorkingDir.resolve("1vie5FmmR.${testNotifyConfig.fileSuffix()}"),
            )

            val expectedRelationsFiles = setOf(
                testRelationsWorkingDir.resolve("aL4F2FimR.${testNotifyRelationsConfig.fileSuffix()}"),
                testRelationsWorkingDir.resolve("0NixcKimg.${testNotifyRelationsConfig.fileSuffix()}"),
                testRelationsWorkingDir.resolve("s6dGL1FVR.${testNotifyRelationsConfig.fileSuffix()}"),
                testRelationsWorkingDir.resolve("FZEIkXeSg.${testNotifyRelationsConfig.fileSuffix()}"),
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
            expectedRelationsFiles.forEach {
                assertThat(it).exists()
                val content = it.readFile()
                assertThat(content).startsWith("{")
                assertThat(content).contains(it.fileName().remove(".${testNotifyRelationsConfig.fileSuffix()}"))
                assertThat(content).endsWith("}")
            }
        }
    }

    @Test
    fun `correctly ignore entries which only have an ID`() {
        // given
        tempDirectory {
            val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = NotifyAnimeDatasetDownloaderConfig.hostname()
                override fun buildDataDownloadLink(id: String): URI = NotifyAnimeDatasetDownloaderConfig.buildDataDownloadLink(id)
                override fun fileSuffix(): FileSuffix = NotifyAnimeDatasetDownloaderConfig.fileSuffix()
            }

            val testNotifyRelationsConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = NotifyRelationsDatasetDownloaderConfig.hostname()
                override fun buildDataDownloadLink(id: String): URI = NotifyRelationsDatasetDownloaderConfig.buildDataDownloadLink(id)
                override fun fileSuffix(): FileSuffix = NotifyRelationsDatasetDownloaderConfig.fileSuffix()
            }

            val testWorkingDir = tempDir.resolve("notify.moe").createDirectory()
            val testRelationsWorkingDir = tempDir.resolve("notify.moe-relations").createDirectory()
            val testAppConfig = object: Config by TestAppConfig {
                override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = when (metaDataProviderConfig.buildDataDownloadLink()) {
                    NotifyAnimeDatasetDownloaderConfig.buildDataDownloadLink() -> testWorkingDir
                    NotifyRelationsDatasetDownloaderConfig.buildDataDownloadLink() -> testRelationsWorkingDir
                    else -> shouldNotBeInvoked()
                }
            }

            val testHttpClient = object: HttpClient by TestHttpClient {
                override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                    val body: ByteArray = when (url) {
                        NotifyAnimeDatasetDownloaderConfig.buildDataDownloadLink().toURL() -> loadTestResource("crawler/notify/NotifyDatasetDownloadCrawlerTest/ignore-dead-entries.txt")
                        NotifyRelationsDatasetDownloaderConfig.buildDataDownloadLink().toURL() -> loadTestResource("crawler/notify/NotifyDatasetDownloadCrawlerTest/example-relations-dataset.txt")
                        else -> shouldNotBeInvoked()
                    }

                    return HttpResponse(
                        code = 200,
                        body = body,
                    )
                }
            }

            val dcsDir = tempDir.resolve("dcs").createDirectory()
            val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = dcsDir
            }

            val crawler = NotifyDatasetDownloadCrawler(
                appConfig = testAppConfig,
                metaDataProviderConfig = testNotifyConfig,
                relationsMetaDataProviderConfig = testNotifyRelationsConfig,
                httpClient = testHttpClient,
                downloadControlStateAccessor = testDownloadControlStateAccessor,
                deadEntriesAccessor = TestDeadEntriesAccessor,
            )

            val expectedFiles = setOf(
                testWorkingDir.resolve("iEOy6VGHg.${testNotifyConfig.fileSuffix()}"),
                testWorkingDir.resolve("_0f7tKmig.${testNotifyConfig.fileSuffix()}"),
                testWorkingDir.resolve("0kLGhFimR.${testNotifyConfig.fileSuffix()}"),
                testWorkingDir.resolve("1vie5FmmR.${testNotifyConfig.fileSuffix()}"),
            )

            val expectedRelationsFiles = setOf(
                testRelationsWorkingDir.resolve("aL4F2FimR.${testNotifyRelationsConfig.fileSuffix()}"),
                testRelationsWorkingDir.resolve("0NixcKimg.${testNotifyRelationsConfig.fileSuffix()}"),
                testRelationsWorkingDir.resolve("s6dGL1FVR.${testNotifyRelationsConfig.fileSuffix()}"),
                testRelationsWorkingDir.resolve("FZEIkXeSg.${testNotifyRelationsConfig.fileSuffix()}"),
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
            expectedRelationsFiles.forEach {
                assertThat(it).exists()
                val content = it.readFile()
                assertThat(content).startsWith("{")
                assertThat(content).contains(it.fileName().remove(".${testNotifyRelationsConfig.fileSuffix()}"))
                assertThat(content).endsWith("}")
            }
        }
    }

    @Test
    fun `correctly identify dead entry`() {
        // given
        tempDirectory {
            val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = NotifyAnimeDatasetDownloaderConfig.hostname()
                override fun buildDataDownloadLink(id: String): URI = NotifyAnimeDatasetDownloaderConfig.buildDataDownloadLink(id)
                override fun fileSuffix(): FileSuffix = NotifyAnimeDatasetDownloaderConfig.fileSuffix()
            }

            val testNotifyRelationsConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = NotifyRelationsDatasetDownloaderConfig.hostname()
                override fun buildDataDownloadLink(id: String): URI = NotifyRelationsDatasetDownloaderConfig.buildDataDownloadLink(id)
                override fun fileSuffix(): FileSuffix = NotifyRelationsDatasetDownloaderConfig.fileSuffix()
            }

            val testWorkingDir = tempDir.resolve("notify.moe").createDirectory()
            val testRelationsWorkingDir = tempDir.resolve("notify.moe-relations").createDirectory()
            val testAppConfig = object: Config by TestAppConfig {
                override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = when (metaDataProviderConfig.buildDataDownloadLink()) {
                    NotifyAnimeDatasetDownloaderConfig.buildDataDownloadLink() -> testWorkingDir
                    NotifyRelationsDatasetDownloaderConfig.buildDataDownloadLink() -> testRelationsWorkingDir
                    else -> shouldNotBeInvoked()
                }
            }

            val testHttpClient = object: HttpClient by TestHttpClient {
                override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                    val body: ByteArray = when (url) {
                        NotifyAnimeDatasetDownloaderConfig.buildDataDownloadLink().toURL() -> loadTestResource("crawler/notify/NotifyDatasetDownloadCrawlerTest/example-anime-dataset.txt")
                        NotifyRelationsDatasetDownloaderConfig.buildDataDownloadLink().toURL() -> loadTestResource("crawler/notify/NotifyDatasetDownloadCrawlerTest/example-relations-dataset.txt")
                        else -> shouldNotBeInvoked()
                    }

                    return HttpResponse(
                        code = 200,
                        body = body,
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

            val crawler = NotifyDatasetDownloadCrawler(
                appConfig = testAppConfig,
                metaDataProviderConfig = testNotifyConfig,
                relationsMetaDataProviderConfig = testNotifyRelationsConfig,
                httpClient = testHttpClient,
                downloadControlStateAccessor = testDownloadControlStateAccessor,
                deadEntriesAccessor = testDeadEntriesAccessor,
            )

            val expectedFiles = setOf(
                testWorkingDir.resolve("iEOy6VGHg.${testNotifyConfig.fileSuffix()}"),
                testWorkingDir.resolve("_0f7tKmig.${testNotifyConfig.fileSuffix()}"),
                testWorkingDir.resolve("0kLGhFimR.${testNotifyConfig.fileSuffix()}"),
                testWorkingDir.resolve("1vie5FmmR.${testNotifyConfig.fileSuffix()}"),
            )

            val expectedRelationsFiles = setOf(
                testRelationsWorkingDir.resolve("aL4F2FimR.${testNotifyRelationsConfig.fileSuffix()}"),
                testRelationsWorkingDir.resolve("0NixcKimg.${testNotifyRelationsConfig.fileSuffix()}"),
                testRelationsWorkingDir.resolve("s6dGL1FVR.${testNotifyRelationsConfig.fileSuffix()}"),
                testRelationsWorkingDir.resolve("FZEIkXeSg.${testNotifyRelationsConfig.fileSuffix()}"),
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
            expectedRelationsFiles.forEach {
                assertThat(it).exists()
                val content = it.readFile()
                assertThat(content).startsWith("{")
                assertThat(content).contains(it.fileName().remove(".${testNotifyRelationsConfig.fileSuffix()}"))
                assertThat(content).endsWith("}")
            }
        }
    }

    @Test
    fun `don't create raw files if the file already exists`() {
        // given
        tempDirectory {
            val testNotifyConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = NotifyAnimeDatasetDownloaderConfig.hostname()
                override fun buildDataDownloadLink(id: String): URI = NotifyAnimeDatasetDownloaderConfig.buildDataDownloadLink(id)
                override fun fileSuffix(): FileSuffix = NotifyAnimeDatasetDownloaderConfig.fileSuffix()
            }

            val testNotifyRelationsConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = NotifyRelationsDatasetDownloaderConfig.hostname()
                override fun buildDataDownloadLink(id: String): URI = NotifyRelationsDatasetDownloaderConfig.buildDataDownloadLink(id)
                override fun fileSuffix(): FileSuffix = NotifyRelationsDatasetDownloaderConfig.fileSuffix()
            }

            val testWorkingDir = tempDir.resolve("notify.moe").createDirectory()
            val testRelationsWorkingDir = tempDir.resolve("notify.moe-relations").createDirectory()
            val testAppConfig = object: Config by TestAppConfig {
                override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = when (metaDataProviderConfig.buildDataDownloadLink()) {
                    NotifyAnimeDatasetDownloaderConfig.buildDataDownloadLink() -> testWorkingDir
                    NotifyRelationsDatasetDownloaderConfig.buildDataDownloadLink() -> testRelationsWorkingDir
                    else -> shouldNotBeInvoked()
                }
            }

            val testHttpClient = object: HttpClient by TestHttpClient {
                override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                    val body: ByteArray = when (url) {
                        NotifyAnimeDatasetDownloaderConfig.buildDataDownloadLink().toURL() -> loadTestResource("crawler/notify/NotifyDatasetDownloadCrawlerTest/example-anime-dataset.txt")
                        NotifyRelationsDatasetDownloaderConfig.buildDataDownloadLink().toURL() -> loadTestResource("crawler/notify/NotifyDatasetDownloadCrawlerTest/example-relations-dataset.txt")
                        else -> shouldNotBeInvoked()
                    }

                    return HttpResponse(
                        code = 200,
                        body = body,
                    )
                }
            }

            val dcsDir = tempDir.resolve("dcs").createDirectory()
            val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = dcsDir
            }

            val crawler = NotifyDatasetDownloadCrawler(
                appConfig = testAppConfig,
                metaDataProviderConfig = testNotifyConfig,
                relationsMetaDataProviderConfig = testNotifyRelationsConfig,
                httpClient = testHttpClient,
                downloadControlStateAccessor = testDownloadControlStateAccessor,
                deadEntriesAccessor = TestDeadEntriesAccessor,
            )

            val file = testWorkingDir.resolve("iEOy6VGHg.${testNotifyConfig.fileSuffix()}")
            "previously created content for anime".writeToFile(file)

            val relationsFile = testRelationsWorkingDir.resolve("aL4F2FimR.${testNotifyRelationsConfig.fileSuffix()}")
            "previously created content for relations".writeToFile(relationsFile)

            // when
            crawler.start()

            //then
            assertThat(file.readFile()).isEqualTo("previously created content for anime")
            assertThat(relationsFile.readFile()).isEqualTo("previously created content for relations")
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
                assertThat(result === previous).isTrue()
            }
        }
    }
}