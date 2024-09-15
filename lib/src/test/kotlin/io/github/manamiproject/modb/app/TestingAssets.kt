package io.github.manamiproject.modb.app

import io.github.manamiproject.kommand.CommandExecutor
import io.github.manamiproject.kommand.CommandLineConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.dataset.DatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DatasetFileType
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateEntry
import io.github.manamiproject.modb.app.merging.ReviewedIsolatedEntriesAccessor
import io.github.manamiproject.modb.app.merging.lock.MergeLock
import io.github.manamiproject.modb.app.merging.lock.MergeLockAccessor
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.ConfigRegistry
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.PathAnimeConverter
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.httpclient.RequestBody
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.serde.json.ExternalResourceJsonDeserializer
import io.github.manamiproject.modb.serde.json.JsonSerializer
import io.github.manamiproject.modb.serde.json.models.Dataset
import io.github.manamiproject.modb.serde.json.models.DeadEntries
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import java.net.URI
import java.net.URL
import java.nio.file.Path
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.Watchable
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit
import java.nio.file.WatchService as JavaWatchService

internal object TestConfigRegistry: ConfigRegistry {
    override fun boolean(key: String): Boolean = shouldNotBeInvoked()
    override fun double(key: String): Double = shouldNotBeInvoked()
    override fun int(key: String): Int? = shouldNotBeInvoked()
    override fun <T : Any> list(key: String): List<T> = shouldNotBeInvoked()
    override fun localDate(key: String): LocalDate = shouldNotBeInvoked()
    override fun localDateTime(key: String): LocalDateTime = shouldNotBeInvoked()
    override fun long(key: String): Long = shouldNotBeInvoked()
    override fun <T : Any> map(key: String): Map<String, T> = shouldNotBeInvoked()
    override fun offsetDateTime(key: String): OffsetDateTime = shouldNotBeInvoked()
    override fun string(key: String): String = shouldNotBeInvoked()
}

internal object TestMetaDataProviderConfig: MetaDataProviderConfig {
    override fun hostname() = shouldNotBeInvoked()
    override fun buildAnimeLink(id: AnimeId): URI = shouldNotBeInvoked()
    override fun buildDataDownloadLink(id: String): URI = shouldNotBeInvoked()
    override fun extractAnimeId(uri: URI): AnimeId = shouldNotBeInvoked()
    override fun fileSuffix(): FileSuffix = shouldNotBeInvoked()
}

internal object TestAppConfig: Config {
    override fun isTestContext(): Boolean = true
    override fun currentWeekWorkingDir(): Directory = shouldNotBeInvoked()
    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = shouldNotBeInvoked()
    override fun outputDirectory(): Directory = shouldNotBeInvoked()
    override fun downloadControlStateDirectory(): Directory = shouldNotBeInvoked()
    override fun downloadsDirectory(): Directory = shouldNotBeInvoked()
    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = shouldNotBeInvoked()
    override fun clock(): Clock = shouldNotBeInvoked()
    override fun canChangeAnimeIds(metaDataProviderConfig: MetaDataProviderConfig): Boolean = shouldNotBeInvoked()
}

internal object TestPathAnimeConverter: PathAnimeConverter {
    override suspend fun convert(path: Path): Collection<Anime> = shouldNotBeInvoked()
}

internal object TestExternalResourceJsonDeserializerDataset: ExternalResourceJsonDeserializer<Dataset> {
    override suspend fun deserialize(url: URL): Dataset = shouldNotBeInvoked()
    override suspend fun deserialize(file: RegularFile): Dataset = shouldNotBeInvoked()
}

internal object TestJsonSerializerCollectionAnime: JsonSerializer<Collection<Anime>> {
    override suspend fun serialize(obj: Collection<Anime>, minify: Boolean): String = shouldNotBeInvoked()
}

internal object TestJavaWatchService: JavaWatchService {
    override fun close() = shouldNotBeInvoked()
    override fun poll(): WatchKey = shouldNotBeInvoked()
    override fun poll(timeout: Long, unit: TimeUnit?): WatchKey = shouldNotBeInvoked()
    override fun take(): WatchKey = shouldNotBeInvoked()
}

internal object TestWatchKey: WatchKey {
    override fun isValid(): Boolean = shouldNotBeInvoked()
    override fun pollEvents(): MutableList<WatchEvent<*>> = shouldNotBeInvoked()
    override fun reset(): Boolean = shouldNotBeInvoked()
    override fun cancel() = shouldNotBeInvoked()
    override fun watchable(): Watchable = shouldNotBeInvoked()
}

internal object TestDownloadControlStateAccessor: DownloadControlStateAccessor {
    override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = shouldNotBeInvoked()
    override suspend fun allAnime(): List<Anime> = shouldNotBeInvoked()
    override suspend fun allDcsEntries(): List<DownloadControlStateEntry> = shouldNotBeInvoked()
    override suspend fun dcsEntryExists(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): Boolean = shouldNotBeInvoked()
    override suspend fun dcsEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): DownloadControlStateEntry = shouldNotBeInvoked()
    override suspend fun createOrUpdate(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId, downloadControlStateEntry: DownloadControlStateEntry) = shouldNotBeInvoked()
    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) = shouldNotBeInvoked()
    override suspend fun changeId(oldId: AnimeId, newId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig): RegularFile = shouldNotBeInvoked()
}

internal object TestDatasetFileAccessor: DatasetFileAccessor {
    override suspend fun fetchEntries(): List<Anime> = shouldNotBeInvoked()
    override suspend fun saveEntries(anime: List<Anime>) = shouldNotBeInvoked()
    override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = shouldNotBeInvoked()
}

internal object TestMergeLockAccessor: MergeLockAccessor {
    override suspend fun hasMergeLock(uris: Set<URI>): Boolean = shouldNotBeInvoked()
    override suspend fun isPartOfMergeLock(uri: URI): Boolean = shouldNotBeInvoked()
    override suspend fun getMergeLock(uri: URI): MergeLock = shouldNotBeInvoked()
    override suspend fun addMergeLock(mergeLock: MergeLock) = shouldNotBeInvoked()
    override suspend fun replaceUri(oldUri: URI, newUri: URI) = shouldNotBeInvoked()
    override suspend fun removeEntry(uri: URI) = shouldNotBeInvoked()
    override suspend fun allSourcesInAllMergeLockEntries(): Set<URI> = shouldNotBeInvoked()
}

internal object TestExternalResourceJsonDeserializerDeadEntries: ExternalResourceJsonDeserializer<DeadEntries> {
    override suspend fun deserialize(url: URL): DeadEntries = shouldNotBeInvoked()
    override suspend fun deserialize(file: RegularFile): DeadEntries = shouldNotBeInvoked()
}

internal object TestJsonSerializerCollectionAnimeId: JsonSerializer<Collection<AnimeId>> {
    override suspend fun serialize(obj: Collection<AnimeId>, minify: Boolean): String = shouldNotBeInvoked()
}

internal object TestDeadEntriesAccessor: DeadEntriesAccessor {
    override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = shouldNotBeInvoked()
    override suspend fun addDeadEntry(animeId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig) = shouldNotBeInvoked()
    override suspend fun determineDeadEntries(sources: Collection<URI>): Set<URI> = shouldNotBeInvoked()
    override suspend fun fetchDeadEntries(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = shouldNotBeInvoked()
}

internal object TestReviewedIsolatedEntriesAccessor: ReviewedIsolatedEntriesAccessor {
    override fun contains(uri: URI): Boolean = shouldNotBeInvoked()
    override suspend fun addCheckedEntry(uri: URI) = shouldNotBeInvoked()
}

internal object TestCommandExecutor: CommandExecutor {
    override var config: CommandLineConfig
        get() = shouldNotBeInvoked()
        set(_) = shouldNotBeInvoked()
    override fun executeCmd(command: List<String>): String = shouldNotBeInvoked()
}

internal object TestHttpClient: HttpClient {
    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = shouldNotBeInvoked()
    override suspend fun post(url: URL, requestBody: RequestBody, headers: Map<String, Collection<String>>): HttpResponse = shouldNotBeInvoked()
}