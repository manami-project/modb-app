package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.app.minusWeeks
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.AnimeSeason
import io.github.manamiproject.modb.core.models.Duration
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE
import org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.net.URI
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

    @Nested
    inner class CalculateQualityScoreTests {

        @Test
        fun `decreasing number of synonyms increases score`() {
            // given
            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 2,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(1),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = Anime(
                    _title = "Test",
                    synonyms = hashSetOf("abcd", "efgh"),
                ),
            )

            val anime = Anime(
                _title = "Test",
                synonyms = hashSetOf("efgh"),
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
                _anime = Anime(
                    _title = "Test",
                    type = Anime.Type.TV,
                ),
            )

            val anime = Anime(
                _title = "Test",
                type = Anime.Type.UNKNOWN,
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
                _anime = Anime(
                    _title = "Test",
                    episodes = 12,
                ),
            )

            val anime = Anime(
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
                _anime = Anime(
                    _title = "Test",
                    status = Anime.Status.UPCOMING
                ),
            )

            val anime = Anime(
                _title = "Test",
                status = Anime.Status.UNKNOWN,
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
                _anime = Anime(
                    _title = "Test",
                    animeSeason = AnimeSeason(
                        season = AnimeSeason.Season.FALL,
                        year = 2023,
                    )
                ),
            )

            val anime = Anime(
                _title = "Test",
                animeSeason = AnimeSeason(
                    season = AnimeSeason.Season.UNDEFINED,
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
                _anime = Anime(
                    _title = "Test",
                    animeSeason = AnimeSeason(
                        season = AnimeSeason.Season.FALL,
                        year = 2023,
                    )
                ),
            )

            val anime = Anime(
                _title = "Test",
                animeSeason = AnimeSeason(
                    season = AnimeSeason.Season.FALL,
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
                _anime = Anime(
                    _title = "Test",
                    picture = URI("https://example.org/picture.jpg"),
                ),
            )

            val anime = Anime(
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
                _anime = Anime(
                    _title = "Test",
                    thumbnail = URI("https://example.org/picture.jpg"),
                ),
            )

            val anime = Anime(
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
                _anime = Anime(
                    _title = "Test",
                    duration = Duration(
                        value = 24,
                        unit = Duration.TimeUnit.MINUTES,
                    )
                ),
            )

            val anime = Anime(
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
                _anime = Anime(
                    _title = "Test",
                    relatedAnime = hashSetOf(URI("https://example.org/real1"))
                ),
            )

            val anime = Anime(
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
                _anime = Anime(
                    _title = "Test",
                    tags = hashSetOf("abcd", "efgh"),
                ),
            )

            val anime = Anime(
                _title = "Test",
                tags = hashSetOf("efgh"),
            )

            // when
            val result = downloadControlStateEntry.calculateQualityScore(anime)

            // then
            assertThat(result).isEqualTo(1u)
        }

        @Test
        fun `each score indicator is counted`() {
            // given
            val anime = Anime(
                _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                type = Anime.Type.UNKNOWN
            )

            val downloadControlStateEntry = DownloadControlStateEntry(
                _weeksWihoutChange = 2,
                _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(1),
                _nextDownload = WeekOfYear.currentWeek(),
                _anime = Anime(
                    _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                    type = Anime.Type.SPECIAL,
                    episodes = 1,
                    status = Anime.Status.FINISHED,
                    animeSeason = AnimeSeason(
                        season = AnimeSeason.Season.SUMMER,
                        year = 2009
                    ),
                    picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                    duration = Duration(
                        value = 24,
                        unit = Duration.TimeUnit.MINUTES,
                    ),
                    sources = hashSetOf(URI("https://myanimelist.net/anime/6351")),
                    synonyms = hashSetOf(
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編",
                    ),
                    relatedAnime = hashSetOf(URI("https://myanimelist.net/anime/2167")),
                    tags = hashSetOf(
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
                _anime = Anime(
                    _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                    type = Anime.Type.SPECIAL,
                    episodes = 1,
                    status = Anime.Status.FINISHED,
                    animeSeason = AnimeSeason(
                        season = AnimeSeason.Season.SUMMER,
                        year = 2009
                    ),
                    picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                    duration = Duration(
                        value = 24,
                        unit = Duration.TimeUnit.MINUTES,
                    ),
                    sources = hashSetOf(URI("https://myanimelist.net/anime/6351")),
                    synonyms = hashSetOf(
                        "Clannad ~After Story~: Another World, Kyou Chapter",
                        "Clannad: After Story OVA",
                        "クラナド　アフターストーリー　もうひとつの世界　杏編",
                    ),
                    relatedAnime = hashSetOf(URI("https://myanimelist.net/anime/2167")),
                    tags = hashSetOf(
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