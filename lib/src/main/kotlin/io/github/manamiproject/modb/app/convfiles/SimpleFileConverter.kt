package io.github.manamiproject.modb.app.convfiles

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.PathAnimeConverter
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.nio.file.Path

/**
 * A simple [FileConverter] which allows the conversion of raw files for a specific meta data provider.
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property metaDataProviderConfig Configuration for a specific meta data provider.
 * @property converter Converts a [Path] to instances of [Anime]. Must match [metaDataProviderConfig].
 */
class SimpleFileConverter(
    private val appConfig: Config = AppConfig.instance,
    private val metaDataProviderConfig: MetaDataProviderConfig,
    private val converter: PathAnimeConverter,
): FileConverter {

    override suspend fun convertUnconvertedFiles(): Unit = withContext(LIMITED_FS) {
        log.info { "Converting unconverted files for [${metaDataProviderConfig.hostname()}]." }

        val workingDir = appConfig.workingDir(metaDataProviderConfig)
        val unconvertedFiles = workingDir.listRegularFiles("*.${metaDataProviderConfig.fileSuffix()}")
            .filterNot { it.changeSuffix(CONVERTED_FILE_SUFFIX).regularFileExists() }

        val jobs = unconvertedFiles.map {
            async {
                convertFileToConvFile(it)
            }
        }

        awaitAll(*jobs.toTypedArray())

        log.info { "Finished converting unconverted files for [${metaDataProviderConfig.hostname()}]." }
    }

    override suspend fun convertFileToConvFile(file: RegularFile) {
        log.debug { "Converting [${file.fileName()}] from [${metaDataProviderConfig.hostname()}]" }

        val anime = converter.convert(file).first()
        Json.toJson(anime).writeToFile(createOutputFile(file))
    }

    private fun createOutputFile(originFile: RegularFile) = originFile.changeSuffix(CONVERTED_FILE_SUFFIX)

    companion object {
        private val log by LoggerDelegate()
    }
}