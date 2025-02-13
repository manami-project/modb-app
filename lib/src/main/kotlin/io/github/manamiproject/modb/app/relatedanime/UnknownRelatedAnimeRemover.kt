package io.github.manamiproject.modb.app.relatedanime

import io.github.manamiproject.modb.core.anime.AnimeRaw

/**
 * Removes unknown entries in [AnimeRaw.relatedAnime].
 * @since 1.0.0
 */
interface UnknownRelatedAnimeRemover {

    /**
     * Removes entries in [AnimeRaw.relatedAnime] if they don't exist in any [AnimeRaw.sources] of [allEntries].
     * @since 1.0.0
     * @param allEntries All entries of the anime dataset.
     * @return List of anime with cleaned-up related anime.
     */
    fun removeRelatedAnimeWhichHaveNoCorrespondingEntryInSources(allEntries: List<AnimeRaw>): List<AnimeRaw>
}