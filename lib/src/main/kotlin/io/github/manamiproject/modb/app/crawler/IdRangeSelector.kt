package io.github.manamiproject.modb.app.crawler

/**
 * Creates a list of anime IDs which need to be downloaded.
 * The IDs can be of any type.
 * @since 1.0.0
 */
interface IdRangeSelector<out T> {

    /**
     * Creates a list of anime IDs which need to be downloaded.
     * @since 1.0.0
     */
    suspend fun idDownloadList(): List<T>
}