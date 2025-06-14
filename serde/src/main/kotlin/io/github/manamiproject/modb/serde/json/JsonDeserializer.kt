package io.github.manamiproject.modb.serde.json

import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream

/**
 * Deserializes JSON to objects.
 * @since 5.0.0
 */
public interface JsonDeserializer<out T> {

    /**
     * Deserializes a valid JSON [String] to objects of type [T].
     * @since 5.0.0
     * @param json Valid JSON in the form of a [String].
     * @return Object of type [T].
     */
    public suspend fun deserialize(json: String): T

    /**
     * Deserializes a valid JSON to objects of type [T].
     * @since 19.0.0
     * @param jsonInputStream Valid JSON provided via [LifecycleAwareInputStream].
     * @return Object of type [T].
     */
    public suspend fun deserialize(jsonInputStream: LifecycleAwareInputStream): T
}