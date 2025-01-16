package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import java.net.URI

/**
 * Configuration for retrieving the CSRF token
 * @since 1.0.0
 */
public object AnilistDefaultTokenRetrieverConfig : MetaDataProviderConfig by AnilistConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}")
}