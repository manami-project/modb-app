package io.github.manamiproject

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import java.net.URI

/**
 * Configuration for downloading and converting anime data from animenewsnetwork.com
 * @since 1.0.0
 */
public object AnimenewsnetworkConfig: MetaDataProviderConfig {

    override fun hostname(): Hostname = "animenewsnetwork.com"

    override fun buildAnimeLink(id: AnimeId): URI = URI("https://www.${hostname()}/encyclopedia/anime.php?id=$id")

    override fun fileSuffix(): FileSuffix = "html"
}