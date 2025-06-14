package io.github.manamiproject.modb.serde.json.deserializer

import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.serde.json.models.DeadEntries
import kotlinx.coroutines.withContext

/**
 * Deserializes dead entry files from [manami-project/anime-offline-database](https://github.com/manami-project/anime-offline-database).
 * This works for any `*.json` file in the `dead-entries/` directory.
 * @since 5.0.0
 */
public class DeadEntriesFromInputStreamDeserializer : Deserializer<LifecycleAwareInputStream, DeadEntries> {

    override suspend fun deserialize(source: LifecycleAwareInputStream): DeadEntries = withContext(LIMITED_CPU) {
        require(source.isNotClosed()) { "Stream must not be closed." }

        log.info { "Parsing dead entries" }

        return@withContext Json.parseJson(source)!!
    }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DeadEntriesFromInputStreamDeserializer]
         * @since 5.2.0
         */
        public val instance: DeadEntriesFromInputStreamDeserializer by lazy { DeadEntriesFromInputStreamDeserializer() }
    }
}