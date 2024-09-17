package io.github.manamiproject.modb.app.crawler.myanimelist

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import java.net.URI

object MyanimelistHighestIdDetectorConfig: MetaDataProviderConfig by MyanimelistConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}/anime.php?o=9&c%5B0%5D=a&c%5B1%5D=d&cv=2&w=1")
}