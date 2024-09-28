package io.github.manamiproject.modb.app.crawlers.notify

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestDownloadControlStateAccessor
import io.github.manamiproject.modb.app.TestDownloader
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.crawlers.IdRangeSelector
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.readFile
import io.github.manamiproject.modb.notify.NotifyConfig
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class NotifyCrawlerTest {

    @Nested
    inner class StartTests {

        @Test
        fun `doesn't do anything if the list of IDs is empty`() {
            // given
            val testMetaDataProviderConfig = object: MetaDataProviderConfig by NotifyConfig {
                override fun isTestContext(): Boolean = true
            }

            val testIdRangeSelector = object : IdRangeSelector<AnimeId> {
                override suspend fun idDownloadList(): List<AnimeId> = emptyList()
            }

            val crawler = NotifyCrawler(
                appConfig = TestAppConfig,
                metaDataProviderConfig = testMetaDataProviderConfig,
                idRangeSelector = testIdRangeSelector,
                downloader = TestDownloader,
                downloadControlStateAccessor = TestDownloadControlStateAccessor,
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
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by NotifyConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object : Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testIdRangeSelector = object: IdRangeSelector<AnimeId> {
                    override suspend fun idDownloadList(): List<AnimeId> = listOf("hq344hw-2", "2gu35z2z5", "90f3f--03f3", "h-2j3t2h")
                }

                val successfulEntry = "successfully loaded content for"
                val testAnimeDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        return "$successfulEntry $id"
                    }
                }

                val crawler = NotifyCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    idRangeSelector = testIdRangeSelector,
                    downloader = testAnimeDownloader,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
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
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by NotifyConfig {
                    override fun isTestContext(): Boolean = true
                }

                val testAppConfig = object : Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testIdRangeSelector = object: IdRangeSelector<AnimeId> {
                    override suspend fun idDownloadList(): List<AnimeId> = listOf("hq344hw-2")
                }

                val testAnimeDownloader = object: Downloader by TestDownloader {
                    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String {
                        onDeadEntry.invoke(id)
                        return EMPTY
                    }
                }

                val invocations = mutableListOf<AnimeId>()
                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) {
                        invocations.add(animeId)
                    }
                }

                val crawler = NotifyCrawler(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    idRangeSelector = testIdRangeSelector,
                    downloader = testAnimeDownloader,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                crawler.start()

                // then
                assertThat(invocations).containsExactlyInAnyOrder(
                    "hq344hw-2",
                )
            }
        }
    }
}