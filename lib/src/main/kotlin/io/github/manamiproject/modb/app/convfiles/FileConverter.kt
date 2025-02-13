package io.github.manamiproject.modb.app.convfiles

import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.anime.AnimeRaw

/**
 * File suffix used for the files containing the serialized [AnimeRaw] object as intermediate format.
 * @since 1.0.0
 */
const val CONVERTED_FILE_SUFFIX = "conv"


/**
 * Converts raw files into a generic intermediate format based on [AnimeRaw].
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
     * @param file Raw file which is supposed the be converted to the intermediate format.
     */
    suspend fun convertFileToConvFile(file: RegularFile)
}