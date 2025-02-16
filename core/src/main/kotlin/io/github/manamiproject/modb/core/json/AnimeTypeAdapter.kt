package io.github.manamiproject.modb.core.json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import io.github.manamiproject.modb.core.anime.AnimeType

internal class AnimeTypeAdapter: JsonAdapter<AnimeType>() {

    override fun fromJson(reader: JsonReader): AnimeType = AnimeType.valueOf(reader.nextString())

    override fun toJson(writer: JsonWriter, value: AnimeType?) {
        requireNotNull(value) { "AnimeTypeAdapter expects non-nullable value, but received null." }
        writer.value(value.toString())
    }
}