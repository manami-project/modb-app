package io.github.manamiproject.modb.app.processors.postprocessors

import io.github.manamiproject.modb.app.dataset.DatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DefaultDatasetFileAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DOWNLOAD_CONTROL_STATE_FILE_SUFFIX
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.app.extensions.findDuplicates
import io.github.manamiproject.modb.core.logging.LoggerDelegate

/**
 * Checks DCS files and dataset for duplicates.
 * The class checks whole entries on the level of a whole object as well as sources.
 * @since 1.0.0
 * @property downloadControlStateAccessor Access to DCS files.
 * @property datasetFileAccessor Access to dataset files.
 * @throws IllegalStateException if duplicates are found.
 */
class DuplicatesValidationPostProcessor(
    private val downloadControlStateAccessor: DownloadControlStateAccessor = DefaultDownloadControlStateAccessor.instance,
    private val datasetFileAccessor: DatasetFileAccessor = DefaultDatasetFileAccessor.instance,
): PostProcessor {

    override suspend fun process(): Boolean {
        log.info { "Checking [*.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX] for duplicates." }

        val dcsEntries = downloadControlStateAccessor.allDcsEntries()
        check(dcsEntries.isNotEmpty()) { "No DCS entries found." }

        val dcsDuplicates = dcsEntries.findDuplicates()
        check(dcsDuplicates.isEmpty()) { "Found duplicates in DCS files: $dcsDuplicates" }

        val dcsAnimeDuplicates = dcsEntries.map { it.anime }.findDuplicates()
        check(dcsAnimeDuplicates.isEmpty()) { "Found duplicates in anime entries of DCS files: $dcsAnimeDuplicates" }

        log.info { "Checking dataset for duplicates." }

        val allDatasetEntries = datasetFileAccessor.fetchEntries()
        check(allDatasetEntries.isNotEmpty()) { "No dataset entries found." }

        val animeDuplicates = allDatasetEntries.findDuplicates()
        check(animeDuplicates.isEmpty()) { "Found duplicates in dataset entries: $animeDuplicates" }

        val animeSourcesDuplicates = allDatasetEntries.flatMap { it.sources }.findDuplicates()
        check(animeSourcesDuplicates.isEmpty()) { "Found duplicates sources of dataset: $animeSourcesDuplicates" }

        return true
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DuplicatesValidationPostProcessor]
         * @since 1.0.0
         */
        val instance: DuplicatesValidationPostProcessor by lazy { DuplicatesValidationPostProcessor() }
    }
}