package io.github.manamiproject.modb.core.converter

import io.github.manamiproject.modb.core.anime.AnimeRaw

/**
 * Converts raw content in form of a [String] into an [AnimeRaw].
 * @since 1.0.0
 */
public interface AnimeConverter {

    /**
     * Converts a [String] into an [AnimeRaw].
     * @since 8.0.0
     * @param rawContent The raw content which will be converted to an [AnimeRaw].
     * @return Instance of [AnimeRaw].
     */
    public suspend fun convert(rawContent: String): AnimeRaw
}