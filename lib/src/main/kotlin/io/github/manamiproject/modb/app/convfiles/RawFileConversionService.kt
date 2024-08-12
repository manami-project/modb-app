package io.github.manamiproject.modb.app.convfiles

import kotlinx.coroutines.TimeoutCancellationException

/**
 * Allows to check the conversion status of raw files.
 * Raw files are converted into an intermediate format with file suffix [CONVERTED_FILE_SUFFIX].
 * @since 1.0.0
 */
interface RawFileConversionService {

    /**
     * Checks if all raw files have been converted.
     * @since 1.0.0
     * @return `true` if files exist which haven't been converted yet.
     */
    suspend fun unconvertedFilesExist(): Boolean

    /**
     * Blocking call that waits until all raw files have been converted.
     * @since 1.0.0
     * @throws TimeoutCancellationException if files have not been converted within a waiting time of 10 seconds.
     */
    suspend fun waitForAllRawFilesToBeConverted()

    /**
     * Start conversion of raw files.
     * @since 1.0.0
     */
    suspend fun start(): Boolean

    /**
     * Shutdown service and free resources.
     * @since 1.0.0
     */
    suspend fun shutdown()
}