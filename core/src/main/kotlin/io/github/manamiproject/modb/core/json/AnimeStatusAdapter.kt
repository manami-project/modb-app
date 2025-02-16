package io.github.manamiproject.modb.core.json

import com.squareup.moshi.*
import io.github.manamiproject.modb.core.anime.AnimeStatus

internal class AnimeStatusAdapter: JsonAdapter<AnimeStatus>() {

    @FromJson
    override fun fromJson(reader: JsonReader): AnimeStatus = AnimeStatus.valueOf(reader.nextString())

    @ToJson
    override fun toJson(writer: JsonWriter, value: AnimeStatus?) {
        requireNotNull(value) { "AnimeStatusAdapter expects non-nullable value, but received null." }
        writer.value(value.toString())
    }
}