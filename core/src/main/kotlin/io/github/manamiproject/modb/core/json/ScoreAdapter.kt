package io.github.manamiproject.modb.core.json

import com.squareup.moshi.*
import com.squareup.moshi.JsonReader.Token.NULL
import io.github.manamiproject.modb.core.anime.NoScore
import io.github.manamiproject.modb.core.anime.Score
import io.github.manamiproject.modb.core.anime.ScoreValue

internal class ScoreAdapter: JsonAdapter<Score>() {

    @FromJson
    override fun fromJson(reader: JsonReader): Score {

       if (reader.peek() == NULL) {
           reader.nextNull<Unit>()
           return NoScore
       }

        reader.beginObject()

        var arithmeticGeometricMean = 0.0
        var arithmeticGeometricMeanDeserialized = false
        var arithmeticMean = 0.0
        var arithmeticMeanDeserialized = false
        var median = 0.0
        var medianDeserialized = false

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "arithmeticGeometricMean" -> {
                    arithmeticGeometricMean = reader.nextDouble()
                    arithmeticGeometricMeanDeserialized = true
                }
                "arithmeticMean" -> {
                    arithmeticMean = reader.nextDouble()
                    arithmeticMeanDeserialized = true
                }
                "median" -> {
                    median = reader.nextDouble()
                    medianDeserialized = true
                }
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        val checkedProperties = listOf(arithmeticGeometricMeanDeserialized, arithmeticMeanDeserialized, medianDeserialized)

        if (checkedProperties.all { !it }) {
            return NoScore
        }

        if (!checkedProperties.all { it }) {
            val missingProperties = listOfNotNull(
                "arithmeticGeometricMean".takeIf { !arithmeticGeometricMeanDeserialized },
                "arithmeticMean".takeIf { !arithmeticMeanDeserialized },
                "median".takeIf { !medianDeserialized },
            ).joinToString(", ")
            throw IllegalStateException("Properties for 'score' are missing: [$missingProperties]")
        }

        return ScoreValue(
            arithmeticGeometricMean = arithmeticGeometricMean,
            arithmeticMean = arithmeticMean,
            median = median,
        )
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: Score?) {
        requireNotNull(value) { "ScoreAdapter expects non-nullable value, but received null." }

        when(value) {
            NoScore -> {
                if (writer.serializeNulls) {
                    writer.nullValue()
                }
            }
            is ScoreValue -> {
                writer.beginObject()
                writer.name("arithmeticGeometricMean").value(value.arithmeticGeometricMean)
                writer.name("arithmeticMean").value(value.arithmeticMean)
                writer.name("median").value(value.median)
                writer.endObject()
            }
        }
    }
}