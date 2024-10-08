package io.github.manamiproject.modb.app.dataset

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.dataset.DatasetFileType.*
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.createZipOf
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.serde.json.*
import io.github.manamiproject.modb.serde.json.models.Dataset

/**
 * Handles the access to the dataset files like
 * + anime-offline-database.json
 * + anime-offline-database-minified.json
 * + anime-offline-database.zip
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property deserializer Deserializes the anime dataset file.
 * @property jsonSerializer Writes the anime dataset file.
 */
class DefaultDatasetFileAccessor(
    private val appConfig: Config = AppConfig.instance,
    private val deserializer: ExternalResourceJsonDeserializer<Dataset> = DefaultExternalResourceJsonDeserializer(deserializer = AnimeListJsonStringDeserializer.instance),
    private val jsonSerializer: JsonSerializer<Collection<Anime>> = AnimeListJsonSerializer(clock = appConfig.clock()),
): DatasetFileAccessor {

    override suspend fun fetchEntries(): List<Anime> {
        log.info { "Loading database" }

        return deserializer.deserialize(offlineDatabaseFile(JSON_MINIFIED)).data
    }

    override suspend fun saveEntries(anime: List<Anime>) {
        val sortedList = anime.sortedWith(
            compareBy<Anime> { it.title }
                .thenBy { it.animeSeason.year }
                .thenBy { it.sources.first() }
        )

        log.info { "Writing json to file." }
        jsonSerializer.serialize(sortedList, minify = false).writeToFile(offlineDatabaseFile(JSON))

        log.info { "Creating minified json." }
        jsonSerializer.serialize(sortedList, minify = true).writeToFile(offlineDatabaseFile(JSON_MINIFIED))

        log.info { "Creating zip file." }
        offlineDatabaseFile(ZIP).createZipOf(offlineDatabaseFile(JSON_MINIFIED))
    }

    override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when (type) {
        JSON -> appConfig.outputDirectory().resolve("anime-offline-database.json")
        JSON_MINIFIED -> appConfig.outputDirectory().resolve("anime-offline-database-minified.json")
        ZIP -> appConfig.outputDirectory().resolve("anime-offline-database.zip")
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DefaultDatasetFileAccessor]
         * @since 1.0.0
         */
        val instance: DefaultDatasetFileAccessor by lazy { DefaultDatasetFileAccessor() }
    }
}