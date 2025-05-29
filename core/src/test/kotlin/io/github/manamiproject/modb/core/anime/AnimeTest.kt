package io.github.manamiproject.modb.core.anime

import io.github.manamiproject.modb.core.TestAnimeObjects
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE_THUMBNAIL
import io.github.manamiproject.modb.core.anime.AnimeSeason.Companion.UNKNOWN_YEAR
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.UNDEFINED
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN as UNKNOWN_STATUS
import io.github.manamiproject.modb.core.anime.AnimeType.UNKNOWN as UNKNOWN_TYPE
import io.github.manamiproject.modb.core.anime.Duration.Companion.UNKNOWN as UNKNOWN_DURATION

internal class AnimeTest {

    @Nested
    inner class ConstructorTests {

        @ParameterizedTest
        @ValueSource(
            strings = [
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
            ]
        )
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
            assertThat(result.score).isEqualTo(NoScore)
            assertThat(result.sources).isEmpty()
            assertThat(result.synonyms).isEmpty()
            assertThat(result.studios).isEmpty()
            assertThat(result.producers).isEmpty()
            assertThat(result.relatedAnime).isEmpty()
            assertThat(result.tags).isEmpty()
        }
    }

    @Nested
    inner class ToStringTests {

        @Test
        fun `create formatted string listing all properties`() {
            // when
            val result = TestAnimeObjects.AllPropertiesSet.obj.toString()

            // then
            assertThat(result).isEqualTo(
                """
                    Anime(
                      sources      = [https://myanimelist.net/anime/58755]
                      title        = 5-toubun no Hanayome*
                      type         = SPECIAL
                      episodes     = 2
                      status       = FINISHED
                      animeSeason  = AnimeSeason(season=FALL, year=2024)
                      picture      = https://cdn.myanimelist.net/images/anime/1915/145336.jpg
                      thumbnail    = https://cdn.myanimelist.net/images/anime/1915/145336t.jpg
                      duration     = 1440 seconds
                      score        = ScoreValue(arithmeticGeometricMean=7.44, arithmeticMean=7.44, median=7.44)
                      synonyms     = [The Quintessential Quintuplets*, 五等分の花嫁*]
                      studios      = [bibury animation studios]
                      producers    = [dax production, nichion, pony canyon]
                      relatedAnime = [https://myanimelist.net/anime/48548]
                      tags         = [comedy, harem, romance, school, shounen]
                    )
                """.trimIndent()
            )
        }
    }
}