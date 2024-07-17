package io.github.manamiproject.modb.app.convfiles

import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.extensions.RegularFile

/**
 * File suffix used for the files containing the serialized [Anime] object as intermediate format.
 * @since 1.0.0
 */
const val CONVERTED_FILE_SUFFIX = "conv"


/**
 * Converts raw files into a generic intermediate format based on [Anime].
 * @since 1.0.0
 */
interface FileConverter {

    /**
     * Converts all raw files which haven't been converted yet.
     * @since 1.0.0
     */
    suspend fun convertUnconvertedFiles()

    /**
     * Converts a specific raw file to into the generic intermediate format.
     * @since 1.0.0
     */
    suspend fun convertFileToConvFile(file: RegularFile)
}