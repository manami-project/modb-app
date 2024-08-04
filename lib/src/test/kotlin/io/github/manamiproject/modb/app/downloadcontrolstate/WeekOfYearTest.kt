package io.github.manamiproject.modb.app.downloadcontrolstate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDate
import kotlin.test.Test


internal class WeekOfYearTest {

    @Nested
    inner class ConstructorTests {

        @Test
        fun `throws exception if the year is set before the year of the first anime`() {
            // given
            val value = 196

            // when
            val result = assertThrows<IllegalArgumentException> {
                WeekOfYear(
                    year = value,
                    week = 1
                )
            }

            // then
            assertThat(result).hasMessage("Invalid value [$value]: Year is set before the year of the first anime.")
        }

        @Test
        fun `throws exception if the year is set too far in the future`() {
            // given
            val value = LocalDate.now().year + 6

            // when
            val result = assertThrows<IllegalArgumentException> {
                WeekOfYear(
                    year = value,
                    week = 1
                )
            }

            // then
            assertThat(result).hasMessage("Invalid value [$value]: Year is set too far on the future.")
        }

        @Test
        fun `throws exception if the week is greater than 53`() {
            // given
            val value = 54

            // when
            val result = assertThrows<IllegalArgumentException> {
                WeekOfYear(
                    year = 2021,
                    week = value,
                )
            }

            // then
            assertThat(result).hasMessage("Week [$value] exceeds number of weeks.")
        }

        @ParameterizedTest
        @ValueSource(ints = [0, -1])
        fun `throws excepption if number of weeks is zero or negative`(value: Int) {
            // when
            val result = assertThrows<IllegalArgumentException> {
                WeekOfYear(
                    year = 2021,
                    week = value,
                )
            }

            // then
            assertThat(result).hasMessage("Week [$value] must not be zero or negative.")
        }

        @Test
        fun `correctly create instance from LocalDate`() {
            // given
            val expected = WeekOfYear(
                year = 2024,
                week = 27,
            )

            // when
            val result = WeekOfYear(LocalDate.of(2024, 7, 1))

            // then
            assertThat(result).isEqualTo(expected)
        }
    }

    @Nested
    inner class PlusWeeksTests {

        @Test
        fun `increase week by 2`() {
            // given
            val weekOfYear = WeekOfYear(
                year = 2021,
                week = 2,
            )

            // when
            val result = weekOfYear.plusWeeks(2)

            // then
            assertThat(result.year).isEqualTo(2021)
            assertThat(result.week).isEqualTo(4)
        }

        @Test
        fun `increase to the last week of the year`() {
            // given
            val weekOfYear = WeekOfYear(
                year = 2021,
                week = 51,
            )

            // when
            val result = weekOfYear.plusWeeks(1)

            // then
            assertThat(result.year).isEqualTo(2021)
            assertThat(result.week).isEqualTo(52)
        }

        @Test
        fun `increase to the first week of the new year`() {
            // given
            val weekOfYear = WeekOfYear(
                year = 2021,
                week = 52,
            )

            // when
            val result = weekOfYear.plusWeeks(1)

            // then
            assertThat(result.year).isEqualTo(2022)
            assertThat(result.week).isEqualTo(1)
        }
    }

    @Nested
    inner class DifferenceTests {

        @Test
        fun `correctly determine difference calling function on ealier date`() {
            // given
            val earlier = WeekOfYear(
                year = 2021,
                week = 12,
            )

            val later = WeekOfYear(
                year = 2021,
                week = 36,
            )

            // when
            val result = earlier.difference(later)

            // then
            assertThat(result).isEqualTo(24)
        }

        @Test
        fun `correctly determine difference calling function on later date`() {
            // given
            val earlier = WeekOfYear(
                year = 2021,
                week = 12,
            )

            val later = WeekOfYear(
                year = 2021,
                week = 36,
            )

            // when
            val result = later.difference(earlier)

            // then
            assertThat(result).isEqualTo(24)
        }
    }

    @Nested
    inner class ToLocalDateTests {

        @Test
        fun `correctly return first week of year`() {
            // given
            val weekOfYear = WeekOfYear(
                week = 1,
                year = 2022,
            )

            // when
            val result = weekOfYear.toLocalDate()

            // then
            assertThat(result).isEqualTo(LocalDate.of(2022, 1,3))
        }

        @Test
        fun `correctly return any week within the year`() {
            // given
            val weekOfYear = WeekOfYear(
                week = 8,
                year = 2022,
            )

            // when
            val result = weekOfYear.toLocalDate()

            // then
            assertThat(result).isEqualTo(LocalDate.of(2022, 2,21))
        }

        @Test
        fun `correctly return last week of year`() {
            // given
            val weekOfYear = WeekOfYear(
                week = 52,
                year = 2021,
            )

            // when
            val result = weekOfYear.toLocalDate()

            // then
            assertThat(result).isEqualTo(LocalDate.of(2021, 12,27))
        }
    }

    @Nested
    inner class CurrentWeekTests {

        @Test
        fun `correctly set value to this week`() {
            // given
            val now = LocalDate.now()
            val expected = WeekOfYear(now)

            // when
            val result = WeekOfYear.currentWeek()

            // then
            assertThat(result.year).isEqualTo(expected.year)
            assertThat(result.week).isEqualTo(expected.week)
        }
    }

    @Nested
    inner class CompareToTests {

        @Nested
        inner class GreaterThanTests {

            @Test
            fun `returns true if week is greater than other week`() {
                // given
                val week = WeekOfYear.currentWeek().plusWeeks(1)
                val other = WeekOfYear.currentWeek()

                // when
                val result = week > other

                // then
                assertThat(result).isTrue()
            }

            @Test
            fun `returns false if week is equal to other week`() {
                // given
                val week = WeekOfYear.currentWeek()
                val other = WeekOfYear.currentWeek()

                // when
                val result = week > other

                // then
                assertThat(result).isFalse()
            }

            @Test
            fun `returns false if week is less to other week`() {
                // given
                val week = WeekOfYear.currentWeek()
                val other = WeekOfYear.currentWeek().plusWeeks(1)

                // when
                val result = week > other

                // then
                assertThat(result).isFalse()
            }
        }

        @Nested
        inner class GreaterThanOrEqualTests {

            @Test
            fun `returns true if week is greater than other week`() {
                // given
                val week = WeekOfYear.currentWeek().plusWeeks(1)
                val other = WeekOfYear.currentWeek()

                // when
                val result = week >= other

                // then
                assertThat(result).isTrue()
            }

            @Test
            fun `returns true if week is equal to other week`() {
                // given
                val week = WeekOfYear.currentWeek()
                val other = WeekOfYear.currentWeek()

                // when
                val result = week >= other

                // then
                assertThat(result).isTrue()
            }

            @Test
            fun `returns false if week is less to other week`() {
                // given
                val week = WeekOfYear.currentWeek()
                val other = WeekOfYear.currentWeek().plusWeeks(1)

                // when
                val result = week >= other

                // then
                assertThat(result).isFalse()
            }
        }

        @Nested
        inner class LessThanTests {

            @Test
            fun `returns true if week is less than other week`() {
                // given
                val week = WeekOfYear.currentWeek()
                val other = WeekOfYear.currentWeek().plusWeeks(1)

                // when
                val result = week < other

                // then
                assertThat(result).isTrue()
            }

            @Test
            fun `returns false if week is equal to other week`() {
                // given
                val week = WeekOfYear.currentWeek()
                val other = WeekOfYear.currentWeek()

                // when
                val result = week < other

                // then
                assertThat(result).isFalse()
            }

            @Test
            fun `returns false if week is greater to other week`() {
                // given
                val week = WeekOfYear.currentWeek().plusWeeks(1)
                val other = WeekOfYear.currentWeek()

                // when
                val result = week < other

                // then
                assertThat(result).isFalse()
            }
        }

        @Nested
        inner class LessThanOrEqualTests {

            @Test
            fun `returns true if week is less than other week`() {
                // given
                val week = WeekOfYear.currentWeek()
                val other = WeekOfYear.currentWeek().plusWeeks(1)

                // when
                val result = week <= other

                // then
                assertThat(result).isTrue()
            }

            @Test
            fun `returns true if week is equal to other week`() {
                // given
                val week = WeekOfYear.currentWeek()
                val other = WeekOfYear.currentWeek()

                // when
                val result = week <= other

                // then
                assertThat(result).isTrue()
            }

            @Test
            fun `returns false if week is greater to other week`() {
                // given
                val week = WeekOfYear.currentWeek().plusWeeks(1)
                val other = WeekOfYear.currentWeek()

                // when
                val result = week <= other

                // then
                assertThat(result).isFalse()
            }
        }
    }

    @Nested
    inner class ToStringTests {

        @Test
        fun `correctly create zero based string`() {
            // given
            val weekOfYear = WeekOfYear(
                year = 2019,
                week = 2,
            )

            // when
            val result = weekOfYear.toString()

            // then
            assertThat(result).isEqualTo("2019-02")
        }

        @Test
        fun `correctly create string with week greater 9`() {
            // given
            val weekOfYear = WeekOfYear(
                year = 2019,
                week = 12,
            )

            // when
            val result = weekOfYear.toString()

            // then
            assertThat(result).isEqualTo("2019-12")
        }
    }

    @Nested
    inner class WeekOfYearExtensionTests {

        @Test
        fun `correctly return week`() {
            // given
            val date = LocalDate.of(2022, 2,21)
            val expected = WeekOfYear(
                week = 8,
                year = 2022,
            )

            // when
            val result = date.weekOfYear()

            // then
            assertThat(result).isEqualTo(expected)
        }
    }
}