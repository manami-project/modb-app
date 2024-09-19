package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.merging.lock.MergeLockAccessor
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
     * Return all [Anime] from DCS files of a specific meta data provicer..
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific meta data provider.
     * @return All anime of a specific meta data provider.
     */
    suspend fun allAnime(metaDataProviderConfig: MetaDataProviderConfig): List<Anime>

    /**
     * Retrieve all DCS entries of a specific meta data provider.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific meta data provider.
     * @return All DCS entries of a specific meta data provider.
     */
    suspend fun allDcsEntries(metaDataProviderConfig: MetaDataProviderConfig): List<DownloadControlStateEntry>

    /**
     * Checks if a [DownloadControlStateEntry] already exists.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific meta data provider.
     * @param animeId Id of the anime as defined by the meta data provider.
     * @return `true` if a DCS file already exists for this id and meta data provider.
     * @see dcsEntry
     */
    suspend fun dcsEntryExists(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): Boolean

    /**
     * Retrieve a specific DCS entry. In order to prevent an exception check if the entry exists using [dcsEntryExists].
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific meta data provider.
     * @param animeId Id of the anime as defined by the meta data provider.
     * @return The requested DCS entry.
     * @throws IllegalStateException if the requested DCS entry doesn't exist.
     * @see dcsEntryExists
     */
    suspend fun dcsEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): DownloadControlStateEntry

    /**
     * Either creates a new DCS entry or updates an existing one.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific meta data provider.
     * @param animeId Id of the anime as defined by the meta data provider.
     * @param downloadControlStateEntry Entry to be created or updated.
     */
    suspend fun createOrUpdate(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId, downloadControlStateEntry: DownloadControlStateEntry): Boolean

    /**
     * Removed the DCS file for a dead entry.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific meta data provider.
     * @param animeId Id of the anime as defined by the meta data provider.
     * @see MergeLockAccessor.removeEntry
     */
    suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId)

    /**
     * Handles everything that needs to be done if an anime has changed its ID.
     * @since 1.0.0
     * @param oldId ID which has been used so far to identify the anime.
     * @param newId New ID which is used by the meta data provider.
     * @param metaDataProviderConfig Configuration for a specific meta data provider.
     * @return Instance of the DCS file with the new ID.
     * @see Config.canChangeAnimeIds
     * @see MergeLockAccessor.replaceUri
     */
    suspend fun changeId(oldId: AnimeId, newId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig): RegularFile
}