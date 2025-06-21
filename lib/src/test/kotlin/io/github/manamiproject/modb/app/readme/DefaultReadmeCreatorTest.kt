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
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.app.merging.ReviewedIsolatedEntriesAccessor
import io.github.manamiproject.modb.app.merging.lock.MergeLockAccessor
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.readFile
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeRaw
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.livechart.LivechartConfig
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import io.github.manamiproject.modb.notify.NotifyConfig
import io.github.manamiproject.modb.simkl.SimklConfig
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset.UTC
import kotlin.test.Test

internal class DefaultReadmeCreatorTest {

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
                val myanimelistTestEntries = listOf(
                    AnimeRaw("myanimelist 1"),
                    AnimeRaw("myanimelist 2"),
                    AnimeRaw("myanimelist 3"),
                    AnimeRaw("myanimelist 4"),
                    AnimeRaw("myanimelist 5"),
                    AnimeRaw("myanimelist 6"),
                    AnimeRaw("myanimelist 7"),
                    AnimeRaw("myanimelist 8"),
                    AnimeRaw("myanimelist 9"),
                )

                val testAnimePlanetConfig = object: MetaDataProviderConfig by AnimePlanetConfig {
                    override fun isTestContext(): Boolean = true
                }
                val animePlanetTestEntries = listOf(
                    AnimeRaw("animePlanet 1"),
                    AnimeRaw("animePlanet 2"),
                    AnimeRaw("animePlanet 3"),
                    AnimeRaw("animePlanet 4"),
                    AnimeRaw("animePlanet 5"),
                    AnimeRaw("animePlanet 6"),
                    AnimeRaw("animePlanet 7"),
                    AnimeRaw("animePlanet 8"),
                )

                val testKitsuConfig = object: MetaDataProviderConfig by KitsuConfig {
                    override fun isTestContext(): Boolean = true
                }
                val kitsuTestEntries = listOf(
                    AnimeRaw("kitsu 1"),
                    AnimeRaw("kitsu 2"),
                    AnimeRaw("kitsu 3"),
                    AnimeRaw("kitsu 4"),
                    AnimeRaw("kitsu 5"),
                    AnimeRaw("kitsu 6"),
                    AnimeRaw("kitsu 7"),
                )

                val testAnisearchConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anisearchTestEntries = listOf(
                    AnimeRaw("anisearch 1"),
                    AnimeRaw("anisearch 2"),
                    AnimeRaw("anisearch 3"),
                    AnimeRaw("anisearch 4"),
                    AnimeRaw("anisearch 5"),
                    AnimeRaw("anisearch 6"),
                )

                val testNotifyConfig = object: MetaDataProviderConfig by NotifyConfig {
                    override fun isTestContext(): Boolean = true
                }
                val notifyTestEntries = listOf(
                    AnimeRaw("notify 1"),
                    AnimeRaw("notify 2"),
                    AnimeRaw("notify 3"),
                    AnimeRaw("notify 4"),
                    AnimeRaw("notify 5"),
                )

                val testAnilistConfig = object: MetaDataProviderConfig by AnilistConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anilistTestEntries = listOf(
                    AnimeRaw("anilist 1"),
                    AnimeRaw("anilist 2"),
                    AnimeRaw("anilist 3"),
                    AnimeRaw("anilist 4"),
                )

                val testAnidbConfig = object: MetaDataProviderConfig by AnidbConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anidbTestEntries = listOf(
                    AnimeRaw("anidb 1"),
                    AnimeRaw("anidb 2"),
                    AnimeRaw("anidb 3"),
                )

                val testLivechartConfig = object: MetaDataProviderConfig by LivechartConfig {
                    override fun isTestContext(): Boolean = true
                }
                val livechartTestEntries = listOf(
                    AnimeRaw("livechart 1"),
                    AnimeRaw("livechart 2"),
                )

                val testSimklConfig = object: MetaDataProviderConfig by SimklConfig {
                    override fun isTestContext(): Boolean = true
                }
                val simklTestEntries = listOf(
                    AnimeRaw("simkl 1"),
                )

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
                        testSimklConfig,
                    )
                    override fun clock() = Clock.fixed(Instant.parse("2020-05-01T16:02:42.00Z"), UTC)
                }

                val testReviewedIsolatedEntriesAccessor = object: ReviewedIsolatedEntriesAccessor by TestReviewedIsolatedEntriesAccessor {
                    override fun contains(uri: URI): Boolean = false
                }

                val testMergeLockService = object : MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun hasMergeLock(uris: Set<URI>): Boolean = false
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(metaDataProviderConfig: MetaDataProviderConfig): List<AnimeRaw> = when(metaDataProviderConfig.hostname()) {
                        testMyanimelistConfig.hostname() -> myanimelistTestEntries
                        testAnimePlanetConfig.hostname() -> animePlanetTestEntries
                        testKitsuConfig.hostname() -> kitsuTestEntries
                        testAnisearchConfig.hostname() -> anisearchTestEntries
                        testNotifyConfig.hostname() -> notifyTestEntries
                        testAnilistConfig.hostname() -> anilistTestEntries
                        testAnidbConfig.hostname() -> anidbTestEntries
                        testLivechartConfig.hostname() -> livechartTestEntries
                        testSimklConfig.hostname() -> simklTestEntries
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
                val myanimelistTestEntries = listOf(
                    AnimeRaw("myanimelist 1"),
                    AnimeRaw("myanimelist 2"),
                    AnimeRaw("myanimelist 3"),
                    AnimeRaw("myanimelist 4"),
                    AnimeRaw("myanimelist 5"),
                    AnimeRaw("myanimelist 6"),
                    AnimeRaw("myanimelist 7"),
                    AnimeRaw("myanimelist 8"),
                    AnimeRaw("myanimelist 9"),
                )

                val testAnimePlanetConfig = object: MetaDataProviderConfig by AnimePlanetConfig {
                    override fun isTestContext(): Boolean = true
                }
                val animePlanetTestEntries = listOf(
                    AnimeRaw("animePlanet 1"),
                    AnimeRaw("animePlanet 2"),
                    AnimeRaw("animePlanet 3"),
                    AnimeRaw("animePlanet 4"),
                    AnimeRaw("animePlanet 5"),
                    AnimeRaw("animePlanet 6"),
                    AnimeRaw("animePlanet 7"),
                    AnimeRaw("animePlanet 8"),
                )

                val testKitsuConfig = object: MetaDataProviderConfig by KitsuConfig {
                    override fun isTestContext(): Boolean = true
                }
                val kitsuTestEntries = listOf(
                    AnimeRaw("kitsu 1"),
                    AnimeRaw("kitsu 2"),
                    AnimeRaw("kitsu 3"),
                    AnimeRaw("kitsu 4"),
                    AnimeRaw("kitsu 5"),
                    AnimeRaw("kitsu 6"),
                    AnimeRaw("kitsu 7"),
                )

                val testAnisearchConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anisearchTestEntries = listOf(
                    AnimeRaw("anisearch 1"),
                    AnimeRaw("anisearch 2"),
                    AnimeRaw("anisearch 3"),
                    AnimeRaw("anisearch 4"),
                    AnimeRaw("anisearch 5"),
                    AnimeRaw("anisearch 6"),
                )

                val testNotifyConfig = object: MetaDataProviderConfig by NotifyConfig {
                    override fun isTestContext(): Boolean = true
                }
                val notifyTestEntries = listOf(
                    AnimeRaw("notify 1"),
                    AnimeRaw("notify 2"),
                    AnimeRaw("notify 3"),
                    AnimeRaw("notify 4"),
                    AnimeRaw("notify 5"),
                )

                val testAnilistConfig = object: MetaDataProviderConfig by AnilistConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anilistTestEntries = listOf(
                    AnimeRaw("anilist 1"),
                    AnimeRaw("anilist 2"),
                    AnimeRaw("anilist 3"),
                    AnimeRaw("anilist 4"),
                )

                val testAnidbConfig = object: MetaDataProviderConfig by AnidbConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anidbTestEntries = listOf(
                    AnimeRaw("anidb 1"),
                    AnimeRaw("anidb 2"),
                    AnimeRaw("anidb 3"),
                )

                val testLivechartConfig = object: MetaDataProviderConfig by LivechartConfig {
                    override fun isTestContext(): Boolean = true
                }
                val livechartTestEntries = listOf(
                    AnimeRaw("livechart 1"),
                    AnimeRaw("livechart 2"),
                )

                val testSimklConfig = object: MetaDataProviderConfig by SimklConfig {
                    override fun isTestContext(): Boolean = true
                }
                val simklTestEntries = listOf(
                    AnimeRaw("simkl 1"),
                )

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
                        testSimklConfig,
                    )
                    override fun clock() = Clock.fixed(Instant.parse("2020-01-08T16:02:42.00Z"), UTC)
                }

                val testReviewedIsolatedEntriesAccessor = object: ReviewedIsolatedEntriesAccessor by TestReviewedIsolatedEntriesAccessor {
                    override fun contains(uri: URI): Boolean = false
                }

                val testMergeLockService = object : MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun hasMergeLock(uris: Set<URI>): Boolean = false
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(metaDataProviderConfig: MetaDataProviderConfig): List<AnimeRaw> = when(metaDataProviderConfig.hostname()) {
                        testMyanimelistConfig.hostname() -> myanimelistTestEntries
                        testAnimePlanetConfig.hostname() -> animePlanetTestEntries
                        testKitsuConfig.hostname() -> kitsuTestEntries
                        testAnisearchConfig.hostname() -> anisearchTestEntries
                        testNotifyConfig.hostname() -> notifyTestEntries
                        testAnilistConfig.hostname() -> anilistTestEntries
                        testAnidbConfig.hostname() -> anidbTestEntries
                        testLivechartConfig.hostname() -> livechartTestEntries
                        testSimklConfig.hostname() -> simklTestEntries
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
                    Anime("Made in Abyss"),
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
                val myanimelistTestEntries = listOf(
                    AnimeRaw("myanimelist 1"),
                    AnimeRaw("myanimelist 2"),
                    AnimeRaw("myanimelist 3"),
                    AnimeRaw("myanimelist 4"),
                    AnimeRaw("myanimelist 5"),
                    AnimeRaw("myanimelist 6"),
                    AnimeRaw("myanimelist 7"),
                    AnimeRaw("myanimelist 8"),
                    AnimeRaw("myanimelist 9"),
                )

                val testAnimePlanetConfig = object: MetaDataProviderConfig by AnimePlanetConfig {
                    override fun isTestContext(): Boolean = true
                }
                val animePlanetTestEntries = listOf(
                    AnimeRaw("animePlanet 1"),
                    AnimeRaw("animePlanet 2"),
                    AnimeRaw("animePlanet 3"),
                    AnimeRaw("animePlanet 4"),
                    AnimeRaw("animePlanet 5"),
                    AnimeRaw("animePlanet 6"),
                    AnimeRaw("animePlanet 7"),
                    AnimeRaw("animePlanet 8"),
                )

                val testKitsuConfig = object: MetaDataProviderConfig by KitsuConfig {
                    override fun isTestContext(): Boolean = true
                }
                val kitsuTestEntries = listOf(
                    AnimeRaw("kitsu 1"),
                    AnimeRaw("kitsu 2"),
                    AnimeRaw("kitsu 3"),
                    AnimeRaw("kitsu 4"),
                    AnimeRaw("kitsu 5"),
                    AnimeRaw("kitsu 6"),
                    AnimeRaw("kitsu 7"),
                )

                val testAnisearchConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anisearchTestEntries = listOf(
                    AnimeRaw("anisearch 1"),
                    AnimeRaw("anisearch 2"),
                    AnimeRaw("anisearch 3"),
                    AnimeRaw("anisearch 4"),
                    AnimeRaw("anisearch 5"),
                    AnimeRaw("anisearch 6"),
                )

                val testNotifyConfig = object: MetaDataProviderConfig by NotifyConfig {
                    override fun isTestContext(): Boolean = true
                }
                val notifyTestEntries = listOf(
                    AnimeRaw("notify 1"),
                    AnimeRaw("notify 2"),
                    AnimeRaw("notify 3"),
                    AnimeRaw("notify 4"),
                    AnimeRaw("notify 5"),
                )

                val testAnilistConfig = object: MetaDataProviderConfig by AnilistConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anilistTestEntries = listOf(
                    AnimeRaw("anilist 1"),
                    AnimeRaw("anilist 2"),
                    AnimeRaw("anilist 3"),
                    AnimeRaw("anilist 4"),
                )

                val testAnidbConfig = object: MetaDataProviderConfig by AnidbConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anidbTestEntries = listOf(
                    AnimeRaw("anidb 1"),
                    AnimeRaw("anidb 2"),
                    AnimeRaw("anidb 3"),
                )

                val testLivechartConfig = object: MetaDataProviderConfig by LivechartConfig {
                    override fun isTestContext(): Boolean = true
                }
                val livechartTestEntries = listOf(
                    AnimeRaw("livechart 1"),
                    AnimeRaw("livechart 2"),
                )

                val testSimklConfig = object: MetaDataProviderConfig by SimklConfig {
                    override fun isTestContext(): Boolean = true
                }
                val simklTestEntries = listOf(
                    AnimeRaw("simkl 1"),
                )

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
                        testSimklConfig,
                    )
                    override fun clock() = Clock.fixed(Instant.parse("2020-05-01T16:02:42.00Z"), UTC)
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
                    override suspend fun allAnime(metaDataProviderConfig: MetaDataProviderConfig): List<AnimeRaw> = when(metaDataProviderConfig.hostname()) {
                        testMyanimelistConfig.hostname() -> myanimelistTestEntries
                        testAnimePlanetConfig.hostname() -> animePlanetTestEntries
                        testKitsuConfig.hostname() -> kitsuTestEntries
                        testAnisearchConfig.hostname() -> anisearchTestEntries
                        testNotifyConfig.hostname() -> notifyTestEntries
                        testAnilistConfig.hostname() -> anilistTestEntries
                        testAnidbConfig.hostname() -> anidbTestEntries
                        testLivechartConfig.hostname() -> livechartTestEntries
                        testSimklConfig.hostname() -> simklTestEntries
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
                val myanimelistTestEntries = listOf(
                    AnimeRaw("myanimelist 1"),
                    AnimeRaw("myanimelist 2"),
                    AnimeRaw("myanimelist 3"),
                    AnimeRaw("myanimelist 4"),
                    AnimeRaw("myanimelist 5"),
                    AnimeRaw("myanimelist 6"),
                    AnimeRaw("myanimelist 7"),
                    AnimeRaw("myanimelist 8"),
                    AnimeRaw("myanimelist 9"),
                )

                val testAnimePlanetConfig = object: MetaDataProviderConfig by AnimePlanetConfig {
                    override fun isTestContext(): Boolean = true
                }
                val animePlanetTestEntries = listOf(
                    AnimeRaw("animePlanet 1"),
                    AnimeRaw("animePlanet 2"),
                    AnimeRaw("animePlanet 3"),
                    AnimeRaw("animePlanet 4"),
                    AnimeRaw("animePlanet 5"),
                    AnimeRaw("animePlanet 6"),
                    AnimeRaw("animePlanet 7"),
                    AnimeRaw("animePlanet 8"),
                )

                val testKitsuConfig = object: MetaDataProviderConfig by KitsuConfig {
                    override fun isTestContext(): Boolean = true
                }
                val kitsuTestEntries = listOf(
                    AnimeRaw("kitsu 1"),
                    AnimeRaw("kitsu 2"),
                    AnimeRaw("kitsu 3"),
                    AnimeRaw("kitsu 4"),
                    AnimeRaw("kitsu 5"),
                    AnimeRaw("kitsu 6"),
                    AnimeRaw("kitsu 7"),
                )

                val testAnisearchConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anisearchTestEntries = listOf(
                    AnimeRaw("anisearch 1"),
                    AnimeRaw("anisearch 2"),
                    AnimeRaw("anisearch 3"),
                    AnimeRaw("anisearch 4"),
                    AnimeRaw("anisearch 5"),
                    AnimeRaw("anisearch 6"),
                )

                val testNotifyConfig = object: MetaDataProviderConfig by NotifyConfig {
                    override fun isTestContext(): Boolean = true
                }
                val notifyTestEntries = listOf(
                    AnimeRaw("notify 1"),
                    AnimeRaw("notify 2"),
                    AnimeRaw("notify 3"),
                    AnimeRaw("notify 4"),
                    AnimeRaw("notify 5"),
                )

                val testAnilistConfig = object: MetaDataProviderConfig by AnilistConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anilistTestEntries = listOf(
                    AnimeRaw("anilist 1"),
                    AnimeRaw("anilist 2"),
                    AnimeRaw("anilist 3"),
                    AnimeRaw("anilist 4"),
                )

                val testAnidbConfig = object: MetaDataProviderConfig by AnidbConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anidbTestEntries = listOf(
                    AnimeRaw("anidb 1"),
                    AnimeRaw("anidb 2"),
                    AnimeRaw("anidb 3"),
                )

                val testLivechartConfig = object: MetaDataProviderConfig by LivechartConfig {
                    override fun isTestContext(): Boolean = true
                }
                val livechartTestEntries = listOf(
                    AnimeRaw("livechart 1"),
                    AnimeRaw("livechart 2"),
                )

                val testSimklConfig = object: MetaDataProviderConfig by SimklConfig {
                    override fun isTestContext(): Boolean = true
                }
                val simklTestEntries = listOf(
                    AnimeRaw("simkl 1"),
                )

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
                        testSimklConfig,
                    )
                    override fun clock() = Clock.fixed(Instant.parse("2020-05-01T16:02:42.00Z"), UTC)
                }

                val testReviewedIsolatedEntriesAccessor = object: ReviewedIsolatedEntriesAccessor by TestReviewedIsolatedEntriesAccessor {
                    override fun contains(uri: URI): Boolean = true
                }

                val testMergeLockService = object : MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun hasMergeLock(uris: Set<URI>): Boolean = false
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(metaDataProviderConfig: MetaDataProviderConfig): List<AnimeRaw> = when(metaDataProviderConfig.hostname()) {
                        testMyanimelistConfig.hostname() -> myanimelistTestEntries
                        testAnimePlanetConfig.hostname() -> animePlanetTestEntries
                        testKitsuConfig.hostname() -> kitsuTestEntries
                        testAnisearchConfig.hostname() -> anisearchTestEntries
                        testNotifyConfig.hostname() -> notifyTestEntries
                        testAnilistConfig.hostname() -> anilistTestEntries
                        testAnidbConfig.hostname() -> anidbTestEntries
                        testLivechartConfig.hostname() -> livechartTestEntries
                        testSimklConfig.hostname() -> simklTestEntries
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
                        title =  "Death Note",
                    ),
                    Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/34599"),
                        ),
                        title = "Made in Abyss",
                    )
                )

                // when
                defaultReadmeCreator.updateWith(mergedAnime)

                // then
                assertThat(testAppConfig.outputDirectory().resolve("README.md").readFile()).isEqualTo(expectedFile)
            }
        }

        @Test
        fun `list animecountdown with 0 if simkl cannot be found`() {
            tempDirectory {
                // given
                val expectedFile = loadTestResource<String>("readme/DefaultReadmeCreatorTest/animecountdown-with-zero-entries.md")

                val testMyanimelistConfig = object: MetaDataProviderConfig by MyanimelistConfig {
                    override fun isTestContext(): Boolean = true
                }
                val myanimelistTestEntries = listOf(
                    AnimeRaw("myanimelist 1"),
                    AnimeRaw("myanimelist 2"),
                )

                val testAppConfig = object: Config by TestAppConfig {
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                    override fun outputDirectory() = tempDir
                    override fun metaDataProviderConfigurations() = setOf(
                        testMyanimelistConfig,
                    )
                    override fun clock() = Clock.fixed(Instant.parse("2020-05-01T16:02:42.00Z"), UTC)
                }

                val testReviewedIsolatedEntriesAccessor = object: ReviewedIsolatedEntriesAccessor by TestReviewedIsolatedEntriesAccessor {
                    override fun contains(uri: URI): Boolean = false
                }

                val testMergeLockService = object : MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun hasMergeLock(uris: Set<URI>): Boolean = false
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(metaDataProviderConfig: MetaDataProviderConfig): List<AnimeRaw> = when(metaDataProviderConfig.hostname()) {
                        testMyanimelistConfig.hostname() -> myanimelistTestEntries
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
                assertThat(result === previous).isTrue()
            }
        }
    }
}