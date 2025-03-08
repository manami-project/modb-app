package io.github.manamiproject.modb.app.crawlers.animenewsnetwork

import io.github.manamiproject.AnimenewsnetworkConfig
import io.github.manamiproject.modb.app.*
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.convfiles.AlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.crawlers.HighestIdDetector
import io.github.manamiproject.modb.app.crawlers.LastPageMemorizer
import io.github.manamiproject.modb.app.crawlers.PaginationIdRangeSelector
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
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
import java.net.SocketException
import java.net.SocketTimeoutException
import kotlin.test.Test

internal class AnimenewsnetworkCrawlerTest {

    @Nested
    inner class StartTests {

        @Test
        fun `don't do anything if the list of IDs is empty`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimenewsnetworkConfig {
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
                    override suspend fun retrieveLastPage(): String = "v"
                    override suspend fun memorizeLastPage(page: String) {}
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<String> by TestPaginationIdRangeSelectorString {
                    override suspend fun idDownloadList(page: String): List<AnimeId> = emptyList()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val animePlanetCrawler = AnimenewsnetworkCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = TestDownloader,
                    networkController = TestNetworkController,
                )

                // when
                assertThatNoException().isThrownBy {
                    runBlocking { animePlanetCrawler.start() }
                }

                // then
                assertThat(tempDir).isEmptyDirectory()
            }
        }

        @Test
        fun `downloads anime scheduled for the current week`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimenewsnetworkConfig {
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
                    override suspend fun retrieveLastPage(): String = "v"
                    override suspend fun memorizeLastPage(page: String) {}
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

                val animePlanetCrawler = AnimenewsnetworkCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = TestNetworkController,
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
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimenewsnetworkConfig {
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
                    override suspend fun retrieveLastPage(): String = "v"
                    override suspend fun memorizeLastPage(page: String) {}
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

                val animePlanetCrawler = AnimenewsnetworkCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = TestNetworkController,
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
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimenewsnetworkConfig {
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
                    override suspend fun retrieveLastPage(): String = "v"
                    override suspend fun memorizeLastPage(page: String) {}
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

                val animePlanetCrawler = AnimenewsnetworkCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = TestNetworkController,
                )

                // when
                animePlanetCrawler.start()

                // then
                assertThat(downloadedEntries).containsExactlyInAnyOrder(
                    "paginated-entry-w",
                    "paginated-entry-x",
                    "paginated-entry-y",
                    "paginated-entry-z",
                )
                assertThat(tempDir.listRegularFiles("*.html").map { it.fileName() }).containsExactlyInAnyOrder(
                    "paginated-entry-w.html",
                    "paginated-entry-x.html",
                    "paginated-entry-y.html",
                    "paginated-entry-z.html",
                )
            }
        }

        @Test
        fun `excludes already downloaded anime when downloading paginated entries`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimenewsnetworkConfig {
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
                    override suspend fun retrieveLastPage(): String = "v"
                    override suspend fun memorizeLastPage(page: String) {}
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<String> by TestPaginationIdRangeSelectorString {
                    override suspend fun idDownloadList(page: String): List<AnimeId> = listOf(
                        "paginated-entry-$page",
                    )
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "paginated-entry-w",
                        "paginated-entry-y",
                    )
                }

                val downloadedEntries = mutableListOf<AnimeId>()
                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        downloadedEntries.add(id)
                        return  id
                    }
                }

                val animePlanetCrawler = AnimenewsnetworkCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = TestNetworkController,
                )

                // when
                animePlanetCrawler.start()

                // then
                assertThat(downloadedEntries).containsExactlyInAnyOrder(
                    "paginated-entry-x",
                    "paginated-entry-z",
                )
                assertThat(tempDir.listRegularFiles("*.html").map { it.fileName() }).containsExactlyInAnyOrder(
                    "paginated-entry-x.html",
                    "paginated-entry-z.html",
                )
            }
        }

        @Test
        fun `excludes entries not scheduled for re-download when downloading paginated entries`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimenewsnetworkConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "paginated-entry-w",
                        "paginated-entry-y",
                    )
                }

                val testLastPageMemorizer = object: LastPageMemorizer<String> by TestLastPageMemorizerString {
                    override suspend fun retrieveLastPage(): String = "v"
                    override suspend fun memorizeLastPage(page: String) {}
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

                val animePlanetCrawler = AnimenewsnetworkCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = TestNetworkController,
                )

                // when
                animePlanetCrawler.start()

                // then
                assertThat(downloadedEntries).containsExactlyInAnyOrder(
                    "paginated-entry-x",
                    "paginated-entry-z",
                )
                assertThat(tempDir.listRegularFiles("*.html").map { it.fileName() }).containsExactlyInAnyOrder(
                    "paginated-entry-x.html",
                    "paginated-entry-z.html",
                )
            }
        }

        @Test
        fun `memorizes last crawled page each time`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimenewsnetworkConfig {
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
                    override suspend fun retrieveLastPage(): String = "v"
                    override suspend fun memorizeLastPage(page: String) {
                        invocations.add(page)
                    }
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

                val animePlanetCrawler = AnimenewsnetworkCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = TestNetworkController,
                )

                // when
                animePlanetCrawler.start()

                // then
                assertThat(invocations).containsExactly("w", "x", "y", "z")
            }
        }

        @Test
        fun `resumes on last memorized page`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimenewsnetworkConfig {
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
                    override suspend fun retrieveLastPage(): String = "v"
                    override suspend fun memorizeLastPage(page: String) {
                        invocations.add(page)
                    }
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

                val animePlanetCrawler = AnimenewsnetworkCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = TestNetworkController,
                )

                // when
                animePlanetCrawler.start()

                // then
                assertThat(invocations).containsExactly("w", "x", "y", "z")
            }
        }

        @Test
        fun `removes DCS file if dead entry has been has been triggered and meta data provider config is AnimenewsnetworkConfig`() {
            tempDirectory {
                // given
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
                    override suspend fun retrieveLastPage(): String = "v"
                    override suspend fun memorizeLastPage(page: String) {}
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

                val animePlanetCrawler = AnimenewsnetworkCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = AnimenewsnetworkConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = testDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = TestNetworkController,
                )

                // when
                animePlanetCrawler.start()

                // then
                assertThat(invocations).containsExactly("re-download-1")
            }
        }

        @Test
        fun `doesn't remove DCS file if meta data provider config is not AnimenewsnetworkConfig`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimenewsnetworkConfig {
                    override fun isTestContext(): Boolean = true
                }

                // given
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
                    override suspend fun retrieveLastPage(): String = "v"
                    override suspend fun memorizeLastPage(page: String) {}
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

                val animePlanetCrawler = AnimenewsnetworkCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = testDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = TestNetworkController,
                )

                // when
                animePlanetCrawler.start()

                // then
                assertThat(invocations).isEmpty()
            }
        }

        @Test
        fun `won't create a file if the response of the downloader is a blank String`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimenewsnetworkConfig {
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
                    override suspend fun retrieveLastPage(): String = "v"
                    override suspend fun memorizeLastPage(page: String) {}
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

                val animePlanetCrawler = AnimenewsnetworkCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = testDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = TestNetworkController,
                )

                // when
                animePlanetCrawler.start()

                // then
                assertThat(tempDir).isEmptyDirectory()
            }
        }

        @Test
        fun `initiates a restart of the network controller if a SocketTimeoutException is thrown`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimenewsnetworkConfig {
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

                val testLastPageMemorizer = object: LastPageMemorizer<String> by TestLastPageMemorizerString {
                    override suspend fun retrieveLastPage(): String = "v"
                    override suspend fun memorizeLastPage(page: String) {}
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<String> by TestPaginationIdRangeSelectorString {
                    override suspend fun idDownloadList(page: String): List<AnimeId> = emptyList()
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
                            throw SocketTimeoutException()
                        }
                    }
                }

                val testNetworkController = object: NetworkController by TestNetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> = withContext(LIMITED_CPU) {
                        hasBeenInvoked = true
                        return@withContext async { true }
                    }
                }

                val crawler = AnimenewsnetworkCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    lastPageMemorizer = testLastPageMemorizer,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = testNetworkController,
                )

                // when
                crawler.start()

                // then
                assertThat(hasBeenInvoked).isTrue()
                assertThat(downloadedEntries).containsExactlyInAnyOrder("1")
                assertThat(tempDir.listRegularFiles("*.html").map { it.fileName() }).containsExactlyInAnyOrder(
                    "1.html",
                )
            }
        }

        @Test
        fun `initiates a restart of the network controller if a SocketException is thrown`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimenewsnetworkConfig {
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

                val testLastPageMemorizer = object: LastPageMemorizer<String> by TestLastPageMemorizerString {
                    override suspend fun retrieveLastPage(): String = "v"
                    override suspend fun memorizeLastPage(page: String) {}
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<String> by TestPaginationIdRangeSelectorString {
                    override suspend fun idDownloadList(page: String): List<AnimeId> = emptyList()
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
                            throw SocketException()
                        }
                    }
                }

                val testNetworkController = object: NetworkController by TestNetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> = withContext(LIMITED_CPU) {
                        hasBeenInvoked = true
                        return@withContext async { true }
                    }
                }

                val crawler = AnimenewsnetworkCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    lastPageMemorizer = testLastPageMemorizer,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = testNetworkController,
                )

                // when
                crawler.start()

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
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimenewsnetworkConfig {
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

                val testLastPageMemorizer = object: LastPageMemorizer<String> by TestLastPageMemorizerString {
                    override suspend fun retrieveLastPage(): String = "v"
                    override suspend fun memorizeLastPage(page: String) {}
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<String> by TestPaginationIdRangeSelectorString {
                    override suspend fun idDownloadList(page: String): List<AnimeId> = emptyList()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                var hasBeenInvoked = false
                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        throw SocketTimeoutException("junit test")
                    }
                }

                val testNetworkController = object: NetworkController by TestNetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> = withContext(LIMITED_CPU) {
                        hasBeenInvoked = true
                        return@withContext async { true }
                    }
                }

                val crawler = AnimenewsnetworkCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    lastPageMemorizer = testLastPageMemorizer,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
                    networkController = testNetworkController,
                )

                // when
                val result = exceptionExpected<SocketTimeoutException> {
                    crawler.start()
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
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimenewsnetworkConfig {
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

                val testLastPageMemorizer = object: LastPageMemorizer<String> by TestLastPageMemorizerString {
                    override suspend fun retrieveLastPage(): String = "v"
                    override suspend fun memorizeLastPage(page: String) {}
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<String> by TestPaginationIdRangeSelectorString {
                    override suspend fun idDownloadList(page: String): List<AnimeId> = emptyList()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        throw NullPointerException("junit test")
                    }
                }

                val crawler = AnimenewsnetworkCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    lastPageMemorizer = testLastPageMemorizer,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = testDownloader,
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
        fun `correctly generates the pages`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimenewsnetworkConfig {
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

                val animePlanetCrawler = AnimenewsnetworkCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = TestDownloader,
                    networkController = TestNetworkController,
                )

                // when
                animePlanetCrawler.start()

                // then
                assertThat(invocations).containsExactly(
                    "9",
                    "a",
                    "b",
                    "c",
                    "d",
                    "e",
                    "f",
                    "g",
                    "h",
                    "i",
                    "j",
                    "k",
                    "l",
                    "m",
                    "n",
                    "o",
                    "p",
                    "q",
                    "r",
                    "s",
                    "t",
                    "u",
                    "v",
                    "w",
                    "x",
                    "y",
                    "z",
                )
            }
        }

        @Test
        fun `throws exception if the last seen page is invalid`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnimenewsnetworkConfig {
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
                    override suspend fun retrieveLastPage(): String = "invalid-page"
                }

                val testPaginationIdRangeSelector = object: PaginationIdRangeSelector<String> by TestPaginationIdRangeSelectorString {
                    override suspend fun idDownloadList(page: String): List<AnimeId> = emptyList()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val animePlanetCrawler = AnimenewsnetworkCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    paginationIdRangeSelector = testPaginationIdRangeSelector,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    downloader = TestDownloader,
                    networkController = TestNetworkController,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    animePlanetCrawler.start()
                }

                // then
                assertThat(result).hasMessage("Invalid last page: [invalid-page].")
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = AnimenewsnetworkCrawler.instance

                // when
                val result = AnimenewsnetworkCrawler.instance

                // then
                assertThat(result).isExactlyInstanceOf(AnimenewsnetworkCrawler::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}