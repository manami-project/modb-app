package io.github.manamiproject.modb.app.crawlers.animeplanet

import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import io.github.manamiproject.modb.app.*
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.convfiles.AlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.crawlers.HighestIdDetector
import io.github.manamiproject.modb.app.crawlers.LastPageMemorizer
import io.github.manamiproject.modb.app.crawlers.PaginationIdRangeSelector
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateScheduler
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.fileName
import io.github.manamiproject.modb.core.extensions.listRegularFiles
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class AnimePlanetCrawlerTest {

    @Nested
    inner class StartTests {

        @Test
        fun `don't do anything if the list of IDs is empty`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimePlanetConfig {
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

                val animePlanetCrawler = AnimePlanetCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = TestDownloader,
                )

                // when
                assertThatNoException().isThrownBy {
                    runBlocking { animePlanetCrawler.start() }
                }
                assertThat(tempDir).isEmptyDirectory()
            }
        }

        @Test
        fun `downloads anime scheduled for the current week`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimePlanetConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "re-download-1",
                        "re-download-2",
                        "re-download-3",
                        "re-download-4",
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

                val animePlanetCrawler = AnimePlanetCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                )

                // when
                animePlanetCrawler.start()

                // then
                val idSequence = listOf(
                    "re-download-1",
                    "re-download-2",
                    "re-download-3",
                    "re-download-4",
                )
                assertThat(downloadedEntries).containsExactlyInAnyOrder(*idSequence.toTypedArray())
                assertThat(downloadedEntries).doesNotContainSequence(idSequence)
                assertThat(tempDir.listRegularFiles("*.html").map { it.fileName() }).containsExactlyInAnyOrder(
                    "re-download-1.html",
                    "re-download-2.html",
                    "re-download-3.html",
                    "re-download-4.html",
                )
            }
        }

        @Test
        fun `excludes already downloaded anime when downloading anime scheduled for current week`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimePlanetConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "re-download-1",
                        "re-download-2",
                        "re-download-3",
                        "re-download-4",
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
                        "re-download-1",
                        "re-download-3",
                    )
                }

                val downloadedEntries = mutableListOf<AnimeId>()
                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        downloadedEntries.add(id)
                        return  id
                    }
                }

                val animePlanetCrawler = AnimePlanetCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                )

                // when
                animePlanetCrawler.start()

                // then
                val idSequence = listOf(
                    "re-download-2",
                    "re-download-4",
                )
                assertThat(downloadedEntries).containsExactlyInAnyOrder(*idSequence.toTypedArray())
                assertThat(downloadedEntries).doesNotContainSequence(idSequence)
                assertThat(tempDir.listRegularFiles("*.html").map { it.fileName() }).containsExactlyInAnyOrder(
                    "re-download-2.html",
                    "re-download-4.html",
                )
            }
        }

        @Test
        fun `downloads paginated entries`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimePlanetConfig {
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
                        "paginated-entry-$page",
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

                val animePlanetCrawler = AnimePlanetCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                )

                // when
                animePlanetCrawler.start()

                // then
                assertThat(downloadedEntries).containsExactlyInAnyOrder(
                    "paginated-entry-1",
                    "paginated-entry-2",
                    "paginated-entry-3",
                    "paginated-entry-4",
                )
                assertThat(tempDir.listRegularFiles("*.html").map { it.fileName() }).containsExactlyInAnyOrder(
                    "paginated-entry-1.html",
                    "paginated-entry-2.html",
                    "paginated-entry-3.html",
                    "paginated-entry-4.html",
                )
            }
        }

        @Test
        fun `excludes already downloaded anime when downloading paginated entries`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimePlanetConfig {
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
                        "paginated-entry-$page",
                    )
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "paginated-entry-2",
                        "paginated-entry-4",
                    )
                }

                val downloadedEntries = mutableListOf<AnimeId>()
                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        downloadedEntries.add(id)
                        return  id
                    }
                }

                val animePlanetCrawler = AnimePlanetCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                )

                // when
                animePlanetCrawler.start()

                // then
                assertThat(downloadedEntries).containsExactlyInAnyOrder(
                    "paginated-entry-1",
                    "paginated-entry-3",
                )
                assertThat(tempDir.listRegularFiles("*.html").map { it.fileName() }).containsExactlyInAnyOrder(
                    "paginated-entry-1.html",
                    "paginated-entry-3.html",
                )
            }
        }

        @Test
        fun `excludes entries not scheduled for re-download when downloading paginated entries`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimePlanetConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "paginated-entry-1",
                        "paginated-entry-3",
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
                        "paginated-entry-$page",
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

                val animePlanetCrawler = AnimePlanetCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                )

                // when
                animePlanetCrawler.start()

                // then
                assertThat(downloadedEntries).containsExactlyInAnyOrder(
                    "paginated-entry-2",
                    "paginated-entry-4",
                )
                assertThat(tempDir.listRegularFiles("*.html").map { it.fileName() }).containsExactlyInAnyOrder(
                    "paginated-entry-2.html",
                    "paginated-entry-4.html",
                )
            }
        }

        @Test
        fun `memorizes last crawled page each time`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimePlanetConfig {
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

                val animePlanetCrawler = AnimePlanetCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                )

                // when
                animePlanetCrawler.start()

                // then
                assertThat(invocations).containsExactly(1, 2, 3, 4)
            }
        }

        @Test
        fun `removes DCS file if dead entry has been has been triggered`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimePlanetConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "re-download-1",
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

                val animePlanetCrawler = AnimePlanetCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                )

                // when
                animePlanetCrawler.start()

                // then
                assertThat(invocations).containsExactly("re-download-1")
            }
        }

        @Test
        fun `won't create a file if the response of the downloader is a blank String`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimePlanetConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "re-download-1",
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

                val animePlanetCrawler = AnimePlanetCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    lastPageDetector = testLastPageDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                )

                // when
                animePlanetCrawler.start()

                // then
                assertThat(tempDir).isEmptyDirectory()
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = AnimePlanetCrawler.instance

                // when
                val result = AnimePlanetCrawler.instance

                // then
                assertThat(result).isExactlyInstanceOf(AnimePlanetCrawler::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}