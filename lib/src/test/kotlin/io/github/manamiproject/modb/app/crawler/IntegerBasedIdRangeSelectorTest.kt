package io.github.manamiproject.modb.app.crawler

import io.github.manamiproject.modb.app.*
import io.github.manamiproject.modb.app.convfiles.AlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateScheduler
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.test.exceptionExpected
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

internal class IntegerBasedIdRangeSelectorTest {

    @Nested
    inner class IdDownloadListTests {

        @ParameterizedTest
        @ValueSource(ints = [0, -1])
        fun `throws exception if the highest id is 0 or negative`(testValue: Int) {
            // given
            val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = "example.org"
            }

            val testHighestIdDetector = object: HighestIdDetector {
                override suspend fun detectHighestId(): Int = testValue
            }

            val idRangeSelector = IntegerBasedIdRangeSelector(
                metaDataProviderConfig = testMetaDataProviderConfig,
                highestIdDetector = testHighestIdDetector,
                deadEntriesAccessor = TestDeadEntriesAccessor,
                downloadControlStateAccessor = TestDownloadControlStateAccessor,
                downloadControlStateScheduler = TestDownloadControlStateScheduler,
                alreadyDownloadedIdsFinder = TestAlreadyDownloadedIdsFinder,
            )

            // when
            val result = exceptionExpected<IllegalStateException> {
                idRangeSelector.idDownloadList()
            }

            // then
            assertThat(result).hasMessage("Highest ID must be greater than 0")
        }

        @Test
        fun `throws exception highest id is smaller than the highest ID already in dataset`() {
            // given
            val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = "example.org"
            }

            val testHighestIdDetector = object: HighestIdDetector {
                override suspend fun detectHighestId(): Int = 5
            }

            val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                override suspend fun highestIdAlreadyInDataset(metaDataProviderConfig: MetaDataProviderConfig): Int = 8
            }

            val idRangeSelector = IntegerBasedIdRangeSelector(
                metaDataProviderConfig = testMetaDataProviderConfig,
                highestIdDetector = testHighestIdDetector,
                deadEntriesAccessor = TestDeadEntriesAccessor,
                downloadControlStateAccessor = testDownloadControlStateAccessor,
                downloadControlStateScheduler = TestDownloadControlStateScheduler,
                alreadyDownloadedIdsFinder = TestAlreadyDownloadedIdsFinder,
            )

            // when
            val result = exceptionExpected<IllegalStateException> {
                idRangeSelector.idDownloadList()
            }

            // then
            assertThat(result).hasMessage("Quality assurance problem for [example.org]. Detected highest ID [5] is smaller than the highest ID already in dataset [8].")
        }

        @Test
        fun `correctly generates sequence of IDs`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testHighestIdDetector = object: HighestIdDetector {
                    override suspend fun detectHighestId(): Int = 8
                }

                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override suspend fun fetchDeadEntries(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun highestIdAlreadyInDataset(metaDataProviderConfig: MetaDataProviderConfig): Int = 5
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testDefaultAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val idRangeSelector = IntegerBasedIdRangeSelector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    highestIdDetector = testHighestIdDetector,
                    deadEntriesAccessor = testDeadEntriesAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testDefaultAlreadyDownloadedIdsFinder,
                )

                // when
                val result = idRangeSelector.idDownloadList()

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8,
                )
            }
        }

        @Test
        fun `result is in random order`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testHighestIdDetector = object: HighestIdDetector {
                    override suspend fun detectHighestId(): Int = 8
                }

                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override suspend fun fetchDeadEntries(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun highestIdAlreadyInDataset(metaDataProviderConfig: MetaDataProviderConfig): Int = 5
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testDefaultAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val idRangeSelector = IntegerBasedIdRangeSelector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    highestIdDetector = testHighestIdDetector,
                    deadEntriesAccessor = testDeadEntriesAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testDefaultAlreadyDownloadedIdsFinder,
                )

                // when
                val result = idRangeSelector.idDownloadList()

                // then
                val expectedIds = setOf(
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8,
                )
                assertThat(result).containsAll(expectedIds)
                assertThat(result).doesNotContainSequence(expectedIds)
            }
        }

        @Test
        fun `removes IDs which are in dead entries lists from generated sequence`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testHighestIdDetector = object: HighestIdDetector {
                    override suspend fun detectHighestId(): Int = 8
                }

                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override suspend fun fetchDeadEntries(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf("3", "5")
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun highestIdAlreadyInDataset(metaDataProviderConfig: MetaDataProviderConfig): Int = 5
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testDefaultAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val idRangeSelector = IntegerBasedIdRangeSelector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    highestIdDetector = testHighestIdDetector,
                    deadEntriesAccessor = testDeadEntriesAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testDefaultAlreadyDownloadedIdsFinder,
                )

                // when
                val result = idRangeSelector.idDownloadList()

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    1,
                    2,
                    4,
                    6,
                    7,
                    8,
                )
            }
        }

        @Test
        fun `removes IDs which are not scheduled for current week from generated sequence`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testHighestIdDetector = object: HighestIdDetector {
                    override suspend fun detectHighestId(): Int = 8
                }

                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override suspend fun fetchDeadEntries(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun highestIdAlreadyInDataset(metaDataProviderConfig: MetaDataProviderConfig): Int = 5
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf("2", "4")
                }

                val testDefaultAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val idRangeSelector = IntegerBasedIdRangeSelector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    highestIdDetector = testHighestIdDetector,
                    deadEntriesAccessor = testDeadEntriesAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testDefaultAlreadyDownloadedIdsFinder,
                )

                // when
                val result = idRangeSelector.idDownloadList()

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    1,
                    3,
                    5,
                    6,
                    7,
                    8,
                )
            }
        }

        @Test
        fun `removes IDs which have already been downloaded from generated sequence`() {
            runBlocking {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testHighestIdDetector = object: HighestIdDetector {
                    override suspend fun detectHighestId(): Int = 8
                }

                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override suspend fun fetchDeadEntries(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun highestIdAlreadyInDataset(metaDataProviderConfig: MetaDataProviderConfig): Int = 5
                }

                val testDownloadControlStateScheduler = object: DownloadControlStateScheduler by TestDownloadControlStateScheduler {
                    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = emptySet()
                }

                val testDefaultAlreadyDownloadedIdsFinder = object: AlreadyDownloadedIdsFinder by TestAlreadyDownloadedIdsFinder {
                    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = setOf("6", "7")
                }

                val idRangeSelector = IntegerBasedIdRangeSelector(
                    metaDataProviderConfig = testMetaDataProviderConfig,
                    highestIdDetector = testHighestIdDetector,
                    deadEntriesAccessor = testDeadEntriesAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    downloadControlStateScheduler = testDownloadControlStateScheduler,
                    alreadyDownloadedIdsFinder = testDefaultAlreadyDownloadedIdsFinder,
                )

                // when
                val result = idRangeSelector.idDownloadList()

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    1,
                    2,
                    3,
                    4,
                    5,
                    8,
                )
            }
        }
    }
}