package io.github.manamiproject.modb.app.crawlers.anidb

import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anidb.AnidbDownloader.Companion.ANIDB_PENDING_FILE_INDICATOR
import io.github.manamiproject.modb.anidb.CrawlerDetectedException
import io.github.manamiproject.modb.app.*
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.crawlers.IdRangeSelector
import io.github.manamiproject.modb.app.crawlers.anidb.AnidbCrawler.Companion.ANIDB_PENDING_FILE_SUFFIX
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.network.NetworkController
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.RetryCase
import io.github.manamiproject.modb.core.httpclient.ThrowableRetryCase
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
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
                    downloader = TestDownloader,
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
                downloader = TestDownloader,
                deadEntriesAccess = TestDeadEntriesAccessor,
                idRangeSelector = testIdRangeSelector,
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
                        return "$successfulEntry $id"
                    }
                }

                val crawler = AnidbCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloader = testAnimeDownloader,
                    deadEntriesAccess = TestDeadEntriesAccessor,
                    idRangeSelector = testIdRangeSelector,
                    networkController = TestNetworkController,
                )

                // when
                crawler.start()

                // then
                testIdRangeSelector.idDownloadList().forEach { id ->
                    val result = tempDir.resolve("$id.html").readFile()
                    assertThat(result).isEqualTo("$successfulEntry $id")
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
                            else -> "$successfulEntry $id"
                        }
                    }
                }

                ANIDB_PENDING_FILE_INDICATOR.writeToFile(tempDir.resolve("1254.${ANIDB_PENDING_FILE_SUFFIX}"))

                val crawler = AnidbCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloader = testAnimeDownloader,
                    deadEntriesAccess = TestDeadEntriesAccessor,
                    idRangeSelector = testIdRangeSelector,
                    networkController = TestNetworkController,
                )

                // when
                crawler.start()

                // then
                assertThat(tempDir).isDirectoryContaining { it.fileName() == "1535.html" }
                assertThat(tempDir).isDirectoryContaining { it.fileName() == "23.html" }
                assertThat(tempDir).isDirectoryContaining { it.fileName() == "424.html" }
                assertThat(tempDir).isDirectoryNotContaining { it.fileName() == "1254.html" }
            }
        }

        @Test
        fun `passes the animeId to the dead entries accessor if the downloader triggers a dead entry`() {
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
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        onDeadEntry.invoke(id)
                        return EMPTY
                    }
                }

                val invocations = mutableListOf<AnimeId>()
                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override suspend fun addDeadEntry(animeId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig) {
                        invocations.add(animeId)
                    }
                }

                val crawler = AnidbCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloader = testAnimeDownloader,
                    deadEntriesAccess = testDeadEntriesAccessor,
                    idRangeSelector = testIdRangeSelector,
                    networkController = TestNetworkController,
                )

                // when
                crawler.start()

                // then
                assertThat(tempDir.listDirectoryEntries()).isEmpty()
                assertThat(invocations).containsExactlyInAnyOrder(
                    "1000",
                )
            }
        }

        @Test
        fun `directly throws an exception if it's not one of the cases that restart the network controller`() {
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

                val testAnimeDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        throw NullPointerException("junit test")
                    }
                }

                val crawler = AnidbCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloader = testAnimeDownloader,
                    deadEntriesAccess = TestDeadEntriesAccessor,
                    idRangeSelector = testIdRangeSelector,
                    networkController = TestNetworkController,
                )

                // when
                val result = exceptionExpected<NullPointerException> {
                    crawler.start()
                }

                // then
                assertThat(result).hasMessage("junit test")
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
                    downloader = testAnimeDownloader,
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
        fun `creates a file which indicates that the respective anime is pending addition and won't require another download`() {
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
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String = ANIDB_PENDING_FILE_INDICATOR
                }

                val crawler = AnidbCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloader = testAnimeDownloader,
                    deadEntriesAccess = TestDeadEntriesAccessor,
                    idRangeSelector = testIdRangeSelector,
                    networkController = TestNetworkController,
                )

                // when
                crawler.start()

                // then
                assertThat(tempDir).isDirectoryContaining { file ->
                    file.fileName() == "1000.pending"
                }
            }
        }

        @Test
        fun `initiates a restart of the network controller if a CrawlerDetectedException is thrown`() {
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

                var hasBeenInvoked = false
                val testNetworkController = object: NetworkController by TestNetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> = withContext(LIMITED_CPU) {
                        hasBeenInvoked = true
                        return@withContext async { true }
                    }
                }

                val successfulEntry = "successfully loaded content for"
                val testAnimeDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        return if (hasBeenInvoked) {
                            "$successfulEntry $id"
                        } else {
                            throw CrawlerDetectedException
                        }
                    }
                }

                val crawler = AnidbCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloader = testAnimeDownloader,
                    deadEntriesAccess = TestDeadEntriesAccessor,
                    idRangeSelector = testIdRangeSelector,
                    networkController = testNetworkController,
                )

                // when
                crawler.start()

                // then
                assertThat(hasBeenInvoked).isTrue()
                testIdRangeSelector.idDownloadList().forEach { id ->
                    val result = tempDir.resolve("$id.html").readFile()
                    assertThat(result).isEqualTo("$successfulEntry $id")
                }
            }
        }

        @Test
        fun `throws an exception if a restart of the network controller didn't help`() {
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

                val testAnimeDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        throw NoRouteToHostException("junit test")
                    }
                }

                val crawler = AnidbCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloader = testAnimeDownloader,
                    deadEntriesAccess = TestDeadEntriesAccessor,
                    idRangeSelector = testIdRangeSelector,
                    networkController = TestNetworkController,
                )

                // when
                val result = exceptionExpected<NoRouteToHostException> {
                    crawler.start()
                }

                // then
                assertThat(result).hasMessage("junit test")
            }
        }

        @Test
        fun `passes the animeId to the dead entries accessor if the downloader triggers a dead entry in case a retry was necessary first`() {
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

                var hasBeenInvoked = false
                val testNetworkController = object: NetworkController by TestNetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> = withContext(LIMITED_CPU) {
                        hasBeenInvoked = true
                        return@withContext async { true }
                    }
                }

                val testAnimeDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        return if (hasBeenInvoked) {
                            onDeadEntry(id)
                            EMPTY
                        } else {
                            throw CrawlerDetectedException
                        }
                    }
                }

                val invocations = mutableListOf<AnimeId>()
                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override suspend fun addDeadEntry(animeId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig) {
                        invocations.add(animeId)
                    }
                }

                val crawler = AnidbCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloader = testAnimeDownloader,
                    deadEntriesAccess = testDeadEntriesAccessor,
                    idRangeSelector = testIdRangeSelector,
                    networkController = testNetworkController,
                )

                // when
                crawler.start()

                // then
                assertThat(hasBeenInvoked).isTrue()
                assertThat(tempDir.listDirectoryEntries()).isEmpty()
                assertThat(invocations).containsExactlyInAnyOrder(
                    "1000",
                )
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