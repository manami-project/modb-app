package io.github.manamiproject.modb.app.crawlers.simkl

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.simkl.SimklConfig
import java.net.URI

object SimklPaginationIdRangeSelectorConfig: MetaDataProviderConfig by SimklConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}/ajax/full/anime.php")
}