package io.github.manamiproject.modb.app.dataset

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestExternalResourceJsonDeserializerDataset
import io.github.manamiproject.modb.app.TestJsonSerializerCollectionAnime
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.dataset.DatasetFileType.*
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.serde.json.ExternalResourceJsonDeserializer
import io.github.manamiproject.modb.serde.json.JsonSerializer
import io.github.manamiproject.modb.serde.json.models.Dataset
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.io.path.Path
import kotlin.test.Test

internal class DefaultDatasetFileAccessorTest {

    @Nested
    inner class SaveEntriesTests {

        @Test
        fun `creates three files using serializers`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                }

                var hasBeenInvoked = false
                val testAnimeListJsonSerializer = object: JsonSerializer<Collection<Anime>> by TestJsonSerializerCollectionAnime {
                    override suspend fun serialize(obj: Collection<Anime>, minify: Boolean): String {
                        hasBeenInvoked = true
                        return "{}"
                    }
                }

                val databaseAccess = DefaultDatasetFileAccessor(
                    appConfig = testAppConfig,
                    deserializer = TestExternalResourceJsonDeserializerDataset,
                    jsonSerializer = testAnimeListJsonSerializer,
                )

                // when
                databaseAccess.saveEntries(emptyList())

                // then
                assertThat(hasBeenInvoked).isTrue()
                assertThat(tempDir.resolve("anime-offline-database.json")).exists()
                assertThat(tempDir.resolve("anime-offline-database-minified.json")).exists()
                assertThat(tempDir.resolve("anime-offline-database.zip")).exists()
            }
        }
    }

    @Nested
    inner class FetchEntriesTests {

        @Test
        fun `delegates loading to the deserializer`() {
            runBlocking {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = Path(".")
                }

                var hasBeenInvoked = false
                val testExternalResourceAvroDeserializer = object: ExternalResourceJsonDeserializer<Dataset> by TestExternalResourceJsonDeserializerDataset {
                    override suspend fun deserialize(file: RegularFile): Dataset {
                        hasBeenInvoked = true
                        return Dataset(
                            lastUpdate = "2020-01-01",
                            data = emptyList(),
                        )
                    }
                }

                val databaseAccess = DefaultDatasetFileAccessor(
                    appConfig = testAppConfig,
                    deserializer = testExternalResourceAvroDeserializer,
                    jsonSerializer = TestJsonSerializerCollectionAnime,
                )

                // when
                databaseAccess.fetchEntries()

                // then
                assertThat(hasBeenInvoked).isTrue()
            }
        }
    }

    @Nested
    inner class OfflineDatabaseFileTests {

        @Test
        fun `json database file resides in offlineDatabaseDirectory`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                }

                val databaseAccess = DefaultDatasetFileAccessor(
                    appConfig = testAppConfig,
                    deserializer = TestExternalResourceJsonDeserializerDataset,
                    jsonSerializer = TestJsonSerializerCollectionAnime,
                )

                // when
                val result = databaseAccess.offlineDatabaseFile(JSON).parent

                // then
                assertThat(result).isEqualTo(testAppConfig.outputDirectory())
            }
        }

        @Test
        fun `minified json database file resides in offlineDatabaseDirectory`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                }

                val databaseAccess = DefaultDatasetFileAccessor(
                    appConfig = testAppConfig,
                    deserializer = TestExternalResourceJsonDeserializerDataset,
                    jsonSerializer = TestJsonSerializerCollectionAnime,
                )

                // when
                val result = databaseAccess.offlineDatabaseFile(JSON_MINIFIED).parent

                // then
                assertThat(result).isEqualTo(testAppConfig.outputDirectory())
            }
        }

        @Test
        fun `zipped database file resides in offlineDatabaseDirectory`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                }

                val databaseAccess = DefaultDatasetFileAccessor(
                    appConfig = testAppConfig,
                    deserializer = TestExternalResourceJsonDeserializerDataset,
                    jsonSerializer = TestJsonSerializerCollectionAnime,
                )

                // when
                val result = databaseAccess.offlineDatabaseFile(ZIP).parent

                // then
                assertThat(result).isEqualTo(testAppConfig.outputDirectory())
            }
        }

        @Test
        fun `return correct database file based on type JSON`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                }

                val databaseAccess = DefaultDatasetFileAccessor(
                    appConfig = testAppConfig,
                    deserializer = TestExternalResourceJsonDeserializerDataset,
                    jsonSerializer = TestJsonSerializerCollectionAnime,
                )

                // when
                val result = databaseAccess.offlineDatabaseFile(JSON).fileName.toString()

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
                }

                val databaseAccess = DefaultDatasetFileAccessor(
                    appConfig = testAppConfig,
                    deserializer = TestExternalResourceJsonDeserializerDataset,
                    jsonSerializer = TestJsonSerializerCollectionAnime,
                )

                // when
                val result = databaseAccess.offlineDatabaseFile(JSON_MINIFIED).fileName.toString()

                // then
                assertThat(result).isEqualTo("anime-offline-database-minified.json")
            }
        }

        @Test
        fun `return correct database file based on type ZIP`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                }

                val databaseAccess = DefaultDatasetFileAccessor(
                    appConfig = testAppConfig,
                    deserializer = TestExternalResourceJsonDeserializerDataset,
                    jsonSerializer = TestJsonSerializerCollectionAnime,
                )

                // when
                val result = databaseAccess.offlineDatabaseFile(ZIP).fileName.toString()

                // then
                assertThat(result).isEqualTo("anime-offline-database.zip")
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