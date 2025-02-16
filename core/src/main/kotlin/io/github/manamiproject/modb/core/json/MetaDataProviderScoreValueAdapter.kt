package io.github.manamiproject.modb.core.json

import com.squareup.moshi.*
import io.github.manamiproject.modb.core.anime.MetaDataProviderScoreValue
import io.github.manamiproject.modb.core.extensions.EMPTY

internal class MetaDataProviderScoreValueAdapter: JsonAdapter<MetaDataProviderScoreValue>() {

    @FromJson
    override fun fromJson(reader: JsonReader): MetaDataProviderScoreValue {
        reader.beginObject()

        var hostname = EMPTY
        var hostnameDeserialized = false
        var value = 0.0
        var valueDeserialized = false
        var minInclusive = 0.0
        var minInclusiveDeserialized = false
        var maxInclusive = 0.0
        var maxInclusiveDeserialized = false

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "hostname" -> {
                    hostname = reader.nextString()
                    hostnameDeserialized = true
                }
                "value" -> {
                    value = reader.nextDouble()
                    valueDeserialized = true
                }
                "range" -> {
                    reader.beginObject()
                    while (reader.hasNext()) {
                        when (reader.nextName()) {
                            "minInclusive" -> {
                                minInclusive = reader.nextDouble()
                                minInclusiveDeserialized = true
                            }
                            "maxInclusive" -> {
                                maxInclusive = reader.nextDouble()
                                maxInclusiveDeserialized = true
                            }
                        }
                    }
                    reader.endObject()
                }
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        when {
            !hostnameDeserialized -> throw IllegalStateException("Property 'hostname' is either missing or null.")
            !valueDeserialized -> throw IllegalStateException("Property 'value' is either missing or null.")
            !minInclusiveDeserialized -> throw IllegalStateException("Property 'range.minInclusive' is either missing or null.")
            !maxInclusiveDeserialized -> throw IllegalStateException("Property 'range.maxInclusive' is either missing or null.")
        }

        return MetaDataProviderScoreValue(
            hostname = hostname,
            value = value,
            originalRange = minInclusive..maxInclusive,
        )
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: MetaDataProviderScoreValue?) {
        requireNotNull(value) { "MetaDataProviderScoreAdapter expects non-nullable value, but received null." }

        writer.beginObject()
            .name("hostname").value(value.hostname)
            .name("value").value(value.value)
            .name("range")
                .beginObject()
                    .name("minInclusive").value(value.originalRange.start)
                    .name("maxInclusive").value(value.originalRange.endInclusive)
                .endObject()
        .endObject()
    }
}