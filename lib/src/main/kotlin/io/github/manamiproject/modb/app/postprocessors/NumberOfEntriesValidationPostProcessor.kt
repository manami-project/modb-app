package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.dataset.DatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DatasetFileType.*
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.dataset.DefaultDatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DefaultDeadEntriesAccessor
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.serde.json.deserializer.DatasetFromJsonInputStreamDeserializer
import io.github.manamiproject.modb.serde.json.deserializer.DatasetFromJsonLinesInputStreamDeserializer
import io.github.manamiproject.modb.serde.json.deserializer.DeadEntriesFromInputStreamDeserializer
import io.github.manamiproject.modb.serde.json.deserializer.Deserializer
import io.github.manamiproject.modb.serde.json.deserializer.FromRegularFileDeserializer
import io.github.manamiproject.modb.serde.json.models.Dataset
import io.github.manamiproject.modb.serde.json.models.DeadEntries

/**
 * Verifies that the number of entries across dataset files as well as dead entries files are equal.
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property deadEntriesAccessor Access to dead entries files.
 * @property datasetFileAccessor Access to dataset files.
 */
class NumberOfEntriesValidationPostProcessor(
    private val appConfig: Config = AppConfig.instance,
    private val datasetFileAccessor: DatasetFileAccessor = DefaultDatasetFileAccessor.instance,
    private val deadEntriesAccessor: DeadEntriesAccessor = DefaultDeadEntriesAccessor.instance,
    private val datasetJsonDeserializer: Deserializer<RegularFile, Dataset> = FromRegularFileDeserializer(deserializer = DatasetFromJsonInputStreamDeserializer.instance),
    private val datasetJsonLinesDeserializer: Deserializer<RegularFile, Dataset> = FromRegularFileDeserializer(deserializer = DatasetFromJsonLinesInputStreamDeserializer.instance),
    private val deadEntriesDeserializer: Deserializer<RegularFile, DeadEntries> = FromRegularFileDeserializer(deserializer = DeadEntriesFromInputStreamDeserializer.instance),
): PostProcessor {

    override suspend fun process(): Boolean {
        log.info { "Checking that number of entries is the same in all dataset files." }

        val datasetJsonPrettyPrint = datasetJsonDeserializer.deserialize(datasetFileAccessor.offlineDatabaseFile(JSON_PRETTY_PRINT)).data.count()
        val datasetJsonMinified = datasetJsonDeserializer.deserialize(datasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED)).data.count()
        val datasetJsonMinifiedZst = datasetJsonDeserializer.deserialize(datasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED_ZST)).data.count()
        val datasetJsonLines = datasetJsonLinesDeserializer.deserialize(datasetFileAccessor.offlineDatabaseFile(JSON_LINES)).data.count()
        val datasetJsonLinesZst = datasetJsonLinesDeserializer.deserialize(datasetFileAccessor.offlineDatabaseFile(JSON_LINES_ZST)).data.count()


        check(listOf(datasetJsonPrettyPrint, datasetJsonMinified, datasetJsonMinifiedZst, datasetJsonLines, datasetJsonLinesZst).distinct().size == 1) {
            "Number of dataset files differ: [jsonPrettyPrint=$datasetJsonPrettyPrint, jsonMinified=$datasetJsonMinified, jsonMinifiedZst=$datasetJsonMinifiedZst, datasetJsonLines=$datasetJsonLines, datasetJsonLinesZst=$datasetJsonLinesZst]"
        }

        log.info { "Checking that number of dead entries is the same across all files." }

        appConfig.metaDataProviderConfigurations()
            .filter { appConfig.deadEntriesSupported(it) }
            .forEach { currentConfig ->
                val deadEntriesJsonPrettyPrint = deadEntriesDeserializer.deserialize(deadEntriesAccessor.deadEntriesFile(currentConfig, JSON_PRETTY_PRINT)).deadEntries.count()
                val deadEntriesJsonMinified = deadEntriesDeserializer.deserialize(deadEntriesAccessor.deadEntriesFile(currentConfig, JSON_MINIFIED)).deadEntries.count()
                val deadEntriesJsonMinifiedZst = deadEntriesDeserializer.deserialize(deadEntriesAccessor.deadEntriesFile(currentConfig, JSON_MINIFIED_ZST)).deadEntries.count()

                check(deadEntriesJsonPrettyPrint == deadEntriesJsonMinified && deadEntriesJsonPrettyPrint == deadEntriesJsonMinifiedZst) {
                    "Number of dead entries files differ for [${currentConfig.hostname()}]: [jsonPrettyPrint=$deadEntriesJsonPrettyPrint, jsonMinified=$deadEntriesJsonMinified, JsonMinifiedZst=$deadEntriesJsonMinifiedZst]"
                }
            }

        return true
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [NumberOfEntriesValidationPostProcessor]
         * @since 1.0.0
         */
        val instance: NumberOfEntriesValidationPostProcessor by lazy { NumberOfEntriesValidationPostProcessor() }
    }
}