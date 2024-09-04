package io.github.manamiproject.modb.app.merging.matching

import io.github.manamiproject.modb.app.merging.goldenrecords.PotentialGoldenRecord
import io.github.manamiproject.modb.core.models.Anime

/**
 * @since 1.0.0
 */
typealias Percent = Double

/**
 * @since 1.0.0
 * @param anime
 * @param potentialGoldenRecord
 * @param matchProbability
 */
data class MatchingProbabilityResult(
    val anime: Anime,
    val potentialGoldenRecord: PotentialGoldenRecord,
    val matchProbability: Percent = 0.0,
)
