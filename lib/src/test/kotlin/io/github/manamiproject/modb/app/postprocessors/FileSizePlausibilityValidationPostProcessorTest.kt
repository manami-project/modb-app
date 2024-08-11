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
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.io.path.createFile
import kotlin.io.path.fileSize
import kotlin.test.Test

internal class FileSizePlausibilityValidationPostProcessorTest {

    @Nested
    inner class ProcessTests {

        @Test
        fun `throws exception if minified json file size is greater than json file size`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                }

                val json = tempDir.resolve("json.txt").createFile()
                "text\n".repeat(100).writeToFile(json)

                val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                "text\n".repeat(1000).writeToFile(jsonMinified)

                val zip = tempDir.resolve("zip.txt").createFile()
                "text\n".repeat(1).writeToFile(zip)

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                        DatasetFileType.JSON -> json
                        DatasetFileType.JSON_MINIFIED -> jsonMinified
                        DatasetFileType.ZIP -> zip
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
                assertThat(result).hasMessage("File sizes for dataset are not plausible: [json=${json.fileSize()}, jsonMinified=${jsonMinified.fileSize()}, zip=${zip.fileSize()}]")
            }
        }

        @Test
        fun `throws exception if zip json file size is greater than minified json file size`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                }

                val json = tempDir.resolve("json.txt").createFile()
                "text\n".repeat(1000).writeToFile(json)

                val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                "text\n".repeat(1).writeToFile(jsonMinified)

                val zip = tempDir.resolve("zip.txt").createFile()
                "text\n".repeat(100).writeToFile(zip)

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                        DatasetFileType.JSON -> json
                        DatasetFileType.JSON_MINIFIED -> jsonMinified
                        DatasetFileType.ZIP -> zip
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
                assertThat(result).hasMessage("File sizes for dataset are not plausible: [json=${json.fileSize()}, jsonMinified=${jsonMinified.fileSize()}, zip=${zip.fileSize()}]")
            }
        }

        @Test
        fun `throws exception if zip json file size is greater than json file size`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                }

                val json = tempDir.resolve("json.txt").createFile()
                "text\n".repeat(100).writeToFile(json)

                val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                "text\n".repeat(1).writeToFile(jsonMinified)

                val zip = tempDir.resolve("zip.txt").createFile()
                "text\n".repeat(1000).writeToFile(zip)

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                        DatasetFileType.JSON -> json
                        DatasetFileType.JSON_MINIFIED -> jsonMinified
                        DatasetFileType.ZIP -> zip
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
                assertThat(result).hasMessage("File sizes for dataset are not plausible: [json=${json.fileSize()}, jsonMinified=${jsonMinified.fileSize()}, zip=${zip.fileSize()}]")
            }
        }

        @Test
        fun `returns true if dataset sizes are valid`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                }

                val json = tempDir.resolve("json.txt").createFile()
                "text\n".repeat(1000).writeToFile(json)

                val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                "text\n".repeat(100).writeToFile(jsonMinified)

                val zip = tempDir.resolve("zip.txt").createFile()
                "text\n".repeat(1).writeToFile(zip)

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                        DatasetFileType.JSON -> json
                        DatasetFileType.JSON_MINIFIED -> jsonMinified
                        DatasetFileType.ZIP -> zip
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

        @Test
        fun `throws exception if minified json file size for dead entries is greater than json file size`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                }

                val json = tempDir.resolve("json.txt").createFile()
                "text\n".repeat(1000).writeToFile(json)

                val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                "text\n".repeat(100).writeToFile(jsonMinified)

                val zip = tempDir.resolve("zip.txt").createFile()
                "text\n".repeat(1).writeToFile(zip)

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                        DatasetFileType.JSON -> json
                        DatasetFileType.JSON_MINIFIED -> jsonMinified
                        DatasetFileType.ZIP -> zip
                    }
                }

                val deadEntriesJson = tempDir.resolve("dead-entries-json.txt").createFile()
                "text\n".repeat(100).writeToFile(deadEntriesJson)

                val deadEntriesJsonMinified = tempDir.resolve("dead-entries-jsonMinified.txt").createFile()
                "text\n".repeat(1000).writeToFile(deadEntriesJsonMinified)

                val deadEntriesZip = tempDir.resolve("dead-entries-zip.txt").createFile()
                "text\n".repeat(1).writeToFile(deadEntriesZip)

                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                        DatasetFileType.JSON -> deadEntriesJson
                        DatasetFileType.JSON_MINIFIED -> deadEntriesJsonMinified
                        DatasetFileType.ZIP -> deadEntriesZip
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
                assertThat(result).hasMessage("File sizes for dead entry files of [example.org] are not plausible: [json=500, jsonMinified=5000, zip=5]")
            }
        }

        @Test
        fun `throws exception if zip json file size for dead entries is greater than minified json file size`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                }

                val json = tempDir.resolve("json.txt").createFile()
                "text\n".repeat(1000).writeToFile(json)

                val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                "text\n".repeat(100).writeToFile(jsonMinified)

                val zip = tempDir.resolve("zip.txt").createFile()
                "text\n".repeat(1).writeToFile(zip)

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                        DatasetFileType.JSON -> json
                        DatasetFileType.JSON_MINIFIED -> jsonMinified
                        DatasetFileType.ZIP -> zip
                    }
                }

                val deadEntriesJson = tempDir.resolve("dead-entries-json.txt").createFile()
                "text\n".repeat(1000).writeToFile(deadEntriesJson)

                val deadEntriesJsonMinified = tempDir.resolve("dead-entries-jsonMinified.txt").createFile()
                "text\n".repeat(1).writeToFile(deadEntriesJsonMinified)

                val deadEntriesZip = tempDir.resolve("dead-entries-zip.txt").createFile()
                "text\n".repeat(100).writeToFile(deadEntriesZip)

                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                        DatasetFileType.JSON -> deadEntriesJson
                        DatasetFileType.JSON_MINIFIED -> deadEntriesJsonMinified
                        DatasetFileType.ZIP -> deadEntriesZip
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
                assertThat(result).hasMessage("File sizes for dead entry files of [example.org] are not plausible: [json=5000, jsonMinified=5, zip=500]")
            }
        }

        @Test
        fun `throws exception if zip json file size for dead entries is greater than json file size`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                }

                val json = tempDir.resolve("json.txt").createFile()
                "text\n".repeat(1000).writeToFile(json)

                val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                "text\n".repeat(100).writeToFile(jsonMinified)

                val zip = tempDir.resolve("zip.txt").createFile()
                "text\n".repeat(1).writeToFile(zip)

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                        DatasetFileType.JSON -> json
                        DatasetFileType.JSON_MINIFIED -> jsonMinified
                        DatasetFileType.ZIP -> zip
                    }
                }

                val deadEntriesJson = tempDir.resolve("dead-entries-json.txt").createFile()
                "text\n".repeat(100).writeToFile(deadEntriesJson)

                val deadEntriesJsonMinified = tempDir.resolve("dead-entries-jsonMinified.txt").createFile()
                "text\n".repeat(1).writeToFile(deadEntriesJsonMinified)

                val deadEntriesZip = tempDir.resolve("dead-entries-zip.txt").createFile()
                "text\n".repeat(1000).writeToFile(deadEntriesZip)

                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                        DatasetFileType.JSON -> deadEntriesJson
                        DatasetFileType.JSON_MINIFIED -> deadEntriesJsonMinified
                        DatasetFileType.ZIP -> deadEntriesZip
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
                assertThat(result).hasMessage("File sizes for dead entry files of [example.org] are not plausible: [json=500, jsonMinified=5, zip=5000]")
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
                }

                val json = tempDir.resolve("json.txt").createFile()
                "text\n".repeat(1000).writeToFile(json)

                val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                "text\n".repeat(100).writeToFile(jsonMinified)

                val zip = tempDir.resolve("zip.txt").createFile()
                "text\n".repeat(1).writeToFile(zip)

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                        DatasetFileType.JSON -> json
                        DatasetFileType.JSON_MINIFIED -> jsonMinified
                        DatasetFileType.ZIP -> zip
                    }
                }

                val deadEntriesJson = tempDir.resolve("dead-entries-json.txt").createFile()
                "text\n".repeat(1000).writeToFile(deadEntriesJson)

                val deadEntriesJsonMinified = tempDir.resolve("dead-entries-jsonMinified.txt").createFile()
                "text\n".repeat(100).writeToFile(deadEntriesJsonMinified)

                val deadEntriesZip = tempDir.resolve("dead-entries-zip.txt").createFile()
                "text\n".repeat(1).writeToFile(deadEntriesZip)

                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                        DatasetFileType.JSON -> deadEntriesJson
                        DatasetFileType.JSON_MINIFIED -> deadEntriesJsonMinified
                        DatasetFileType.ZIP -> deadEntriesZip
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
        fun `ignores files which don't exist`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                }

                val json = tempDir.resolve("json.txt").createFile()
                "text\n".repeat(1000).writeToFile(json)

                val jsonMinified = tempDir.resolve("jsonMinified.txt").createFile()
                "text\n".repeat(100).writeToFile(jsonMinified)

                val zip = tempDir.resolve("zip.txt").createFile()
                "text\n".repeat(1).writeToFile(zip)

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = when(type) {
                        DatasetFileType.JSON -> json
                        DatasetFileType.JSON_MINIFIED -> jsonMinified
                        DatasetFileType.ZIP -> zip
                    }
                }

                val deadEntriesJson = tempDir.resolve("dead-entries-json.txt")
                val deadEntriesJsonMinified = tempDir.resolve("dead-entries-jsonMinified.txt")
                val deadEntriesZip = tempDir.resolve("dead-entries-zip.txt")

                val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                    override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = when(type) {
                        DatasetFileType.JSON -> deadEntriesJson
                        DatasetFileType.JSON_MINIFIED -> deadEntriesJsonMinified
                        DatasetFileType.ZIP -> deadEntriesZip
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
                assertThat(result===previous).isTrue()
            }
        }
    }
}