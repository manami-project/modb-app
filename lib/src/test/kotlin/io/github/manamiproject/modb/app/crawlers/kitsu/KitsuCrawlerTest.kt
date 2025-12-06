package io.github.manamiproject.modb.app.crawlers.kitsu

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
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Nested
import kotlin.io.path.listDirectoryEntries
import kotlin.test.Test

internal class KitsuCrawlerTest {

    @Nested
    inner class StartTests {

        @Test
        fun `doesn't do anything if the list of IDs is empty`() {
            // given
            val testMetaDataProviderConfig = object: MetaDataProviderConfig by KitsuConfig {
                override fun isTestContext(): Boolean = true
            }

            val testIdRangeSelector = object : IdRangeSelector<Int> {
                override suspend fun idDownloadList(): List<Int> = emptyList()
            }

            val crawler = KitsuCrawler(
                appConfig = TestAppConfig,
                metaDataProviderConfig = testMetaDataProviderConfig,
                downloader = TestDownloader,
                deadEntriesAccess = TestDeadEntriesAccessor,
                idRangeSelector = testIdRangeSelector,
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
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by KitsuConfig {
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

                val crawler = KitsuCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    downloader = testAnimeDownloader,
                    deadEntriesAccess = TestDeadEntriesAccessor,
                    idRangeSelector = testIdRangeSelector,
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
        fun `passes the animeId to dead entries accessor if downloader triggers a dead entry and metaDataProviderConfig is KitsuConfig, because only mian crawler must trigger it`() {
            tempDirectory {
                // given
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

                val crawler = KitsuCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = KitsuConfig,
                    downloader = testAnimeDownloader,
                    deadEntriesAccess = testDeadEntriesAccessor,
                    idRangeSelector = testIdRangeSelector,
                )

                // when
                crawler.start()

                // then
                assertThat(tempDir.listDirectoryEntries()).isEmpty()
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
            // given
            val previous = KitsuCrawler.instance

            // when
            val result = KitsuCrawler.instance

            // then
            assertThat(result).isExactlyInstanceOf(KitsuCrawler::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}