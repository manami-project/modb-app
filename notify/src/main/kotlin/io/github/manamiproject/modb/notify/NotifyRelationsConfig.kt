package io.github.manamiproject.modb.notify

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import java.net.URI

/**
 * Configuration for downloading related anime from notify.moe.
 * @since 1.0.0
 */
public object NotifyRelationsConfig : MetaDataProviderConfig by NotifyConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://notify.moe/api/animerelations/$id")
}
