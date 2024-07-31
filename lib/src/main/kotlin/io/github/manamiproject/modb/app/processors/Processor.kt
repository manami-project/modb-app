package io.github.manamiproject.modb.app.processors

/**
 * Generic processor.
 * @since 1.0.0
 */
interface Processor {

    /**
     * Execute processor.
     * @since 1.0.0
     */
    suspend fun process()
}