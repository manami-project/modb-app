package io.github.manamiproject.modb.serde.json.serializer

/**
 * Serializes objects to [JSON lines](https://jsonlines.org).
 * @since 6.0.0
 */
public interface JsonLinesSerializer<in T> {

    /**
     * Creates JSON lines from a [Collection] of objects.
     * @since 6.0.0
     * @param obj Collection of object to be serialized as JSON lines.
     * @return JSON output as [String].
     */
    public suspend fun serialize(obj: Collection<T>): String
}