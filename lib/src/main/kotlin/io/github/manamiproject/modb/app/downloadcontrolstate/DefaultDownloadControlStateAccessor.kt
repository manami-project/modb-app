package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.convfiles.CONVERTED_FILE_SUFFIX
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.deleteIfExists
import kotlin.io.path.moveTo

private typealias InternalKey = String

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

    private val downloadControlStateEntries = HashMap<InternalKey, DownloadControlStateEntry>()
    private val initializationMutex = Mutex()
    private val writeAccess = Mutex()
    private var isInitialized = false

    override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory {
        val dir = appConfig.downloadControlStateDirectory().resolve(metaDataProviderConfig.hostname())

        if (!dir.directoryExists()) {
            dir.createDirectories()
        }

        return dir
    }

    override suspend fun allDcsEntries(): List<DownloadControlStateEntry> {
        if (!isInitialized) {
            init()
        }

        return downloadControlStateEntries.values.toList()
    }

    override suspend fun allAnime(): List<Anime> {
        if (!isInitialized) {
            init()
        }

        return allDcsEntries().map { it.anime }
    }

    override suspend fun dcsEntryExists(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): Boolean {
        if (!isInitialized) {
            init()
        }

        return downloadControlStateEntries.containsKey(internalKey(metaDataProviderConfig, animeId))
    }

    override suspend fun dcsEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): DownloadControlStateEntry {
        if (!isInitialized) {
            init()
        }

        val internalKey = internalKey(metaDataProviderConfig, animeId)
        check(dcsEntryExists(metaDataProviderConfig, animeId)) { "Requested DCS entry with internal id [$internalKey] doesnt exist." }
        return downloadControlStateEntries[internalKey]!!
    }

    override suspend fun createOrUpdate(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId, downloadControlStateEntry: DownloadControlStateEntry): Boolean {
        if (!isInitialized) {
            init()
        }

        writeAccess.withLock {
            val subDir = downloadControlStateDirectory(metaDataProviderConfig)
            val downloadControlStateFile = subDir.resolve("$animeId.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")

            if (!dcsEntryExists(metaDataProviderConfig, animeId)) {
                log.debug { "Creating new DCS entry for [$animeId] of [${metaDataProviderConfig.hostname()}]." }
                Json.toJson(downloadControlStateEntry).writeToFile(downloadControlStateFile, true)
                downloadControlStateEntries[internalKey(metaDataProviderConfig, animeId)] = downloadControlStateEntry
                return true
            }

            val currentDownloadControlStateEntry = dcsEntry(metaDataProviderConfig, animeId)

            if (currentDownloadControlStateEntry.lastDownloaded == WeekOfYear.currentWeek()) {
                log.debug { "Not updating DCS file for [${animeId}] of [${metaDataProviderConfig.hostname()}], because it has been updated already." }
                return false
            }

            log.info { "Updating DCS entry for [$animeId] of [${metaDataProviderConfig.hostname()}]." }

            Json.toJson(downloadControlStateEntry).writeToFile(downloadControlStateFile, true)
            downloadControlStateEntries[internalKey(metaDataProviderConfig, animeId)] = downloadControlStateEntry
            return true
        }
    }

    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) {
        if (!isInitialized) {
            init()
        }

        writeAccess.withLock {
            val hasBeenDeleted = downloadControlStateDirectory(metaDataProviderConfig)
                .resolve("$animeId.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")
                .deleteIfExists()

            if (hasBeenDeleted) {
                log.debug { "Removed [${metaDataProviderConfig.hostname()}] DCS file for [$animeId]" }
            }

            downloadControlStateEntries.remove(internalKey(metaDataProviderConfig, animeId))

            val uri = metaDataProviderConfig.buildAnimeLink(animeId)

            if (mergeLockAccess.isPartOfMergeLock(uri)) {
                log.debug { "Removing merge.lock entry [$animeId] of [${metaDataProviderConfig.hostname()}]" }
                mergeLockAccess.removeEntry(uri)
            }
        }
    }

    override suspend fun changeId(oldId: AnimeId, newId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig): RegularFile {
        if (!isInitialized) {
            init()
        }

        return writeAccess.withLock {
            require(appConfig.canChangeAnimeIds(metaDataProviderConfig)) {
                "Called changeId for [${metaDataProviderConfig.hostname()}] which is not configured as a meta data provider that changes IDs."
            }

            log.debug { "Updating [$oldId] to [$newId] of [${metaDataProviderConfig.hostname()}]." }

            val file = downloadControlStateDirectory(metaDataProviderConfig).resolve("$oldId.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")
            check(file.regularFileExists()) { "[${metaDataProviderConfig.hostname()}] file [${file.fileName()}] doesn't exist." }

            log.debug { "Renaming [*.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX] file from [$oldId.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX] to [$newId.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX]." }

            val newFile = file.parent.resolve("$newId.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")
            file.moveTo(newFile, true)

            val oldUri = metaDataProviderConfig.buildAnimeLink(oldId)
            if (mergeLockAccess.isPartOfMergeLock(oldUri)) {
                mergeLockAccess.replaceUri(oldUri, metaDataProviderConfig.buildAnimeLink(newId))
            }

            log.debug { "Removing [*.$CONVERTED_FILE_SUFFIX] file." }
            appConfig.workingDir(metaDataProviderConfig).resolve("$oldId.$CONVERTED_FILE_SUFFIX").deleteIfExists()

            log.debug { "Removing [${metaDataProviderConfig.fileSuffix()}] file." }
            appConfig.workingDir(metaDataProviderConfig).resolve("$oldId.${metaDataProviderConfig.fileSuffix()}").deleteIfExists()

            newFile
        }
    }

    private suspend fun init() = withContext(LIMITED_FS) {
        initializationMutex.withLock {
            if (!isInitialized) {
                log.info { "Parsing all DCS entries." }

                val jobs = appConfig.metaDataProviderConfigurations()
                    .map { metaDataProviderConfig ->
                        downloadControlStateDirectory(metaDataProviderConfig) to metaDataProviderConfig
                    }
                    .map { (directory, config) ->
                        directory.listRegularFiles("*.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").map {
                            async {
                                parseAndCheckEntry(config, it)
                            }
                        }
                    }.flatten()

                awaitAll(*jobs.toTypedArray()).forEach { downloadControlStateEntry ->
                    downloadControlStateEntries[internalKey(downloadControlStateEntry)] = downloadControlStateEntry
                }

                isInitialized = true
            }
        }
    }

    private suspend fun parseAndCheckEntry(metaDataProviderConfig: MetaDataProviderConfig, file: RegularFile): DownloadControlStateEntry {
        log.debug { "Parsing and checking DCS file [${file.fileName()}] of [${metaDataProviderConfig.hostname()}]" }

        val dcsEntry = Json.parseJson<DownloadControlStateEntry>(file.readFile())!!

        check(metaDataProviderConfig.extractAnimeId(dcsEntry.anime.sources.first()) == file.fileName().substringBefore('.')) {
            "Filename and id don't match for [${file.fileName}] of [${metaDataProviderConfig.hostname()}]."
        }

        return dcsEntry
    }

    private fun internalKey(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): InternalKey = "${metaDataProviderConfig.hostname()}-$animeId"

    private fun internalKey(downloadControlStateEntry: DownloadControlStateEntry): InternalKey {
        val source = downloadControlStateEntry.anime.sources.first()
        val metaDataProviderConfig = appConfig.findMetaDataProviderConfig(source.host)
        val animeId = metaDataProviderConfig.extractAnimeId(source)
        return internalKey(metaDataProviderConfig, animeId)
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