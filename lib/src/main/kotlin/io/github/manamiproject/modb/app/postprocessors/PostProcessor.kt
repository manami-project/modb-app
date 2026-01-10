package io.github.manamiproject.modb.app.postprocessors

/**
 * Processor which runs after crawling, merging and creating the dataset files.
 * @since 1.0.0
 */
interface PostProcessor {

    /**
     * Execute processor.
     * @since 1.0.0
     * @return `true` if execution ended successfully.
     * @throws IllegalStateException in case of an error.
     */
    suspend fun process(): Boolean
}