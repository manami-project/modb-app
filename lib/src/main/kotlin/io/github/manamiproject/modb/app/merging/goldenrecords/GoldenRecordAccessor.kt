package io.github.manamiproject.modb.app.merging.goldenrecords

import io.github.manamiproject.modb.app.merging.matching.MatchingProbabilityCalculator
import io.github.manamiproject.modb.core.models.Anime
import java.net.URI
import java.util.*

/**
 * Manages golden records. During the merge prorcess which checks one DCS entry after another a golden record represents
 * a temporary entry for the dataset. A golden record is either a single unmergable anime or an anime consisting of
 * multiple DCS anime instances merged together.
 *
 * Golden records only exist within this class. Outside of this class the instances are referred to as
 * [PotentialGoldenRecord]. Those are used to calculate the probability which shows how likely it is that an anime
 * should be merged with an existing golden record.
 *
 * The finalized of golden records can be retrieved via [allEntries]. This is the final result and the basis for the
 * dataset.
 * @since 1.0.0
 */
interface GoldenRecordAccessor {

    /**
     * Creates a new golden record.
     * @since 1.0.0
     * @param anime Anime for which a new folden record will be created.
     */
    fun createGoldenRecord(anime: Anime)

    /**
     * Allows you to find a single golden record based on a [Set] of [URI].
     * @since 1.0.0
     * @param sources Could be [Anime.sources].
     * @return Either a [PotentialGoldenRecord] or null of no golden record was found for any of the given [URI].
     */
    fun findGoldenRecordBySource(sources: Set<URI>): PotentialGoldenRecord?

    /**
     * Tries to find possible golden records for a specific anime. In order to determine which record is the best
     * fit, you have to calculate the probability using [MatchingProbabilityCalculator.calculate].
     * @since 1.0.0
     * @param anime Anime for which fitting golden records are searched.
     * @return A [Set] of potentially fitting golden records.
     */
    fun findPossibleGoldenRecords(anime: Anime): Set<PotentialGoldenRecord>

    /**
     * Merge an anime with a golden record.
     * @since 1.0.0
     * @param goldenRecordId ID of the golden record. The ID is temporary and only valid for the current run of the app.
     * @param anime Anime which should be merged into an existing golden record.
     */
    fun merge(goldenRecordId: UUID, anime: Anime): Anime

    /**
     * Retrieves the golden records as a list. This is the basis for the dataset.
     * @since 1.0.0
     * @return All golden records presented as [List] of [Anime].
     */
    fun allEntries(): List<Anime>

    /**
     * Clears the current golden record list.
     * @since 1.0.0
     */
    fun clear()
}