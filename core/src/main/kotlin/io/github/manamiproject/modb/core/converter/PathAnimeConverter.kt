package io.github.manamiproject.modb.core.converter

import io.github.manamiproject.modb.core.anime.AnimeRaw
import java.nio.file.Path

/**
 * Converts a single file or multiple files in a directory into a single [AnimeRaw] or a [List] of [AnimeRaw]s.
 * @since 1.0.0
 */
public interface PathAnimeConverter {

    /**
     * Converts a file into a single [AnimeRaw] (wrapped in a [Collection]) or a directory into a [Collection] of [AnimeRaw]s.
     * @since 8.0.0
     * @param path Can either be a file or a directory.
     * @return Converted [AnimeRaw].
     * @throws IllegalArgumentException if the given [Path] is neither file nor directory.
     */
    public suspend fun convert(path: Path): Collection<AnimeRaw>
}