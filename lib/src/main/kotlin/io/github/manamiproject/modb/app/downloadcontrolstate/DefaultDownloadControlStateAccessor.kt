package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.convfiles.CONVERTED_FILE_SUFFIX
import io.github.manamiproject.modb.app.merging.lock.DefaultMergeLockAccessor
import io.github.manamiproject.modb.app.merging.lock.MergeLockAccessor
import io.github.manamiproject.modb.core.anime.AnimeRaw
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.date.WeekOfYear
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.moveTo

private typealias InternalKey = String

/**
 * Default implementation that allows access to DCS files.
 * If the metadata provider specific DCS directory doesn't exist, it will be created.
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property mergeLockAccess Access to merge locks.
 */
@OptIn(ExperimentalAtomicApi::class)
class DefaultDownloadControlStateAccessor(
    private val appConfig: Config = AppConfig.instance,
    private val mergeLockAccess: MergeLockAccessor = DefaultMergeLockAccessor.instance,
): DownloadControlStateAccessor {

    private val downloadControlStateEntries = AtomicReference<Map<InternalKey, DownloadControlStateEntry>>(emptyMap())
    private val initializationMutex = Mutex()
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

        return downloadControlStateEntries.load().values.map { originalDcs ->
            originalDcs.copy(
                _lastDownloaded = originalDcs.lastDownloaded.copy(),
                _nextDownload = originalDcs.nextDownload.copy(),
                _anime = originalDcs.anime.copy(
                    _sources = originalDcs.anime.sources.toHashSet(),
                    _synonyms = originalDcs.anime.synonyms.toHashSet(),
                    _relatedAnime = originalDcs.anime.relatedAnime.toHashSet(),
                    _tags = originalDcs.anime.tags.toHashSet(),
                ),
            ).apply {
                anime.addScores(originalDcs.anime.scores)
            }
        }.toList()
    }

    override suspend fun allDcsEntries(metaDataProviderConfig: MetaDataProviderConfig): List<DownloadControlStateEntry> {
        if (!isInitialized) {
            init()
        }

        return allDcsEntries().filter { it.anime.sources.first().host == metaDataProviderConfig.hostname() }
    }

    override suspend fun allAnime(): List<AnimeRaw> {
        if (!isInitialized) {
            init()
        }

        return allDcsEntries().map { it.anime }
    }

    override suspend fun allAnime(metaDataProviderConfig: MetaDataProviderConfig): List<AnimeRaw> {
        if (!isInitialized) {
            init()
        }

        return allAnime().filter { it.sources.first().host == metaDataProviderConfig.hostname() }
    }

    override suspend fun dcsEntryExists(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): Boolean {
        if (!isInitialized) {
            init()
        }

        return downloadControlStateEntries.load().containsKey(internalKey(metaDataProviderConfig, animeId))
    }

    override suspend fun dcsEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): DownloadControlStateEntry {
        if (!isInitialized) {
            init()
        }

        val internalKey = internalKey(metaDataProviderConfig, animeId)
        check(dcsEntryExists(metaDataProviderConfig, animeId)) { "Requested DCS entry with internal id [$internalKey] doesnt exist." }
        return downloadControlStateEntries.load()[internalKey]!!.copy()
    }

    override suspend fun createOrUpdate(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId, downloadControlStateEntry: DownloadControlStateEntry): Boolean {
        if (!isInitialized) {
            init()
        }

        val subDir = downloadControlStateDirectory(metaDataProviderConfig)
        val downloadControlStateFile = subDir.resolve("$animeId.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")

        if (!dcsEntryExists(metaDataProviderConfig, animeId)) {
            log.debug { "Creating new DCS entry for [$animeId] of [${metaDataProviderConfig.hostname()}]." }
            Json.toJson(downloadControlStateEntry).writeToFile(downloadControlStateFile)
            val newMap = downloadControlStateEntries.load().toMutableMap().also { it[internalKey(metaDataProviderConfig, animeId)] = downloadControlStateEntry }
            safelyStore(newMap)
            return true
        }

        val currentDownloadControlStateEntry = dcsEntry(metaDataProviderConfig, animeId)

        if (currentDownloadControlStateEntry.lastDownloaded == WeekOfYear.currentWeek()) {
            log.debug { "Not updating DCS file for [${animeId}] of [${metaDataProviderConfig.hostname()}], because it has been updated already." }
            return false
        }

        log.info { "Updating DCS entry for [$animeId] of [${metaDataProviderConfig.hostname()}]." }

        Json.toJson(downloadControlStateEntry).writeToFile(downloadControlStateFile)
        val newMap = downloadControlStateEntries.load().toMutableMap().also { it[internalKey(metaDataProviderConfig, animeId)] = downloadControlStateEntry }
        safelyStore(newMap)
        return true
    }

    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) {
        if (!isInitialized) {
            init()
        }

        val hasBeenDeleted = downloadControlStateDirectory(metaDataProviderConfig)
            .resolve("$animeId.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")
            .deleteIfExists()

        if (hasBeenDeleted) {
            log.debug { "Removed [${metaDataProviderConfig.hostname()}] DCS file for [$animeId]" }
        }

        val newMap = downloadControlStateEntries.load().toMutableMap().also { it.remove(internalKey(metaDataProviderConfig, animeId)) }
        safelyStore(newMap)

        val uri = metaDataProviderConfig.buildAnimeLink(animeId)

        if (mergeLockAccess.isPartOfMergeLock(uri)) {
            log.debug { "Removing merge.lock entry [$animeId] of [${metaDataProviderConfig.hostname()}]" }
            mergeLockAccess.removeEntry(uri)
        }
    }

    override suspend fun changeId(oldId: AnimeId, newId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig): RegularFile {
        if (!isInitialized) {
            init()
        }

        require(appConfig.canChangeAnimeIds(metaDataProviderConfig)) {
            "Called changeId for [${metaDataProviderConfig.hostname()}] which is not configured as a meta data provider that changes IDs."
        }

        log.debug { "Updating [$oldId] to [$newId] of [${metaDataProviderConfig.hostname()}]." }

        val file = downloadControlStateDirectory(metaDataProviderConfig).resolve("$oldId.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")
        check(file.regularFileExists()) { "[${metaDataProviderConfig.hostname()}] file [${file.fileName()}] doesn't exist." }

        log.debug { "Renaming [*.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX] file from [$oldId.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX] to [$newId.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX]." }

        val newFile = file.parent.resolve("$newId.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")
        file.moveTo(newFile, true)

        downloadControlStateEntries.load()[internalKey(metaDataProviderConfig, oldId)]?.let { entry ->
            val newMap = downloadControlStateEntries.load().toMutableMap()
            newMap[internalKey(metaDataProviderConfig, newId)] = entry
            newMap.remove(internalKey(metaDataProviderConfig, oldId))
            safelyStore(newMap)
        }

        val oldUri = metaDataProviderConfig.buildAnimeLink(oldId)
        if (mergeLockAccess.isPartOfMergeLock(oldUri)) {
            mergeLockAccess.replaceUri(oldUri, metaDataProviderConfig.buildAnimeLink(newId))
        }

        log.debug { "Removing [*.$CONVERTED_FILE_SUFFIX] file." }
        appConfig.workingDir(metaDataProviderConfig).resolve("$oldId.$CONVERTED_FILE_SUFFIX").deleteIfExists()

        log.debug { "Removing [${metaDataProviderConfig.fileSuffix()}] file." }
        appConfig.workingDir(metaDataProviderConfig).resolve("$oldId.${metaDataProviderConfig.fileSuffix()}").deleteIfExists()

        return newFile
    }

    override suspend fun highestIdAlreadyInDataset(metaDataProviderConfig: MetaDataProviderConfig): Int {
        log.info { "Finding the highest ID already in dataset for [${metaDataProviderConfig.hostname()}]." }

        val list = allAnime(metaDataProviderConfig)
            .map { it.sources.first() }
            .map { metaDataProviderConfig.extractAnimeId(it) }

        return if (list.isEmpty()) {
            0
        } else {
            list.maxOf { it.toIntOrNull() ?: 0 }
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

                val entries = mutableMapOf<InternalKey, DownloadControlStateEntry>()

                awaitAll(*jobs.toTypedArray()).forEach { downloadControlStateEntry ->
                    entries[internalKey(downloadControlStateEntry)] = downloadControlStateEntry
                }

                safelyStore(entries)

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

    @KoverIgnore
    private fun safelyStore(newMap: Map<InternalKey, DownloadControlStateEntry>) {
        while (true) {
            val current = downloadControlStateEntries.load()
            if (downloadControlStateEntries.compareAndSet(current, newMap)) break
        }
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