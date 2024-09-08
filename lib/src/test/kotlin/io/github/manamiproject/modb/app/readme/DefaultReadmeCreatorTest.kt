package io.github.manamiproject.modb.app.readme

import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import io.github.manamiproject.modb.anisearch.AnisearchConfig
import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestDownloadControlStateAccessor
import io.github.manamiproject.modb.app.TestMergeLockAccessor
import io.github.manamiproject.modb.app.TestReviewedIsolatedEntriesAccessor
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.downloadcontrolstate.DOWNLOAD_CONTROL_STATE_FILE_SUFFIX
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.app.merging.ReviewedIsolatedEntriesAccessor
import io.github.manamiproject.modb.app.merging.lock.MergeLockAccessor
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.readFile
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.livechart.LivechartConfig
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import io.github.manamiproject.modb.notify.NotifyConfig
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset.UTC
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.test.Test

class DefaultReadmeCreatorTest {

    @Nested
    inner class UpdateWithTests {

        @Test
        fun `successfully updates the date, the amount of entries as well as the number of raw entries`() {
            tempDirectory {
                // given
                val expectedFile = loadTestResource<String>("readme/DefaultReadmeCreatorTest/expected-default.md")

                val testMyanimelistConfig = object: MetaDataProviderConfig by MyanimelistConfig {
                    override fun isTestContext(): Boolean = true
                }
                val malDcsDir = tempDir.resolve(testMyanimelistConfig.hostname()).createDirectory()

                malDcsDir.resolve("mal_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_4.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_5.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_6.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_7.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_8.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testAnimePlanetConfig = object: MetaDataProviderConfig by AnimePlanetConfig {
                    override fun isTestContext(): Boolean = true
                }
                val animePlanetDcsDir = tempDir.resolve(testAnimePlanetConfig.hostname()).createDirectory()

                animePlanetDcsDir.resolve("anime-planet_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_4.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_5.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_6.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_7.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testKitsuConfig = object: MetaDataProviderConfig by KitsuConfig {
                    override fun isTestContext(): Boolean = true
                }
                val kitsuDcsDir = tempDir.resolve(testKitsuConfig.hostname()).createDirectory()

                kitsuDcsDir.resolve("kitsu_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                kitsuDcsDir.resolve("kitsu_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                kitsuDcsDir.resolve("kitsu_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                kitsuDcsDir.resolve("kitsu_4.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                kitsuDcsDir.resolve("kitsu_5.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                kitsuDcsDir.resolve("kitsu_6.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testAnisearchConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anisearchDcsDir = tempDir.resolve(testAnisearchConfig.hostname()).createDirectory()

                anisearchDcsDir.resolve("anisearch_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anisearchDcsDir.resolve("anisearch_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anisearchDcsDir.resolve("anisearch_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anisearchDcsDir.resolve("anisearch_4.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anisearchDcsDir.resolve("anisearch_5.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testNotifyConfig = object: MetaDataProviderConfig by NotifyConfig {
                    override fun isTestContext(): Boolean = true
                }
                val notifyDcsDir = tempDir.resolve(testNotifyConfig.hostname()).createDirectory()

                notifyDcsDir.resolve("notify_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                notifyDcsDir.resolve("notify_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                notifyDcsDir.resolve("notify_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                notifyDcsDir.resolve("notify_4.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testAnilistConfig = object: MetaDataProviderConfig by AnilistConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anilistDcsDir = tempDir.resolve(testAnilistConfig.hostname()).createDirectory()

                anilistDcsDir.resolve("anilist_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anilistDcsDir.resolve("anilist_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anilistDcsDir.resolve("anilist_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testAnidbConfig = object: MetaDataProviderConfig by AnidbConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anidbDcsDir = tempDir.resolve(testAnidbConfig.hostname()).createDirectory()

                anidbDcsDir.resolve("anidb_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anidbDcsDir.resolve("anidb_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testLivechartConfig = object: MetaDataProviderConfig by LivechartConfig {
                    override fun isTestContext(): Boolean = true
                }
                val livechartDcsDir = tempDir.resolve(testLivechartConfig.hostname()).createDirectory()

                livechartDcsDir.resolve("livechart_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testAppConfig = object: Config by TestAppConfig {
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                    override fun outputDirectory() = tempDir
                    override fun metaDataProviderConfigurations() = setOf(
                        testMyanimelistConfig,
                        testAnimePlanetConfig,
                        testKitsuConfig,
                        testAnisearchConfig,
                        testNotifyConfig,
                        testAnilistConfig,
                        testAnidbConfig,
                        testLivechartConfig,
                    )
                    override fun clock() = Clock.fixed(Instant.parse("2020-05-01T16:02:42.00Z"), UTC)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val testReviewedIsolatedEntriesAccessor = object: ReviewedIsolatedEntriesAccessor by TestReviewedIsolatedEntriesAccessor {
                    override fun contains(uri: URI): Boolean = false
                }

                val testMergeLockService = object : MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun hasMergeLock(uris: Set<URI>): Boolean = false
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = when(metaDataProviderConfig.hostname()) {
                        testMyanimelistConfig.hostname() -> malDcsDir
                        testAnimePlanetConfig.hostname() -> animePlanetDcsDir
                        testKitsuConfig.hostname() -> kitsuDcsDir
                        testAnisearchConfig.hostname() -> anisearchDcsDir
                        testNotifyConfig.hostname() -> notifyDcsDir
                        testAnilistConfig.hostname() -> anilistDcsDir
                        testAnidbConfig.hostname() -> anidbDcsDir
                        testLivechartConfig.hostname() -> livechartDcsDir
                        else -> shouldNotBeInvoked()
                    }
                }

                val defaultReadmeCreator = DefaultReadmeCreator(
                    appConfig = testAppConfig,
                    mergeLockAccessor = testMergeLockService,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    reviewedIsolatedEntriesAccessor = testReviewedIsolatedEntriesAccessor,
                )

                val mergedAnime = listOf(
                    Anime("Death Note"),
                    Anime("Made in Abyss")
                )

                // when
                defaultReadmeCreator.updateWith(mergedAnime)

                // then
                assertThat(testAppConfig.outputDirectory().resolve("README.md").readFile()).isEqualTo(expectedFile)
            }
        }

        @Test
        fun `zero based week`() {
            tempDirectory {
                // given
                val expectedFile = loadTestResource<String>("readme/DefaultReadmeCreatorTest/zero-based-week.md")

                val testMyanimelistConfig = object: MetaDataProviderConfig by MyanimelistConfig {
                    override fun isTestContext(): Boolean = true
                }
                val malDcsDir = tempDir.resolve(testMyanimelistConfig.hostname()).createDirectory()

                malDcsDir.resolve("mal_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_4.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_5.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_6.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_7.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_8.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testAnimePlanetConfig = object: MetaDataProviderConfig by AnimePlanetConfig {
                    override fun isTestContext(): Boolean = true
                }
                val animePlanetDcsDir = tempDir.resolve(testAnimePlanetConfig.hostname()).createDirectory()

                animePlanetDcsDir.resolve("anime-planet_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_4.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_5.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_6.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_7.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testKitsuConfig = object: MetaDataProviderConfig by KitsuConfig {
                    override fun isTestContext(): Boolean = true
                }
                val kitsuDcsDir = tempDir.resolve(testKitsuConfig.hostname()).createDirectory()

                kitsuDcsDir.resolve("kitsu_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                kitsuDcsDir.resolve("kitsu_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                kitsuDcsDir.resolve("kitsu_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                kitsuDcsDir.resolve("kitsu_4.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                kitsuDcsDir.resolve("kitsu_5.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                kitsuDcsDir.resolve("kitsu_6.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testAnisearchConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anisearchDcsDir = tempDir.resolve(testAnisearchConfig.hostname()).createDirectory()

                anisearchDcsDir.resolve("anisearch_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anisearchDcsDir.resolve("anisearch_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anisearchDcsDir.resolve("anisearch_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anisearchDcsDir.resolve("anisearch_4.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anisearchDcsDir.resolve("anisearch_5.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testNotifyConfig = object: MetaDataProviderConfig by NotifyConfig {
                    override fun isTestContext(): Boolean = true
                }
                val notifyDcsDir = tempDir.resolve(testNotifyConfig.hostname()).createDirectory()

                notifyDcsDir.resolve("notify_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                notifyDcsDir.resolve("notify_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                notifyDcsDir.resolve("notify_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                notifyDcsDir.resolve("notify_4.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testAnilistConfig = object: MetaDataProviderConfig by AnilistConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anilistDcsDir = tempDir.resolve(testAnilistConfig.hostname()).createDirectory()

                anilistDcsDir.resolve("anilist_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anilistDcsDir.resolve("anilist_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anilistDcsDir.resolve("anilist_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testAnidbConfig = object: MetaDataProviderConfig by AnidbConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anidbDcsDir = tempDir.resolve(testAnidbConfig.hostname()).createDirectory()

                anidbDcsDir.resolve("anidb_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anidbDcsDir.resolve("anidb_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testLivechartConfig = object: MetaDataProviderConfig by LivechartConfig {
                    override fun isTestContext(): Boolean = true
                }
                val livechartDcsDir = tempDir.resolve(testLivechartConfig.hostname()).createDirectory()

                livechartDcsDir.resolve("livechart_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testAppConfig = object: Config by TestAppConfig {
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                    override fun outputDirectory() = tempDir
                    override fun metaDataProviderConfigurations() = setOf(
                        testMyanimelistConfig,
                        testAnimePlanetConfig,
                        testKitsuConfig,
                        testAnisearchConfig,
                        testNotifyConfig,
                        testAnilistConfig,
                        testAnidbConfig,
                        testLivechartConfig,
                    )
                    override fun clock() = Clock.fixed(Instant.parse("2020-01-08T16:02:42.00Z"), UTC)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val testReviewedIsolatedEntriesAccessor = object: ReviewedIsolatedEntriesAccessor by TestReviewedIsolatedEntriesAccessor {
                    override fun contains(uri: URI): Boolean = false
                }

                val testMergeLockService = object : MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun hasMergeLock(uris: Set<URI>): Boolean = false
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = when(metaDataProviderConfig.hostname()) {
                        testMyanimelistConfig.hostname() -> malDcsDir
                        testAnimePlanetConfig.hostname() -> animePlanetDcsDir
                        testKitsuConfig.hostname() -> kitsuDcsDir
                        testAnisearchConfig.hostname() -> anisearchDcsDir
                        testNotifyConfig.hostname() -> notifyDcsDir
                        testAnilistConfig.hostname() -> anilistDcsDir
                        testAnidbConfig.hostname() -> anidbDcsDir
                        testLivechartConfig.hostname() -> livechartDcsDir
                        else -> shouldNotBeInvoked()
                    }
                }

                val defaultReadmeCreator = DefaultReadmeCreator(
                    appConfig = testAppConfig,
                    mergeLockAccessor = testMergeLockService,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    reviewedIsolatedEntriesAccessor = testReviewedIsolatedEntriesAccessor,
                )

                val mergedAnime = listOf(
                    Anime("Death Note"),
                    Anime("Made in Abyss")
                )

                // when
                defaultReadmeCreator.updateWith(mergedAnime)

                // then
                assertThat(testAppConfig.outputDirectory().resolve("README.md").readFile()).isEqualTo(expectedFile)
            }
        }

        @Test
        fun `mere locks have impact on the percentage of reviewed entries`() {
            tempDirectory {
                // given
                val expectedFile = loadTestResource<String>("readme/DefaultReadmeCreatorTest/merge-locks.md")

                val testMyanimelistConfig = object: MetaDataProviderConfig by MyanimelistConfig {
                    override fun isTestContext(): Boolean = true
                }
                val malDcsDir = tempDir.resolve(testMyanimelistConfig.hostname()).createDirectory()

                malDcsDir.resolve("mal_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_4.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_5.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_6.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_7.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_8.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testAnimePlanetConfig = object: MetaDataProviderConfig by AnimePlanetConfig {
                    override fun isTestContext(): Boolean = true
                }
                val animePlanetDcsDir = tempDir.resolve(testAnimePlanetConfig.hostname()).createDirectory()

                animePlanetDcsDir.resolve("anime-planet_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_4.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_5.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_6.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_7.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testKitsuConfig = object: MetaDataProviderConfig by KitsuConfig {
                    override fun isTestContext(): Boolean = true
                }
                val kitsuDcsDir = tempDir.resolve(testKitsuConfig.hostname()).createDirectory()

                kitsuDcsDir.resolve("kitsu_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                kitsuDcsDir.resolve("kitsu_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                kitsuDcsDir.resolve("kitsu_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                kitsuDcsDir.resolve("kitsu_4.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                kitsuDcsDir.resolve("kitsu_5.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                kitsuDcsDir.resolve("kitsu_6.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testAnisearchConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anisearchDcsDir = tempDir.resolve(testAnisearchConfig.hostname()).createDirectory()

                anisearchDcsDir.resolve("anisearch_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anisearchDcsDir.resolve("anisearch_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anisearchDcsDir.resolve("anisearch_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anisearchDcsDir.resolve("anisearch_4.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anisearchDcsDir.resolve("anisearch_5.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testNotifyConfig = object: MetaDataProviderConfig by NotifyConfig {
                    override fun isTestContext(): Boolean = true
                }
                val notifyDcsDir = tempDir.resolve(testNotifyConfig.hostname()).createDirectory()

                notifyDcsDir.resolve("notify_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                notifyDcsDir.resolve("notify_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                notifyDcsDir.resolve("notify_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                notifyDcsDir.resolve("notify_4.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testAnilistConfig = object: MetaDataProviderConfig by AnilistConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anilistDcsDir = tempDir.resolve(testAnilistConfig.hostname()).createDirectory()

                anilistDcsDir.resolve("anilist_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anilistDcsDir.resolve("anilist_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anilistDcsDir.resolve("anilist_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testAnidbConfig = object: MetaDataProviderConfig by AnidbConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anidbDcsDir = tempDir.resolve(testAnidbConfig.hostname()).createDirectory()

                anidbDcsDir.resolve("anidb_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anidbDcsDir.resolve("anidb_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testLivechartConfig = object: MetaDataProviderConfig by LivechartConfig {
                    override fun isTestContext(): Boolean = true
                }
                val livechartDcsDir = tempDir.resolve(testLivechartConfig.hostname()).createDirectory()

                livechartDcsDir.resolve("livechart_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testAppConfig = object: Config by TestAppConfig {
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                    override fun outputDirectory() = tempDir
                    override fun metaDataProviderConfigurations() = setOf(
                        testMyanimelistConfig,
                        testAnimePlanetConfig,
                        testKitsuConfig,
                        testAnisearchConfig,
                        testNotifyConfig,
                        testAnilistConfig,
                        testAnidbConfig,
                        testLivechartConfig,
                    )
                    override fun clock() = Clock.fixed(Instant.parse("2020-05-01T16:02:42.00Z"), UTC)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val testReviewedIsolatedEntriesAccessor = object: ReviewedIsolatedEntriesAccessor by TestReviewedIsolatedEntriesAccessor {
                    override fun contains(uri: URI): Boolean = false
                }

                val testMergeLockService = object : MergeLockAccessor by TestMergeLockAccessor {
                    var hasBeenInvoked = false
                    override suspend fun hasMergeLock(uris: Set<URI>): Boolean {
                        val ret = hasBeenInvoked
                        if (!hasBeenInvoked) hasBeenInvoked = true
                        return ret
                    }
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = when(metaDataProviderConfig.hostname()) {
                        testMyanimelistConfig.hostname() -> malDcsDir
                        testAnimePlanetConfig.hostname() -> animePlanetDcsDir
                        testKitsuConfig.hostname() -> kitsuDcsDir
                        testAnisearchConfig.hostname() -> anisearchDcsDir
                        testNotifyConfig.hostname() -> notifyDcsDir
                        testAnilistConfig.hostname() -> anilistDcsDir
                        testAnidbConfig.hostname() -> anidbDcsDir
                        testLivechartConfig.hostname() -> livechartDcsDir
                        else -> shouldNotBeInvoked()
                    }
                }

                val defaultReadmeCreator = DefaultReadmeCreator(
                    appConfig = testAppConfig,
                    mergeLockAccessor = testMergeLockService,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    reviewedIsolatedEntriesAccessor = testReviewedIsolatedEntriesAccessor,
                )

                val mergedAnime = listOf(
                    Anime("Death Note"),
                    Anime("Made in Abyss")
                )

                // when
                defaultReadmeCreator.updateWith(mergedAnime)

                // then
                assertThat(testAppConfig.outputDirectory().resolve("README.md").readFile()).isEqualTo(expectedFile)
            }
        }

        @Test
        fun `reviewed isolated entries have impact on the percentage of reviewed entries`() {
            tempDirectory {
                // given
                val expectedFile = loadTestResource<String>("readme/DefaultReadmeCreatorTest/reviewed-isolated-entries.md")

                val testMyanimelistConfig = object: MetaDataProviderConfig by MyanimelistConfig {
                    override fun isTestContext(): Boolean = true
                }
                val malDcsDir = tempDir.resolve(testMyanimelistConfig.hostname()).createDirectory()

                malDcsDir.resolve("mal_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_4.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_5.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_6.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_7.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                malDcsDir.resolve("mal_8.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testAnimePlanetConfig = object: MetaDataProviderConfig by AnimePlanetConfig {
                    override fun isTestContext(): Boolean = true
                }
                val animePlanetDcsDir = tempDir.resolve(testAnimePlanetConfig.hostname()).createDirectory()

                animePlanetDcsDir.resolve("anime-planet_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_4.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_5.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_6.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                animePlanetDcsDir.resolve("anime-planet_7.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testKitsuConfig = object: MetaDataProviderConfig by KitsuConfig {
                    override fun isTestContext(): Boolean = true
                }
                val kitsuDcsDir = tempDir.resolve(testKitsuConfig.hostname()).createDirectory()

                kitsuDcsDir.resolve("kitsu_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                kitsuDcsDir.resolve("kitsu_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                kitsuDcsDir.resolve("kitsu_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                kitsuDcsDir.resolve("kitsu_4.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                kitsuDcsDir.resolve("kitsu_5.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                kitsuDcsDir.resolve("kitsu_6.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testAnisearchConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anisearchDcsDir = tempDir.resolve(testAnisearchConfig.hostname()).createDirectory()

                anisearchDcsDir.resolve("anisearch_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anisearchDcsDir.resolve("anisearch_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anisearchDcsDir.resolve("anisearch_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anisearchDcsDir.resolve("anisearch_4.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anisearchDcsDir.resolve("anisearch_5.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testNotifyConfig = object: MetaDataProviderConfig by NotifyConfig {
                    override fun isTestContext(): Boolean = true
                }
                val notifyDcsDir = tempDir.resolve(testNotifyConfig.hostname()).createDirectory()

                notifyDcsDir.resolve("notify_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                notifyDcsDir.resolve("notify_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                notifyDcsDir.resolve("notify_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                notifyDcsDir.resolve("notify_4.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testAnilistConfig = object: MetaDataProviderConfig by AnilistConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anilistDcsDir = tempDir.resolve(testAnilistConfig.hostname()).createDirectory()

                anilistDcsDir.resolve("anilist_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anilistDcsDir.resolve("anilist_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anilistDcsDir.resolve("anilist_3.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testAnidbConfig = object: MetaDataProviderConfig by AnidbConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anidbDcsDir = tempDir.resolve(testAnidbConfig.hostname()).createDirectory()

                anidbDcsDir.resolve("anidb_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                anidbDcsDir.resolve("anidb_2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testLivechartConfig = object: MetaDataProviderConfig by LivechartConfig {
                    override fun isTestContext(): Boolean = true
                }
                val livechartDcsDir = tempDir.resolve(testLivechartConfig.hostname()).createDirectory()

                livechartDcsDir.resolve("livechart_1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val testAppConfig = object: Config by TestAppConfig {
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                    override fun outputDirectory() = tempDir
                    override fun metaDataProviderConfigurations() = setOf(
                        testMyanimelistConfig,
                        testAnimePlanetConfig,
                        testKitsuConfig,
                        testAnisearchConfig,
                        testNotifyConfig,
                        testAnilistConfig,
                        testAnidbConfig,
                        testLivechartConfig,
                    )
                    override fun clock() = Clock.fixed(Instant.parse("2020-05-01T16:02:42.00Z"), UTC)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val testReviewedIsolatedEntriesAccessor = object: ReviewedIsolatedEntriesAccessor by TestReviewedIsolatedEntriesAccessor {
                    override fun contains(uri: URI): Boolean = true
                }

                val testMergeLockService = object : MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun hasMergeLock(uris: Set<URI>): Boolean = false
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = when(metaDataProviderConfig.hostname()) {
                        testMyanimelistConfig.hostname() -> malDcsDir
                        testAnimePlanetConfig.hostname() -> animePlanetDcsDir
                        testKitsuConfig.hostname() -> kitsuDcsDir
                        testAnisearchConfig.hostname() -> anisearchDcsDir
                        testNotifyConfig.hostname() -> notifyDcsDir
                        testAnilistConfig.hostname() -> anilistDcsDir
                        testAnidbConfig.hostname() -> anidbDcsDir
                        testLivechartConfig.hostname() -> livechartDcsDir
                        else -> shouldNotBeInvoked()
                    }
                }

                val defaultReadmeCreator = DefaultReadmeCreator(
                    appConfig = testAppConfig,
                    mergeLockAccessor = testMergeLockService,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    reviewedIsolatedEntriesAccessor = testReviewedIsolatedEntriesAccessor,
                )

                val mergedAnime = listOf(
                    Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/1535"),
                        ),
                        _title =  "Death Note",
                    ),
                    Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/34599"),
                        ),
                        _title = "Made in Abyss",
                    )
                )

                // when
                defaultReadmeCreator.updateWith(mergedAnime)

                // then
                assertThat(testAppConfig.outputDirectory().resolve("README.md").readFile()).isEqualTo(expectedFile)
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = DefaultReadmeCreator.instance

                // when
                val result = DefaultReadmeCreator.instance

                // then
                assertThat(result).isExactlyInstanceOf(DefaultReadmeCreator::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}