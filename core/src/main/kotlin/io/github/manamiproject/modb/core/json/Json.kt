package io.github.manamiproject.modb.core.json

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.addAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import io.github.manamiproject.modb.core.json.Json.SerializationOptions.DEACTIVATE_PRETTY_PRINT
import io.github.manamiproject.modb.core.json.Json.SerializationOptions.DEACTIVATE_SERIALIZE_NULL
import io.github.manamiproject.modb.core.models.Anime
import kotlinx.coroutines.withContext
import java.io.InputStream
import com.squareup.moshi.JsonAdapter as MoshiAdapter

/**
 * Handles serialization and deserialization of objects to/from JSON.
 * @since 11.0.0
 */
public object Json {

    private const val JSON_IDENT: String = "  "

    @PublishedApi
    @OptIn(ExperimentalStdlibApi::class)
    internal val moshi: Moshi = Moshi.Builder()
        .addAdapter(UriAdapter())
        .addAdapter(DurationAdapter())
        .addAdapter(AnimeTypeAdapter())
        .addAdapter(AnimeStatusAdapter())
        .addAdapter(AnimeSeasonAdapter())
        .addAdapter(AnimeAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()

    /**
     * Parse a [String] into an object.
     *
     * **WARNING** [Collection]s of a non-nullable type can still contain null.
     * @since 8.0.0
     * @param json Valid JSON as [String].
     * @return Deserialzed JSON as object of given type [T].
     */
    @OptIn(ExperimentalStdlibApi::class)
    public suspend inline fun <reified T> parseJson(json: String): T? = withContext(LIMITED_CPU) {
        return@withContext moshi.adapter<T>().nullSafe().fromJson(json)
    }

    /**
     * Parse an [InputStream] into an object.
     *
     * **WARNING** [Collection]s of a non-nullable type can still contain null.
     * @since 8.0.0
     * @param json Valid JSON as [InputStream].
     * @return Deserialized JSON as object of given type [T].
     */
    @OptIn(ExperimentalStdlibApi::class)
    public suspend inline fun <reified T> parseJson(json: InputStream): T? = withContext(LIMITED_FS) {
        return@withContext moshi.adapter<T>().nullSafe().fromJson(json.bufferedReader().readText())
    }

    /**
     * Serialize any object to JSON. Uses default set of custom adaoters for serializing [Anime].
     * @since 8.0.0
     * @param obj Any object that is supposed to be serialized to JSON.
     * @param options Options that can change the default behavior of the JSON serialization.
     * @return Given object serialized in JSON as [String].
     */
    public suspend fun toJson(obj: Any, vararg options: SerializationOptions): String = withContext(LIMITED_CPU) {
        return@withContext configureJsonAdapter(SerializationSettings(options.toSet())).toJson(obj)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun configureJsonAdapter(settings: SerializationSettings): MoshiAdapter<Any> {
        var jsonAdapter = moshi.adapter<Any>()

        if (settings.serializeNullActivated) {
            jsonAdapter = jsonAdapter.serializeNulls()
        }

        if (settings.prettyPrintActivated) {
            jsonAdapter = jsonAdapter.indent(JSON_IDENT)
        }

        return jsonAdapter
    }

    /**
     * Possible options to customize serialization of JSON documents using [Json.toJson].
     * @since 11.0.0
     */
    public enum class SerializationOptions {
        /**
         * By default the output JSON string is formatted. Using this option will create a minified string instead.
         * @since 11.0.0
         */
        DEACTIVATE_PRETTY_PRINT,
        /**
         * By default a property providing `null` as value will be serialized. Using this option will omit these properties.
         * @since 11.0.0
         */
        DEACTIVATE_SERIALIZE_NULL;
    }
}

private class SerializationSettings(val options: Set<Json.SerializationOptions>) {
    /** Prettyprint is activated by default. */
    val prettyPrintActivated: Boolean = false.takeIf { options.contains(DEACTIVATE_PRETTY_PRINT)} ?: true
    /** Serialize null is activated by default. */
    val serializeNullActivated: Boolean = false.takeIf { options.contains(DEACTIVATE_SERIALIZE_NULL)} ?: true
}