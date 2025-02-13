package io.github.manamiproject.modb.core.models

import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import java.time.LocalDate

/**
 * Generic return type for meta data provider score.
 * @since 16.7.0
 */
public sealed class MetaDataProviderScore

/**
 * Indicates that no score has been found.
 * @since 16.7.0
 */
public data object NoMetaDataProviderScore: MetaDataProviderScore()

/**
 * Represents the score as it is found on the site of the meta data provider.
 * @since 16.7.0
 * @param hostname Hostname of the meta data provider which was the source of this score.
 * @param value Score as-is
 * @param originalRange The range in which scores can be represented on the meta data provider site.
 * @param createdAt The date on which the value was determined.
 */
public data class MetaDataProviderScoreValue(
    val hostname: Hostname,
    private val value: Double = 0.0,
    private val originalRange: ClosedFloatingPointRange<Double>,
    val createdAt: LocalDate = LocalDate.now(),
): MetaDataProviderScore() {

    init {
        require(hostname.neitherNullNorBlank()) { "hostname must not be blank" }
        require("^([a-zA-Z]|-|\\d)+\\.[a-z]+$".toRegex().matches(hostname)) { "hostname has invalid format" }
        require(value >= 0.0) { "value must be >= 0.0" }
        require(originalRange.start >= 0.0) { "originalRange start must be >= 0.0" }
        require(originalRange.endInclusive >= 0.0) { "originalRange end must be >= 0.0" }
    }

    /**
     * Returns the original value rescaled to a score system from 1 to 10.
     * @since 16.7.0
     * @return A value between 1 to 10 representing the score of an anime where 1 is the worst rating and 10 the best.
     */
    public fun scaledValue(): Double = rescale(value, originalRange, 1.0..10.0)

    private fun rescale(
        value: Double,
        originalRange: ClosedFloatingPointRange<Double>,
        newRange: ClosedFloatingPointRange<Double>,
    ): Double {
        if (originalRange == newRange) {
            return value
        }

        if (value == 0.0) {
            return 0.0
        }

        val newMin = newRange.start
        val newMax = newRange.endInclusive

        val minValue = originalRange.start
        val maxValue = originalRange.endInclusive

        return ( (value - minValue) / (maxValue - minValue) ) * (newMax - newMin) + newMin
    }
}