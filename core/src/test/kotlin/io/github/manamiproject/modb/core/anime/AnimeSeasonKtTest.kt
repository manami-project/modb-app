package io.github.manamiproject.modb.core.anime

import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDate
import kotlin.test.Test

internal class AnimeSeasonKtTest {

    @Nested
    inner class SeasonTests {

        @ParameterizedTest
        @ValueSource(strings = ["SPRING", "SpRiNg", " SpRiNg", "SpRiNg "])
        fun `'SPRING' by string`(value: String) {
            // when
            val result = AnimeSeason.Season.of(value)

            // then
            assertThat(result).isEqualTo(SPRING)
        }

        @ParameterizedTest
        @ValueSource(strings = ["SUMMER", "SuMmEr", " SuMmEr", "SuMmEr "])
        fun `'SUMMER' by string`(value: String) {
            // when
            val result = AnimeSeason.Season.of(value)

            // then
            assertThat(result).isEqualTo(SUMMER)
        }

        @ParameterizedTest
        @ValueSource(strings = ["FALL", "FaLl", " FaLl", "FaLl "])
        fun `'FALL' by string`(value: String) {
            // when
            val result = AnimeSeason.Season.of(value)

            // then
            assertThat(result).isEqualTo(FALL)
        }

        @ParameterizedTest
        @ValueSource(strings = ["WINTER","WiNtEr", " WiNtEr", "WiNtEr "])
        fun `'WINTER' by string`(value: String) {
            // when
            val result = AnimeSeason.Season.of(value)

            // then
            assertThat(result).isEqualTo(WINTER)
        }

        @ParameterizedTest
        @ValueSource(strings = ["UNDEFINED", "UnDeFiNeD", " UnDeFiNeD", "UnDeFiNeD "])
        fun `'UNDEFINED' by string`(value: String) {
            // when
            val result = AnimeSeason.Season.of(value)

            // then
            assertThat(result).isEqualTo(UNDEFINED)
        }

        @Test
        fun `'UNDEFINED' as failover for any non-matching string`() {
            // given
            val value = "non-matching-string"

            // when
            val result = AnimeSeason.Season.of(value)

            // then
            assertThat(result).isEqualTo(UNDEFINED)
        }

        @Test
        fun `default is 'UNDEFINED'`() {
            // when
            val result = AnimeSeason()

            // then
            assertThat(result.season).isEqualTo(UNDEFINED)
        }
    }

    @Nested
    inner class YearTests {

        @Nested
        inner class ConstructorTests {

            @Test
            fun `year cannot be before 1900`() {
                // when
                val result = assertThrows<IllegalArgumentException> {
                    AnimeSeason(
                        year = 1899,
                    )
                }

                // then
                assertThat(result).hasMessage("Year of premiere [1899] is not valid")
            }

            @Test
            fun `year can be from 1907 on, because that is the year of the first anime in japan`() {
                // when
                val result = AnimeSeason(
                    year = 1907,
                )

                // then
                assertThat(result.isYearOfPremiereUnknown()).isFalse()
                assertThat(result.isYearOfPremiereKnown()).isTrue()
            }

            @Test
            fun `year cannot be 11 or more years in the future`() {
                // given
                val year = LocalDate.now().year + 11

                // when
                val result = assertThrows<IllegalArgumentException> {
                    AnimeSeason(
                        year = year,
                    )
                }

                // then
                assertThat(result).hasMessage("Year of premiere [$year] is not valid")
            }

            @Test
            fun `year can be up to 10 years from now`() {
                // given
                val year = LocalDate.now().year + 10

                // when
                val result = AnimeSeason(
                    year = year,
                )

                // then
                assertThat(result.year).isEqualTo(year)
            }

            @Test
            fun `default year is 0 and means that no particular year is known`() {
                // given
                val animeSeason = AnimeSeason()

                // when
                val isYearOfPremiereUnknown = animeSeason.isYearOfPremiereUnknown()
                val isYearOfPremiereKnown = animeSeason.isYearOfPremiereKnown()

                // then
                assertThat(animeSeason.year).isZero()
                assertThat(isYearOfPremiereUnknown).isTrue()
                assertThat(isYearOfPremiereKnown).isFalse()
            }
        }
    }
}