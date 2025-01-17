package io.github.manamiproject.modb.core.converter

import io.github.manamiproject.modb.core.models.Anime
import java.nio.file.Path

/**
 * Converts a single file or multiple files in a directory into a single [Anime] or a [List] of [Anime]s.
 * @since 1.0.0
 */
public interface PathAnimeConverter {

    /**
     * Converts a file into a single [Anime] (wrapped in a [Collection]) or a directory into a [Collection] of [Anime]s.
     * @since 8.0.0
     * @param path Can either be a file or a directory.
     * @return Converted [Anime].
     * @throws IllegalArgumentException if the given [Path] is neither file nor directory.
     */
    public suspend fun convert(path: Path): Collection<Anime>
}