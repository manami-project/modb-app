package io.github.manamiproject.modb.serde.json.deserializer

/**
 * Deserializes any given type [IN] to [OUT].
 * @since 5.0.0
 */
public interface Deserializer<in IN, out OUT> {

    /**
     * Deserializes a valid input to an output of type [OUT].
     * @since 6.0.0
     * @param source Valid input provided in the form of [IN].
     * @return Object of type [OUT].
     */
    public suspend fun deserialize(source: IN): OUT
}