package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.Directory
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
     * @param metaDataProviderConfig
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
}