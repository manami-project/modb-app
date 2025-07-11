package io.github.manamiproject.modb.serde.json.deserializer

import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.serde.json.models.Dataset
import io.github.manamiproject.modb.serde.json.models.DatasetMetaData
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext

/**
 * Can deserialize JSON lines into a [Dataset].
 * This class cannot handle default JSON. This class can only process JSON lines content.
 * @since 6.0.0
 * @throws IllegalArgumentException if the given [LifecycleAwareInputStream] is closed.
 */
public class DatasetFromJsonLinesInputStreamDeserializer: Deserializer<LifecycleAwareInputStream, Dataset> {

    override suspend fun deserialize(source: LifecycleAwareInputStream): Dataset = withContext(LIMITED_CPU) {
        require(source.isNotClosed()) { "Stream must not be closed." }

        log.info { "Deserializing dataset" }

        return@withContext source.bufferedReader().use { reader ->
            val datasetMetaData = Json.parseJson<DatasetMetaData>(reader.readLine())!!

            val data = reader.lineSequence().asFlow().map {
                Json.parseJson<Anime>(it)!!
            }.toList()

            Dataset(
                `$schema` = datasetMetaData.`$schema`,
                license = datasetMetaData.license,
                repository = datasetMetaData.repository,
                scoreRange = datasetMetaData.scoreRange,
                lastUpdate = datasetMetaData.lastUpdate,
                data = data,
            )
        }
    }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DatasetFromJsonLinesInputStreamDeserializer]
         * @since 6.0.0
         */
        public val instance: DatasetFromJsonLinesInputStreamDeserializer by lazy { DatasetFromJsonLinesInputStreamDeserializer() }
    }
}