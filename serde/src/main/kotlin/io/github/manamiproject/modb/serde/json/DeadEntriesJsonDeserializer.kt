package io.github.manamiproject.modb.serde.json

import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.serde.json.models.DeadEntries
import kotlinx.coroutines.withContext

/**
 * Can deserialize dead entry files from [manami-project/anime-offline-database](https://github.com/manami-project/anime-offline-database).
 * This works for any `*.json` file in the `dead-entries/` directory..
 * @since 5.0.0
 */
public class DeadEntriesJsonDeserializer : JsonDeserializer<DeadEntries> {

    override suspend fun deserialize(json: String): DeadEntries = withContext(LIMITED_CPU) {
        require(json.neitherNullNorBlank()) { "Given JSON string must not be blank." }

        log.info { "Parsing dead entries" }

        return@withContext Json.parseJson(json)!!
    }

    override suspend fun deserialize(jsonInputStream: LifecycleAwareInputStream): DeadEntries = withContext(LIMITED_CPU) {
        require(jsonInputStream.isNotClosed()) { "Stream must not be closed." }

        log.info { "Parsing dead entries" }

        return@withContext Json.parseJson(jsonInputStream)!!
    }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DeadEntriesJsonDeserializer]
         * @since 5.2.0
         */
        public val instance: DeadEntriesJsonDeserializer by lazy { DeadEntriesJsonDeserializer() }
    }
}