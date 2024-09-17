package io.github.manamiproject.modb.app.crawler.anisearch

import io.github.manamiproject.modb.anisearch.AnisearchConfig
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import java.net.URI

object AnisearchIdRangeSelectorConfig: MetaDataProviderConfig by AnisearchConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}/anime/index/page-$id?char=all&sort=title&order=asc&view=2&limit=100&title=&titlex=")
}