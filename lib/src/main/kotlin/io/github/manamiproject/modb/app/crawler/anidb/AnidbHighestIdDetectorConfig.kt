package io.github.manamiproject.modb.app.crawler.anidb

import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import java.net.URI

object AnidbHighestIdDetectorConfig: MetaDataProviderConfig by AnidbConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}/latest/anime")
}