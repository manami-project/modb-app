package io.github.manamiproject.modb.core.extractor

import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import io.github.manamiproject.modb.core.extensions.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.nio.file.Path

/**
 * Uses a [DataExtractor] to extract from a single file or multiple files in a directory.
 * @since 11.1.0
 * @param dataExtractor Extractor for the raw content.
 * @param fileSuffix File suffix to determine which files to include.
 */
public class DefaultPathDataExtractor(
    private val dataExtractor: DataExtractor,
    private val fileSuffix: FileSuffix,
) : PathDataExtractor {

    override suspend fun extract(path: Path, selection: Map<OutputKey, Selector>): Collection<ExtractionResult> = withContext(LIMITED_CPU) {
        when{
            path.regularFileExists() -> convertSingleFile(path, selection)
            path.directoryExists() -> convertAllFilesInADirectory(path, selection)
            else -> throw IllegalArgumentException("Given path [$path] does not exist.")
        }
    }

    private suspend fun convertSingleFile(file: RegularFile, selection: Map<OutputKey, Selector>) = withContext(LIMITED_CPU) {
        listOf(dataExtractor.extract(file.readFile(), selection))
    }

    private suspend fun convertAllFilesInADirectory(path: Directory, selection: Map<OutputKey, Selector>): Collection<ExtractionResult> = withContext(LIMITED_FS) {
        val jobs = path.listRegularFiles(glob = "*$fileSuffix")
            .map {
                async {
                    convertSingleFile(it, selection)
                }
            }

        awaitAll(*jobs.toTypedArray()).flatten().toList()
    }
}