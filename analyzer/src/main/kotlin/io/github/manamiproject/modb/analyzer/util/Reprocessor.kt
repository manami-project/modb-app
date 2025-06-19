package io.github.manamiproject.modb.analyzer.util

import io.github.manamiproject.modb.analyzer.fluentapi.*
import io.github.manamiproject.modb.analyzer.fluentapi.mergeAnime
import io.github.manamiproject.modb.analyzer.fluentapi.removeUnknownEntriesFromRelatedAnime
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateAccessor
import io.github.manamiproject.modb.app.extensions.alertDeletedAnimeByTitle
import io.github.manamiproject.modb.app.postprocessors.*
import io.github.manamiproject.modb.core.coverage.KoverIgnore

@KoverIgnore
internal object Reprocessor {

    suspend fun reprocess() {
        DefaultDownloadControlStateAccessor.instance.allAnime()
            .alertDeletedAnimeByTitle()
            .mergeAnime()
            .removeUnknownEntriesFromRelatedAnime()
            .addAnimeCountdown()
            .transformToDatasetEntries()
            .saveToDataset()
            .updateStatistics()

        listOf(
            NoLockFilesLeftValidationPostProcessor.instance,
            DownloadControlStateWeeksValidationPostProcessor.instance,
            StudiosAndProducersExtractionChecker.instance,
            DuplicatesValidationPostProcessor.instance,
            ZstandardFilesForDeadEntriesCreatorPostProcessor.instance,
            DeadEntriesValidationPostProcessor.instance,
            SourcesConsistencyValidationPostProcessor.instance,
            NumberOfEntriesValidationPostProcessor.instance,
            FileSizePlausibilityValidationPostProcessor.instance,
            DeleteOldDownloadDirectoriesPostProcessor.instance,
        ).forEach { it.process() }
    }
}