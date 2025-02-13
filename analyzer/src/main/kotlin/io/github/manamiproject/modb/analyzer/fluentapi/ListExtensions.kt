package io.github.manamiproject.modb.analyzer.fluentapi

import io.github.manamiproject.modb.app.dataset.AnimeCountdownUrlAdder
import io.github.manamiproject.modb.app.dataset.DefaultDatasetFileAccessor
import io.github.manamiproject.modb.app.merging.DefaultMergingService
import io.github.manamiproject.modb.app.readme.DefaultReadmeCreator
import io.github.manamiproject.modb.app.relatedanime.DefaultUnknownRelatedAnimeRemover
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeRaw
import io.github.manamiproject.modb.core.anime.DefaultAnimeRawToAnimeTransformer

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
internal fun List<AnimeRaw>.addAnimeCountdown(): List<AnimeRaw> = AnimeCountdownUrlAdder.addAnimeCountdown(this)

@KoverIgnore
internal fun List<AnimeRaw>.removeUnknownEntriesFromRelatedAnime(): List<AnimeRaw> = DefaultUnknownRelatedAnimeRemover.instance.removeRelatedAnimeWhichHaveNoCorrespondingEntryInSources(this)

@KoverIgnore
internal suspend fun List<AnimeRaw>.mergeAnime(): List<AnimeRaw> = DefaultMergingService.instance.merge(this)

@KoverIgnore
internal fun List<AnimeRaw>.transformToDatasetEntries(): List<Anime> = this.map { DefaultAnimeRawToAnimeTransformer.instance.transform(it) }