package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.core.extensions.pickRandom
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Status.ONGOING
import io.github.manamiproject.modb.core.models.Anime.Status.UPCOMING
import io.github.manamiproject.modb.core.models.Anime.Status.FINISHED

/**
 * A download control state entry contains an anime as well as meta info about its download and update cycle.
 * Anime which are either [ONGOING] or [UPCOMING] are scheduled for download every week. These entries have a higher
 * tendency to change than anime which [FINISHED] long ago.
 * If an anime has no changes for the first time, it's scheduled for redownload in 2-4 weeks in order to further
 * distribute the number of downloads. The value is picked randomly.
 * If an anime hasn't changed repeatedly, it will be downloaded in the number of weeks without changes up to this point.
 * **Example:** If there haven't been any changes after 3 weeks, it will be downloaded again in 6 weeks. This is limited
 * to 12 weeks. The time until the next download takes place cannot exceed 12 weeks, because every anime must be
 * downloaded from each meta data provider at least once per quarter.
 * Anime in a DCS entry are always bound to a specific meta data provider. Merging takes place in a later step.
 * @since 1.0.0
 * @property _weeksWihoutChange Number of weeks in which the anime has been downloaded without any changes.
 * @property _lastDownloaded Week of year in which the anime has been downloaded the last time.
 * @property _nextDownload Week of year in which the anime should be downloaded again.
 * @property _anime The anime. The entry only contains data from a single meta data privider.
 * @throws IllegalArgumentException if [_weeksWihoutChange] is negative.
 */
data class DownloadControlStateEntry(
    private var _weeksWihoutChange: Int,
    private var _lastDownloaded: WeekOfYear,
    private var _nextDownload: WeekOfYear,
    private var _anime: Anime,
) {

    /**
     * @since 1.0.0
     * @see DownloadControlStateEntry._weeksWihoutChange
     */
    val weeksWihoutChange
        get() = _weeksWihoutChange

    /**
     * @since 1.0.0
     * @see DownloadControlStateEntry._lastDownloaded
     */
    val lastDownloaded
        get() = _lastDownloaded

    /**
     * @since 1.0.0
     * @see DownloadControlStateEntry._nextDownload
     */
    val nextDownload
        get() = _nextDownload

    /**
     * @since 1.0.0
     * @see DownloadControlStateEntry._anime
     */
    val anime
        get() = _anime

    init {
        require(_weeksWihoutChange >= 0) { "_weeksWihoutChange must not be negative." }
    }

    /**
     * Updates the properties of the DCS entry.
     * @since 1.0.0
     * @param anime Newly downloaded anime to check against the [DownloadControlStateEntry.anime].
     * @return Same instance with updated properties.
     */
    fun update(anime: Anime): DownloadControlStateEntry {
        when {
            _anime != anime || anime.status in setOf(ONGOING, UPCOMING) -> {
                scheduleRedownloadForChangedAnime()
                _anime = anime
            }
            _anime == anime -> scheduleRedownloadForUnchangedAnime()
            else -> throw IllegalStateException("Unhandled case when updating DCS entry.")
        }

        return this
    }

    private fun scheduleRedownloadForChangedAnime() {
        _weeksWihoutChange = 0
        _lastDownloaded = WeekOfYear.currentWeek()
        _nextDownload = WeekOfYear.currentWeek().plusWeeks(1)
    }

    private fun scheduleRedownloadForUnchangedAnime() {
        when(_weeksWihoutChange) {
            0 -> {
                _weeksWihoutChange = 1
                _lastDownloaded = WeekOfYear.currentWeek()
                _nextDownload = WeekOfYear.currentWeek().plusWeeks(setOf(2, 3, 4).pickRandom())
            }
            else -> {
                _weeksWihoutChange += _lastDownloaded.difference(_nextDownload)
                _lastDownloaded = WeekOfYear.currentWeek()
                val waitingTime = if (_weeksWihoutChange > 12) 12 else _weeksWihoutChange
                _nextDownload = WeekOfYear.currentWeek().plusWeeks(waitingTime)
            }
        }
    }
}
