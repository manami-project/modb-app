package io.github.manamiproject.modb.app.crawler.livechart

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.livechart.LivechartConfig
import java.net.URI

object LivechartNewestYearDetectorConfig: MetaDataProviderConfig by LivechartConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}/charts")
}