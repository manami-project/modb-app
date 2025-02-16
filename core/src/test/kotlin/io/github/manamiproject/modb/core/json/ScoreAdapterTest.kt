package io.github.manamiproject.modb.core.json

import io.github.manamiproject.modb.core.anime.NoScore
import io.github.manamiproject.modb.core.anime.ScoreValue
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class ScoreAdapterTest {

    @Nested
    inner class FromJsonTests {

        @Test
        fun `throws exception if arithmeticGeometricMean is missing`() {
            // given
            val adapter = ScoreAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""{"arithmeticMean":2.38,"median":3.47}""")
            }

            // then
            assertThat(result).hasMessage("Properties for 'score' are missing: [arithmeticGeometricMean]")
        }

        @Test
        fun `throws exception if arithmeticMean is missing`() {
            // given
            val adapter = ScoreAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""{"arithmeticGeometricMean":1.29,"median":3.47}""")
            }

            // then
            assertThat(result).hasMessage("Properties for 'score' are missing: [arithmeticMean]")
        }

        @Test
        fun `throws exception if median is missing`() {
            // given
            val adapter = ScoreAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""{"arithmeticGeometricMean":1.29,"arithmeticMean":2.38}""")
            }

            // then
            assertThat(result).hasMessage("Properties for 'score' are missing: [median]")
        }

        @Test
        fun `exception lists all properties which are missing`() {
            // given
            val adapter = ScoreAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""{"arithmeticMean":2.38}""")
            }

            // then
            assertThat(result).hasMessage("Properties for 'score' are missing: [arithmeticGeometricMean, median]")
        }

        @Test
        fun `correctly deserialze valid score`() {
            // given
            val adapter = ScoreAdapter()
            val expected = ScoreValue(
                arithmeticGeometricMean = 1.29,
                arithmeticMean = 2.38,
                median = 3.47,
            )

            // when
            val result = adapter.fromJson("""{"arithmeticGeometricMean":1.29,"arithmeticMean":2.38,"median":3.47}""")

            // then
            assertThat(result).isEqualTo(expected)
        }

        @Test
        fun `returns NoScore for null`() {
            // given
            val adapter = ScoreAdapter()

            // when
            val result = adapter.fromJson("""null""")

            // then
            assertThat(result).isEqualTo(NoScore)
        }

        @Test
        fun `ignore other properties`() {
            // given
            val adapter = ScoreAdapter()
            val expected = ScoreValue(
                arithmeticGeometricMean = 1.29,
                arithmeticMean = 2.38,
                median = 3.47,
            )

            // when
            val result = adapter.fromJson("""{"arithmeticGeometricMean":1.29,"unmaped":true,"arithmeticMean":2.38,"median":3.47}""")

            // then
            assertThat(result).isEqualTo(expected)
        }
    }

    @Nested
    inner class ToJsonTests {

        @Test
        fun `throws exception if value is null`() {
            // given
            val adapter = ScoreAdapter()

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                adapter.toJson(null)
            }

            // then
            assertThat(result).hasMessage("ScoreAdapter expects non-nullable value, but received null.")
        }

        @Test
        fun `correctly serializes NoScore if serializeNulls is false`() {
            // given
            val adapter = ScoreAdapter()

            // when
            val result = adapter.toJson(NoScore)

            // then
            assertThat(result).isEqualTo(EMPTY)
        }

        @Test
        fun `correctly serializes NoScore if serializeNulls is true`() {
            // given
            val adapter = ScoreAdapter().serializeNulls()

            // when
            val result = adapter.toJson(NoScore)

            // then
            assertThat(result).isEqualTo("""null""")
        }

        @Test
        fun `correctly serializes ScoreValue`() {
            // given
            val adapter = ScoreAdapter()
            val scoreValue = ScoreValue(
                arithmeticGeometricMean = 1.29,
                arithmeticMean = 2.38,
                median = 3.47,
            )

            // when
            val result = adapter.toJson(scoreValue)

            // then
            assertThat(result).isEqualTo("""{"arithmeticGeometricMean":1.29,"arithmeticMean":2.38,"median":3.47}""")
        }
    }
}