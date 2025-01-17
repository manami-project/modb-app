package io.github.manamiproject.modb.serde.json

import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.serde.json.models.DeadEntries
import kotlinx.coroutines.withContext

/**
 * Can deserialize dead entry files from [manami-project/anime-offline-database](https://github.com/manami-project/anime-offline-database).
 * @since 5.0.0
 */
public class DeadEntriesJsonStringDeserializer : JsonDeserializer<DeadEntries> {

    override suspend fun deserialize(json: String): DeadEntries = withContext(LIMITED_CPU) {
        require(json.neitherNullNorBlank()) { "Given JSON string must not be blank." }

        log.info { "Parsing dead entries" }

        return@withContext Json.parseJson(json)!!
    }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DeadEntriesJsonStringDeserializer]
         * @since 5.2.0
         */
        public val instance: DeadEntriesJsonStringDeserializer by lazy { DeadEntriesJsonStringDeserializer() }
    }
}