package io.github.manamiproject.modb.app.crawlers.animeplanet

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

internal class AnimePlanetPaginationIdRangeSelectorTest {

    @Nested
    inner class IdDownloadListTests {

        @Test
        fun `throws exception if entriesOnThePage cannot be located`() {
            // given
            val testHttpClient = object: HttpClient by TestHttpClient {
                override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                    code = 200,
                    body = "<html></head></body></html>",
                )
            }

            val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
            }

            val animePlanetPaginationIdRangeSelector = AnimePlanetPaginationIdRangeSelector(
                httpClient = testHttpClient,
                downloadControlStateScheduler = testDownloadControlStateScheduler,
                alreadyDownloadedIdsFinder = TestAlreadyDownloadedIdsFinder,
            )

            // when
            val result = exceptionExpected<IllegalStateException> {
                animePlanetPaginationIdRangeSelector.idDownloadList(4)
            }

            // then
            assertThat(result).hasMessage("Unable to locate entries on page.")
        }

        @Test
        fun `correctly extracts anime IDs from page`() {
            runBlocking {
                // given
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = loadTestResource<ByteArray>("crawler/animeplanet/AnimePlanetPaginationIdRangeSelectorTest/page-31.html"),
                    )
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val animePlanetPaginationIdRangeSelector = AnimePlanetPaginationIdRangeSelector(
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                )

                // when
                val result = animePlanetPaginationIdRangeSelector.idDownloadList(31)

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    "a-piece-of-cake",
                    "ao-subarashii-sekai",
                    "aoki-and-taka-no-tsume",
                    "ao-shi-jiu-chong-tian",
                    "aotu-shijie",
                    "aoshima-megu",
                    "aotu-xueyuan-3",
                    "aoku-tsuzuku-pass",
                    "ape-escape-2nd-season",
                    "ape-escape",
                    "a-piece-of-green",
                    "ao-no-doumon",
                    "aotu-xueyuan-ge-rui-de-xinnian",
                    "ao-no-butai-ni-omoi-wo-yosete",
                    "apartment",
                    "apfelland-monogatari",
                    "a-pawn",
                    "ao-oni-the-animation-movie",
                    "aotu-shijie-2",
                    "aoki-uru-overture",
                    "a-place-further-than-the-universe-yokoku",
                    "a-piece-of-phantasmagoria",
                    "a-place-further-than-the-universe",
                    "aotu-xueyuan",
                    "aotenjou-no-clown",
                    "aos",
                    "aoki-uru",
                    "aotu-shijie-3",
                    "aotu-shijie-4",
                    "aotu-shijie-3-tebie-pian",
                    "apache-yakyuugun",
                    "aotu-xueyuan-2",
                    "a-piper",
                    "ao-oni-the-blue-monster",
                    "aozora",
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
                        body = loadTestResource<ByteArray>("crawler/animeplanet/AnimePlanetPaginationIdRangeSelectorTest/page-31.html"),
                    )
                }

                var invocations = 0
                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> {
                        invocations++
                        return setOf("test-value")
                    }
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val animePlanetPaginationIdRangeSelector = AnimePlanetPaginationIdRangeSelector(
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                )

                animePlanetPaginationIdRangeSelector.idDownloadList(31)

                // when
                animePlanetPaginationIdRangeSelector.idDownloadList(31)

                // then
                assertThat(invocations).isOne()
            }
        }

        @Test
        fun `result is in random order`() {
            runBlocking {
                // given
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = loadTestResource<ByteArray>("crawler/animeplanet/AnimePlanetPaginationIdRangeSelectorTest/page-31.html"),
                    )
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val animePlanetPaginationIdRangeSelector = AnimePlanetPaginationIdRangeSelector(
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                )

                // when
                val result = animePlanetPaginationIdRangeSelector.idDownloadList(31)

                // then
                val expectedIds = setOf(
                    "a-pawn",
                    "a-piece-of-cake",
                    "a-piece-of-green",
                    "a-piece-of-phantasmagoria",
                    "a-piper",
                    "a-place-further-than-the-universe-yokoku",
                    "a-place-further-than-the-universe",
                    "ao-no-butai-ni-omoi-wo-yosete",
                    "ao-no-doumon",
                    "ao-oni-the-animation-movie",
                    "ao-oni-the-blue-monster",
                    "ao-shi-jiu-chong-tian",
                    "ao-subarashii-sekai",
                    "aoki-and-taka-no-tsume",
                    "aoki-uru-overture",
                    "aoki-uru",
                    "aoku-tsuzuku-pass",
                    "aos",
                    "aoshima-megu",
                    "aotenjou-no-clown",
                    "aotu-shijie-2",
                    "aotu-shijie-3-tebie-pian",
                    "aotu-shijie-3",
                    "aotu-shijie-4",
                    "aotu-shijie",
                    "aotu-xueyuan-2",
                    "aotu-xueyuan-3",
                    "aotu-xueyuan-ge-rui-de-xinnian",
                    "aotu-xueyuan",
                    "aozora",
                    "apache-yakyuugun",
                    "apartment",
                    "ape-escape-2nd-season",
                    "ape-escape",
                    "apfelland-monogatari",
                )
                assertThat(result).containsAll(expectedIds)
                assertThat(result).doesNotContainSequence(expectedIds)
            }
        }

        @Test
        fun `anime IDs which have been downloaded already are excluded from the result`() {
            runBlocking {
                // given
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = loadTestResource<ByteArray>("crawler/animeplanet/AnimePlanetPaginationIdRangeSelectorTest/page-31.html"),
                    )
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "aos",
                        "a-piper",
                        "aozora",
                    )
                }

                val animePlanetPaginationIdRangeSelector = AnimePlanetPaginationIdRangeSelector(
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                )

                // when
                val result = animePlanetPaginationIdRangeSelector.idDownloadList(31)

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    "a-piece-of-cake",
                    "ao-subarashii-sekai",
                    "aoki-and-taka-no-tsume",
                    "ao-shi-jiu-chong-tian",
                    "aotu-shijie",
                    "aoshima-megu",
                    "aotu-xueyuan-3",
                    "aoku-tsuzuku-pass",
                    "ape-escape-2nd-season",
                    "ape-escape",
                    "a-piece-of-green",
                    "ao-no-doumon",
                    "aotu-xueyuan-ge-rui-de-xinnian",
                    "ao-no-butai-ni-omoi-wo-yosete",
                    "apartment",
                    "apfelland-monogatari",
                    "a-pawn",
                    "ao-oni-the-animation-movie",
                    "aotu-shijie-2",
                    "aoki-uru-overture",
                    "a-place-further-than-the-universe-yokoku",
                    "a-piece-of-phantasmagoria",
                    "a-place-further-than-the-universe",
                    "aotu-xueyuan",
                    "aotenjou-no-clown",
                    "aoki-uru",
                    "aotu-shijie-3",
                    "aotu-shijie-4",
                    "aotu-shijie-3-tebie-pian",
                    "apache-yakyuugun",
                    "aotu-xueyuan-2",
                    "ao-oni-the-blue-monster",
                )
            }
        }

        @Test
        fun `anime not scheduled for redownload this week are removed from the result`() {
            runBlocking {
                // given
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = loadTestResource<ByteArray>("crawler/animeplanet/AnimePlanetPaginationIdRangeSelectorTest/page-31.html"),
                    )
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf(
                        "a-place-further-than-the-universe-yokoku",
                        "a-piece-of-green",
                        "a-piece-of-cake",
                        "ao-subarashii-sekai",
                        "aoki-and-taka-no-tsume",
                    )
                }

                val testAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val animePlanetPaginationIdRangeSelector = AnimePlanetPaginationIdRangeSelector(
                    httpClient = testHttpClient,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testAlreadyDownloadedIdsFinder,
                )

                // when
                val result = animePlanetPaginationIdRangeSelector.idDownloadList(31)

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    "ao-shi-jiu-chong-tian",
                    "aotu-shijie",
                    "aoshima-megu",
                    "aotu-xueyuan-3",
                    "aoku-tsuzuku-pass",
                    "ape-escape-2nd-season",
                    "ape-escape",
                    "ao-no-doumon",
                    "aotu-xueyuan-ge-rui-de-xinnian",
                    "ao-no-butai-ni-omoi-wo-yosete",
                    "apartment",
                    "apfelland-monogatari",
                    "a-pawn",
                    "ao-oni-the-animation-movie",
                    "aotu-shijie-2",
                    "aoki-uru-overture",
                    "a-piece-of-phantasmagoria",
                    "a-place-further-than-the-universe",
                    "aotu-xueyuan",
                    "aotenjou-no-clown",
                    "aos",
                    "aoki-uru",
                    "aotu-shijie-3",
                    "aotu-shijie-4",
                    "aotu-shijie-3-tebie-pian",
                    "apache-yakyuugun",
                    "aotu-xueyuan-2",
                    "a-piper",
                    "ao-oni-the-blue-monster",
                    "aozora",
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
                val previous = AnimePlanetPaginationIdRangeSelector.instance

                // when
                val result = AnimePlanetPaginationIdRangeSelector.instance

                // then
                assertThat(result).isExactlyInstanceOf(AnimePlanetPaginationIdRangeSelector::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}