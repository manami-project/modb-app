package io.github.manamiproject.modb.anidb

import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig

/**
 * Configuration for downloading and converting anime data from anidb.net
 * @since 1.0.0
 */
public object AnidbConfig : MetaDataProviderConfig {

    override fun hostname(): Hostname = "anidb.net"

    override fun fileSuffix(): FileSuffix = "html"
}
