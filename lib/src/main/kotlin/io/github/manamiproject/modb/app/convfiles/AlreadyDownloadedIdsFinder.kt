package io.github.manamiproject.modb.app.convfiles

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig

/**
 * Checks raw files to see which IDs have already been downloaded so far.
 * @since 1.0.0
 */
interface AlreadyDownloadedIdsFinder {

    /**
     * Returns the list of IDs already downloaded for a specific metadata provider.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific metadata provider.
     * @return Set of anime IDs already downloaded for the given metadata provider.
     */
    suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId>
}