package io.github.manamiproject.modb.animecountdown

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import java.net.URI

public object AnimeCountdownConfig: MetaDataProviderConfig {

    override fun fileSuffix(): FileSuffix  = "html"

    override fun hostname(): Hostname = "animecountdown.com"

    override fun buildAnimeLink(id: AnimeId): URI = URI("https://${hostname()}/$id")
}