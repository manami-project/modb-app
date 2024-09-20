package io.github.manamiproject.modb.app.crawler

/**
 * Can detect the highest id.
 * Normally this would be the highest id of an anime in case the meta data provider uses integer based anime ids.
 * It can also be used to detect the last page for page based crawler.
 * @since 1.0.0
 */
interface HighestIdDetector {

    /**
     * Returns the highest id.
     * @since 1.0.0
     * @return Id as [Int].
     * @throws IllegalStateException if the id cannot be retrieved.
     */
    suspend fun detectHighestId(): Int
}