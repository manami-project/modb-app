package io.github.manamiproject.modb.serde.json

import io.github.manamiproject.modb.core.extensions.RegularFile
import java.net.URL

/**
 * Deserializes external resources such as [RegularFile]s or [URL]s.
 * @since 5.0.0
 */
public interface ExternalResourceJsonDeserializer<out T> {

    /**
     * Deserializes content retrieved from an [URL] to a [List] of objects of type [T].
     * @since 5.0.0
     * @return [List] of objects of type [T].
     */
    public suspend fun deserialize(url: URL): T

    /**
     * Deserializes content retrieved from a [RegularFile] to a [List] of objects of type [T].
     * @since 5.0.0
     * @return [List] of objects of type [T].
     */
    public suspend fun deserialize(file: RegularFile): T
}