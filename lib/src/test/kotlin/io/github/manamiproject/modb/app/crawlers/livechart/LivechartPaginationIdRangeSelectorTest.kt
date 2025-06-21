package io.github.manamiproject.modb.app.crawlers.livechart

import io.github.manamiproject.modb.app.TestAlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.TestDownloadControlStateScheduler
import io.github.manamiproject.modb.app.TestHttpClient
import io.github.manamiproject.modb.app.convfiles.AlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateScheduler
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URL
import kotlin.test.Test

internal class LivechartPaginationIdRangeSelectorTest {

    @Nested
    inner class IdDownloadListTests {

        @Test
        fun `correctly extracts page content`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by LivechartPaginationIdRangeSelectorConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = loadTestResource<ByteArray>("crawler/livechart/LivechartPaginationIdRangeSelectorTest/spring-2025.html")
                    )
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val livechartPaginationIdRangeSelector = LivechartPaginationIdRangeSelector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                )

                // when
                val result = livechartPaginationIdRangeSelector.idDownloadList("spring-2025")

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    "11222",
                    "12774",
                    "12832",
                    "12030",
                    "12120",
                    "11460",
                    "11854",
                    "12824",
                    "12773",
                    "12114",
                    "12836",
                    "12684",
                    "12827",
                    "12721",
                    "12795",
                )
            }
        }

        @Test
        fun `removes anime not scheduled for the current week`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by LivechartPaginationIdRangeSelectorConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = loadTestResource<ByteArray>("crawler/livechart/LivechartPaginationIdRangeSelectorTest/spring-2025.html")
                    )
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "12774",
                        "12114",
                        "12827",
                        "12721",
                    )
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val livechartPaginationIdRangeSelector = LivechartPaginationIdRangeSelector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                )

                // when
                val result = livechartPaginationIdRangeSelector.idDownloadList("spring-2025")

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    "11222",
                    "12832",
                    "12030",
                    "12120",
                    "11460",
                    "11854",
                    "12824",
                    "12773",
                    "12836",
                    "12684",
                    "12795",
                )
            }
        }

        @Test
        fun `removes anime have already been downloaded`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by LivechartPaginationIdRangeSelectorConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = loadTestResource<ByteArray>("crawler/livechart/LivechartPaginationIdRangeSelectorTest/spring-2025.html")
                    )
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "12120",
                        "11460",
                        "12774",
                        "12795",
                        "11854",
                    )
                }

                val livechartPaginationIdRangeSelector = LivechartPaginationIdRangeSelector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                )

                // when
                val result = livechartPaginationIdRangeSelector.idDownloadList("spring-2025")

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    "11222",
                    "12832",
                    "12030",
                    "12824",
                    "12773",
                    "12114",
                    "12836",
                    "12684",
                    "12827",
                    "12721",
                )
            }
        }

        @Test
        fun `retrieves anime IDs which are not scheduled for redownload this week only once`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by LivechartPaginationIdRangeSelectorConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = loadTestResource<ByteArray>("crawler/livechart/LivechartPaginationIdRangeSelectorTest/spring-2025.html")
                    )
                }

                var invocations = 0
                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> {
                        invocations++
                        return setOf("12773")
                    }
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val livechartPaginationIdRangeSelector = LivechartPaginationIdRangeSelector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                )
                livechartPaginationIdRangeSelector.idDownloadList("spring-2025")

                // when
                livechartPaginationIdRangeSelector.idDownloadList("spring-2025")

                // then
                assertThat(invocations).isOne()
            }
        }

        @Test
        fun `throws exception if entries on the page cannot be extracted`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by LivechartPaginationIdRangeSelectorConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = "<html></head></body></html>",
                    )
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val livechartPaginationIdRangeSelector = LivechartPaginationIdRangeSelector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = TestAlreadyDownloadedIdsFinder,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    livechartPaginationIdRangeSelector.idDownloadList("spring-2025")
                }

                // then
                assertThat(result).hasMessage("Unable to extract animeIdList.")
            }
        }

        @Test
        fun `returns empty list if http response code is 404`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by LivechartPaginationIdRangeSelectorConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 404,
                        body = "<html></head></body></html>",
                    )
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val livechartPaginationIdRangeSelector = LivechartPaginationIdRangeSelector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                )

                // when
                val result = livechartPaginationIdRangeSelector.idDownloadList("spring-2025")

                // then
                assertThat(result).isEmpty()
            }
        }

        @Test
        fun `returns empty list if html contains an empty list and a note that there are no anime to show`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by LivechartPaginationIdRangeSelectorConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = loadTestResource<String>("crawler/livechart/LivechartPaginationIdRangeSelectorTest/no-anime-to-show.html"),
                    )
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val livechartPaginationIdRangeSelector = LivechartPaginationIdRangeSelector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                )

                // when
                val result = livechartPaginationIdRangeSelector.idDownloadList("spring-2026")

                // then
                assertThat(result).isEmpty()
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = LivechartPaginationIdRangeSelector.instance

                // when
                val result = LivechartPaginationIdRangeSelector.instance

                // then
                assertThat(result).isExactlyInstanceOf(LivechartPaginationIdRangeSelector::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}