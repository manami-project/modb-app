package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.downloadcontrolstate.*
import io.github.manamiproject.modb.core.date.WeekOfYear
import io.github.manamiproject.modb.core.date.compareTo
import io.github.manamiproject.modb.core.logging.LoggerDelegate

/**
 * Checks that all DCS entries have [DownloadControlStateEntry._nextDownload] set in the future and that
 * [DownloadControlStateEntry._lastDownloaded] is either set to the current week or a week in the past.
 * Supposed to run after updating all DCS entries.
 * @since 1.0.0
 * @property downloadControlStateAccessor Access to DCS files.
 * @throws IllegalStateException if there are entries that violate the constraint.
 */
class DownloadControlStateWeeksValidationPostProcessor(
    private val downloadControlStateAccessor: DownloadControlStateAccessor = DefaultDownloadControlStateAccessor.instance,
): PostProcessor {

    override suspend fun process(): Boolean {
        log.info { "Validating weeks in DCS entries." }

        downloadControlStateAccessor.allDcsEntries().forEach {
            check(it.nextDownload > WeekOfYear.currentWeek()) { "Week for next download of ${it.anime.sources} is not set in the future." }
            check(it.lastDownloaded <= WeekOfYear.currentWeek()) { "Week for last download of ${it.anime.sources} is neither current week nor a week of the past." }
        }

        return true
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