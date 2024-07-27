package io.github.manamiproject.modb.app.convfiles

/**
 * Actively watches directories for changes.
 * @since 1.0.0
 */
interface WatchService {

    /**
     * Execute anything that has to be done before starting to watch directories.
     * @since 1.0.0
     */
    suspend fun prepare()

    /**
     * Start watching directories.
     * @since 1.0.0
     */
    suspend fun watch()

    /**
     * Stop watching directories.
     * @since 1.0.0
     */
    suspend fun stop()
}