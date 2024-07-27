package io.github.manamiproject.modb.app.convfiles

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.PathAnimeConverter
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime
import kotlinx.coroutines.*
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import kotlin.io.path.deleteIfExists
import kotlin.io.path.isRegularFile

/**
 * Used for meta data provider which only require a single file in order to convert raw data into an [Anime] object.
 * Watches the directory of a [MetaDataProviderConfig] for changes and uses a [SimpleFileConverter] to convert the
 * raw data into the intermediate format.
 * @since 1.0.0
 * @param appConfig Application specific configuration. Uses [AppConfig] by default.
 * @param converter Instance used to convert multiple files into the intermediate format which represents an [Anime].
 * @property metaDataProviderConfig Configuration for a specific meta data provider.
 */
class SimpleConversionWatchService(
    appConfig: Config = AppConfig.instance,
    converter: PathAnimeConverter,
    private val metaDataProviderConfig: MetaDataProviderConfig
): WatchService {

    private var isPrepared = false
    private val watchService = FileSystems.getDefault().newWatchService()
    private val workingDir = appConfig.workingDir(metaDataProviderConfig)
    private val fileConverter = SimpleFileConverter(
        appConfig = appConfig,
        metaDataProviderConfig = metaDataProviderConfig,
        converter = converter,
    )

    override suspend fun prepare() = withContext(LIMITED_FS) {
        deleteLocks()
        fileConverter.convertUnconvertedFiles()
        workingDir.register(watchService, ENTRY_CREATE)
        isPrepared = true
    }

    override suspend fun watch() = withContext(LIMITED_FS) {
        if (!isPrepared) {
            prepare()
        }

        var key = watchService.longPoll()

        while (key != null) {
            val dir = (key.watchable() as Path)
            val events = key.pollEvents()

            events.asSequence()
                .filter { it.kind() == ENTRY_CREATE }
                .map { it.context() as Path }
                .map { dir.resolve(it) }
                .filter { it.isRegularFile() }
                .filter { it.fileSuffix() == metaDataProviderConfig.fileSuffix() }
                .forEach {
                    val file = dir.resolve(it)
                    waitWhileFileIsBeingWrittenAsync(file).join()
                    fileConverter.convertFileToConvFile(file)
                }

            key.reset()
            key = watchService.longPoll()
        }
    }

    override suspend fun stop() = withContext(LIMITED_FS) {
        watchService.close()
    }

    private suspend fun deleteLocks() {
        log.info { "Deleting [$LOCK_FILE_SUFFIX] files in [$workingDir]" }

        workingDir.listRegularFiles("*.$LOCK_FILE_SUFFIX").forEach {
            log.debug { "Deleting [$it]" }
            it.deleteIfExists()
        }
    }

    private suspend fun waitWhileFileIsBeingWrittenAsync(file: RegularFile) = withContext(LIMITED_FS) {
        launch {
            val expectedFile = file.changeSuffix(LOCK_FILE_SUFFIX)
            while (expectedFile.regularFileExists() && isActive) {
                delay(1)
            }
        }
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}