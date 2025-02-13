package io.github.manamiproject.modb.core.anime

import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.SUMMER
import io.github.manamiproject.modb.core.anime.AnimeStatus.FINISHED
import io.github.manamiproject.modb.core.anime.AnimeType.TV
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.Test

internal class DefaultAnimeRawToAnimeTransformerTest {

    @Nested
    inner class TransformTests {

        @Test
        fun `correctly transforms object with default values`() {
            // given
            val animeRaw = AnimeRaw("test")

            // when
            val result = DefaultAnimeRawToAnimeTransformer.instance.transform(animeRaw)

            // then
            assertThat(result.title).isEqualTo(animeRaw.title)
            assertThat(result.type).isEqualTo(animeRaw.type)
            assertThat(result.episodes).isEqualTo(animeRaw.episodes)
            assertThat(result.status).isEqualTo(animeRaw.status)
            assertThat(result.animeSeason).isEqualTo(result.animeSeason)
            assertThat(result.picture).isEqualTo(result.picture)
            assertThat(result.thumbnail).isEqualTo(result.thumbnail)
            assertThat(result.duration).isEqualTo(result.duration)
            assertThat(result.sources).isEqualTo(result.sources)
            assertThat(result.synonyms).isEqualTo(result.synonyms)
            assertThat(result.relatedAnime).isEqualTo(result.relatedAnime)
            assertThat(result.tags).isEqualTo(result.tags)
        }

        @Test
        fun `correctly transforms object`() {
            // given
            val animeRaw = AnimeRaw(
                _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                _sources = hashSetOf(
                    URI("https://livechart.me/anime/3681"),
                    URI("https://anisearch.com/anime/6826"),
                    URI("https://kitsu.io/anime/4529"),
                    URI("https://anime-planet.com/anime/clannad-another-world-kyou-chapter"),
                    URI("https://anilist.co/anime/6351"),
                    URI("https://notify.moe/anime/3L63cKimg"),
                    URI("https://myanimelist.net/anime/6351"),
                ),
                _relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/4181"),
                    URI("https://anilist.co/anime/2167"),
                    URI("https://anime-planet.com/anime/clannad"),
                    URI("https://livechart.me/anime/10537"),
                    URI("https://anime-planet.com/anime/clannad-another-world-tomoyo-chapter"),
                    URI("https://livechart.me/anime/10976"),
                    URI("https://anime-planet.com/anime/clannad-movie"),
                    URI("https://anisearch.com/anime/4199"),
                    URI("https://notify.moe/anime/F2eY5Fmig"),
                    URI("https://livechart.me/anime/3581"),
                    URI("https://anime-planet.com/anime/clannad-after-story"),
                    URI("https://livechart.me/anime/3588"),
                    URI("https://myanimelist.net/anime/2167"),
                    URI("https://livechart.me/anime/3657"),
                    URI("https://anilist.co/anime/4059"),
                    URI("https://livechart.me/anime/3822"),
                    URI("https://kitsu.io/anime/1962"),
                ),
                type = TV,
                episodes = 24,
                status = FINISHED,
                animeSeason = AnimeSeason(
                    season = SUMMER,
                    year = 2009
                ),
                picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                duration = Duration(24, MINUTES),
                _synonyms = hashSetOf(
                    "Clannad (TV)",
                    "Kuranado",
                    "Clannad TV",
                    "CLANNAD",
                    "クラナド",
                    "Кланнад",
                    "Кланад",
                    "كلاناد",
                    "Clannad 1",
                    "클라나드",
                    "خانواده",
                    "کلاناد",
                    "แคลนนาด",
                    "くらなど",
                    "ＣＬＡＮＮＡＤ -クラナド-",
                ),
                _tags = hashSetOf(
                    "baseball",
                    "based on a visual novel",
                    "basketball",
                    "amnesia",
                    "coming of age",
                    "asia",
                    "daily life",
                    "comedy",
                    "delinquents",
                    "earth",
                    "romance",
                    "ensemble cast",
                    "drama",
                )
            )

            // when
            val result = DefaultAnimeRawToAnimeTransformer.instance.transform(animeRaw)

            // then
            assertThat(result.title).isEqualTo(animeRaw.title)
            assertThat(result.type).isEqualTo(animeRaw.type)
            assertThat(result.episodes).isEqualTo(animeRaw.episodes)
            assertThat(result.status).isEqualTo(animeRaw.status)
            assertThat(result.animeSeason).isEqualTo(result.animeSeason)
            assertThat(result.picture).isEqualTo(result.picture)
            assertThat(result.thumbnail).isEqualTo(result.thumbnail)
            assertThat(result.duration).isEqualTo(result.duration)
            assertThat(result.sources).isEqualTo(result.sources)
            assertThat(result.synonyms).isEqualTo(result.synonyms)
            assertThat(result.relatedAnime).isEqualTo(result.relatedAnime)
            assertThat(result.tags).isEqualTo(result.tags)
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = DefaultAnimeRawToAnimeTransformer.instance

                // when
                val result = DefaultAnimeRawToAnimeTransformer.instance

                // then
                assertThat(result).isExactlyInstanceOf(DefaultAnimeRawToAnimeTransformer::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}