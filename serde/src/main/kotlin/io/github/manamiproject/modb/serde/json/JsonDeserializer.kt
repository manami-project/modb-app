package io.github.manamiproject.modb.serde.json

/**
 * Deserializes JSON to objects.
 * @since 5.0.0
 */
public interface JsonDeserializer<out T> {

    /**
     * Deserializes a valid JSON [String] to a list of objects of type [T].
     * @since 5.0.0
     * @return Object of type [T].
     */
    public suspend fun deserialize(json: String): T
}