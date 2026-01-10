package io.github.manamiproject.modb.app.crawlers

/**
 * Crawler that orchestrates various helper classes necessary in order to download anime data from a specific
 * metadata provider.
 * @since 1.0.0
 */
interface Crawler {

    /**
     * Starts the crawler.
     * @since 1.0.0
     */
    suspend fun start()
}