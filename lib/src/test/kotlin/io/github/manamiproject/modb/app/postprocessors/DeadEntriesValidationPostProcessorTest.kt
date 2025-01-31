package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.*
import io.github.manamiproject.modb.app.TestDatasetFileAccessor
import io.github.manamiproject.modb.app.TestDeadEntriesAccessor
import io.github.manamiproject.modb.app.TestDownloadControlStateAccessor
import io.github.manamiproject.modb.app.TestMergeLockAccessor
import io.github.manamiproject.modb.app.dataset.DatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.app.merging.lock.MergeLockAccessor
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.Test

internal class DeadEntriesValidationPostProcessorTest {

    @Nested
    inner class ProcessTests {

        @Test
        fun `returns true if no dead entries have been found`() {
            runBlocking {
                // given
                val testAnime = Anime(
                    _title = "Death Note",
                    _sources = hashSetOf(URI("https://myanimelist.net/anime/1535")),
                )

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override suspend fun fetchEntries(): List<Anime> = listOf(testAnime)
                }

                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override suspend fun determineDeadEntries(sources: Collection<URI>): Set<URI> = emptySet()
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(): List<Anime> = listOf(testAnime)
                }

                val testMergeLockAccess = object: MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun allSourcesInAllMergeLockEntries(): Set<URI> = setOf(testAnime.sources.first())
                }

                val deadEntriesValidationPostProcessor = DeadEntriesValidationPostProcessor(
                    datasetFileAccessor = testDatasetFileAccessor,
                    deadEntriesAccessor = testDeadEntriesAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    mergeLockAccess = testMergeLockAccess,
                )

                // when
                val result = deadEntriesValidationPostProcessor.process()

                // then
                assertThat(result).isTrue()
            }
        }

        @Test
        fun `throws exception if dcs entry contains dead entry`() {
            runBlocking {
                // given
                val testAnime = Anime(
                    _title = "Death Note",
                    _sources = hashSetOf(URI("https://myanimelist.net/anime/1535")),
                )

                val testDeadEntry = Anime(
                    _title = "Death Note",
                    _sources = hashSetOf(URI("https://myanimelist.net/anime/9997")),
                )

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override suspend fun fetchEntries(): List<Anime> = listOf(testAnime)
                }

                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override suspend fun determineDeadEntries(sources: Collection<URI>): Set<URI> = setOf(testDeadEntry.sources.first())
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(): List<Anime> = listOf(
                        testAnime,
                        testDeadEntry,
                    )
                }

                val testMergeLockAccess = object: MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun allSourcesInAllMergeLockEntries(): Set<URI> = setOf(testAnime.sources.first())
                }

                val deadEntriesValidationPostProcessor = DeadEntriesValidationPostProcessor(
                    datasetFileAccessor = testDatasetFileAccessor,
                    deadEntriesAccessor = testDeadEntriesAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    mergeLockAccess = testMergeLockAccess,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    deadEntriesValidationPostProcessor.process()
                }

                // then
                assertThat(result).hasMessage("Dead entries found: [https://myanimelist.net/anime/9997]")
            }
        }

        @Test
        fun `throws exception if merge locks contain dead entry`() {
            runBlocking {
                // given
                val testAnime = Anime(
                    _title = "Death Note",
                    _sources = hashSetOf(URI("https://myanimelist.net/anime/1535")),
                )

                val testDeadEntry = Anime(
                    _title = "Death Note",
                    _sources = hashSetOf(URI("https://myanimelist.net/anime/9997")),
                )

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override suspend fun fetchEntries(): List<Anime> = listOf(testAnime)
                }

                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override suspend fun determineDeadEntries(sources: Collection<URI>): Set<URI> = setOf(testDeadEntry.sources.first())
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(): List<Anime> = listOf(testAnime)
                }

                val testMergeLockAccess = object: MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun allSourcesInAllMergeLockEntries(): Set<URI> = setOf(
                        testAnime.sources.first(),
                        testDeadEntry.sources.first(),
                    )
                }

                val deadEntriesValidationPostProcessor = DeadEntriesValidationPostProcessor(
                    datasetFileAccessor = testDatasetFileAccessor,
                    deadEntriesAccessor = testDeadEntriesAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    mergeLockAccess = testMergeLockAccess,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    deadEntriesValidationPostProcessor.process()
                }

                // then
                assertThat(result).hasMessage("Dead entries found: [https://myanimelist.net/anime/9997]")
            }
        }

        @Test
        fun `throws exception if dataset contains dead entry`() {
            runBlocking {
                // given
                val testAnime = Anime(
                    _title = "Death Note",
                    _sources = hashSetOf(URI("https://myanimelist.net/anime/1535")),
                )

                val testDeadEntry = Anime(
                    _title = "Death Note",
                    _sources = hashSetOf(URI("https://myanimelist.net/anime/9997")),
                )

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override suspend fun fetchEntries(): List<Anime> = listOf(
                        testAnime,
                        testDeadEntry,
                    )
                }

                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override suspend fun determineDeadEntries(sources: Collection<URI>): Set<URI> = setOf(testDeadEntry.sources.first())
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(): List<Anime> = listOf(testAnime)
                }

                val testMergeLockAccess = object: MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun allSourcesInAllMergeLockEntries(): Set<URI> = setOf(testAnime.sources.first())
                }

                val deadEntriesValidationPostProcessor = DeadEntriesValidationPostProcessor(
                    datasetFileAccessor = testDatasetFileAccessor,
                    deadEntriesAccessor = testDeadEntriesAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    mergeLockAccess = testMergeLockAccess,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    deadEntriesValidationPostProcessor.process()
                }

                // then
                assertThat(result).hasMessage("Dead entries found: [https://myanimelist.net/anime/9997]")
            }
        }

        @Test
        fun `returns true if DCS contains sources which would normally be dead entries, but are ignored by using ignoreMetaDataConfiguration`() {
            runBlocking {
                // given
                val testAnime = Anime(
                    _title = "Death Note",
                    _sources = hashSetOf(URI("https://myanimelist.net/anime/1535")),
                )

                val testDeadEntry = Anime(
                    _title = "Death Note",
                    _sources = hashSetOf(URI("https://animecountdown.com/9997")),
                )

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override suspend fun fetchEntries(): List<Anime> = listOf(testAnime)
                }

                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override suspend fun determineDeadEntries(sources: Collection<URI>): Set<URI> = setOf(testDeadEntry.sources.first())
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(): List<Anime> = listOf(
                        testAnime,
                        testDeadEntry,
                    )
                }

                val testMergeLockAccess = object: MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun allSourcesInAllMergeLockEntries(): Set<URI> = setOf(testAnime.sources.first())
                }

                val testIgnoredMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "animecountdown.com"
                }

                val deadEntriesValidationPostProcessor = DeadEntriesValidationPostProcessor(
                    datasetFileAccessor = testDatasetFileAccessor,
                    deadEntriesAccessor = testDeadEntriesAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    mergeLockAccess = testMergeLockAccess,
                    ignoreMetaDataConfiguration = setOf(testIgnoredMetaDataProviderConfig),
                )

                // when
                val result = deadEntriesValidationPostProcessor.process()

                // then
                assertThat(result).isTrue()
            }
        }

        @Test
        fun `returns true if dataset contains sources which would normally be dead entries, but are ignored by using ignoreMetaDataConfiguration`() {
            runBlocking {
                // given
                val testAnime = Anime(
                    _title = "Death Note",
                    _sources = hashSetOf(URI("https://myanimelist.net/anime/1535")),
                )

                val testDeadEntry = Anime(
                    _title = "Death Note",
                    _sources = hashSetOf(URI("https://animecountdown.com/9997")),
                )

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override suspend fun fetchEntries(): List<Anime> = listOf(
                        testAnime,
                        testDeadEntry,
                    )
                }

                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override suspend fun determineDeadEntries(sources: Collection<URI>): Set<URI> = setOf(testDeadEntry.sources.first())
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(): List<Anime> = listOf(testAnime)
                }

                val testMergeLockAccess = object: MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun allSourcesInAllMergeLockEntries(): Set<URI> = setOf(testAnime.sources.first())
                }

                val testIgnoredMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "animecountdown.com"
                }

                val deadEntriesValidationPostProcessor = DeadEntriesValidationPostProcessor(
                    datasetFileAccessor = testDatasetFileAccessor,
                    deadEntriesAccessor = testDeadEntriesAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    mergeLockAccess = testMergeLockAccess,
                    ignoreMetaDataConfiguration = setOf(testIgnoredMetaDataProviderConfig),
                )

                // when
                val result = deadEntriesValidationPostProcessor.process()

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
                val previous = DeadEntriesValidationPostProcessor.instance

                // when
                val result = DeadEntriesValidationPostProcessor.instance

                // then
                assertThat(result).isExactlyInstanceOf(DeadEntriesValidationPostProcessor::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}