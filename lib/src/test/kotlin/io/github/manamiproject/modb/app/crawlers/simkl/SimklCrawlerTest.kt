package io.github.manamiproject.modb.app.crawlers.simkl

import io.github.manamiproject.modb.app.*
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.convfiles.AlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.crawlers.LastPageMemorizer
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateScheduler
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.listRegularFiles
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.httpclient.RequestBody
import io.github.manamiproject.modb.simkl.SimklConfig
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URL
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.test.Test

internal class SimklCrawlerTest {

    @Nested
    inner class StartTests {

        @ParameterizedTest
        @ValueSource(strings = ["<div style=\"height: 500px\"></div>", ""])
        fun `don't do anything if the list of IDs is empty`(value: String) {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by SimklConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                    override fun clock(): Clock = Clock.fixed(Instant.parse("2025-11-17T17:55:43.035Z"), ZoneId.systemDefault())
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testPaginationConfig = object: MetaDataProviderConfig by SimklPaginationIdRangeSelectorConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 2027
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun post(url: URL, requestBody: RequestBody, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = value,
                    )
                }

                val simklCrawler = SimklCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    paginationConfig = testPaginationConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    httpClient = testHttpClient,
                    downloader = TestDownloader,
                )

                // when
                assertThatNoException().isThrownBy {
                    runBlocking { simklCrawler.start() }
                }

                // then
                assertThat(tempDir).isEmptyDirectory()
            }
        }

        @Test
        fun `downloads anime scheduled for the current week`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by SimklConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                    override fun clock(): Clock = Clock.fixed(Instant.parse("1905-11-17T17:55:43.035Z"), ZoneId.systemDefault())
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "1535",
                        "65273",
                        "5782",
                        "98268",
                    )
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testPaginationConfig = object: MetaDataProviderConfig by SimklPaginationIdRangeSelectorConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun post(url: URL, requestBody: RequestBody, headers: Map<String, Collection<String>>): HttpResponse {
                        return HttpResponse(
                            code = 200,
                            body = "<div style=\"height: 500px\"></div>",
                        )
                    }
                }

                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String = "entry $id"
                }

                val simklCrawler = SimklCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    paginationConfig = testPaginationConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    httpClient = testHttpClient,
                    downloader = testDownloader,
                )

                // when
                assertThatNoException().isThrownBy {
                    runBlocking { simklCrawler.start() }
                }

                // then
                assertThat(tempDir.resolve("1535.${testMetaDataProviderConfig.fileSuffix()}").regularFileExists()).isTrue()
                assertThat(tempDir.resolve("65273.${testMetaDataProviderConfig.fileSuffix()}").regularFileExists()).isTrue()
                assertThat(tempDir.resolve("5782.${testMetaDataProviderConfig.fileSuffix()}").regularFileExists()).isTrue()
                assertThat(tempDir.resolve("98268.${testMetaDataProviderConfig.fileSuffix()}").regularFileExists()).isTrue()
            }
        }

        @Test
        fun `excludes already downloaded anime when downloading anime scheduled for current week`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by SimklConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                    override fun clock(): Clock = Clock.fixed(Instant.parse("1905-11-17T17:55:43.035Z"), ZoneId.systemDefault())
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "1535",
                        "65273",
                        "445",
                        "5782",
                        "98268",
                        "990013",
                    )
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testPaginationConfig = object: MetaDataProviderConfig by SimklPaginationIdRangeSelectorConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "445",
                        "990013",
                    )
                }

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun post(url: URL, requestBody: RequestBody, headers: Map<String, Collection<String>>): HttpResponse {
                        return HttpResponse(
                            code = 200,
                            body = "<div style=\"height: 500px\"></div>",
                        )
                    }
                }

                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String = "entry $id"
                }

                val simklCrawler = SimklCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    paginationConfig = testPaginationConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    httpClient = testHttpClient,
                    downloader = testDownloader,
                )

                // when
                assertThatNoException().isThrownBy {
                    runBlocking { simklCrawler.start() }
                }

                // then
                assertThat(tempDir.listRegularFiles("*.${testMetaDataProviderConfig.fileSuffix()}")).containsExactlyInAnyOrder(
                    tempDir.resolve("1535.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("65273.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("5782.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("98268.${testMetaDataProviderConfig.fileSuffix()}"),
                )
            }
        }

        @Test
        fun `pagination starts with first year of anime`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by SimklConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                    override fun clock(): Clock = Clock.fixed(Instant.parse("1910-11-17T17:55:43.035Z"), ZoneId.systemDefault())
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testPaginationConfig = object: MetaDataProviderConfig by SimklPaginationIdRangeSelectorConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val requests = mutableListOf<String>()
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun post(url: URL, requestBody: RequestBody, headers: Map<String, Collection<String>>): HttpResponse {
                        requests.add(requestBody.body)
                        return HttpResponse(
                            code = 200,
                            body = "<div style=\"height: 500px\"></div>",
                        )
                    }
                }

                val simklCrawler = SimklCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    paginationConfig = testPaginationConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    httpClient = testHttpClient,
                    downloader = TestDownloader,
                )

                // when
                assertThatNoException().isThrownBy {
                    runBlocking { simklCrawler.start() }
                }

                // then
                assertThat(requests).containsExactly(
                    "action=genres&cat=shows&filt_tv=%2Fanime%2Fall%2F1907%2Fa-z&afilt_tv=&offset=0&async=true&double=0",
                    "action=genres&cat=shows&filt_tv=%2Fanime%2Fall%2F1908%2Fa-z&afilt_tv=&offset=0&async=true&double=0",
                    "action=genres&cat=shows&filt_tv=%2Fanime%2Fall%2F1909%2Fa-z&afilt_tv=&offset=0&async=true&double=0",
                    "action=genres&cat=shows&filt_tv=%2Fanime%2Fall%2F1910%2Fa-z&afilt_tv=&offset=0&async=true&double=0",
                    "action=genres&cat=shows&filt_tv=%2Fanime%2Fall%2F1911%2Fa-z&afilt_tv=&offset=0&async=true&double=0",
                    "action=genres&cat=shows&filt_tv=%2Fanime%2Fall%2F1912%2Fa-z&afilt_tv=&offset=0&async=true&double=0",
                )
                assertThat(tempDir).isEmptyDirectory()
            }
        }

        @Test
        fun `downloads paginated entries`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by SimklConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                    override fun clock(): Clock = Clock.fixed(Instant.parse("1905-11-17T17:55:43.035Z"), ZoneId.systemDefault())
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testPaginationConfig = object: MetaDataProviderConfig by SimklPaginationIdRangeSelectorConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                var invocations = 0
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun post(url: URL, requestBody: RequestBody, headers: Map<String, Collection<String>>): HttpResponse {
                        invocations++
                        return when (invocations) {
                            1 -> HttpResponse(
                                code = 200,
                                body = loadTestResource<ByteArray>("crawler/simkl/SimklCrawlerTest/page-with-entries.html"),
                            )
                            else -> HttpResponse(
                                code = 200,
                                body = "<div style=\"height: 500px\"></div>",
                            )
                        }
                    }
                }

                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String = "entry $id"
                }

                val simklCrawler = SimklCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    paginationConfig = testPaginationConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    httpClient = testHttpClient,
                    downloader = testDownloader,
                )

                // when
                assertThatNoException().isThrownBy {
                    runBlocking { simklCrawler.start() }
                }

                // then
                assertThat(tempDir.listRegularFiles("*.${testMetaDataProviderConfig.fileSuffix()}")).containsExactlyInAnyOrder(
                    tempDir.resolve("1572403.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1572395.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1572511.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1572357.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1573161.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1572361.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1573167.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1573171.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1572667.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1572671.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1573165.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1573169.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1525689.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1572461.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1911626.${testMetaDataProviderConfig.fileSuffix()}"),
                )
            }
        }

        @Test
        fun `excludes already downloaded anime when downloading paginated entries`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by SimklConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                    override fun clock(): Clock = Clock.fixed(Instant.parse("1905-11-17T17:55:43.035Z"), ZoneId.systemDefault())
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testPaginationConfig = object: MetaDataProviderConfig by SimklPaginationIdRangeSelectorConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "1572403",
                        "1573167",
                        "1525689",
                        "1572461",
                    )
                }

                var invocations = 0
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun post(url: URL, requestBody: RequestBody, headers: Map<String, Collection<String>>): HttpResponse {
                        invocations++
                        return when (invocations) {
                            1 -> HttpResponse(
                                code = 200,
                                body = loadTestResource<ByteArray>("crawler/simkl/SimklCrawlerTest/page-with-entries.html"),
                            )
                            else -> HttpResponse(
                                code = 200,
                                body = "<div style=\"height: 500px\"></div>",
                            )
                        }
                    }
                }

                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String = "entry $id"
                }

                val simklCrawler = SimklCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    paginationConfig = testPaginationConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    httpClient = testHttpClient,
                    downloader = testDownloader,
                )

                // when
                assertThatNoException().isThrownBy {
                    runBlocking { simklCrawler.start() }
                }

                // then
                assertThat(tempDir.listRegularFiles("*.${testMetaDataProviderConfig.fileSuffix()}")).containsExactlyInAnyOrder(
                    tempDir.resolve("1572395.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1572511.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1572357.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1573161.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1572361.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1573171.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1572667.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1572671.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1573165.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1573169.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1911626.${testMetaDataProviderConfig.fileSuffix()}"),
                )
            }
        }

        @Test
        fun `excludes entries not scheduled for re-download when downloading paginated entries`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by SimklConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                    override fun clock(): Clock = Clock.fixed(Instant.parse("1905-11-17T17:55:43.035Z"), ZoneId.systemDefault())
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "1572403",
                        "1573167",
                        "1525689",
                        "1572461",
                    )
                }

                val testPaginationConfig = object: MetaDataProviderConfig by SimklPaginationIdRangeSelectorConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                var invocations = 0
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun post(url: URL, requestBody: RequestBody, headers: Map<String, Collection<String>>): HttpResponse {
                        invocations++
                        return when (invocations) {
                            1 -> HttpResponse(
                                code = 200,
                                body = loadTestResource<ByteArray>("crawler/simkl/SimklCrawlerTest/page-with-entries.html"),
                            )
                            else -> HttpResponse(
                                code = 200,
                                body = "<div style=\"height: 500px\"></div>",
                            )
                        }
                    }
                }

                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String = "entry $id"
                }

                val simklCrawler = SimklCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    paginationConfig = testPaginationConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    httpClient = testHttpClient,
                    downloader = testDownloader,
                )

                // when
                assertThatNoException().isThrownBy {
                    runBlocking { simklCrawler.start() }
                }

                // then
                assertThat(tempDir.listRegularFiles("*.${testMetaDataProviderConfig.fileSuffix()}")).containsExactlyInAnyOrder(
                    tempDir.resolve("1572395.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1572511.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1572357.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1573161.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1572361.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1573171.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1572667.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1572671.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1573165.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1573169.${testMetaDataProviderConfig.fileSuffix()}"),
                    tempDir.resolve("1911626.${testMetaDataProviderConfig.fileSuffix()}"),
                )
            }
        }

        @Test
        fun `memorizes last crawled page each time`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by SimklConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                    override fun clock(): Clock = Clock.fixed(Instant.parse("1910-11-17T17:55:43.035Z"), ZoneId.systemDefault())
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testPaginationConfig = object: MetaDataProviderConfig by SimklPaginationIdRangeSelectorConfig {
                    override fun isTestContext(): Boolean = true
                }

                val memorizedPages = mutableListOf<Int>()
                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {
                        memorizedPages.add(page)
                    }
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun post(url: URL, requestBody: RequestBody, headers: Map<String, Collection<String>>): HttpResponse {
                        return HttpResponse(
                            code = 200,
                            body = "<div style=\"height: 500px\"></div>",
                        )
                    }
                }

                val simklCrawler = SimklCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    paginationConfig = testPaginationConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    httpClient = testHttpClient,
                    downloader = TestDownloader,
                )

                // when
                assertThatNoException().isThrownBy {
                    runBlocking { simklCrawler.start() }
                }

                // then
                assertThat(memorizedPages).containsExactly(
                    1907,
                    1908,
                    1909,
                    1910,
                    1911,
                    1912,
                )
                assertThat(tempDir).isEmptyDirectory()
            }
        }

        @Test
        fun `resumes on last memorized page`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by SimklConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                    override fun clock(): Clock = Clock.fixed(Instant.parse("1910-11-17T17:55:43.035Z"), ZoneId.systemDefault())
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testPaginationConfig = object: MetaDataProviderConfig by SimklPaginationIdRangeSelectorConfig {
                    override fun isTestContext(): Boolean = true
                }

                val memorizedPages = mutableListOf<Int>()
                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1909
                    override suspend fun memorizeLastPage(page: Int) {
                        memorizedPages.add(page)
                    }
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun post(url: URL, requestBody: RequestBody, headers: Map<String, Collection<String>>): HttpResponse {
                        return HttpResponse(
                            code = 200,
                            body = "<div style=\"height: 500px\"></div>",
                        )
                    }
                }

                val simklCrawler = SimklCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    paginationConfig = testPaginationConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    httpClient = testHttpClient,
                    downloader = TestDownloader,
                )

                // when
                assertThatNoException().isThrownBy {
                    runBlocking { simklCrawler.start() }
                }

                // then
                assertThat(memorizedPages).containsExactly(
                    1909,
                    1910,
                    1911,
                    1912,
                )
                assertThat(tempDir).isEmptyDirectory()
            }
        }

        @Test
        fun `removes DCS file if dead entry has been has been triggered`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by SimklConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                    override fun clock(): Clock = Clock.fixed(Instant.parse("1905-11-17T17:55:43.035Z"), ZoneId.systemDefault())
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "9451",
                    )
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testPaginationConfig = object: MetaDataProviderConfig by SimklPaginationIdRangeSelectorConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun post(url: URL, requestBody: RequestBody, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = "<div style=\"height: 500px\"></div>",
                    )
                }
                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        onDeadEntry.invoke(id)
                        return EMPTY
                    }
                }

                val invocations = mutableListOf<String>()
                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override suspend fun addDeadEntry(animeId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig) {
                        invocations.add(animeId)
                    }
                }

                val simklCrawler = SimklCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    paginationConfig = testPaginationConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = testDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    httpClient = testHttpClient,
                    downloader = testDownloader,
                )

                // when
                assertThatNoException().isThrownBy {
                    runBlocking { simklCrawler.start() }
                }

                // then
                assertThat(invocations).containsExactly(
                    "9451",
                )
                assertThat(tempDir).isEmptyDirectory()
            }
        }

        @Test
        fun `won't create a file if the response of the downloader is a blank String`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by SimklConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                    override fun clock(): Clock = Clock.fixed(Instant.parse("1905-11-17T17:55:43.035Z"), ZoneId.systemDefault())
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "9451",
                    )
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testPaginationConfig = object: MetaDataProviderConfig by SimklPaginationIdRangeSelectorConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testLastPageMemorizer = object: LastPageMemorizer<Int> by TestLastPageMemorizerInt {
                    override suspend fun retrieveLastPage(): Int = 1
                    override suspend fun memorizeLastPage(page: Int) {}
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun post(url: URL, requestBody: RequestBody, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = "<div style=\"height: 500px\"></div>",
                    )
                }
                val testDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String = EMPTY
                }

                val simklCrawler = SimklCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    paginationConfig = testPaginationConfig,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    deadEntriesAccessor = TestDeadEntriesAccessor,
                    lastPageMemorizer = testLastPageMemorizer,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    httpClient = testHttpClient,
                    downloader = testDownloader,
                )

                // when
                assertThatNoException().isThrownBy {
                    runBlocking { simklCrawler.start() }
                }

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
                val previous = SimklCrawler.instance

                // when
                val result = SimklCrawler.instance

                // then
                assertThat(result).isExactlyInstanceOf(SimklCrawler::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}