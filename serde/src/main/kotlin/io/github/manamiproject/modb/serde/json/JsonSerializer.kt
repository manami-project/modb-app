package io.github.manamiproject.modb.serde.json

/**
 * Serializes objects to JSON.
 * @since 5.0.0
 */
public interface JsonSerializer<in T, in JSON_LINE_TYPE> {

    /**
     * Serializes an object either as minified or as pretty print JSON.
     * @since 6.0.0
     * @param obj Object to be serialized.
     * @param minify Whether the resulting output should be minified or not. **Default** is `false`.
     * @return JSON output as [String].
     */
    public suspend fun serializeJson(obj: T, minify: Boolean = false): String

    /**
     * Creates a JSON line file. First line can be a meta data object followed by the actual data.
     * Each line represents a single valid, minified JSON object.
     * @since 6.0.0
     * @param obj Collection of objects to be serialized.
     */
    public suspend fun serializeJsonLine(obj: Collection<JSON_LINE_TYPE>): String
}