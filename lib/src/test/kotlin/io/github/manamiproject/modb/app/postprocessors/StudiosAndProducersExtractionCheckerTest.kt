package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.TestAnimeObjects
import io.github.manamiproject.modb.app.TestDatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DatasetFileAccessor
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.Test

internal class StudiosAndProducersExtractionCheckerTest {

    @Nested
    inner class ProcessTests {

        @Test
        fun `returns true for an empty list`() {
            runTest {
                // given
                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override suspend fun fetchEntries(): List<Anime> = emptyList()
                }

                val postProcessor = StudiosAndProducersExtractionChecker(
                    datasetFileAccessor = testDatasetFileAccessor,
                )

                // when
                val result = postProcessor.process()

                // then
                assertThat(result).isTrue()
            }
        }

        @Test
        fun `returns true if everything is fine`() {
            runTest {
                // given
                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override suspend fun fetchEntries(): List<Anime> = listOf(
                        TestAnimeObjects.NullableNotSet.obj,
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )
                }

                val postProcessor = StudiosAndProducersExtractionChecker(
                    datasetFileAccessor = testDatasetFileAccessor,
                )

                // when
                val result = postProcessor.process()

                // then
                assertThat(result).isTrue()
            }
        }

        @Test
        fun `throws exception if number of studios exceeds threshold`() {
            runTest {
                // given
                val testAnime = TestAnimeObjects.DefaultAnime.obj.copy(
                    sources = hashSetOf(URI("https://example.org/anime/994")),
                    studios = (1..21).map { it.toString() }.toHashSet(),
                )

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override suspend fun fetchEntries(): List<Anime> = listOf(
                        testAnime,
                    )
                }

                val postProcessor = StudiosAndProducersExtractionChecker(
                    datasetFileAccessor = testDatasetFileAccessor,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    postProcessor.process()
                }

                // then
                assertThat(result).hasMessage("Entries having more than [20] studios: [[https://example.org/anime/994]]")
            }
        }

        @Test
        fun `throws exception if number of producers exceeds threshold`() {
            runTest {
                // given
                val testAnime = TestAnimeObjects.DefaultAnime.obj.copy(
                    sources = hashSetOf(URI("https://example.org/anime/994")),
                    producers = (1..31).map { it.toString() }.toHashSet(),
                )

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override suspend fun fetchEntries(): List<Anime> = listOf(
                        testAnime,
                    )
                }

                val postProcessor = StudiosAndProducersExtractionChecker(
                    datasetFileAccessor = testDatasetFileAccessor,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    postProcessor.process()
                }

                // then
                assertThat(result).hasMessage("Entries having more than [30] producers: [[https://example.org/anime/994]]")
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = StudiosAndProducersExtractionChecker.instance

                // when
                val result = StudiosAndProducersExtractionChecker.instance

                // then
                assertThat(result).isExactlyInstanceOf(StudiosAndProducersExtractionChecker::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}