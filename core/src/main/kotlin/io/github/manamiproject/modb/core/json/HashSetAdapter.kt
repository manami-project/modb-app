package io.github.manamiproject.modb.core.json

import com.squareup.moshi.*

internal class HashSetAdapter<T>(private val elementAdapter: JsonAdapter<T>) : JsonAdapter<HashSet<T>>() {

    @FromJson
    override fun fromJson(reader: JsonReader): HashSet<T> {
        val result = HashSet<T>()
        reader.beginArray()
        while (reader.hasNext()) {
            val element = elementAdapter.fromJson(reader)
            if (element != null) {
                result.add(element)
            }
        }
        reader.endArray()
        return result
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: HashSet<T>?) {
        requireNotNull(value) { "HashSetAdapter is non-nullable, but received null." }

        writer.beginArray()
        for (element in value) {
            elementAdapter.toJson(writer, element)
        }
        writer.endArray()
    }
}