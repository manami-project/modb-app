package io.github.manamiproject.modb.app.crawlers.anilist

import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestDeadEntriesAccessor
import io.github.manamiproject.modb.app.TestDownloader
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.crawlers.IdRangeSelector
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.readFile
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class AnilistCrawlerTest {

    @Nested
    inner class StartTests {

        @Test
        fun `doesn't do anything if the list of IDs is empty`() {
            // given
            val testIdRangeSelector = object : IdRangeSelector<Int> {
                override suspend fun idDownloadList(): List<Int> = emptyList()
            }

            val crawler = AnilistCrawler(
                appConfig = TestAppConfig,
                downloader = TestDownloader,
                deadEntriesAccess = TestDeadEntriesAccessor,
                idRangeSelector = testIdRangeSelector
            )

            // when
            assertThatNoException().isThrownBy {
                runBlocking { crawler.start() }
            }
        }

        @Test
        fun `successfully loads multiple entries`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnilistConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object : Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testIdRangeSelector = object: IdRangeSelector<Int> {
                    override suspend fun idDownloadList(): List<Int> = listOf(1535, 23, 1254, 424)
                }

                val successfulEntry = "successfully loaded content for"
                val testAnimeDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        return "$successfulEntry $id"
                    }
                }

                val crawler = AnilistCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloader = testAnimeDownloader,
                    deadEntriesAccess = TestDeadEntriesAccessor,
                    idRangeSelector = testIdRangeSelector
                )

                // when
                crawler.start()

                // then
                testIdRangeSelector.idDownloadList().forEach { id ->
                    val result = tempDir.resolve("$id.json").readFile()
                    assertThat(result).isEqualTo("$successfulEntry $id")
                }
            }
        }

        @Test
        fun `passes the animeId to the dead entries accessor if the downloader triggers a dead entry`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by AnilistConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object : Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testIdRangeSelector = object: IdRangeSelector<Int> {
                    override suspend fun idDownloadList(): List<Int> = listOf(1000)
                }

                val testAnimeDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        onDeadEntry.invoke(id)
                        return EMPTY
                    }
                }

                val invocations = mutableListOf<AnimeId>()
                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override suspend fun addDeadEntry(animeId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig) {
                        invocations.add(animeId)
                    }
                }

                val crawler = AnilistCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloader = testAnimeDownloader,
                    deadEntriesAccess = testDeadEntriesAccessor,
                    idRangeSelector = testIdRangeSelector
                )

                // when
                crawler.start()

                // then
                assertThat(invocations).containsExactlyInAnyOrder(
                    "1000",
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
                val previous = AnilistCrawler.instance

                // when
                val result = AnilistCrawler.instance

                // then
                assertThat(result).isExactlyInstanceOf(AnilistCrawler::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}