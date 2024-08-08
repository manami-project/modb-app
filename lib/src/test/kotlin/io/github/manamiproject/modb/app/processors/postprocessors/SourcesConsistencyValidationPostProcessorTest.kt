package io.github.manamiproject.modb.app.processors.postprocessors

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestDatasetFileAccessor
import io.github.manamiproject.modb.app.TestDownloadControlStateAccessor
import io.github.manamiproject.modb.app.TestMetaDataProviderConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.dataset.DatasetFileAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.Test

internal class SourcesConsistencyValidationPostProcessorTest {

    @Nested
    inner class ProcessTests {

        @Test
        fun `throws exception if there are no sources in conv files`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(): List<Anime> = emptyList()
                }

                val sourcesConsistencyValidationPostProcessor = SourcesConsistencyValidationPostProcessor(
                    appConfig = testAppConfig,
                    datasetFileAccessor = TestDatasetFileAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    sourcesConsistencyValidationPostProcessor.process()
                }

                // then
                assertThat(result).hasMessage("No sources in [*.conv] files found.")
            }
        }

        @Test
        fun `throws exception if there are no sources in dcs files`() {
            tempDirectory {
                // given
                val anime = Anime(
                    _title = "Death Note",
                    sources = hashSetOf(URI("htps://myanimelist.net/anime/1535")),
                )

                Json.toJson(anime).writeToFile(tempDir.resolve("1535.conv"))

                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(): List<Anime> = emptyList()
                }

                val sourcesConsistencyValidationPostProcessor = SourcesConsistencyValidationPostProcessor(
                    appConfig = testAppConfig,
                    datasetFileAccessor = TestDatasetFileAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    sourcesConsistencyValidationPostProcessor.process()
                }

                // then
                assertThat(result).hasMessage("No sources in [*.dcs] files found.")
            }
        }

        @Test
        fun `throws exception if sources in conv files exist which are not present in dcs files`() {
            tempDirectory {
                // given
                val deathNote = Anime(
                    _title = "Death Note",
                    sources = hashSetOf(URI("htps://myanimelist.net/anime/1535")),
                )
                val koeNoKatachi = Anime(
                    _title = "Koe no Katachi",
                    sources = hashSetOf(URI("htps://myanimelist.net/anime/28851")),
                )

                Json.toJson(deathNote).writeToFile(tempDir.resolve("1535.conv"))
                Json.toJson(koeNoKatachi).writeToFile(tempDir.resolve("28851.conv"))

                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(): List<Anime> = listOf(
                        deathNote,
                    )
                }

                val sourcesConsistencyValidationPostProcessor = SourcesConsistencyValidationPostProcessor(
                    appConfig = testAppConfig,
                    datasetFileAccessor = TestDatasetFileAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    sourcesConsistencyValidationPostProcessor.process()
                }

                // then
                assertThat(result).hasMessage("There are entries existing in [*.conv] files, but not in [*.dcs] files: [htps://myanimelist.net/anime/28851]")
            }
        }

        @Test
        fun `throws exception if there are no sources in dataset`() {
            tempDirectory {
                // given
                val deathNote = Anime(
                    _title = "Death Note",
                    sources = hashSetOf(URI("htps://myanimelist.net/anime/1535")),
                )
                val koeNoKatachi = Anime(
                    _title = "Koe no Katachi",
                    sources = hashSetOf(URI("htps://myanimelist.net/anime/28851")),
                )

                Json.toJson(deathNote).writeToFile(tempDir.resolve("1535.conv"))

                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(): List<Anime> = listOf(
                        deathNote,
                        koeNoKatachi
                    )
                }

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override suspend fun fetchEntries(): List<Anime> = emptyList()
                }

                val sourcesConsistencyValidationPostProcessor = SourcesConsistencyValidationPostProcessor(
                    appConfig = testAppConfig,
                    datasetFileAccessor = testDatasetFileAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    sourcesConsistencyValidationPostProcessor.process()
                }

                // then
                assertThat(result).hasMessage("No sources in dataset found.")
            }
        }

        @Test
        fun `throws exception if dcs entries contains sources which are not present in dataset`() {
            tempDirectory {
                // given
                val deathNote = Anime(
                    _title = "Death Note",
                    sources = hashSetOf(URI("htps://myanimelist.net/anime/1535")),
                )
                val koeNoKatachi = Anime(
                    _title = "Koe no Katachi",
                    sources = hashSetOf(URI("htps://myanimelist.net/anime/28851")),
                )

                Json.toJson(deathNote).writeToFile(tempDir.resolve("1535.conv"))

                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(): List<Anime> = listOf(
                        deathNote,
                        koeNoKatachi
                    )
                }

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override suspend fun fetchEntries(): List<Anime> = listOf(
                        deathNote,
                    )
                }

                val sourcesConsistencyValidationPostProcessor = SourcesConsistencyValidationPostProcessor(
                    appConfig = testAppConfig,
                    datasetFileAccessor = testDatasetFileAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    sourcesConsistencyValidationPostProcessor.process()
                }

                // then
                assertThat(result).hasMessage("Sources in dataset and sources in DCS entries differ: [htps://myanimelist.net/anime/28851]")
            }
        }

        @Test
        fun `throws exception if dataset contains sources which don't exist in dcs entries`() {
            tempDirectory {
                // given
                val deathNote = Anime(
                    _title = "Death Note",
                    sources = hashSetOf(URI("htps://myanimelist.net/anime/1535")),
                )
                val koeNoKatachi = Anime(
                    _title = "Koe no Katachi",
                    sources = hashSetOf(URI("htps://myanimelist.net/anime/28851")),
                )

                Json.toJson(deathNote).writeToFile(tempDir.resolve("1535.conv"))

                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(): List<Anime> = listOf(
                        deathNote,
                    )
                }

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override suspend fun fetchEntries(): List<Anime> = listOf(
                        deathNote,
                        koeNoKatachi
                    )
                }

                val sourcesConsistencyValidationPostProcessor = SourcesConsistencyValidationPostProcessor(
                    appConfig = testAppConfig,
                    datasetFileAccessor = testDatasetFileAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    sourcesConsistencyValidationPostProcessor.process()
                }

                // then
                assertThat(result).hasMessage("Sources in dataset and sources in DCS entries differ: [htps://myanimelist.net/anime/28851]")
            }
        }

        @Test
        fun `returns true if everything is valid`() {
            tempDirectory {
                // given
                val deathNote = Anime(
                    _title = "Death Note",
                    sources = hashSetOf(URI("htps://myanimelist.net/anime/1535")),
                )
                val koeNoKatachi = Anime(
                    _title = "Koe no Katachi",
                    sources = hashSetOf(URI("htps://myanimelist.net/anime/28851")),
                )

                Json.toJson(deathNote).writeToFile(tempDir.resolve("1535.conv"))

                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(): List<Anime> = listOf(
                        deathNote,
                        koeNoKatachi
                    )
                }

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override suspend fun fetchEntries(): List<Anime> = listOf(
                        deathNote,
                        koeNoKatachi
                    )
                }

                val sourcesConsistencyValidationPostProcessor = SourcesConsistencyValidationPostProcessor(
                    appConfig = testAppConfig,
                    datasetFileAccessor = testDatasetFileAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                val result = sourcesConsistencyValidationPostProcessor.process()

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
                val previous = SourcesConsistencyValidationPostProcessor.instance

                // when
                val result = SourcesConsistencyValidationPostProcessor.instance

                // then
                assertThat(result).isExactlyInstanceOf(SourcesConsistencyValidationPostProcessor::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}