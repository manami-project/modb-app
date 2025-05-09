package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.TestDatasetFileAccessor
import io.github.manamiproject.modb.app.TestDownloadControlStateAccessor
import io.github.manamiproject.modb.app.dataset.DatasetFileAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateEntry
import io.github.manamiproject.modb.core.date.WeekOfYear
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeRaw
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.Test

internal class DuplicatesValidationPostProcessorTest {

    @Nested
    inner class ProcessTests {

        @Test
        fun `throws exception if dcs entries is empty`() {
            // given
            val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                override suspend fun allDcsEntries(): List<DownloadControlStateEntry> = emptyList()
            }

            val duplicatesValidationPostProcessor = DuplicatesValidationPostProcessor(
                downloadControlStateAccessor = testDownloadControlStateAccessor,
                datasetFileAccessor = TestDatasetFileAccessor,
            )

            // when
            val result = exceptionExpected<IllegalStateException> {
                duplicatesValidationPostProcessor.process()
            }

            // then
            assertThat(result).hasMessage("No DCS entries found.")
        }

        @Test
        fun `throws exception if dcs entries contain duplicates`() {
            // given
            val dcsEntries = listOf(
                DownloadControlStateEntry(
                    _weeksWihoutChange = 1,
                    _lastDownloaded = WeekOfYear(2024, 32),
                    _nextDownload = WeekOfYear(2024, 33),
                    _anime = AnimeRaw("example"),
                ),
                DownloadControlStateEntry(
                    _weeksWihoutChange = 1,
                    _lastDownloaded = WeekOfYear(2024, 32),
                    _nextDownload = WeekOfYear(2024, 33),
                    _anime =  AnimeRaw("example"),
                ),
            )

            val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                override suspend fun allDcsEntries(): List<DownloadControlStateEntry> = dcsEntries
            }

            val duplicatesValidationPostProcessor = DuplicatesValidationPostProcessor(
                downloadControlStateAccessor = testDownloadControlStateAccessor,
                datasetFileAccessor = TestDatasetFileAccessor,
            )

            // when
            val result = exceptionExpected<IllegalStateException> {
                duplicatesValidationPostProcessor.process()
            }

            // then
            assertThat(result).hasMessage("""
            Found duplicates in DCS files: [DownloadControlStateEntry(_weeksWihoutChange=1, _lastDownloaded=2024-32, _nextDownload=2024-33, _anime=AnimeRaw(
              sources = []
              title = example
              type = UNKNOWN
              episodes = 0
              status = UNKNOWN
              animeSeason = AnimeSeason(season=UNDEFINED, year=0)
              picture = https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png
              thumbnail = https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png
              duration = 0 seconds
              scores = []
              synonyms = []
              relatedAnime = []
              tags = []
            ))]""".trimIndent())
        }

        @Test
        fun `throws exception if anime within dcs entries is duplicated`() {
            // given
            val dcsEntries = listOf(
                DownloadControlStateEntry(
                    _weeksWihoutChange = 8,
                    _lastDownloaded = WeekOfYear(2019, 5),
                    _nextDownload = WeekOfYear(2019, 11),
                    _anime =  AnimeRaw("example"),
                ),
                DownloadControlStateEntry(
                    _weeksWihoutChange = 1,
                    _lastDownloaded = WeekOfYear(2024, 32),
                    _nextDownload = WeekOfYear(2024, 33),
                    _anime =  AnimeRaw("example"),
                ),
            )

            val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                override suspend fun allDcsEntries(): List<DownloadControlStateEntry> = dcsEntries
            }

            val duplicatesValidationPostProcessor = DuplicatesValidationPostProcessor(
                downloadControlStateAccessor = testDownloadControlStateAccessor,
                datasetFileAccessor = TestDatasetFileAccessor,
            )

            // when
            val result = exceptionExpected<IllegalStateException> {
                duplicatesValidationPostProcessor.process()
            }

            // then
            assertThat(result).hasMessage("""
            Found duplicates in anime entries of DCS files: [AnimeRaw(
              sources = []
              title = example
              type = UNKNOWN
              episodes = 0
              status = UNKNOWN
              animeSeason = AnimeSeason(season=UNDEFINED, year=0)
              picture = https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png
              thumbnail = https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png
              duration = 0 seconds
              scores = []
              synonyms = []
              relatedAnime = []
              tags = []
            )]""".trimIndent())
        }

        @Test
        fun `throws exception if dataset is empty`() {
            // given
            val dcsEntries = listOf(
                DownloadControlStateEntry(
                    _weeksWihoutChange = 8,
                    _lastDownloaded = WeekOfYear(2019, 5),
                    _nextDownload = WeekOfYear(2019, 11),
                    _anime =  AnimeRaw("example"),
                ),
                DownloadControlStateEntry(
                    _weeksWihoutChange = 1,
                    _lastDownloaded = WeekOfYear(2024, 32),
                    _nextDownload = WeekOfYear(2024, 33),
                    _anime =  AnimeRaw("Something else"),
                ),
            )

            val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                override suspend fun allDcsEntries(): List<DownloadControlStateEntry> = dcsEntries
            }

            val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                override suspend fun fetchEntries(): List<Anime> = emptyList()
            }

            val duplicatesValidationPostProcessor = DuplicatesValidationPostProcessor(
                downloadControlStateAccessor = testDownloadControlStateAccessor,
                datasetFileAccessor = testDatasetFileAccessor,
            )

            // when
            val result = exceptionExpected<IllegalStateException> {
                duplicatesValidationPostProcessor.process()
            }

            // then
            assertThat(result).hasMessage("No dataset entries found.")
        }

        @Test
        fun `throws exception if dataset contains duplicated entries`() {
            // given
            val dcsEntries = listOf(
                DownloadControlStateEntry(
                    _weeksWihoutChange = 8,
                    _lastDownloaded = WeekOfYear(2019, 5),
                    _nextDownload = WeekOfYear(2019, 11),
                    _anime =  AnimeRaw("example"),
                ),
                DownloadControlStateEntry(
                    _weeksWihoutChange = 1,
                    _lastDownloaded = WeekOfYear(2024, 32),
                    _nextDownload = WeekOfYear(2024, 33),
                    _anime =  AnimeRaw("Something else"),
                ),
            )

            val dataSetEntries = listOf(
                Anime("example"),
                Anime("example"),
            )

            val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                override suspend fun allDcsEntries(): List<DownloadControlStateEntry> = dcsEntries
            }

            val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                override suspend fun fetchEntries(): List<Anime> = dataSetEntries
            }

            val duplicatesValidationPostProcessor = DuplicatesValidationPostProcessor(
                downloadControlStateAccessor = testDownloadControlStateAccessor,
                datasetFileAccessor = testDatasetFileAccessor,
            )

            // when
            val result = exceptionExpected<IllegalStateException> {
                duplicatesValidationPostProcessor.process()
            }

            // then
            assertThat(result).hasMessage("""
            Found duplicates in dataset entries: [Anime(
              sources = []
              title = example
              type = UNKNOWN
              episodes = 0
              status = UNKNOWN
              animeSeason = AnimeSeason(season=UNDEFINED, year=0)
              picture = https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png
              thumbnail = https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png
              duration = 0 seconds
              score = NoScore
              synonyms = []
              relatedAnime = []
              tags = []
            )]""".trimIndent())
        }

        @Test
        fun `throws exception if dataset contains duplicated sources`() {
            // given
            val dcsEntries = listOf(
                DownloadControlStateEntry(
                    _weeksWihoutChange = 8,
                    _lastDownloaded = WeekOfYear(2019, 5),
                    _nextDownload = WeekOfYear(2019, 11),
                    _anime =  AnimeRaw("example"),
                ),
                DownloadControlStateEntry(
                    _weeksWihoutChange = 1,
                    _lastDownloaded = WeekOfYear(2024, 32),
                    _nextDownload = WeekOfYear(2024, 33),
                    _anime =  AnimeRaw("Something else"),
                ),
            )

            val dataSetEntries = listOf(
                Anime(
                    title = "example",
                    sources = hashSetOf(URI("https://example.org/anime/1535")),
                ),
                Anime(
                    title = "Something else",
                    sources = hashSetOf(URI("https://example.org/anime/1535")),
                ),
            )

            val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                override suspend fun allDcsEntries(): List<DownloadControlStateEntry> = dcsEntries
            }

            val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                override suspend fun fetchEntries(): List<Anime> = dataSetEntries
            }

            val duplicatesValidationPostProcessor = DuplicatesValidationPostProcessor(
                downloadControlStateAccessor = testDownloadControlStateAccessor,
                datasetFileAccessor = testDatasetFileAccessor,
            )

            // when
            val result = exceptionExpected<IllegalStateException> {
                duplicatesValidationPostProcessor.process()
            }

            // then
            assertThat(result).hasMessage("Found duplicates sources of dataset: [https://example.org/anime/1535]")
        }

        @Test
        fun `returns true if everything is valid`() {
            runBlocking {
                // given
                val animeRawA = AnimeRaw(
                    _title = "example",
                    _sources = hashSetOf(URI("https://example.org/anime/1234")),
                )
                val animeRawB = AnimeRaw(
                    _title = "Something else",
                    _sources = hashSetOf(URI("https://example.org/anime/1535")),
                )

                val dcsEntries = listOf(
                    DownloadControlStateEntry(
                        _weeksWihoutChange = 8,
                        _lastDownloaded = WeekOfYear(2019, 5),
                        _nextDownload = WeekOfYear(2019, 11),
                        _anime =  animeRawA,
                    ),
                    DownloadControlStateEntry(
                        _weeksWihoutChange = 1,
                        _lastDownloaded = WeekOfYear(2024, 32),
                        _nextDownload = WeekOfYear(2024, 33),
                        _anime =  animeRawB,
                    ),
                )

                val animeA = Anime(
                    title = "example",
                    sources = hashSetOf(URI("https://example.org/anime/1234")),
                )
                val animeB = Anime(
                    title = "Something else",
                    sources = hashSetOf(URI("https://example.org/anime/1535")),
                )

                val dataSetEntries = listOf(
                    animeA,
                    animeB,
                )

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allDcsEntries(): List<DownloadControlStateEntry> = dcsEntries
                }

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override suspend fun fetchEntries(): List<Anime> = dataSetEntries
                }

                val duplicatesValidationPostProcessor = DuplicatesValidationPostProcessor(
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    datasetFileAccessor = testDatasetFileAccessor,
                )

                // when
                val result = duplicatesValidationPostProcessor.process()

                // then
                assertThat(result).isTrue()
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = DuplicatesValidationPostProcessor.instance

                // when
                val result = DuplicatesValidationPostProcessor.instance

                // then
                assertThat(result).isExactlyInstanceOf(DuplicatesValidationPostProcessor::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}