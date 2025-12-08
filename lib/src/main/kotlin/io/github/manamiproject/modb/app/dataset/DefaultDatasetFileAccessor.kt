package io.github.manamiproject.modb.app.dataset

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.dataset.DatasetFileType.*
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.extensions.writeToZstandardFile
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.serde.json.deserializer.DatasetFromJsonInputStreamDeserializer
import io.github.manamiproject.modb.serde.json.deserializer.Deserializer
import io.github.manamiproject.modb.serde.json.deserializer.FromRegularFileDeserializer
import io.github.manamiproject.modb.serde.json.models.Dataset
import io.github.manamiproject.modb.serde.json.serializer.DatasetJsonLinesSerializer
import io.github.manamiproject.modb.serde.json.serializer.DatasetJsonSerializer
import io.github.manamiproject.modb.serde.json.serializer.JsonLinesSerializer
import io.github.manamiproject.modb.serde.json.serializer.JsonSerializer

/**
 * Handles the access to the different dataset files.
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property deserializer Deserializes the anime dataset file.
 * @property jsonSerializer Writes the anime dataset file.
 */
class DefaultDatasetFileAccessor(
    private val appConfig: Config = AppConfig.instance,
    private val deserializer: Deserializer<RegularFile, Dataset> = FromRegularFileDeserializer(deserializer = DatasetFromJsonInputStreamDeserializer.instance),
    private val jsonSerializer: JsonSerializer<Collection<Anime>> = DatasetJsonSerializer(clock = appConfig.clock()),
    private val jsonLinesSerializer: JsonLinesSerializer<Anime> = DatasetJsonLinesSerializer(clock = appConfig.clock()),
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

        log.info { "Creating minified JSON file and its Zstandard compressed version." }
        jsonSerializer.serialize(sortedList, minify = true).apply {
            writeToFile(offlineDatabaseFile(JSON_MINIFIED))
            writeToZstandardFile(offlineDatabaseFile(JSON_MINIFIED_ZST))
        }

        log.info { "Creating JSON lines file and its Zstandard compressed version." }
        jsonLinesSerializer.serialize(sortedList).apply {
            writeToFile(offlineDatabaseFile(JSON_LINES))
            writeToZstandardFile(offlineDatabaseFile(JSON_LINES_ZST))
        }
    }

    override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when (type) {
        JSON_MINIFIED -> appConfig.outputDirectory().resolve("anime-offline-database-minified.json")
        JSON_MINIFIED_ZST -> appConfig.outputDirectory().resolve("anime-offline-database-minified.json.zst")
        JSON_LINES -> appConfig.outputDirectory().resolve("anime-offline-database.jsonl")
        JSON_LINES_ZST -> appConfig.outputDirectory().resolve("anime-offline-database.jsonl.zst")
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