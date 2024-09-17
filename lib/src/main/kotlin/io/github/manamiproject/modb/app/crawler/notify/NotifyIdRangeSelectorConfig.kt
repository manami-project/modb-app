package io.github.manamiproject.modb.app.crawler.notify

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.notify.NotifyConfig
import java.net.URI

object NotifyIdRangeSelectorConfig: MetaDataProviderConfig by NotifyConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}/explore/anime/any/any/any/$id")
}