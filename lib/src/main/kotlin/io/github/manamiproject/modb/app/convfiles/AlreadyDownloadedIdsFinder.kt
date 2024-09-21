package io.github.manamiproject.modb.app.convfiles

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig

/**
 * Checks conv files to see which IDs have already been downloaded so far.
 * @since 1.0.0
 */
interface AlreadyDownloadedIdsFinder {

    /**
     * Returns the list of IDs already downloaded for a specfic meta data provider.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific meta data provider.
     * @return Set of anime IDs already downloaded for the given meta data provider.
     */
    suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId>
}