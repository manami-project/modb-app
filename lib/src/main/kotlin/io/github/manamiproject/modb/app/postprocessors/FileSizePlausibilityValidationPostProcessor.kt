package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.dataset.DatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DatasetFileType.*
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.dataset.DefaultDatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DefaultDeadEntriesAccessor
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.core.logging.LoggerDelegate
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

        val json = datasetFileAccessor.offlineDatabaseFile(JSON)
        check(json.regularFileExists()) { "Dataset *.json file doesn't exist." }
        val jsonSize = json.fileSize()

        val jsonMinified = datasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED)
        check(jsonMinified.regularFileExists()) { "Dataset *-minified.json file doesn't exist." }
        val jsonMinifiedSize = jsonMinified.fileSize()

        val zip = datasetFileAccessor.offlineDatabaseFile(ZIP)
        check(zip.regularFileExists()) { "Dataset *.zip file doesn't exist." }
        val zipSize = zip.fileSize()

        check(jsonMinifiedSize in (zipSize + 1)..<jsonSize) { "File sizes for dataset are not plausible: [json=$jsonSize, jsonMinified=$jsonMinifiedSize, zip=$zipSize]" }

        log.info { "Checking that sizes of the dead entries files are plausible." }

        appConfig.metaDataProviderConfigurations()
            .filter { appConfig.deadEntriesSupported(it) }
            .forEach { currentConfig ->
                val deadEntriesJson = deadEntriesAccessor.deadEntriesFile(currentConfig, JSON)
                check(deadEntriesJson.regularFileExists()) { "Dead entries *.json file for [${currentConfig.hostname()}] doesn't exist." }
                val deadEntriesJsonSize = deadEntriesJson.fileSize()

                val deadEntriesJsonMinified = deadEntriesAccessor.deadEntriesFile(currentConfig, JSON_MINIFIED)
                check(deadEntriesJsonMinified.regularFileExists()) { "Dead entries *-minified.json file for [${currentConfig.hostname()}] doesn't exist." }
                val deadEntriesJsonMinifiedSize = deadEntriesJsonMinified.fileSize()

                val deadEntriesZip = deadEntriesAccessor.deadEntriesFile(currentConfig, ZIP)
                check(deadEntriesZip.regularFileExists()) { "Dead entries *.zip file for [${currentConfig.hostname()}] doesn't exist." }
                val deadEntriesZipSize = deadEntriesZip.fileSize()

                check(deadEntriesJsonMinifiedSize in (deadEntriesZipSize + 1)..<deadEntriesJsonSize) {
                    "File sizes for dead entry files of [${currentConfig.hostname()}] are not plausible: [json=$deadEntriesJsonSize, jsonMinified=$deadEntriesJsonMinifiedSize, zip=$deadEntriesZipSize]"
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