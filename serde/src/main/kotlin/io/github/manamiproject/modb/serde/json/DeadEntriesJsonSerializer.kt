package io.github.manamiproject.modb.serde.json

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.json.Json.SerializationOptions.DEACTIVATE_PRETTY_PRINT
import io.github.manamiproject.modb.core.json.Json.SerializationOptions.DEACTIVATE_SERIALIZE_NULL
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.serde.json.models.DeadEntries
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_DATE

/**
 * Can serialize a [Collection] of dead entries files from [manami-project/anime-offline-database](https://github.com/manami-project/anime-offline-database).
 * The resulting lists is duplicate free and sorted.
 * @since 5.0.0
 * @param clock Instance of a clock to determine the current date.
 */
public class DeadEntriesJsonSerializer(
    private val clock: Clock = Clock.systemDefaultZone(),
): JsonSerializer<Collection<AnimeId>> {

    override suspend fun serialize(obj: Collection<AnimeId>, minify: Boolean): String = withContext(LIMITED_CPU) {
        log.debug { "Sorting dead entries" }

        val deadEntriesDocument = DeadEntries(
            lastUpdate = LocalDate.now(clock).format(ISO_DATE),
            deadEntries = obj.toSet().sorted(),
        )

        return@withContext if (minify) {
            log.info { "Serializing dead entries minified." }
            Json.toJson(deadEntriesDocument, DEACTIVATE_PRETTY_PRINT, DEACTIVATE_SERIALIZE_NULL)
        } else {
            log.info { "Serializing dead entries pretty print." }
            Json.toJson(deadEntriesDocument)
        }
    }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DeadEntriesJsonSerializer]
         * @since 5.2.0
         */
        public val instance: DeadEntriesJsonSerializer by lazy { DeadEntriesJsonSerializer() }
    }
}