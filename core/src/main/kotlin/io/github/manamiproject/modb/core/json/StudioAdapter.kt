package io.github.manamiproject.modb.core.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import io.github.manamiproject.modb.core.anime.Studio

internal class StudioAdapter: JsonAdapter<Studio>() {

    @FromJson
    override fun fromJson(reader: JsonReader): Studio = reader.nextString()

    @ToJson
    override fun toJson(writer: JsonWriter, value: Studio?) {
        requireNotNull(value) { "StudioAdapter expects non-nullable value, but received null." }
        writer.value(value)
    }
}