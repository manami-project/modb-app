package io.github.manamiproject.modb.kitsu

import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import java.net.URI

/**
 * Configuration for downloading and converting anime data from kitsu.app
 * @since 1.0.0
 */
public object KitsuConfig : MetaDataProviderConfig {

    override fun hostname(): Hostname = "kitsu.app"

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}/api/edge/anime?fields[categories]=title&fields[genres]=name&fields[producers]=name&filter[id]=${id}&include=categories,genres,animeProductions.producer,mediaRelationships.destination")

    override fun fileSuffix(): FileSuffix = "json"
}
