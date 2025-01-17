package io.github.manamiproject.modb.livechart

import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig

/**
 * Configuration for downloading and converting anime data from livechart.me
 * @since 1.0.0
 */
public object LivechartConfig: MetaDataProviderConfig {

    override fun fileSuffix(): FileSuffix = "html"

    override fun hostname(): Hostname = "livechart.me"
}