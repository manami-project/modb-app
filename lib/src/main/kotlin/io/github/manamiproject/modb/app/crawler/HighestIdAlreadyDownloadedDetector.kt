package io.github.manamiproject.modb.app.crawler

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig

/**
 * Retrieves the highest ID already downloaded.
 * @since 1.0.0
 */
interface HighestIdAlreadyDownloadedDetector {

    /**
     * Retrieves the highest ID already downloaded for a specific meta data provider.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific meta data provider.
     * @return Either `0` if there are no anime for the given [metaDataProviderConfig] or if the meta data provider doesn't use integer for animd IDs. Otherwise it returns the highest ID already downloaded.
     */
    suspend fun detectHighestIdAlreadyDownloaded(metaDataProviderConfig: MetaDataProviderConfig): Int
}