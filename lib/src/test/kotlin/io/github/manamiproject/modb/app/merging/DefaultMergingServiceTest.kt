package io.github.manamiproject.modb.app.merging

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestMergeLockAccessor
import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.merging.goldenrecords.DefaultGoldenRecordAccessor
import io.github.manamiproject.modb.app.merging.lock.MergeLockAccessor
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Status.FINISHED
import io.github.manamiproject.modb.core.models.Anime.Type.*
import io.github.manamiproject.modb.core.models.AnimeSeason
import io.github.manamiproject.modb.core.models.Duration
import io.github.manamiproject.modb.core.models.Duration.TimeUnit.*
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.Test

internal class DefaultMergingServiceTest {

    @Nested
    inner class MergeEntryTests {

        @Test
        fun `merge three equal anime with slightly different titles`() {
            runBlocking {
                // given
                val aniList11Eyes = Anime(
                    sources = hashSetOf(
                        URI("https://anilist.co/anime/6682"),
                    ),
                    _title = "11eyes",
                    type = TV,
                    episodes = 12,
                    picture = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx6682-ZptgLsCCNHjL.jpg"),
                    thumbnail = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/default.jpg"),
                    status = FINISHED,
                    duration =  Duration(25, MINUTES),
                    animeSeason = AnimeSeason(
                        year = 2009,
                    ),
                    synonyms = hashSetOf(
                        "11eyes -Tsumi to Batsu to Aganai no Shoujo-",
                        "イレブンアイズ",
                    ),
                    relatedAnime = hashSetOf(
                        URI("https://anilist.co/anime/110465"),
                        URI("https://anilist.co/anime/7739"),
                    ),
                )

                val anidb11Eyes = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/6751"),
                    ),
                    _title = "11 Eyes",
                    type = TV,
                    episodes = 12,
                    picture = URI("https://cdn.anidb.net/images/main/32901.jpg"),
                    thumbnail = URI("https://cdn.anidb.net/images/main/32901.jpg-thumb.jpg"),
                    status = FINISHED,
                    duration = Duration(25, MINUTES),
                    animeSeason = AnimeSeason(
                        year = 2009,
                    ),
                    synonyms = hashSetOf(
                        "11 akių",
                        "11 глаз",
                        "11 چشم",
                        "11eyes",
                        "11eyes -罪與罰與贖的少女-",
                        "11eyes: Tsumi to Batsu to Aganai no Shoujo",
                        "أحد عشر عيناً",
                        "イレブンアイズ",
                        "罪与罚与赎的少女",
                    ),
                )

                val mal11Eyes = Anime(
                    sources = hashSetOf(
                        URI("https://myanimelist.net/anime/6682"),
                    ),
                    _title = "11 - eyes",
                    type = TV,
                    episodes = 12,
                    picture = URI("https://cdn.myanimelist.net/images/anime/6/73520.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/6/73520t.jpg"),
                    status = FINISHED,
                    duration = Duration(25, MINUTES),
                    animeSeason = AnimeSeason(
                        year = 2009,
                    ),
                    synonyms = hashSetOf(
                        "11eyes -Tsumi to Batsu to Aganai no Shoujo-",
                        "11eyes イレブンアイズ",
                    ),
                    relatedAnime = hashSetOf(
                        URI("https://myanimelist.net/anime/20557"),
                        URI("https://myanimelist.net/anime/7739"),
                    ),
                )

                val animeList = listOf(aniList11Eyes, anidb11Eyes, mal11Eyes)

                val expectedMergedAnime = anidb11Eyes.copy().apply {
                    addSources(anidb11Eyes.sources)
                    addSynonyms(anidb11Eyes.synonyms)
                    addRelatedAnime(anidb11Eyes.relatedAnime)
                    addTags(anidb11Eyes.tags)

                }
                .mergeWith(aniList11Eyes)
                .mergeWith(mal11Eyes)

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations() = AppConfig.instance.metaDataProviderConfigurations()
                }

                val testMergeLockAccessor = object: MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun hasMergeLock(uris: Set<URI>): Boolean = false
                }

                val defaultMergingService = DefaultMergingService(
                    appConfig = testAppConfig,
                    mergeLockAccessor = testMergeLockAccessor,
                    goldenRecordAccessor = DefaultGoldenRecordAccessor(),
                )

                // when
                val result = defaultMergingService.merge(animeList)

                // then
                assertThat(result).containsExactlyInAnyOrder(expectedMergedAnime)
            }
        }

        @Test
        fun `don't merge two totally different anime`() {
            runBlocking {
                // given
                val anidb = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/1"),
                    ),
                    _title = "Seikai no Monshou",
                    type = TV,
                    episodes = 13,
                    picture = URI("https://cdn.anidb.net/images/main/224618.jpg"),
                    thumbnail = URI("https://cdn.anidb.net/images/main/224618.jpg-thumb.jpg"),
                    duration = Duration(25, MINUTES),
                    animeSeason = AnimeSeason(
                        year = 1999,
                    ),
                    synonyms = hashSetOf(
                        "CotS",
                        "Crest of the Stars",
                        "Hvězdný erb",
                        "SnM",
                        "星界の紋章",
                        "星界之纹章",
                    ),
                    relatedAnime = hashSetOf(
                        URI("https://anidb.net/anime/1623"),
                        URI("https://anidb.net/anime/4"),
                        URI("https://anidb.net/anime/6"),
                    ),
                )

                val mal = Anime(
                    sources = hashSetOf(
                        URI("https://myanimelist.net/anime/1"),
                    ),
                    _title = "Cowboy Bebop",
                    type = TV,
                    episodes = 26,
                    picture = URI("https://cdn.myanimelist.net/images/anime/4/19644.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/4/19644t.jpg"),
                    duration = Duration(24, MINUTES),
                    animeSeason = AnimeSeason(
                        year = 1998,
                    ),
                    synonyms = hashSetOf(
                        "カウボーイビバップ",
                    ),
                    relatedAnime = hashSetOf(
                        URI("https://myanimelist.net/anime/17205"),
                        URI("https://myanimelist.net/anime/4037"),
                        URI("https://myanimelist.net/anime/5"),
                    ),
                )

                val animeList = listOf(anidb, mal)

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations() = AppConfig.instance.metaDataProviderConfigurations()
                }

                val testMergeLockService = object: MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun hasMergeLock(uris: Set<URI>): Boolean = false
                }

                val defaultMergingService = DefaultMergingService(
                    appConfig = testAppConfig,
                    mergeLockAccessor = testMergeLockService,
                    goldenRecordAccessor = DefaultGoldenRecordAccessor(),
                )

                // when
                val result = defaultMergingService.merge(animeList)

                // then
                assertThat(result).containsExactlyInAnyOrder(anidb, mal)
            }
        }

        @Test
        fun `merge two totally different anime, because they are part of a merge lock`() {
            runBlocking {
                // given
                val aniList11Eyes = Anime(
                    sources = hashSetOf(
                        URI("https://anilist.co/anime/6682"),
                    ),
                    _title = "11eyes",
                    type = TV,
                    episodes = 12,
                    picture = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx6682-ZptgLsCCNHjL.jpg"),
                    thumbnail = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/default.jpg"),
                    status = FINISHED,
                    synonyms = hashSetOf(
                        "11eyes -Tsumi to Batsu to Aganai no Shoujo-",
                        "イレブンアイズ",
                    ),
                    relatedAnime = hashSetOf(
                        URI("https://anilist.co/anime/110465"),
                        URI("https://anilist.co/anime/7739"),
                    ),
                )

                val bungakuShoujo = Anime(
                    sources = hashSetOf(
                        URI("https://kitsu.app/anime/4550"),
                        URI("https://myanimelist.net/anime/6408"),
                        URI("https://notify.moe/anime/UUie5KimR")
                    ),
                    _title = "\"Bungaku Shoujo\" Movie",
                    type = MOVIE,
                    episodes = 1,
                    status = FINISHED,
                    picture = URI("https://cdn.myanimelist.net/images/anime/8/81162.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/8/81162t.jpg"),
                    synonyms = hashSetOf(
                        "Book Girl",
                        "Literature Girl",
                        "げきじょうばんぶんがくしょうじょ",
                        "劇場版“文学少女”",
                    ),
                    relatedAnime = hashSetOf(
                        URI("https://kitsu.app/anime/5078"),
                        URI("https://kitsu.app/anime/5352"),
                        URI("https://myanimelist.net/anime/7669"),
                        URI("https://myanimelist.net/anime/8481"),
                        URI("https://notify.moe/anime/UBTmtKiiR"),
                        URI("https://notify.moe/anime/oTdkpKiig"),
                    ),
                )

                val expectedEntry = aniList11Eyes.copy().apply {
                    addSources(aniList11Eyes.sources)
                    addSynonyms(aniList11Eyes.synonyms)
                    addRelatedAnime(aniList11Eyes.relatedAnime)
                    addTags(aniList11Eyes.tags)
                }.mergeWith(bungakuShoujo)

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = AppConfig.instance.metaDataProviderConfigurations()
                }

                val testMergeLockService = object: MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun hasMergeLock(uris: Set<URI>): Boolean = true
                    override suspend fun getMergeLock(uri: URI): Set<URI> = aniList11Eyes.sources.union(bungakuShoujo.sources)
                }

                val animeList = listOf(aniList11Eyes, bungakuShoujo)

                val defaultMergingService = DefaultMergingService(
                    appConfig = testAppConfig,
                    mergeLockAccessor = testMergeLockService,
                    goldenRecordAccessor = DefaultGoldenRecordAccessor(),
                )

                // when
                val result = defaultMergingService.merge(animeList)

                // then
                assertThat(result).containsExactly(expectedEntry)
            }
        }

        @Test
        fun `multiple golden records`() {
            runBlocking {
                // given
                val entry1 = Anime(
                    sources = hashSetOf(
                        URI("https://myanimelist.net/anime/59110"),
                    ),
                    _title = "daydream",
                    episodes = 1,
                    animeSeason = AnimeSeason(
                        season = AnimeSeason.Season.WINTER,
                        year = 2024,
                    ),
                    status = FINISHED,
                    type = SPECIAL,
                    duration = Duration(
                        value = 120,
                        unit = SECONDS,
                    ),
                    synonyms = hashSetOf(
                        "白昼夢",
                    ),
                )

                val entry2 = Anime(
                    sources = hashSetOf(
                        URI("https://myanimelist.net/anime/58364"),
                    ),
                    _title = "daydream",
                    episodes = 1,
                    type = SPECIAL,
                    status = FINISHED,
                    animeSeason = AnimeSeason(
                        season = AnimeSeason.Season.WINTER,
                        year = 2024,
                    ),
                    duration = Duration(
                        value = 120,
                        unit = SECONDS,
                    ),
                    synonyms = hashSetOf(
                        "崩壊学園9周年記念楽曲「白昼夢」",
                        "houkai gakuen 9th anniversary song daydream",
                        "houkai gakuen 9th anniversary song daydream",
                        "崩壊学園9周年記念楽曲「白昼夢」",
                    ),
                )

                val entry3 = Anime(
                    sources = hashSetOf(
                        URI("https://anisearch.com/anime/19120"),
                    ),
                    _title = "Daydream",
                    episodes = 1,
                    type = ONA,
                    status = FINISHED,
                    animeSeason = AnimeSeason(
                        season = AnimeSeason.Season.SUMMER,
                        year = 2001,
                    ),
                    duration = Duration(
                        value = 240,
                        unit = SECONDS,
                    ),
                    synonyms = hashSetOf(
                        "春猿火 - daydream",
                    ),
                )

                val expectedResult = listOf(
                    entry1.copy().mergeWith(entry2),
                    entry3,
                )

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = AppConfig.instance.metaDataProviderConfigurations()
                }

                val testMergeLockService = object: MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun hasMergeLock(uris: Set<URI>): Boolean = false
                }

                val animeList = listOf(entry1, entry3, entry2)

                val defaultMergingService = DefaultMergingService(
                    appConfig = testAppConfig,
                    mergeLockAccessor = testMergeLockService,
                    goldenRecordAccessor = DefaultGoldenRecordAccessor(),
                )

                // when
                val result = defaultMergingService.merge(animeList)

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    *expectedResult.toTypedArray(),
                )
            }
        }

        @Test
        fun `found multiple potential golden records, but none returns sufficient probability and therefore the entries set for retry`() {
            runBlocking {
                // given
                val malCowboyBebop = Anime(
                    sources = hashSetOf(
                        URI("https://myanimelist.net/anime/1"),
                    ),
                    _title = "Cowboy Bebop",
                    type = TV,
                    episodes = 26,
                    picture = URI("https://cdn.myanimelist.net/images/anime/4/19644.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/4/19644t.jpg"),
                    duration = Duration(24, MINUTES),
                    animeSeason = AnimeSeason(
                        year = 1998,
                    ),
                    synonyms = hashSetOf(
                        "カウボーイビバップ",
                    ),
                    relatedAnime = hashSetOf(
                        URI("https://myanimelist.net/anime/17205"),
                        URI("https://myanimelist.net/anime/4037"),
                        URI("https://myanimelist.net/anime/5"),
                    ),
                )

                val malDaydream = Anime(
                    sources = hashSetOf(
                        URI("https://myanimelist.net/anime/59110"),
                    ),
                    _title = "daydream",
                    episodes = 1,
                    animeSeason = AnimeSeason(
                        season = AnimeSeason.Season.WINTER,
                        year = 2024,
                    ),
                    status = FINISHED,
                    type = SPECIAL,
                    duration = Duration(
                        value = 120,
                        unit = SECONDS,
                    ),
                    synonyms = hashSetOf(
                        "白昼夢",
                    ),
                )

                val aniSearchDaydream = Anime(
                    sources = hashSetOf(
                        URI("https://anisearch.com/anime/19120"),
                    ),
                    _title = "Daydream",
                    episodes = 4,
                    type = ONA,
                    status = FINISHED,
                    animeSeason = AnimeSeason(
                        season = AnimeSeason.Season.SUMMER,
                        year = 2001,
                    ),
                    duration = Duration(
                        value = 240,
                        unit = SECONDS,
                    ),
                    synonyms = hashSetOf(
                        "春猿火 - daydream",
                    ),
                )

                val anilDaydream = Anime(
                    sources = hashSetOf(
                        URI("https://anilist.com/anime/10001"),
                    ),
                    _title = "Daydream",
                    episodes = 1,
                    type = MOVIE,
                    status = Anime.Status.UPCOMING,
                    animeSeason = AnimeSeason(
                        season = AnimeSeason.Season.SUMMER,
                        year = 2006,
                    ),
                    duration = Duration(
                        value = 1,
                        unit = HOURS,
                    ),
                )

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = AppConfig.instance.metaDataProviderConfigurations()
                }

                val testMergeLockService = object: MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun hasMergeLock(uris: Set<URI>): Boolean = false
                }

                val animeList = listOf(malCowboyBebop, malDaydream, aniSearchDaydream, anilDaydream)

                val defaultMergingService = DefaultMergingService(
                    appConfig = testAppConfig,
                    mergeLockAccessor = testMergeLockService,
                    goldenRecordAccessor = DefaultGoldenRecordAccessor(),
                )

                // when
                val result = defaultMergingService.merge(animeList)

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    malCowboyBebop,
                    malDaydream,
                    aniSearchDaydream,
                    anilDaydream,
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
                val previous = DefaultMergingService.instance

                // when
                val result = DefaultMergingService.instance

                // then
                assertThat(result).isExactlyInstanceOf(DefaultMergingService::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}