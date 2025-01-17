package io.github.manamiproject.modb.core.json

import com.squareup.moshi.*
import io.github.manamiproject.modb.core.models.Anime

internal class AnimeStatusAdapter: JsonAdapter<Anime.Status>() {

    @FromJson
    override fun fromJson(reader: JsonReader): Anime.Status = Anime.Status.valueOf(reader.nextString())

    @ToJson
    override fun toJson(writer: JsonWriter, value: Anime.Status?) {
        requireNotNull(value) { "AnimeStatusAdapter is non-nullable, but received null." }
        writer.value(value.toString())
    }
}