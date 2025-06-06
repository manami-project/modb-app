package io.github.manamiproject.modb.app.crawlers.notify

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.notify.NotifyConfig
import java.net.URI

/**
 * Configuration for downloading the whole dataset of anime as an alternative to the default crawler notify.moe
 * @since 1.2.0
 */
object NotifyAnimeDatasetDownloaderConfig: MetaDataProviderConfig by NotifyConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}/api/types/Anime/download")
}