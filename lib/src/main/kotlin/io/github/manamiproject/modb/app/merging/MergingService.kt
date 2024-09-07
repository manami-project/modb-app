package io.github.manamiproject.modb.app.merging

import io.github.manamiproject.modb.core.models.Anime

/**
 * Merges anime.
 * @since 1.0.0
 */
interface MergingService {

    /**
     * @since 1.0.0
     * @param unmergedAnime List of unmerged anime.
     * @return List of merged anime.
     */
    suspend fun merge(unmergedAnime: List<Anime>): List<Anime>
}