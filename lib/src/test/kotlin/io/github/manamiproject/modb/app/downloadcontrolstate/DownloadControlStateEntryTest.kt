package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.app.minusWeeks
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE
import org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import kotlin.test.Test

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
                    _anime = Anime("title"),
                )
            }

            // then
            assertThat(result).hasMessage("_weeksWihoutChange must not be negative.")
        }
    }

    @Nested
    inner class UpdateTests {

        @ParameterizedTest
        @EnumSource(value = Anime.Status::class, mode = EXCLUDE, names = ["ONGOING", "UPCOMING"])
        fun `if the anime provides changes then schedule redownload for next week`(status: Anime.Status) {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 3,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(3),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = Anime("title"),
            )

            val newAnime = Anime(
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
        @EnumSource(value = Anime.Status::class, mode = EXCLUDE, names = ["ONGOING", "UPCOMING"])
        fun `if the anime doesn't change first time schedule redownload in 2 to 4 weeks`(status: Anime.Status) {
            // given
            val anime = Anime(
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
        @EnumSource(value = Anime.Status::class, mode = EXCLUDE, names = ["ONGOING", "UPCOMING"])
        fun `if the anime didn't change repeatedly expand waiting by the weeks without changes so far`(status: Anime.Status) {
            // given
            val anime = Anime(
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
        @EnumSource(value = Anime.Status::class, mode = EXCLUDE, names = ["ONGOING", "UPCOMING"])
        fun `if the anime didn't change repeatedly expand waiting time for 12 weeks maximum`(status: Anime.Status) {
            // given
            val anime = Anime(
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
        @EnumSource(value = Anime.Status::class, mode = INCLUDE, names = ["ONGOING", "UPCOMING"])
        fun `if the anime didn't change, but the status is either ONGOING or UPCOMING then schedule redownload for next week`(status: Anime.Status) {
            // given
            val anime = Anime(
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
    }
}