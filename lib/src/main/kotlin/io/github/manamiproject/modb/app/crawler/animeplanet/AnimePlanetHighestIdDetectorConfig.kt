package io.github.manamiproject.modb.app.crawler.animeplanet

import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import java.net.URI

/**
 * Configuration for detecting the highest id on anime-planet.com
 * @since 1.0.0
 */
object AnimePlanetHighestIdDetectorConfig: MetaDataProviderConfig by AnimePlanetConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}/anime/all")
}