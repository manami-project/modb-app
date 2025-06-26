package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestDatasetFileAccessor
import io.github.manamiproject.modb.app.TestDeadEntriesAccessor
import io.github.manamiproject.modb.app.TestMetaDataProviderConfig
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
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.io.path.createFile
import kotlin.io.path.fileSize
import kotlin.test.Test

internal class FileSizePlausibilityValidationPostProcessorTest {

    @Nested
    inner class ProcessTests {

        @Nested
        inner class DatasetFilesTests {

            @Test
            fun `throws exception if JSON_MINIFIED file size is greater than JSON_PRETTY_PRINT file size`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val jsonPrettyPrint = tempDir.resolve("json.txt").createFile()
                    "text\n".repeat(100).writeToFile(jsonPrettyPrint)

                    val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                    "text\n".repeat(1000).writeToFile(jsonMinified)

                    val jsonMinifiedZst = tempDir.resolve("jsonMinified.zst").createFile()
                    "text\n".repeat(1000).writeToZstandardFile(jsonMinifiedZst, compressionLevel = 1)

                    val jsonLines = tempDir.resolve("jsonLines.txt").createFile()
                    "text\n".repeat(1000).writeToFile(jsonLines)

                    val jsonLinesZst = tempDir.resolve("jsonLines.zst").createFile()
                    "text\n".repeat(1000).writeToZstandardFile(jsonLinesZst, compressionLevel = 1)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> jsonPrettyPrint
                            JSON_MINIFIED -> jsonMinified
                            JSON_MINIFIED_ZST -> jsonMinifiedZst
                            JSON_LINES -> jsonLines
                            JSON_LINES_ZST -> jsonLinesZst
                        }
                    }

                    val fileSizePlausibilityValidationPostProcessor = FileSizePlausibilityValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = TestDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        fileSizePlausibilityValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("File sizes for dataset are not plausible: [jsonPrettyPrint=${jsonPrettyPrint.fileSize()}, jsonMinified=${jsonMinified.fileSize()}, jsonMinifiedZst=${jsonMinifiedZst.fileSize()}]")
                }
            }

            @Test
            fun `throws exception if JSON_MINIFIED_ZST file size is greater than JSON_MINIFIED file size`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val jsonPrettyPrint = tempDir.resolve("json.txt").createFile()
                    "text\n".repeat(1000).writeToFile(jsonPrettyPrint)

                    val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                    "text\n".repeat(1).writeToFile(jsonMinified)

                    val jsonMinifiedZst = tempDir.resolve("jsonMinified.zst").createFile()
                    "text\n".repeat(1000).writeToZstandardFile(jsonMinifiedZst, compressionLevel = 1)

                    val jsonLines = tempDir.resolve("jsonLines.txt").createFile()
                    "text\n".repeat(1000).writeToFile(jsonLines)

                    val jsonLinesZst = tempDir.resolve("jsonLines.zst").createFile()
                    "text\n".repeat(1000).writeToZstandardFile(jsonLinesZst, compressionLevel = 1)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> jsonPrettyPrint
                            JSON_MINIFIED -> jsonMinified
                            JSON_MINIFIED_ZST -> jsonMinifiedZst
                            JSON_LINES -> jsonLines
                            JSON_LINES_ZST -> jsonLinesZst
                        }
                    }

                    val fileSizePlausibilityValidationPostProcessor = FileSizePlausibilityValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = TestDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        fileSizePlausibilityValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("File sizes for dataset are not plausible: [jsonPrettyPrint=${jsonPrettyPrint.fileSize()}, jsonMinified=${jsonMinified.fileSize()}, jsonMinifiedZst=${jsonMinifiedZst.fileSize()}]")
                }
            }

            @Test
            fun `throws exception if JSON_LINES_ZST file size is greater than JSON_LINES file size`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val jsonPrettyPrint = tempDir.resolve("json.txt").createFile()
                    "text\n".repeat(1000).writeToFile(jsonPrettyPrint)

                    val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                    "text\n".repeat(500).writeToFile(jsonMinified)

                    val jsonMinifiedZst = tempDir.resolve("jsonMinified.zst").createFile()
                    "text\n".repeat(500).writeToZstandardFile(jsonMinifiedZst, compressionLevel = 1)

                    val jsonLines = tempDir.resolve("jsonLines.txt").createFile()
                    "text\n".repeat(1).writeToFile(jsonLines)

                    val jsonLinesZst = tempDir.resolve("jsonLines.zst").createFile()
                    "text\n".repeat(10000).writeToZstandardFile(jsonLinesZst, compressionLevel = 1)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> jsonPrettyPrint
                            JSON_MINIFIED -> jsonMinified
                            JSON_MINIFIED_ZST -> jsonMinifiedZst
                            JSON_LINES -> jsonLines
                            JSON_LINES_ZST -> jsonLinesZst
                        }
                    }

                    val fileSizePlausibilityValidationPostProcessor = FileSizePlausibilityValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = TestDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        fileSizePlausibilityValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("File sizes for dataset are not plausible: [jsonLinesZst=${jsonLinesZst.fileSize()}, jsonLines=${jsonLines.fileSize()}]")
                }
            }

            @Test
            fun `throws exception if JSON_MINIFIED file size is greater than JSON_LINES file size`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val jsonPrettyPrint = tempDir.resolve("json.txt").createFile()
                    "text\n".repeat(1500).writeToFile(jsonPrettyPrint)

                    val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                    "text\n".repeat(1000).writeToFile(jsonMinified)

                    val jsonMinifiedZst = tempDir.resolve("jsonMinified.zst").createFile()
                    "text\n".repeat(1000).writeToZstandardFile(jsonMinifiedZst, compressionLevel = 1)

                    val jsonLines = tempDir.resolve("jsonLines.txt").createFile()
                    "text\n".repeat(500).writeToFile(jsonLines)

                    val jsonLinesZst = tempDir.resolve("jsonLines.zst").createFile()
                    "text\n".repeat(500).writeToZstandardFile(jsonLinesZst, compressionLevel = 1)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> jsonPrettyPrint
                            JSON_MINIFIED -> jsonMinified
                            JSON_MINIFIED_ZST -> jsonMinifiedZst
                            JSON_LINES -> jsonLines
                            JSON_LINES_ZST -> jsonLinesZst
                        }
                    }

                    val fileSizePlausibilityValidationPostProcessor = FileSizePlausibilityValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = TestDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        fileSizePlausibilityValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("File sizes for dataset are not plausible: [jsonLines=${jsonLines.fileSize()}, jsonMinified=${jsonMinified.fileSize()}]")
                }
            }

            @Test
            fun `throws exception if JSON_PRETTY_PRINT file doesn't exist`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                    "text\n".repeat(1000).writeToFile(jsonMinified)

                    val jsonMinifiedZst = tempDir.resolve("jsonMinified.zst").createFile()
                    "text\n".repeat(1000).writeToZstandardFile(jsonMinifiedZst, compressionLevel = 1)

                    val jsonLines = tempDir.resolve("jsonLines.txt").createFile()
                    "text\n".repeat(500).writeToFile(jsonLines)

                    val jsonLinesZst = tempDir.resolve("jsonLines.zst").createFile()
                    "text\n".repeat(500).writeToZstandardFile(jsonLinesZst, compressionLevel = 1)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> tempDir.resolve("json.txt")
                            JSON_MINIFIED -> jsonMinified
                            JSON_MINIFIED_ZST -> jsonMinifiedZst
                            JSON_LINES -> jsonLines
                            JSON_LINES_ZST -> jsonLinesZst
                        }
                    }

                    val fileSizePlausibilityValidationPostProcessor = FileSizePlausibilityValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = TestDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        fileSizePlausibilityValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("Dataset *.json file doesn't exist.")
                }
            }

            @Test
            fun `throws exception if JSON_MINIFIED file doesn't exist`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val jsonPrettyPrint = tempDir.resolve("json.txt").createFile()
                    "text\n".repeat(1000).writeToFile(jsonPrettyPrint)

                    val jsonMinifiedZst = tempDir.resolve("jsonMinified.zst").createFile()
                    "text\n".repeat(1000).writeToZstandardFile(jsonMinifiedZst, compressionLevel = 1)

                    val jsonLines = tempDir.resolve("jsonLines.txt").createFile()
                    "text\n".repeat(500).writeToFile(jsonLines)

                    val jsonLinesZst = tempDir.resolve("jsonLines.zst").createFile()
                    "text\n".repeat(500).writeToZstandardFile(jsonLinesZst, compressionLevel = 1)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> jsonPrettyPrint
                            JSON_MINIFIED -> tempDir.resolve("jsonMinified.txt")
                            JSON_MINIFIED_ZST -> jsonMinifiedZst
                            JSON_LINES -> jsonLines
                            JSON_LINES_ZST -> jsonLinesZst
                        }
                    }

                    val fileSizePlausibilityValidationPostProcessor = FileSizePlausibilityValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = TestDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        fileSizePlausibilityValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("Dataset *-minified.json file doesn't exist.")
                }
            }

            @Test
            fun `throws exception if JSON_MINIFIED_ZST file doesn't exist`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val jsonPrettyPrint = tempDir.resolve("json.txt").createFile()
                    "text\n".repeat(1000).writeToFile(jsonPrettyPrint)

                    val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                    "text\n".repeat(1000).writeToFile(jsonMinified)

                    val jsonLines = tempDir.resolve("jsonLines.txt").createFile()
                    "text\n".repeat(500).writeToFile(jsonLines)

                    val jsonLinesZst = tempDir.resolve("jsonLines.zst").createFile()
                    "text\n".repeat(500).writeToZstandardFile(jsonLinesZst, compressionLevel = 1)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> jsonPrettyPrint
                            JSON_MINIFIED -> jsonMinified
                            JSON_MINIFIED_ZST -> tempDir.resolve("jsonMinified.zst")
                            JSON_LINES -> jsonLines
                            JSON_LINES_ZST -> jsonLinesZst
                        }
                    }

                    val fileSizePlausibilityValidationPostProcessor = FileSizePlausibilityValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = TestDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        fileSizePlausibilityValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("Dataset *-minified.json.zst file doesn't exist.")
                }
            }

            @Test
            fun `throws exception if JSON_LINES file doesn't exist`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val jsonPrettyPrint = tempDir.resolve("json.txt").createFile()
                    "text\n".repeat(1000).writeToFile(jsonPrettyPrint)

                    val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                    "text\n".repeat(500).writeToFile(jsonMinified)

                    val jsonMinifiedZst = tempDir.resolve("jsonMinified.zst").createFile()
                    "text\n".repeat(500).writeToZstandardFile(jsonMinifiedZst, compressionLevel = 1)

                    val jsonLinesZst = tempDir.resolve("jsonLines.zst").createFile()
                    "text\n".repeat(1000).writeToZstandardFile(jsonLinesZst, compressionLevel = 1)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> jsonPrettyPrint
                            JSON_MINIFIED -> jsonMinified
                            JSON_MINIFIED_ZST -> jsonMinifiedZst
                            JSON_LINES -> tempDir.resolve("jsonLines.txt")
                            JSON_LINES_ZST -> jsonLinesZst
                        }
                    }

                    val fileSizePlausibilityValidationPostProcessor = FileSizePlausibilityValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = TestDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        fileSizePlausibilityValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("Dataset *.jsonl file doesn't exist.")
                }
            }

            @Test
            fun `throws exception if JSON_LINES_ZST file doesn't exist`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val jsonPrettyPrint = tempDir.resolve("json.txt").createFile()
                    "text\n".repeat(1000).writeToFile(jsonPrettyPrint)

                    val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                    "text\n".repeat(500).writeToFile(jsonMinified)

                    val jsonMinifiedZst = tempDir.resolve("jsonMinified.zst").createFile()
                    "text\n".repeat(500).writeToZstandardFile(jsonMinifiedZst, compressionLevel = 1)

                    val jsonLines = tempDir.resolve("jsonLines.txt").createFile()
                    "text\n".repeat(1000).writeToFile(jsonLines)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> jsonPrettyPrint
                            JSON_MINIFIED -> jsonMinified
                            JSON_MINIFIED_ZST -> jsonMinifiedZst
                            JSON_LINES -> jsonLines
                            JSON_LINES_ZST -> tempDir.resolve("jsonLines.zst")
                        }
                    }

                    val fileSizePlausibilityValidationPostProcessor = FileSizePlausibilityValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = TestDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        fileSizePlausibilityValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("Dataset *.jsonl.zst file doesn't exist.")
                }
            }

            @Test
            fun `returns true if dataset file sizes are valid`() {
                tempDirectory {
                    // given
                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                    }

                    val jsonPrettyPrint = tempDir.resolve("json.txt").createFile()
                    "text\n".repeat(1000).writeToFile(jsonPrettyPrint)

                    val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                    "text\n".repeat(500).writeToFile(jsonMinified)

                    val jsonMinifiedZst = tempDir.resolve("jsonMinified.zst").createFile()
                    "text\n".repeat(500).writeToZstandardFile(jsonMinifiedZst, compressionLevel = 1)

                    val jsonLines = tempDir.resolve("jsonLines.txt").createFile()
                    "text\n".repeat(499).writeToFile(jsonLines)

                    val jsonLinesZst = tempDir.resolve("jsonLines.zst").createFile()
                    "text\n".repeat(499).writeToZstandardFile(jsonLinesZst, compressionLevel = 1)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> jsonPrettyPrint
                            JSON_MINIFIED -> jsonMinified
                            JSON_MINIFIED_ZST -> jsonMinifiedZst
                            JSON_LINES -> jsonLines
                            JSON_LINES_ZST -> jsonLinesZst
                        }
                    }

                    val fileSizePlausibilityValidationPostProcessor = FileSizePlausibilityValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = TestDeadEntriesAccessor,
                    )

                    // when
                    val result = fileSizePlausibilityValidationPostProcessor.process()

                    // then
                    assertThat(result).isTrue()
                }
            }
        }

        @Nested
        inner class DeadEntriesFilesTests {

            @Test
            fun `throws exception if JSON_MINIFIED file size is greater than JSON_PRETTY_PRINT file size`() {
                tempDirectory {
                    // given
                    val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                    }

                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                        override fun deadEntriesSupported(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                    }

                    val jsonPrettyPrint = tempDir.resolve("json.txt").createFile()
                    "text\n".repeat(1000).writeToFile(jsonPrettyPrint)

                    val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                    "text\n".repeat(500).writeToFile(jsonMinified)

                    val jsonMinifiedZst = tempDir.resolve("jsonMinified.zst").createFile()
                    "text\n".repeat(500).writeToZstandardFile(jsonMinifiedZst, compressionLevel = 1)

                    val jsonLines = tempDir.resolve("jsonLines.txt").createFile()
                    "text\n".repeat(499).writeToFile(jsonLines)

                    val jsonLinesZst = tempDir.resolve("jsonLines.zst").createFile()
                    "text\n".repeat(499).writeToZstandardFile(jsonLinesZst, compressionLevel = 1)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> jsonPrettyPrint
                            JSON_MINIFIED -> jsonMinified
                            JSON_MINIFIED_ZST -> jsonMinifiedZst
                            JSON_LINES -> jsonLines
                            JSON_LINES_ZST -> jsonLinesZst
                        }
                    }

                    val deadEntriesJsonPrettyPrint = tempDir.resolve("dead-entries.txt").createFile()
                    "text\n".repeat(100).writeToFile(deadEntriesJsonPrettyPrint)

                    val deadEntriesJsonMinified = tempDir.resolve("dead-entries-minified.txt").createFile()
                    "text\n".repeat(1000).writeToFile(deadEntriesJsonMinified)

                    val deadEntriesJsonMinifiedZst = tempDir.resolve("dead-entries.zst").createFile()
                    "text\n".repeat(1).writeToZstandardFile(deadEntriesJsonMinifiedZst, compressionLevel = 1)

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> deadEntriesJsonPrettyPrint
                            JSON_MINIFIED -> deadEntriesJsonMinified
                            JSON_MINIFIED_ZST -> deadEntriesJsonMinifiedZst
                            else -> shouldNotBeInvoked()
                        }
                    }

                    val fileSizePlausibilityValidationPostProcessor = FileSizePlausibilityValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = testDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        fileSizePlausibilityValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("File sizes for dead entry files of [example.org] are not plausible: [json=${deadEntriesJsonPrettyPrint.fileSize()}, jsonMinified=${deadEntriesJsonMinified.fileSize()}, jsonMinifiedZst=${deadEntriesJsonMinifiedZst.fileSize()}]")
                }
            }

            @Test
            fun `throws exception if JSON_MINIFIED_ZST file size is greater than JSON_MINIFIED file size`() {
                tempDirectory {
                    // given
                    val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                    }

                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                        override fun deadEntriesSupported(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                    }

                    val jsonPrettyPrint = tempDir.resolve("json.txt").createFile()
                    "text\n".repeat(1000).writeToFile(jsonPrettyPrint)

                    val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                    "text\n".repeat(500).writeToFile(jsonMinified)

                    val jsonMinifiedZst = tempDir.resolve("jsonMinified.zst").createFile()
                    "text\n".repeat(500).writeToZstandardFile(jsonMinifiedZst, compressionLevel = 1)

                    val jsonLines = tempDir.resolve("jsonLines.txt").createFile()
                    "text\n".repeat(499).writeToFile(jsonLines)

                    val jsonLinesZst = tempDir.resolve("jsonLines.zst").createFile()
                    "text\n".repeat(499).writeToZstandardFile(jsonLinesZst, compressionLevel = 1)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> jsonPrettyPrint
                            JSON_MINIFIED -> jsonMinified
                            JSON_MINIFIED_ZST -> jsonMinifiedZst
                            JSON_LINES -> jsonLines
                            JSON_LINES_ZST -> jsonLinesZst
                        }
                    }

                    val deadEntriesJsonPrettyPrint = tempDir.resolve("dead-entries.txt").createFile()
                    "text\n".repeat(100).writeToFile(deadEntriesJsonPrettyPrint)

                    val deadEntriesJsonMinified = tempDir.resolve("dead-entries-minified.txt").createFile()
                    "text\n".repeat(1).writeToFile(deadEntriesJsonMinified)

                    val deadEntriesJsonMinifiedZst = tempDir.resolve("dead-entries.zst").createFile()
                    "text\n".repeat(10000).writeToZstandardFile(deadEntriesJsonMinifiedZst, compressionLevel = 1)

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> deadEntriesJsonPrettyPrint
                            JSON_MINIFIED -> deadEntriesJsonMinified
                            JSON_MINIFIED_ZST -> deadEntriesJsonMinifiedZst
                            else -> shouldNotBeInvoked()
                        }
                    }

                    val fileSizePlausibilityValidationPostProcessor = FileSizePlausibilityValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = testDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        fileSizePlausibilityValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("File sizes for dead entry files of [example.org] are not plausible: [json=${deadEntriesJsonPrettyPrint.fileSize()}, jsonMinified=${deadEntriesJsonMinified.fileSize()}, jsonMinifiedZst=${deadEntriesJsonMinifiedZst.fileSize()}]")
                }
            }

            @Test
            fun `returns true if dead entry file sizes are valid`() {
                tempDirectory {
                    // given
                    val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                    }

                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                        override fun deadEntriesSupported(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                    }

                    val jsonPrettyPrint = tempDir.resolve("json.txt").createFile()
                    "text\n".repeat(1000).writeToFile(jsonPrettyPrint)

                    val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                    "text\n".repeat(500).writeToFile(jsonMinified)

                    val jsonMinifiedZst = tempDir.resolve("jsonMinified.zst").createFile()
                    "text\n".repeat(500).writeToZstandardFile(jsonMinifiedZst, compressionLevel = 1)

                    val jsonLines = tempDir.resolve("jsonLines.txt").createFile()
                    "text\n".repeat(499).writeToFile(jsonLines)

                    val jsonLinesZst = tempDir.resolve("jsonLines.zst").createFile()
                    "text\n".repeat(499).writeToZstandardFile(jsonLinesZst, compressionLevel = 1)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> jsonPrettyPrint
                            JSON_MINIFIED -> jsonMinified
                            JSON_MINIFIED_ZST -> jsonMinifiedZst
                            JSON_LINES -> jsonLines
                            JSON_LINES_ZST -> jsonLinesZst
                        }
                    }

                    val deadEntriesJsonPrettyPrint = tempDir.resolve("dead-entries.txt").createFile()
                    "text\n".repeat(1000).writeToFile(deadEntriesJsonPrettyPrint)

                    val deadEntriesJsonMinified = tempDir.resolve("dead-entries-minified.txt").createFile()
                    "text\n".repeat(500).writeToFile(deadEntriesJsonMinified)

                    val deadEntriesJsonMinifiedZst = tempDir.resolve("dead-entries.zst").createFile()
                    "text\n".repeat(500).writeToZstandardFile(deadEntriesJsonMinifiedZst, compressionLevel = 1)

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> deadEntriesJsonPrettyPrint
                            JSON_MINIFIED -> deadEntriesJsonMinified
                            JSON_MINIFIED_ZST -> deadEntriesJsonMinifiedZst
                            else -> shouldNotBeInvoked()
                        }
                    }

                    val fileSizePlausibilityValidationPostProcessor = FileSizePlausibilityValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = testDeadEntriesAccessor,
                    )

                    // when
                    val result = fileSizePlausibilityValidationPostProcessor.process()

                    // then
                    assertThat(result).isTrue()
                }
            }

            @Test
            fun `ignores meta data providers which don't support dead entries`() {
                tempDirectory {
                    // given
                    val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                    }

                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                        override fun deadEntriesSupported(metaDataProviderConfig: MetaDataProviderConfig): Boolean = false
                    }

                    val jsonPrettyPrint = tempDir.resolve("json.txt").createFile()
                    "text\n".repeat(1000).writeToFile(jsonPrettyPrint)

                    val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                    "text\n".repeat(500).writeToFile(jsonMinified)

                    val jsonMinifiedZst = tempDir.resolve("jsonMinified.zst").createFile()
                    "text\n".repeat(500).writeToZstandardFile(jsonMinifiedZst, compressionLevel = 1)

                    val jsonLines = tempDir.resolve("jsonLines.txt").createFile()
                    "text\n".repeat(499).writeToFile(jsonLines)

                    val jsonLinesZst = tempDir.resolve("jsonLines.zst").createFile()
                    "text\n".repeat(499).writeToZstandardFile(jsonLinesZst, compressionLevel = 1)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> jsonPrettyPrint
                            JSON_MINIFIED -> jsonMinified
                            JSON_MINIFIED_ZST -> jsonMinifiedZst
                            JSON_LINES -> jsonLines
                            JSON_LINES_ZST -> jsonLinesZst
                        }
                    }

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> shouldNotBeInvoked()
                            JSON_MINIFIED -> shouldNotBeInvoked()
                            JSON_MINIFIED_ZST -> shouldNotBeInvoked()
                            else -> shouldNotBeInvoked()
                        }
                    }

                    val fileSizePlausibilityValidationPostProcessor = FileSizePlausibilityValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = testDeadEntriesAccessor,
                    )

                    // when
                    val result = fileSizePlausibilityValidationPostProcessor.process()

                    // then
                    assertThat(result).isTrue()
                }
            }

            @Test
            fun `throws exception if JSON_PRETTY_PRINT file doesn't exist`() {
                tempDirectory {
                    // given
                    val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                    }

                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                        override fun deadEntriesSupported(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                    }

                    val jsonPrettyPrint = tempDir.resolve("json.txt").createFile()
                    "text\n".repeat(1000).writeToFile(jsonPrettyPrint)

                    val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                    "text\n".repeat(500).writeToFile(jsonMinified)

                    val jsonMinifiedZst = tempDir.resolve("jsonMinified.zst").createFile()
                    "text\n".repeat(500).writeToZstandardFile(jsonMinifiedZst, compressionLevel = 1)

                    val jsonLines = tempDir.resolve("jsonLines.txt").createFile()
                    "text\n".repeat(499).writeToFile(jsonLines)

                    val jsonLinesZst = tempDir.resolve("jsonLines.zst").createFile()
                    "text\n".repeat(499).writeToZstandardFile(jsonLinesZst, compressionLevel = 1)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> jsonPrettyPrint
                            JSON_MINIFIED -> jsonMinified
                            JSON_MINIFIED_ZST -> jsonMinifiedZst
                            JSON_LINES -> jsonLines
                            JSON_LINES_ZST -> jsonLinesZst
                        }
                    }

                    val deadEntriesJsonMinified = tempDir.resolve("dead-entries-minified.txt").createFile()
                    "text\n".repeat(500).writeToFile(deadEntriesJsonMinified)

                    val deadEntriesJsonMinifiedZst = tempDir.resolve("dead-entries.zst").createFile()
                    "text\n".repeat(500).writeToZstandardFile(deadEntriesJsonMinifiedZst, compressionLevel = 1)

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> tempDir.resolve("dead-entries.txt")
                            JSON_MINIFIED -> deadEntriesJsonMinified
                            JSON_MINIFIED_ZST -> deadEntriesJsonMinifiedZst
                            else -> shouldNotBeInvoked()
                        }
                    }

                    val fileSizePlausibilityValidationPostProcessor = FileSizePlausibilityValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = testDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        fileSizePlausibilityValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("Dead entries *.json file for [example.org] doesn't exist.")
                }
            }

            @Test
            fun `throws exception if JSON_MINIFIED file doesn't exist`() {
                tempDirectory {
                    // given
                    val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                    }

                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                        override fun deadEntriesSupported(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                    }

                    val jsonPrettyPrint = tempDir.resolve("json.txt").createFile()
                    "text\n".repeat(1000).writeToFile(jsonPrettyPrint)

                    val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                    "text\n".repeat(500).writeToFile(jsonMinified)

                    val jsonMinifiedZst = tempDir.resolve("jsonMinified.zst").createFile()
                    "text\n".repeat(500).writeToZstandardFile(jsonMinifiedZst, compressionLevel = 1)

                    val jsonLines = tempDir.resolve("jsonLines.txt").createFile()
                    "text\n".repeat(499).writeToFile(jsonLines)

                    val jsonLinesZst = tempDir.resolve("jsonLines.zst").createFile()
                    "text\n".repeat(499).writeToZstandardFile(jsonLinesZst, compressionLevel = 1)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> jsonPrettyPrint
                            JSON_MINIFIED -> jsonMinified
                            JSON_MINIFIED_ZST -> jsonMinifiedZst
                            JSON_LINES -> jsonLines
                            JSON_LINES_ZST -> jsonLinesZst
                        }
                    }

                    val deadEntriesJsonPrettyPrint = tempDir.resolve("dead-entries.txt").createFile()
                    "text\n".repeat(1000).writeToFile(deadEntriesJsonPrettyPrint)

                    val deadEntriesJsonMinifiedZst = tempDir.resolve("dead-entries.zst").createFile()
                    "text\n".repeat(500).writeToZstandardFile(deadEntriesJsonMinifiedZst, compressionLevel = 1)

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> deadEntriesJsonPrettyPrint
                            JSON_MINIFIED -> tempDir.resolve("dead-entries-minified.txt")
                            JSON_MINIFIED_ZST -> deadEntriesJsonMinifiedZst
                            else -> shouldNotBeInvoked()
                        }
                    }

                    val fileSizePlausibilityValidationPostProcessor = FileSizePlausibilityValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = testDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        fileSizePlausibilityValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("Dead entries *-minified.json file for [example.org] doesn't exist.")
                }
            }

            @Test
            fun `throws exception if JSON_MINIFIED_ZST file doesn't exist`() {
                tempDirectory {
                    // given
                    val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                    }

                    val testAppConfig = object: Config by TestAppConfig {
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                        override fun deadEntriesSupported(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                    }

                    val jsonPrettyPrint = tempDir.resolve("json.txt").createFile()
                    "text\n".repeat(1000).writeToFile(jsonPrettyPrint)

                    val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                    "text\n".repeat(500).writeToFile(jsonMinified)

                    val jsonMinifiedZst = tempDir.resolve("jsonMinified.zst").createFile()
                    "text\n".repeat(500).writeToZstandardFile(jsonMinifiedZst, compressionLevel = 1)

                    val jsonLines = tempDir.resolve("jsonLines.txt").createFile()
                    "text\n".repeat(499).writeToFile(jsonLines)

                    val jsonLinesZst = tempDir.resolve("jsonLines.zst").createFile()
                    "text\n".repeat(499).writeToZstandardFile(jsonLinesZst, compressionLevel = 1)

                    val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                        override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> jsonPrettyPrint
                            JSON_MINIFIED -> jsonMinified
                            JSON_MINIFIED_ZST -> jsonMinifiedZst
                            JSON_LINES -> jsonLines
                            JSON_LINES_ZST -> jsonLinesZst
                        }
                    }

                    val deadEntriesJsonPrettyPrint = tempDir.resolve("dead-entries.txt").createFile()
                    "text\n".repeat(1000).writeToFile(deadEntriesJsonPrettyPrint)

                    val deadEntriesJsonMinified = tempDir.resolve("dead-entries-minified.txt").createFile()
                    "text\n".repeat(500).writeToFile(deadEntriesJsonMinified)

                    val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                        override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                            JSON_PRETTY_PRINT -> deadEntriesJsonPrettyPrint
                            JSON_MINIFIED -> deadEntriesJsonMinified
                            JSON_MINIFIED_ZST -> tempDir.resolve("dead-entries.zst")
                            else -> shouldNotBeInvoked()
                        }
                    }

                    val fileSizePlausibilityValidationPostProcessor = FileSizePlausibilityValidationPostProcessor(
                        appConfig = testAppConfig,
                        datasetFileAccessor = testDatasetFileAccessor,
                        deadEntriesAccessor = testDeadEntriesAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        fileSizePlausibilityValidationPostProcessor.process()
                    }

                    // then
                    assertThat(result).hasMessage("Dead entries *-minified.json.zst file for [example.org] doesn't exist.")
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
                val previous = FileSizePlausibilityValidationPostProcessor.instance

                // when
                val result = FileSizePlausibilityValidationPostProcessor.instance

                // then
                assertThat(result).isExactlyInstanceOf(FileSizePlausibilityValidationPostProcessor::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}