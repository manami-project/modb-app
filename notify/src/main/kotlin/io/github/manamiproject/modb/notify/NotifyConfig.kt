package io.github.manamiproject.modb.notify

import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import java.net.URI

/**
 * Configuration for downloading anime data from notify.moe
 * @since 1.0.0
 */
public object NotifyConfig : MetaDataProviderConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}/api/anime/$id")

    override fun hostname(): Hostname = "notify.moe"

    override fun fileSuffix(): FileSuffix = "json"
}
