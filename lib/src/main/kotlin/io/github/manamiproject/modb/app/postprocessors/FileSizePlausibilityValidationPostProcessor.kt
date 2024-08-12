package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.dataset.DatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DatasetFileType.*
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.dataset.DefaultDatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DefaultDeadEntriesAccessor
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlin.io.path.exists
import kotlin.io.path.fileSize

/**
 * Checks if the file sizes of the different file types are plausible.
 * It is expected that the file size is as follows: json > minified json > zip
 * @since 1.0.0
 * @param appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property datasetFileAccessor Access to dataset files.
 * @property deadEntriesAccessor Access to dead entries files.
 * @throws IllegalStateException if file sizes don't make sense.
 */
class FileSizePlausibilityValidationPostProcessor(
    private val appConfig: Config = AppConfig.instance,
    private val datasetFileAccessor: DatasetFileAccessor = DefaultDatasetFileAccessor.instance,
    private val deadEntriesAccessor: DeadEntriesAccessor = DefaultDeadEntriesAccessor.instance,
): PostProcessor {

    override suspend fun process(): Boolean {
        log.info { "Checking that sizes of the dataset files are plausible." }

        val json = datasetFileAccessor.offlineDatabaseFile(JSON).fileSize()
        val jsonMinified = datasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED).fileSize()
        val zip = datasetFileAccessor.offlineDatabaseFile(ZIP).fileSize()

        check(jsonMinified in (zip + 1)..<json) { "File sizes for dataset are not plausible: [json=$json, jsonMinified=$jsonMinified, zip=$zip]" }

        log.info { "Checking that sizes of the dead entries files are plausible." }

        appConfig.metaDataProviderConfigurations()
            .filter { appConfig.deadEntriesSupported(it) }
            .forEach { currentConfig ->
                if (!deadEntriesAccessor.deadEntriesFile(currentConfig, JSON).exists()) {
                    return@forEach
                }

                val deadEntriesJson = deadEntriesAccessor.deadEntriesFile(currentConfig, JSON).fileSize()
                val deadEntriesJsonMinified = deadEntriesAccessor.deadEntriesFile(currentConfig, JSON_MINIFIED).fileSize()
                val deadEntriesZip = deadEntriesAccessor.deadEntriesFile(currentConfig, ZIP).fileSize()

                check(deadEntriesJsonMinified in (deadEntriesZip + 1)..<deadEntriesJson) {
                    "File sizes for dead entry files of [${currentConfig.hostname()}] are not plausible: [json=$deadEntriesJson, jsonMinified=$deadEntriesJsonMinified, zip=$deadEntriesZip]"
                }
            }

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