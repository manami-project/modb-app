package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Status.*
import io.github.manamiproject.modb.core.models.Anime.Type.*
import io.github.manamiproject.modb.core.models.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.models.Duration
import io.github.manamiproject.modb.core.models.Duration.TimeUnit.*
import io.github.manamiproject.modb.test.loadTestResource
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.Test

internal class AnilistAnimeConverterTest {

    @Nested
    inner class TitleTests {

        @Test
        fun `title containing special chars`() {
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/title/special_chars.json")

                val converter = AnilistAnimeConverter()

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
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/type/tv.json")

                val converter = AnilistAnimeConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(TV)
            }
        }

        @Test
        fun `type is tv_short and is mapped to tv`() {
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/type/tv_short.json")

                val converter = AnilistAnimeConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(TV)
            }
        }

        @Test
        fun `type is special`() {
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/type/special.json")

                val converter = AnilistAnimeConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }

        @Test
        fun `type is ova`() {
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/type/ova.json")

                val converter = AnilistAnimeConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(OVA)
            }
        }

        @Test
        fun `type is ona`() {
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/type/ona.json")

                val converter = AnilistAnimeConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(ONA)
            }
        }

        @Test
        fun `type is movie`() {
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/type/movie.json")

                val converter = AnilistAnimeConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(MOVIE)
            }
        }

        @Test
        fun `type is music is mapped to special`() {
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/type/music.json")

                val converter = AnilistAnimeConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(SPECIAL)
            }
        }

        @Test
        fun `type is null and is mapped to UNKNOWN`() {
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/type/null.json")

                val converter = AnilistAnimeConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.type).isEqualTo(Anime.Type.UNKNOWN)
            }
        }
    }

    @Nested
    inner class EpisodesTests {

        @Test
        fun `fixed number of episodes`() {
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/episodes/39.json")

                val converter = AnilistAnimeConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.episodes).isEqualTo(39)
            }
        }

        @Test
        fun `neither episodes nor nextairingepisode is set`() {
            runBlocking {
                // given
                val testFileContent =
                    loadTestResource<String>("file_converter_tests/episodes/neither_episodes_nor_nextairingepisode_is_set.json")

                val converter = AnilistAnimeConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.episodes).isEqualTo(0)
            }
        }

        @Test
        fun `ongoing series for which the value has to be taken from nextairingepisode`() {
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/episodes/ongoing.json")

                val converter = AnilistAnimeConverter()

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
            runBlocking {
                // given
                val testFileContent =
                    loadTestResource<String>("file_converter_tests/picture_and_thumbnail/picture_available.json")

                val converter = AnilistAnimeConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.picture).isEqualTo(URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx2167-ubU2875AFRTH.jpg"))
                assertThat(result.thumbnail).isEqualTo(URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx2167-ubU2875AFRTH.jpg"))
            }
        }

        @Test
        fun `picture is unavailable`() {
            runBlocking {
                // given
                val testFileContent =
                    loadTestResource<String>("file_converter_tests/picture_and_thumbnail/picture_unavailable.json")

                val converter = AnilistAnimeConverter()

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
            runBlocking {
                // given
                val testFileContent =
                    loadTestResource<String>("file_converter_tests/synonyms/synonyms_from_titles_and_synonyms.json")

                val converter = AnilistAnimeConverter()

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
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/sources/15689.json")

                val converter = AnilistAnimeConverter()

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
            runBlocking {
                // given
                val testFileContent =
                    loadTestResource<String>("file_converter_tests/related_anime/no_adaption_no_relations.json")

                val converter = AnilistAnimeConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.relatedAnime).isEmpty()
            }
        }

        @Test
        fun `no adaption, multiple relations`() {
            runBlocking {
                // given
                val testFileContent =
                    loadTestResource<String>("file_converter_tests/related_anime/no_adaption_multiple_relations.json")

                val converter = AnilistAnimeConverter()

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
            runBlocking {
                // given
                val testFileContent =
                    loadTestResource<String>("file_converter_tests/related_anime/has_one_adaption_and_one_relation.json")

                val converter = AnilistAnimeConverter()

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
            runBlocking {
                // given
                val testFileContent =
                    loadTestResource<String>("file_converter_tests/related_anime/has_adaption_and_multiple_relations.json")

                val converter = AnilistAnimeConverter()

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
            runBlocking {
                // given
                val testFileContent =
                    loadTestResource<String>("file_converter_tests/related_anime/has_adaption_but_no_relation.json")

                val converter = AnilistAnimeConverter()

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
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/status/finished.json")

                val converter = AnilistAnimeConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(FINISHED)
            }
        }

        @Test
        fun `'RELEASING' is mapped to 'ONGOING'`() {
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/status/releasing.json")

                val converter = AnilistAnimeConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(ONGOING)
            }
        }

        @Test
        fun `'NOT_YET_RELEASED' is mapped to 'UPCOMING'`() {
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/status/not_yet_released.json")

                val converter = AnilistAnimeConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(UPCOMING)
            }
        }

        @Test
        fun `'CANCELLED' is mapped to 'UNKNOWN'`() {
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/status/cancelled.json")

                val converter = AnilistAnimeConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(Anime.Status.UNKNOWN)
            }
        }

        @Test
        fun `null is mapped to 'UNKNOWN'`() {
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/status/null.json")

                val converter = AnilistAnimeConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.status).isEqualTo(Anime.Status.UNKNOWN)
            }
        }
    }

    @Nested
    inner class TagsTests {

        @Test
        fun `put names of genres and tags as distinct list into the anime's tags`() {
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/tags/tags.json")

                val converter = AnilistAnimeConverter()

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
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/duration/null.json")

                val converter = AnilistAnimeConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.duration).isEqualTo(Duration(0, SECONDS))
            }
        }

        @Test
        fun `duration is set to 0`() {
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/duration/0.json")

                val converter = AnilistAnimeConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.duration).isEqualTo(Duration(0, SECONDS))
            }
        }

        @Test
        fun `anilist only uses minutes for duration - so this entry although 15 seconds long is set to 1 min`() {
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/duration/min_duration.json")

                val converter = AnilistAnimeConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.duration).isEqualTo(Duration(1, MINUTES))
            }
        }

        @Test
        fun `duration of 24 minutes`() {
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/duration/24.json")

                val converter = AnilistAnimeConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.duration).isEqualTo(Duration(24, MINUTES))
            }
        }

        @Test
        fun `duration of 2 hours`() {
            runBlocking {
                // given
                val testFileContent = loadTestResource<String>("file_converter_tests/duration/120.json")

                val converter = AnilistAnimeConverter()

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
                runBlocking {
                    // given
                    val testFileContent =
                        loadTestResource<String>("file_converter_tests/anime_season/season_is_null_and_start_date_is_null.json")

                    val converter = AnilistAnimeConverter()

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(UNDEFINED)
                }
            }

            @Test
            fun `season is 'spring'`() {
                runBlocking {
                    // given
                    val testFileContent = loadTestResource<String>("file_converter_tests/anime_season/spring.json")

                    val converter = AnilistAnimeConverter()

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(SPRING)
                }
            }

            @Test
            fun `season is 'summer'`() {
                runBlocking {
                    // given
                    val testFileContent = loadTestResource<String>("file_converter_tests/anime_season/summer.json")

                    val converter = AnilistAnimeConverter()

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(SUMMER)
                }
            }

            @Test
            fun `season is 'fall'`() {
                runBlocking {
                    // given
                    val testFileContent = loadTestResource<String>("file_converter_tests/anime_season/fall.json")

                    val converter = AnilistAnimeConverter()

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.season).isEqualTo(FALL)
                }
            }

            @Test
            fun `season is 'wimter'`() {
                runBlocking {
                    // given
                    val testFileContent = loadTestResource<String>("file_converter_tests/anime_season/winter.json")

                    val converter = AnilistAnimeConverter()

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
                runBlocking {
                    // given
                    val testFileContent =
                        loadTestResource<String>("file_converter_tests/anime_season/seasonyear_set.json")

                    val converter = AnilistAnimeConverter()

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2023)
                }
            }

            @Test
            fun `year is not set and default is 0`() {
                runBlocking {
                    // given
                    val testFileContent =
                        loadTestResource<String>("file_converter_tests/anime_season/season_is_null_and_start_date_is_null.json")

                    val converter = AnilistAnimeConverter()

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(0)
                }
            }

            @Test
            fun `year is 2006 - seasonYear is null but start date is set`() {
                runBlocking {
                    // given
                    val testFileContent = loadTestResource<String>("file_converter_tests/anime_season/season_is_null_and_start_date_is_2006.json")

                    val converter = AnilistAnimeConverter()

                    // when
                    val result = converter.convert(testFileContent)

                    // then
                    assertThat(result.animeSeason.year).isEqualTo(2006)
                }
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
            assertThat(result===previous).isTrue()
        }
    }
}