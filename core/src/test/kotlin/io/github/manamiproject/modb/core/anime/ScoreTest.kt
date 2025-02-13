package io.github.manamiproject.modb.core.anime

import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.time.LocalDate
import kotlin.test.Test

internal class ScoreTest {

    @Nested
    inner class NoScoreTests {

        @Test
        fun `NoScore is a Score`() {
            // then
            assertThat(NoScore).isInstanceOf(Score::class.java)
        }
    }

    @Nested
    inner class ScoreValueTests {

        @Test
        fun `default values`() {
            // when
            val result = ScoreValue()

            //then
            assertThat(result.median).isZero()
            assertThat(result.arithmeticMean).isZero()
            assertThat(result.arithmeticGeometricMean).isZero()
            assertThat(result.createdAt).isEqualTo(LocalDate.now())
        }

        @Nested
        inner class ConstructorTests {

            @Test
            fun `throws exception if median is negative`() {
                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    ScoreValue(
                        median = -0.1,
                    )
                }

                // then
                assertThat(result).hasMessage("median must be >= 0.0")
            }

            @Test
            fun `throws exception if arithmeticMean is negative`() {
                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    ScoreValue(
                        arithmeticMean = -0.1,
                    )
                }

                // then
                assertThat(result).hasMessage("arithmeticMean must be >= 0.0")
            }

            @Test
            fun `throws exception if arithmeticGeometricMean is negative`() {
                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    ScoreValue(
                        arithmeticGeometricMean = -0.1,
                    )
                }

                // then
                assertThat(result).hasMessage("arithmeticGeometricMean must be >= 0.0")
            }
        }
    }
}