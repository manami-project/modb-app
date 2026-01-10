package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.merging.lock.MergeLockAccessor
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.anime.AnimeRaw

/**
 * File suffix used for the files containing the serialized [DownloadControlStateEntry] object.
 * @since 1.0.0
 */
const val DOWNLOAD_CONTROL_STATE_FILE_SUFFIX = "dcs"

/**
 * Allows access to DCS files.
 * @since 1.0.0
 */
interface DownloadControlStateAccessor {

    /**
     * Retrieve the metadata provider specific DCS file directory.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific metadata provider.
     * @return Directory in which the DCS files are stored for the given [metaDataProviderConfig].
     */
    fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory

    /**
     * Return all [AnimeRaw] from DCS files.
     * @since 1.0.0
     * @return All anime from all metadata providers.
     */
    suspend fun allAnime(): List<AnimeRaw>

    /**
     * Retrieve all DCS entries.
     * @since 1.0.0
     * @return All DCS entries from all metadata providers.
     */
    suspend fun allDcsEntries(): List<DownloadControlStateEntry>

    /**
     * Return all [AnimeRaw] from DCS files of a specific metadata provider.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific metadata provider.
     * @return All anime of a specific metadata provider.
     */
    suspend fun allAnime(metaDataProviderConfig: MetaDataProviderConfig): List<AnimeRaw>

    /**
     * Retrieve all DCS entries of a specific metadata provider.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific metadata provider.
     * @return All DCS entries of a specific metadata provider.
     */
    suspend fun allDcsEntries(metaDataProviderConfig: MetaDataProviderConfig): List<DownloadControlStateEntry>

    /**
     * Checks if a [DownloadControlStateEntry] already exists.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific metadata provider.
     * @param animeId ID of the anime as defined by the metadata provider.
     * @return `true` if a DCS file already exists for this id and metadata provider.
     * @see dcsEntry
     */
    suspend fun dcsEntryExists(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): Boolean

    /**
     * Retrieve a specific DCS entry. In order to prevent an exception check if the entry exists using [dcsEntryExists].
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific metadata provider.
     * @param animeId ID of the anime as defined by the metadata provider.
     * @return The requested DCS entry.
     * @throws IllegalStateException if the requested DCS entry doesn't exist.
     * @see dcsEntryExists
     */
    suspend fun dcsEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): DownloadControlStateEntry

    /**
     * Either creates a new DCS entry or updates an existing one.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific metadata provider.
     * @param animeId ID of the anime as defined by the metadata provider.
     * @param downloadControlStateEntry Entry to be created or updated.
     */
    suspend fun createOrUpdate(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId, downloadControlStateEntry: DownloadControlStateEntry): Boolean

    /**
     * Removed the DCS file for a dead entry.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific metadata provider.
     * @param animeId ID of the anime as defined by the metadata provider.
     * @see MergeLockAccessor.removeEntry
     */
    suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId)

    /**
     * Handles everything that needs to be done if an anime has changed its ID.
     * @since 1.0.0
     * @param oldId ID which has been used so far to identify the anime.
     * @param newId New ID which is used by the metadata provider.
     * @param metaDataProviderConfig Configuration for a specific metadata provider.
     * @return Instance of the DCS file with the new ID.
     * @see Config.canChangeAnimeIds
     * @see MergeLockAccessor.replaceUri
     */
    suspend fun changeId(oldId: AnimeId, newId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig): RegularFile

    /**
     * Retrieves the highest ID already in dataset for a specific metadata provider.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific metadata provider.
     * @return Either `0` if there are no anime for the given [metaDataProviderConfig] or if the metadata provider doesn't use integer for anime IDs. Otherwise, it returns the highest ID already downloaded.
     */
    suspend fun highestIdAlreadyInDataset(metaDataProviderConfig: MetaDataProviderConfig): Int
}