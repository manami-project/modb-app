package io.github.manamiproject.modb.app.crawlers.notify

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.notify.NotifyConfig
import java.net.URI

/**
 * Configuration for downloading the whole dataset as an alternative to the default crawler notify.moe
 * @since 1.2.0
 */
object NotifyDatasetDownloaderConfig: MetaDataProviderConfig by NotifyConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://notify.moe/api/types/Anime/download")
}