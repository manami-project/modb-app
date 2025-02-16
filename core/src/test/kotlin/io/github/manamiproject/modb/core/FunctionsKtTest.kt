package io.github.manamiproject.modb.core

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.testResource
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI
import java.net.URL
import kotlin.test.Test

internal class FunctionsKtTest {

    @Nested
    inner class LoadResourceTests {

        @Test
        fun `load test resource from root directory`() {
            runBlocking {
                // when
                val result = loadResource("FunctionsKtTest/load_resource_tests/test-file.txt")

                // then
                assertThat(result).isEqualTo("File in\n\nroot directory.")
            }
        }

        @Test
        fun `load test resource from subdirectory`() {
            runBlocking {
                // when
                val result = loadResource("FunctionsKtTest/load_resource_tests/subdirectory/other-test-file.txt")

                // then
                assertThat(result).isEqualTo("File in\nsubdirectory.")
            }
        }

        @Test
        fun `returns a list of the names of the elements if the given path is a directory`() {
            runBlocking {
                val path = "FunctionsKtTest/load_resource_tests"

                // when
                val result = loadResource(path)

                // then
                assertThat(result).isEqualTo("subdirectory\ntest-file.txt\n")
            }
        }

        @Test
        fun `throws an exception if the the given path does not exist`() {
            val path = "non-existent-file.txt"

            // when
            val result = exceptionExpected<IllegalStateException> {
                loadResource(path)
            }

            // then
            assertThat(result).hasMessage("Unable to load file [$path]")
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
        fun `throws an exception if the the given path is blank or empty`(value: String) {
            // when
            val result = exceptionExpected<IllegalArgumentException> {
                loadResource(value)
            }

            // then
            assertThat(result).hasMessage("Given path must not be blank.")
        }
    }

    @Nested
    inner class ResourceFileExistsTests {

        @Nested
        inner class FileProtocolTests {

            @Test
            fun `returns true if the file exists`() {
                // given
                val path = "FunctionsKtTest/resource_file_exists_tests/test-file.txt"

                // when
                val result = resourceFileExists(path)

                // then
                assertThat(result).isTrue()
            }

            @Test
            fun `returns false if the file exists`() {
                // given
                val path = "FunctionsKtTest/resource_file_exists_tests/non-existent-file.txt"

                // when
                val result = resourceFileExists(path)

                // then
                assertThat(result).isFalse()
            }

            @Test
            fun `returns false if the given path is a directory`() {
                // given
                val path = "FunctionsKtTest/resource_file_exists_tests"

                // when
                val result = resourceFileExists(path)

                // then
                assertThat(result).isFalse()
            }
        }

        @Nested
        inner class JarProtocolTests {

            @Test
            fun `returns false if the jar file doesn't exist`() {
                // given
                val existingTestResource = testResource("FunctionsKtTest/resource_file_exists_tests/not-exists.jar")
                val renamedPath = existingTestResource.toUri().toString().replace("test-file.txt", "non-existent.jar")
                val testClassLoader = object: ClassLoader() {
                    override fun getResource(name: String?): URL? = URI("jar:${renamedPath}!/$name").toURL()
                }

                // when
                val result = resourceFileExists("test-file.txt", testClassLoader)

                // then
                assertThat(result).isFalse()
            }

            @Test
            fun `returns false if the jar file exist, but is empty`() {
                // given
                val jar = testResource("FunctionsKtTest/resource_file_exists_tests/empty.jar")
                val testClassLoader = object: ClassLoader() {
                    override fun getResource(name: String?): URL? = URI("jar:${jar.toUri()}!/$name").toURL()
                }

                // when
                val result = resourceFileExists("test-file.txt", testClassLoader)

                // then
                assertThat(result).isFalse()
            }

            @Test
            fun `returns false if the jar file exist, but it doesn't contain the resource`() {
                // given
                val jar = testResource("FunctionsKtTest/resource_file_exists_tests/not-exists.jar")
                val testClassLoader = object: ClassLoader() {
                    override fun getResource(name: String?): URL? = URI("jar:${jar.toUri()}!/$name").toURL()
                }

                // when
                val result = resourceFileExists("test-file.txt", testClassLoader)

                // then
                assertThat(result).isFalse()
            }

            @Test
            fun `returns true if the jar file contains the resource file`() {
                // given
                val jar = testResource("FunctionsKtTest/resource_file_exists_tests/exists.jar")
                val testClassLoader = object: ClassLoader() {
                    override fun getResource(name: String?): URL? = URI("jar:${jar.toUri()}!/$name").toURL()
                }

                // when
                val result = resourceFileExists("test-file.txt", testClassLoader)

                // then
                assertThat(result).isTrue()
            }

            @Test
            fun `returns false, because resource in jar is a directory`() {
                // given
                val jar = testResource("FunctionsKtTest/resource_file_exists_tests/contains-directory.jar")
                val testClassLoader = object: ClassLoader() {
                    override fun getResource(name: String?): URL? = URI("jar:${jar.toUri()}!/$name").toURL()
                }

                // when
                val result = resourceFileExists("test-directory", testClassLoader)

                // then
                assertThat(result).isFalse()
            }
        }

        @Test
        fun `throws exception if protocol is unknown`() {
            // given
            val path = "FunctionsKtTest/resource_file_exists_tests"
            val testClassLoader = object: ClassLoader() {
                override fun getResource(name: String?): URL? = URI("https://example.org/file.txt").toURL()
            }

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                resourceFileExists(path, testClassLoader)
            }

            // then
            assertThat(result).hasMessage("Unknown protocol.")
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
        fun `throws an exception if the the given path is blank or empty`(value: String) {
            // when
            val result = assertThrows<IllegalArgumentException> {
                resourceFileExists(value)
            }

            // then
            assertThat(result).hasMessage("Given path must not be blank.")
        }
    }

    @Nested
    inner class RandomTests {

        @Test
        fun `parameter cannot be equal`() {
            // when
            val result = assertThrows<IllegalArgumentException> {
                random(4, 4)
            }

            // then
            assertThat(result).hasMessage("Numbers cannot be equal.")
        }

        @Test
        fun `generates a random number within the given interval`() {
            // given
            val min = 1
            val max = 2

            // when
            val result = random(min, max)

            // then
            assertThat(result).isBetween(min.toLong(), max.toLong())
        }

        @Test
        fun `order of the parameters doesn't matter`() {
            // given
            val min = 1
            val max = 2

            // when
            val result = random(max, min)

            // then
            assertThat(result).isBetween(min.toLong(), max.toLong())
        }
    }

    @Nested
    inner class ExcludeFromTestContextTests {

        @Test
        fun `execute code if current context is not test context`() {
            runBlocking {
                // given
                var hasBeenInvoked = false

                val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun isTestContext(): Boolean = false
                }

                // when
                excludeFromTestContext(testConfig) { hasBeenInvoked = true }

                // then
                assertThat(hasBeenInvoked).isTrue()
            }
        }

        @Test
        fun `don't execute code if current context is test context`() {
            runBlocking {
                // given
                var hasBeenInvoked = false

                val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun isTestContext(): Boolean = true
                }

                // when
                excludeFromTestContext(testConfig) { hasBeenInvoked = true }

                // then
                assertThat(hasBeenInvoked).isFalse()
            }
        }
    }
}