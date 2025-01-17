package io.github.manamiproject.modb.serde.json

/**
 * Serializes objects to JSON.
 * @since 5.0.0
 */
public interface JsonSerializer<in T> {

    /**
     * Serializes an object either as minified or as pretty print JSON.
     * @since 5.0.0
     * @param obj Object to be serialized.
     * @param minify Whether the resulting output should be minified or not. **Default** is `false`.
     * @return JSON output as [String].
     */
    public suspend fun serialize(obj: T, minify: Boolean = false): String
}