package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE_THUMBNAIL
import io.github.manamiproject.modb.core.anime.AnimeRaw
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.UNDEFINED
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.date.WeekOfYear
import io.github.manamiproject.modb.core.extensions.pickRandom
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN as UNKNOWN_STATUS
import io.github.manamiproject.modb.core.anime.AnimeType.UNKNOWN as UNKNOWN_TYPE

/**
 * A download control state entry contains an anime as well as meta info about its download and update cycle.
 * Anime which are either [ONGOING] or [UPCOMING] are scheduled for download every week. These entries have a higher
 * tendency to change than anime which [FINISHED] long ago.
 * If an anime has no changes for the first time, it's scheduled for redownload in 2-4 weeks in order to further
 * distribute the number of downloads. The value is picked randomly.
 * If an anime hasn't changed repeatedly, it will be downloaded in the number of weeks without changes up to this point.
 * Changes are examined by checking all properties with the exception of [AnimeRaw.scores] and [AnimeRaw.activateChecks].
 *
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
    private var _anime: AnimeRaw,
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
     * Check for equality excludes [AnimeRaw.scores] and [AnimeRaw.activateChecks].
     * @since 1.0.0
     * @param anime Newly downloaded anime to check against the [DownloadControlStateEntry.anime].
     * @return Same instance with updated properties.
     */
    fun update(anime: AnimeRaw): DownloadControlStateEntry {
        when {
            !isEqualIgnoringMetaDataProviderScore(anime) || anime.status in setOf(ONGOING, UPCOMING) -> {
                scheduleRedownloadForChangedAnime()
                _anime = anime
            }
            else -> {
                scheduleRedownloadForUnchangedAnime()
                _anime = anime
            }
        }

        return this
    }

    /**
     * Calculates a score between the [DownloadControlStateEntry.anime] and the [AnimeRaw] that has been created with the
     * current run of the application. Using this score over all anime entries of the respective meta data provider it
     * is possible to detect problems in the [AnimeConverter].
     * @since 1.0.0
     * @param currentAnime New or more recent version of the anime.
     * @return A value that reflects a possible decrease in quality. The higher the value the more likely it is that
     * there is a problem with the respective [AnimeConverter]. Value cannot be negative.
     */
    fun calculateQualityScore(currentAnime: AnimeRaw): UInt {
        var score = 0u

        if (currentAnime.synonyms.size < anime.synonyms.size) {
            score += 1u
        }

        if (currentAnime.type == UNKNOWN_TYPE && anime.type != UNKNOWN_TYPE) {
            score += 1u
        }

        if (currentAnime.episodes == 0 && anime.episodes > 0) {
            score += 1u
        }

        if (currentAnime.status == UNKNOWN_STATUS && anime.status != UNKNOWN_STATUS) {
            score += 1u
        }

        if (currentAnime.animeSeason.season == UNDEFINED && anime.animeSeason.season != UNDEFINED) {
            score += 1u
        }

        if (currentAnime.animeSeason.isYearOfPremiereUnknown() && anime.animeSeason.isYearOfPremiereKnown()) {
            score += 1u
        }

        if (currentAnime.picture == NO_PICTURE && anime.picture != NO_PICTURE) {
            score += 1u
        }

        if (currentAnime.thumbnail == NO_PICTURE_THUMBNAIL && anime.thumbnail != NO_PICTURE_THUMBNAIL) {
            score += 1u
        }

        if (currentAnime.duration.duration == 0 && anime.duration.duration > 0) {
            score += 1u
        }

        if (currentAnime.relatedAnime.isEmpty() && anime.relatedAnime.isNotEmpty()) {
            score += 1u
        }

        if (currentAnime.tags.size < anime.tags.size) {
            score += 1u
        }

        if (currentAnime.scores.size < anime.scores.size) {
            score += 1u
        }

        if (currentAnime.studios.size < anime.studios.size) {
            score += 1u
        }

        if (currentAnime.producers.size < anime.producers.size) {
            score += 1u
        }

        return score
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

    private fun isEqualIgnoringMetaDataProviderScore(other: AnimeRaw): Boolean {
        return other.title == _anime.title
                && other.sources == _anime.sources
                && other.type == _anime.type
                && other.episodes == _anime.episodes
                && other.status == _anime.status
                && other.animeSeason == _anime.animeSeason
                && other.picture == _anime.picture
                && other.thumbnail == _anime.thumbnail
                && other.duration == _anime.duration
                && other.synonyms == _anime.synonyms
                && other.relatedAnime == _anime.relatedAnime
                && other.tags == _anime.tags
                && other.studios == _anime.studios
                && other.producers == _anime.producers
    }
}
