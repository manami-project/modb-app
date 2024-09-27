package io.github.manamiproject.modb.app.crawlers.livechart

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.livechart.LivechartConfig
import java.net.URI

/**
 * Configuration for creating the id range for livechart.me
 * @since 1.0.0
 */
object LivechartPaginationIdRangeSelectorConfig: MetaDataProviderConfig by LivechartConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}/$id/all")
}