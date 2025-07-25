package io.github.manamiproject.modb.core.anime

import io.github.manamiproject.modb.core.TestAnimeRawObjects
import io.github.manamiproject.modb.core.TestScoreCalculator
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class DefaultAnimeRawToAnimeTransformerTest {

    @Nested
    inner class TransformTests {

        @Test
        fun `correctly transforms object with default values`() {
            // given
            val testScoreCalculator = object: ScoreCalculator by TestScoreCalculator {
                override fun calculateScore(scores: Collection<MetaDataProviderScore>): Score = NoScore
            }

            val animeRawToAnimeTransformer = DefaultAnimeRawToAnimeTransformer(
                scoreCalculator = testScoreCalculator,
            )

            val animeRaw = AnimeRaw("test")

            // when
            val result = animeRawToAnimeTransformer.transform(animeRaw)

            // then
            assertThat(result.title).isEqualTo(animeRaw.title)
            assertThat(result.type).isEqualTo(animeRaw.type)
            assertThat(result.episodes).isEqualTo(animeRaw.episodes)
            assertThat(result.status).isEqualTo(animeRaw.status)
            assertThat(result.animeSeason).isEqualTo(animeRaw.animeSeason)
            assertThat(result.picture).isEqualTo(animeRaw.picture)
            assertThat(result.thumbnail).isEqualTo(animeRaw.thumbnail)
            assertThat(result.duration).isEqualTo(animeRaw.duration)
            assertThat(result.score).isEqualTo(NoScore)
            assertThat(result.sources).isEqualTo(animeRaw.sources)
            assertThat(result.synonyms).isEqualTo(animeRaw.synonyms)
            assertThat(result.studios).isEqualTo(animeRaw.studios)
            assertThat(result.producers).isEqualTo(animeRaw.producers)
            assertThat(result.relatedAnime).isEqualTo(animeRaw.relatedAnime)
            assertThat(result.tags).isEqualTo(animeRaw.tags)
        }

        @Test
        fun `correctly transforms object`() {
            // given
            val animeRaw = TestAnimeRawObjects.AllPropertiesSet.obj

            val testScoreCalculator = object: ScoreCalculator by TestScoreCalculator {
                override fun calculateScore(scores: Collection<MetaDataProviderScore>): Score = ScoreValue(
                    arithmeticGeometricMean = 1.29,
                    arithmeticMean = 2.38,
                    median = 3.47,
                )
            }

            val animeRawToAnimeTransformer = DefaultAnimeRawToAnimeTransformer(
                scoreCalculator = testScoreCalculator,
            )

            // when
            val result = animeRawToAnimeTransformer.transform(animeRaw)

            // then
            assertThat(result.title).isEqualTo(animeRaw.title)
            assertThat(result.type).isEqualTo(animeRaw.type)
            assertThat(result.episodes).isEqualTo(animeRaw.episodes)
            assertThat(result.status).isEqualTo(animeRaw.status)
            assertThat(result.animeSeason).isEqualTo(animeRaw.animeSeason)
            assertThat(result.picture).isEqualTo(animeRaw.picture)
            assertThat(result.thumbnail).isEqualTo(animeRaw.thumbnail)
            assertThat(result.duration).isEqualTo(animeRaw.duration)
            assertThat(result.score).isExactlyInstanceOf(ScoreValue::class.java)
            assertThat((result.score as ScoreValue).arithmeticGeometricMean).isEqualTo(1.29)
            assertThat((result.score).arithmeticMean).isEqualTo(2.38)
            assertThat((result.score).median).isEqualTo(3.47)
            assertThat(result.sources).isEqualTo(animeRaw.sources)
            assertThat(result.synonyms).isEqualTo(animeRaw.synonyms)
            assertThat(result.studios).isEqualTo(animeRaw.studios)
            assertThat(result.producers).isEqualTo(animeRaw.producers)
            assertThat(result.relatedAnime).isEqualTo(animeRaw.relatedAnime)
            assertThat(result.tags).isEqualTo(animeRaw.tags)
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
                assertThat(result === previous).isTrue()
            }
        }
    }
}