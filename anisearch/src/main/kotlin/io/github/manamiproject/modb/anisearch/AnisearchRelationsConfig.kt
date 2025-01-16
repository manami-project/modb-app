package io.github.manamiproject.modb.anisearch

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import java.net.URI

/**
 * Configuration for downloading tags from anisearch.com
 * @since 1.0.0
 */
public object AnisearchRelationsConfig : MetaDataProviderConfig by AnisearchConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}/anime/$id/relations")
}