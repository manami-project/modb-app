package io.github.manamiproject.modb.app.postprocessors

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
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeRaw
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
                    override suspend fun allAnime(): List<AnimeRaw> = emptyList()
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
                assertThat(result).hasMessage("No sources in [*.conv] files.")
            }
        }

        @Test
        fun `throws exception if there are no sources in dcs files`() {
            tempDirectory {
                // given
                val anime = AnimeRaw(
                    _title = "Death Note",
                    _sources = hashSetOf(URI("htps://myanimelist.net/anime/1535")),
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
                    override suspend fun allAnime(): List<AnimeRaw> = emptyList()
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
                assertThat(result).hasMessage("No sources in [*.dcs] files.")
            }
        }

        @Test
        fun `throws exception if sources in conv files exist which are not present in dcs files`() {
            tempDirectory {
                // given
                val deathNote = AnimeRaw(
                    _title = "Death Note",
                    _sources = hashSetOf(URI("htps://myanimelist.net/anime/1535")),
                )
                val koeNoKatachi = AnimeRaw(
                    _title = "Koe no Katachi",
                    _sources = hashSetOf(URI("htps://myanimelist.net/anime/28851")),
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
                    override suspend fun allAnime(): List<AnimeRaw> = listOf(
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
                assertThat(result).hasMessage("There are entries existing in [*.conv] files, but not in [*.dcs]. Affected sources: [htps://myanimelist.net/anime/28851]")
            }
        }

        @Test
        fun `throws exception if there are no sources in dataset`() {
            tempDirectory {
                // given
                val deathNote = AnimeRaw(
                    _title = "Death Note",
                    _sources = hashSetOf(URI("htps://myanimelist.net/anime/1535")),
                )
                val koeNoKatachi = AnimeRaw(
                    _title = "Koe no Katachi",
                    _sources = hashSetOf(URI("htps://myanimelist.net/anime/28851")),
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
                    override suspend fun allAnime(): List<AnimeRaw> = listOf(
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
                val deathNoteRaw = AnimeRaw(
                    _title = "Death Note",
                    _sources = hashSetOf(URI("htps://myanimelist.net/anime/1535")),
                )
                val koeNoKatachiRaw = AnimeRaw(
                    _title = "Koe no Katachi",
                    _sources = hashSetOf(URI("htps://myanimelist.net/anime/28851")),
                )

                val deathNoteAnime = Anime(
                    title = "Death Note",
                    sources = hashSetOf(URI("htps://myanimelist.net/anime/1535")),
                )

                Json.toJson(deathNoteRaw).writeToFile(tempDir.resolve("1535.conv"))

                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(): List<AnimeRaw> = listOf(
                        deathNoteRaw,
                        koeNoKatachiRaw
                    )
                }

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override suspend fun fetchEntries(): List<Anime> = listOf(
                        deathNoteAnime,
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
                val deathNoteRaw = AnimeRaw(
                    _title = "Death Note",
                    _sources = hashSetOf(URI("htps://myanimelist.net/anime/1535")),
                )
                val deathNote = Anime(
                    title = "Death Note",
                    sources = hashSetOf(URI("htps://myanimelist.net/anime/1535")),
                )
                val koeNoKatachi = Anime(
                    title = "Koe no Katachi",
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
                    override suspend fun allAnime(): List<AnimeRaw> = listOf(
                        deathNoteRaw,
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
        fun `throws exception if dataset contains multiple sources in one entry of which one doesn't exist in dcs entries`() {
            tempDirectory {
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val otherMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "otherexample.com"
                }

                // given
                val deathNoteRaw = AnimeRaw(
                    _title = "Death Note",
                    _sources = hashSetOf(
                        URI("https://${testMetaDataProviderConfig.hostname()}/anime/1535"),
                        URI("https://${otherMetaDataProviderConfig.hostname()}/anime/40190"),
                    ),
                )

                val deathNote = Anime(
                    title = "Death Note",
                    sources = hashSetOf(
                        URI("https://${testMetaDataProviderConfig.hostname()}/anime/1535"),
                        URI("https://${otherMetaDataProviderConfig.hostname()}/anime/40190"),
                    ),
                )

                Json.toJson(deathNoteRaw).writeToFile(tempDir.resolve("1535.conv"))

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(
                        testMetaDataProviderConfig,
                        otherMetaDataProviderConfig,
                    )
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(): List<AnimeRaw> = listOf(
                        AnimeRaw(
                            _title = "Death Note",
                            _sources = hashSetOf(
                                URI("https://${testMetaDataProviderConfig.hostname()}/anime/1535"),
                            ),
                        )
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
                assertThat(result).hasMessage("There are entries existing in [*.conv] files, but not in [*.dcs]. Affected sources: [https://otherexample.com/anime/40190]")
            }
        }

        @Test
        fun `returns true if dataset contains multiple sources in one entry of which one doesn't exist in dcs entries in case the hostname is to be ignored`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val metaDataProviderConfigToBeIgnored = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "otherexample.com"
                }

                val deathNoteDataset = Anime(
                    title = "Death Note",
                    sources = hashSetOf(
                        URI("https://${testMetaDataProviderConfig.hostname()}/anime/1535"),
                        URI("https://${metaDataProviderConfigToBeIgnored.hostname()}/anime/40190"),
                    ),
                )

                val deathNoteDcsEntry = AnimeRaw(
                    _title = "Death Note",
                    _sources = hashSetOf(
                        URI("https://${testMetaDataProviderConfig.hostname()}/anime/1535"),
                    ),
                )

                Json.toJson(deathNoteDcsEntry).writeToFile(tempDir.resolve("1535.conv"))

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(): List<AnimeRaw> = listOf(deathNoteDcsEntry)
                }

                val testDatasetFileAccessor = object: DatasetFileAccessor by TestDatasetFileAccessor {
                    override suspend fun fetchEntries(): List<Anime> = listOf(
                        deathNoteDataset,
                    )
                }

                val sourcesConsistencyValidationPostProcessor = SourcesConsistencyValidationPostProcessor(
                    appConfig = testAppConfig,
                    datasetFileAccessor = testDatasetFileAccessor,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    ignoreMetaDataConfiguration = setOf(metaDataProviderConfigToBeIgnored),
                )

                // when
                val result = sourcesConsistencyValidationPostProcessor.process()

                // then
                assertThat(result).isTrue()
            }
        }

        @Test
        fun `returns true if everything is valid`() {
            tempDirectory {
                // given
                val deathNoteRaw = AnimeRaw(
                    _title = "Death Note",
                    _sources = hashSetOf(URI("htps://myanimelist.net/anime/1535")),
                )
                val koeNoKatachiRaw = AnimeRaw(
                    _title = "Koe no Katachi",
                    _sources = hashSetOf(URI("htps://myanimelist.net/anime/28851")),
                )

                val deathNote = Anime(
                    title = "Death Note",
                    sources = hashSetOf(URI("htps://myanimelist.net/anime/1535")),
                )
                val koeNoKatachi = Anime(
                    title = "Koe no Katachi",
                    sources = hashSetOf(URI("htps://myanimelist.net/anime/28851")),
                )

                Json.toJson(deathNoteRaw).writeToFile(tempDir.resolve("1535.conv"))

                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun allAnime(): List<AnimeRaw> = listOf(
                        deathNoteRaw,
                        koeNoKatachiRaw
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
                assertThat(result === previous).isTrue()
            }
        }
    }
}