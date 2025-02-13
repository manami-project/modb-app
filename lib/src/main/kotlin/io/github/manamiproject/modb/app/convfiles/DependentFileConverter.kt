package io.github.manamiproject.modb.app.convfiles

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.PathAnimeConverter
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.anime.AnimeRaw
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.WatchService
import kotlin.io.path.exists
import kotlin.io.path.notExists

/**
 * Converts raw files into an intermediate format containing an [AnimeRaw].
 * This [FileConverter] is used for meta data providers which require multiple files to be able to create an [AnimeRaw].
 * It can handle [MetaDataProviderConfig] with differing values for [MetaDataProviderConfig.fileSuffix].
 * @since 1.0.0
 * @param appConfig Application specific configuration. Uses [AppConfig] by default.
 * @param dependentMetaDataProciderConfigs Additional configuration used for additional data like tags or related anime.
 * @property metaDataProviderConfig Configuration for a specific meta data provider. This is the "main" config.
 * @property converter Converts a [Path] to instances of [AnimeRaw]. Must match [metaDataProviderConfig].
 */
class DependentFileConverter @KoverIgnore constructor(
    appConfig: Config = AppConfig.instance,
    dependentMetaDataProciderConfigs: List<MetaDataProviderConfig>,
    private val metaDataProviderConfig: MetaDataProviderConfig,
    private val converter: PathAnimeConverter,
): FileConverter {

    private val outputDirectory = appConfig.workingDir(metaDataProviderConfig)
    private val metaDataConfigs = mutableMapOf<String, MetaDataProviderConfig>()
    private val watchServices = mutableMapOf<String, WatchService>()
    private val workingDirs = mutableMapOf<String, Directory>()

    init {
        val allConfigs = setOf(metaDataProviderConfig).union(dependentMetaDataProciderConfigs)
        require(allConfigs.map { it.hostname() }.toSet().size == 1) { "All configs must be from the same meta data provider." }

        allConfigs.forEach {
            metaDataConfigs[it.identityHashCode()] = it
            watchServices[it.identityHashCode()] = FileSystems.getDefault().newWatchService()
            workingDirs[it.identityHashCode()] = appConfig.workingDir(it)
        }
    }

    override suspend fun convertUnconvertedFiles() = withContext(LIMITED_FS) {
        log.info { "Converting unconverted files for [${metaDataProviderConfig.hostname()}]." }

        val allPossibleUnconvertedFiles = mutableSetOf<String>()

        workingDirs.forEach { dirEntry ->
            dirEntry.value.listRegularFiles("*.${metaDataConfigs[dirEntry.key]!!.fileSuffix()}")
                .map { it.fileName().remove(".${metaDataConfigs[dirEntry.key]!!.fileSuffix()}") }
                .forEach { file -> allPossibleUnconvertedFiles.add(file) }
        }

        allPossibleUnconvertedFiles.asSequence()
            .filter { fileName -> hasNotBeenConvertedYet(fileName) }
            .filter { fileName -> allRawFilesRequiredForConversionExist(fileName) }
            .map { fileName -> outputDirectory.resolve("$fileName.${metaDataProviderConfig.fileSuffix()}") }
            .chunked(250)
            .forEach {
                val jobs = it.map {
                    async {
                        convertFileToConvFile(it)
                    }
                }

                awaitAll(*jobs.toTypedArray())
            }

        log.info { "Finished converting unconverted files for [${metaDataProviderConfig.hostname()}]." }
    }

    override suspend fun convertFileToConvFile(file: RegularFile) {
        if (hasNotBeenConvertedYet(file.fileName()) && allRawFilesRequiredForConversionExist(file.fileName())) {
            log.debug { "Converting [${file.fileName()}] from [${metaDataConfigs.values.first().hostname()}]" }
            val anime = converter.convert(outputDirectory.resolve(file)).first()
            Json.toJson(anime).writeToFile(outputDirectory.resolve(createOutputFile(file)))
        }
    }

    private fun createOutputFile(originFile: RegularFile) = originFile.changeSuffix(CONVERTED_FILE_SUFFIX)

    private fun hasNotBeenConvertedYet(fileName: String): Boolean = createOutputFile(outputDirectory.resolve(fileName)).notExists()

    private fun allRawFilesRequiredForConversionExist(fileName: String): Boolean {
        var allFilesExist = true

        workingDirs.forEach { dirEntry ->
            val fileSuffix = metaDataConfigs[dirEntry.key]!!.fileSuffix()
            allFilesExist = allFilesExist && dirEntry.value.resolve(fileName).changeSuffix(fileSuffix).exists()
        }

        return allFilesExist
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}