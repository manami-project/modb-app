package io.github.manamiproject.modb.animeplanet

import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig

/**
 * Configuration for downloading and converting anime data from anime-planet.com
 * @since 1.0.0
 */
public object AnimePlanetConfig : MetaDataProviderConfig {

    override fun fileSuffix(): FileSuffix = "html"

    override fun hostname(): Hostname = "anime-planet.com"
}