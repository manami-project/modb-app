package io.github.manamiproject.modb.app.convfiles

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.PathAnimeConverter
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.excludeFromTestContext
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.anime.AnimeRaw
import kotlinx.coroutines.*
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.io.path.deleteIfExists
import java.nio.file.WatchService as JavaWatchService

private typealias IdentityHashCode = String

/**
 * Used for meta data provider which require multiple files in order to convert raw data into an [AnimeRaw] object.
 * Watches multiple directories for changes based on the different [MetaDataProviderConfig]s.
 * As soon as all data is available a [DependentFileConverter] is used to convert the files.
 * @since 1.0.0
 * @param converter Instance used to convert multiple files into the intermediate format which represents an [AnimeRaw].
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property mainConfig Configuration for a specific meta data provider. This is the "main" config.
 * @property dependentMetaDataProciderConfigs Additional configuration used for additional data like tags or related anime.
 */
class DependentConversionWatchService(
    converter: PathAnimeConverter,
    private val appConfig: Config = AppConfig.instance,
    private val mainConfig: MetaDataProviderConfig,
    private val dependentMetaDataProciderConfigs: List<MetaDataProviderConfig>,
): WatchService {

    private var isPrepared = false
    private val watchServices = mutableMapOf<IdentityHashCode, JavaWatchService>()
    private val metaDataConfigs = mutableMapOf<IdentityHashCode, MetaDataProviderConfig>()
    private val workingDirs = mutableMapOf<IdentityHashCode, Directory>()
    private val validFileSuffixes = mutableSetOf<FileSuffix>()
    private val jobs = CopyOnWriteArrayList(mutableListOf<Job>())

    private val dependentFileConverter = DependentFileConverter(
        appConfig = appConfig,
        dependentMetaDataProciderConfigs = dependentMetaDataProciderConfigs,
        metaDataProviderConfig = mainConfig,
        converter = converter,
    )

    init {
        require(setOf(mainConfig).union(dependentMetaDataProciderConfigs).map { it.hostname() }.toSet().size == 1) { "All configs must be from the same meta data provider." }
    }

    override suspend fun prepare() = withContext(LIMITED_FS) {
        setOf(mainConfig).union(dependentMetaDataProciderConfigs).forEach { config ->
            val watchService = FileSystems.getDefault().newWatchService()
            val workingDir = appConfig.workingDir(config)

            workingDir.register(watchService, ENTRY_CREATE)

            watchServices[config.identityHashCode()] = watchService
            workingDirs[config.identityHashCode()] = workingDir
            metaDataConfigs[config.identityHashCode()] = config
            validFileSuffixes.addAll(metaDataConfigs.map { it.value.fileSuffix() })
        }
        workingDirs.values.forEach { deleteLocks(it) }
        dependentFileConverter.convertUnconvertedFiles()
        isPrepared = true
    }

    override suspend fun watch() = withContext(LIMITED_FS) {
        if (!isPrepared) {
            prepare()
        }

        watchServices.values.forEach {
            val job = launch {
                processEvents(it)
            }
            jobs.add(job)
        }
    }

    override suspend fun stop() = withContext(LIMITED_FS) {
        watchServices.values.forEach { it.close() }
        jobs.forEach { it.cancelAndJoin() }
    }

    private suspend fun deleteLocks(workingDir: Directory) {
        log.info { "Deleting [$LOCK_FILE_SUFFIX] files in [$workingDir]" }

        workingDir.listRegularFiles("*.$LOCK_FILE_SUFFIX").forEach {
            log.debug { "Deleting [$it]" }
            it.deleteIfExists()
        }
    }

    private suspend fun processEvents(watchService: JavaWatchService) {
        var key = watchService.longPoll()

        while (key != null) {
            key.pollEvents().asSequence()
                .filter { it.kind() == ENTRY_CREATE }
                .map { it.context() as Path }
                .filter { hasValidSuffix(it) }
                .filter { allFilesForConversionExist(it) }
                .forEach { convertFile(it) }

            key.reset()
            key = watchService.longPoll()
        }
    }

    private fun hasValidSuffix(file: RegularFile): Boolean = validFileSuffixes.contains(file.fileSuffix())

    private fun allFilesForConversionExist(file: RegularFile): Boolean {
        var allFilesExist = true

        workingDirs.forEach {
            val fileSuffix = metaDataConfigs[it.key]!!.fileSuffix()
            val fileName = file.fileName.changeSuffix(fileSuffix)
            allFilesExist = allFilesExist && it.value.resolve(fileName).regularFileExists()
        }

        return allFilesExist
    }

    private suspend fun convertFile(file: RegularFile) {
        workingDirs.values.forEach {
            waitWhileFileIsBeingWrittenAsync(it.resolve(file))
        }

        val outputDir = appConfig.workingDir(mainConfig)
        dependentFileConverter.convertFileToConvFile(outputDir.resolve(file))
    }

    private suspend fun waitWhileFileIsBeingWrittenAsync(file: RegularFile) = withContext(LIMITED_FS) {
        val expectedFile = file.changeSuffix(LOCK_FILE_SUFFIX)
        while (expectedFile.regularFileExists() && isActive) {
            wait()
        }
    }

    @KoverIgnore
    private suspend fun wait() {
        excludeFromTestContext(appConfig) {
            delay(100)
        }
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}