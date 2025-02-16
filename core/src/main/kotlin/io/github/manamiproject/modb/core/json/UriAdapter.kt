package io.github.manamiproject.modb.core.json

import com.squareup.moshi.*
import java.net.URI

internal class UriAdapter: JsonAdapter<URI>() {

    @FromJson
    override fun fromJson(reader: JsonReader): URI = URI(reader.nextString())

    @ToJson
    override fun toJson(writer: JsonWriter, value: URI?) {
        requireNotNull(value) { "UriAdapter expects non-nullable value, but received null." }
        writer.value(value.toString())
    }
}