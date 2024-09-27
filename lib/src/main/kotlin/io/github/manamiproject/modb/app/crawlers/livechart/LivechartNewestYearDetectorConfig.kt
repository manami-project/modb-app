package io.github.manamiproject.modb.app.crawlers.livechart

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.livechart.LivechartConfig
import java.net.URI

/**
 * Configuration for detecting the newest year on livechart.me
 * @since 1.0.0
 */
object LivechartNewestYearDetectorConfig: MetaDataProviderConfig by LivechartConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}/charts")
}