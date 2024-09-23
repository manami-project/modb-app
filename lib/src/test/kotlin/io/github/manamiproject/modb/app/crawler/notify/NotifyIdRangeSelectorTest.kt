package io.github.manamiproject.modb.app.crawler.notify

import io.github.manamiproject.modb.app.TestAlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.TestDownloadControlStateScheduler
import io.github.manamiproject.modb.app.TestHttpClient
import io.github.manamiproject.modb.app.convfiles.AlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateScheduler
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.notify.NotifyConfig
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URL
import kotlin.test.Test

class NotifyIdRangeSelectorTest {

    @Nested
    inner class IdDownloadListTests {

        @Test
        fun `combines IDs from 'any' and 'music'`() {
            runBlocking {
                // given
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        val responseBody = when {
                            url.toString().substringAfterLast('/') == "any" -> loadTestResource<String>("crawler/notify/NotifyIdRangeSelectorTest/ids_for_any.html")
                            url.toString().substringAfterLast('/') == "music" -> loadTestResource<String>("crawler/notify/NotifyIdRangeSelectorTest/ids_for_music.html")
                            else -> shouldNotBeInvoked()
                        }

                        return HttpResponse(200, responseBody.toByteArray())
                    }
                }

                val testDowloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val notifyIdRangeSelector = NotifyIdRangeSelector(
                    metaDataProviderConfig = NotifyConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDowloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                )

                // when
                val result = notifyIdRangeSelector.idDownloadList()

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    "9h8IHNjWR",
                    "M_NrcCWWR",
                    "ENlCnwZGR",
                    "_9rVj4zMg",
                )
            }
        }

        @Test
        fun `result is in random order`() {
            runBlocking {
                // given
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        val responseBody = when {
                            url.toString().substringAfterLast('/') == "any" -> loadTestResource<String>("crawler/notify/NotifyIdRangeSelectorTest/ids_for_any.html")
                            url.toString().substringAfterLast('/') == "music" -> loadTestResource<String>("crawler/notify/NotifyIdRangeSelectorTest/ids_for_music.html")
                            else -> shouldNotBeInvoked()
                        }

                        return HttpResponse(200, responseBody.toByteArray())
                    }
                }

                val testDowloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val notifyIdRangeSelector = NotifyIdRangeSelector(
                    metaDataProviderConfig = NotifyConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDowloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                )

                // when
                val result = notifyIdRangeSelector.idDownloadList()

                // then
                val expectedIds = setOf(
                    "M_NrcCWWR",
                    "ENlCnwZGR",
                    "_9rVj4zMg",
                    "9h8IHNjWR",
                )
                assertThat(result).containsAll(expectedIds)
                assertThat(result).doesNotContainSequence(expectedIds)
            }
        }

        @Test
        fun `adds IDs which are scheduled for this week in case they are missing in the overview list`() {
            runBlocking {
                // given
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        val responseBody = when {
                            url.toString().substringAfterLast('/') == "any" -> loadTestResource<String>("crawler/notify/NotifyIdRangeSelectorTest/ids_for_any.html")
                            url.toString().substringAfterLast('/') == "music" -> loadTestResource<String>("crawler/notify/NotifyIdRangeSelectorTest/ids_for_music.html")
                            else -> shouldNotBeInvoked()
                        }

                        return HttpResponse(200, responseBody.toByteArray())
                    }
                }

                val testDowloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf("--rEdownLoAD")
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val notifyIdRangeSelector = NotifyIdRangeSelector(
                    metaDataProviderConfig = NotifyConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDowloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                )

                // when
                val result = notifyIdRangeSelector.idDownloadList()

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    "9h8IHNjWR",
                    "M_NrcCWWR",
                    "ENlCnwZGR",
                    "_9rVj4zMg",
                    "--rEdownLoAD",
                )
            }
        }

        @Test
        fun `removes IDs which are not scheduled for current week from generated sequence`() {
            runBlocking {
                // given
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        val responseBody = when {
                            url.toString().substringAfterLast('/') == "any" -> loadTestResource<String>("crawler/notify/NotifyIdRangeSelectorTest/ids_for_any.html")
                            url.toString().substringAfterLast('/') == "music" -> loadTestResource<String>("crawler/notify/NotifyIdRangeSelectorTest/ids_for_music.html")
                            else -> shouldNotBeInvoked()
                        }

                        return HttpResponse(200, responseBody.toByteArray())
                    }
                }

                val testDowloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "9h8IHNjWR",
                        "ENlCnwZGR",
                    )
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val notifyIdRangeSelector = NotifyIdRangeSelector(
                    metaDataProviderConfig = NotifyConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDowloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                )

                // when
                val result = notifyIdRangeSelector.idDownloadList()

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    "M_NrcCWWR",
                    "_9rVj4zMg",
                )
            }
        }

        @Test
        fun `removes IDs which have already been downloaded from generated sequence`() {
            runBlocking {
                // given
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        val responseBody = when {
                            url.toString().substringAfterLast('/') == "any" -> loadTestResource<String>("crawler/notify/NotifyIdRangeSelectorTest/ids_for_any.html")
                            url.toString().substringAfterLast('/') == "music" -> loadTestResource<String>("crawler/notify/NotifyIdRangeSelectorTest/ids_for_music.html")
                            else -> shouldNotBeInvoked()
                        }

                        return HttpResponse(200, responseBody.toByteArray())
                    }
                }

                val testDowloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "M_NrcCWWR",
                        "_9rVj4zMg",
                    )
                }

                val notifyIdRangeSelector = NotifyIdRangeSelector(
                    metaDataProviderConfig = NotifyConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDowloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                )

                // when
                val result = notifyIdRangeSelector.idDownloadList()

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    "ENlCnwZGR",
                    "9h8IHNjWR",
                )
            }
        }

        @Test
        fun `throws exception if idRange cannot be extracted for 'any'`() {
            runBlocking {
                // given
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        val responseBody = when {
                            url.toString().substringAfterLast('/') == "any" -> "<html></head></body></html>"
                            url.toString().substringAfterLast('/') == "music" -> loadTestResource<String>("crawler/notify/NotifyIdRangeSelectorTest/ids_for_music.html")
                            else -> shouldNotBeInvoked()
                        }

                        return HttpResponse(200, responseBody.toByteArray())
                    }
                }

                val testDowloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val notifyIdRangeSelector = NotifyIdRangeSelector(
                    metaDataProviderConfig = NotifyConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDowloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = TestAlreadyDownloadedIdsFinder,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    notifyIdRangeSelector.idDownloadList()
                }

                // then
                assertThat(result).hasMessage("Unable to extract idRange.")
            }
        }

        @Test
        fun `throws exception if idRange cannot be extracted for 'music'`() {
            runBlocking {
                // given
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                        val responseBody = when {
                            url.toString().substringAfterLast('/') == "any" -> loadTestResource<String>("crawler/notify/NotifyIdRangeSelectorTest/ids_for_any.html")
                            url.toString().substringAfterLast('/') == "music" -> "<html></head></body></html>"
                            else -> shouldNotBeInvoked()
                        }

                        return HttpResponse(200, responseBody.toByteArray())
                    }
                }

                val testDowloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val notifyIdRangeSelector = NotifyIdRangeSelector(
                    metaDataProviderConfig = NotifyConfig,
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDowloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = TestAlreadyDownloadedIdsFinder,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    notifyIdRangeSelector.idDownloadList()
                }

                // then
                assertThat(result).hasMessage("Unable to extract idRange.")
            }
        }
    }
}