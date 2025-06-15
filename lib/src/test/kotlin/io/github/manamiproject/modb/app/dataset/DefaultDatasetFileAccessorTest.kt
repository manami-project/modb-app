package io.github.manamiproject.modb.app.dataset

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestDeserializer
import io.github.manamiproject.modb.app.TestJsonSerializer
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.dataset.DatasetFileType.*
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.fileName
import io.github.manamiproject.modb.serde.json.deserializer.Deserializer
import io.github.manamiproject.modb.serde.json.serializer.JsonSerializer
import io.github.manamiproject.modb.serde.json.models.Dataset
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset.UTC
import kotlin.io.path.Path
import kotlin.test.Test

internal class DefaultDatasetFileAccessorTest {

    @Nested
    inner class SaveEntriesTests {

        @Test
        fun `creates five files using serializers`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun clock(): Clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                }

                var hasBeenInvoked = false
                val testSerializer = object: JsonSerializer<Collection<Anime>> by TestJsonSerializer() {
                    override suspend fun serialize(obj: Collection<Anime>, minify: Boolean): String {
                        hasBeenInvoked = true
                        return "{}"
                    }
                }

                val databaseAccess = DefaultDatasetFileAccessor(
                    appConfig = testAppConfig,
                    deserializer = TestDeserializer(),
                    jsonSerializer = testSerializer,
                )

                // when
                databaseAccess.saveEntries(emptyList())

                // then
                assertThat(hasBeenInvoked).isTrue()
                assertThat(tempDir.resolve("anime-offline-database.json")).exists()
                assertThat(tempDir.resolve("anime-offline-database-minified.json")).exists()
                assertThat(tempDir.resolve("anime-offline-database-minified.json.zst")).exists()
                assertThat(tempDir.resolve("anime-offline-database.jsonl")).exists()
                assertThat(tempDir.resolve("anime-offline-database.jsonl.zst")).exists()
            }
        }
    }

    @Nested
    inner class FetchEntriesTests {

        @Test
        fun `retrieves data from the minified JSON`() {
            runBlocking {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = Path(".")
                    override fun clock(): Clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                }

                var invokedWith = EMPTY
                val testDeserializer = object: Deserializer<RegularFile, Dataset> by TestDeserializer() {
                    override suspend fun deserialize(source: RegularFile): Dataset {
                        invokedWith = source.fileName()
                        return Dataset(
                            `$schema` = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/heads/master/anime-offline-database.schema.json"),
                            lastUpdate = "2020-01-01",
                            data = emptyList(),
                        )
                    }
                }

                val databaseAccess = DefaultDatasetFileAccessor(
                    appConfig = testAppConfig,
                    deserializer = testDeserializer,
                    jsonSerializer = TestJsonSerializer(),
                )

                // when
                databaseAccess.fetchEntries()

                // then
                assertThat(invokedWith).isEqualTo("anime-offline-database-minified.json")
            }
        }
    }

    @Nested
    inner class OfflineDatabaseFileTests {

        @ParameterizedTest
        @EnumSource(value = DatasetFileType::class)
        fun `file resides in offlineDatabaseDirectory`(value: DatasetFileType) {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun clock(): Clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                }

                val databaseAccess = DefaultDatasetFileAccessor(
                    appConfig = testAppConfig,
                    deserializer = TestDeserializer(),
                    jsonSerializer = TestJsonSerializer(),
                )

                // when
                val result = databaseAccess.offlineDatabaseFile(value).parent

                // then
                assertThat(result).isEqualTo(testAppConfig.outputDirectory())
            }
        }

        @Test
        fun `return correct database file based on type JSON_PRETTY_PRINT`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun clock(): Clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                }

                val databaseAccess = DefaultDatasetFileAccessor(
                    appConfig = testAppConfig,
                    deserializer = TestDeserializer(),
                    jsonSerializer = TestJsonSerializer(),
                )

                // when
                val result = databaseAccess.offlineDatabaseFile(JSON_PRETTY_PRINT).fileName.toString()

                // then
                assertThat(result).isEqualTo("anime-offline-database.json")
            }
        }

        @Test
        fun `return correct database file based on type JSON_MINIFIED`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun clock(): Clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                }

                val databaseAccess = DefaultDatasetFileAccessor(
                    appConfig = testAppConfig,
                    deserializer = TestDeserializer(),
                    jsonSerializer = TestJsonSerializer(),
                )

                // when
                val result = databaseAccess.offlineDatabaseFile(JSON_MINIFIED).fileName.toString()

                // then
                assertThat(result).isEqualTo("anime-offline-database-minified.json")
            }
        }

        @Test
        fun `return correct database file based on type JSON_MINIFIED_ZST`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun clock(): Clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                }

                val databaseAccess = DefaultDatasetFileAccessor(
                    appConfig = testAppConfig,
                    deserializer = TestDeserializer(),
                    jsonSerializer = TestJsonSerializer(),
                )

                // when
                val result = databaseAccess.offlineDatabaseFile(JSON_MINIFIED_ZST).fileName.toString()

                // then
                assertThat(result).isEqualTo("anime-offline-database-minified.json.zst")
            }
        }

        @Test
        fun `return correct database file based on type JSON_LINES`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun clock(): Clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                }

                val databaseAccess = DefaultDatasetFileAccessor(
                    appConfig = testAppConfig,
                    deserializer = TestDeserializer(),
                    jsonSerializer = TestJsonSerializer(),
                )

                // when
                val result = databaseAccess.offlineDatabaseFile(JSON_LINES).fileName.toString()

                // then
                assertThat(result).isEqualTo("anime-offline-database.jsonl")
            }
        }

        @Test
        fun `return correct database file based on type JSON_LINES_ZST`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun clock(): Clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                }

                val databaseAccess = DefaultDatasetFileAccessor(
                    appConfig = testAppConfig,
                    deserializer = TestDeserializer(),
                    jsonSerializer = TestJsonSerializer(),
                )

                // when
                val result = databaseAccess.offlineDatabaseFile(JSON_LINES_ZST).fileName.toString()

                // then
                assertThat(result).isEqualTo("anime-offline-database.jsonl.zst")
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = DefaultDatasetFileAccessor.instance

            // when
            val result = DefaultDatasetFileAccessor.instance

            // then
            assertThat(result).isExactlyInstanceOf(DefaultDatasetFileAccessor::class.java)
            assertThat(result===previous).isTrue()
        }
    }
}