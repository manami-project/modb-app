package io.github.manamiproject.modb.serde.json.serializer

import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.date.WeekOfYear
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.json.Json.SerializationOptions.DEACTIVATE_PRETTY_PRINT
import io.github.manamiproject.modb.core.json.Json.SerializationOptions.DEACTIVATE_SERIALIZE_NULL
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.serde.json.models.Dataset
import io.github.manamiproject.modb.serde.json.models.License
import kotlinx.coroutines.withContext
import java.net.URI
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Can serialize a [Collection] of [Anime] to JSON.
 * [manami-project/anime-offline-database](https://github.com/manami-project/anime-offline-database).
 * The resulting list will be sorted by title, type and episodes in that order.
 * @since 6.0.0
 * @property clock Instance of a clock to determine the current date.
 */
public class DatasetJsonSerializer(
    private val clock: Clock = Clock.systemDefaultZone(),
) : JsonSerializer<Collection<Anime>> {

    override suspend fun serialize(obj: Collection<Anime>, minify: Boolean): String =
        withContext(LIMITED_CPU) {
            log.debug { "Sorting dataset by title, type and episodes." }

            val sortedList = obj.toSet().sortedWith(compareBy({ it.title.lowercase() }, { it.type }, { it.episodes }))
            val currentWeek = WeekOfYear(LocalDate.now(clock))

            val schemaLink = when (minify) {
                true -> URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/tags/$currentWeek/schemas/anime-offline-database-minified.schema.json")
                else -> URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/tags/$currentWeek/schemas/anime-offline-database.schema.json")
            }

            val data = Dataset(
                `$schema` = schemaLink,
                license = License().copy(
                    url = URI("https://github.com/manami-project/anime-offline-database/blob/$currentWeek/LICENSE"),
                ),
                data = sortedList,
                lastUpdate = LocalDate.now(clock).format(DateTimeFormatter.ISO_DATE),
            )

            return@withContext if (minify) {
                log.info { "Serializing anime list minified." }
                Json.toJson(
                    obj = data,
                    DEACTIVATE_PRETTY_PRINT,
                    DEACTIVATE_SERIALIZE_NULL,
                )
            } else {
                log.info { "Serializing anime list pretty print." }
                Json.toJson(data)
            }
        }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DatasetJsonSerializer]
         * @since 6.0.0
         */
        public val instance: DatasetJsonSerializer by lazy { DatasetJsonSerializer() }
    }
}