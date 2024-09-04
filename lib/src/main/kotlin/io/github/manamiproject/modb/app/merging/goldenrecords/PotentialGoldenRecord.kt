package io.github.manamiproject.modb.app.merging.goldenrecords

import io.github.manamiproject.modb.core.models.Anime
import java.util.*

/**
 * Within the merging step a golden record represents a temporary final step of an anime. When the whole merging process
 * has finished the golden records are the base for the entries of the finalized data set.
 * During the merging process golden records are the targets for [Anime] being merged into.
 *
 * The merging process runs through every DCS entry. So the lifecycle of a golden record starts with a single anime
 * which is then merged each time with fitting other anime.
 *
 * Golden records only exist inside [GoldenRecordAccessor]. Outside of the class they are referred to
 * as [PotentialGoldenRecord].
 * @since 1.0.0
 * @param id Randomly generated ID to be able to identify this golden record in the current run.
 * @param anime Anime which can already consist of multiple [Anime] being merged together.
 */
data class PotentialGoldenRecord(
    val id: UUID,
    val anime: Anime,
)
