package io.github.manamiproject.modb.core.json

import com.squareup.moshi.*
import io.github.manamiproject.modb.core.anime.Title

internal class TitleAdapter: JsonAdapter<Title>() {

    @FromJson
    override fun fromJson(reader: JsonReader): Title = reader.nextString()

    @ToJson
    override fun toJson(writer: JsonWriter, value: Title?) {
        requireNotNull(value) { "TitleAdapter expects non-nullable value, but received null." }
        writer.value(value)
    }
}