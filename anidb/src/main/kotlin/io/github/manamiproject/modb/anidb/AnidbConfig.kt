package io.github.manamiproject.modb.anidb

import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import java.net.URI

/**
 * Configuration for downloading and converting anime data from anidb.net
 * @since 1.0.0
 */
public object AnidbConfig : MetaDataProviderConfig {

    override fun hostname(): Hostname = "anidb.net"

    override fun fileSuffix(): FileSuffix = "xml"

    override fun buildDataDownloadLink(id: String): URI = URI("http://api.${hostname()}:9001/httpapi?request=anime&client=mediabrowser&clientver=1&protover=1&aid=$id")
}
