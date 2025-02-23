package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.app.minusWeeks
import io.github.manamiproject.modb.core.anime.*
import io.github.manamiproject.modb.core.anime.AnimeRaw.Companion.NO_PICTURE
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.anime.Duration.Companion.UNKNOWN as UNKNOWN_DURATION
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE
import org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE
import java.net.URI
import kotlin.test.Test
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN as UNKNOWN_STATUS
import io.github.manamiproject.modb.core.anime.AnimeType.UNKNOWN as UNKNOWN_TYPE

internal class DownloadControlStateEntryTest {

    @Nested
    inner class ConstructorTests {

        @Test
        fun `throws exception if _weeksWihoutChange is negative`() {
            // when
            val result = exceptionExpected<IllegalArgumentException> {
                DownloadControlStateEntry(
                    _weeksWihoutChange = -1,
                    _lastDownloaded = WeekOfYear(
                        year = 2021,
                        week = 1,
                    ),
                    _nextDownload = WeekOfYear(
                        year = 2021,
                        week = 2,
                    ),
                    _anime = AnimeRaw("title"),
                )
            }

            // then
            assertThat(result).hasMessage("_weeksWihoutChange must not be negative.")
        }
    }

    @Nested
    inner class UpdateTests {

        @ParameterizedTest
        @EnumSource(value = AnimeStatus::class, mode = EXCLUDE, names = ["ONGOING", "UPCOMING"])
        fun `if the anime provides changes then schedule redownload for next week`(status: AnimeStatus) {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 3,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(3),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw("title"),
            )

            val newAnime = AnimeRaw(
                _title = "Title",
                episodes = 12,
                status = status,
            )

            // when
            val result = downloadControlStateEntry.update(newAnime)

            // then
            assertThat(result.weeksWihoutChange).isZero()
            assertThat(result.lastDownloaded).isEqualTo(WeekOfYear.currentWeek())
            assertThat(result.nextDownload).isEqualTo(WeekOfYear.currentWeek().plusWeeks(1))
            assertThat(result.anime).isEqualTo(newAnime)
        }

        @ParameterizedTest
        @EnumSource(value = AnimeStatus::class, mode = EXCLUDE, names = ["ONGOING", "UPCOMING"])
        fun `if the anime doesn't change first time schedule redownload in 2 to 4 weeks`(status: AnimeStatus) {
            // given
            val anime = AnimeRaw(
                _title = "title",
                status = status,
            )
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 0,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(1),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = anime,
            )

            // when
            val result = downloadControlStateEntry.update(anime)

            // then
            assertThat(result.weeksWihoutChange).isOne()
            assertThat(result.lastDownloaded).isEqualTo(WeekOfYear.currentWeek())
            assertThat(result.nextDownload.week).isIn(
                WeekOfYear.currentWeek().plusWeeks(2).week,
                WeekOfYear.currentWeek().plusWeeks(3).week,
                WeekOfYear.currentWeek().plusWeeks(4).week,
            )
            assertThat(result.anime).isEqualTo(anime)
        }

        @ParameterizedTest
        @EnumSource(value = AnimeStatus::class, mode = EXCLUDE, names = ["ONGOING", "UPCOMING"])
        fun `if the anime didn't change repeatedly expand waiting by the weeks without changes so far`(status: AnimeStatus) {
            // given
            val anime = AnimeRaw(
                _title = "title",
                status = status,
            )
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 2,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(3),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = anime,
            )

            // when
            val result = downloadControlStateEntry.update(anime)

            // then
            assertThat(result.weeksWihoutChange).isEqualTo(5)
            assertThat(result.lastDownloaded).isEqualTo(WeekOfYear.currentWeek())
            assertThat(result.nextDownload.week).isEqualTo(WeekOfYear.currentWeek().plusWeeks(5).week)
            assertThat(result.anime).isEqualTo(anime)
        }

        @ParameterizedTest
        @EnumSource(value = AnimeStatus::class, mode = EXCLUDE, names = ["ONGOING", "UPCOMING"])
        fun `if the anime didn't change repeatedly expand waiting time for 12 weeks maximum`(status: AnimeStatus) {
            // given
            val anime = AnimeRaw(
                _title = "title",
                status = status,
            )
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 10,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(3),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = anime,
            )

            // when
            val result = downloadControlStateEntry.update(anime)

            // then
            assertThat(result.weeksWihoutChange).isEqualTo(13)
            assertThat(result.lastDownloaded).isEqualTo(WeekOfYear.currentWeek())
            assertThat(result.nextDownload.week).isEqualTo(WeekOfYear.currentWeek().plusWeeks(12).week)
            assertThat(result.anime).isEqualTo(anime)
        }

        @ParameterizedTest
        @EnumSource(value = AnimeStatus::class, mode = INCLUDE, names = ["ONGOING", "UPCOMING"])
        fun `if the anime didn't change, but the status is either ONGOING or UPCOMING then schedule redownload for next week`(status: AnimeStatus) {
            // given
            val anime = AnimeRaw(
                _title = "title",
                status = status
            )
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 0,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(1),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = anime,
            )

            // when
            val result = downloadControlStateEntry.update(anime)

            // then
            assertThat(result.weeksWihoutChange).isZero()
            assertThat(result.lastDownloaded).isEqualTo(WeekOfYear.currentWeek())
            assertThat(result.nextDownload).isEqualTo(WeekOfYear.currentWeek().plusWeeks(1))
            assertThat(result.anime).isEqualTo(result.anime)
        }

        @Test
        fun `different title is considered a change`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 3,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(3),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw("title"),
            )

            val newAnime = downloadControlStateEntry.anime.copy(
                _title = "other",
            )

            // when
            val result = downloadControlStateEntry.update(newAnime)

            // then
            assertThat(result.weeksWihoutChange).isZero()
            assertThat(result.lastDownloaded).isEqualTo(WeekOfYear.currentWeek())
            assertThat(result.nextDownload).isEqualTo(WeekOfYear.currentWeek().plusWeeks(1))
            assertThat(result.anime).isEqualTo(newAnime)
        }

        @Test
        fun `different sources is considered a change`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 3,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(3),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "title",
                    _sources = hashSetOf(URI("https://example.org/anime/some-title")),
                ),
            )

            val newAnime = downloadControlStateEntry.anime.copy(
                _sources = hashSetOf(URI("https://example.org/anime/other-title")),
            )

            // when
            val result = downloadControlStateEntry.update(newAnime)

            // then
            assertThat(result.weeksWihoutChange).isZero()
            assertThat(result.lastDownloaded).isEqualTo(WeekOfYear.currentWeek())
            assertThat(result.nextDownload).isEqualTo(WeekOfYear.currentWeek().plusWeeks(1))
            assertThat(result.anime).isEqualTo(newAnime)
        }

        @Test
        fun `different type is considered a change`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 3,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(3),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "title",
                    type = UNKNOWN_TYPE,
                ),
            )

            val newAnime = downloadControlStateEntry.anime.copy(
                type = MOVIE,
            )

            // when
            val result = downloadControlStateEntry.update(newAnime)

            // then
            assertThat(result.weeksWihoutChange).isZero()
            assertThat(result.lastDownloaded).isEqualTo(WeekOfYear.currentWeek())
            assertThat(result.nextDownload).isEqualTo(WeekOfYear.currentWeek().plusWeeks(1))
            assertThat(result.anime).isEqualTo(newAnime)
        }

        @Test
        fun `different episodes is considered a change`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 3,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(3),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "title",
                    episodes = 0,
                ),
            )

            val newAnime = downloadControlStateEntry.anime.copy(
                episodes = 12,
            )

            // when
            val result = downloadControlStateEntry.update(newAnime)

            // then
            assertThat(result.weeksWihoutChange).isZero()
            assertThat(result.lastDownloaded).isEqualTo(WeekOfYear.currentWeek())
            assertThat(result.nextDownload).isEqualTo(WeekOfYear.currentWeek().plusWeeks(1))
            assertThat(result.anime).isEqualTo(newAnime)
        }

        @Test
        fun `different status is considered a change`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 3,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(3),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "title",
                    status = UNKNOWN_STATUS,
                ),
            )

            val newAnime = downloadControlStateEntry.anime.copy(
                status = ONGOING,
            )

            // when
            val result = downloadControlStateEntry.update(newAnime)

            // then
            assertThat(result.weeksWihoutChange).isZero()
            assertThat(result.lastDownloaded).isEqualTo(WeekOfYear.currentWeek())
            assertThat(result.nextDownload).isEqualTo(WeekOfYear.currentWeek().plusWeeks(1))
            assertThat(result.anime).isEqualTo(newAnime)
        }

        @Test
        fun `different animeSeason is considered a change`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 3,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(3),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "title",
                    animeSeason = AnimeSeason(
                        year = 2025,
                    ),
                ),
            )

            val newAnime = downloadControlStateEntry.anime.copy(
                animeSeason = AnimeSeason(
                    season = FALL,
                    year = 2025,
                ),
            )

            // when
            val result = downloadControlStateEntry.update(newAnime)

            // then
            assertThat(result.weeksWihoutChange).isZero()
            assertThat(result.lastDownloaded).isEqualTo(WeekOfYear.currentWeek())
            assertThat(result.nextDownload).isEqualTo(WeekOfYear.currentWeek().plusWeeks(1))
            assertThat(result.anime).isEqualTo(newAnime)
        }

        @Test
        fun `different picture is considered a change`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 3,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(3),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "title",
                    picture = NO_PICTURE,
                ),
            )

            val newAnime = downloadControlStateEntry.anime.copy(
                picture = URI("https://example.org/media/1.png"),
            )

            // when
            val result = downloadControlStateEntry.update(newAnime)

            // then
            assertThat(result.weeksWihoutChange).isZero()
            assertThat(result.lastDownloaded).isEqualTo(WeekOfYear.currentWeek())
            assertThat(result.nextDownload).isEqualTo(WeekOfYear.currentWeek().plusWeeks(1))
            assertThat(result.anime).isEqualTo(newAnime)
        }

        @Test
        fun `different thumbnail is considered a change`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 3,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(3),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "title",
                    thumbnail = NO_PICTURE,
                ),
            )

            val newAnime = downloadControlStateEntry.anime.copy(
                thumbnail = URI("https://example.org/media/1.png"),
            )

            // when
            val result = downloadControlStateEntry.update(newAnime)

            // then
            assertThat(result.weeksWihoutChange).isZero()
            assertThat(result.lastDownloaded).isEqualTo(WeekOfYear.currentWeek())
            assertThat(result.nextDownload).isEqualTo(WeekOfYear.currentWeek().plusWeeks(1))
            assertThat(result.anime).isEqualTo(newAnime)
        }

        @Test
        fun `different duration is considered a change`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 3,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(3),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "title",
                    duration = UNKNOWN_DURATION,
                ),
            )

            val newAnime = downloadControlStateEntry.anime.copy(
                duration = Duration(
                    value = 20,
                    unit = MINUTES,
                ),
            )

            // when
            val result = downloadControlStateEntry.update(newAnime)

            // then
            assertThat(result.weeksWihoutChange).isZero()
            assertThat(result.lastDownloaded).isEqualTo(WeekOfYear.currentWeek())
            assertThat(result.nextDownload).isEqualTo(WeekOfYear.currentWeek().plusWeeks(1))
            assertThat(result.anime).isEqualTo(newAnime)
        }

        @Test
        fun `different synonyms is considered a change`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 3,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(3),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "title",
                    _synonyms = hashSetOf("Alternative title"),
                ),
            )

            val newAnime = downloadControlStateEntry.anime.copy(
                _synonyms = hashSetOf(
                    "Alternative title",
                    "Additional title",
                ),
            )

            // when
            val result = downloadControlStateEntry.update(newAnime)

            // then
            assertThat(result.weeksWihoutChange).isZero()
            assertThat(result.lastDownloaded).isEqualTo(WeekOfYear.currentWeek())
            assertThat(result.nextDownload).isEqualTo(WeekOfYear.currentWeek().plusWeeks(1))
            assertThat(result.anime).isEqualTo(newAnime)
        }

        @Test
        fun `different relatedAnime is considered a change`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 3,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(3),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "title",
                    _relatedAnime = hashSetOf(URI("https://example.org/anime/some-title")),
                ),
            )

            val newAnime = downloadControlStateEntry.anime.copy(
                _relatedAnime = hashSetOf(
                    URI("https://example.org/anime/some-title"),
                    URI("https://example.org/anime/additional-title"),
                ),
            )

            // when
            val result = downloadControlStateEntry.update(newAnime)

            // then
            assertThat(result.weeksWihoutChange).isZero()
            assertThat(result.lastDownloaded).isEqualTo(WeekOfYear.currentWeek())
            assertThat(result.nextDownload).isEqualTo(WeekOfYear.currentWeek().plusWeeks(1))
            assertThat(result.anime).isEqualTo(newAnime)
        }

        @Test
        fun `different tags is considered a change`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 3,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(3),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "title",
                    _tags = hashSetOf("descriptive tag"),
                ),
            )

            val newAnime = downloadControlStateEntry.anime.copy(
                _tags = hashSetOf(
                    "descriptive tag",
                    "Additional tag",
                ),
            )

            // when
            val result = downloadControlStateEntry.update(newAnime)

            // then
            assertThat(result.weeksWihoutChange).isZero()
            assertThat(result.lastDownloaded).isEqualTo(WeekOfYear.currentWeek())
            assertThat(result.nextDownload).isEqualTo(WeekOfYear.currentWeek().plusWeeks(1))
            assertThat(result.anime).isEqualTo(newAnime)
        }

        @Test
        fun `different score is NOT considered a change, therefore next download is scheduled for an unchanged anime, but the score is being updated`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 0,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(1),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "title",
                ).addScores(
                    MetaDataProviderScoreValue(
                        hostname = "example.org",
                        value = 7.8,
                        range = 1.0..10.0
                    )
                ),
            )

            val newAnime = downloadControlStateEntry.anime.copy().addScores(
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 7.9,
                    range = 1.0..10.0
                )
            )

            // when
            val result = downloadControlStateEntry.update(newAnime)

            // then
            assertThat(result.weeksWihoutChange).isOne()
            assertThat(result.lastDownloaded).isEqualTo(WeekOfYear.currentWeek())
            assertThat(result.nextDownload.week).isIn(
                WeekOfYear.currentWeek().plusWeeks(2).week,
                WeekOfYear.currentWeek().plusWeeks(3).week,
                WeekOfYear.currentWeek().plusWeeks(4).week,
            )
            assertThat(result.anime).isEqualTo(newAnime)
        }

        @Test
        fun `different activateChecks is NOT considered a change, therefore next download is scheduled for an unchanged anime`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 0,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(1),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "title",
                    activateChecks = false,
                )
            )

            val newAnime = downloadControlStateEntry.anime.copy(
                activateChecks = true,
            )

            // when
            val result = downloadControlStateEntry.update(newAnime)

            // then
            assertThat(result.weeksWihoutChange).isOne()
            assertThat(result.lastDownloaded).isEqualTo(WeekOfYear.currentWeek())
            assertThat(result.nextDownload.week).isIn(
                WeekOfYear.currentWeek().plusWeeks(2).week,
                WeekOfYear.currentWeek().plusWeeks(3).week,
                WeekOfYear.currentWeek().plusWeeks(4).week,
            )
            assertThat(result.anime).isEqualTo(newAnime)
        }
    }

    @Nested
    inner class CalculateQualityScoreTests {

        @Test
        fun `decreasing number of synonyms increases score`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 2,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(1),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "Test",
                    _synonyms = hashSetOf("abcd", "efgh"),
                ),
            )

            val anime = AnimeRaw(
                _title = "Test",
                _synonyms = hashSetOf("efgh"),
            )

            // when
            val result = downloadControlStateEntry.calculateQualityScore(anime)

            // then
            assertThat(result).isEqualTo(1u)
        }

        @Test
        fun `changing type to UNKNOWN increases score`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 2,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(1),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "Test",
                    type = TV,
                ),
            )

            val anime = AnimeRaw(
                _title = "Test",
                type = UNKNOWN_TYPE,
            )

            // when
            val result = downloadControlStateEntry.calculateQualityScore(anime)

            // then
            assertThat(result).isEqualTo(1u)
        }

        @Test
        fun `setting number of episodes to zero if it was greater than 0 before increases the score`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 2,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(1),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "Test",
                    episodes = 12,
                ),
            )

            val anime = AnimeRaw(
                _title = "Test",
                episodes = 0,
            )

            // when
            val result = downloadControlStateEntry.calculateQualityScore(anime)

            // then
            assertThat(result).isEqualTo(1u)
        }

        @Test
        fun `changing status to UNKNOWN increases score`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 2,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(1),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "Test",
                    status = UPCOMING
                ),
            )

            val anime = AnimeRaw(
                _title = "Test",
                status = UNKNOWN_STATUS,
            )

            // when
            val result = downloadControlStateEntry.calculateQualityScore(anime)

            // then
            assertThat(result).isEqualTo(1u)
        }

        @Test
        fun `changing season to UNDEFINED increases score`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 2,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(1),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "Test",
                    animeSeason = AnimeSeason(
                        season = FALL,
                        year = 2023,
                    )
                ),
            )

            val anime = AnimeRaw(
                _title = "Test",
                animeSeason = AnimeSeason(
                    season = UNDEFINED,
                    year = 2023,
                )
            )

            // when
            val result = downloadControlStateEntry.calculateQualityScore(anime)

            // then
            assertThat(result).isEqualTo(1u)
        }

        @Test
        fun `setting year to zero if it was greater than 0 before increases the score`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 2,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(1),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "Test",
                    animeSeason = AnimeSeason(
                        season = FALL,
                        year = 2023,
                    )
                ),
            )

            val anime = AnimeRaw(
                _title = "Test",
                animeSeason = AnimeSeason(
                    season = FALL,
                    year = 0,
                )
            )

            // when
            val result = downloadControlStateEntry.calculateQualityScore(anime)

            // then
            assertThat(result).isEqualTo(1u)
        }

        @Test
        fun `changing picture to default increases score`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 2,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(1),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "Test",
                    picture = URI("https://example.org/picture.jpg"),
                ),
            )

            val anime = AnimeRaw(
                _title = "Test",
            )

            // when
            val result = downloadControlStateEntry.calculateQualityScore(anime)

            // then
            assertThat(result).isEqualTo(1u)
        }

        @Test
        fun `changing thumbnail to default increases score`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 2,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(1),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "Test",
                    thumbnail = URI("https://example.org/picture.jpg"),
                ),
            )

            val anime = AnimeRaw(
                _title = "Test",
            )

            // when
            val result = downloadControlStateEntry.calculateQualityScore(anime)

            // then
            assertThat(result).isEqualTo(1u)
        }

        @Test
        fun `setting duration to zero if it was greater than 0 before increases the score`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 2,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(1),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "Test",
                    duration = Duration(
                        value = 24,
                        unit = MINUTES,
                    )
                ),
            )

            val anime = AnimeRaw(
                _title = "Test",
            )

            // when
            val result = downloadControlStateEntry.calculateQualityScore(anime)

            // then
            assertThat(result).isEqualTo(1u)
        }

        @Test
        fun `increase score if number if related anime dropped to 0`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 2,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(1),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "Test",
                    _relatedAnime = hashSetOf(URI("https://example.org/real1"))
                ),
            )

            val anime = AnimeRaw(
                _title = "Test",
            )

            // when
            val result = downloadControlStateEntry.calculateQualityScore(anime)

            // then
            assertThat(result).isEqualTo(1u)
        }

        @Test
        fun `decreasing number of tags increases score`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 2,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(1),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "Test",
                    _tags = hashSetOf(
                        "abcd",
                        "efgh",
                    ),
                ),
            )

            val anime = AnimeRaw(
                _title = "Test",
                _tags = hashSetOf(
                    "efgh",
                ),
            )

            // when
            val result = downloadControlStateEntry.calculateQualityScore(anime)

            // then
            assertThat(result).isEqualTo(1u)
        }

        @Test
        fun `each score indicator is counted`() {
            // given
            val anime = AnimeRaw(
                _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                type = UNKNOWN_TYPE,
            )

            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 2,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(1),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                    type = SPECIAL,
                    episodes = 1,
                    status = FINISHED,
                    animeSeason = AnimeSeason(
                        season = SUMMER,
                        year = 2009,
                    ),
                    picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                    duration = Duration(
                        value = 24,
                        unit = MINUTES,
                    ),
                    _sources = hashSetOf(URI("https://myanimelist.net/anime/6351")),
                    _synonyms = hashSetOf(
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編",
                    ),
                    _relatedAnime = hashSetOf(URI("https://myanimelist.net/anime/2167")),
                    _tags = hashSetOf(
                        "comedy",
                        "drama",
                        "romance",
                        "school",
                        "slice of life",
                        "supernatural",
                    ),
                )
            )

            // when
            val result = downloadControlStateEntry.calculateQualityScore(anime)

            // then
            assertThat(result).isEqualTo(11u)
        }

        @Test
        fun `return 0 if none of the score indicators apply`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 2,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(1),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = AnimeRaw(
                    _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                    type = SPECIAL,
                    episodes = 1,
                    status = FINISHED,
                    animeSeason = AnimeSeason(
                        season = SUMMER,
                        year = 2009,
                    ),
                    picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                    duration = Duration(
                        value = 24,
                        unit = MINUTES,
                    ),
                    _sources = hashSetOf(URI("https://myanimelist.net/anime/6351")),
                    _synonyms = hashSetOf(
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編",
                    ),
                    _relatedAnime = hashSetOf(URI("https://myanimelist.net/anime/2167")),
                    _tags = hashSetOf(
                        "comedy",
                        "drama",
                        "romance",
                        "school",
                        "slice of life",
                        "supernatural",
                    ),
                )
            )

            // when
            val result = downloadControlStateEntry.calculateQualityScore(downloadControlStateEntry.anime)

            // then
            assertThat(result).isEqualTo(0u)
        }
    }
}