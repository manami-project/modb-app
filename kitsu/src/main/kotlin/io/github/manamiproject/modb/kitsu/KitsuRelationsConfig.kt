package io.github.manamiproject.modb.kitsu

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import java.net.URI

/**
 * Configuration for downloading tags from kitsu.app
 * @since 1.0.0
 */
public object KitsuRelationsConfig : MetaDataProviderConfig by KitsuConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}/api/edge/media-relationships?filter[source_id]=$id&filter[source_type]=Anime&include=destination&sort=role")
}
