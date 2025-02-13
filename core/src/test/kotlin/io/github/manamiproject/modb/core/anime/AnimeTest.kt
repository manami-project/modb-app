package io.github.manamiproject.modb.core.anime

import io.github.manamiproject.modb.core.anime.AnimeRaw.Companion.NO_PICTURE
import io.github.manamiproject.modb.core.anime.AnimeRaw.Companion.NO_PICTURE_THUMBNAIL
import io.github.manamiproject.modb.core.anime.AnimeSeason.Companion.UNKNOWN_YEAR
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.SUMMER
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.UNDEFINED
import io.github.manamiproject.modb.core.anime.AnimeStatus.FINISHED
import io.github.manamiproject.modb.core.anime.AnimeType.SPECIAL
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI
import kotlin.test.Test
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN as UNKNOWN_STATUS
import io.github.manamiproject.modb.core.anime.AnimeType.UNKNOWN as UNKNOWN_TYPE
import io.github.manamiproject.modb.core.anime.Duration.Companion.UNKNOWN as UNKNOWN_DURATION

internal class AnimeTest {

    @Nested
    inner class ConstructorTests {

        @ParameterizedTest
        @ValueSource(strings = [
            "",
            "   ",
            "\u00A0",
            "\u202F",
            "\u200A",
            "\u205F",
            "\u2000",
            "\u2001",
            "\u2002",
            "\u2003",
            "\u2004",
            "\u2005",
            "\u2006",
            "\u2007",
            "\u2008",
            "\u2009",
            "\uFEFF",
            "\u180E",
            "\u2060",
            "\u200D",
            "\u0090",
            "\u200C",
            "\u200B",
            "\u00AD",
            "\u000C",
            "\u2028",
            "\r",
            "\n",
            "\t",
        ])
        fun `throws exception if title is empty or blank or zero-width non-joiner`(value: String) {
            // when
            val result = assertThrows<IllegalArgumentException> {
                Anime(value)
            }

            // then
            assertThat(result).hasMessage("Title cannot be blank.")
        }

        @Test
        fun `default values`() {
            // when
            val result = Anime("test")

            // then
            assertThat(result.type).isEqualTo(UNKNOWN_TYPE)
            assertThat(result.episodes).isZero()
            assertThat(result.status).isEqualTo(UNKNOWN_STATUS)
            assertThat(result.animeSeason.season).isEqualTo(UNDEFINED)
            assertThat(result.animeSeason.year).isEqualTo(UNKNOWN_YEAR)
            assertThat(result.picture).isEqualTo(NO_PICTURE)
            assertThat(result.thumbnail).isEqualTo(NO_PICTURE_THUMBNAIL)
            assertThat(result.duration).isEqualTo(UNKNOWN_DURATION)
            assertThat(result.sources).isEmpty()
            assertThat(result.synonyms).isEmpty()
            assertThat(result.relatedAnime).isEmpty()
            assertThat(result.tags).isEmpty()
        }
    }

    @Nested
    inner class ToStringTests {

        @Test
        fun `create formatted string listing all properties`() {
            // given
            val anime = Anime(
                title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                sources = hashSetOf(
                    URI("https://myanimelist.net/anime/6351"),
                ),
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/2167"),
                ),
                type = SPECIAL,
                episodes = 1,
                status = FINISHED,
                animeSeason = AnimeSeason(
                    season = SUMMER,
                    year = 2009
                ),
                picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                duration = Duration(2, MINUTES),
                synonyms = hashSetOf(
                    "Clannad ~After Story~: Another World, Kyou Chapter",
                    "Clannad: After Story OVA",
                    "クラナド　アフターストーリー　もうひとつの世界　杏編",
                ),
                tags = hashSetOf(
                    "comedy",
                    "drama",
                    "romance",
                    "school",
                    "slice of life",
                    "supernatural",
                ),
            )

            // when
            val result = anime.toString()

            // then
            assertThat(result).isEqualTo(
                """
                    Anime(
                      sources = [https://myanimelist.net/anime/6351]
                      title = Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen
                      type = SPECIAL
                      episodes = 1
                      status = FINISHED
                      animeSeason = AnimeSeason(season=SUMMER, year=2009)
                      picture = https://cdn.myanimelist.net/images/anime/10/19621.jpg
                      thumbnail = https://cdn.myanimelist.net/images/anime/10/19621t.jpg
                      duration = 120 seconds
                      synonyms = [Clannad ~After Story~: Another World, Kyou Chapter, Clannad: After Story OVA, クラナド　アフターストーリー　もうひとつの世界　杏編]
                      relatedAnime = [https://myanimelist.net/anime/2167]
                      tags = [comedy, drama, romance, school, slice of life, supernatural]
                    )
                """.trimIndent()
            )
        }
    }
}