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

    suspend fun reprocess(save: Boolean) {
        DefaultDownloadControlStateAccessor.instance.allAnime()
            .alertDeletedAnimeByTitle()
            .mergeAnime()
            .removeUnknownEntriesFromRelatedAnime()
            .addAnimeCountdown()
            .transformToDatasetEntries()
            .also {
                if (save) {
                    it.saveToDataset()
                }
            }
            .updateStatistics()

        if (save) {
            ZstandardFilesForDeadEntriesCreatorPostProcessor.instance.process()
        }

        listOf(
            NoLockFilesLeftValidationPostProcessor.instance,
            DownloadControlStateWeeksValidationPostProcessor.instance,
            StudiosAndProducersExtractionChecker.instance,
            DuplicatesValidationPostProcessor.instance,
            DeadEntriesValidationPostProcessor.instance,
            SourcesConsistencyValidationPostProcessor.instance,
            NumberOfEntriesValidationPostProcessor.instance,
            FileSizePlausibilityValidationPostProcessor.instance,
            DeleteOldDownloadDirectoriesPostProcessor.instance,
            // Doesn't need ReleaseInfoFileCreatorPostProcessor, because reprocessing using analyzer never creates weekly updates, but delta updates.
        ).forEach { it.process() }
    }
}