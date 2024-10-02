package io.github.manamiproject.modb.app.crawlers.anisearch

import io.github.manamiproject.modb.anisearch.AnisearchConfig
import io.github.manamiproject.modb.app.*
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.convfiles.AlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.crawlers.HighestIdDetector
import io.github.manamiproject.modb.app.crawlers.LastPageMemorizer
import io.github.manamiproject.modb.app.crawlers.PaginationIdRangeSelector
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateScheduler
import io.github.manamiproject.modb.app.network.NetworkController
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.fileName
import io.github.manamiproject.modb.core.extensions.listRegularFiles
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Nested
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.UnknownHostException
import kotlin.test.Test

internal class AnisearchCrawlerTest {

    @Nested
    inner class StartTests {

        @Test
        fun `don't do anything if the list of IDs is empty`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testLastPageDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<Int> by TestPaginationIdRangeSelectorInt {
                    override suspend fun idDownloadList(page: Int): List<AnimeId> = emptyList()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val anisearchCrawler = AnisearchCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = TestDownloader,
                    networkController = TestNetworkController,
                )

                // when
                assertThatNoException().isThrownBy {
                    runBlocking { anisearchCrawler.start() }
                }
                assertThat(tempDir).isEmptyDirectory()
            }
        }

        @Test
        fun `downloads anime scheduled for the current week`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "1",
                        "2",
                        "3",
                        "4",
                    )
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testLastPageDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<Int> by TestPaginationIdRangeSelectorInt {
                    override suspend fun idDownloadList(page: Int): List<AnimeId> = emptyList()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val downloadedEntries = mutableListOf<AnimeId>()
                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        downloadedEntries.add(id)
                        return  id
                    }
                }

                val anisearchCrawler = AnisearchCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = TestNetworkController,
                )

                // when
                anisearchCrawler.start()

                // then
                val idSequence = listOf(
                    "1",
                    "2",
                    "3",
                    "4",
                )
                assertThat(downloadedEntries).containsExactlyInAnyOrder(*idSequence.toTypedArray())
                assertThat(downloadedEntries).doesNotContainSequence(idSequence)
                assertThat(tempDir.listRegularFiles("*.html").map { it.fileName() }).containsExactlyInAnyOrder(
                    "1.html",
                    "2.html",
                    "3.html",
                    "4.html",
                )
            }
        }

        @Test
        fun `excludes already downloaded anime when downloading anime scheduled for current week`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "1",
                        "2",
                        "3",
                        "4",
                    )
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testLastPageDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<Int> by TestPaginationIdRangeSelectorInt {
                    override suspend fun idDownloadList(page: Int): List<AnimeId> = emptyList()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "1",
                        "3",
                    )
                }

                val downloadedEntries = mutableListOf<AnimeId>()
                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        downloadedEntries.add(id)
                        return  id
                    }
                }

                val anisearchCrawler = AnisearchCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = TestNetworkController,
                )

                // when
                anisearchCrawler.start()

                // then
                val idSequence = listOf(
                    "2",
                    "4",
                )
                assertThat(downloadedEntries).containsExactlyInAnyOrder(*idSequence.toTypedArray())
                assertThat(downloadedEntries).doesNotContainSequence(idSequence)
                assertThat(tempDir.listRegularFiles("*.html").map { it.fileName() }).containsExactlyInAnyOrder(
                    "2.html",
                    "4.html",
                )
            }
        }

        @Test
        fun `downloads paginated entries`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testLastPageDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 4
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<Int> by TestPaginationIdRangeSelectorInt {
                    override suspend fun idDownloadList(page: Int): List<AnimeId> = listOf(
                        "$page",
                    )
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val downloadedEntries = mutableListOf<AnimeId>()
                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        downloadedEntries.add(id)
                        return  id
                    }
                }

                val anisearchCrawler = AnisearchCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = TestNetworkController,
                )

                // when
                anisearchCrawler.start()

                // then
                assertThat(downloadedEntries).containsExactlyInAnyOrder(
                    "1",
                    "2",
                    "3",
                    "4",
                )
                assertThat(tempDir.listRegularFiles("*.html").map { it.fileName() }).containsExactlyInAnyOrder(
                    "1.html",
                    "2.html",
                    "3.html",
                    "4.html",
                )
            }
        }

        @Test
        fun `excludes already downloaded anime when downloading paginated entries`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testLastPageDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 4
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<Int> by TestPaginationIdRangeSelectorInt {
                    override suspend fun idDownloadList(page: Int): List<AnimeId> = listOf(
                        "$page",
                    )
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "2",
                        "4",
                    )
                }

                val downloadedEntries = mutableListOf<AnimeId>()
                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        downloadedEntries.add(id)
                        return  id
                    }
                }

                val anisearchCrawler = AnisearchCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = TestNetworkController,
                )

                // when
                anisearchCrawler.start()

                // then
                assertThat(downloadedEntries).containsExactlyInAnyOrder(
                    "1",
                    "3",
                )
                assertThat(tempDir.listRegularFiles("*.html").map { it.fileName() }).containsExactlyInAnyOrder(
                    "1.html",
                    "3.html",
                )
            }
        }

        @Test
        fun `excludes entries not scheduled for re-download when downloading paginated entries`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "1",
                        "3",
                    )
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testLastPageDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 4
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<Int> by TestPaginationIdRangeSelectorInt {
                    override suspend fun idDownloadList(page: Int): List<AnimeId> = listOf(
                        "$page",
                    )
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val downloadedEntries = mutableListOf<AnimeId>()
                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        downloadedEntries.add(id)
                        return  id
                    }
                }

                val anisearchCrawler = AnisearchCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = TestNetworkController,
                )

                // when
                anisearchCrawler.start()

                // then
                assertThat(downloadedEntries).containsExactlyInAnyOrder(
                    "2",
                    "4",
                )
                assertThat(tempDir.listRegularFiles("*.html").map { it.fileName() }).containsExactlyInAnyOrder(
                    "2.html",
                    "4.html",
                )
            }
        }

        @Test
        fun `memorizes last crawled page each time`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val invocations = mutableListOf<Int>()
                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {
                        invocations.add(page)
                    }
                }

                val testLastPageDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 4
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<Int> by TestPaginationIdRangeSelectorInt {
                    override suspend fun idDownloadList(page: Int): List<AnimeId> = emptyList()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val downloadedEntries = mutableListOf<AnimeId>()
                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        downloadedEntries.add(id)
                        return  id
                    }
                }

                val anisearchCrawler = AnisearchCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = TestNetworkController,
                )

                // when
                anisearchCrawler.start()

                // then
                assertThat(invocations).containsExactly(1, 2, 3, 4)
            }
        }

        @Test
        fun `resumes on last memorized page`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val invocations = mutableListOf<Int>()
                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 3
                    override suspend fun memorizeLastPage(page: Int) {
                        invocations.add(page)
                    }
                }

                val testLastPageDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 4
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<Int> by TestPaginationIdRangeSelectorInt {
                    override suspend fun idDownloadList(page: Int): List<AnimeId> = emptyList()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val downloadedEntries = mutableListOf<AnimeId>()
                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        downloadedEntries.add(id)
                        return  id
                    }
                }

                val anisearchCrawler = AnisearchCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = TestNetworkController,
                )

                // when
                anisearchCrawler.start()

                // then
                assertThat(invocations).containsExactly(3, 4)
            }
        }

        @Test
        fun `removes DCS file if dead entry has been has been triggered and meta data provider config is AnisearchConfig`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "1",
                    )
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val invocations = mutableListOf<AnimeId>()
                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) {
                        invocations.add(animeId)
                    }
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testLastPageDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<Int> by TestPaginationIdRangeSelectorInt {
                    override suspend fun idDownloadList(page: Int): List<AnimeId> = emptyList()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        onDeadEntry(id)
                        return EMPTY
                    }
                }

                val anisearchCrawler = AnisearchCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = AnisearchConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = TestNetworkController,
                )

                // when
                anisearchCrawler.start()

                // then
                assertThat(invocations).containsExactly("1")
            }
        }

        @Test
        fun `don't removes DCS file if meta data provider config is not AnisearchConfig`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "1",
                    )
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val invocations = mutableListOf<AnimeId>()
                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) {
                        invocations.add(animeId)
                    }
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testLastPageDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<Int> by TestPaginationIdRangeSelectorInt {
                    override suspend fun idDownloadList(page: Int): List<AnimeId> = emptyList()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        onDeadEntry(id)
                        return EMPTY
                    }
                }

                val anisearchCrawler = AnisearchCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = TestNetworkController,
                )

                // when
                anisearchCrawler.start()

                // then
                assertThat(invocations).isEmpty()
            }
        }

        @Test
        fun `won't create a file if the response of the downloader is a blank String`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "1",
                    )
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val invocations = mutableListOf<AnimeId>()
                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) {
                        invocations.add(animeId)
                    }
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testLastPageDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<Int> by TestPaginationIdRangeSelectorInt {
                    override suspend fun idDownloadList(page: Int): List<AnimeId> = emptyList()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String = EMPTY
                }

                val anisearchCrawler = AnisearchCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = TestNetworkController,
                )

                // when
                anisearchCrawler.start()

                // then
                assertThat(tempDir).isEmptyDirectory()
            }
        }

        @Test
        fun `initiates a restart of the network controller if a ConnectException is thrown`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "1",
                    )
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testLastPageDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<Int> by TestPaginationIdRangeSelectorInt {
                    override suspend fun idDownloadList(page: Int): List<AnimeId> = emptyList()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                var hasBeenInvoked = false
                val downloadedEntries = mutableListOf<AnimeId>()
                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        return if (hasBeenInvoked) {
                            downloadedEntries.add(id)
                            id
                        } else {
                            throw ConnectException()
                        }
                    }
                }

                val testNetworkController = object: NetworkController by TestNetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> = withContext(LIMITED_CPU) {
                        hasBeenInvoked = true
                        return@withContext async { true }
                    }
                }

                val anisearchCrawler = AnisearchCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = testNetworkController,
                )

                // when
                anisearchCrawler.start()

                // then
                assertThat(hasBeenInvoked).isTrue()
                assertThat(downloadedEntries).containsExactlyInAnyOrder("1")
                assertThat(tempDir.listRegularFiles("*.html").map { it.fileName() }).containsExactlyInAnyOrder(
                    "1.html",
                )
            }
        }

        @Test
        fun `initiates a restart of the network controller if a UnknownHostException is thrown`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "1",
                    )
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testLastPageDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<Int> by TestPaginationIdRangeSelectorInt {
                    override suspend fun idDownloadList(page: Int): List<AnimeId> = emptyList()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                var hasBeenInvoked = false
                val downloadedEntries = mutableListOf<AnimeId>()
                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        return if (hasBeenInvoked) {
                            downloadedEntries.add(id)
                            id
                        } else {
                            throw UnknownHostException()
                        }
                    }
                }

                val testNetworkController = object: NetworkController by TestNetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> = withContext(LIMITED_CPU) {
                        hasBeenInvoked = true
                        return@withContext async { true }
                    }
                }

                val anisearchCrawler = AnisearchCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = testNetworkController,
                )

                // when
                anisearchCrawler.start()

                // then
                assertThat(hasBeenInvoked).isTrue()
                assertThat(downloadedEntries).containsExactlyInAnyOrder("1")
                assertThat(tempDir.listRegularFiles("*.html").map { it.fileName() }).containsExactlyInAnyOrder(
                    "1.html",
                )
            }
        }

        @Test
        fun `initiates a restart of the network controller if a NoRouteToHostException is thrown`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "1",
                    )
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testLastPageDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<Int> by TestPaginationIdRangeSelectorInt {
                    override suspend fun idDownloadList(page: Int): List<AnimeId> = emptyList()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                var hasBeenInvoked = false
                val downloadedEntries = mutableListOf<AnimeId>()
                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        return if (hasBeenInvoked) {
                            downloadedEntries.add(id)
                            id
                        } else {
                            throw NoRouteToHostException()
                        }
                    }
                }

                val testNetworkController = object: NetworkController by TestNetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> = withContext(LIMITED_CPU) {
                        hasBeenInvoked = true
                        return@withContext async { true }
                    }
                }

                val anisearchCrawler = AnisearchCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = testNetworkController,
                )

                // when
                anisearchCrawler.start()

                // then
                assertThat(hasBeenInvoked).isTrue()
                assertThat(downloadedEntries).containsExactlyInAnyOrder("1")
                assertThat(tempDir.listRegularFiles("*.html").map { it.fileName() }).containsExactlyInAnyOrder(
                    "1.html",
                )
            }
        }

        @Test
        fun `throws an exception if a restart of the network controller didn't help`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "1",
                    )
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testLastPageDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<Int> by TestPaginationIdRangeSelectorInt {
                    override suspend fun idDownloadList(page: Int): List<AnimeId> = emptyList()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                var hasBeenInvoked = false
                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        throw NoRouteToHostException("junit test")
                    }
                }

                val testNetworkController = object: NetworkController by TestNetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> = withContext(LIMITED_CPU) {
                        hasBeenInvoked = true
                        return@withContext async { true }
                    }
                }

                val anisearchCrawler = AnisearchCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = testNetworkController,
                )

                // when
                val result = exceptionExpected<NoRouteToHostException> {
                    anisearchCrawler.start()
                }

                // then
                assertThat(hasBeenInvoked).isTrue()
                assertThat(result).hasMessage("junit test")
            }
        }

        @Test
        fun `directly throws exception if it's not one of the cases that restart the network controller`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "1",
                    )
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testLastPageDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<Int> by TestPaginationIdRangeSelectorInt {
                    override suspend fun idDownloadList(page: Int): List<AnimeId> = emptyList()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        throw NullPointerException("junit test")
                    }
                }

                val anisearchCrawler = AnisearchCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = TestNetworkController,
                )

                // when
                val result = exceptionExpected<NullPointerException> {
                    anisearchCrawler.start()
                }

                // then
                assertThat(result).hasMessage("junit test")
            }
        }
    }
}