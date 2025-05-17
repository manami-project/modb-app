package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.*
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.dataset.DatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DatasetFileType
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.createZipOf
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.tempDirectory
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
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val prettyPrintFile = tempDir.resolve("prettyprint.json")
                    val prettyPrintJson = createExpectedDatasetPrettyPrint(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint,
                    )
                    prettyPrintJson.writeToFile(prettyPrintFile)

                    val minifiedFile = tempDir.resolve("minified.json")
                    val minifiedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedMinified,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    minifiedJson.writeToFile(minifiedFile)

                    val zippedJsonFile = tempDir.resolve("zipped.json")
                    val zippedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    zippedJson.writeToFile(zippedJsonFile)
                    val zipFile = tempDir.resolve("zipped.zip").createZipOf(zippedJsonFile)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> prettyPrintFile
                            DatasetFileType.JSON_MINIFIED -> minifiedFile
                            DatasetFileType.ZIP -> zipFile
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
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val prettyPrintFile = tempDir.resolve("prettyprint.json")
                    val prettyPrintJson = createExpectedDatasetPrettyPrint(
                        TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint,
                    )
                    prettyPrintJson.writeToFile(prettyPrintFile)

                    val minifiedFile = tempDir.resolve("minified.json")
                    val minifiedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedMinified,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    minifiedJson.writeToFile(minifiedFile)

                    val zippedJsonFile = tempDir.resolve("zipped.json")
                    val zippedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    zippedJson.writeToFile(zippedJsonFile)
                    val zipFile = tempDir.resolve("zipped.zip").createZipOf(zippedJsonFile)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> prettyPrintFile
                            DatasetFileType.JSON_MINIFIED -> minifiedFile
                            DatasetFileType.ZIP -> zipFile
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
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val prettyPrintFile = tempDir.resolve("prettyprint.json")
                    val prettyPrintJson = createExpectedDatasetPrettyPrint(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint,
                    )
                    prettyPrintJson.writeToFile(prettyPrintFile)

                    val minifiedFile = tempDir.resolve("minified.json")
                    val minifiedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    minifiedJson.writeToFile(minifiedFile)

                    val zippedJsonFile = tempDir.resolve("zipped.json")
                    val zippedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    zippedJson.writeToFile(zippedJsonFile)
                    val zipFile = tempDir.resolve("zipped.zip").createZipOf(zippedJsonFile)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> prettyPrintFile
                            DatasetFileType.JSON_MINIFIED -> minifiedFile
                            DatasetFileType.ZIP -> zipFile
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
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val prettyPrintFile = tempDir.resolve("prettyprint.json")
                    val prettyPrintJson = createExpectedDatasetPrettyPrint(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint,
                    )
                    prettyPrintJson.writeToFile(prettyPrintFile)

                    val minifiedFile = tempDir.resolve("minified.json")
                    val minifiedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedMinified,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    minifiedJson.writeToFile(minifiedFile)

                    val zippedJsonFile = tempDir.resolve("zipped.json")
                    val zippedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    zippedJson.writeToFile(zippedJsonFile)
                    val zipFile = tempDir.resolve("zipped.zip").createZipOf(zippedJsonFile)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> prettyPrintFile
                            DatasetFileType.JSON_MINIFIED -> minifiedFile
                            DatasetFileType.ZIP -> zipFile
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

                    val minifiedFile = tempDir.resolve("minified.json")
                    val minifiedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedMinified,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    minifiedJson.writeToFile(minifiedFile)

                    val zippedJsonFile = tempDir.resolve("zipped.json")
                    val zippedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    zippedJson.writeToFile(zippedJsonFile)
                    val zipFile = tempDir.resolve("zipped.zip").createZipOf(zippedJsonFile)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> tempDir.resolve("non-existent-pretty-print.json")
                            DatasetFileType.JSON_MINIFIED -> minifiedFile
                            DatasetFileType.ZIP -> zipFile
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
                    assertThat(result).hasMessageEndingWith("non-existent-pretty-print.json]")
                }
            }

            @Test
            fun `throws exception if dataset json minified file doesn't exist`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val prettyPrintFile = tempDir.resolve("prettyprint.json")
                    val prettyPrintJson = createExpectedDatasetPrettyPrint(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint,
                    )
                    prettyPrintJson.writeToFile(prettyPrintFile)

                    val zippedJsonFile = tempDir.resolve("zipped.json")
                    val zippedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    zippedJson.writeToFile(zippedJsonFile)
                    val zipFile = tempDir.resolve("zipped.zip").createZipOf(zippedJsonFile)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> prettyPrintFile
                            DatasetFileType.JSON_MINIFIED -> tempDir.resolve("non-existent-minified.json")
                            DatasetFileType.ZIP -> zipFile
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
                    assertThat(result).hasMessageEndingWith("non-existent-minified.json]")
                }
            }

            @Test
            fun `throws exception if dataset zip file doesn't exist`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val prettyPrintFile = tempDir.resolve("prettyprint.json")
                    val prettyPrintJson = createExpectedDatasetPrettyPrint(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint,
                    )
                    prettyPrintJson.writeToFile(prettyPrintFile)

                    val minifiedFile = tempDir.resolve("minified.json")
                    val minifiedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedMinified,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    minifiedJson.writeToFile(minifiedFile)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> prettyPrintFile
                            DatasetFileType.JSON_MINIFIED -> minifiedFile
                            DatasetFileType.ZIP -> tempDir.resolve("non-existent.zip")
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
                    assertThat(result).hasMessageEndingWith("non-existent.zip]")
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

                    val prettyPrintFile = tempDir.resolve("prettyprint.json")
                    val prettyPrintJson = createExpectedDatasetPrettyPrint(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                    )
                    prettyPrintJson.writeToFile(prettyPrintFile)

                    val minifiedFile = tempDir.resolve("minified.json")
                    val minifiedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedMinified,
                    )
                    minifiedJson.writeToFile(minifiedFile)

                    val zippedJsonFile = tempDir.resolve("zipped.json")
                    val zippedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                    )
                    zippedJson.writeToFile(zippedJsonFile)
                    val zipFile = tempDir.resolve("zipped.zip").createZipOf(zippedJsonFile)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> prettyPrintFile
                            DatasetFileType.JSON_MINIFIED -> minifiedFile
                            DatasetFileType.ZIP -> zipFile
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

                    val testMetaDataProviderConfig1Json = createExpectedDeadEntriesPrettyPrint(
                        "10001",
                    )
                    val testMetaDataProviderConfig1Minified = createExpectedDeadEntriesMinified(
                        "10001",
                    )
                    val testMetaDataProviderConfig1Zipped = createExpectedDeadEntriesMinified(
                        "10001",
                    )
                    tempDir.resolve(testMetaDataProviderConfig1.hostname()).createDirectory()
                    testMetaDataProviderConfig1Json.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.JSON))
                    testMetaDataProviderConfig1Minified.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.JSON_MINIFIED))
                    val testMetaDataProviderConfig1JsonForZip = tempDir.resolve(testMetaDataProviderConfig1.hostname()).resolve("jsonForZip.json")
                    testMetaDataProviderConfig1Zipped.writeToFile(testMetaDataProviderConfig1JsonForZip)
                    testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.ZIP).createZipOf(testMetaDataProviderConfig1JsonForZip)

                    val testMetaDataProviderConfig2Json = createExpectedDeadEntriesPrettyPrint(
                        "20008",
                        "20009",
                    )
                    val testMetaDataProviderConfig2Minified = createExpectedDeadEntriesMinified(
                        "20008",
                        "20009",
                    )
                    val testMetaDataProviderConfig2Zipped = createExpectedDeadEntriesMinified(
                        "20008",
                        "20009",
                    )
                    tempDir.resolve(testMetaDataProviderConfig2.hostname()).createDirectory()
                    testMetaDataProviderConfig2Json.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.JSON))
                    testMetaDataProviderConfig2Minified.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.JSON_MINIFIED))
                    val testMetaDataProviderConfig2JsonForZip = tempDir.resolve(testMetaDataProviderConfig2.hostname()).resolve("jsonForZip.json")
                    testMetaDataProviderConfig2Zipped.writeToFile(testMetaDataProviderConfig2JsonForZip)
                    testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.ZIP).createZipOf(testMetaDataProviderConfig2JsonForZip)

                    // when
                    val result = numberOfEntriesValidationPostProcessor.process()

                    // then
                    assertThat(result).isTrue()
                }
            }

            @Test
            fun `throws exception if dead entries json file has differing number of entries`() {
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

                    val prettyPrintFile = tempDir.resolve("prettyprint.json")
                    val prettyPrintJson = createExpectedDatasetPrettyPrint(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint,
                    )
                    prettyPrintJson.writeToFile(prettyPrintFile)

                    val minifiedFile = tempDir.resolve("minified.json")
                    val minifiedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedMinified,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    minifiedJson.writeToFile(minifiedFile)

                    val zippedJsonFile = tempDir.resolve("zipped.json")
                    val zippedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    zippedJson.writeToFile(zippedJsonFile)
                    val zipFile = tempDir.resolve("zipped.zip").createZipOf(zippedJsonFile)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> prettyPrintFile
                            DatasetFileType.JSON_MINIFIED -> minifiedFile
                            DatasetFileType.ZIP -> zipFile
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

                    val testMetaDataProviderConfig1Json = createExpectedDeadEntriesPrettyPrint(
                        "10001",
                        "300",
                    )
                    val testMetaDataProviderConfig1Minified = createExpectedDeadEntriesMinified(
                        "10001",
                    )
                    val testMetaDataProviderConfig1Zipped = createExpectedDeadEntriesMinified(
                        "10001",
                    )
                    tempDir.resolve(testMetaDataProviderConfig1.hostname()).createDirectory()
                    testMetaDataProviderConfig1Json.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.JSON))
                    testMetaDataProviderConfig1Minified.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.JSON_MINIFIED))
                    val testMetaDataProviderConfig1JsonForZip = tempDir.resolve(testMetaDataProviderConfig1.hostname()).resolve("jsonForZip.json")
                    testMetaDataProviderConfig1Zipped.writeToFile(testMetaDataProviderConfig1JsonForZip)
                    testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.ZIP).createZipOf(testMetaDataProviderConfig1JsonForZip)

                    val testMetaDataProviderConfig2Json = createExpectedDeadEntriesPrettyPrint(
                        "20008",
                        "20009",
                    )
                    val testMetaDataProviderConfig2Minified = createExpectedDeadEntriesMinified(
                        "20008",
                        "20009",
                    )
                    val testMetaDataProviderConfig2Zipped = createExpectedDeadEntriesMinified(
                        "20008",
                        "20009",
                    )
                    tempDir.resolve(testMetaDataProviderConfig2.hostname()).createDirectory()
                    testMetaDataProviderConfig2Json.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.JSON))
                    testMetaDataProviderConfig2Minified.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.JSON_MINIFIED))
                    val testMetaDataProviderConfig2JsonForZip = tempDir.resolve(testMetaDataProviderConfig2.hostname()).resolve("jsonForZip.json")
                    testMetaDataProviderConfig2Zipped.writeToFile(testMetaDataProviderConfig2JsonForZip)
                    testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.ZIP).createZipOf(testMetaDataProviderConfig2JsonForZip)

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        numberOfEntriesValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("Number of dead entries files differ for [example.org]: [json=2, jsonMinified=1, zip=1]")
                }
            }

            @Test
            fun `throws exception if dead entries json minified file has differing number of entries`() {
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

                    val prettyPrintFile = tempDir.resolve("prettyprint.json")
                    val prettyPrintJson = createExpectedDatasetPrettyPrint(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint,
                    )
                    prettyPrintJson.writeToFile(prettyPrintFile)

                    val minifiedFile = tempDir.resolve("minified.json")
                    val minifiedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedMinified,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    minifiedJson.writeToFile(minifiedFile)

                    val zippedJsonFile = tempDir.resolve("zipped.json")
                    val zippedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    zippedJson.writeToFile(zippedJsonFile)
                    val zipFile = tempDir.resolve("zipped.zip").createZipOf(zippedJsonFile)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> prettyPrintFile
                            DatasetFileType.JSON_MINIFIED -> minifiedFile
                            DatasetFileType.ZIP -> zipFile
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

                    val testMetaDataProviderConfig1Json = createExpectedDeadEntriesPrettyPrint(
                        "10001",
                    )
                    val testMetaDataProviderConfig1Minified = createExpectedDeadEntriesMinified(
                        "10001",
                    )
                    val testMetaDataProviderConfig1Zipped = createExpectedDeadEntriesMinified(
                        "10001",
                    )
                    tempDir.resolve(testMetaDataProviderConfig1.hostname()).createDirectory()
                    testMetaDataProviderConfig1Json.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.JSON))
                    testMetaDataProviderConfig1Minified.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.JSON_MINIFIED))
                    val testMetaDataProviderConfig1JsonForZip = tempDir.resolve(testMetaDataProviderConfig1.hostname()).resolve("jsonForZip.json")
                    testMetaDataProviderConfig1Zipped.writeToFile(testMetaDataProviderConfig1JsonForZip)
                    testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.ZIP).createZipOf(testMetaDataProviderConfig1JsonForZip)

                    val testMetaDataProviderConfig2Json = createExpectedDeadEntriesPrettyPrint(
                        "20008",
                        "20009",
                    )
                    val testMetaDataProviderConfig2Minified = createExpectedDeadEntriesMinified(
                        "20009",
                    )
                    val testMetaDataProviderConfig2Zipped = createExpectedDeadEntriesMinified(
                        "20008",
                        "20009",
                    )
                    tempDir.resolve(testMetaDataProviderConfig2.hostname()).createDirectory()
                    testMetaDataProviderConfig2Json.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.JSON))
                    testMetaDataProviderConfig2Minified.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.JSON_MINIFIED))
                    val testMetaDataProviderConfig2JsonForZip = tempDir.resolve(testMetaDataProviderConfig2.hostname()).resolve("jsonForZip.json")
                    testMetaDataProviderConfig2Zipped.writeToFile(testMetaDataProviderConfig2JsonForZip)
                    testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.ZIP).createZipOf(testMetaDataProviderConfig2JsonForZip)

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        numberOfEntriesValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("Number of dead entries files differ for [other-example.me]: [json=2, jsonMinified=1, zip=2]")
                }
            }

            @Test
            fun `throws exception if dead entries zip file has differing number of entries`() {
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

                    val prettyPrintFile = tempDir.resolve("prettyprint.json")
                    val prettyPrintJson = createExpectedDatasetPrettyPrint(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint,
                    )
                    prettyPrintJson.writeToFile(prettyPrintFile)

                    val minifiedFile = tempDir.resolve("minified.json")
                    val minifiedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedMinified,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    minifiedJson.writeToFile(minifiedFile)

                    val zippedJsonFile = tempDir.resolve("zipped.json")
                    val zippedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    zippedJson.writeToFile(zippedJsonFile)
                    val zipFile = tempDir.resolve("zipped.zip").createZipOf(zippedJsonFile)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> prettyPrintFile
                            DatasetFileType.JSON_MINIFIED -> minifiedFile
                            DatasetFileType.ZIP -> zipFile
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

                    val testMetaDataProviderConfig1Json = createExpectedDeadEntriesPrettyPrint(
                        "10001",
                    )
                    val testMetaDataProviderConfig1Minified = createExpectedDeadEntriesMinified(
                        "10001",
                    )
                    val testMetaDataProviderConfig1Zipped = createExpectedDeadEntriesMinified(
                        "10001",
                        "300",
                    )
                    tempDir.resolve(testMetaDataProviderConfig1.hostname()).createDirectory()
                    testMetaDataProviderConfig1Json.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.JSON))
                    testMetaDataProviderConfig1Minified.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.JSON_MINIFIED))
                    val testMetaDataProviderConfig1JsonForZip = tempDir.resolve(testMetaDataProviderConfig1.hostname()).resolve("jsonForZip.json")
                    testMetaDataProviderConfig1Zipped.writeToFile(testMetaDataProviderConfig1JsonForZip)
                    testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, DatasetFileType.ZIP).createZipOf(testMetaDataProviderConfig1JsonForZip)

                    val testMetaDataProviderConfig2Json = createExpectedDeadEntriesPrettyPrint(
                        "20008",
                        "20009",
                    )
                    val testMetaDataProviderConfig2Minified = createExpectedDeadEntriesMinified(
                        "20008",
                        "20009",
                    )
                    val testMetaDataProviderConfig2Zipped = createExpectedDeadEntriesMinified(
                        "20008",
                        "20009",
                    )
                    tempDir.resolve(testMetaDataProviderConfig2.hostname()).createDirectory()
                    testMetaDataProviderConfig2Json.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.JSON))
                    testMetaDataProviderConfig2Minified.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.JSON_MINIFIED))
                    val testMetaDataProviderConfig2JsonForZip = tempDir.resolve(testMetaDataProviderConfig2.hostname()).resolve("jsonForZip.json")
                    testMetaDataProviderConfig2Zipped.writeToFile(testMetaDataProviderConfig2JsonForZip)
                    testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, DatasetFileType.ZIP).createZipOf(testMetaDataProviderConfig2JsonForZip)

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

                    val prettyPrintFile = tempDir.resolve("prettyprint.json")
                    val prettyPrintJson = createExpectedDatasetPrettyPrint(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint,
                    )
                    prettyPrintJson.writeToFile(prettyPrintFile)

                    val minifiedFile = tempDir.resolve("minified.json")
                    val minifiedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedMinified,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    minifiedJson.writeToFile(minifiedFile)

                    val zippedJsonFile = tempDir.resolve("zipped.json")
                    val zippedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    zippedJson.writeToFile(zippedJsonFile)
                    val zipFile = tempDir.resolve("zipped.zip").createZipOf(zippedJsonFile)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> prettyPrintFile
                            DatasetFileType.JSON_MINIFIED -> minifiedFile
                            DatasetFileType.ZIP -> zipFile
                        }
                    }

                    val deadEntriesMinified = tempDir.resolve("deadEntriesMinified.json")
                    createExpectedDeadEntriesMinified().writeToFile(deadEntriesMinified)
                    val deadEntriesZip = tempDir.resolve("deadEntries.zip")
                    deadEntriesZip.createZipOf(deadEntriesMinified)

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> tempDir.resolve("non-existent.json")
                            DatasetFileType.JSON_MINIFIED -> deadEntriesMinified
                            DatasetFileType.ZIP -> deadEntriesZip
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
                    assertThat(result).hasMessageEndingWith("non-existent.json]")
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

                    val prettyPrintFile = tempDir.resolve("prettyprint.json")
                    val prettyPrintJson = createExpectedDatasetPrettyPrint(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint,
                    )
                    prettyPrintJson.writeToFile(prettyPrintFile)

                    val minifiedFile = tempDir.resolve("minified.json")
                    val minifiedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedMinified,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    minifiedJson.writeToFile(minifiedFile)

                    val zippedJsonFile = tempDir.resolve("zipped.json")
                    val zippedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    zippedJson.writeToFile(zippedJsonFile)
                    val zipFile = tempDir.resolve("zipped.zip").createZipOf(zippedJsonFile)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> prettyPrintFile
                            DatasetFileType.JSON_MINIFIED -> minifiedFile
                            DatasetFileType.ZIP -> zipFile
                        }
                    }

                    val deadEntriesPrettyPrint = tempDir.resolve("deadEntriesPrettyPrint.json")
                    createExpectedDeadEntriesPrettyPrint().writeToFile(deadEntriesPrettyPrint)
                    val deadEntriesZip = tempDir.resolve("deadEntries.zip")
                    deadEntriesZip.createZipOf(deadEntriesPrettyPrint)

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> deadEntriesPrettyPrint
                            DatasetFileType.JSON_MINIFIED -> tempDir.resolve("non-existent-minified.json")
                            DatasetFileType.ZIP -> deadEntriesZip
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
                    assertThat(result).hasMessageEndingWith("non-existent-minified.json]")
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

                    val prettyPrintFile = tempDir.resolve("prettyprint.json")
                    val prettyPrintJson = createExpectedDatasetPrettyPrint(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint,
                    )
                    prettyPrintJson.writeToFile(prettyPrintFile)

                    val minifiedFile = tempDir.resolve("minified.json")
                    val minifiedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedMinified,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    minifiedJson.writeToFile(minifiedFile)

                    val zippedJsonFile = tempDir.resolve("zipped.json")
                    val zippedJson = createExpectedDatasetMinified(
                        TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                        TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    )
                    zippedJson.writeToFile(zippedJsonFile)
                    val zipFile = tempDir.resolve("zipped.zip").createZipOf(zippedJsonFile)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> prettyPrintFile
                            DatasetFileType.JSON_MINIFIED -> minifiedFile
                            DatasetFileType.ZIP -> zipFile
                        }
                    }

                    val deadEntriesPrettyPrint = tempDir.resolve("deadEntriesPrettyPrint.json")
                    createExpectedDeadEntriesPrettyPrint().writeToFile(deadEntriesPrettyPrint)
                    val deadEntriesinified = tempDir.resolve("deadEntriesMinified.json")
                    createExpectedDeadEntriesMinified().writeToFile(deadEntriesinified)

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            DatasetFileType.JSON -> deadEntriesPrettyPrint
                            DatasetFileType.JSON_MINIFIED -> deadEntriesinified
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