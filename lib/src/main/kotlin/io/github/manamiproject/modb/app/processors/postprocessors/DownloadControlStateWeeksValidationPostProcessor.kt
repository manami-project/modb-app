package io.github.manamiproject.modb.app.processors.postprocessors

import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.WeekOfYear
import io.github.manamiproject.modb.app.downloadcontrolstate.compareTo
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateEntry
import io.github.manamiproject.modb.core.logging.LoggerDelegate

/**
 * Checks that all DCS entries have [DownloadControlStateEntry._nextDownload] set in the future and that
 * [DownloadControlStateEntry._lastDownloaded] is either set to the current week or a week in the past.
 * Supposed to run after updating all DCS entries.
 * @since 1.0.0
 * @property downloadControlStateAccessor
 */
class DownloadControlStateWeeksValidationPostProcessor(
    private val downloadControlStateAccessor: DownloadControlStateAccessor = DefaultDownloadControlStateAccessor.instance,
): PostProcessor {

    override suspend fun process() {
        log.info { "Validating weeks in DCS entries." }

        downloadControlStateAccessor.allDcsEntries().forEach {
            check(it.nextDownload > WeekOfYear.currentWeek()) { "Week for next download of [${it.anime.sources.first()}] is not set in the future." }
            check(it.lastDownloaded <= WeekOfYear.currentWeek()) { "Week for last download of [${it.anime.sources.first()}] is neither current week nor a week of the past." }
        }
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DownloadControlStateWeeksValidationPostProcessor]
         * @since 1.0.0
         */
        val instance: DownloadControlStateWeeksValidationPostProcessor by lazy { DownloadControlStateWeeksValidationPostProcessor() }
    }
}