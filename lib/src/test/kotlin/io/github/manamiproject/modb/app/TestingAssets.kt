package io.github.manamiproject.modb.app

import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.config.*
import io.github.manamiproject.modb.core.converter.PathAnimeConverter
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import java.net.URI
import java.nio.file.Path
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

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