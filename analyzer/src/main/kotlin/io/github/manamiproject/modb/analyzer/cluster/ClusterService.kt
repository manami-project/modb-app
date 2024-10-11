package io.github.manamiproject.modb.analyzer.cluster

import io.github.manamiproject.modb.app.merging.DefaultReviewedIsolatedEntriesAccessor
import io.github.manamiproject.modb.app.merging.ReviewedIsolatedEntriesAccessor
import io.github.manamiproject.modb.app.merging.lock.DefaultMergeLockAccessor
import io.github.manamiproject.modb.app.merging.lock.MergeLockAccessor
import io.github.manamiproject.modb.core.models.Anime
import kotlinx.coroutines.runBlocking

/**
 * Access to clusters of anime.
 * A cluster is defined by the number of URLs in `sources`.
 * @sice 1.0.0
 * @property reviewedIsolatedEntriesAccessor
 * @property mergeLockAccessor
 */
internal class ClusterService(
    private val reviewedIsolatedEntriesAccessor: ReviewedIsolatedEntriesAccessor = DefaultReviewedIsolatedEntriesAccessor.instance,
    private val mergeLockAccessor: MergeLockAccessor = DefaultMergeLockAccessor.instance,
) {

    /**
     * Returns the number of database entries and unreviewd entries grouped by the number of sources.
     * @since 1.0.0
     * @param datasetEntries All anime to be clustered.
     * @return A [Map] having the number of sources as key. The value is a Pair where the first value is the number of
     * entries within the database for this cluster and the second value is the number if unreviewed entries.
     */
    fun clusterDistribution(datasetEntries: List<Anime>): Map<Int, Pair<Int, Int>> {
        val numberOfEntriesPerCluster = datasetEntries.groupBy { it.sources.size }
            .map { it.key to it.value.size }
            .sortedBy { it.first }
            .toMap()

        val numberOfUnreviewedEntries = datasetEntries.asSequence()
            .map {
                val result = runBlocking {
                    if (it.sources.size == 1) {
                        reviewedIsolatedEntriesAccessor.contains(it.sources.first()) || mergeLockAccessor.hasMergeLock(it.sources)
                    } else {
                        mergeLockAccessor.hasMergeLock(it.sources)
                    }
                }

                return@map it.sources.size to result
            }
            .filterNot { (_, value) -> value }
            .groupBy { it.first }
            .map { it.key to it.value.size }
            .sortedBy { it.first }.toList()
            .toMap()

        return numberOfEntriesPerCluster.map { it.key to (it.value to (numberOfUnreviewedEntries[it.key] ?: 0)) }.toMap()
    }

    /**
     * Finds anime in a given list whose number of `sources` is equal to the cluster size provided as parameter.
     * @since 1.0.0
     * @param datasetEntries All anime in which to look for entries where the the number `sources` matches [cluster].
     * @param cluster Number of entries in `sources` to look for.
     */
    fun fetchCluster(datasetEntries: List<Anime>, cluster: Int): List<Anime> {
        return datasetEntries.groupBy {
            it.sources.size
        }[cluster].orEmpty()
    }
}