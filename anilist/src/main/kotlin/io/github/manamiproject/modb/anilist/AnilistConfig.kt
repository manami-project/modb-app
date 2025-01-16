package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import java.net.URI

/**
 * Configuration for downloading and converting anime data from anilist.co
 * @since 1.0.0
 */
public object AnilistConfig : MetaDataProviderConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}/graphql")

    override fun hostname(): Hostname = "anilist.co"

    override fun fileSuffix(): FileSuffix = "json"
}