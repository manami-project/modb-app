package io.github.manamiproject.modb.app.crawler

/**
 * Name for the file which which contains the last page.
 * @since 1.0.0
 */
const val LAST_PAGE_MEMORIZER_FILE_NAME = "last_page.txt"

/**
 * Memorizes the last page for a crawler which does page-based crawling.
 * In case a restart is necessary the application doesn't have to iterate over all pages again.
 * This optimizes the process and further reduces the load on the meta data providers.
 * @since 1.0.0
 */
interface LastPageMemorizer<T> {

    /**
     * Memorize the current page.
     * @since 1.0.0
     * @param page Last pages crawled.
     */
    suspend fun memorizeLastPage(page: T)

    /**
     * Retrieve the last successfully crawled page.
     * @since 1.0.0
     * @return The last page which has been crawled successfully.
     */
    suspend fun retrieveLastPage(): T
}