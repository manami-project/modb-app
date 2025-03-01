package io.github.manamiproject.modb.serde.json.models

/**
 * Describes the upper and lower boundaries of a score.
 * @since 5.4.0
 * @property minInclusive Minimum value that a score can take.
 * @property maxInclusive Maximum value that a score can take.
 */
public data class ScoreRange(
    val minInclusive: Double = 1.0,
    val maxInclusive: Double = 10.0,
)
