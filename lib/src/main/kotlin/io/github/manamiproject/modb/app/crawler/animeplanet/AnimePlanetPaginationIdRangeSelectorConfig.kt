package io.github.manamiproject.modb.app.crawler.animeplanet

import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import java.net.URI

/**
 * Configuration for creating the id range for anime-planet.com
 * @since 1.0.0
 */
object AnimePlanetPaginationIdRangeSelectorConfig: MetaDataProviderConfig by AnimePlanetConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}/anime/all?sort=title&order=asc&page=$id&bvm=list")
}