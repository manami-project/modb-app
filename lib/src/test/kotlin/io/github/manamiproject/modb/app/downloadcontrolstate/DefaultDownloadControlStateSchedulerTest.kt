package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestDownloadControlStateAccessor
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.anime.AnimeRaw
import io.github.manamiproject.modb.core.anime.AnimeStatus.FINISHED
import io.github.manamiproject.modb.core.anime.AnimeType.MOVIE
import io.github.manamiproject.modb.core.anime.AnimeType.TV
import io.github.manamiproject.modb.core.anime.AnimeSeason
import io.github.manamiproject.modb.core.anime.Duration
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.core.date.WeekOfYear
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset.UTC
import kotlin.test.Test

internal class DefaultDownloadControlStateSchedulerTest {

    @Nested
    inner class FindEntriesNotScheduledForCurrentWeekTests {

        @Test
        fun `same year`() {
            tempDirectory {
                // given
                val testAppConfig = object : Config by TestAppConfig {
                    override fun clock(): Clock = Clock.fixed(Instant.parse("2021-01-31T16:02:42.00Z"), UTC)
                }

                val expectedEntry = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(2021, 44),
                    _nextDownload = WeekOfYear(2021, 45),
                    _anime = AnimeRaw(
                        _title = "Fruits Basket",
                        type = TV,
                        episodes = 26,
                        picture = URI("https://media.kitsu.app/anime/poster_images/99/small.jpg?1474922066"),
                        thumbnail = URI("https://media.kitsu.app/anime/poster_images/99/tiny.jpg?1474922066"),
                        status = FINISHED,
                        duration = Duration(24, MINUTES),
                        animeSeason = AnimeSeason(
                            year = 2001,
                        ),
                        _sources = hashSetOf(URI("https://kitsu.app/anime/99")),
                        _synonyms = hashSetOf(
                            "Furuba",
                            "フルーツバスケット",
                        ),
                        _relatedAnime = hashSetOf(
                            URI("https://kitsu.app/anime/41995"),
                        ),
                    ),
                )
                val unexpectedEntry = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(2021, 2),
                    _nextDownload = WeekOfYear(2021, 4),
                    _anime = AnimeRaw(
                        _title = "Unexpected",
                        _sources = hashSetOf(URI("https://kitsu.app/anime/19999999")),
                    ),
                )

                val testDownloadControlStateAccessor: DownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allDcsEntries(metaDataProviderConfig: MetaDataProviderConfig): List<DownloadControlStateEntry> = listOf(
                        expectedEntry,
                        unexpectedEntry,
                    )
                }

                val defaultDownloadControlStateScheduler = DefaultDownloadControlStateScheduler(
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                val result = defaultDownloadControlStateScheduler.findEntriesNotScheduledForCurrentWeek(KitsuConfig)

                // then
                assertThat(result).containsExactlyInAnyOrder("99")
            }
        }

        @Test
        fun `next yeart`() {
            tempDirectory {
                // given
                val testAppConfig = object : Config by TestAppConfig {
                    override fun clock(): Clock = Clock.fixed(Instant.parse("2021-01-31T16:02:42.00Z"), UTC)
                }

                val expectedEntry = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(2021, 44),
                    _nextDownload = WeekOfYear(2022, 2),
                    _anime = AnimeRaw(
                        _title = "Fruits Basket",
                        type = TV,
                        episodes = 26,
                        picture = URI("https://media.kitsu.app/anime/poster_images/99/small.jpg?1474922066"),
                        thumbnail = URI("https://media.kitsu.app/anime/poster_images/99/tiny.jpg?1474922066"),
                        status = FINISHED,
                        duration = Duration(24, MINUTES),
                        animeSeason = AnimeSeason(
                            year = 2001,
                        ),
                        _sources = hashSetOf(URI("https://kitsu.app/anime/99")),
                        _synonyms = hashSetOf(
                            "Furuba",
                            "フルーツバスケット",
                        ),
                        _relatedAnime = hashSetOf(
                            URI("https://kitsu.app/anime/41995"),
                        ),
                    ),
                )
                val unexpectedEntry = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(2021, 2),
                    _nextDownload = WeekOfYear(2021, 4),
                    _anime = AnimeRaw(
                        _title = "Unexpected",
                        _sources = hashSetOf(URI("https://kitsu.app/anime/19999999")),
                    ),
                )

                val testDownloadControlStateAccessor: DownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allDcsEntries(metaDataProviderConfig: MetaDataProviderConfig): List<DownloadControlStateEntry> = listOf(
                        expectedEntry,
                        unexpectedEntry,
                    )
                }

                val defaultDownloadControlStateScheduler = DefaultDownloadControlStateScheduler(
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                val result = defaultDownloadControlStateScheduler.findEntriesNotScheduledForCurrentWeek(KitsuConfig)

                // then
                assertThat(result).containsExactlyInAnyOrder("99")
            }
        }
    }

    @Nested
    inner class FindEntriesScheduledForCurrentWeekTests {

        @Test
        fun `same year`() {
            tempDirectory {
                // given
                val testAppConfig = object : Config by TestAppConfig {
                    override fun clock(): Clock = Clock.fixed(Instant.parse("2021-01-31T16:02:42.00Z"), UTC)
                }

                val expectedEntry = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(2021, 2),
                    _nextDownload = WeekOfYear(2021, 4),
                    _anime = AnimeRaw(
                        _title = "Shin Seiki Evangelion Movie: THE END OF EVANGELION",
                        type = MOVIE,
                        episodes = 1,
                        picture = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/32-7YrdcGEX1FP3.png"),
                        thumbnail = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/default.jpg"),
                        status = FINISHED,
                        duration = Duration(87, MINUTES),
                        animeSeason = AnimeSeason(
                            year = 1997,
                        ),
                        _sources = hashSetOf(URI("https://anilist.co/anime/32")),
                        _synonyms = hashSetOf(
                            "Neon Genesis Evangelion: The End of Evangelion",
                            "新世紀エヴァンゲリオン劇場版 THE END OF EVANGELION",
                        ),
                        _relatedAnime = hashSetOf(
                            URI("https://anilist.co/anime/30"),
                        ),
                        _tags = hashSetOf(
                            "action",
                            "drama",
                            "mecha",
                            "psychological",
                            "sci-fi",
                        ),
                    )
                )

                val unexpectedEntry = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(2021, 44),
                    _nextDownload = WeekOfYear(2021, 45),
                    _anime = AnimeRaw(
                        _title = "Unexpected Entry",
                        _sources = hashSetOf(URI("https://anilist.co/anime/19999999")),
                    )
                )

                val testDownloadControlStateAccessor: DownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allDcsEntries(metaDataProviderConfig: MetaDataProviderConfig): List<DownloadControlStateEntry> = listOf(
                        expectedEntry,
                        unexpectedEntry,
                    )
                }

                val defaultDownloadControlStateScheduler = DefaultDownloadControlStateScheduler(
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                val result = defaultDownloadControlStateScheduler.findEntriesScheduledForCurrentWeek(AnilistConfig)

                // then
                assertThat(result).containsExactlyInAnyOrder("32")
            }
        }

        @Test
        fun `previous year`() {
            tempDirectory {
                // given
                val testAppConfig = object : Config by TestAppConfig {
                    override fun clock(): Clock = Clock.fixed(Instant.parse("2021-01-31T16:02:42.00Z"), UTC)
                }

                val expectedEntry = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(2020, 49),
                    _nextDownload = WeekOfYear(2020, 51),
                    _anime = AnimeRaw(
                        _title = "Shin Seiki Evangelion Movie: THE END OF EVANGELION",
                        type = MOVIE,
                        episodes = 1,
                        picture = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/32-7YrdcGEX1FP3.png"),
                        thumbnail = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/default.jpg"),
                        status = FINISHED,
                        duration = Duration(87, MINUTES),
                        animeSeason = AnimeSeason(
                            year = 1997,
                        ),
                        _sources = hashSetOf(URI("https://anilist.co/anime/32")),
                        _synonyms = hashSetOf(
                            "Neon Genesis Evangelion: The End of Evangelion",
                            "新世紀エヴァンゲリオン劇場版 THE END OF EVANGELION",
                        ),
                        _relatedAnime = hashSetOf(
                            URI("https://anilist.co/anime/30"),
                        ),
                        _tags = hashSetOf(
                            "action",
                            "drama",
                            "mecha",
                            "psychological",
                            "sci-fi",
                        ),
                    )
                )

                val unexpectedEntry = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(2021, 44),
                    _nextDownload = WeekOfYear(2021, 45),
                    _anime = AnimeRaw(
                        _title = "Unexpected Entry",
                        _sources = hashSetOf(URI("https://anilist.co/anime/19999999")),
                    )
                )

                val testDownloadControlStateAccessor: DownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allDcsEntries(metaDataProviderConfig: MetaDataProviderConfig): List<DownloadControlStateEntry> = listOf(
                        expectedEntry,
                        unexpectedEntry,
                    )
                }

                val defaultDownloadControlStateScheduler = DefaultDownloadControlStateScheduler(
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                val result = defaultDownloadControlStateScheduler.findEntriesScheduledForCurrentWeek(AnilistConfig)

                // then
                assertThat(result).containsExactlyInAnyOrder("32")
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = DefaultDownloadControlStateScheduler.instance

                // when
                val result = DefaultDownloadControlStateScheduler.instance

                // then
                assertThat(result).isExactlyInstanceOf(DefaultDownloadControlStateScheduler::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}