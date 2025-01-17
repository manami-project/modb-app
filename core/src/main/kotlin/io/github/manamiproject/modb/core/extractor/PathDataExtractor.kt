package io.github.manamiproject.modb.core.extractor

import io.github.manamiproject.modb.core.models.Anime
import java.nio.file.Path

/**
 * Extracts data from a single file or multiple files in a directory into a single [Anime] or a [List] of [Anime]s.
 * @since 11.1.0
 */
public interface PathDataExtractor {

    /**
     * Extracts data from a file or multiple files of a directory either using XPath for HTML/XML or JsonPath for JSON.
     * @since 11.1.0
     * @param path Can either be a file or a directory.
     * @return A [Collection] of [Map]s containing the resulting data. Key is the identifier corresponding to the key
     * from [selection]. The value is the identified data from files based on the XPath or JsonPath.
     * @throws IllegalArgumentException if the given [Path] is neither file nor directory.
     */
    public suspend fun extract(path: Path, selection: Map<OutputKey, Selector>): Collection<ExtractionResult>
}