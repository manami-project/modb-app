package io.github.manamiproject.modb.serde.json.serializer

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.date.WeekOfYear
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.serde.json.models.DeadEntries
import io.github.manamiproject.modb.serde.json.models.License
import kotlinx.coroutines.withContext
import java.net.URI
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Can serialize a [Collection] of dead entries files from [manami-project/anime-offline-database](https://github.com/manami-project/anime-offline-database).
 * The resulting lists is duplicate free and sorted.
 * @since 6.0.0
 * @param clock Instance of a clock to determine the current date.
 */
public class DeadEntriesJsonSerializer(
    private val clock: Clock = Clock.systemDefaultZone(),
): JsonSerializer<Collection<AnimeId>> {

    override suspend fun serialize(obj: Collection<AnimeId>, minify: Boolean): String = withContext(LIMITED_CPU) {
            log.debug { "Sorting dead entries" }

            val currentWeek = WeekOfYear(LocalDate.now(clock))

            val deadEntriesDocument = DeadEntries(
                `$schema` = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/tags/${currentWeek}/schemas/dead-entries.schema.json"),
                license = License().copy(
                    url = URI("https://github.com/manami-project/anime-offline-database/blob/$currentWeek/LICENSE"),
                ),
                lastUpdate = LocalDate.now(clock).format(DateTimeFormatter.ISO_DATE),
                deadEntries = obj.toSet().sorted(),
            )

            return@withContext if (minify) {
                log.info { "Serializing dead entries minified." }
                Json.toJson(
                    deadEntriesDocument,
                    Json.SerializationOptions.DEACTIVATE_PRETTY_PRINT,
                    Json.SerializationOptions.DEACTIVATE_SERIALIZE_NULL,
                )
            } else {
                log.info { "Serializing dead entries pretty print." }
                Json.toJson(deadEntriesDocument)
            }
        }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DeadEntriesJsonSerializer]
         * @since 6.0.0
         */
        public val instance: DeadEntriesJsonSerializer by lazy { DeadEntriesJsonSerializer() }
    }
}