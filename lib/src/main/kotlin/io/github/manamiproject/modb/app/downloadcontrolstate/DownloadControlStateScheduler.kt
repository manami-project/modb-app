package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig

/**
 * Allows to check which anime to download in the current week which are not supposed to be downloaded.
 * @since 1.0.0
 */
interface DownloadControlStateScheduler {

    /**
     * Determine which anime are not supposed to be downloaded for a specific meta data provider in the current week.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific meta data provider.
     * @return Set of IDs for anime which are not supposed to be downloaded this week.
     */
    suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId>

    /**
     * Determine which anime are supposed to be downloaded for a specific meta data provider in the current week.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific meta data provider.
     * @return Set of IDs for anime which are supposed to be downloaded this week.
     */
    suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId>
}