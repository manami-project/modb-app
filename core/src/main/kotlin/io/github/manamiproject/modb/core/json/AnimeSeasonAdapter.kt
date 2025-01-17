package io.github.manamiproject.modb.core.json

import com.squareup.moshi.*
import io.github.manamiproject.modb.core.models.AnimeSeason
import io.github.manamiproject.modb.core.models.AnimeSeason.Companion.UNKNOWN_YEAR

internal class AnimeSeasonAdapter: JsonAdapter<AnimeSeason>() {

    @FromJson
    override fun fromJson(reader: JsonReader): AnimeSeason {
        reader.beginObject()

        var season = AnimeSeason.Season.UNDEFINED
        var seasonDeserialized = false
        var year = UNKNOWN_YEAR

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "season" -> {
                    season = AnimeSeason.Season.valueOf(reader.nextString())
                    seasonDeserialized = true
                }
                "year" -> {
                    year = if (reader.peek() != JsonReader.Token.NULL) { reader.nextInt() } else { reader.nextNull<Any>(); 0 }
                }
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        if (!seasonDeserialized) {
            throw IllegalStateException("Property 'season' is either missing or null.")
        }

        return AnimeSeason(
            season = season,
            year = year,
        )
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: AnimeSeason?) {
        requireNotNull(value) { "AnimeSeasonAdapter is non-nullable, but received null." }

        writer.beginObject()
        writer.name("season").value(value.season.toString())

        val year = value.year

        when {
            year == 0 && writer.serializeNulls -> writer.name("year").nullValue()
            year != 0 -> writer.name("year").value(year)
        }

        writer.endObject()
    }
}