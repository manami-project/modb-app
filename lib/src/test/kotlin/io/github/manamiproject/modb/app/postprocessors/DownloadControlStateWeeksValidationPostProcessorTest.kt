package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.TestDownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateEntry
import io.github.manamiproject.modb.app.downloadcontrolstate.WeekOfYear
import io.github.manamiproject.modb.app.minusWeeks
import io.github.manamiproject.modb.app.postprocessors.DownloadControlStateWeeksValidationPostProcessor
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI
import kotlin.test.Test

internal class DownloadControlStateWeeksValidationPostProcessorTest {

    @Nested
    inner class ProcessTests {

        @ParameterizedTest
        @ValueSource(ints = [0, 1])
        fun `throws exception if an entry contains a for nextDownload which is equal to the current week or even older`(numberOfWeeks: Int) {
            // given
            val list = listOf(
                DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear.currentWeek(),
                    _nextDownload = WeekOfYear.currentWeek().minusWeeks(numberOfWeeks),
                    _anime = Anime(
                        _title = "test1",
                        sources = hashSetOf(URI("https://example.org/anime/1")),
                    )
                ),
            )

            val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                override suspend fun allDcsEntries(): List<DownloadControlStateEntry> = list
            }

            val downloadControlStateWeeksValidator = DownloadControlStateWeeksValidationPostProcessor(
                downloadControlStateAccessor = testDownloadControlStateAccessor
            )

            // when
            val result = exceptionExpected<IllegalStateException> {
                downloadControlStateWeeksValidator.process()
            }

            // then
            assertThat(result).hasMessage("Week for next download of [https://example.org/anime/1] is not set in the future.")
        }

        @Test
        fun `throws exception if an entry contains a value for lastDownload which lies in the future`() {
            // given
            val list = listOf(
                DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear.currentWeek().plusWeeks(1),
                    _nextDownload = WeekOfYear.currentWeek().plusWeeks(1),
                    _anime = Anime(
                        _title = "test1",
                        sources = hashSetOf(URI("https://example.org/anime/1")),
                    )
                ),
            )

            val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                override suspend fun allDcsEntries(): List<DownloadControlStateEntry> = list
            }

            val downloadControlStateWeeksValidator = DownloadControlStateWeeksValidationPostProcessor(
                downloadControlStateAccessor = testDownloadControlStateAccessor
            )

            // when
            val result = exceptionExpected<IllegalStateException> {
                downloadControlStateWeeksValidator.process()
            }

            // then
            assertThat(result).hasMessage("Week for last download of [https://example.org/anime/1] is neither current week nor a week of the past.")
        }

        @ParameterizedTest
        @ValueSource(ints = [0, 1])
        fun `returns true if everything is valid`(numberOfWeeks: Int) {
            runBlocking {
                // given
                val list = listOf(
                    DownloadControlStateEntry(
                        _weeksWihoutChange = 0,
                        _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(numberOfWeeks),
                        _nextDownload = WeekOfYear.currentWeek().plusWeeks(1),
                        _anime = Anime(
                            _title = "test1",
                            sources = hashSetOf(URI("https://example.org/anime/1")),
                        )
                    ),
                )

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allDcsEntries(): List<DownloadControlStateEntry> = list
                }

                val downloadControlStateWeeksValidator = DownloadControlStateWeeksValidationPostProcessor(
                    downloadControlStateAccessor = testDownloadControlStateAccessor
                )

                // when
                val result = downloadControlStateWeeksValidator.process()

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
                val previous = DownloadControlStateWeeksValidationPostProcessor.instance

                // when
                val result = DownloadControlStateWeeksValidationPostProcessor.instance

                // then
                assertThat(result).isExactlyInstanceOf(DownloadControlStateWeeksValidationPostProcessor::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}