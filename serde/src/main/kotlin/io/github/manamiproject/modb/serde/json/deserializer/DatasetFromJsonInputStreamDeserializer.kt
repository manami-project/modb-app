package io.github.manamiproject.modb.serde.json.deserializer

import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.serde.json.models.Dataset
import kotlinx.coroutines.withContext

/**
 * Can deserialize the dataset JSON provided by a [LifecycleAwareInputStream].
 * This class cannot handle JSON lines content. This class can only process default JSON.
 * @since 6.0.0
 * @throws IllegalArgumentException if the given [LifecycleAwareInputStream] is closed.
 */
public class DatasetFromJsonInputStreamDeserializer: Deserializer<LifecycleAwareInputStream, Dataset> {

    override suspend fun deserialize(source: LifecycleAwareInputStream): Dataset = withContext(LIMITED_CPU) {
        require(source.isNotClosed()) { "Stream must not be closed." }

        log.info { "Deserializing dataset" }

        return@withContext source.use { Json.parseJson<Dataset>(source)!! }
    }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DatasetFromJsonInputStreamDeserializer]
         * @since 6.0.0
         */
        public val instance: DatasetFromJsonInputStreamDeserializer by lazy { DatasetFromJsonInputStreamDeserializer() }
    }
}