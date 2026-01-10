package io.github.manamiproject.modb.app.dataset

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.RegularFile
import java.net.URI

/**
 * Access to the dead entries files from the dataset.
 * @since 1.0.0
 */
interface DeadEntriesAccessor {

    /**
     * Retrieve the dead entries file for a specific metadata provider.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific metadata provider.
     * @param type Type of dataset file.
     * @return Dead entries file for a specific metadata provider from the dataset.
     */
    fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile

    /**
     * Add entry to dead entries file of a specific metadata provider.
     * @since 1.0.0
     * @param animeId ID of the anime as defined by the metadata provider.
     * @param metaDataProviderConfig Configuration for a specific metadata provider.
     */
    suspend fun addDeadEntry(animeId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig)

    /**
     * Find dead entries in a list of [URI].
     * @since 1.0.0
     * @param sources List of URIs to check for dead entries.
     * @return List of dead entries
     */
    suspend fun determineDeadEntries(sources: Collection<URI>): Set<URI>

    /**
     * Fetch all dead entries for a specific metadata provider from the dataset.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific metadata provider.
     * @return All dead entries for a specific metadata provider.
     */
    suspend fun fetchDeadEntries(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId>
}