package io.github.manamiproject.modb.core.json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import io.github.manamiproject.modb.core.models.Anime

internal class AnimeTypeAdapter: JsonAdapter<Anime.Type>() {

    override fun fromJson(reader: JsonReader): Anime.Type = Anime.Type.valueOf(reader.nextString())

    override fun toJson(writer: JsonWriter, value: Anime.Type?) {
        requireNotNull(value) { "AnimeTypeAdapter is non-nullable, but received null." }
        writer.value(value.toString())
    }
}