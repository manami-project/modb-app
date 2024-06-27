package io.github.manamiproject.modb.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

internal class UtilTest {

    @Nested
    inner class DifferenceTests {

        @Test
        fun `first parameter is higher`() {
            // given
            val higher = 1995
            val lower = 1992

            // when
            val result = difference(higher, lower)

            // then
            assertThat(result).isEqualTo(3)
        }

        @Test
        fun `second parameter is higher`() {
            // given
            val higher = 1995
            val lower = 1992

            // when
            val result = difference(lower, higher)

            // then
            assertThat(result).isEqualTo(3)
        }

        @Test
        fun `both parameter are equal`() {
            // given
            val number = 1995

            // when
            val result = difference(number, number)

            // then
            assertThat(result).isZero()
        }

        @Test
        fun `both parameter can be 0`() {
            // given
            val number = 0

            // when
            val result = difference(number, number)

            // then
            assertThat(result).isZero()
        }

        @Test
        fun `throws exception if the first parameter is negative`() {
            // when
            val result = assertThrows<IllegalArgumentException> {
                difference(-1, 5)
            }

            // then
            assertThat(result).hasMessage("Passed numbers must be positive or 0")
        }

        @Test
        fun `throws exception if the second parameter is negative`() {
            // when
            val result = assertThrows<IllegalArgumentException> {
                difference(5, -1)
            }

            // then
            assertThat(result).hasMessage("Passed numbers must be positive or 0")
        }
    }

    @Nested
    inner class WeightedProbabilityOfTwoNumbersBeingEqualTests {

        @Test
        fun `throws exception if the factor is 1`() {
            // when
            val result = assertThrows<IllegalArgumentException> {
                weightedProbabilityOfTwoNumbersBeingEqual(4, 5, 1)
            }

            // then
            assertThat(result).hasMessage("Factor must be > 1")
        }

        @Test
        fun `throws exception if the factor is 0`() {
            // when
            val result = assertThrows<IllegalArgumentException> {
                weightedProbabilityOfTwoNumbersBeingEqual(4, 5, 0)
            }

            // then
            assertThat(result).hasMessage("Factor must be > 1")
        }

        @Test
        fun `throws exception if the factor is negative`() {
            // when
            val result = assertThrows<IllegalArgumentException> {
                weightedProbabilityOfTwoNumbersBeingEqual(4, 5, -1)
            }

            // then
            assertThat(result).hasMessage("Factor must be > 1")
        }

        @Test
        fun `two numbers being equal having a probability of 100 percent`() {
            // given
            val number = 5

            // when
            val result = weightedProbabilityOfTwoNumbersBeingEqual(number, number, 10)

            // then
            assertThat(result).isEqualTo(1.0)
        }

        @Test
        fun `two numbers differ by 1, factor 2 returns 98 percent probability`() {
            // given
            val number1 = 2
            val number2 = 3

            // when
            val result = weightedProbabilityOfTwoNumbersBeingEqual(number1, number2, 2)

            // then
            assertThat(result).isEqualTo(0.98)
        }

        @Test
        fun `two numbers differ by 2, factor 2 returns 96 perent probability`() {
            // given
            val number1 = 2
            val number2 = 4

            // when
            val result = weightedProbabilityOfTwoNumbersBeingEqual(number1, number2, 2)

            // then
            assertThat(result).isEqualTo(0.96)
        }

        @Test
        fun `two numbers differ by 3, factor 2 returns 92 percent probability`() {
            // given
            val number1 = 2
            val number2 = 5

            // when
            val result = weightedProbabilityOfTwoNumbersBeingEqual(number1, number2, 2)

            // then
            assertThat(result).isEqualTo(0.92)
        }

        @Test
        fun `two numbers differ by 1, factor 4 returns 96 percent probability`() {
            // given
            val number1 = 2
            val number2 = 3

            // when
            val result = weightedProbabilityOfTwoNumbersBeingEqual(number1, number2, 4)

            // then
            assertThat(result).isEqualTo(0.96)
        }

        @Test
        fun `two numbers differ by 2, factor 4 returns 84 percent probability`() {
            // given
            val number1 = 2
            val number2 = 4

            // when
            val result = weightedProbabilityOfTwoNumbersBeingEqual(number1, number2, 4)

            // then
            assertThat(result).isEqualTo(0.84)
        }

        @Test
        fun `two numbers differ by 3, factor 4 returns 96 percent probability`() {
            // given
            val number1 = 2
            val number2 = 5

            // when
            val result = weightedProbabilityOfTwoNumbersBeingEqual(number1, number2, 4)

            // then
            assertThat(result).isEqualTo(0.36)
        }

        @Test
        fun `if the difference is too big it will return 0 percent`() {
            // given
            val number1 = 2
            val number2 = 56

            // when
            val result = weightedProbabilityOfTwoNumbersBeingEqual(number1, number2, 4)

            // then
            assertThat(result).isEqualTo(0.0)
        }
    }
}