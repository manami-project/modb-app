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
 * It is expected that the file size is as follows: json > minified json > zstd compressed minified json
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

        val jsonPrettyPrint = datasetFileAccessor.offlineDatabaseFile(JSON_PRETTY_PRINT)
        check(jsonPrettyPrint.regularFileExists()) { "Dataset *.json file doesn't exist." }
        val jsonPrettyPrintSize = jsonPrettyPrint.fileSize()

        val jsonMinified = datasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED)
        check(jsonMinified.regularFileExists()) { "Dataset *-minified.json file doesn't exist." }
        val jsonMinifiedSize = jsonMinified.fileSize()

        val jsonMinifiedZst = datasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED_ZST)
        check(jsonMinifiedZst.regularFileExists()) { "Dataset *-minified.json.zst file doesn't exist." }
        val jsonMinifiedZstSize = jsonMinifiedZst.fileSize()

        check(jsonMinifiedZstSize < jsonMinifiedSize && jsonMinifiedSize < jsonPrettyPrintSize) { "File sizes for dataset are not plausible: [jsonPrettyPrint=$jsonPrettyPrintSize, jsonMinified=$jsonMinifiedSize, jsonMinifiedZst=$jsonMinifiedZstSize]" }

        val jsonLines = datasetFileAccessor.offlineDatabaseFile(JSON_LINES)
        check(jsonLines.regularFileExists()) { "Dataset *.jsonl file doesn't exist." }
        val jsonLinesSize = jsonLines.fileSize()

        val jsonLinesZst = datasetFileAccessor.offlineDatabaseFile(JSON_LINES_ZST)
        check(jsonLinesZst.regularFileExists()) { "Dataset *.jsonl.zst file doesn't exist." }
        val jsonLinesZstSize = jsonLinesZst.fileSize()

        check(jsonLinesZstSize < jsonLinesSize) { "File sizes for dataset are not plausible: [jsonLines=$jsonLinesSize, jsonLinesZst=$jsonLinesZstSize]" }
        check(jsonMinifiedSize < jsonLinesSize) { "File sizes for dataset are not plausible: [jsonMinified=$jsonMinifiedSize, jsonLines=$jsonLinesSize]" }

        log.info { "Checking that sizes of the dead entries files are plausible." }

        appConfig.metaDataProviderConfigurations()
            .filter { appConfig.deadEntriesSupported(it) }
            .forEach { currentConfig ->
                val deadEntriesJsonPrettyPrint = deadEntriesAccessor.deadEntriesFile(currentConfig, JSON_PRETTY_PRINT)
                check(deadEntriesJsonPrettyPrint.regularFileExists()) { "Dead entries *.json file for [${currentConfig.hostname()}] doesn't exist." }
                val deadEntriesJsonPrettyPrintSize = deadEntriesJsonPrettyPrint.fileSize()

                val deadEntriesJsonMinified = deadEntriesAccessor.deadEntriesFile(currentConfig, JSON_MINIFIED)
                check(deadEntriesJsonMinified.regularFileExists()) { "Dead entries *-minified.json file for [${currentConfig.hostname()}] doesn't exist." }
                val deadEntriesJsonMinifiedSize = deadEntriesJsonMinified.fileSize()

                val deadEntriesJsonMinifiedZst = deadEntriesAccessor.deadEntriesFile(currentConfig, JSON_MINIFIED_ZST)
                check(deadEntriesJsonMinifiedZst.regularFileExists()) { "Dead entries *-minified.json.zst file for [${currentConfig.hostname()}] doesn't exist." }
                val deadEntriesJsonMinifiedZstSize = deadEntriesJsonMinifiedZst.fileSize()

                check(deadEntriesJsonMinifiedZstSize < deadEntriesJsonMinifiedSize && deadEntriesJsonMinifiedSize < deadEntriesJsonPrettyPrintSize) {
                    "File sizes for dead entry files of [${currentConfig.hostname()}] are not plausible: [json=$deadEntriesJsonPrettyPrintSize, jsonMinified=$deadEntriesJsonMinifiedSize, jsonMinifiedZst=$deadEntriesJsonMinifiedZstSize]"
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