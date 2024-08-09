package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.dataset.DatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DatasetFileType.*
import io.github.manamiproject.modb.app.dataset.DefaultDatasetFileAccessor
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlin.io.path.fileSize

/**
 * Checks if the file sizes of the different file types are plausible.
 * It is expected that the file size is as follows: json > minified json > zip
 * @since 1.0.0
 * @property datasetFileAccessor Access to dataset files.
 * @throws IllegalStateException if file sizes don't make sense.
 */
class FileSizePlausibilityValidationPostProcessor(
    private val datasetFileAccessor: DatasetFileAccessor = DefaultDatasetFileAccessor.instance,
): PostProcessor {

    override suspend fun process(): Boolean {
        log.info { "Checking that sizes of the dataset files are plausible." }

        val json = datasetFileAccessor.offlineDatabaseFile(JSON).fileSize()
        val jsonMinified = datasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED).fileSize()
        val zip = datasetFileAccessor.offlineDatabaseFile(ZIP).fileSize()

        check(jsonMinified in (zip + 1)..<json) { "File sizes for dataset are not plausible: [json=$json, jsonMinified=$jsonMinified, zip=$zip]" }

        return true
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [FileSizePlausibilityValidationPostProcessor]
         * @since 1.0.0
         */
        val instance: FileSizePlausibilityValidationPostProcessor by lazy { FileSizePlausibilityValidationPostProcessor() }
    }
}