package io.github.manamiproject.modb.app

import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.config.*
import io.github.manamiproject.modb.core.converter.PathAnimeConverter
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.serde.json.ExternalResourceJsonDeserializer
import io.github.manamiproject.modb.serde.json.JsonSerializer
import io.github.manamiproject.modb.serde.json.models.Dataset
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
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
import kotlin.time.Duration
import java.nio.file.WatchService as JavaWatchService

internal object TestConfigRegistry: ConfigRegistry {
    override fun boolean(key: String): Boolean = shouldNotBeInvoked()
    override fun double(key: String): Double = shouldNotBeInvoked()
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