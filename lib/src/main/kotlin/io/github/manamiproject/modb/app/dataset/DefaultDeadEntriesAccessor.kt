package io.github.manamiproject.modb.app.dataset

import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import io.github.manamiproject.modb.anisearch.AnisearchConfig
import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.dataset.DatasetFileType.*
import io.github.manamiproject.modb.app.downloadcontrolstate.DOWNLOAD_CONTROL_STATE_FILE_SUFFIX
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.livechart.LivechartConfig
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import io.github.manamiproject.modb.notify.NotifyConfig
import io.github.manamiproject.modb.serde.json.*
import io.github.manamiproject.modb.serde.json.models.DeadEntries
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.URI
import kotlin.io.path.createDirectories

/**
 * Handles the access to dead entries files.
 * Each supported meta data provider has its own dead entries file.
 * Not every meta data provider supports dead entries.
 * For some all IDs are loaded upfront and for some pagination is used. Those meta data providers are not supported for
 * most functions. Based on the existence of a DCS file they can howver return a value
 * for [DeadEntriesAccessor.determineDeadEntries].
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property downloadControlStateAccessor Access to DCS files.
 * @property jsonSerializer Serializer that creates the json files.
 * @property jsonDeserializer Deserializer for the json files.
 */
class DefaultDeadEntriesAccessor(
    private val appConfig: Config = AppConfig.instance,
    private val downloadControlStateAccessor: DownloadControlStateAccessor = DefaultDownloadControlStateAccessor.instance,
    private val jsonSerializer: JsonSerializer<Collection<AnimeId>> = DeadEntriesJsonSerializer.instance,
    private val jsonDeserializer: ExternalResourceJsonDeserializer<DeadEntries> = DefaultExternalResourceJsonDeserializer(
        deserializer = DeadEntriesJsonStringDeserializer.instance,
    ),
): DeadEntriesAccessor {

    private val deadEntries = DeadEntriesInMemory()
    private var isInitialized = false

    override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile {
        require(supportedMetaDataProviders.contains(metaDataProviderConfig)) { "Meta data provider [${metaDataProviderConfig.hostname()}] doesn't support dead entry files." }

        val hostnameWithoutTld = metaDataProviderConfig.hostname().split('.').first()
        val deadEntriesFolder = appConfig.outputDirectory().resolve("dead-entries")

        if (!deadEntriesFolder.directoryExists()) {
            deadEntriesFolder.createDirectories()
        }

        return when (type) {
            JSON -> deadEntriesFolder.resolve("$hostnameWithoutTld.json")
            JSON_MINIFIED -> deadEntriesFolder.resolve("$hostnameWithoutTld-minified.json")
            ZIP -> deadEntriesFolder.resolve("$hostnameWithoutTld.zip")
        }
    }

    override suspend fun addDeadEntry(animeId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig) {
        if (!isInitialized) {
            init()
        }

        log.info { "Adding [$animeId] from [${metaDataProviderConfig.hostname()}] to dead entries list" }

        if (supportedMetaDataProviders.contains(metaDataProviderConfig)) {
            Mutex().withLock {
                writeFileAndUpdateInMemoryData(animeId, metaDataProviderConfig)
            }
        }

        downloadControlStateAccessor.removeDeadEntry(animeId, metaDataProviderConfig)
    }

    override suspend fun determineDeadEntries(sources: Collection<URI>): Set<URI> {
        if (!isInitialized) {
            init()
        }

        return sources.filter { uri ->
            when(uri.host) {
                AnidbConfig.hostname() -> containsDeadEntry(uri)
                AnilistConfig.hostname() -> containsDeadEntry(uri)
                AnimePlanetConfig.hostname() -> entryNotExistsAsDcsFile(uri)
                AnisearchConfig.hostname() -> entryNotExistsAsDcsFile(uri)
                KitsuConfig.hostname() -> containsDeadEntry(uri)
                LivechartConfig.hostname() -> entryNotExistsAsDcsFile(uri)
                MyanimelistConfig.hostname() -> containsDeadEntry(uri)
                NotifyConfig.hostname() -> entryNotExistsAsDcsFile(uri)
                else -> throw IllegalArgumentException("Unable to fetch dead entries: No case defined for [${uri.host}].")
            }
        }.toSet()
    }

    override suspend fun fetchDeadEntries(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> {
        if (!isInitialized) {
            init()
        }

        return when (metaDataProviderConfig.hostname()) {
            AnidbConfig.hostname() -> deadEntries.anidb
            AnilistConfig.hostname() -> deadEntries.anilist
            KitsuConfig.hostname() -> deadEntries.kitsu
            MyanimelistConfig.hostname() -> deadEntries.mal
            else -> throw IllegalArgumentException("Meta data provider [${metaDataProviderConfig.hostname()}] is not supported.")
        }.toSet()
    }

    private suspend fun init() {
        Mutex().withLock {
            if (!isInitialized) {
                appConfig.metaDataProviderConfigurations()
                    .filter { supportedMetaDataProviders.contains(it) }
                    .forEach { metaDataProviderConfig ->
                        val file = deadEntriesFile(metaDataProviderConfig, JSON_MINIFIED)

                        if (!file.regularFileExists()) {
                            return@forEach
                        }

                        val parsedEntries = jsonDeserializer.deserialize(file)

                        when(metaDataProviderConfig.hostname()) {
                            AnidbConfig.hostname() -> deadEntries.anidb.addAll(parsedEntries.deadEntries)
                            AnilistConfig.hostname() -> deadEntries.anilist.addAll(parsedEntries.deadEntries)
                            KitsuConfig.hostname() -> deadEntries.kitsu.addAll(parsedEntries.deadEntries)
                            MyanimelistConfig.hostname() -> deadEntries.mal.addAll(parsedEntries.deadEntries)
                            else -> throw IllegalStateException("Meta data provider [${metaDataProviderConfig.hostname()}] is not supported.")
                        }
                    }
                isInitialized = true
            }
        }
    }

    private suspend fun writeFileAndUpdateInMemoryData(animeId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig) {
        val deadEntries: HashSet<AnimeId> = when(metaDataProviderConfig.hostname()) {
            AnidbConfig.hostname() -> {
                deadEntries.anidb.add(animeId)
                deadEntries.anidb
            }
            AnilistConfig.hostname() -> {
                deadEntries.anilist.add(animeId)
                deadEntries.anilist
            }
            KitsuConfig.hostname() -> {
                deadEntries.kitsu.add(animeId)
                deadEntries.kitsu
            }
            MyanimelistConfig.hostname() -> {
                deadEntries.mal.add(animeId)
                deadEntries.mal
            }
            else -> throw IllegalStateException("Meta data provider [${metaDataProviderConfig.hostname()}] is not supported.")
        }

        jsonSerializer.serialize(deadEntries, minify = false).writeToFile(deadEntriesFile(metaDataProviderConfig, JSON))
        jsonSerializer.serialize(deadEntries, minify = true).writeToFile(deadEntriesFile(metaDataProviderConfig, JSON_MINIFIED))
        deadEntriesFile(metaDataProviderConfig, ZIP).createZipOf(deadEntriesFile(metaDataProviderConfig, JSON_MINIFIED))
    }

    private fun containsDeadEntry(uri: URI): Boolean {
        val config = appConfig.findMetaDataProviderConfig(uri.host)
        val id = config.extractAnimeId(uri)

        return when(config.hostname()) {
            AnidbConfig.hostname() -> deadEntries.anidb.contains(id)
            AnilistConfig.hostname() -> deadEntries.anilist.contains(id)
            KitsuConfig.hostname() -> deadEntries.kitsu.contains(id)
            MyanimelistConfig.hostname() -> deadEntries.mal.contains(id)
            else -> throw IllegalStateException("Unable to fetch dead entries: No case defined for given config.")
        }
    }

    private fun entryNotExistsAsDcsFile(uri: URI): Boolean {
        val config = appConfig.findMetaDataProviderConfig(uri.host)
        val id = config.extractAnimeId(uri)
        val dcsFile = downloadControlStateAccessor.downloadControlStateDirectory(config).resolve("$id.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")

        return !dcsFile.regularFileExists()
    }

    companion object {
        private val log by LoggerDelegate()
        private val supportedMetaDataProviders = setOf(AnidbConfig, AnilistConfig, KitsuConfig, MyanimelistConfig)

        /**
         * Singleton of [DefaultDeadEntriesAccessor]
         * @since 1.0.0
         */
        val instance: DefaultDeadEntriesAccessor by lazy { DefaultDeadEntriesAccessor() }
    }
}

private data class DeadEntriesInMemory(
    val anidb: HashSet<AnimeId> = HashSet(),
    val anilist: HashSet<AnimeId> = HashSet(),
    val kitsu: HashSet<AnimeId> = HashSet(),
    val mal: HashSet<AnimeId> = HashSet(),
)