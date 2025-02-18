package io.github.manamiproject.modb.core.anime

import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class DefaultScoreCalculatorTest {
    
    @Nested
    inner class CalculateScoreTests {
        
        @Test
        fun `no score`() {
            // given
            val scoreCreator = DefaultScoreCalculator()

            // when
            val result = scoreCreator.calculateScore(emptyList())
    
            // then
            assertThat(result).isEqualTo(NoScore)
        }
    
        @Test
        fun `return values as-is for a single raw score`() {
            // given
            val scoreCreator = DefaultScoreCalculator()
            val scores = listOf(
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 5.0,
                    originalRange = 1.0..10.0,
                ),
            )

            // when
            val result = scoreCreator.calculateScore(scores) as ScoreValue

            // then
            assertThat(result.arithmeticGeometricMean).isEqualTo(5.0)
            assertThat(result.arithmeticMean).isEqualTo(5.0)
            assertThat(result.median).isEqualTo(5.0)
        }
    
        @Test
        fun `correctly calculate for two raw scores`() {
            // given
            val scoreCreator = DefaultScoreCalculator()
            val scores = listOf(
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 3.14,
                    originalRange = 1.0..10.0,
                ),
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 7.85,
                    originalRange = 1.0..10.0,
                ),
            )

            // when
            val result = scoreCreator.calculateScore(scores) as ScoreValue

            // then
            assertThat(result.arithmeticGeometricMean).isEqualTo(5.226525514938778)
            assertThat(result.arithmeticMean).isEqualTo(5.495)
            assertThat(result.median).isEqualTo(5.495)
        }
    
        @Test
        fun `correctly calculate for three raw scores`() {
            // given
            val scoreCreator = DefaultScoreCalculator()
            val scores = listOf(
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 3.14,
                    originalRange = 1.0..10.0,
                ),
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 7.85,
                    originalRange = 1.0..10.0,
                ),
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 6.19,
                    originalRange = 1.0..10.0,
                ),
            )


            // when
            val result = scoreCreator.calculateScore(scores) as ScoreValue

            // then
            assertThat(result.arithmeticGeometricMean).isEqualTo(5.533451026668135)
            assertThat(result.arithmeticMean).isEqualTo(5.726666666666667)
            assertThat(result.median).isEqualTo(6.19)
        }
    
        @Test
        fun `correctly calculate for four raw scores`() {
            // given
            val scoreCreator = DefaultScoreCalculator()
            val scores = listOf(
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 3.14,
                    originalRange = 1.0..10.0,
                ),
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 7.85,
                    originalRange = 1.0..10.0,
                ),
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 6.19,
                    originalRange = 1.0..10.0,
                ),
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 5.21,
                    originalRange = 1.0..10.0,
                ),
            )

            // when
            val result = scoreCreator.calculateScore(scores) as ScoreValue

            // then
            assertThat(result.arithmeticGeometricMean).isEqualTo(5.452724485612647)
            assertThat(result.arithmeticMean).isEqualTo(5.5975)
            assertThat(result.median).isEqualTo(5.7)
        }
    
        @Test
        fun `use correct abort condition to prevent infinite loop`() {
            // given
            val scoreCreator = DefaultScoreCalculator()
            val scores = listOf(
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 7.090909090909091,
                    originalRange = 1.0..10.0,
                ),
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 7.514545454545455,
                    originalRange = 1.0..10.0,
                ),
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 7.67,
                    originalRange = 1.0..10.0,
                ),
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 7.16,
                    originalRange = 1.0..10.0,
                ),
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 5.95,
                    originalRange = 1.0..10.0,
                ),
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 7.342,
                    originalRange = 1.0..10.0,
                ),
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 6.9411764705882355,
                    originalRange = 1.0..10.0,
                ),
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 7.27,
                    originalRange = 1.0..10.0,
                ),
            )

            // when
            val result = scoreCreator.calculateScore(scores) as ScoreValue

            // then
            assertThat(result.arithmeticGeometricMean).isEqualTo(7.108196705468187)
            assertThat(result.arithmeticMean).isEqualTo(7.117328877005347)
            assertThat(result.median).isEqualTo(7.215)
        }
    } 

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = DefaultScoreCalculator.instance

                // when
                val result = DefaultScoreCalculator.instance

                // then
                assertThat(result).isExactlyInstanceOf(DefaultScoreCalculator::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}