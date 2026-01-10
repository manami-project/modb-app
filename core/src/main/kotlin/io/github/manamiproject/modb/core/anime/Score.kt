package io.github.manamiproject.modb.core.anime

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
 * Aggregated score across all available metadata providers.
 * @since 17.0.0
 * @property arithmeticGeometricMean arithmetic-geometric-mean
 * @property arithmeticMean arithmetic mean
 * @property median median
 */
public data class ScoreValue(
    val arithmeticGeometricMean: Double = 0.0,
    val arithmeticMean: Double = 0.0,
    val median: Double = 0.0,
): Score() {

    init {
        require(arithmeticGeometricMean >= 0.0) { "arithmeticGeometricMean must be >= 0.0" }
        require(arithmeticMean >= 0.0) { "arithmeticMean must be >= 0.0" }
        require(median >= 0.0) { "median must be >= 0.0" }
    }
}