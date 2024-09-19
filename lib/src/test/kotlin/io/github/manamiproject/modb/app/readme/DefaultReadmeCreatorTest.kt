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
                val myanimelistTestEntries = listOf(
                    Anime("myanimelist 1"),
                    Anime("myanimelist 2"),
                    Anime("myanimelist 3"),
                    Anime("myanimelist 4"),
                    Anime("myanimelist 5"),
                    Anime("myanimelist 6"),
                    Anime("myanimelist 7"),
                    Anime("myanimelist 8"),
                )

                val testAnimePlanetConfig = object: MetaDataProviderConfig by AnimePlanetConfig {
                    override fun isTestContext(): Boolean = true
                }
                val animePlanetTestEntries = listOf(
                    Anime("animePlanet 1"),
                    Anime("animePlanet 2"),
                    Anime("animePlanet 3"),
                    Anime("animePlanet 4"),
                    Anime("animePlanet 5"),
                    Anime("animePlanet 6"),
                    Anime("animePlanet 7"),
                )

                val testKitsuConfig = object: MetaDataProviderConfig by KitsuConfig {
                    override fun isTestContext(): Boolean = true
                }
                val kitsuTestEntries = listOf(
                    Anime("kitsu 1"),
                    Anime("kitsu 2"),
                    Anime("kitsu 3"),
                    Anime("kitsu 4"),
                    Anime("kitsu 5"),
                    Anime("kitsu 6"),
                )

                val testAnisearchConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anisearchTestEntries = listOf(
                    Anime("anisearch 1"),
                    Anime("anisearch 2"),
                    Anime("anisearch 3"),
                    Anime("anisearch 4"),
                    Anime("anisearch 5"),
                )

                val testNotifyConfig = object: MetaDataProviderConfig by NotifyConfig {
                    override fun isTestContext(): Boolean = true
                }
                val notifyTestEntries = listOf(
                    Anime("notify 1"),
                    Anime("notify 2"),
                    Anime("notify 3"),
                    Anime("notify 4"),
                )

                val testAnilistConfig = object: MetaDataProviderConfig by AnilistConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anilistTestEntries = listOf(
                    Anime("anilist 1"),
                    Anime("anilist 2"),
                    Anime("anilist 3"),
                )

                val testAnidbConfig = object: MetaDataProviderConfig by AnidbConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anidbTestEntries = listOf(
                    Anime("anidb 1"),
                    Anime("anidb 2"),
                )

                val testLivechartConfig = object: MetaDataProviderConfig by LivechartConfig {
                    override fun isTestContext(): Boolean = true
                }
                val livechartTestEntries = listOf(
                    Anime("livechart 1"),
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
                    override suspend fun allAnime(metaDataProviderConfig: MetaDataProviderConfig): List<Anime> = when(metaDataProviderConfig.hostname()) {
                        testMyanimelistConfig.hostname() -> myanimelistTestEntries
                        testAnimePlanetConfig.hostname() -> animePlanetTestEntries
                        testKitsuConfig.hostname() -> kitsuTestEntries
                        testAnisearchConfig.hostname() -> anisearchTestEntries
                        testNotifyConfig.hostname() -> notifyTestEntries
                        testAnilistConfig.hostname() -> anilistTestEntries
                        testAnidbConfig.hostname() -> anidbTestEntries
                        testLivechartConfig.hostname() -> livechartTestEntries
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
                    Anime("myanimelist 1"),
                    Anime("myanimelist 2"),
                    Anime("myanimelist 3"),
                    Anime("myanimelist 4"),
                    Anime("myanimelist 5"),
                    Anime("myanimelist 6"),
                    Anime("myanimelist 7"),
                    Anime("myanimelist 8"),
                )

                val testAnimePlanetConfig = object: MetaDataProviderConfig by AnimePlanetConfig {
                    override fun isTestContext(): Boolean = true
                }
                val animePlanetTestEntries = listOf(
                    Anime("animePlanet 1"),
                    Anime("animePlanet 2"),
                    Anime("animePlanet 3"),
                    Anime("animePlanet 4"),
                    Anime("animePlanet 5"),
                    Anime("animePlanet 6"),
                    Anime("animePlanet 7"),
                )

                val testKitsuConfig = object: MetaDataProviderConfig by KitsuConfig {
                    override fun isTestContext(): Boolean = true
                }
                val kitsuTestEntries = listOf(
                    Anime("kitsu 1"),
                    Anime("kitsu 2"),
                    Anime("kitsu 3"),
                    Anime("kitsu 4"),
                    Anime("kitsu 5"),
                    Anime("kitsu 6"),
                )

                val testAnisearchConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anisearchTestEntries = listOf(
                    Anime("anisearch 1"),
                    Anime("anisearch 2"),
                    Anime("anisearch 3"),
                    Anime("anisearch 4"),
                    Anime("anisearch 5"),
                )

                val testNotifyConfig = object: MetaDataProviderConfig by NotifyConfig {
                    override fun isTestContext(): Boolean = true
                }
                val notifyTestEntries = listOf(
                    Anime("notify 1"),
                    Anime("notify 2"),
                    Anime("notify 3"),
                    Anime("notify 4"),
                )

                val testAnilistConfig = object: MetaDataProviderConfig by AnilistConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anilistTestEntries = listOf(
                    Anime("anilist 1"),
                    Anime("anilist 2"),
                    Anime("anilist 3"),
                )

                val testAnidbConfig = object: MetaDataProviderConfig by AnidbConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anidbTestEntries = listOf(
                    Anime("anidb 1"),
                    Anime("anidb 2"),
                )

                val testLivechartConfig = object: MetaDataProviderConfig by LivechartConfig {
                    override fun isTestContext(): Boolean = true
                }
                val livechartTestEntries = listOf(
                    Anime("livechart 1"),
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
                    override suspend fun allAnime(metaDataProviderConfig: MetaDataProviderConfig): List<Anime> = when(metaDataProviderConfig.hostname()) {
                        testMyanimelistConfig.hostname() -> myanimelistTestEntries
                        testAnimePlanetConfig.hostname() -> animePlanetTestEntries
                        testKitsuConfig.hostname() -> kitsuTestEntries
                        testAnisearchConfig.hostname() -> anisearchTestEntries
                        testNotifyConfig.hostname() -> notifyTestEntries
                        testAnilistConfig.hostname() -> anilistTestEntries
                        testAnidbConfig.hostname() -> anidbTestEntries
                        testLivechartConfig.hostname() -> livechartTestEntries
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
                    Anime("myanimelist 1"),
                    Anime("myanimelist 2"),
                    Anime("myanimelist 3"),
                    Anime("myanimelist 4"),
                    Anime("myanimelist 5"),
                    Anime("myanimelist 6"),
                    Anime("myanimelist 7"),
                    Anime("myanimelist 8"),
                )

                val testAnimePlanetConfig = object: MetaDataProviderConfig by AnimePlanetConfig {
                    override fun isTestContext(): Boolean = true
                }
                val animePlanetTestEntries = listOf(
                    Anime("animePlanet 1"),
                    Anime("animePlanet 2"),
                    Anime("animePlanet 3"),
                    Anime("animePlanet 4"),
                    Anime("animePlanet 5"),
                    Anime("animePlanet 6"),
                    Anime("animePlanet 7"),
                )

                val testKitsuConfig = object: MetaDataProviderConfig by KitsuConfig {
                    override fun isTestContext(): Boolean = true
                }
                val kitsuTestEntries = listOf(
                    Anime("kitsu 1"),
                    Anime("kitsu 2"),
                    Anime("kitsu 3"),
                    Anime("kitsu 4"),
                    Anime("kitsu 5"),
                    Anime("kitsu 6"),
                )

                val testAnisearchConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anisearchTestEntries = listOf(
                    Anime("anisearch 1"),
                    Anime("anisearch 2"),
                    Anime("anisearch 3"),
                    Anime("anisearch 4"),
                    Anime("anisearch 5"),
                )

                val testNotifyConfig = object: MetaDataProviderConfig by NotifyConfig {
                    override fun isTestContext(): Boolean = true
                }
                val notifyTestEntries = listOf(
                    Anime("notify 1"),
                    Anime("notify 2"),
                    Anime("notify 3"),
                    Anime("notify 4"),
                )

                val testAnilistConfig = object: MetaDataProviderConfig by AnilistConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anilistTestEntries = listOf(
                    Anime("anilist 1"),
                    Anime("anilist 2"),
                    Anime("anilist 3"),
                )

                val testAnidbConfig = object: MetaDataProviderConfig by AnidbConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anidbTestEntries = listOf(
                    Anime("anidb 1"),
                    Anime("anidb 2"),
                )

                val testLivechartConfig = object: MetaDataProviderConfig by LivechartConfig {
                    override fun isTestContext(): Boolean = true
                }
                val livechartTestEntries = listOf(
                    Anime("livechart 1"),
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
                    override suspend fun allAnime(metaDataProviderConfig: MetaDataProviderConfig): List<Anime> = when(metaDataProviderConfig.hostname()) {
                        testMyanimelistConfig.hostname() -> myanimelistTestEntries
                        testAnimePlanetConfig.hostname() -> animePlanetTestEntries
                        testKitsuConfig.hostname() -> kitsuTestEntries
                        testAnisearchConfig.hostname() -> anisearchTestEntries
                        testNotifyConfig.hostname() -> notifyTestEntries
                        testAnilistConfig.hostname() -> anilistTestEntries
                        testAnidbConfig.hostname() -> anidbTestEntries
                        testLivechartConfig.hostname() -> livechartTestEntries
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
                    Anime("myanimelist 1"),
                    Anime("myanimelist 2"),
                    Anime("myanimelist 3"),
                    Anime("myanimelist 4"),
                    Anime("myanimelist 5"),
                    Anime("myanimelist 6"),
                    Anime("myanimelist 7"),
                    Anime("myanimelist 8"),
                )

                val testAnimePlanetConfig = object: MetaDataProviderConfig by AnimePlanetConfig {
                    override fun isTestContext(): Boolean = true
                }
                val animePlanetTestEntries = listOf(
                    Anime("animePlanet 1"),
                    Anime("animePlanet 2"),
                    Anime("animePlanet 3"),
                    Anime("animePlanet 4"),
                    Anime("animePlanet 5"),
                    Anime("animePlanet 6"),
                    Anime("animePlanet 7"),
                )

                val testKitsuConfig = object: MetaDataProviderConfig by KitsuConfig {
                    override fun isTestContext(): Boolean = true
                }
                val kitsuTestEntries = listOf(
                    Anime("kitsu 1"),
                    Anime("kitsu 2"),
                    Anime("kitsu 3"),
                    Anime("kitsu 4"),
                    Anime("kitsu 5"),
                    Anime("kitsu 6"),
                )

                val testAnisearchConfig = object: MetaDataProviderConfig by AnisearchConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anisearchTestEntries = listOf(
                    Anime("anisearch 1"),
                    Anime("anisearch 2"),
                    Anime("anisearch 3"),
                    Anime("anisearch 4"),
                    Anime("anisearch 5"),
                )

                val testNotifyConfig = object: MetaDataProviderConfig by NotifyConfig {
                    override fun isTestContext(): Boolean = true
                }
                val notifyTestEntries = listOf(
                    Anime("notify 1"),
                    Anime("notify 2"),
                    Anime("notify 3"),
                    Anime("notify 4"),
                )

                val testAnilistConfig = object: MetaDataProviderConfig by AnilistConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anilistTestEntries = listOf(
                    Anime("anilist 1"),
                    Anime("anilist 2"),
                    Anime("anilist 3"),
                )

                val testAnidbConfig = object: MetaDataProviderConfig by AnidbConfig {
                    override fun isTestContext(): Boolean = true
                }
                val anidbTestEntries = listOf(
                    Anime("anidb 1"),
                    Anime("anidb 2"),
                )

                val testLivechartConfig = object: MetaDataProviderConfig by LivechartConfig {
                    override fun isTestContext(): Boolean = true
                }
                val livechartTestEntries = listOf(
                    Anime("livechart 1"),
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
                    override suspend fun allAnime(metaDataProviderConfig: MetaDataProviderConfig): List<Anime> = when(metaDataProviderConfig.hostname()) {
                        testMyanimelistConfig.hostname() -> myanimelistTestEntries
                        testAnimePlanetConfig.hostname() -> animePlanetTestEntries
                        testKitsuConfig.hostname() -> kitsuTestEntries
                        testAnisearchConfig.hostname() -> anisearchTestEntries
                        testNotifyConfig.hostname() -> notifyTestEntries
                        testAnilistConfig.hostname() -> anilistTestEntries
                        testAnidbConfig.hostname() -> anidbTestEntries
                        testLivechartConfig.hostname() -> livechartTestEntries
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