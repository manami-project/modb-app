package io.github.manamiproject.modb.app.convfiles

import io.github.manamiproject.modb.anidb.AnidbAnimeConverter
import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anilist.AnilistAnimeConverter
import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.animeplanet.AnimePlanetAnimeConverter
import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import io.github.manamiproject.modb.anisearch.AnisearchAnimeConverter
import io.github.manamiproject.modb.anisearch.AnisearchConfig
import io.github.manamiproject.modb.anisearch.AnisearchRelationsConfig
import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.crawlers.notify.NotifyAnimeDatasetDownloaderConfig
import io.github.manamiproject.modb.app.crawlers.notify.NotifyRelationsDatasetDownloaderConfig
import io.github.manamiproject.modb.core.converter.DefaultPathAnimeConverter
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.excludeFromTestContext
import io.github.manamiproject.modb.core.extensions.fileSuffix
import io.github.manamiproject.modb.core.extensions.listRegularFiles
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.kitsu.KitsuAnimeConverter
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.kitsu.KitsuRelationsConfig
import io.github.manamiproject.modb.kitsu.KitsuTagsConfig
import io.github.manamiproject.modb.livechart.LivechartAnimeConverter
import io.github.manamiproject.modb.livechart.LivechartConfig
import io.github.manamiproject.modb.myanimelist.MyanimelistAnimeConverter
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import io.github.manamiproject.modb.notify.NotifyAnimeConverter
import io.github.manamiproject.modb.simkl.SimklAnimeConverter
import io.github.manamiproject.modb.simkl.SimklConfig
import kotlinx.coroutines.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.toDuration

/**
 * Default implementation of [RawFileConversionService].
 * Uses [WatchService] implementations to constantly convert files as they are created.
 * @since 1.0.0
 * @param appConfig Application specific configuration. Uses [AppConfig] by default.
 */
class DefaultRawFileConversionService(
    private val appConfig: Config = AppConfig.instance,
): RawFileConversionService {

    private val watchServices = CopyOnWriteArrayList(mutableListOf<WatchService>())

    override suspend fun unconvertedFilesExist(): Boolean {
        log.info { "Checking if there are still files which need to be converted." }

        var unconverted = 0
        var converted = 0

        appConfig.metaDataProviderConfigurations().forEach { config ->
            appConfig.workingDir(config)
                .listRegularFiles()
                .forEach { file ->
                    when {
                        file.fileSuffix() == config.fileSuffix() -> unconverted++
                        file.fileSuffix() == CONVERTED_FILE_SUFFIX -> converted++
                    }
                }
        }

        return unconverted > converted
    }

    override suspend fun waitForAllRawFilesToBeConverted() {
        withTimeout(10.toDuration(SECONDS)) {
            while (unconvertedFilesExist()) {
                wait()
            }
        }
    }

    @KoverIgnore
    private suspend fun wait() {
        excludeFromTestContext(appConfig) {
            delay(2.toDuration(SECONDS))
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun start(): Boolean {
        if (watchServices.isNotEmpty()) {
            log.info { "Skipping start, because watch services are already running. Either use shutdown before starting anew or create new instance of the class." }
            return false
        }

        log.info { "Starting watch services." }

        buildWatchServices()

        withContext(LIMITED_CPU) {
            watchServices.forEach { watchService ->
                watchService.prepare()
            }
            watchServices.forEach { watchService ->
                GlobalScope.launch {
                    watchService.watch()
                }
            }
        }

        return true
    }

    override suspend fun shutdown() {
        watchServices.forEach { it.stop() }
        watchServices.clear()
    }

    private fun buildWatchServices() {
        mapOf(
            AnidbConfig to AnidbAnimeConverter.instance,
            AnilistConfig to AnilistAnimeConverter.instance,
            AnimePlanetConfig to AnimePlanetAnimeConverter.instance,
            LivechartConfig to LivechartAnimeConverter.instance,
            MyanimelistConfig to MyanimelistAnimeConverter.instance,
            SimklConfig to SimklAnimeConverter.instance,
        ).map { entry ->
            SimpleConversionWatchService(
                appConfig = appConfig,
                metaDataProviderConfig = entry.key,
                converter = DefaultPathAnimeConverter(
                    animeConverter = entry.value,
                    fileSuffix = entry.key.fileSuffix(),
                )
            )
        }.forEach {
            watchServices.add(it)
        }

        watchServices.add(
            DependentConversionWatchService(
                appConfig = appConfig,
                mainConfig = AnisearchConfig,
                dependentMetaDataProviderConfigs = listOf(
                    AnisearchRelationsConfig,
                ),
                converter = DefaultPathAnimeConverter(
                    animeConverter = AnisearchAnimeConverter(
                        relationsDir = appConfig.workingDir(AnisearchRelationsConfig),
                    ),
                    fileSuffix = AnisearchConfig.fileSuffix(),
                ),
            )
        )

        watchServices.add(
            DependentConversionWatchService(
                appConfig = appConfig,
                mainConfig = KitsuConfig,
                dependentMetaDataProviderConfigs = listOf(
                    KitsuRelationsConfig,
                    KitsuTagsConfig,
                ),
                converter = DefaultPathAnimeConverter(
                    animeConverter = KitsuAnimeConverter(
                        relationsDir = appConfig.workingDir(KitsuRelationsConfig),
                        tagsDir = appConfig.workingDir(KitsuTagsConfig),
                    ),
                    fileSuffix = KitsuConfig.fileSuffix(),
                ),
            )
        )

        watchServices.add(
            DependentConversionWatchService(
                appConfig = appConfig,
                mainConfig = NotifyAnimeDatasetDownloaderConfig,
                dependentMetaDataProviderConfigs = listOf(
                    NotifyRelationsDatasetDownloaderConfig,
                ),
                converter = DefaultPathAnimeConverter(
                    animeConverter = NotifyAnimeConverter(
                        relationsDir = appConfig.workingDir(NotifyRelationsDatasetDownloaderConfig),
                    ),
                    fileSuffix = NotifyAnimeDatasetDownloaderConfig.fileSuffix(),
                )
            )
        )
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DefaultRawFileConversionService]
         * @since 1.0.0
         */
        val instance: DefaultRawFileConversionService by lazy { DefaultRawFileConversionService() }
    }
}