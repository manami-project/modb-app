package io.github.manamiproject.modb.app.crawlers.notify

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.notify.NotifyConfig
import java.net.URI

/**
 * Configuration for creating the id range for notify.moe
 * @since 1.0.0
 */
object NotifyIdRangeSelectorConfig: MetaDataProviderConfig by NotifyConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}/explore/anime/any/any/any/$id")
}