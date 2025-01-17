package io.github.manamiproject.modb.core.converter

import io.github.manamiproject.modb.core.models.Anime

/**
 * Converts raw content in form of a [String] into an [Anime].
 * @since 1.0.0
 */
public interface AnimeConverter {

    /**
     * Converts a [String] into an [Anime].
     * @since 8.0.0
     * @param rawContent The raw content which will be converted to an [Anime].
     * @return Instance of [Anime].
     */
    public suspend fun convert(rawContent: String): Anime
}