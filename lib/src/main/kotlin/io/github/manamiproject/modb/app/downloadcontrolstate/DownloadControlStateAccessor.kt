package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.merging.lock.MergeLockAccess
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.models.Anime

/**
 * File suffix used for the files containing the serialized [DownloadControlStateEntry] object.
 * @since 1.0.0
 */
const val DOWNLOAD_CONTROL_STATE_FILE_SUFFIX = "dcs"

/**
 * Allows acces to DCS files.
 * @since 1.0.0
 */
interface DownloadControlStateAccessor {

    /**
     * Retrieve the meta data provider specific DCS file directory.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific meta data provider.
     * @return Direcory in which the DCS files are stored for the given [metaDataProviderConfig].
     */
    fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory

    /**
     * Return all [Anime] from DCS files.
     * @since 1.0.0
     * @return All anime from all meta data providers.
     */
    suspend fun allAnime(): List<Anime>

    /**
     * Retrieve all DCS entries.
     * @since 1.0.0
     * @return All DCS entries from all meta data providers.
     */
    suspend fun allDcsEntries(): List<DownloadControlStateEntry>

    /**
     * Removed the DCS file for a dead entry.
     * @since 1.0.0
     * @param id Id of the anime as defined by the meta data provider.
     * @param metaDataProviderConfig Configuration for a specific meta data provider.
     * @see MergeLockAccess.removeEntry
     */
    suspend fun removeDeadEntry(id: AnimeId, metaDataProviderConfig: MetaDataProviderConfig)

    /**
     * Handles everything that needs to be done if an anime has changed its ID.
     * @since 1.0.0
     * @param oldId ID which has been used so far to identify the anime.
     * @param newId New ID which is used by the meta data provider.
     * @param metaDataProviderConfig Configuration for a specific meta data provider.
     * @return Instance of the DCS file with the new ID.
     * @see Config.canChangeAnimeIds
     * @see MergeLockAccess.replaceUri
     */
    suspend fun changeId(oldId: AnimeId, newId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig): RegularFile
}