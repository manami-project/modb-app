package io.github.manamiproject.modb.app.relatedanime

import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.anime.AnimeRaw

/**
 * This implementation doesn't use dead entries to check if entries need to be removed, but checks the data within the
 * dataset. A related anime is only acceptable if a corresponding entry exists in any `sources` of any anime in the
 * dataset.
 * @since 1.0.0
 */
class DefaultUnknownRelatedAnimeRemover: UnknownRelatedAnimeRemover {

    override fun removeRelatedAnimeWhichHaveNoCorrespondingEntryInSources(allEntries: List<AnimeRaw>): List<AnimeRaw> {
        log.info { "Removing related anime which are unknown." }

        val allSources = allEntries.flatMap { it.sources }.toHashSet()

        allEntries.map { anime ->
            anime.removeRelatedAnimeIf { uri -> !allSources.contains(uri) }
        }

        return allEntries
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DefaultUnknownRelatedAnimeRemover]
         * @since 1.0.0
         */
        val instance: DefaultUnknownRelatedAnimeRemover by lazy { DefaultUnknownRelatedAnimeRemover() }
    }
}