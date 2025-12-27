package io.github.manamiproject.modb.app.crawlers.livechart

import io.github.manamiproject.modb.app.*
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.convfiles.AlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.crawlers.HighestIdDetector
import io.github.manamiproject.modb.app.crawlers.LastPageMemorizer
import io.github.manamiproject.modb.app.crawlers.PaginationIdRangeSelector
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateScheduler
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.fileName
import io.github.manamiproject.modb.core.extensions.listRegularFiles
import io.github.manamiproject.modb.livechart.LivechartConfig
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class LivechartCrawlerTest {

    @Nested
    inner class StartTests {

        @Test
        fun `don't do anything if the list of IDs is empty`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by LivechartConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testLastPageMemorizer = object: LastPageMemorizer<String> by TestLastPageMemorizerString {
                    override suspend fun retrieveLastPage(): String = "winter-1907"
                    override suspend fun memorizeLastPage(page: String) {}
                }

                val testNewestYearDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1910
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<String> by TestPaginationIdRangeSelectorString {
                    override suspend fun idDownloadList(page: String): List<AnimeId> = emptyList()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val animePlanetCrawler = LivechartCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    newestYearDetector = testNewestYearDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = TestDownloader,
                )

                // when
                assertThatNoException().isThrownBy {
                    runTest { animePlanetCrawler.start() }
                }

                // then
                assertThat(tempDir).isEmptyDirectory()
            }
        }

        @Test
        fun `downloads anime scheduled for the current week`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by LivechartConfig {
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

                val testLastPageMemorizer = object: LastPageMemorizer<String> by TestLastPageMemorizerString {
                    override suspend fun retrieveLastPage(): String = "winter-1907"
                    override suspend fun memorizeLastPage(page: String) {}
                }

                val testNewestYearDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1910
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<String> by TestPaginationIdRangeSelectorString {
                    override suspend fun idDownloadList(page: String): List<AnimeId> = emptyList()
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

                val animePlanetCrawler = LivechartCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    newestYearDetector = testNewestYearDetector,
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
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by LivechartConfig {
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

                val testLastPageMemorizer = object: LastPageMemorizer<String> by TestLastPageMemorizerString {
                    override suspend fun retrieveLastPage(): String = "winter-1907"
                    override suspend fun memorizeLastPage(page: String) {}
                }

                val testNewestYearDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1910
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<String> by TestPaginationIdRangeSelectorString {
                    override suspend fun idDownloadList(page: String): List<AnimeId> = emptyList()
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

                val animePlanetCrawler = LivechartCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    newestYearDetector = testNewestYearDetector,
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
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by LivechartConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testLastPageMemorizer = object: LastPageMemorizer<String> by TestLastPageMemorizerString {
                    override suspend fun retrieveLastPage(): String = "winter-1907"
                    override suspend fun memorizeLastPage(page: String) {}
                }

                val testNewestYearDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1910
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<String> by TestPaginationIdRangeSelectorString {
                    override suspend fun idDownloadList(page: String): List<AnimeId> = listOf(
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

                val animePlanetCrawler = LivechartCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    newestYearDetector = testNewestYearDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                )

                // when
                animePlanetCrawler.start()

                // then
                assertThat(downloadedEntries).containsExactlyInAnyOrder(
                    "paginated-entry-winter-1908",
                    "paginated-entry-winter-1909",
                    "paginated-entry-winter-1910",
                )
                assertThat(tempDir.listRegularFiles("*.html").map { it.fileName() }).containsExactlyInAnyOrder(
                    "paginated-entry-winter-1908.html",
                    "paginated-entry-winter-1909.html",
                    "paginated-entry-winter-1910.html",
                )
            }
        }

        @Test
        fun `excludes already downloaded anime when downloading paginated entries`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by LivechartConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testLastPageMemorizer = object: LastPageMemorizer<String> by TestLastPageMemorizerString {
                    override suspend fun retrieveLastPage(): String = "winter-1907"
                    override suspend fun memorizeLastPage(page: String) {}
                }

                val testNewestYearDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1911
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<String> by TestPaginationIdRangeSelectorString {
                    override suspend fun idDownloadList(page: String): List<AnimeId> = listOf(
                        "paginated-entry-$page",
                    )
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "paginated-entry-winter-1908",
                        "paginated-entry-winter-1910"
                    )
                }

                val downloadedEntries = mutableListOf<AnimeId>()
                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        downloadedEntries.add(id)
                        return  id
                    }
                }

                val animePlanetCrawler = LivechartCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    newestYearDetector = testNewestYearDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                )

                // when
                animePlanetCrawler.start()

                // then
                assertThat(downloadedEntries).containsExactlyInAnyOrder(
                    "paginated-entry-winter-1909",
                    "paginated-entry-winter-1911",
                )
                assertThat(tempDir.listRegularFiles("*.html").map { it.fileName() }).containsExactlyInAnyOrder(
                    "paginated-entry-winter-1909.html",
                    "paginated-entry-winter-1911.html",
                )
            }
        }

        @Test
        fun `excludes entries not scheduled for re-download when downloading paginated entries`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by LivechartConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "paginated-entry-winter-1907",
                        "paginated-entry-winter-1909",
                    )
                }

                val testLastPageMemorizer = object: LastPageMemorizer<String> by TestLastPageMemorizerString {
                    override suspend fun retrieveLastPage(): String = "winter-1907"
                    override suspend fun memorizeLastPage(page: String) {}
                }

                val testNewestYearDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1910
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<String> by TestPaginationIdRangeSelectorString {
                    override suspend fun idDownloadList(page: String): List<AnimeId> = listOf(
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

                val animePlanetCrawler = LivechartCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    newestYearDetector = testNewestYearDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                )

                // when
                animePlanetCrawler.start()

                // then
                assertThat(downloadedEntries).containsExactlyInAnyOrder(
                    "paginated-entry-winter-1908",
                    "paginated-entry-winter-1910",
                )
                assertThat(tempDir.listRegularFiles("*.html").map { it.fileName() }).containsExactlyInAnyOrder(
                    "paginated-entry-winter-1908.html",
                    "paginated-entry-winter-1910.html",
                )
            }
        }

        @Test
        fun `memorizes last crawled page each time`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by LivechartConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val invocations = mutableListOf<String>()
                val testLastPageMemorizer = object: LastPageMemorizer<String> by TestLastPageMemorizerString {
                    override suspend fun retrieveLastPage(): String = "winter-1907"
                    override suspend fun memorizeLastPage(page: String) {
                        invocations.add(page)
                    }
                }

                val testNewestYearDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1910
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<String> by TestPaginationIdRangeSelectorString {
                    override suspend fun idDownloadList(page: String): List<AnimeId> = emptyList()
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

                val animePlanetCrawler = LivechartCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    newestYearDetector = testNewestYearDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                )

                // when
                animePlanetCrawler.start()

                // then
                assertThat(invocations).containsExactly("winter-1908", "winter-1909", "winter-1910")
            }
        }

        @Test
        fun `resumes on last memorized page`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by LivechartConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val invocations = mutableListOf<String>()
                val testLastPageMemorizer = object: LastPageMemorizer<String> by TestLastPageMemorizerString {
                    override suspend fun retrieveLastPage(): String = "winter-1908"
                    override suspend fun memorizeLastPage(page: String) {
                        invocations.add(page)
                    }
                }

                val testNewestYearDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1910
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<String> by TestPaginationIdRangeSelectorString {
                    override suspend fun idDownloadList(page: String): List<AnimeId> = emptyList()
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

                val animePlanetCrawler = LivechartCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    newestYearDetector = testNewestYearDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                )

                // when
                animePlanetCrawler.start()

                // then
                assertThat(invocations).containsExactly("winter-1909", "winter-1910")
            }
        }

        @Test
        fun `removes DCS file if dead entry has been has been triggered`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by LivechartConfig {
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
                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override suspend fun addDeadEntry(animeId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig) {
                        invocations.add(animeId)
                    }
                }

                val testLastPageMemorizer = object: LastPageMemorizer<String> by TestLastPageMemorizerString {
                    override suspend fun retrieveLastPage(): String = "winter-1907"
                    override suspend fun memorizeLastPage(page: String) {}
                }

                val testNewestYearDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1910
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<String> by TestPaginationIdRangeSelectorString {
                    override suspend fun idDownloadList(page: String): List<AnimeId> = emptyList()
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

                val animePlanetCrawler = LivechartCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = testDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    newestYearDetector = testNewestYearDetector,
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
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by LivechartConfig {
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
                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override suspend fun addDeadEntry(animeId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig) {
                        invocations.add(animeId)
                    }
                }

                val testLastPageMemorizer = object: LastPageMemorizer<String> by TestLastPageMemorizerString {
                    override suspend fun retrieveLastPage(): String = "winter-1907"
                    override suspend fun memorizeLastPage(page: String) {}
                }

                val testNewestYearDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1910
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<String> by TestPaginationIdRangeSelectorString {
                    override suspend fun idDownloadList(page: String): List<AnimeId> = emptyList()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String = EMPTY
                }

                val animePlanetCrawler = LivechartCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = testDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    newestYearDetector = testNewestYearDetector,
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

        @Test
        fun `correctly generates the pages`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by LivechartConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testLastPageMemorizer = object: LastPageMemorizer<String> by TestLastPageMemorizerString {
                    override suspend fun retrieveLastPage(): String = EMPTY
                    override suspend fun memorizeLastPage(page: String) {}
                }

                val testNewestYearDetector = object: HighestIdDetector by TestHighestIdDetector {
                    override suspend fun detectHighestId(): Int = 1908
                }

                val invocations = mutableListOf<String>()
                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<String> by TestPaginationIdRangeSelectorString {
                    override suspend fun idDownloadList(page: String): List<AnimeId> {
                        invocations.add(page)
                        return emptyList()
                    }
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val animePlanetCrawler = LivechartCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    newestYearDetector = testNewestYearDetector,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = TestDownloader,
                )

                // when
                animePlanetCrawler.start()

                // then
                assertThat(invocations).containsExactly(
                    "fall-1907",
                    "fall-1908",
                    "spring-1907",
                    "spring-1908",
                    "summer-1907",
                    "summer-1908",
                    "tba",
                    "winter-1907",
                    "winter-1908",
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
                val previous = LivechartCrawler.instance

                // when
                val result = LivechartCrawler.instance

                // then
                assertThat(result).isExactlyInstanceOf(LivechartCrawler::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}