package io.github.manamiproject.modb.simkl

import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig

/**
 * Configuration for downloading and converting anime data from simkl.com
 * @since 1.0.0
 */
public object SimklConfig: MetaDataProviderConfig {

    override fun fileSuffix(): FileSuffix = "html"

    override fun hostname(): Hostname = "simkl.com"
}