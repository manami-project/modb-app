package io.github.manamiproject.modb.analyzer.fluentapi

import io.github.manamiproject.modb.app.dataset.AnimeCountdownUrlAdder
import io.github.manamiproject.modb.app.dataset.DefaultDatasetFileAccessor
import io.github.manamiproject.modb.app.merging.DefaultMergingService
import io.github.manamiproject.modb.app.readme.DefaultReadmeCreator
import io.github.manamiproject.modb.app.relatedanime.DefaultUnknownRelatedAnimeRemover
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.models.Anime

@KoverIgnore
internal suspend fun List<Anime>.updateStatistics(): List<Anime> {
    DefaultReadmeCreator.instance.updateWith(this)
    return this
}

@KoverIgnore
internal suspend fun List<Anime>.saveToDataset(): List<Anime> {
    DefaultDatasetFileAccessor.instance.saveEntries(this)
    return this
}

@KoverIgnore
internal fun List<Anime>.addAnimeCountdown(): List<Anime> = AnimeCountdownUrlAdder.addAnimeCountdown(this)

@KoverIgnore
internal fun List<Anime>.removeUnknownEntriesFromRelatedAnime(): List<Anime> = DefaultUnknownRelatedAnimeRemover.instance.removeRelatedAnimeWhichHaveNoCorrespondingEntryInSources(this)

@KoverIgnore
internal suspend fun List<Anime>.mergeAnime(): List<Anime> = DefaultMergingService.instance.merge(this)