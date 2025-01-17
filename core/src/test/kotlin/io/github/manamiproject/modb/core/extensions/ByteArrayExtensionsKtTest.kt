package io.github.manamiproject.modb.core.extensions

import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.readBytes
import kotlin.test.Test

internal class ByteArrayExtensionsKtTest {

    @Nested
    inner class WriteToFileTests {

        @Test
        fun `throws exception if given Path already exists, but is a directory`() {
            tempDirectory {
                // given
                val file = tempDir.resolve("test").createDirectory()

                // when
                val result = exceptionExpected<Exception> { //exception type varies depending on the OS
                    "text".toByteArray().writeToFile(file, false)
                }

                // then
                assertThat(result).hasMessageContaining(file.toString()) //message varies depending on the OS
            }
        }

        @Test
        fun `throws exception if the ByteArray is empty`() {
            tempDirectory {
                // given
                val file = tempDir.resolve("test.txt")

                // when
                val result = exceptionExpected<IllegalStateException> {
                    EMPTY.toByteArray().writeToFile(file, false)
                }

                // then
                assertThat(result).hasMessage("Tried to write file [$file], but the ByteArray was empty.")
            }
        }

        @Test
        fun `successfully write ByteArray without lock file`() {
            tempDirectory {
                // given
                val obj = "Some content\nfor a test file.".toByteArray()
                val file = tempDir.resolve("test.txt")

                // when
                obj.writeToFile(file, false)

                // then
                assertThat(file).exists()
                assertThat(file.readBytes()).isEqualTo(obj)
            }
        }

        @Test
        fun `successfully write ByteArray with lock file`() {
            tempDirectory {
                // given
                val obj = "Some content\nfor a test file.".toByteArray()
                val file = tempDir.resolve("test.txt")

                // when
                obj.writeToFile(file, true)

                // then
                assertThat(file).exists()
                assertThat(file.readBytes()).isEqualTo(obj)
            }
        }

        @Test
        fun `overrides file if the file already exists`() {
            tempDirectory {
                // given
                val file = tempDir.resolve("test.txt").createFile()
                "Some content\nfor a test file.".toByteArray().writeToFile(file)

                val obj = "Some totally different content.".toByteArray()

                // when
                obj.writeToFile(file, false)

                // then
                assertThat(file).exists()
                assertThat(file.readBytes()).isEqualTo(obj)
            }
        }
    }
}