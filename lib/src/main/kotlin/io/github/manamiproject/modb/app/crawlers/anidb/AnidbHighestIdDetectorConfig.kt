package io.github.manamiproject.modb.app.crawlers.anidb

import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import java.net.URI

/**
 * Configuration for detecting the highest id on anidb.net
 * @since 1.0.0
 */
object AnidbHighestIdDetectorConfig: MetaDataProviderConfig by AnidbConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}/latest/anime")
}