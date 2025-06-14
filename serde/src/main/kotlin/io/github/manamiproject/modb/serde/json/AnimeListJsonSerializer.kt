package io.github.manamiproject.modb.serde.json

import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.json.Json.SerializationOptions.*
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.date.WeekOfYear
import io.github.manamiproject.modb.serde.json.models.Dataset
import io.github.manamiproject.modb.serde.json.models.DatasetMetaData
import io.github.manamiproject.modb.serde.json.models.License
import kotlinx.coroutines.withContext
import java.net.URI
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_DATE

/**
 * Can serialize a [Collection] of [Anime] to a variety of formats for
 * [manami-project/anime-offline-database](https://github.com/manami-project/anime-offline-database).
 * The resulting list will be sorted by title, type and episodes in that order.
 * @since 5.0.0
 * @param clock Instance of a clock to determine the current date.
 */
public class AnimeListJsonSerializer(
    private val clock: Clock = Clock.systemDefaultZone(),
) : JsonSerializer<Collection<Anime>, Anime> {

    override suspend fun serializeJson(obj: Collection<Anime>, minify: Boolean): String = withContext(LIMITED_CPU) {
        log.debug { "Sorting dataset by title, type and episodes." }

        val sortedList = obj.toSet().sortedWith(compareBy({ it.title.lowercase() }, {it.type}, { it.episodes }))
        val currentWeek = WeekOfYear(LocalDate.now(clock))

        val schemaLink = when(minify) {
            true -> URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/tags/$currentWeek/anime-offline-database-minified.schema.json")
            else -> URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/tags/$currentWeek/anime-offline-database.schema.json")
        }

        val data = Dataset(
            `$schema` = schemaLink,
            license = License().copy(
                url = "https://github.com/manami-project/anime-offline-database/blob/$currentWeek/LICENSE",
            ),
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

    override suspend fun serializeJsonLine(obj: Collection<Anime>): String = withContext(LIMITED_CPU) {
        log.debug { "Sorting dataset by title, type and episodes." }

        val sortedList = obj.toSet().sortedWith(compareBy({ it.title.lowercase() }, {it.type}, { it.episodes }))
        val currentWeek = WeekOfYear(LocalDate.now(clock))

        val metaData = DatasetMetaData(
            `$schema` = URI(""), //FIXME
            license = License().copy(
                url = "https://github.com/manami-project/anime-offline-database/blob/$currentWeek/LICENSE",
            ),
            lastUpdate = LocalDate.now(clock).format(ISO_DATE),
        )

        log.info { "Serializing anime list as JSON line." }

        val resultBuilder = StringBuilder(Json.toJson(metaData, DEACTIVATE_PRETTY_PRINT, DEACTIVATE_SERIALIZE_NULL))
        sortedList.map { Json.toJson(it, DEACTIVATE_PRETTY_PRINT, DEACTIVATE_SERIALIZE_NULL) }.forEach {
            resultBuilder.append("\n").append(it)
        }

        return@withContext resultBuilder.toString()
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