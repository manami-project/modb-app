package io.github.manamiproject.modb.serde.json.deserializer

import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.serde.json.models.Dataset
import kotlinx.coroutines.withContext

/**
 * Can deserialize the dataset JSON file provided by a [LifecycleAwareInputStream].
 * [manami-project/anime-offline-database](https://github.com/manami-project/anime-offline-database).
 * @since 6.0.0
 * @throws IllegalArgumentException if the given [LifecycleAwareInputStream] is closed.
 */
public class DatasetFromInputStreamDeserializer: Deserializer<LifecycleAwareInputStream, Dataset> {

    override suspend fun deserialize(source: LifecycleAwareInputStream): Dataset = withContext(LIMITED_CPU) {
        require(source.isNotClosed()) { "Stream must not be closed." }

        log.info { "Deserializing dataset" }

        return@withContext Json.parseJson<Dataset>(source)!!
    }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DatasetFromInputStreamDeserializer]
         * @since 6.0.0
         */
        public val instance: DatasetFromInputStreamDeserializer by lazy { DatasetFromInputStreamDeserializer() }
    }
}