package io.github.manamiproject.modb.core.extractor

import io.github.manamiproject.modb.core.TestDataExtractor
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.test.Test

internal class DefaultPathDataExtractorTest {

    @Test
    fun `throws exception if the given Path does not exist`() {
        tempDirectory {
            // given
            val file = tempDir.resolve("test.txt")

            val specificTestConverter = object: DataExtractor by TestDataExtractor { }
            val converter = DefaultPathDataExtractor(specificTestConverter, "txt")

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                converter.extract(file, mapOf("result" to "//test"))
            }

            // then
            assertThat(result).hasMessage("Given path [$file] does not exist.")
        }
    }

    @Test
    fun `successfully converts a single file`() {
        tempDirectory {
            // given
            val file = tempDir.resolve("test.txt").createFile()
            "Correct file".writeToFile(file)

            val expected = ExtractionResult(mapOf("result" to "testValue"))

            val specificTestConverter = object : DataExtractor by TestDataExtractor {
                override suspend fun extract(
                    rawContent: String,
                    selection: Map<OutputKey, Selector>
                ): ExtractionResult {
                    return if (rawContent == "Correct file") {
                        expected
                    } else {
                        shouldNotBeInvoked()
                    }
                }
            }

            val converter = DefaultPathDataExtractor(specificTestConverter, "txt")

            // when
            val result = converter.extract(file, mapOf("result" to "//test"))

            // then
            assertThat(result).containsExactly(expected)
        }
    }

    @Test
    fun `successfully convert a whole directory`() {
        tempDirectory {
            // given
            val directory = tempDir.resolve("dir").createDirectory().apply {
                "accept 1".writeToFile(this.resolve("file1.txt"))
                "accept 2".writeToFile(this.resolve("file2.txt"))
            }

            val specificTestConverter = object: DataExtractor by TestDataExtractor {
                override suspend fun extract(
                    rawContent: String,
                    selection: Map<OutputKey, Selector>
                ): ExtractionResult {
                    return if (rawContent.startsWith("accept")) {
                        ExtractionResult(mapOf("result" to rawContent))
                    } else {
                        shouldNotBeInvoked()
                    }
                }
            }

            val converter = DefaultPathDataExtractor(specificTestConverter, "txt")

            // when
            val result = converter.extract(directory, mapOf("result" to "//test"))

            // then
            assertThat(result).containsExactlyInAnyOrder(
                ExtractionResult(mapOf("result" to "accept 1")),
                ExtractionResult(mapOf("result" to "accept 2")),
            )
        }
    }
}