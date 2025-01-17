package io.github.manamiproject.modb.test

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import org.junit.jupiter.api.assertThrows
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

internal class TempDirectoryKtTest {

    @Nested
    inner class TempDirectoryTests {

        @Test
        fun `delete temp directory including subdirectory and files`() {
            // given
            val tempDirectory = TempDirectory()

            val testFileName = "test-file.txt"
            Files.copy(testResource("test_resource_tests/$testFileName"), tempDirectory.tempDir.resolve(testFileName))

            val otherTestFileName = "other-test-file.txt"
            val subdirectoryName = "subdirectory"
            val subdirectory = tempDirectory.tempDir.resolve(subdirectoryName).apply {
                Files.createDirectory(this)
            }

            Files.copy(testResource("test_resource_tests/$subdirectoryName/$otherTestFileName"), subdirectory.resolve(otherTestFileName))

            // when
            tempDirectory.close()

            // then
            assertThat(tempDirectory.tempDir).doesNotExist()
        }
    }

    @Nested
    inner class InfixMethodTests {

        @Test
        fun `removes temp directory even if an exception is thrown`() {
            // given
            val currentDirectory = Paths.get(("."))
            var tempDir: Path = currentDirectory

            // when
            assertThrows<IllegalStateException> {
                tempDirectory {
                    tempDir = this.tempDir

                    throw IllegalStateException()
                }
            }

            // then
            assertThat(tempDir).isNotEqualTo(currentDirectory)
            assertThat(tempDir).doesNotExist()
        }

        @Test
        fun `successfully create and delete temp dir`() {
            // given
            val currentDirectory = Paths.get(("."))
            var tempDir: Path = currentDirectory

            // when
            tempDirectory {
                tempDir = this.tempDir
            }

            // then
            assertThat(tempDir).isNotEqualTo(currentDirectory)
            assertThat(tempDir).doesNotExist()
        }
    }
}