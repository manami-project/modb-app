package io.github.manamiproject.modb.app.crawlers.anisearch

import io.github.manamiproject.modb.anisearch.AnisearchConfig
import io.github.manamiproject.modb.app.TestAlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.TestDownloadControlStateScheduler
import io.github.manamiproject.modb.app.TestHttpClient
import io.github.manamiproject.modb.app.TestNetworkController
import io.github.manamiproject.modb.app.convfiles.AlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateScheduler
import io.github.manamiproject.modb.app.network.NetworkController
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.loadTestResource
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.URL
import java.net.UnknownHostException
import kotlin.test.Test

internal class AnisearchPaginationIdRangeSelectorTest {

    @Nested
    inner class IdDownloadListTests {

        @Test
        fun `correctly extracts page content`() {
            runBlocking {
                // given
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = loadTestResource("crawler/anisearch/AnisearchPaginationIdRangeSelectorTest/page-4.html"),
                    )
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val anisearchPaginationIdRangeSelector = AnisearchPaginationIdRangeSelector(
                    metaDataProviderConfig = AnisearchConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    networkController = TestNetworkController,
                )

                // when
                val result = anisearchPaginationIdRangeSelector.idDownloadList(4)

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    "789",
                    "2085",
                    "14073",
                    "12453",
                    "18571",
                    "2668",
                    "1119",
                    "9910",
                    "696",
                    "7697",
                    "3378",
                    "13644",
                    "1278",
                    "6187",
                    "11923",
                    "11896",
                    "18960",
                    "4122",
                    "5949",
                    "16522",
                    "12256",
                    "729",
                    "19492",
                    "3191",
                    "2428",
                    "13735",
                    "15502",
                    "3511",
                    "3887",
                    "590",
                    "19262",
                    "13581",
                    "17764",
                    "2665",
                    "9476",
                    "17964",
                    "18535",
                    "3374",
                    "3306",
                    "14712",
                    "14894",
                    "18428",
                    "14707",
                    "4038",
                    "3797",
                    "18130",
                    "10557",
                    "13321",
                    "11254",
                    "18603",
                    "1415",
                    "11491",
                    "8357",
                    "7821",
                    "18440",
                    "14635",
                    "1502",
                    "8389",
                    "16924",
                    "6508",
                    "12281",
                    "14028",
                    "3383",
                    "16002",
                    "4424",
                    "10674",
                    "8156",
                    "18444",
                    "19034",
                    "15285",
                    "13866",
                    "8774",
                    "5530",
                    "6434",
                    "5008",
                    "18269",
                    "1804",
                    "18648",
                    "5028",
                    "13134",
                    "10677",
                    "10821",
                    "18663",
                    "13294",
                    "8294",
                    "18848",
                    "13292",
                    "18912",
                    "14902",
                    "10115",
                    "12117",
                    "6502",
                    "1030",
                    "12211",
                    "1443",
                    "13416",
                    "9817",
                    "14613",
                    "7847",
                    "11193",
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
                        body = loadTestResource("crawler/anisearch/AnisearchPaginationIdRangeSelectorTest/page-4.html"),
                    )
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "789",
                        "13581",
                        "18603",
                        "18663",
                        "9476",
                    )
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val anisearchPaginationIdRangeSelector = AnisearchPaginationIdRangeSelector(
                    metaDataProviderConfig = AnisearchConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    networkController = TestNetworkController,
                )

                // when
                val result = anisearchPaginationIdRangeSelector.idDownloadList(4)

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    "2085",
                    "14073",
                    "12453",
                    "18571",
                    "2668",
                    "1119",
                    "9910",
                    "696",
                    "7697",
                    "3378",
                    "13644",
                    "1278",
                    "6187",
                    "11923",
                    "11896",
                    "18960",
                    "4122",
                    "5949",
                    "16522",
                    "12256",
                    "729",
                    "19492",
                    "3191",
                    "2428",
                    "13735",
                    "15502",
                    "3511",
                    "3887",
                    "590",
                    "19262",
                    "17764",
                    "2665",
                    "17964",
                    "18535",
                    "3374",
                    "3306",
                    "14712",
                    "14894",
                    "18428",
                    "14707",
                    "4038",
                    "3797",
                    "18130",
                    "10557",
                    "13321",
                    "11254",
                    "1415",
                    "11491",
                    "8357",
                    "7821",
                    "18440",
                    "14635",
                    "1502",
                    "8389",
                    "16924",
                    "6508",
                    "12281",
                    "14028",
                    "3383",
                    "16002",
                    "4424",
                    "10674",
                    "8156",
                    "18444",
                    "19034",
                    "15285",
                    "13866",
                    "8774",
                    "5530",
                    "6434",
                    "5008",
                    "18269",
                    "1804",
                    "18648",
                    "5028",
                    "13134",
                    "10677",
                    "10821",
                    "13294",
                    "8294",
                    "18848",
                    "13292",
                    "18912",
                    "14902",
                    "10115",
                    "12117",
                    "6502",
                    "1030",
                    "12211",
                    "1443",
                    "13416",
                    "9817",
                    "14613",
                    "7847",
                    "11193",
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
                        body = loadTestResource("crawler/anisearch/AnisearchPaginationIdRangeSelectorTest/page-4.html"),
                    )
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "6187",
                        "11923",
                        "14707",
                        "4038",
                        "9817",
                        "14613",
                        "7847",
                        "11193",
                    )
                }

                val anisearchPaginationIdRangeSelector = AnisearchPaginationIdRangeSelector(
                    metaDataProviderConfig = AnisearchConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    networkController = TestNetworkController,
                )

                // when
                val result = anisearchPaginationIdRangeSelector.idDownloadList(4)

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    "789",
                    "2085",
                    "14073",
                    "12453",
                    "18571",
                    "2668",
                    "1119",
                    "9910",
                    "696",
                    "7697",
                    "3378",
                    "13644",
                    "1278",
                    "11896",
                    "18960",
                    "4122",
                    "5949",
                    "16522",
                    "12256",
                    "729",
                    "19492",
                    "3191",
                    "2428",
                    "13735",
                    "15502",
                    "3511",
                    "3887",
                    "590",
                    "19262",
                    "13581",
                    "17764",
                    "2665",
                    "9476",
                    "17964",
                    "18535",
                    "3374",
                    "3306",
                    "14712",
                    "14894",
                    "18428",
                    "3797",
                    "18130",
                    "10557",
                    "13321",
                    "11254",
                    "18603",
                    "1415",
                    "11491",
                    "8357",
                    "7821",
                    "18440",
                    "14635",
                    "1502",
                    "8389",
                    "16924",
                    "6508",
                    "12281",
                    "14028",
                    "3383",
                    "16002",
                    "4424",
                    "10674",
                    "8156",
                    "18444",
                    "19034",
                    "15285",
                    "13866",
                    "8774",
                    "5530",
                    "6434",
                    "5008",
                    "18269",
                    "1804",
                    "18648",
                    "5028",
                    "13134",
                    "10677",
                    "10821",
                    "18663",
                    "13294",
                    "8294",
                    "18848",
                    "13292",
                    "18912",
                    "14902",
                    "10115",
                    "12117",
                    "6502",
                    "1030",
                    "12211",
                    "1443",
                    "13416",
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
                        body = loadTestResource("crawler/anisearch/AnisearchPaginationIdRangeSelectorTest/page-4.html"),
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

                val anisearchPaginationIdRangeSelector = AnisearchPaginationIdRangeSelector(
                    metaDataProviderConfig = AnisearchConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    networkController = TestNetworkController,
                )
                anisearchPaginationIdRangeSelector.idDownloadList(4)

                // when
                anisearchPaginationIdRangeSelector.idDownloadList(4)

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

                val anisearchPaginationIdRangeSelector = AnisearchPaginationIdRangeSelector(
                    metaDataProviderConfig = AnisearchConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = TestAlreadyDownloadedIdsFinder,
                    networkController = TestNetworkController,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    anisearchPaginationIdRangeSelector.idDownloadList(4)
                }

                // then
                assertThat(result).hasMessage("Unable to extract entriesOnThePage.")
            }
        }

        @Test
        fun `initiates a restart of the network controller if a ConnectException is thrown`() {
            runBlocking {
                // given
                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                var hasBeenInvoked = false
                val testNetworkController = object: NetworkController by TestNetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> {
                        hasBeenInvoked = true
                        return async { true }
                    }
                }

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        return if (hasBeenInvoked) {
                            HttpResponse(
                                code = 200,
                                body = loadTestResource("crawler/anisearch/AnisearchLastPageDetectorTest/page-1.html"),
                            )
                        } else {
                            throw ConnectException()
                        }
                    }
                }

                val anisearchPaginationIdRangeSelector = AnisearchPaginationIdRangeSelector(
                    metaDataProviderConfig = AnisearchConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    networkController = testNetworkController,
                )

                // when
                anisearchPaginationIdRangeSelector.idDownloadList(4)

                // then
                assertThat(hasBeenInvoked).isTrue()
            }
        }

        @Test
        fun `initiates a restart of the network controller if a UnknownHostException is thrown`() {
            runBlocking {
                // given
                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                var hasBeenInvoked = false
                val testNetworkController = object: NetworkController by TestNetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> {
                        hasBeenInvoked = true
                        return async { true }
                    }
                }

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        return if (hasBeenInvoked) {
                            HttpResponse(
                                code = 200,
                                body = loadTestResource("crawler/anisearch/AnisearchLastPageDetectorTest/page-1.html"),
                            )
                        } else {
                            throw UnknownHostException()
                        }
                    }
                }

                val anisearchPaginationIdRangeSelector = AnisearchPaginationIdRangeSelector(
                    metaDataProviderConfig = AnisearchConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    networkController = testNetworkController,
                )

                // when
                anisearchPaginationIdRangeSelector.idDownloadList(4)

                // then
                assertThat(hasBeenInvoked).isTrue()
            }
        }

        @Test
        fun `initiates a restart of the network controller if a NoRouteToHostException is thrown`() {
            runBlocking {
                // given
                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                var hasBeenInvoked = false
                val testNetworkController = object: NetworkController by TestNetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> {
                        hasBeenInvoked = true
                        return async { true }
                    }
                }

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        return if (hasBeenInvoked) {
                            HttpResponse(
                                code = 200,
                                body = loadTestResource("crawler/anisearch/AnisearchLastPageDetectorTest/page-1.html"),
                            )
                        } else {
                            throw NoRouteToHostException()
                        }
                    }
                }

                val anisearchPaginationIdRangeSelector = AnisearchPaginationIdRangeSelector(
                    metaDataProviderConfig = AnisearchConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    networkController = testNetworkController,
                )

                // when
                anisearchPaginationIdRangeSelector.idDownloadList(4)

                // then
                assertThat(hasBeenInvoked).isTrue()
            }
        }

        @Test
        fun `throws an exception if a restart of the network controller didn't help`() {
            runBlocking {
                // given
                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testNetworkController = object: NetworkController by TestNetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> {
                        return async { true }
                    }
                }

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        throw NoRouteToHostException("junit test")
                    }
                }

                val anisearchPaginationIdRangeSelector = AnisearchPaginationIdRangeSelector(
                    metaDataProviderConfig = AnisearchConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    networkController = testNetworkController,
                )

                // when
                val result = exceptionExpected<NoRouteToHostException> {
                    anisearchPaginationIdRangeSelector.idDownloadList(4)
                }

                // then
                assertThat(result).hasMessage("junit test")
            }
        }

        @Test
        fun `directly throws exception if it's not one of the cases that restart the network controller`() {
            runBlocking {
                // given
                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testNetworkController = object: NetworkController by TestNetworkController {
                    override suspend fun restartAsync(): Deferred<Boolean> {
                        return async { true }
                    }
                }

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        throw NullPointerException("junit test")
                    }
                }

                val anisearchPaginationIdRangeSelector = AnisearchPaginationIdRangeSelector(
                    metaDataProviderConfig = AnisearchConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                    networkController = testNetworkController,
                )

                // when
                val result = exceptionExpected<NullPointerException> {
                    anisearchPaginationIdRangeSelector.idDownloadList(4)
                }

                // then
                assertThat(result).hasMessage("junit test")
            }
        }
    }
}