package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.anime.Duration
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.*
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.test.loadTestResource
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.Test
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN as UNKNOWN_STATUS
import io.github.manamiproject.modb.core.anime.AnimeType.UNKNOWN as UNKNOWN_TYPE

internal class AnilistAnimeConverterTest {

    @Nested
    inner class TitleTests {

        @Test
        fun `title containing special chars`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/title/special_chars.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.title).isEqualTo("Tobidasu PriPara: Mi~nna de Mezase! Idol☆Grand Prix")
            }
        }
    }

    @Nested
    inner class TypeTests {

        @Test
        fun `type is tv`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/type/tv.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(TV)
            }
        }

        @Test
        fun `type is tv_short and is mapped to tv`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/type/tv_short.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(TV)
            }
        }

        @Test
        fun `type is special`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/type/special.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }

        @Test
        fun `type is ova`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/type/ova.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(OVA)
            }
        }

        @Test
        fun `type is ona`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/type/ona.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(ONA)
            }
        }

        @Test
        fun `type is movie`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/type/movie.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(MOVIE)
            }
        }

        @Test
        fun `type is music is mapped to special`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/type/music.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }

        @Test
        fun `type is null and is mapped to UNKNOWN`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/type/null.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(UNKNOWN_TYPE)
            }
        }
    }

    @Nested
    inner class EpisodesTests {

        @Test
        fun `fixed number of episodes`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/episodes/39.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.episodes).isEqualTo(39)
            }
        }

        @Test
        fun `neither episodes nor nextairingepisode is set`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/episodes/neither_episodes_nor_nextairingepisode_is_set.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.episodes).isEqualTo(0)
            }
        }

        @Test
        fun `ongoing series for which the value has to be taken from nextairingepisode`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/episodes/ongoing.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.episodes).isEqualTo(1082)
            }
        }
    }

    @Nested
    inner class PictureAndThumbnailTests {

        @Test
        fun `picture is available, but anilist never provides a thumbnail so the thumbnail is the same as the picture`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/picture_and_thumbnail/picture_available.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.picture).isEqualTo(URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx2167-ubU2875AFRTH.jpg"))
                assertThat(result.thumbnail).isEqualTo(URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx2167-ubU2875AFRTH.jpg"))
            }
        }

        @Test
        fun `picture is unavailable`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/picture_and_thumbnail/picture_unavailable.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.picture).isEqualTo(URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/default.jpg"))
                assertThat(result.thumbnail).isEqualTo(URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/default.jpg"))
            }
        }
    }

    @Nested
    inner class SynonymsTests {

        @Test
        fun `synonyms taken from titles and synonyms, ignoring null`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/synonyms/synonyms_from_titles_and_synonyms.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "Prism Paradise",
                    "Tobidasu PriPara: Minna de Mezase! Idol\u2606Grand Prix",
                    "Tobidasu PuriPara: Mi~nna de Mezase! Idol\u2606Grand Prix",
                    "とびだすプリパラ　み～んなでめざせ！アイドル☆グランプリ",
                )
            }
        }
    }

    @Nested
    inner class SourcesTests {

        @Test
        fun `extract correct id and build anime link correctly`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/sources/15689.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.sources.first()).isEqualTo(URI("https://anilist.co/anime/15689"))
            }
        }
    }

    @Nested
    inner class RelationsTests {

        @Test
        fun `no adaption, no relations`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/related_anime/no_adaption_no_relations.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.relatedAnime).isEmpty()
            }
        }

        @Test
        fun `no adaption, multiple relations`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/related_anime/no_adaption_multiple_relations.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.relatedAnime).containsExactlyInAnyOrder(
                    URI("https://anilist.co/anime/107298"),
                    URI("https://anilist.co/anime/116147"),
                    URI("https://anilist.co/anime/97857"),
                )
            }
        }

        @Test
        fun `one adaption, one relation`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/related_anime/has_one_adaption_and_one_relation.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.relatedAnime).containsExactly(
                    URI("https://anilist.co/anime/2337")
                )
            }
        }

        @Test
        fun `has adaption, multiple relations`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/related_anime/has_adaption_and_multiple_relations.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.relatedAnime).containsExactlyInAnyOrder(
                    URI("https://anilist.co/anime/100148"),
                    URI("https://anilist.co/anime/1081"),
                    URI("https://anilist.co/anime/1301"),
                    URI("https://anilist.co/anime/1302"),
                    URI("https://anilist.co/anime/1491"),
                    URI("https://anilist.co/anime/1645"),
                    URI("https://anilist.co/anime/1676"),
                    URI("https://anilist.co/anime/1706"),
                    URI("https://anilist.co/anime/17269"),
                    URI("https://anilist.co/anime/2202"),
                    URI("https://anilist.co/anime/2203"),
                    URI("https://anilist.co/anime/2470"),
                )
            }
        }

        @Test
        fun `has adaption, no relations`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/related_anime/has_adaption_but_no_relation.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.relatedAnime).isEmpty()
            }
        }
    }

    @Nested
    inner class StatusTests {

        @Test
        fun `'FINISHED' is mapped to 'FINISHED'`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/status/finished.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(FINISHED)
            }
        }

        @Test
        fun `'RELEASING' is mapped to 'ONGOING'`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/status/releasing.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(ONGOING)
            }
        }

        @Test
        fun `'NOT_YET_RELEASED' is mapped to 'UPCOMING'`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/status/not_yet_released.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(UPCOMING)
            }
        }

        @Test
        fun `'CANCELLED' is mapped to 'UNKNOWN'`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/status/cancelled.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(UNKNOWN_STATUS)
            }
        }

        @Test
        fun `null is mapped to 'UNKNOWN'`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/status/null.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(UNKNOWN_STATUS)
            }
        }
    }

    @Nested
    inner class TagsTests {

        @Test
        fun `put names of genres and tags as distinct list into the anime's tags`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/tags/tags.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.tags).containsExactlyInAnyOrder(
                    "action",
                    "adventure",
                    "amnesia",
                    "anti-hero",
                    "crime",
                    "cult",
                    "cyberpunk",
                    "cyborg",
                    "drama",
                    "drugs",
                    "ensemble cast",
                    "episodic",
                    "gambling",
                    "gender bending",
                    "guns",
                    "male protagonist",
                    "martial arts",
                    "noir",
                    "philosophy",
                    "police",
                    "primarily adult cast",
                    "sci-fi",
                    "space",
                    "tanned skin",
                    "terrorism",
                    "tomboy",
                    "tragedy",
                    "travel",
                    "yakuza",
                )
            }
        }
    }

    @Nested
    inner class DurationTests {

        @Test
        fun `duration is not set and therefore 0`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/duration/null.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.duration).isEqualTo(Duration(0, SECONDS))
            }
        }

        @Test
        fun `duration is set to 0`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/duration/0.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.duration).isEqualTo(Duration(0, SECONDS))
            }
        }

        @Test
        fun `anilist only uses minutes for duration - so this entry although 15 seconds long is set to 1 min`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/duration/min_duration.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.duration).isEqualTo(Duration(1, MINUTES))
            }
        }

        @Test
        fun `duration of 24 minutes`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/duration/24.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.duration).isEqualTo(Duration(24, MINUTES))
            }
        }

        @Test
        fun `duration of 2 hours`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/duration/120.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.duration).isEqualTo(Duration(2, HOURS))
            }
        }
    }

    @Nested
    inner class AnimeSeasonTests {

        @Nested
        inner class SeasonTests {

            @Test
            fun `season is 'undefined'`() {
                runTest {
                    // given
                    val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnilistConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                    }
                    val converter = AnilistAnimeConverter(testAnilistConfig)

                    val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/anime_season/season_is_null_and_start_date_is_null.json")

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(UNDEFINED)
                }
            }

            @Test
            fun `season is 'spring'`() {
                runTest {
                    // given
                    val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnilistConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                    }
                    val converter = AnilistAnimeConverter(testAnilistConfig)

                    val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/anime_season/spring.json")

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(SPRING)
                }
            }

            @Test
            fun `season is 'summer'`() {
                runTest {
                    // given
                    val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnilistConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                    }
                    val converter = AnilistAnimeConverter(testAnilistConfig)

                    val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/anime_season/summer.json")

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(SUMMER)
                }
            }

            @Test
            fun `season is 'fall'`() {
                runTest {
                    // given
                    val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnilistConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                    }
                    val converter = AnilistAnimeConverter(testAnilistConfig)

                    val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/anime_season/fall.json")

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(FALL)
                }
            }

            @Test
            fun `season is 'wimter'`() {
                runTest {
                    // given
                    val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnilistConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                    }
                    val converter = AnilistAnimeConverter(testAnilistConfig)

                    val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/anime_season/winter.json")

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(WINTER)
                }
            }
        }

        @Nested
        inner class YearOfPremiereTests {

            @Test
            fun `seasonYear is set`() {
                runTest {
                    // given
                    val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnilistConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                    }
                    val converter = AnilistAnimeConverter(testAnilistConfig)

                    val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/anime_season/seasonyear_set.json")

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2023)
                }
            }

            @Test
            fun `year is not set and default is 0`() {
                runTest {
                    // given
                    val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnilistConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                    }
                    val converter = AnilistAnimeConverter(testAnilistConfig)

                    val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/anime_season/season_is_null_and_start_date_is_null.json")

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(0)
                }
            }

            @Test
            fun `year is 2006 - seasonYear is null but start date is set`() {
                runTest {
                    // given
                    val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = AnilistConfig.hostname()
                        override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                    }
                    val converter = AnilistAnimeConverter(testAnilistConfig)

                    val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/anime_season/season_is_null_and_start_date_is_2006.json")

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2006)
                }
            }
        }
    }

    @Nested
    inner class ScoresTests {

        @Test
        fun `successfully load score`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                // when
                val result = converter.convert(loadTestResource("AnilistAnimeConverterTest/scores/score.json"))

                // then
                assertThat(result.scores).hasSize(1)
                assertThat(result.scores.first().scaledValue()).isEqualTo(9.090909090909092)
            }
        }

        @Test
        fun `returns NoMetaDataProviderScore`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                // when
                val result = converter.convert(loadTestResource("AnilistAnimeConverterTest/scores/no-score.json"))

                // then
                assertThat(result.scores).isEmpty()
            }
        }
    }

    @Nested
    inner class StudiosTests {

        @Test
        fun `multiple studios`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/studios/multiple_studios.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    "satelight",
                    "hornets",
                )
            }
        }

        @Test
        fun `no studios`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/studios/no_studios.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.studios).isEmpty()
            }
        }
    }

    @Nested
    inner class ProducersTests {

        @Test
        fun `multiple producers`() {
            runTest {
                // given
                val testAnilistConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent =
                    loadTestResource<String>("AnilistAnimeConverterTest/producers/multiple_producers.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    "mixer",
                    "north stars pictures",
                    "jy animation",
                    "crunchyroll",
                    "ca-cygames anime fund",
                    "movic",
                )
            }
        }

        @Test
        fun `no producers`() {
            runTest {
                // given
                val testAnilistConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = AnilistConfig.hostname()
                    override fun buildAnimeLink(id: AnimeId): URI = AnilistConfig.buildAnimeLink(id)
                }
                val converter = AnilistAnimeConverter(testAnilistConfig)

                val testFileContent = loadTestResource<String>("AnilistAnimeConverterTest/producers/no_producers.json")

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.producers).isEmpty()
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = AnilistAnimeConverter.instance

            // when
            val result = AnilistAnimeConverter.instance

            // then
            assertThat(result).isExactlyInstanceOf(AnilistAnimeConverter::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}