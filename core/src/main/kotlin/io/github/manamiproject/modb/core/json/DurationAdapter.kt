package io.github.manamiproject.modb.core.json

import com.squareup.moshi.*
import com.squareup.moshi.JsonReader.Token.NULL
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.anime.Duration
import io.github.manamiproject.modb.core.anime.Duration.Companion.UNKNOWN as UNKNOWN_DURATION
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.SECONDS

internal class DurationAdapter: JsonAdapter<Duration>() {

    @FromJson
    override fun fromJson(reader: JsonReader): Duration {
        if (reader.peek() == NULL) {
            reader.nextNull<Unit>()
            return UNKNOWN_DURATION
        }

        reader.beginObject()

        var value = 0
        var valueDeserialized = false
        var unit = EMPTY
        var unitDeserialized = false

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "value" -> {
                    value = reader.nextInt()
                    valueDeserialized = true
                }
                "unit" -> {
                    unit = reader.nextString()
                    unitDeserialized = true
                }
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        when {
            !valueDeserialized -> throw IllegalStateException("Property 'value' is either missing or null.")
            !unitDeserialized -> throw IllegalStateException("Property 'unit' is either missing or null.")
        }

        return Duration(
            value = value,
            unit = Duration.TimeUnit.valueOf(unit)
        )
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: Duration?) {
        requireNotNull(value) { "DurationAdapter expects non-nullable value, but received null." }

        when {
            value.duration == 0 && writer.serializeNulls -> {
                writer.nullValue()
            }
            value.duration != 0 -> {
                writer.beginObject()
                writer.name("value").value(value.duration)
                writer.name("unit").value(SECONDS.toString())
                writer.endObject()
            }
        }
    }
}