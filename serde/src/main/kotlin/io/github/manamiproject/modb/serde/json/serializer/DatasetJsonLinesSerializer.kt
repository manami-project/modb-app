package io.github.manamiproject.modb.serde.json.serializer

import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.date.WeekOfYear
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.json.Json.SerializationOptions.*
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.serde.json.models.DatasetMetaData
import io.github.manamiproject.modb.serde.json.models.License
import kotlinx.coroutines.withContext
import java.net.URI
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_DATE

/**
 * Serializes a [Collection] of [Anime] into a JSON lines file.
 * Each line is a minified JSON of an [Anime] object except for the first line which is of type [DatasetMetaData].
 * @since 6.0.0
 * @property clock Instance of a clock to determine the current date.
 */
public class DatasetJsonLinesSerializer(
    private val clock: Clock = Clock.systemDefaultZone(),
): JsonLinesSerializer<Anime> {

    override suspend fun serialize(obj: Collection<Anime>): String = withContext(LIMITED_CPU) {
        log.debug { "Sorting dataset by title, type and episodes." }

        val sortedList = obj.toSet().sortedWith(compareBy({ it.title.lowercase() }, {it.type}, { it.episodes }))
        val currentWeek = WeekOfYear(LocalDate.now(clock))

        val metaData = DatasetMetaData(
            `$schema` = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/tags/$currentWeek/schemas/anime-offline-database.jsonl.schema.json"),
            license = License().copy(
                url = URI("https://github.com/manami-project/anime-offline-database/blob/$currentWeek/LICENSE"),
            ),
            lastUpdate = LocalDate.now(clock).format(ISO_DATE),
        )

        log.info { "Serializing anime list as JSON line." }

        val resultBuilder = StringBuilder(Json.toJson(
            obj = metaData,
            DEACTIVATE_PRETTY_PRINT,
            DEACTIVATE_SERIALIZE_NULL,
        ))

        sortedList.map { Json.toJson(it, DEACTIVATE_PRETTY_PRINT, DEACTIVATE_SERIALIZE_NULL) }.forEach {
            resultBuilder.append("\n").append(it)
        }

        return@withContext resultBuilder.toString()
    }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DatasetJsonLinesSerializer]
         * @since 6.0.0
         */
        public val instance: DatasetJsonLinesSerializer by lazy { DatasetJsonLinesSerializer() }
    }
}