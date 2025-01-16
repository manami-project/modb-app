package io.github.manamiproject.modb.anisearch

import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig

/**
 * Configuration for downloading and converting anime data from anissearch.com
 * @since 1.0.0
 */
public object AnisearchConfig : MetaDataProviderConfig {

    override fun hostname(): Hostname = "anisearch.com"

    override fun fileSuffix(): FileSuffix = "html"
}