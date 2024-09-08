package io.github.manamiproject.modb.app.readme

import io.github.manamiproject.modb.core.models.Anime

/**
 * Updates the `README.md` of the dataset repo.
 * @since 1.0.0
 */
interface ReadmeCreator {

    /**
     * Updates the `README.md` file.
     * @since 1.0.0
     * @param mergedAnime List of finalized anime list.
     */
    suspend fun updateWith(mergedAnime: List<Anime>)
}