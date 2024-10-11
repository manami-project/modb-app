package io.github.manamiproject.modb.analyzer.util

import io.github.manamiproject.modb.anidb.AnidbAnimeConverter
import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anidb.AnidbDownloader
import io.github.manamiproject.modb.anilist.AnilistAnimeConverter
import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.anilist.AnilistDownloader
import io.github.manamiproject.modb.animeplanet.AnimePlanetAnimeConverter
import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import io.github.manamiproject.modb.animeplanet.AnimePlanetDownloader
import io.github.manamiproject.modb.anisearch.AnisearchAnimeConverter
import io.github.manamiproject.modb.anisearch.AnisearchConfig
import io.github.manamiproject.modb.anisearch.AnisearchDownloader
import io.github.manamiproject.modb.anisearch.AnisearchRelationsConfig
import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateUpdater
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.kitsu.*
import io.github.manamiproject.modb.livechart.LivechartAnimeConverter
import io.github.manamiproject.modb.livechart.LivechartConfig
import io.github.manamiproject.modb.livechart.LivechartDownloader
import io.github.manamiproject.modb.myanimelist.MyanimelistAnimeConverter
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import io.github.manamiproject.modb.myanimelist.MyanimelistDownloader
import io.github.manamiproject.modb.notify.NotifyAnimeConverter
import io.github.manamiproject.modb.notify.NotifyConfig
import io.github.manamiproject.modb.notify.NotifyDownloader
import io.github.manamiproject.modb.notify.NotifyRelationsConfig
import java.net.URI

@KoverIgnore
internal object AnimeLoader {

    suspend fun load(uri: URI) {
        val config = AppConfig.instance.findMetaDataProviderConfig(uri.host)
        val animeId = config.extractAnimeId(uri)

        val anime = when(uri.host) {
            AnidbConfig.hostname() -> {
                val content = AnidbDownloader(config).download(animeId)
                content.writeToFile(AppConfig.instance.workingDir(config).resolve("$animeId.${config.fileSuffix()}"))
                AnidbAnimeConverter(config).convert(content)
            }
            AnilistConfig.hostname() -> {
                val content = AnilistDownloader(config).download(animeId)
                content.writeToFile(AppConfig.instance.workingDir(config).resolve("$animeId.${config.fileSuffix()}"))
                AnilistAnimeConverter(config).convert(content)
            }
            AnimePlanetConfig.hostname() -> {
                val content = AnimePlanetDownloader(config).download(animeId)
                content.writeToFile(AppConfig.instance.workingDir(config).resolve("$animeId.${config.fileSuffix()}"))
                AnimePlanetAnimeConverter(config).convert(content)
            }
            AnisearchConfig.hostname() -> {
                val relationsWorkingDir = AppConfig.instance.workingDir(AnisearchRelationsConfig)
                AnisearchDownloader(AnisearchRelationsConfig).download(animeId).writeToFile(relationsWorkingDir.resolve("$animeId.${AnisearchRelationsConfig.fileSuffix()}"))
                val raw = AnisearchDownloader(config).download(animeId)
                raw.writeToFile(AppConfig.instance.workingDir(config).resolve("$animeId.${config.fileSuffix()}"))
                AnisearchAnimeConverter(
                    relationsDir = relationsWorkingDir,
                ).convert(raw)
            }
            KitsuConfig.hostname() -> {
                val relationsWorkingDir = AppConfig.instance.workingDir(KitsuRelationsConfig)
                KitsuDownloader(metaDataProviderConfig = KitsuRelationsConfig).download(animeId).writeToFile(relationsWorkingDir.resolve("$animeId.${KitsuRelationsConfig.fileSuffix()}"))
                val tagsWorkingDir = AppConfig.instance.workingDir(KitsuTagsConfig)
                KitsuDownloader(metaDataProviderConfig = KitsuTagsConfig).download(animeId).writeToFile(tagsWorkingDir.resolve("$animeId.${KitsuTagsConfig.fileSuffix()}"))
                val raw = KitsuDownloader(metaDataProviderConfig = KitsuConfig).download(animeId)
                raw.writeToFile(AppConfig.instance.workingDir(config).resolve("$animeId.${config.fileSuffix()}"))
                KitsuAnimeConverter(
                    relationsDir = relationsWorkingDir,
                    tagsDir = tagsWorkingDir,
                ).convert(raw)
            }
            LivechartConfig.hostname() -> {
                val content = LivechartDownloader.instance.download(animeId)
                content.writeToFile(AppConfig.instance.workingDir(config).resolve("$animeId.${config.fileSuffix()}"))
                LivechartAnimeConverter.instance.convert(content)
            }
            MyanimelistConfig.hostname() -> {
                val content = MyanimelistDownloader(config).download(animeId)
                content.writeToFile(AppConfig.instance.workingDir(config).resolve("$animeId.${config.fileSuffix()}"))
                MyanimelistAnimeConverter(config).convert(content)
            }
            NotifyConfig.hostname() -> {
                val relationsWorkingDir = AppConfig.instance.workingDir(NotifyRelationsConfig)
                NotifyDownloader(metaDataProviderConfig = NotifyRelationsConfig).download(animeId).writeToFile(relationsWorkingDir.resolve("$animeId.${NotifyRelationsConfig.fileSuffix()}"))
                val raw = NotifyDownloader(metaDataProviderConfig = NotifyConfig).download(animeId)
                raw.writeToFile(AppConfig.instance.workingDir(config).resolve("$animeId.${config.fileSuffix()}"))
                NotifyAnimeConverter(relationsDir = relationsWorkingDir).convert(raw)
            }
            else -> throw IllegalStateException("Unknown host [${uri.host}]")
        }

        Json.toJson(anime).writeToFile(AppConfig.instance.workingDir(config).resolve("$animeId.conv"))
        DefaultDownloadControlStateUpdater.instance.updateAll()
    }
}