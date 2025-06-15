package io.github.manamiproject.modb.serde.json.deserializer

import com.github.luben.zstd.ZstdOutputStream
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import io.github.manamiproject.modb.serde.TestDeserializer
import io.github.manamiproject.modb.serde.json.models.Dataset
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.tempDirectory
import io.github.manamiproject.modb.test.testResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.io.path.outputStream
import kotlin.test.Test

internal class FromRegularFileDeserializerTest {

    @Nested
    inner class DeserializeTests {

        @Test
        fun `throws exception if the given path is a directory`() {
            tempDirectory {
                // given
                val deserializer = FromRegularFileDeserializer(
                    deserializer = TestDeserializer<LifecycleAwareInputStream, Dataset>(),
                )

                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    deserializer.deserialize(tempDir)
                }

                // then
                assertThat(result).hasMessage("The given path does not exist or is not a regular file: [${tempDir.toAbsolutePath()}]")
            }
        }

        @Test
        fun `throws exception if the given file does not exist`() {
            tempDirectory {
                // given
                val deserializer = FromRegularFileDeserializer(
                    deserializer = TestDeserializer<LifecycleAwareInputStream, Dataset>(),
                )
                val testFile = tempDir.resolve("anime-offline-database.json")

                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    deserializer.deserialize(testFile)
                }

                // then
                assertThat(result).hasMessage("The given path does not exist or is not a regular file: [${testFile.toAbsolutePath()}]")
            }
        }

        @Test
        fun `throws exception if the given file is neither json nor Zstandard file`() {
            tempDirectory {
                // given
                val deserializer = FromRegularFileDeserializer(
                    deserializer = TestDeserializer<LifecycleAwareInputStream, Dataset>(),
                )

                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    deserializer.deserialize(testResource("logback-test.xml"))
                }

                // then
                assertThat(result).hasMessage("File of type [xml] is not supported.]")
            }
        }

        @Test
        fun `delegates input stream for JSON deserialization`() {
            tempDirectory {
                // given
                val testFile = tempDir.resolve("test.json")
                """{ "test": true }""".writeToFile(testFile)

                var invokedWith = EMPTY
                val testDeserializer = object : Deserializer<LifecycleAwareInputStream, Dataset> by TestDeserializer() {
                    override suspend fun deserialize(source: LifecycleAwareInputStream): Dataset {
                        invokedWith = source.bufferedReader().readText()
                        return Dataset(
                            `$schema` = URI("https://example.org"),
                            lastUpdate = "2020-01-01",
                            data = emptyList(),
                        )
                    }
                }

                val deserializer = FromRegularFileDeserializer(
                    deserializer = testDeserializer,
                )

                // when
                deserializer.deserialize(testFile)

                // then
                assertThat(invokedWith).isEqualTo("""{ "test": true }""")
            }
        }

        @Test
        fun `delegates input stream for ZST deserialization`() {
            tempDirectory {
                // given
                val testFile = tempDir.resolve("test.zst")
                testFile.outputStream().use { fos ->
                    ZstdOutputStream(fos, 22).use { zstOut ->
                        zstOut.write("""{ "test": true }""".toByteArray())
                    }
                }

                var invokedWith = EMPTY
                val testDeserializer = object : Deserializer<LifecycleAwareInputStream, Dataset> by TestDeserializer() {
                    override suspend fun deserialize(source: LifecycleAwareInputStream): Dataset {
                        invokedWith = source.use { it.bufferedReader().readText() }
                        return Dataset(
                            `$schema` = URI("https://example.org"),
                            lastUpdate = "2020-01-01",
                            data = emptyList(),
                        )
                    }
                }

                val deserializer = FromRegularFileDeserializer(
                    deserializer = testDeserializer,
                )

                // when
                deserializer.deserialize(testFile)

                // then
                assertThat(invokedWith).isEqualTo("""{ "test": true }""")
            }
        }
    }
}