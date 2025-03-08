package io.github.manamiproject.modb.app.crawlers.animenewsnetwork

import io.github.manamiproject.AnimenewsnetworkConfig
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import java.net.URI

/**
 * Configuration for creating the id range for animenewsnetwork.com
 * @since 1.6.0
 */
object AnimenewsnetworkPaginationIdRangeSelectorConfig: MetaDataProviderConfig by AnimenewsnetworkConfig{

    override fun buildDataDownloadLink(id: String): URI = URI("https://www.${hostname()}/encyclopedia/anime.php?list=$id")
}