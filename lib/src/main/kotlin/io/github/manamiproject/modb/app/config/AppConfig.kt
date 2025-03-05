package io.github.manamiproject.modb.app.config

import io.github.manamiproject.AnimenewsnetworkConfig
import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import io.github.manamiproject.modb.anisearch.AnisearchConfig
import io.github.manamiproject.modb.anisearch.AnisearchRelationsConfig
import io.github.manamiproject.modb.app.crawlers.animeplanet.AnimePlanetPaginationIdRangeSelectorConfig
import io.github.manamiproject.modb.app.crawlers.livechart.LivechartPaginationIdRangeSelectorConfig
import io.github.manamiproject.modb.app.crawlers.notify.NotifyAnimeDatasetDownloaderConfig
import io.github.manamiproject.modb.app.crawlers.notify.NotifyRelationsDatasetDownloaderConfig
import io.github.manamiproject.modb.app.crawlers.simkl.SimklPaginationIdRangeSelectorConfig
import io.github.manamiproject.modb.core.date.weekOfYear
import io.github.manamiproject.modb.core.config.ConfigRegistry
import io.github.manamiproject.modb.core.config.DefaultConfigRegistry
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.config.StringPropertyDelegate
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.directoryExists
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.kitsu.KitsuRelationsConfig
import io.github.manamiproject.modb.kitsu.KitsuTagsConfig
import io.github.manamiproject.modb.livechart.LivechartConfig
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import io.github.manamiproject.modb.notify.NotifyConfig
import io.github.manamiproject.modb.notify.NotifyRelationsConfig
import io.github.manamiproject.modb.simkl.SimklConfig
import java.time.LocalDate
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

/**
 * Implementation of [Config] which contains all necessary properties and all derivative configurations.
 * @since 1.0.0
 * @param configRegistry Implementation of [ConfigRegistry] used for populating properties. Uses [DefaultConfigRegistry] by default.
 */
class AppConfig(
    configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
): Config {

    private val weekOfYear = LocalDate.now(clock()).weekOfYear()

    private val downloadsDirectory: String by StringPropertyDelegate(
        namespace = CONFIG_NAMESPACE,
        configRegistry = configRegistry,
    )

    private val outputDirectory: String by StringPropertyDelegate(
        namespace = CONFIG_NAMESPACE,
        configRegistry = configRegistry,
    )

    private val downloadControlStateDirectory: String by StringPropertyDelegate(
        namespace = CONFIG_NAMESPACE,
        configRegistry = configRegistry,
    )

    override fun downloadsDirectory(): Directory {
        val dir = Path(downloadsDirectory)
        check(dir.directoryExists()) { "Download directory set by 'downloadsDirectory' to [$downloadsDirectory] doesn't exist or is not a directory." }
        return dir
    }

    override fun currentWeekWorkingDir(): Directory {
        val zeroBasedWeek = if (weekOfYear.week < 10) "0${weekOfYear.week}" else weekOfYear.week.toString()
        return downloadsDirectory().resolve("${weekOfYear.year}-$zeroBasedWeek")
    }

    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory {
        val hostname = metaDataProviderConfig.hostname()
        val workingDir = when(metaDataProviderConfig) {
            AnidbConfig,
            AnilistConfig,
            AnimePlanetConfig,
            AnimePlanetPaginationIdRangeSelectorConfig,
            AnimenewsnetworkConfig,
            AnisearchConfig,
            KitsuConfig,
            LivechartConfig,
            LivechartPaginationIdRangeSelectorConfig,
            MyanimelistConfig,
            NotifyAnimeDatasetDownloaderConfig,
            NotifyConfig,
            SimklConfig,
            SimklPaginationIdRangeSelectorConfig -> currentWeekWorkingDir().resolve(hostname)
            AnisearchRelationsConfig, KitsuRelationsConfig, NotifyRelationsConfig, NotifyRelationsDatasetDownloaderConfig -> currentWeekWorkingDir().resolve("$hostname-relations")
            KitsuTagsConfig -> currentWeekWorkingDir().resolve("$hostname-tags")
            else -> throw IllegalStateException("No working directory mapping for [${metaDataProviderConfig::class.simpleName}]")
        }

        check(!workingDir.regularFileExists()) { "Working directory must not be a regular file." }

        if (!workingDir.directoryExists()) {
            workingDir.createDirectories()
        }

        return workingDir
    }

    override fun outputDirectory(): Directory {
        val dir = Path(outputDirectory)
        check(dir.directoryExists()) { "Output directory set by 'outputDirectory' to [$outputDirectory] doesn't exist or is not a directory." }
        return dir
    }

    override fun downloadControlStateDirectory(): Directory {
        val dir = Path(downloadControlStateDirectory)
        check(dir.directoryExists()) { "Output directory set by 'downloadControlStateDirectory' to [$downloadControlStateDirectory] doesn't exist or is not a directory." }
        return dir
    }

    companion object {
        /**
         * Namespace under which configuration properties are being placed.
         * @since 1.0.0
         */
        const val CONFIG_NAMESPACE = "modb.app"

        /**
         * Singleton of [AppConfig]
         * @since 1.0.0
         */
        val instance: AppConfig by lazy { AppConfig() }
    }
}