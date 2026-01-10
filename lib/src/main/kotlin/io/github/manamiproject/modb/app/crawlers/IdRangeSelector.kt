package io.github.manamiproject.modb.app.crawlers

/**
 * Creates a list of anime IDs which need to be downloaded.
 * The IDs can be of any type.
 * @since 1.0.0
 */
interface IdRangeSelector<out ANIME_ID_TYPE> {

    /**
     * Creates a list of anime IDs which need to be downloaded.
     * @since 1.0.0
     */
    suspend fun idDownloadList(): List<ANIME_ID_TYPE>
}