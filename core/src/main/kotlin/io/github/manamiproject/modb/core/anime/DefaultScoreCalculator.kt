package io.github.manamiproject.modb.core.anime

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Default implementation for [ScoreCalculator].
 * @since 17.0.0
 */
public class DefaultScoreCalculator: ScoreCalculator {

    override fun calculateScore(scores: Collection<MetaDataProviderScore>): Score {
        return when {
            scores.isEmpty() -> NoScore
            else -> {
                val rescaledScores = scores.filterIsInstance<MetaDataProviderScoreValue>().map { it.scaledValue() }

                ScoreValue(
                    arithmeticGeometricMean = arithmeticGeometricMean(rescaledScores),
                    arithmeticMean = arithmeticMean(rescaledScores),
                    median = median(rescaledScores),
                )
            }
        }
    }

    private fun arithmeticMean(values: List<Double>) = values.sum() / values.size.toDouble()

    private fun median(values: List<Double>): Double {
        val sorted = values.sorted()

        return if (sorted.size % 2 == 0) {
            val lower = sorted[sorted.size / 2 - 1]
            val upper = sorted[sorted.size / 2]
            (lower + upper) / 2.0
        } else {
            sorted[sorted.size / 2]
        }
    }

    private fun arithmeticGeometricMean(values: List<Double>, epsilon: Double = 1.0E-256): Double {
        var am = arithmeticMean(values)
        var gm = values.reduce { acc, d -> acc * d }.pow( 1.0 / values.size.toDouble())

        while (am != gm && difference(am, gm) > epsilon) {
            val previousAm = am
            val previousGm = gm

            am = arithmeticMean(listOf(am, gm))
            gm = sqrt(previousAm * previousGm)

            if (am == previousAm && gm == previousGm) break
        }

        return gm
    }

    private fun difference(number1: Double, number2: Double): Double {
        val message = "Passed numbers must be positive or 0"
        require(number1 >= 0) { message }
        require(number2 >= 0) { message }

        val higher = if (number1 > number2) number1 else number2
        val lower = if (number1 < number2) number1 else number2

        return higher - lower
    }

    public companion object {

        /**
         * Singleton of [DefaultScoreCalculator]
         * @since 17.0.0
         */
        public val instance: DefaultScoreCalculator by lazy { DefaultScoreCalculator() }
    }
}