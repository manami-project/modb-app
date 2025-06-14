package io.github.manamiproject.modb.serde.json

import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.serde.json.models.Dataset
import kotlinx.coroutines.withContext

/**
 * Can deserialize the [manami-project/anime-offline-database](https://github.com/manami-project/anime-offline-database).
 * Works for
 * + anime-offline-database.json
 * + anime-offline-database-minified.json
 * @since 5.0.0
 */
public class AnimeListJsonStringDeserializer : JsonDeserializer<Dataset> {

    override suspend fun deserialize(json: String): Dataset = withContext(LIMITED_CPU) {
        require(json.neitherNullNorBlank()) { "Given JSON string must not be blank." }

        log.info { "Deserializing dataset" }

        return@withContext Json.parseJson<Dataset>(json)!!
    }

    override suspend fun deserialize(jsonInputStream: LifecycleAwareInputStream): Dataset = withContext(LIMITED_CPU) {
        require(jsonInputStream.isNotClosed()) { "Stream must not be closed." }

        log.info { "Deserializing dataset" }

        return@withContext Json.parseJson<Dataset>(jsonInputStream)!!
    }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [AnimeListJsonStringDeserializer]
         * @since 5.2.0
         */
        public val instance: AnimeListJsonStringDeserializer by lazy { AnimeListJsonStringDeserializer() }
    }
}