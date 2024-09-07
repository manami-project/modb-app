package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.dataset.DatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.dataset.DefaultDatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DefaultDeadEntriesAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DOWNLOAD_CONTROL_STATE_FILE_SUFFIX
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.app.merging.lock.DefaultMergeLockAccessor
import io.github.manamiproject.modb.app.merging.lock.MergeLockAccessor
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import java.net.URI

/**
 * Checks dataset, dcs files and merge locks for dead entries.
 * @since 1.0.0
 * @property datasetFileAccessor Access to dataset files.
 * @property deadEntriesAccessor Access to dead entries files.
 * @property downloadControlStateAccessor Access to DCS files.
 * @property mergeLockAccess Access to merge locks.
 * @throws IllegalStateException if dead entries are found.
 */
class DeadEntriesValidationPostProcessor(
    private val datasetFileAccessor: DatasetFileAccessor = DefaultDatasetFileAccessor.instance,
    private val deadEntriesAccessor: DeadEntriesAccessor = DefaultDeadEntriesAccessor.instance,
    private val downloadControlStateAccessor: DownloadControlStateAccessor = DefaultDownloadControlStateAccessor.instance,
    private val mergeLockAccess: MergeLockAccessor = DefaultMergeLockAccessor.instance,
): PostProcessor {

    override suspend fun process(): Boolean {
        log.info { "Checking if [*.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX] files contain dead entries." }
        checkForDeadEntries(downloadControlStateAccessor.allAnime().flatMap { it.sources })

        log.info { "Checking if a merge lock contains dead entries." }
        checkForDeadEntries(mergeLockAccess.allSourcesInAllMergeLockEntries())

        log.info { "Checking if dataset contains dead entries." }
        checkForDeadEntries(datasetFileAccessor.fetchEntries().flatMap { it.sources })

        return true
    }

    private suspend fun checkForDeadEntries(list: Collection<URI>) {
        val allDeadEntries = deadEntriesAccessor.determineDeadEntries(list)
        val deadEntriesInList = list.intersect(allDeadEntries)

        check(deadEntriesInList.isEmpty()) { "Dead entries found: [${deadEntriesInList.joinToString(", ")}]" }
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DeadEntriesValidationPostProcessor]
         * @since 1.0.0
         */
        val instance: DeadEntriesValidationPostProcessor by lazy { DeadEntriesValidationPostProcessor() }
    }
}