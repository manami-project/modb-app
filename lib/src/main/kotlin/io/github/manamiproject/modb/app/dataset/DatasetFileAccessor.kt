package io.github.manamiproject.modb.app.dataset

import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.anime.Anime

/**
 * Access to the dataset file.
 * @since 1.0.0
 */
interface DatasetFileAccessor {

    /**
     * Retrieve entries from dataset.
     * @since 1.0.0
     * @return List of anime.
     */
    suspend fun fetchEntries(): List<Anime>

    /**
     * Create dataset from list of [Anime].
     * @since 1.0.0
     * @param anime List of anime to be saved as dataset file.
     */
    suspend fun saveEntries(anime: List<Anime>)

    /**
     * Get dataset file based on the [type].
     * @since 1.0.0
     * @param type Type of dataset file.
     * @return Dataset file.
     */
    fun offlineDatabaseFile(type: DatasetFileType): RegularFile
}