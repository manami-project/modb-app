package io.github.manamiproject.modb.test

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.opentest4j.AssertionFailedError
import java.io.InputStream
import java.nio.file.Files

internal class FunctionsKtTests {

    @Test
    fun `shouldNotBeInvoked() throws exception with specific message` () {
        // when
        val result = assertThrows<AssertionFailedError> {
            shouldNotBeInvoked()
        }

        // then
        assertThat(result).hasMessage("should not be invoked")
    }

    @Nested
    inner class TestResourceTests {

        @Test
        fun `test resource in root directory`() {
            // given
            val path = "test-file.txt"

            // when
            val result = testResource("test_resource_tests/$path")

            // then
            assertThat(result).exists()
            assertThat(result).isRegularFile()
            assertThat(result.fileName.toString()).endsWith(path)
        }

        @Test
        fun `test resource in subdirectory`() {
            // given
            val filename = "other-test-file.txt"

            // when
            val result = testResource("test_resource_tests/subdirectory/$filename")

            // then
            assertThat(result).exists()
            assertThat(result).isRegularFile()
            assertThat(result.fileName.toString()).endsWith(filename)
        }

        @Test
        fun `grants access to a directory`() {
            // when
            val result = testResource("test_resource_tests")

            // then
            assertThat(result).exists()
            assertThat(result).isDirectory()
            assertThat(Files.list(result)).hasSize(2)
        }

        @Test
        fun `throws an exception if the the given path does not exist`() {
            val path = "non-existent-file.txt"

            // when
            val result = assertThrows<IllegalStateException> {
                testResource(path)
            }

            // then
            assertThat(result).hasMessage("Path [$path] not found.")
        }

        @ParameterizedTest
        @ValueSource(strings = [
            "",
            "   ",
            "\u00A0",
            "\u202F",
            "\u200A",
            "\u205F",
            "\u2000",
            "\u2001",
            "\u2002",
            "\u2003",
            "\u2004",
            "\u2005",
            "\u2006",
            "\u2007",
            "\u2008",
            "\u2009",
            "\uFEFF",
            "\u180E",
            "\u2060",
            "\u200D",
            "\u0090",
            "\u200C",
            "\u200B",
            "\u00AD",
            "\u000C",
            "\u2028",
            "\r",
            "\n",
            "\t",
        ])
        fun `throws exception if the path is empty or blank`(path: String) {
            // when
            val result = assertThrows<IllegalArgumentException> {
                testResource(path)
            }

            // then
            assertThat(result).hasMessage("Path must not be blank")
        }
    }

    @Nested
    inner class LoadTestResourceTests {

        @Test
        fun `load test resource as String from root directory`() {
            // when
            val result = loadTestResource<String>("test_resource_tests/test-file.txt")

            // then
            assertThat(result).isEqualTo("File in\n\nroot directory.")
        }

        @Test
        fun `load test resource as String from subdirectory`() {
            // when
            val result = loadTestResource<String>("test_resource_tests/subdirectory/other-test-file.txt")

            // then
            assertThat(result).isEqualTo("File in\nsubdirectory.")
        }

        @Test
        fun `load test resource as ByteArray from root directory`() {
            // when
            val result = loadTestResource<ByteArray>("test_resource_tests/test-file.txt")

            // then
            assertThat(result).isEqualTo("File in\n\nroot directory.".toByteArray())
        }

        @Test
        fun `load test resource as ByteArray from subdirectory`() {
            // when
            val result = loadTestResource<ByteArray>("test_resource_tests/subdirectory/other-test-file.txt")

            // then
            assertThat(result).isEqualTo("File in\nsubdirectory.".toByteArray())
        }

        @Test
        fun `load test resource as InputStream from root directory`() {
            // when
            val result = loadTestResource<InputStream>("test_resource_tests/test-file.txt")

            // then
            assertThat(result.readAllBytes()).isEqualTo("File in\n\nroot directory.".toByteArray())
        }

        @Test
        fun `load test resource as InputStream from subdirectory`() {
            // when
            val result = loadTestResource<InputStream>("test_resource_tests/subdirectory/other-test-file.txt")

            // then
            assertThat(result.readAllBytes()).isEqualTo("File in\nsubdirectory.".toByteArray())
        }

        @Test
        fun `throws an exception if the the given path is a directory`() {
            val path = "test_resource_tests"

            // when
            val result = assertThrows<IllegalStateException> {
                loadTestResource(path)
            }

            // then
            assertThat(result).hasMessage("[$path] either not exists or is not a file.")
        }

        @Test
        fun `throws an exception if the the given path does not exist`() {
            val path = "non-existent-file.txt"

            // when
            val result = assertThrows<IllegalStateException> {
                loadTestResource(path)
            }

            // then
            assertThat(result).hasMessage("Path [$path] not found.")
        }

        @Test
        fun `throws an exception if the the generic type is not supported`() {
            val path = "test_resource_tests/test-file.txt"

            // when
            val result = assertThrows<IllegalStateException> {
                loadTestResource<Int>(path)
            }

            // then
            assertThat(result).hasMessage("Unsupported file type. String, ByteArray and InputStream are supported.")
        }

        @ParameterizedTest
        @ValueSource(strings = [
            "",
            "   ",
            "\u00A0",
            "\u202F",
            "\u200A",
            "\u205F",
            "\u2000",
            "\u2001",
            "\u2002",
            "\u2003",
            "\u2004",
            "\u2005",
            "\u2006",
            "\u2007",
            "\u2008",
            "\u2009",
            "\uFEFF",
            "\u180E",
            "\u2060",
            "\u200D",
            "\u0090",
            "\u200C",
            "\u200B",
            "\u00AD",
            "\u000C",
            "\u2028",
            "\r",
            "\n",
            "\t",
        ])
        fun `throws exception if the path is empty or blank`(path: String) {
            // when
            val result = assertThrows<IllegalArgumentException> {
                loadTestResource(path)
            }

            // then
            assertThat(result).hasMessage("Path must not be blank")
        }
    }

    @Nested
    inner class ExceptionExpectedTests {

        @Test
        fun `fails if suspend function doesn't throw anything`() {
            var result: Throwable? = null

            // when
            try {
                exceptionExpected<AssertionFailedError> {

                }
            } catch (e: AssertionError) {
                result = e
            }

            // then
            assertThat(result).hasMessage("No exception has been thrown")
        }

        @Test
        fun `fails if exception has different type`() {
            var result: Throwable? = null

            // when
            try {
                exceptionExpected<IllegalArgumentException> {
                    throw IllegalStateException()
                }
            } catch (e: AssertionError) {
                result = e
            }

            // then
            assertThat(result).hasMessage("Expected [IllegalArgumentException] to be thrown, but [IllegalStateException] was thrown.")
        }

        @Test
        fun `successfully returns exception`() {
            // when
            val result = exceptionExpected<IllegalArgumentException> {
                throw IllegalArgumentException("test message")
            }

            // then
            assertThat(result).hasMessage("test message")
        }
    }
}