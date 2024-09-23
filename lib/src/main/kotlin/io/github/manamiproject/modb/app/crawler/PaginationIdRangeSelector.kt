package io.github.manamiproject.modb.app.crawler

import io.github.manamiproject.modb.core.config.AnimeId

/**
 * Creates a list of anime IDs which need to be downloaded.
 * This selector is for crawlers which use pagination.
 * The identifiers for the pages can be of any type.
 * @since 1.0.0
 */
interface PaginationIdRangeSelector<PAGETYPE> {

    /**
     * Returns a list of anime IDs which appeard on the given page and need to be downloaded.
     * @since 1.0.0
     * @param page Identifier of the page from which the anime IDs are being fetched.
     * @return A list of anime IDs.
     */
    suspend fun idDownloadList(page: PAGETYPE): List<AnimeId>
}