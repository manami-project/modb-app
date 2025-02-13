package io.github.manamiproject.modb.app.merging.matching

import io.github.manamiproject.modb.app.merging.goldenrecords.GoldenRecordAccessor
import io.github.manamiproject.modb.app.merging.goldenrecords.PotentialGoldenRecord
import io.github.manamiproject.modb.core.anime.AnimeRaw

/**
 * Checks if the instance of an [AnimeRaw] belongs to an existing golden record.
 * @since 1.0.0
 */
interface MatchingProbabilityCalculator {

    /**
     * Calculates the probability how likely it is that an anime and a golden record should be merged together.
     * @since 1.0.0
     * @param anime Anime which might potentially be merged with the golden record.
     * @param potentialGoldenRecord The potential record acting as target for the [anime].
     * @return A probability how likely it is that this combination should be merged together. If you actually want
     * to perform the merge based on the result you must use [GoldenRecordAccessor.merge].
     */
    fun calculate(anime: AnimeRaw, potentialGoldenRecord: PotentialGoldenRecord): MatchingProbabilityResult
}