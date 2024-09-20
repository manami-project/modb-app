package io.github.manamiproject.modb.app.crawler.anisearch

import io.github.manamiproject.modb.anisearch.AnisearchConfig
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import java.net.URI

/**
 * Configuration for detecting the highest id on anisearch.com
 * @since 1.0.0
 */
object AnisearchLastPageDetectorConfig: MetaDataProviderConfig by AnisearchConfig {

    override fun buildDataDownloadLink(id: String): URI = AnisearchIdRangeSelectorConfig.buildDataDownloadLink("1")
}