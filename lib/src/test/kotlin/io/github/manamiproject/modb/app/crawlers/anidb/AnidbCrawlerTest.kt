package io.github.manamiproject.modb.app.crawlers.anidb

import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anidb.AnidbWebViewConfig
import io.github.manamiproject.modb.app.*
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.crawlers.IdRangeSelector
import io.github.manamiproject.modb.app.crawlers.anidb.AnidbCrawler.Companion.ANIDB_PENDING_FILE_INDICATOR
import io.github.manamiproject.modb.app.crawlers.anidb.AnidbCrawler.Companion.ANIDB_PENDING_FILE_SUFFIX
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.app.network.NetworkController
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.RetryCase
import io.github.manamiproject.modb.core.httpclient.ThrowableRetryCase
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Nested
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.UnknownHostException
import kotlin.io.path.listDirectoryEntries
import kotlin.test.Test

internal class AnidbCrawlerTest {

    @Nested
    inner class ConstructorTests {

        @Test
        fun `adds RetryCases for ConnectException, UnknownHostException and NoRouteToHostException with restarting the NetworkController`() {
            tempDirectory {
                // given
                val cases = mutableListOf<RetryCase>()
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override fun addRetryCases(vararg retryCases: RetryCase): HttpClient {
                        cases.addAll(retryCases)
                        return this
                    }
                }

                var restartInvocations = 0
                val testNetworkController = object: NetworkController by TestNetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> {
                        restartInvocations++
                        return runBlocking { async { true } }
                    }
                }

                // when
                AnidbCrawler(
                    appConfig = TestAppConfig,
                    metaDataProviderConfig = TestMetaDataProviderConfig,
                    deadEntriesAccess = TestDeadEntriesAccessor,
                    idRangeSelector = TestIdRangeSelectorInt,
                    httpClient = testHttpClient,
                    apiDownloader = TestDownloader,
                    networkController = testNetworkController,
                )

                // then
                assertThat(cases).hasSize(3)

                val connectException = cases.find { (it as ThrowableRetryCase).retryIf(ConnectException()) }
                assertThat(connectException).isNotNull()
                connectException!!.executeBefore.invoke()
                assertThat(restartInvocations).isEqualTo(1)

                val unknownHostException = cases.find { (it as ThrowableRetryCase).retryIf(UnknownHostException()) }
                assertThat(unknownHostException).isNotNull()
                unknownHostException!!.executeBefore.invoke()
                assertThat(restartInvocations).isEqualTo(2)

                val noRouteToHostException = cases.find { (it as ThrowableRetryCase).retryIf(NoRouteToHostException()) }
                assertThat(noRouteToHostException).isNotNull()
                noRouteToHostException!!.executeBefore.invoke()
                assertThat(restartInvocations).isEqualTo(3)
            }
        }
    }

    @Nested
    inner class StartTests {

        @Test
        fun `doesn't do anything if the list of IDs is empty`() {
            // given
            val testIdRangeSelector = object : IdRangeSelector<Int> {
                override suspend fun idDownloadList(): List<Int> = emptyList()
            }

            val crawler = AnidbCrawler(
                appConfig = TestAppConfig,
                deadEntriesAccess = TestDeadEntriesAccessor,
                downloadControlStateAccessor = TestDownloadControlStateAccessor,
                idRangeSelector = testIdRangeSelector,
                apiDownloader = TestDownloader,
                webViewDownloader = TestDownloader,
                networkController = TestNetworkController,
            )

            // when
            assertThatNoException().isThrownBy {
                runTest { crawler.start() }
            }
        }

        @Test
        fun `successfully loads multiple entries`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnidbConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object : Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testIdRangeSelector = object: IdRangeSelector<Int> {
                    override suspend fun idDownloadList(): List<Int> = listOf(1535, 23, 1254, 424)
                }

                val successfulEntry = "successfully loaded content for"
                val testAnimeDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        return "<anime>$successfulEntry $id</anime>"
                    }
                }

                val crawler = AnidbCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    deadEntriesAccess = TestDeadEntriesAccessor,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    idRangeSelector = testIdRangeSelector,
                    apiDownloader = testAnimeDownloader,
                    webViewDownloader = TestDownloader,
                    networkController = TestNetworkController,
                )

                // when
                crawler.start()

                // then
                testIdRangeSelector.idDownloadList().forEach { id ->
                    val result = tempDir.resolve("$id.xml").readFile()
                    assertThat(result).isEqualTo("<anime>$successfulEntry $id</anime>")
                }
            }
        }

        @Test
        fun `removes IDs of anime which are pending addition and have been downloaded already`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnidbConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object : Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testIdRangeSelector = object: IdRangeSelector<Int> {
                    override suspend fun idDownloadList(): List<Int> = listOf(1535, 23, 1254, 424)
                }

                val successfulEntry = "successfully loaded content for"
                val testAnimeDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        return when(id) {
                            "1254" -> shouldNotBeInvoked()
                            else -> "<anime>$successfulEntry $id</anime>"
                        }
                    }
                }

                ANIDB_PENDING_FILE_INDICATOR.writeToFile(tempDir.resolve("1254.${ANIDB_PENDING_FILE_SUFFIX}"))

                val crawler = AnidbCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    apiDownloader = testAnimeDownloader,
                    deadEntriesAccess = TestDeadEntriesAccessor,
                    idRangeSelector = testIdRangeSelector,
                    networkController = TestNetworkController,
                )

                // when
                crawler.start()

                // then
                assertThat(tempDir).isDirectoryContaining { it.fileName() == "1535.xml" }
                assertThat(tempDir).isDirectoryContaining { it.fileName() == "23.xml" }
                assertThat(tempDir).isDirectoryContaining { it.fileName() == "424.xml" }
                assertThat(tempDir).isDirectoryNotContaining { it.fileName() == "1254.xml" }
            }
        }

        @Test
        fun `doesn't to anything if downloader returns an empty string`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnidbConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object : Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testIdRangeSelector = object: IdRangeSelector<Int> {
                    override suspend fun idDownloadList(): List<Int> = listOf(1000)
                }

                val testAnimeDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String = EMPTY
                }

                val crawler = AnidbCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    apiDownloader = testAnimeDownloader,
                    deadEntriesAccess = TestDeadEntriesAccessor,
                    idRangeSelector = testIdRangeSelector,
                    networkController = TestNetworkController,
                )

                // when
                crawler.start()

                // then
                assertThat(tempDir.listDirectoryEntries()).isEmpty()
            }
        }

        @Test
        fun `throws exception if dead entry function is invoked on an API call, because that is not possible`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnidbWebViewConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object : Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testIdRangeSelector = object: IdRangeSelector<Int> {
                    override suspend fun idDownloadList(): List<Int> = listOf(1535)
                }

                val testAnimeDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        onDeadEntry.invoke(id)
                        return EMPTY
                    }
                }

                val crawler = AnidbCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    deadEntriesAccess = TestDeadEntriesAccessor,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    idRangeSelector = testIdRangeSelector,
                    apiDownloader = testAnimeDownloader,
                    webViewDownloader = TestDownloader,
                    networkController = TestNetworkController,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    crawler.start()
                }

                // then
                assertThat(result).hasMessage("Should not happen [anidbId=1535].")
            }
        }

        @Test
        fun `an entry which exists in DCS has been deleted`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnidbConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object : Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testIdRangeSelector = object: IdRangeSelector<Int> {
                    override suspend fun idDownloadList(): List<Int> = listOf(1535)
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun dcsEntryExists(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): Boolean = true
                }

                val testApiAnimeDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        return loadTestResource("crawler/anidb.AnidbCrawlerTest/not_found.xml")
                    }
                }

                var hasBeenInvoked = false
                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override suspend fun addDeadEntry(animeId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig) {
                        hasBeenInvoked = true
                    }
                }

                val crawler = AnidbCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    deadEntriesAccess = testDeadEntriesAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    idRangeSelector = testIdRangeSelector,
                    apiDownloader = testApiAnimeDownloader,
                    webViewDownloader = TestDownloader,
                    networkController = TestNetworkController,
                )

                // when
                crawler.start()

                // then
                assertThat(hasBeenInvoked).isTrue()
            }
        }

        @Test
        fun `an entry which doesn't exist in DCS is a deleted entry`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnidbConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object : Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testIdRangeSelector = object: IdRangeSelector<Int> {
                    override suspend fun idDownloadList(): List<Int> = listOf(1535)
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun dcsEntryExists(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): Boolean = false
                }

                val testApiAnimeDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        return loadTestResource("crawler/anidb.AnidbCrawlerTest/not_found.xml")
                    }
                }

                val testWebViewAnimeDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        onDeadEntry.invoke(id)
                        return EMPTY
                    }
                }

                var hasBeenInvoked = false
                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override suspend fun addDeadEntry(animeId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig) {
                        hasBeenInvoked = true
                    }
                }

                val crawler = AnidbCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    deadEntriesAccess = testDeadEntriesAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    idRangeSelector = testIdRangeSelector,
                    apiDownloader = testApiAnimeDownloader,
                    webViewDownloader = testWebViewAnimeDownloader,
                    networkController = TestNetworkController,
                )

                // when
                crawler.start()

                // then
                assertThat(hasBeenInvoked).isTrue()
            }
        }

        @Test
        fun `creates a static file indicating to prevent recurring downloads if the entry is in status PENDING_ADDITION`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnidbConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object : Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testIdRangeSelector = object: IdRangeSelector<Int> {
                    override suspend fun idDownloadList(): List<Int> = listOf(1535)
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun dcsEntryExists(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): Boolean = false
                }

                val testApiAnimeDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        return loadTestResource("crawler/anidb.AnidbCrawlerTest/not_found.xml")
                    }
                }

                val testWebViewAnimeDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String = loadTestResource("crawler/anidb.AnidbCrawlerTest/addition_pending.html")
                }

                val crawler = AnidbCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    deadEntriesAccess = TestDeadEntriesAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    idRangeSelector = testIdRangeSelector,
                    apiDownloader = testApiAnimeDownloader,
                    webViewDownloader = testWebViewAnimeDownloader,
                    networkController = TestNetworkController,
                )

                // when
                crawler.start()

                // then
                assertThat(tempDir.resolve("1535.${ANIDB_PENDING_FILE_SUFFIX}")).exists()
            }
        }

        @Test
        fun `throws an exception if the web ui response is of an unexpected type`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnidbConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object : Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testIdRangeSelector = object: IdRangeSelector<Int> {
                    override suspend fun idDownloadList(): List<Int> = listOf(1535)
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun dcsEntryExists(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): Boolean = false
                }

                val testApiAnimeDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        return loadTestResource("crawler/anidb.AnidbCrawlerTest/not_found.xml")
                    }
                }

                val testWebViewAnimeDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        return "<anime>unexpected existing entry</anime>"
                    }
                }

                val crawler = AnidbCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    deadEntriesAccess = TestDeadEntriesAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    idRangeSelector = testIdRangeSelector,
                    apiDownloader = testApiAnimeDownloader,
                    webViewDownloader = testWebViewAnimeDownloader,
                    networkController = TestNetworkController,
                )

                // when
                val result = exceptionExpected < IllegalStateException> {
                    crawler.start()
                }

                // then
                assertThat(result).hasMessage("Type result [UNKNOWN] after web view download for id [anidbId=1535].")
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = AnidbCrawler.instance

                // when
                val result = AnidbCrawler.instance

                // then
                assertThat(result).isExactlyInstanceOf(AnidbCrawler::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}