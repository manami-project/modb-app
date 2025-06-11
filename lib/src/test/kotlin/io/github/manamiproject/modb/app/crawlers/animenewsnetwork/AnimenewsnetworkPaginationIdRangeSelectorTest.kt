package io.github.manamiproject.modb.app.crawlers.animenewsnetwork

import io.github.manamiproject.modb.app.TestAlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.TestDeadEntriesAccessor
import io.github.manamiproject.modb.app.TestDownloadControlStateScheduler
import io.github.manamiproject.modb.app.TestHttpClient
import io.github.manamiproject.modb.app.convfiles.AlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateScheduler
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extractor.XmlDataExtractor
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

internal class AnimenewsnetworkPaginationIdRangeSelectorTest {

    @Test
    fun `correctly extracts page content`() {
        runBlocking {
            // given
            val testHttpClient = object: HttpClient by TestHttpClient {
                override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                    code = 200,
                    body = loadTestResource<ByteArray>("crawler/animenewsnetwork/AnimenewsnetworkPaginationIdRangeSelectorTest/page-x.html"),
                )
            }

            val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
            }

            val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
            }

            val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                override suspend fun fetchDeadEntries(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
            }

            val paginationIdRangeSelector = AnimenewsnetworkPaginationIdRangeSelector(
                metaDataProviderConfig = AnimenewsnetworkPaginationIdRangeSelectorConfig,
                paginationIdRangeSelectorConfig = AnimenewsnetworkPaginationIdRangeSelectorConfig,
                httpClient = testHttpClient,
                extractor = XmlDataExtractor,
                downloadControlStateScheduler = testDownloadControlStateScheduler,
                alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                deadEntriesAccessor = testDeadEntriesAccessor,
            )

            // when
            val result = paginationIdRangeSelector.idDownloadList("x")

            // then
            assertThat(result).containsExactlyInAnyOrder(
                "95",
                "464",
                "3579",
                "4813",
                "608",
                "9160",
                "22983",
                "12366",
                "1992",
                "3886",
                "9747",
                "3356",
                "4569",
                "3612",
                "12600",
                "5528",
                "22512",
                "4759",
                "6319",
                "21250",
                "3248",
                "6052",
                "4212",
                "8551",
                "11396",
                "12056",
                "10583",
            )
        }
    }

    @Test
    fun `removes anime not scheduled for the current week`() {
        runBlocking {
            // given
            val testHttpClient = object: HttpClient by TestHttpClient {
                override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                    code = 200,
                    body = loadTestResource<ByteArray>("crawler/animenewsnetwork/AnimenewsnetworkPaginationIdRangeSelectorTest/page-x.html"),
                )
            }

            val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                    "95",
                    "3886",
                    "21250",
                    "6052",
                    "10583",
                )
            }

            val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
            }

            val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                override suspend fun fetchDeadEntries(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
            }

            val paginationIdRangeSelector = AnimenewsnetworkPaginationIdRangeSelector(
                metaDataProviderConfig = AnimenewsnetworkPaginationIdRangeSelectorConfig,
                paginationIdRangeSelectorConfig = AnimenewsnetworkPaginationIdRangeSelectorConfig,
                httpClient = testHttpClient,
                extractor = XmlDataExtractor,
                downloadControlStateScheduler = testDownloadControlStateScheduler,
                alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                deadEntriesAccessor = testDeadEntriesAccessor,
            )

            // when
            val result = paginationIdRangeSelector.idDownloadList("x")

            // then
            assertThat(result).containsExactlyInAnyOrder(
                "464",
                "3579",
                "4813",
                "608",
                "9160",
                "22983",
                "12366",
                "1992",
                "9747",
                "3356",
                "4569",
                "3612",
                "12600",
                "5528",
                "22512",
                "4759",
                "6319",
                "3248",
                "4212",
                "8551",
                "11396",
                "12056",
            )
        }
    }

    @Test
    fun `removes anime have already been downloaded`() {
        runBlocking {
            // given
            val testHttpClient = object: HttpClient by TestHttpClient {
                override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                    code = 200,
                    body = loadTestResource<ByteArray>("crawler/animenewsnetwork/AnimenewsnetworkPaginationIdRangeSelectorTest/page-x.html"),
                )
            }

            val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
            }

            val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                    "95",
                    "3886",
                    "21250",
                    "6052",
                    "10583",
                )
            }

            val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                override suspend fun fetchDeadEntries(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
            }

            val paginationIdRangeSelector = AnimenewsnetworkPaginationIdRangeSelector(
                metaDataProviderConfig = AnimenewsnetworkPaginationIdRangeSelectorConfig,
                paginationIdRangeSelectorConfig = AnimenewsnetworkPaginationIdRangeSelectorConfig,
                httpClient = testHttpClient,
                extractor = XmlDataExtractor,
                downloadControlStateScheduler = testDownloadControlStateScheduler,
                alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                deadEntriesAccessor = testDeadEntriesAccessor,
            )

            // when
            val result = paginationIdRangeSelector.idDownloadList("x")

            // then
            assertThat(result).containsExactlyInAnyOrder(
                "464",
                "3579",
                "4813",
                "608",
                "9160",
                "22983",
                "12366",
                "1992",
                "9747",
                "3356",
                "4569",
                "3612",
                "12600",
                "5528",
                "22512",
                "4759",
                "6319",
                "3248",
                "4212",
                "8551",
                "11396",
                "12056",
            )
        }
    }

    @Test
    fun `removes dead entries`() {
        runBlocking {
            // given
            val testHttpClient = object: HttpClient by TestHttpClient {
                override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                    code = 200,
                    body = loadTestResource<ByteArray>("crawler/animenewsnetwork/AnimenewsnetworkPaginationIdRangeSelectorTest/page-x.html"),
                )
            }

            val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
            }

            val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
            }

            val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                override suspend fun fetchDeadEntries(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                    "95",
                    "3886",
                    "21250",
                    "6052",
                    "10583",
                )
            }

            val paginationIdRangeSelector = AnimenewsnetworkPaginationIdRangeSelector(
                metaDataProviderConfig = AnimenewsnetworkPaginationIdRangeSelectorConfig,
                paginationIdRangeSelectorConfig = AnimenewsnetworkPaginationIdRangeSelectorConfig,
                httpClient = testHttpClient,
                extractor = XmlDataExtractor,
                downloadControlStateScheduler = testDownloadControlStateScheduler,
                alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                deadEntriesAccessor = testDeadEntriesAccessor,
            )

            // when
            val result = paginationIdRangeSelector.idDownloadList("x")

            // then
            assertThat(result).containsExactlyInAnyOrder(
                "464",
                "3579",
                "4813",
                "608",
                "9160",
                "22983",
                "12366",
                "1992",
                "9747",
                "3356",
                "4569",
                "3612",
                "12600",
                "5528",
                "22512",
                "4759",
                "6319",
                "3248",
                "4212",
                "8551",
                "11396",
                "12056",
            )
        }
    }

    @Test
    fun `retrieves anime IDs which are not scheduled for redownload this week only once`() {
        runBlocking {
            // given
            val testHttpClient = object: HttpClient by TestHttpClient {
                override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                    code = 200,
                    body = loadTestResource<ByteArray>("crawler/animenewsnetwork/AnimenewsnetworkPaginationIdRangeSelectorTest/page-x.html"),
                )
            }

            var invocations = 0
            val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> {
                    invocations ++
                    return setOf("2085")
                }
            }

            val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
            }

            val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                override suspend fun fetchDeadEntries(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
            }

            val paginationIdRangeSelector = AnimenewsnetworkPaginationIdRangeSelector(
                metaDataProviderConfig = AnimenewsnetworkPaginationIdRangeSelectorConfig,
                paginationIdRangeSelectorConfig = AnimenewsnetworkPaginationIdRangeSelectorConfig,
                httpClient = testHttpClient,
                extractor = XmlDataExtractor,
                downloadControlStateScheduler = testDownloadControlStateScheduler,
                alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                deadEntriesAccessor = testDeadEntriesAccessor,
            )

            paginationIdRangeSelector.idDownloadList("x")

            // when
            paginationIdRangeSelector.idDownloadList("x")

            // then
            assertThat(invocations).isOne()
        }
    }

    @Test
    fun `throws exception if entries on the page cannot be extracted`() {
        runBlocking {
            // given
            val testHttpClient = object: HttpClient by TestHttpClient {
                override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                    code = 200,
                    body = "<html></head></body></html>".toByteArray(),
                )
            }

            val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
            }

            val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                override suspend fun fetchDeadEntries(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
            }

            val paginationIdRangeSelector = AnimenewsnetworkPaginationIdRangeSelector(
                metaDataProviderConfig = AnimenewsnetworkPaginationIdRangeSelectorConfig,
                paginationIdRangeSelectorConfig = AnimenewsnetworkPaginationIdRangeSelectorConfig,
                httpClient = testHttpClient,
                extractor = XmlDataExtractor,
                downloadControlStateScheduler = testDownloadControlStateScheduler,
                alreadyDownloadedIdsFinder = TestAlreadyDownloadedIdsFinder,
                deadEntriesAccessor = testDeadEntriesAccessor,
            )

            // when
            val result = exceptionExpected<IllegalStateException> {
                paginationIdRangeSelector.idDownloadList("x")
            }

            // then
            assertThat(result).hasMessage("Unable to extract entriesOnThePage.")
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = AnimenewsnetworkPaginationIdRangeSelector.instance

                // when
                val result = AnimenewsnetworkPaginationIdRangeSelector.instance

                // then
                assertThat(result).isExactlyInstanceOf(AnimenewsnetworkPaginationIdRangeSelector::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}