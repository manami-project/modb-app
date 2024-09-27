package io.github.manamiproject.modb.app.crawlers

/**
 * Crawler that orchestrates variours helper classes necessary in order to download anime data from a specific
 * meta data provider.
 * @since 1.0.0
 */
internal interface Crawler {

    /**
     * Starts the crawler.
     * @since 1.0.0
     */
    suspend fun start()
}