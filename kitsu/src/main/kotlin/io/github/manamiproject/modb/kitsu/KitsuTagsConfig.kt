package io.github.manamiproject.modb.kitsu

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import java.net.URI

/**
 * Configuration for downloading related anime from kitsu.app
 * @since 1.0.0
 */
public object KitsuTagsConfig : MetaDataProviderConfig by KitsuConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}/api/edge/anime/$id/categories")
}