package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestDatasetFileAccessor
import io.github.manamiproject.modb.app.TestDeadEntriesAccessor
import io.github.manamiproject.modb.app.TestMetaDataProviderConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.dataset.DatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DatasetFileType
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.copyTo
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.tempDirectory
import io.github.manamiproject.modb.test.testResource
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.io.path.createDirectory
import kotlin.test.Test

internal class NumberOfEntriesValidationPostProcessorTest {

    @Nested
    inner class ProcessTests {

        @Nested
        inner class DataSetFilestests {

            @Test
            fun `returns true if all dataset entries have the same number of entries`() {
                runBlocking {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.json")
                            DatasetFileType.JSON_MINIFIED -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries-minified.json")
                            DatasetFileType.ZIP -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.zip")
                        }
                    }

                    val numberOfEntriesValidationPostProcessor = NumberOfEntriesValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = TestDeadEntriesAccessor,
                    )

                    // when
                    val result = numberOfEntriesValidationPostProcessor.process()

                    // then
                    assertThat(result).isTrue()
                }
            }

            @Test
            fun `throws exception if dataset json file has differing number of entries`() {
                runBlocking {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/1-entry.json")
                            DatasetFileType.JSON_MINIFIED -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries-minified.json")
                            DatasetFileType.ZIP -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.zip")
                        }
                    }

                    val numberOfEntriesValidationPostProcessor = NumberOfEntriesValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = TestDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        numberOfEntriesValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("Number of dataset files differ: [json=1, jsonMinified=2, zip=2]")
                }
            }

            @Test
            fun `throws exception if dataset json minified file has differing number of entries`() {
                runBlocking {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.json")
                            DatasetFileType.JSON_MINIFIED -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/1-entry-minified.json")
                            DatasetFileType.ZIP -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.zip")
                        }
                    }

                    val numberOfEntriesValidationPostProcessor = NumberOfEntriesValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = TestDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        numberOfEntriesValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("Number of dataset files differ: [json=2, jsonMinified=1, zip=2]")
                }
            }

            @Test
            fun `throws exception if dataset zip file has differing number of entries`() {
                runBlocking {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.json")
                            DatasetFileType.JSON_MINIFIED -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries-minified.json")
                            DatasetFileType.ZIP -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/1-entry.zip")
                        }
                    }

                    val numberOfEntriesValidationPostProcessor = NumberOfEntriesValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = TestDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        numberOfEntriesValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("Number of dataset files differ: [json=2, jsonMinified=2, zip=1]")
                }
            }

            @Test
            fun `throws exception if dataset json file doesn't exist`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> tempDir.resolve("not-exists.json")
                            DatasetFileType.JSON_MINIFIED -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries-minified.json")
                            DatasetFileType.ZIP -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.zip")
                        }
                    }

                    val numberOfEntriesValidationPostProcessor = NumberOfEntriesValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = TestDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalArgumentException> {
                        numberOfEntriesValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessageStartingWith("The given path does not exist or is not a regular file: [")
                    assertThat(result).hasMessageEndingWith("not-exists.json]")
                }
            }

            @Test
            fun `throws exception if dataset json minified file doesn't exist`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.json")
                            DatasetFileType.JSON_MINIFIED -> tempDir.resolve("not-exists-minified.json")
                            DatasetFileType.ZIP -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.zip")
                        }
                    }

                    val numberOfEntriesValidationPostProcessor = NumberOfEntriesValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = TestDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalArgumentException> {
                        numberOfEntriesValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessageStartingWith("The given path does not exist or is not a regular file: [")
                    assertThat(result).hasMessageEndingWith("not-exists-minified.json]")
                }
            }

            @Test
            fun `throws exception if dataset zip file doesn't exist`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.json")
                            DatasetFileType.JSON_MINIFIED -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries-minified.json")
                            DatasetFileType.ZIP -> tempDir.resolve("not-exists.zip")
                        }
                    }

                    val numberOfEntriesValidationPostProcessor = NumberOfEntriesValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = TestDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalArgumentException> {
                        numberOfEntriesValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessageStartingWith("The given path does not exist or is not a regular file: [")
                    assertThat(result).hasMessageEndingWith("not-exists.zip]")
                }
            }
        }

        @Nested
        inner class DeadEntriesFilesTests {

            @Test
            fun `returns true if all dead entries have the same number of entries`() {
                tempDirectory {
                    // given
                    val testMetaDataProviderConfig1 = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                    }

                    val testMetaDataProviderConfig2 = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "other-example.me"
                    }

                    val testAppConfig = object: Config by TestAppConfig {
                        override fun deadEntriesSupported(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(
                            testMetaDataProviderConfig1,
                            testMetaDataProviderConfig2,
                        )
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.json")
                            DatasetFileType.JSON_MINIFIED -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries-minified.json")
                            DatasetFileType.ZIP -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.zip")
                        }
                    }

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries.json")
                            DatasetFileType.JSON_MINIFIED -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries-minified.json")
                            DatasetFileType.ZIP -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries.zip")
                        }
                    }

                    val numberOfEntriesValidationPostProcessor = NumberOfEntriesValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = testDeadEntriesAccessor,
                    )

                    tempDir.resolve(testMetaDataProviderConfig1.hostname()).createDirectory()
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-1-entry.json")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.JSON))
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-1-entry-minified.json")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.JSON_MINIFIED))
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-1-entry.zip")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.ZIP))

                    tempDir.resolve(testMetaDataProviderConfig2.hostname()).createDirectory()
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-2-entries.json")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.JSON))
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-2-entries-minified.json")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.JSON_MINIFIED))
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-2-entries.zip")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.ZIP))

                    // when
                    val result = numberOfEntriesValidationPostProcessor.process()

                    // then
                    assertThat(result).isTrue()
                }
            }

            @Test
            fun `throws exception if dead entries zip file has differing number if entries`() {
                tempDirectory {
                    // given
                    val testMetaDataProviderConfig1 = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                    }

                    val testMetaDataProviderConfig2 = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "other-example.me"
                    }

                    val testAppConfig = object: Config by TestAppConfig {
                        override fun deadEntriesSupported(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(
                            testMetaDataProviderConfig1,
                            testMetaDataProviderConfig2,
                        )
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.json")
                            DatasetFileType.JSON_MINIFIED -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries-minified.json")
                            DatasetFileType.ZIP -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.zip")
                        }
                    }

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries.json")
                            DatasetFileType.JSON_MINIFIED -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries-minified.json")
                            DatasetFileType.ZIP -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries.zip")
                        }
                    }

                    val numberOfEntriesValidationPostProcessor = NumberOfEntriesValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = testDeadEntriesAccessor,
                    )

                    tempDir.resolve(testMetaDataProviderConfig1.hostname()).createDirectory()
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-2-entries.json")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.JSON))
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-1-entry-minified.json")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.JSON_MINIFIED))
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-1-entry.zip")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.ZIP))

                    tempDir.resolve(testMetaDataProviderConfig2.hostname()).createDirectory()
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-2-entries.json")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.JSON))
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-2-entries-minified.json")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.JSON_MINIFIED))
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-2-entries.zip")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.ZIP))

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        numberOfEntriesValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("Number of dead entries files differ for [example.org]: [json=2, jsonMinified=1, zip=1]")
                }
            }

            @Test
            fun `throws exception if dead entries json minified file has differing number if entries`() {
                tempDirectory {
                    // given
                    val testMetaDataProviderConfig1 = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                    }

                    val testMetaDataProviderConfig2 = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "other-example.me"
                    }

                    val testAppConfig = object: Config by TestAppConfig {
                        override fun deadEntriesSupported(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(
                            testMetaDataProviderConfig1,
                            testMetaDataProviderConfig2,
                        )
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.json")
                            DatasetFileType.JSON_MINIFIED -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries-minified.json")
                            DatasetFileType.ZIP -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.zip")
                        }
                    }

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries.json")
                            DatasetFileType.JSON_MINIFIED -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries-minified.json")
                            DatasetFileType.ZIP -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries.zip")
                        }
                    }

                    val numberOfEntriesValidationPostProcessor = NumberOfEntriesValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = testDeadEntriesAccessor,
                    )

                    tempDir.resolve(testMetaDataProviderConfig1.hostname()).createDirectory()
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-1-entry.json")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.JSON))
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-1-entry-minified.json")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.JSON_MINIFIED))
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-1-entry.zip")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.ZIP))

                    tempDir.resolve(testMetaDataProviderConfig2.hostname()).createDirectory()
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-2-entries.json")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.JSON))
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-1-entry-minified.json")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.JSON_MINIFIED))
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-2-entries.zip")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.ZIP))

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        numberOfEntriesValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("Number of dead entries files differ for [other-example.me]: [json=2, jsonMinified=1, zip=2]")
                }
            }

            @Test
            fun `throws exception if dead entries json file has differing number if entries`() {
                tempDirectory {
                    // given
                    val testMetaDataProviderConfig1 = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                    }

                    val testMetaDataProviderConfig2 = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "other-example.me"
                    }

                    val testAppConfig = object: Config by TestAppConfig {
                        override fun deadEntriesSupported(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(
                            testMetaDataProviderConfig1,
                            testMetaDataProviderConfig2,
                        )
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.json")
                            DatasetFileType.JSON_MINIFIED -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries-minified.json")
                            DatasetFileType.ZIP -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.zip")
                        }
                    }

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries.json")
                            DatasetFileType.JSON_MINIFIED -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries-minified.json")
                            DatasetFileType.ZIP -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries.zip")
                        }
                    }

                    val numberOfEntriesValidationPostProcessor = NumberOfEntriesValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = testDeadEntriesAccessor,
                    )

                    tempDir.resolve(testMetaDataProviderConfig1.hostname()).createDirectory()
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-1-entry.json")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.JSON))
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-1-entry-minified.json")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.JSON_MINIFIED))
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-2-entries.zip")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.ZIP))

                    tempDir.resolve(testMetaDataProviderConfig2.hostname()).createDirectory()
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-2-entries.json")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.JSON))
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-2-entries-minified.json")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.JSON_MINIFIED))
                    testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-2-entries.zip")
                        .copyTo(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.ZIP))

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        numberOfEntriesValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("Number of dead entries files differ for [example.org]: [json=1, jsonMinified=1, zip=2]")
                }
            }

            @Test
            fun `throws exception if dead entries json file doesn't exist`() {
                tempDirectory {
                    // given
                    val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                    }

                    val testAppConfig = object: Config by TestAppConfig {
                        override fun deadEntriesSupported(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(
                            testMetaDataProviderConfig,
                        )
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.json")
                            DatasetFileType.JSON_MINIFIED -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries-minified.json")
                            DatasetFileType.ZIP -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.zip")
                        }
                    }

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> tempDir.resolve("not-exists.json")
                            DatasetFileType.JSON_MINIFIED -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-2-entries-minified.json")
                            DatasetFileType.ZIP -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-2-entries.zip")
                        }
                    }

                    val numberOfEntriesValidationPostProcessor = NumberOfEntriesValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = testDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalArgumentException> {
                        numberOfEntriesValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessageStartingWith("The given path does not exist or is not a regular file: [")
                    assertThat(result).hasMessageEndingWith("not-exists.json]")
                }
            }

            @Test
            fun `throws exception if dead entries json minified file doesn't exist`() {
                tempDirectory {
                    // given
                    val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                    }

                    val testAppConfig = object: Config by TestAppConfig {
                        override fun deadEntriesSupported(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(
                            testMetaDataProviderConfig,
                        )
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.json")
                            DatasetFileType.JSON_MINIFIED -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries-minified.json")
                            DatasetFileType.ZIP -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.zip")
                        }
                    }

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-2-entries.json")
                            DatasetFileType.JSON_MINIFIED -> tempDir.resolve("not-exists-minified.json")
                            DatasetFileType.ZIP -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-2-entries.zip")
                        }
                    }

                    val numberOfEntriesValidationPostProcessor = NumberOfEntriesValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = testDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalArgumentException> {
                        numberOfEntriesValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessageStartingWith("The given path does not exist or is not a regular file: [")
                    assertThat(result).hasMessageEndingWith("not-exists-minified.json]")
                }
            }

            @Test
            fun `throws exception if dead entries zip file doesn't exist`() {
                tempDirectory {
                    // given
                    val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                    }

                    val testAppConfig = object: Config by TestAppConfig {
                        override fun deadEntriesSupported(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(
                            testMetaDataProviderConfig,
                        )
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.json")
                            DatasetFileType.JSON_MINIFIED -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries-minified.json")
                            DatasetFileType.ZIP -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/2-entries.zip")
                        }
                    }

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-2-entries.json")
                            DatasetFileType.JSON_MINIFIED -> testResource("postprocessors/NumberOfEntriesValidationPostProcessorTest/dead-entries-2-entries-minified.json")
                            DatasetFileType.ZIP -> tempDir.resolve("not-exists.zip")
                        }
                    }

                    val numberOfEntriesValidationPostProcessor = NumberOfEntriesValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = testDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalArgumentException> {
                        numberOfEntriesValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessageStartingWith("The given path does not exist or is not a regular file: [")
                    assertThat(result).hasMessageEndingWith("not-exists.zip]")
                }
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = NumberOfEntriesValidationPostProcessor.instance

                // when
                val result = NumberOfEntriesValidationPostProcessor.instance

                // then
                assertThat(result).isExactlyInstanceOf(NumberOfEntriesValidationPostProcessor::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}