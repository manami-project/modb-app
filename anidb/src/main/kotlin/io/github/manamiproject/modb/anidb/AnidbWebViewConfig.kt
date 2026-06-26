package io.github.manamiproject.modb.anidb

import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig

/**
 * Configuration for downloading HTML from the webview of anidb.net
 * @since 8.0.0
 */
public object AnidbWebViewConfig : MetaDataProviderConfig {

    override fun hostname(): Hostname = "anidb.net"

    override fun fileSuffix(): FileSuffix = "html"
}
