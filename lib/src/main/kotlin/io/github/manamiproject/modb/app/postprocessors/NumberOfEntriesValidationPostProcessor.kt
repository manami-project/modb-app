package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.dataset.DatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DatasetFileType.*
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.dataset.DefaultDatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DefaultDeadEntriesAccessor
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.serde.json.AnimeListJsonStringDeserializer
import io.github.manamiproject.modb.serde.json.DeadEntriesJsonStringDeserializer
import io.github.manamiproject.modb.serde.json.DefaultExternalResourceJsonDeserializer

/**
 * Verifies that the number of entries across dataset files as well as dead entries files are equal.
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property deadEntriesAccessor Access to dead entries files.
 * @property datasetFileAccessor Access to dataset files.
 */
class NumberOfEntriesValidationPostProcessor(
    private val appConfig: Config = AppConfig.instance,
    private val deadEntriesAccessor: DeadEntriesAccessor = DefaultDeadEntriesAccessor.instance,
    private val datasetFileAccessor: DatasetFileAccessor = DefaultDatasetFileAccessor.instance,
): PostProcessor {

    override suspend fun process(): Boolean {
        log.info { "Checking that number of entries is the same in all dataset files." }

        val datasetJsonDeserializer = DefaultExternalResourceJsonDeserializer(deserializer = AnimeListJsonStringDeserializer.instance)
        val datasetJson = datasetJsonDeserializer.deserialize(datasetFileAccessor.offlineDatabaseFile(JSON)).data.count()
        val datasetJsonMinified = datasetJsonDeserializer.deserialize(datasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED)).data.count()
        val datasetZip = datasetJsonDeserializer.deserialize(datasetFileAccessor.offlineDatabaseFile(ZIP)).data.count()

        check(datasetJson == datasetJsonMinified && datasetJson == datasetZip) {
            "Number of dataset files differ: [json=$datasetJson, jsonMinified=$datasetJsonMinified, zip=$datasetZip]"
        }

        log.info { "Checking that number of dead entries is the same across all files." }

        val deadEntriesJsonDeserializer = DefaultExternalResourceJsonDeserializer(deserializer = DeadEntriesJsonStringDeserializer.instance)

        appConfig.metaDataProviderConfigurations()
            .filter { appConfig.deadEntriesSupported(it) }
            .forEach { currentConfig ->
                val deadEntriesJson = deadEntriesJsonDeserializer.deserialize(deadEntriesAccessor.deadEntriesFile(currentConfig, JSON)).deadEntries.count()
                val deadEntriesJsonMinified = deadEntriesJsonDeserializer.deserialize(deadEntriesAccessor.deadEntriesFile(currentConfig, JSON_MINIFIED)).deadEntries.count()
                val deadEntriesZip = deadEntriesJsonDeserializer.deserialize(deadEntriesAccessor.deadEntriesFile(currentConfig, ZIP)).deadEntries.count()

                check(deadEntriesJson == deadEntriesJsonMinified && deadEntriesJson == deadEntriesZip) {
                    "Number of dead entries files differ for [${currentConfig.hostname()}]: [json=$deadEntriesJson, jsonMinified=$deadEntriesJsonMinified, zip=$deadEntriesZip]"
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