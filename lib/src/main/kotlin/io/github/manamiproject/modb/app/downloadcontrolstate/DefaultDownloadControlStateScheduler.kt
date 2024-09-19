package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import java.time.LocalDate

/**
 * Default implementation to determine which anime are scheduled to download this week and which are not.
 * Files are parsed each time you call functions.
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property downloadControlStateAccessor Access to DCS files.
 */
class DefaultDownloadControlStateScheduler(
    private val appConfig: Config = AppConfig.instance,
    private val downloadControlStateAccessor: DownloadControlStateAccessor = DefaultDownloadControlStateAccessor.instance,
): DownloadControlStateScheduler {

    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> {
        log.info { "Finding all anime which are not scheduled to download this week." }

        return downloadControlStateAccessor.allDcsEntries(metaDataProviderConfig)
            .filter { isNotCurrentWeek(it.nextDownload) }
            .map { metaDataProviderConfig.extractAnimeId(it.anime.sources.first()) }
            .toSet()
    }

    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> {
        log.info { "Finding all anime which are scheduled to download this week." }

        return downloadControlStateAccessor.allDcsEntries(metaDataProviderConfig)
            .filter { isCurrentWeek(it.nextDownload) }
            .map { metaDataProviderConfig.extractAnimeId(it.anime.sources.first()) }
            .toSet()
    }

    private fun isNotCurrentWeek(weekOfYear: WeekOfYear): Boolean = !isCurrentWeek(weekOfYear)

    private fun isCurrentWeek(weekOfYear: WeekOfYear): Boolean {
        val now = LocalDate.now(appConfig.clock()).weekOfYear()
        return weekOfYear.year == now.year && weekOfYear.week == now.week
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DefaultDownloadControlStateScheduler]
         * @since 1.0.0
         */
        val instance: DefaultDownloadControlStateScheduler by lazy { DefaultDownloadControlStateScheduler() }
    }
}