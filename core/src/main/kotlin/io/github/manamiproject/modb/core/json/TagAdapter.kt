package io.github.manamiproject.modb.core.json

import com.squareup.moshi.*
import io.github.manamiproject.modb.core.anime.Tag

internal class TagAdapter: JsonAdapter<Tag>() {

    @FromJson
    override fun fromJson(reader: JsonReader): Tag = reader.nextString()

    @ToJson
    override fun toJson(writer: JsonWriter, value: Tag?) {
        requireNotNull(value) { "TagAdapter expects non-nullable value, but received null." }
        writer.value(value)
    }
}