package io.github.manamiproject.modb.serde.json

import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.json.Json.SerializationOptions.*
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.serde.json.models.Dataset
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_DATE

/**
 * Can serialize a [Collection] of [Anime] to the [manami-project/anime-offline-database](https://github.com/manami-project/anime-offline-database) JSON file.
 * The resulting list will be sorted by title, type and episodes in that order.
 * @since 5.0.0
 * @param clock Instance of a clock to determine the current date.
 */
public class AnimeListJsonSerializer(
    private val clock: Clock = Clock.systemDefaultZone(),
) : JsonSerializer<Collection<Anime>> {

    override suspend fun serialize(obj: Collection<Anime>, minify: Boolean): String = withContext(LIMITED_CPU) {
        log.debug { "Sorting dataset by title, type and episodes." }

        val sortedList = obj.toSet().sortedWith(compareBy({ it.title.lowercase() }, {it.type}, { it.episodes }))

        val data = Dataset(
            data = sortedList,
            lastUpdate = LocalDate.now(clock).format(ISO_DATE),
        )

        return@withContext if (minify) {
            log.info { "Serializing anime list minified." }
            Json.toJson(data, DEACTIVATE_PRETTY_PRINT, DEACTIVATE_SERIALIZE_NULL)
        } else {
            log.info { "Serializing anime list pretty print." }
            Json.toJson(data)
        }
    }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [AnimeListJsonSerializer]
         * @since 5.2.0
         */
        public val instance: AnimeListJsonSerializer by lazy { AnimeListJsonSerializer() }
    }
}