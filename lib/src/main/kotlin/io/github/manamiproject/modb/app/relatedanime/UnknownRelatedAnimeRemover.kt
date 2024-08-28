package io.github.manamiproject.modb.app.relatedanime

import io.github.manamiproject.modb.core.models.Anime

/**
 * Removes unknown entries in [Anime.relatedAnime].
 * @since 1.0.0
 */
interface UnknownRelatedAnimeRemover {

    /**
     * Removes entries in [Anime.relatedAnime] if they don't exist in any [Anime.sources] of [allEntries].
     * @since 1.0.0
     * @param allEntries All entries of the anime dataset.
     * @return List of anime with cleaned-up related anime.
     */
    fun removeRelatedAnimeWhichHaveNoCorrespondingEntryInSources(allEntries: List<Anime>): List<Anime>
}