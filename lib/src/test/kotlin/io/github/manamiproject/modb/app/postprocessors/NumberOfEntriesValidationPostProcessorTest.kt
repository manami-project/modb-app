package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.*
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.dataset.DatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DatasetFileType
import io.github.manamiproject.modb.app.dataset.DatasetFileType.*
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.extensions.writeToZstandardFile
import io.github.manamiproject.modb.serde.json.serializer.DatasetJsonLinesSerializer
import io.github.manamiproject.modb.serde.json.serializer.DatasetJsonSerializer
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.shouldNotBeInvoked
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
            fun `returns true if all entries have the same number of entries`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> tempDir.resolve("prettyprint.json")
                            JSON_MINIFIED -> tempDir.resolve("minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve("minified.json.zst")
                            JSON_LINES -> tempDir.resolve("dataset.jsonl")
                            JSON_LINES_ZST -> tempDir.resolve("dataset.jsonl.zst")
                        }
                    }

                    val anime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    DatasetJsonSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_PRETTY_PRINT))
                        serialize(anime, true).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED))
                        serialize(anime, true).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED_ZST), compressionLevel = 1)
                    }
                    DatasetJsonLinesSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES))
                        serialize(anime).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES_ZST), compressionLevel = 1)
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
            fun `throws exception if pretty print json file has differing number of entries`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> tempDir.resolve("prettyprint.json")
                            JSON_MINIFIED -> tempDir.resolve("minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve("minified.json.zst")
                            JSON_LINES -> tempDir.resolve("dataset.jsonl")
                            JSON_LINES_ZST -> tempDir.resolve("dataset.jsonl.zst")
                        }
                    }

                    val anime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    val differingNumberOfAnime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                    )

                    DatasetJsonSerializer.instance.apply {
                        serialize(differingNumberOfAnime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_PRETTY_PRINT))
                        serialize(anime, true).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED))
                        serialize(anime, true).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED_ZST), compressionLevel = 1)
                    }
                    DatasetJsonLinesSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES))
                        serialize(anime).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES_ZST), compressionLevel = 1)
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
                    assertThat(result).hasMessage("Number of dataset files differ: [jsonPrettyPrint=1, jsonMinified=2, jsonMinifiedZst=2, datasetJsonLines=2, datasetJsonLinesZst=2]")
                }
            }

            @Test
            fun `throws exception if json minified file has differing number of entries`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> tempDir.resolve("prettyprint.json")
                            JSON_MINIFIED -> tempDir.resolve("minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve("minified.json.zst")
                            JSON_LINES -> tempDir.resolve("dataset.jsonl")
                            JSON_LINES_ZST -> tempDir.resolve("dataset.jsonl.zst")
                        }
                    }

                    val anime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    val differingNumberOfAnime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                    )

                    DatasetJsonSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_PRETTY_PRINT))
                        serialize(differingNumberOfAnime, true).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED))
                        serialize(anime, true).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED_ZST), compressionLevel = 1)
                    }
                    DatasetJsonLinesSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES))
                        serialize(anime).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES_ZST), compressionLevel = 1)
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
                    assertThat(result).hasMessage("Number of dataset files differ: [jsonPrettyPrint=2, jsonMinified=1, jsonMinifiedZst=2, datasetJsonLines=2, datasetJsonLinesZst=2]")
                }
            }

            @Test
            fun `throws exception if minified zst file has differing number of entries`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> tempDir.resolve("prettyprint.json")
                            JSON_MINIFIED -> tempDir.resolve("minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve("minified.json.zst")
                            JSON_LINES -> tempDir.resolve("dataset.jsonl")
                            JSON_LINES_ZST -> tempDir.resolve("dataset.jsonl.zst")
                        }
                    }

                    val anime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    val differingNumberOfAnime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                    )

                    DatasetJsonSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_PRETTY_PRINT))
                        serialize(anime, true).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED))
                        serialize(differingNumberOfAnime, true).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED_ZST), compressionLevel = 1)
                    }
                    DatasetJsonLinesSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES))
                        serialize(anime).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES_ZST), compressionLevel = 1)
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
                    assertThat(result).hasMessage("Number of dataset files differ: [jsonPrettyPrint=2, jsonMinified=2, jsonMinifiedZst=1, datasetJsonLines=2, datasetJsonLinesZst=2]")
                }
            }

            @Test
            fun `throws exception if json lines file has differing number of entries`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> tempDir.resolve("prettyprint.json")
                            JSON_MINIFIED -> tempDir.resolve("minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve("minified.json.zst")
                            JSON_LINES -> tempDir.resolve("dataset.jsonl")
                            JSON_LINES_ZST -> tempDir.resolve("dataset.jsonl.zst")
                        }
                    }

                    val anime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    val differingNumberOfAnime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                    )

                    DatasetJsonSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_PRETTY_PRINT))
                        serialize(anime, true).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED))
                        serialize(anime, true).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED_ZST), compressionLevel = 1)
                    }
                    DatasetJsonLinesSerializer.instance.apply {
                        serialize(differingNumberOfAnime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES))
                        serialize(anime).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES_ZST), compressionLevel = 1)
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
                    assertThat(result).hasMessage("Number of dataset files differ: [jsonPrettyPrint=2, jsonMinified=2, jsonMinifiedZst=2, datasetJsonLines=1, datasetJsonLinesZst=2]")
                }
            }

            @Test
            fun `throws exception if json lines zst file has differing number of entries`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> tempDir.resolve("prettyprint.json")
                            JSON_MINIFIED -> tempDir.resolve("minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve("minified.json.zst")
                            JSON_LINES -> tempDir.resolve("dataset.jsonl")
                            JSON_LINES_ZST -> tempDir.resolve("dataset.jsonl.zst")
                        }
                    }

                    val anime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    val differingNumberOfAnime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                    )

                    DatasetJsonSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_PRETTY_PRINT))
                        serialize(anime, true).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED))
                        serialize(anime, true).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED_ZST), compressionLevel = 1)
                    }
                    DatasetJsonLinesSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES))
                        serialize(differingNumberOfAnime).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES_ZST), compressionLevel = 1)
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
                    assertThat(result).hasMessage("Number of dataset files differ: [jsonPrettyPrint=2, jsonMinified=2, jsonMinifiedZst=2, datasetJsonLines=2, datasetJsonLinesZst=1]")
                }
            }

            @Test
            fun `throws exception if pretty print json file doesn't exist`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> tempDir.resolve("prettyprint.json")
                            JSON_MINIFIED -> tempDir.resolve("minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve("minified.json.zst")
                            JSON_LINES -> tempDir.resolve("dataset.jsonl")
                            JSON_LINES_ZST -> tempDir.resolve("dataset.jsonl.zst")
                        }
                    }

                    val anime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    DatasetJsonSerializer.instance.apply {
                        serialize(anime, true).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED))
                        serialize(anime, true).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED_ZST), compressionLevel = 1)
                    }
                    DatasetJsonLinesSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES))
                        serialize(anime).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES_ZST), compressionLevel = 1)
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
                    assertThat(result).hasMessageEndingWith("prettyprint.json]")
                }
            }

            @Test
            fun `throws exception if json minified file doesn't exist`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> tempDir.resolve("prettyprint.json")
                            JSON_MINIFIED -> tempDir.resolve("minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve("minified.json.zst")
                            JSON_LINES -> tempDir.resolve("dataset.jsonl")
                            JSON_LINES_ZST -> tempDir.resolve("dataset.jsonl.zst")
                        }
                    }

                    val anime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    DatasetJsonSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_PRETTY_PRINT))
                        serialize(anime, true).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED_ZST), compressionLevel = 1)
                    }
                    DatasetJsonLinesSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES))
                        serialize(anime).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES_ZST), compressionLevel = 1)
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
                    assertThat(result).hasMessageEndingWith("minified.json]")
                }
            }

            @Test
            fun `throws exception if json minified zst file doesn't exist`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> tempDir.resolve("prettyprint.json")
                            JSON_MINIFIED -> tempDir.resolve("minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve("minified.json.zst")
                            JSON_LINES -> tempDir.resolve("dataset.jsonl")
                            JSON_LINES_ZST -> tempDir.resolve("dataset.jsonl.zst")
                        }
                    }

                    val anime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    DatasetJsonSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_PRETTY_PRINT))
                        serialize(anime, true).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED))
                    }
                    DatasetJsonLinesSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES))
                        serialize(anime).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES_ZST), compressionLevel = 1)
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
                    assertThat(result).hasMessageEndingWith("minified.json.zst]")
                }
            }

            @Test
            fun `throws exception if json lines file doesn't exist`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> tempDir.resolve("prettyprint.json")
                            JSON_MINIFIED -> tempDir.resolve("minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve("minified.json.zst")
                            JSON_LINES -> tempDir.resolve("dataset.jsonl")
                            JSON_LINES_ZST -> tempDir.resolve("dataset.jsonl.zst")
                        }
                    }

                    val anime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    val differingNumberOfAnime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                    )

                    DatasetJsonSerializer.instance.apply {
                        serialize(differingNumberOfAnime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_PRETTY_PRINT))
                        serialize(anime, true).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED))
                        serialize(anime, true).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED_ZST), compressionLevel = 1)
                    }
                    DatasetJsonLinesSerializer.instance.apply {
                        serialize(anime).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES_ZST), compressionLevel = 1)
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
                    assertThat(result).hasMessageEndingWith("dataset.jsonl]")
                }
            }

            @Test
            fun `throws exception if json lines zst file doesn't exist`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> tempDir.resolve("prettyprint.json")
                            JSON_MINIFIED -> tempDir.resolve("minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve("minified.json.zst")
                            JSON_LINES -> tempDir.resolve("dataset.jsonl")
                            JSON_LINES_ZST -> tempDir.resolve("dataset.jsonl.zst")
                        }
                    }

                    val anime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    val differingNumberOfAnime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                    )

                    DatasetJsonSerializer.instance.apply {
                        serialize(differingNumberOfAnime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_PRETTY_PRINT))
                        serialize(anime, true).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED))
                        serialize(anime, true).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED_ZST), compressionLevel = 1)
                    }
                    DatasetJsonLinesSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES))
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
                    assertThat(result).hasMessageEndingWith("dataset.jsonl.zst]")
                }
            }
        }

        @Nested
        inner class DeadEntriesFilesTests {

            @Test
            fun `returns true if all files have the same number of entries`() {
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
                            JSON_PRETTY_PRINT -> tempDir.resolve("prettyprint.json")
                            JSON_MINIFIED -> tempDir.resolve("minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve("minified.json.zst")
                            JSON_LINES -> tempDir.resolve("dataset.jsonl")
                            JSON_LINES_ZST -> tempDir.resolve("dataset.jsonl.zst")
                        }
                    }

                    val anime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    DatasetJsonSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_PRETTY_PRINT))
                        serialize(anime, true).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED))
                        serialize(anime, true).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED_ZST), compressionLevel = 1)
                    }
                    DatasetJsonLinesSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES))
                        serialize(anime).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES_ZST), compressionLevel = 1)
                    }

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries.json")
                            JSON_MINIFIED -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries-minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries-minified.json.zst")
                            else -> shouldNotBeInvoked()
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
                    val testMetaDataProviderConfig1MinifiedZst = createExpectedDeadEntriesMinified(
                        "10001",
                    )
                    tempDir.resolve(testMetaDataProviderConfig1.hostname()).createDirectory()
                    testMetaDataProviderConfig1Json.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, JSON_PRETTY_PRINT))
                    testMetaDataProviderConfig1Minified.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1,JSON_MINIFIED))
                    testMetaDataProviderConfig1MinifiedZst.writeToZstandardFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1,JSON_MINIFIED_ZST), compressionLevel = 1)

                    val testMetaDataProviderConfig2Json = createExpectedDeadEntriesPrettyPrint(
                        "20008",
                        "20009",
                    )
                    val testMetaDataProviderConfig2Minified = createExpectedDeadEntriesMinified(
                        "20008",
                        "20009",
                    )
                    val testMetaDataProviderConfig2MinifiedZst = createExpectedDeadEntriesMinified(
                        "20008",
                        "20009",
                    )
                    tempDir.resolve(testMetaDataProviderConfig2.hostname()).createDirectory()
                    testMetaDataProviderConfig2Json.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, JSON_PRETTY_PRINT))
                    testMetaDataProviderConfig2Minified.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, JSON_MINIFIED))
                    testMetaDataProviderConfig2MinifiedZst.writeToZstandardFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2,JSON_MINIFIED_ZST), compressionLevel = 1)

                    // when
                    val result = numberOfEntriesValidationPostProcessor.process()

                    // then
                    assertThat(result).isTrue()
                }
            }

            @Test
            fun `throws exception if prettty print json file has differing number of entries`() {
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
                            JSON_PRETTY_PRINT -> tempDir.resolve("prettyprint.json")
                            JSON_MINIFIED -> tempDir.resolve("minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve("minified.json.zst")
                            JSON_LINES -> tempDir.resolve("dataset.jsonl")
                            JSON_LINES_ZST -> tempDir.resolve("dataset.jsonl.zst")
                        }
                    }

                    val anime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    DatasetJsonSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_PRETTY_PRINT))
                        serialize(anime, true).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED))
                        serialize(anime, true).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED_ZST), compressionLevel = 1)
                    }
                    DatasetJsonLinesSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES))
                        serialize(anime).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES_ZST), compressionLevel = 1)
                    }

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries.json")
                            JSON_MINIFIED -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries-minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries-minified.json.zst")
                            else -> shouldNotBeInvoked()
                        }
                    }

                    val numberOfEntriesValidationPostProcessor = NumberOfEntriesValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = testDeadEntriesAccessor,
                    )

                    val testMetaDataProviderConfig1Json = createExpectedDeadEntriesPrettyPrint(
                        "10001",
                        "999",
                    )
                    val testMetaDataProviderConfig1Minified = createExpectedDeadEntriesMinified(
                        "10001",
                    )
                    val testMetaDataProviderConfig1MinifiedZst = createExpectedDeadEntriesMinified(
                        "10001",
                    )
                    tempDir.resolve(testMetaDataProviderConfig1.hostname()).createDirectory()
                    testMetaDataProviderConfig1Json.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, JSON_PRETTY_PRINT))
                    testMetaDataProviderConfig1Minified.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1,JSON_MINIFIED))
                    testMetaDataProviderConfig1MinifiedZst.writeToZstandardFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1,JSON_MINIFIED_ZST), compressionLevel = 1)

                    val testMetaDataProviderConfig2Json = createExpectedDeadEntriesPrettyPrint(
                        "20008",
                        "20009",
                    )
                    val testMetaDataProviderConfig2Minified = createExpectedDeadEntriesMinified(
                        "20008",
                        "20009",
                    )
                    val testMetaDataProviderConfig2MinifiedZst = createExpectedDeadEntriesMinified(
                        "20008",
                        "20009",
                    )
                    tempDir.resolve(testMetaDataProviderConfig2.hostname()).createDirectory()
                    testMetaDataProviderConfig2Json.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, JSON_PRETTY_PRINT))
                    testMetaDataProviderConfig2Minified.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, JSON_MINIFIED))
                    testMetaDataProviderConfig2MinifiedZst.writeToZstandardFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2,JSON_MINIFIED_ZST), compressionLevel = 1)

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        numberOfEntriesValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("Number of dead entries files differ for [example.org]: [jsonPrettyPrint=2, jsonMinified=1, JsonMinifiedZst=1]")
                }
            }

            @Test
            fun `throws exception if json minified file has differing number of entries`() {
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
                            JSON_PRETTY_PRINT -> tempDir.resolve("prettyprint.json")
                            JSON_MINIFIED -> tempDir.resolve("minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve("minified.json.zst")
                            JSON_LINES -> tempDir.resolve("dataset.jsonl")
                            JSON_LINES_ZST -> tempDir.resolve("dataset.jsonl.zst")
                        }
                    }

                    val anime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    DatasetJsonSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_PRETTY_PRINT))
                        serialize(anime, true).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED))
                        serialize(anime, true).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED_ZST), compressionLevel = 1)
                    }
                    DatasetJsonLinesSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES))
                        serialize(anime).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES_ZST), compressionLevel = 1)
                    }

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries.json")
                            JSON_MINIFIED -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries-minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries-minified.json.zst")
                            else -> shouldNotBeInvoked()
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
                    val testMetaDataProviderConfig1MinifiedZst = createExpectedDeadEntriesMinified(
                        "10001",
                    )
                    tempDir.resolve(testMetaDataProviderConfig1.hostname()).createDirectory()
                    testMetaDataProviderConfig1Json.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, JSON_PRETTY_PRINT))
                    testMetaDataProviderConfig1Minified.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1,JSON_MINIFIED))
                    testMetaDataProviderConfig1MinifiedZst.writeToZstandardFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1,JSON_MINIFIED_ZST), compressionLevel = 1)

                    val testMetaDataProviderConfig2Json = createExpectedDeadEntriesPrettyPrint(
                        "20008",
                        "20009",
                    )
                    val testMetaDataProviderConfig2Minified = createExpectedDeadEntriesMinified(
                        "20008",
                    )
                    val testMetaDataProviderConfig2MinifiedZst = createExpectedDeadEntriesMinified(
                        "20008",
                        "20009",
                    )
                    tempDir.resolve(testMetaDataProviderConfig2.hostname()).createDirectory()
                    testMetaDataProviderConfig2Json.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, JSON_PRETTY_PRINT))
                    testMetaDataProviderConfig2Minified.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, JSON_MINIFIED))
                    testMetaDataProviderConfig2MinifiedZst.writeToZstandardFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2,JSON_MINIFIED_ZST), compressionLevel = 1)

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        numberOfEntriesValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("Number of dead entries files differ for [other-example.me]: [jsonPrettyPrint=2, jsonMinified=1, JsonMinifiedZst=2]")
                }
            }

            @Test
            fun `throws exception if minified json zst file has differing number of entries`() {
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
                            JSON_PRETTY_PRINT -> tempDir.resolve("prettyprint.json")
                            JSON_MINIFIED -> tempDir.resolve("minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve("minified.json.zst")
                            JSON_LINES -> tempDir.resolve("dataset.jsonl")
                            JSON_LINES_ZST -> tempDir.resolve("dataset.jsonl.zst")
                        }
                    }

                    val anime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    DatasetJsonSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_PRETTY_PRINT))
                        serialize(anime, true).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED))
                        serialize(anime, true).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED_ZST), compressionLevel = 1)
                    }
                    DatasetJsonLinesSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES))
                        serialize(anime).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES_ZST), compressionLevel = 1)
                    }

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries.json")
                            JSON_MINIFIED -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries-minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries-minified.json.zst")
                            else -> shouldNotBeInvoked()
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
                    val testMetaDataProviderConfig1MinifiedZst = createExpectedDeadEntriesMinified(
                        "10001",
                        "999",
                    )
                    tempDir.resolve(testMetaDataProviderConfig1.hostname()).createDirectory()
                    testMetaDataProviderConfig1Json.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1, JSON_PRETTY_PRINT))
                    testMetaDataProviderConfig1Minified.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1,JSON_MINIFIED))
                    testMetaDataProviderConfig1MinifiedZst.writeToZstandardFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig1,JSON_MINIFIED_ZST), compressionLevel = 1)

                    val testMetaDataProviderConfig2Json = createExpectedDeadEntriesPrettyPrint(
                        "20008",
                        "20009",
                    )
                    val testMetaDataProviderConfig2Minified = createExpectedDeadEntriesMinified(
                        "20008",
                        "20009",
                    )
                    val testMetaDataProviderConfig2MinifiedZst = createExpectedDeadEntriesMinified(
                        "20008",
                        "20009",
                    )
                    tempDir.resolve(testMetaDataProviderConfig2.hostname()).createDirectory()
                    testMetaDataProviderConfig2Json.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, JSON_PRETTY_PRINT))
                    testMetaDataProviderConfig2Minified.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2, JSON_MINIFIED))
                    testMetaDataProviderConfig2MinifiedZst.writeToZstandardFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig2,JSON_MINIFIED_ZST), compressionLevel = 1)

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        numberOfEntriesValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("Number of dead entries files differ for [example.org]: [jsonPrettyPrint=1, jsonMinified=1, JsonMinifiedZst=2]")
                }
            }

            @Test
            fun `throws exception if pretty print json file doesn't exist`() {
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
                            JSON_PRETTY_PRINT -> tempDir.resolve("prettyprint.json")
                            JSON_MINIFIED -> tempDir.resolve("minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve("minified.json.zst")
                            JSON_LINES -> tempDir.resolve("dataset.jsonl")
                            JSON_LINES_ZST -> tempDir.resolve("dataset.jsonl.zst")
                        }
                    }

                    val anime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    DatasetJsonSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_PRETTY_PRINT))
                        serialize(anime, true).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED))
                        serialize(anime, true).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED_ZST), compressionLevel = 1)
                    }
                    DatasetJsonLinesSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES))
                        serialize(anime).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES_ZST), compressionLevel = 1)
                    }

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("non-existent.json")
                            JSON_MINIFIED -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries-minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries-minified.json.zst")
                            else -> shouldNotBeInvoked()
                        }
                    }

                    val testMetaDataProviderConfig1Minified = createExpectedDeadEntriesMinified(
                        "10001",
                    )
                    val testMetaDataProviderConfig1MinifiedZst = createExpectedDeadEntriesMinified(
                        "10001",
                    )
                    tempDir.resolve(testMetaDataProviderConfig.hostname()).createDirectory()
                    testMetaDataProviderConfig1Minified.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig,JSON_MINIFIED))
                    testMetaDataProviderConfig1MinifiedZst.writeToZstandardFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig,JSON_MINIFIED_ZST), compressionLevel = 1)

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
            fun `throws exception if json minified file doesn't exist`() {
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
                            JSON_PRETTY_PRINT -> tempDir.resolve("prettyprint.json")
                            JSON_MINIFIED -> tempDir.resolve("minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve("minified.json.zst")
                            JSON_LINES -> tempDir.resolve("dataset.jsonl")
                            JSON_LINES_ZST -> tempDir.resolve("dataset.jsonl.zst")
                        }
                    }

                    val anime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    DatasetJsonSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_PRETTY_PRINT))
                        serialize(anime, true).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED))
                        serialize(anime, true).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED_ZST), compressionLevel = 1)
                    }
                    DatasetJsonLinesSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES))
                        serialize(anime).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES_ZST), compressionLevel = 1)
                    }

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries.json")
                            JSON_MINIFIED -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("non-existent.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries-minified.json.zst")
                            else -> shouldNotBeInvoked()
                        }
                    }

                    val testMetaDataProviderConfig1PrettyPrint = createExpectedDeadEntriesPrettyPrint(
                        "10001",
                    )
                    val testMetaDataProviderConfig1MinifiedZst = createExpectedDeadEntriesMinified(
                        "10001",
                    )
                    tempDir.resolve(testMetaDataProviderConfig.hostname()).createDirectory()
                    testMetaDataProviderConfig1PrettyPrint.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig,JSON_PRETTY_PRINT))
                    testMetaDataProviderConfig1MinifiedZst.writeToZstandardFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig,JSON_MINIFIED_ZST), compressionLevel = 1)

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
            fun `throws exception if json minified zst file doesn't exist`() {
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
                            JSON_PRETTY_PRINT -> tempDir.resolve("prettyprint.json")
                            JSON_MINIFIED -> tempDir.resolve("minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve("minified.json.zst")
                            JSON_LINES -> tempDir.resolve("dataset.jsonl")
                            JSON_LINES_ZST -> tempDir.resolve("dataset.jsonl.zst")
                        }
                    }

                    val anime = setOf(
                        TestAnimeObjects.DefaultAnime.obj,
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    DatasetJsonSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_PRETTY_PRINT))
                        serialize(anime, true).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED))
                        serialize(anime, true).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_MINIFIED_ZST), compressionLevel = 1)
                    }
                    DatasetJsonLinesSerializer.instance.apply {
                        serialize(anime).writeToFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES))
                        serialize(anime).writeToZstandardFile(testDatasetFileAccessor.offlineDatabaseFile(JSON_LINES_ZST), compressionLevel = 1)
                    }

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries.json")
                            JSON_MINIFIED -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("dead-entries-minified.json")
                            JSON_MINIFIED_ZST -> tempDir.resolve(metaDataProviderConfig.hostname()).resolve("non-existent.json.zst")
                            else -> shouldNotBeInvoked()
                        }
                    }

                    val testMetaDataProviderConfig1PrettyPrint = createExpectedDeadEntriesPrettyPrint(
                        "10001",
                    )
                    val testMetaDataProviderConfig1Minified = createExpectedDeadEntriesMinified(
                        "10001",
                    )
                    tempDir.resolve(testMetaDataProviderConfig.hostname()).createDirectory()
                    testMetaDataProviderConfig1PrettyPrint.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig,JSON_PRETTY_PRINT))
                    testMetaDataProviderConfig1Minified.writeToFile(testDeadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig,JSON_MINIFIED))

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
                    assertThat(result).hasMessageEndingWith("non-existent.json.zst]")
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