package io.github.manamiproject.modb.app.dataset

import io.github.manamiproject.modb.animecountdown.AnimeCountdownConfig
import io.github.manamiproject.modb.core.anime.AnimeRaw
import io.github.manamiproject.modb.simkl.SimklConfig

object AnimeCountdownUrlAdder {

    fun addAnimeCountdown(list: List<AnimeRaw>): List<AnimeRaw> {
        return list.map { anime ->
            val sources = anime.sources.filter { source -> source.host == SimklConfig.hostname() }
                .map { SimklConfig.extractAnimeId(it) }
                .map { AnimeCountdownConfig.buildAnimeLink(it) }
            val relatedAnime = anime.relatedAnime.filter { source -> source.host == SimklConfig.hostname() }
                .map { SimklConfig.extractAnimeId(it) }
                .map { AnimeCountdownConfig.buildAnimeLink(it) }

            anime.addSources(sources)
            anime.addRelatedAnime(relatedAnime)
        }
    }
}