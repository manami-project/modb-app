package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.TestDatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DatasetFileType
import io.github.manamiproject.modb.app.postprocessors.FileSizePlausibilityValidationPostProcessor
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
                    datasetFileAccessor = testDatasetFileAccessor,
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
                    datasetFileAccessor = testDatasetFileAccessor,
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
                    datasetFileAccessor = testDatasetFileAccessor,
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
        fun `returns true if sizes are valid`() {
            tempDirectory {
                // given
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
                    datasetFileAccessor = testDatasetFileAccessor,
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