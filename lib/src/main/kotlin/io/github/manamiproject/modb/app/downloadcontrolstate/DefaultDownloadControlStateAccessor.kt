package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.merging.lock.DefaultMergeLockAccess
import io.github.manamiproject.modb.app.merging.lock.MergeLockAccess
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists

/**
 * Default implemenation that allows access to DCS files.
 * If the meta data provider specific DCS directory doesn't exist, it will be created.
 * Files are parsed each time you call functions.
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property mergeLockAccess Access to merge locks.
 */
class DefaultDownloadControlStateAccessor(
    private val appConfig: Config = AppConfig.instance,
    private val mergeLockAccess: MergeLockAccess = DefaultMergeLockAccess.instance,
): DownloadControlStateAccessor {

    override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory {
        val dir = appConfig.downloadControlStateDirectory().resolve(metaDataProviderConfig.hostname())

        if (!dir.directoryExists()) {
            dir.createDirectories()
        }

        return dir
    }

    override suspend fun allAnime(): List<Anime> = allDcsEntries().map { it.anime }

    override suspend fun allDcsEntries(): List<DownloadControlStateEntry> = withContext(LIMITED_FS) {
        log.info { "Parsing all DCS entries." }

        val jobs = appConfig.metaDataProviderConfigurations()
            .map { config-> downloadControlStateDirectory(config) to config }
            .map { (directory, config) ->
                directory.listRegularFiles("*.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").map {
                    async {
                        checkAndExtractEntry(config, it)
                    }
                }
            }.flatten()

        return@withContext awaitAll(*jobs.toTypedArray())
    }

    override suspend fun removeDeadEntry(id: AnimeId, metaDataProviderConfig: MetaDataProviderConfig) {
        val hasBeenDeleted = downloadControlStateDirectory(metaDataProviderConfig)
            .resolve("$id.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")
            .deleteIfExists()

        if (hasBeenDeleted) {
            log.debug { "Removed [${metaDataProviderConfig.hostname()}] DCS file for [$id]" }
        }

        val uri = metaDataProviderConfig.buildAnimeLink(id)

        if (mergeLockAccess.isPartOfMergeLock(uri)) {
            log.debug { "Removing merge.lock entry [$id] of [${metaDataProviderConfig.hostname()}]" }
            mergeLockAccess.removeEntry(uri)
        }
    }

    private suspend fun checkAndExtractEntry(config: MetaDataProviderConfig, file: RegularFile): DownloadControlStateEntry {
        log.debug { "Parsing and checking DCS file [${file.fileName()}] of [${config.hostname()}]" }

        val dcsEntry = Json.parseJson<DownloadControlStateEntry>(file.readFile())!!

        check(config.extractAnimeId(dcsEntry.anime.sources.first()) == file.fileName().substringBefore('.')) {
            "Filename and id don't match for [${file.fileName}] of [${config.hostname()}]."
        }

        return dcsEntry
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DefaultDownloadControlStateAccessor]
         * @since 1.0.0
         */
        val instance: DefaultDownloadControlStateAccessor by lazy { DefaultDownloadControlStateAccessor() }
    }
}