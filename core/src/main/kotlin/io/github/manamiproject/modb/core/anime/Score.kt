package io.github.manamiproject.modb.core.anime

import java.time.LocalDate

/**
 * Generic return type for an anime score.
 * @since 17.0.0
 */
public sealed class Score

/**
 * Indicates that no score has been found.
 * @since 17.0.0
 */
public data object NoScore: Score()

/**
 * Aggregated score across all available meta data providers.
 * @since 17.0.0
 * @param arithmeticMean aithmetic mean
 * @param arithmeticGeometricMean arithmetic-geometric-mean
 * @param median median
 * @param createdAt The date on which the value was determined.
 */
public data class ScoreValue(
    val arithmeticMean: Double = 0.0,
    val arithmeticGeometricMean: Double = 0.0,
    val median: Double = 0.0,
    val createdAt: LocalDate = LocalDate.now(),
): Score() {

    init {
        require(arithmeticMean >= 0.0) { "arithmeticMean must be >= 0.0" }
        require(arithmeticGeometricMean >= 0.0) { "arithmeticGeometricMean must be >= 0.0" }
        require(median >= 0.0) { "median must be >= 0.0" }
    }
}