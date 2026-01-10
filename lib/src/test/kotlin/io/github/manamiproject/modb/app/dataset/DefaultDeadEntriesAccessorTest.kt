package io.github.manamiproject.modb.app.dataset

import io.github.manamiproject.AnimenewsnetworkConfig
import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import io.github.manamiproject.modb.anisearch.AnisearchConfig
import io.github.manamiproject.modb.app.*
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.dataset.DatasetFileType.*
import io.github.manamiproject.modb.app.downloadcontrolstate.DOWNLOAD_CONTROL_STATE_FILE_SUFFIX
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.core.anime.AnimeRaw
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.livechart.LivechartConfig
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import io.github.manamiproject.modb.serde.json.deserializer.DeadEntriesFromInputStreamDeserializer
import io.github.manamiproject.modb.serde.json.deserializer.Deserializer
import io.github.manamiproject.modb.serde.json.deserializer.FromRegularFileDeserializer
import io.github.manamiproject.modb.serde.json.models.DeadEntries
import io.github.manamiproject.modb.serde.json.serializer.DeadEntriesJsonSerializer
import io.github.manamiproject.modb.simkl.SimklConfig
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE
import org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.test.Test

internal class DefaultDeadEntriesAccessorTest {

    @Nested
    inner class DeadEntriesFileTests {

        @Test
        fun `throws exception if metadata provider is not supported`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val defaultDatasetFileAccessor = DefaultDeadEntriesAccessor(
                    appConfig = TestAppConfig,
                    jsonDeserializer = TestDeserializer(),
                    jsonSerializer = TestJsonSerializer(),
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                )

                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    defaultDatasetFileAccessor.deadEntriesFile(testMetaDataProviderConfig, JSON_MINIFIED).parent
                }

                // then
                assertThat(result).hasMessage("Metadata provider [example.org] doesn't support dead entry files.")
            }
        }

        @ParameterizedTest
        @EnumSource(value = DatasetFileType::class, mode = EXCLUDE, names = ["JSON_LINES", "JSON_LINES_ZST"])
        fun `file resides in offlineDatabaseDirectory`(value: DatasetFileType) {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = MyanimelistConfig

                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                }

                val defaultDatasetFileAccessor = DefaultDeadEntriesAccessor(
                    appConfig = testAppConfig,
                    jsonDeserializer = TestDeserializer(),
                    jsonSerializer = TestJsonSerializer(),
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                )

                // when
                val result = defaultDatasetFileAccessor.deadEntriesFile(testMetaDataProviderConfig, value).parent

                // then
                assertThat(result).isEqualTo(testAppConfig.outputDirectory().resolve("dead-entries"))
            }
        }

        @Test
        fun `deadEntriesFile returns correct file for type JSON_MINIFIED`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = MyanimelistConfig

                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                }

                val databaseAccess = DefaultDeadEntriesAccessor(
                    appConfig = testAppConfig,
                    jsonDeserializer = TestDeserializer(),
                    jsonSerializer = TestJsonSerializer(),
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                )

                // when
                val result = databaseAccess.deadEntriesFile(testMetaDataProviderConfig, JSON_MINIFIED).fileName()

                // then
                assertThat(result).isEqualTo("myanimelist-minified.json")
            }
        }

        @Test
        fun `deadEntriesFile returns correct file for type JSON_MINIFIED_ZST`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = MyanimelistConfig

                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                }

                val databaseAccess = DefaultDeadEntriesAccessor(
                    appConfig = testAppConfig,
                    jsonDeserializer = TestDeserializer(),
                    jsonSerializer = TestJsonSerializer(),
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                )

                // when
                val result = databaseAccess.deadEntriesFile(testMetaDataProviderConfig, JSON_MINIFIED_ZST).fileName()

                // then
                assertThat(result).isEqualTo("myanimelist-minified.json.zst")
            }
        }

        @ParameterizedTest
        @EnumSource(value = DatasetFileType::class, mode = INCLUDE, names = ["JSON_LINES", "JSON_LINES_ZST"])
        fun `throws exception for type JSON lines types`(input: DatasetFileType) {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = MyanimelistConfig

                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                }

                val databaseAccess = DefaultDeadEntriesAccessor(
                    appConfig = testAppConfig,
                    jsonDeserializer = TestDeserializer(),
                    jsonSerializer = TestJsonSerializer(),
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                )

                // when
                val result = exceptionExpected<UnsupportedOperationException> {
                    databaseAccess.deadEntriesFile(testMetaDataProviderConfig, input).fileName()
                }

                // then
                assertThat(result).hasMessage("Dead entries don't support JSON line format.")
            }
        }

        @Test
        fun `creates directory if it doesn't exist yet`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = MyanimelistConfig

                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                }

                val databaseAccess = DefaultDeadEntriesAccessor(
                    appConfig = testAppConfig,
                    jsonDeserializer = TestDeserializer(),
                    jsonSerializer = TestJsonSerializer(),
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                )

                val directory = testAppConfig.outputDirectory().resolve("dead-entries")
                val directoryExistsBefore = directory.directoryExists()

                // when
                databaseAccess.deadEntriesFile(testMetaDataProviderConfig, JSON_MINIFIED).fileName()

                // then
                assertThat(directoryExistsBefore).isFalse()
                assertThat(directory).exists()
                assertThat(directory).isDirectory()
            }
        }
    }

    @Nested
    inner class AddDeadEntryTests {

        @ParameterizedTest
        @ValueSource(classes = [
            AnidbConfig::class,
            AnilistConfig::class,
            AnimenewsnetworkConfig::class,
            KitsuConfig::class,
            MyanimelistConfig::class,
        ])
        fun `successfully save dead entry`(configClass: Class<*>) {
            tempDirectory {
                // given
                val id = "123456789"
                val testMetaDataProviderConfig = configClass.kotlin.objectInstance as MetaDataProviderConfig

                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) { }
                }

                val testDeserializer = object: Deserializer<RegularFile, DeadEntries> by TestDeserializer() {
                    override suspend fun deserialize(source: RegularFile): DeadEntries = DeadEntries(
                        `$schema` = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/heads/master/dead-entries/dead-entries.schema.json"),
                        lastUpdate = "2024-08-01",
                        deadEntries = emptyList(),
                    )
                }

                val deadEntriesAccessor = DefaultDeadEntriesAccessor(
                    appConfig = testAppConfig,
                    jsonDeserializer = testDeserializer,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                deadEntriesAccessor.addDeadEntry(id, testMetaDataProviderConfig)

                // then
                setOf(JSON_MINIFIED, JSON_MINIFIED).forEach {
                    val result = FromRegularFileDeserializer(
                        deserializer = DeadEntriesFromInputStreamDeserializer.instance).deserialize(deadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig, it),
                    ).deadEntries
                    assertThat(result).containsExactly(
                        id,
                    )
                }
                assertThat(deadEntriesAccessor.fetchDeadEntries(testMetaDataProviderConfig)).containsExactly(
                    id,
                )
            }
        }

        @ParameterizedTest
        @ValueSource(classes = [
            AnidbConfig::class,
            AnilistConfig::class,
            AnimenewsnetworkConfig::class,
            KitsuConfig::class,
            MyanimelistConfig::class,
        ])
        fun `zst file is not written, because it will be created in a post-processor due to the processing time on the highest compression level`(configClass: Class<*>) {
            tempDirectory {
                // given
                val id = "123456789"
                val testMetaDataProviderConfig = configClass.kotlin.objectInstance as MetaDataProviderConfig

                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) { }
                }

                val testDeserializer = object: Deserializer<RegularFile, DeadEntries> by TestDeserializer() {
                    override suspend fun deserialize(source: RegularFile): DeadEntries = DeadEntries(
                        `$schema` = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/heads/master/dead-entries/dead-entries.schema.json"),
                        lastUpdate = "2024-08-01",
                        deadEntries = emptyList(),
                    )
                }

                val deadEntriesAccessor = DefaultDeadEntriesAccessor(
                    appConfig = testAppConfig,
                    jsonDeserializer = testDeserializer,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                deadEntriesAccessor.addDeadEntry(id, testMetaDataProviderConfig)

                // then
                assertThat(deadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig, JSON_MINIFIED_ZST)).doesNotExist()
            }
        }

        @ParameterizedTest
        @ValueSource(classes = [
            AnidbConfig::class,
            AnilistConfig::class,
            AnimenewsnetworkConfig::class,
            KitsuConfig::class,
            MyanimelistConfig::class,
        ])
        fun `appends new entries by overriding existing file`(configClass: Class<*>) {
            tempDirectory {
                // given
                val initialId = "123456789"
                val newId = "987654321"
                val testMetaDataProviderConfig = configClass.kotlin.objectInstance as MetaDataProviderConfig

                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) { }
                }

                val deadEntriesDir = tempDir.resolve("dead-entries").createDirectory()
                val minified = deadEntriesDir.resolve("${testMetaDataProviderConfig.hostname().substringBefore('.')}-minified.json")

                DeadEntriesJsonSerializer.instance.serialize(listOf(
                    initialId,
                ), minify = true).writeToFile(minified)

                val deadEntriesAccessor = DefaultDeadEntriesAccessor(
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                deadEntriesAccessor.addDeadEntry(newId, testMetaDataProviderConfig)

                // then
                setOf(JSON_MINIFIED, JSON_MINIFIED).forEach {
                    val result = FromRegularFileDeserializer(
                        deserializer = DeadEntriesFromInputStreamDeserializer.instance).deserialize(deadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig, it),
                    ).deadEntries
                    assertThat(result).containsExactly(
                        initialId,
                        newId,
                    )
                }
                assertThat(deadEntriesAccessor.fetchDeadEntries(testMetaDataProviderConfig)).containsExactly(
                    initialId,
                    newId,
                )
            }
        }

        @ParameterizedTest
        @ValueSource(classes = [
            AnidbConfig::class,
            AnilistConfig::class,
            AnimenewsnetworkConfig::class,
            KitsuConfig::class,
            MyanimelistConfig::class,
        ])
        fun `prevent duplicates`(configClass: Class<*>) {
            tempDirectory {
                // given
                val initialId = "123456789"
                val testMetaDataProviderConfig = configClass.kotlin.objectInstance as MetaDataProviderConfig

                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) { }
                }

                val deadEntriesDir = tempDir.resolve("dead-entries").createDirectory()
                val minified = deadEntriesDir.resolve("${testMetaDataProviderConfig.hostname().substringBefore('.')}-minified.json")
                val minifiedZst = deadEntriesDir.resolve("${testMetaDataProviderConfig.hostname().substringBefore('.')}-minified.json.zst")


                DeadEntriesJsonSerializer.instance.serialize(listOf(
                    initialId,
                ), minify = true).writeToFile(minified)
                DeadEntriesJsonSerializer.instance.serialize(listOf(
                    initialId,
                ), minify = true).writeToZstandardFile(minifiedZst)

                val deadEntriesAccessor = DefaultDeadEntriesAccessor(
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                deadEntriesAccessor.addDeadEntry(initialId, testMetaDataProviderConfig)

                // then
                DatasetFileType.entries.filterNot { it.name.contains("JSON_LINES") }.forEach {
                    val result = FromRegularFileDeserializer(
                        deserializer = DeadEntriesFromInputStreamDeserializer.instance).deserialize(deadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig, it),
                    ).deadEntries
                    assertThat(result).containsExactly(
                        initialId,
                    )
                }
                assertThat(deadEntriesAccessor.fetchDeadEntries(testMetaDataProviderConfig)).containsExactly(
                    initialId,
                )
            }
        }

        @ParameterizedTest
        @ValueSource(classes = [
            AnidbConfig::class,
            AnilistConfig::class,
            AnimenewsnetworkConfig::class,
            KitsuConfig::class,
            MyanimelistConfig::class,
        ])
        fun `creates a new dead entries file if the given does not exist`(configClass: Class<*>) {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = configClass.kotlin.objectInstance as MetaDataProviderConfig

                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) { }
                }

                val testDeserializer = object: Deserializer<RegularFile, DeadEntries> by TestDeserializer() {
                    override suspend fun deserialize(source: RegularFile): DeadEntries = DeadEntries(
                        `$schema` = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/heads/master/dead-entries/dead-entries.schema.json"),
                        lastUpdate = "2024-08-01",
                        deadEntries = emptyList(),
                    )
                }

                val deadEntriesAccessor = DefaultDeadEntriesAccessor(
                    jsonDeserializer = testDeserializer,
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                deadEntriesAccessor.addDeadEntry("some-id", testMetaDataProviderConfig)

                // then
                assertThat(deadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig, JSON_MINIFIED)).exists()
                assertThat(deadEntriesAccessor.deadEntriesFile(testMetaDataProviderConfig, JSON_MINIFIED)).exists()
            }
        }

        @ParameterizedTest
        @ValueSource(classes = [
            AnidbConfig::class,
            AnilistConfig::class,
            AnimenewsnetworkConfig::class,
            KitsuConfig::class,
            MyanimelistConfig::class,
        ])
        fun `removes the corresponding file from download control state`(configClass: Class<*>) {
            tempDirectory {
                // given
                val testMetaDataProviderConfig: MetaDataProviderConfig = configClass.kotlin.objectInstance as MetaDataProviderConfig

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun outputDirectory(): Directory = tempDir
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                }

                var hasBeenInvoked = false
                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) {
                        hasBeenInvoked = true
                    }
                }

                val testDeserializer = object: Deserializer<RegularFile, DeadEntries> by TestDeserializer() {
                    override suspend fun deserialize(source: RegularFile): DeadEntries = DeadEntries(
                        `$schema` = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/heads/master/dead-entries/dead-entries.schema.json"),
                        lastUpdate = "2024-08-01",
                        deadEntries = emptyList(),
                    )
                }

                val deadEntriesAccessor = DefaultDeadEntriesAccessor(
                    jsonDeserializer = testDeserializer,
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                deadEntriesAccessor.addDeadEntry("some-id", testMetaDataProviderConfig)

                // then
                assertThat(hasBeenInvoked).isTrue()
            }
        }

        @ParameterizedTest
        @ValueSource(classes = [
            AnimePlanetConfig::class,
            AnisearchConfig::class,
            LivechartConfig::class,
            SimklConfig::class,
        ])
        fun `doesn't create a dead entry for unsupported metadata provider, because either all IDs are collected upfront or download is done via pagination`(configClass: Class<*>) {
            tempDirectory {
                // given
                val testMetaDataProviderConfig: MetaDataProviderConfig = configClass.kotlin.objectInstance as MetaDataProviderConfig

                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) { }
                }

                val deadEntriesAccessor = DefaultDeadEntriesAccessor(
                    jsonSerializer = TestJsonSerializer(),
                    jsonDeserializer = TestDeserializer(),
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                deadEntriesAccessor.addDeadEntry("an-id", testMetaDataProviderConfig)

                // then
                assertThat(tempDir.resolve("dead-entries")).doesNotExist()
            }
        }

        @ParameterizedTest
        @ValueSource(classes = [
            AnimePlanetConfig::class,
            AnisearchConfig::class,
            LivechartConfig::class,
            SimklConfig::class,
        ])
        fun `invokes removal of dcs file for unsupported metadata provider`(configClass: Class<*>) {
            tempDirectory {
                // given
                val testMetaDataProviderConfig: MetaDataProviderConfig = configClass.kotlin.objectInstance as MetaDataProviderConfig

                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                }

                var removingDcsEntryHasBeenInvoked = false
                var invokedId = EMPTY
                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) {
                        removingDcsEntryHasBeenInvoked = true
                        invokedId = animeId
                    }
                }

                val deadEntriesAccessor = DefaultDeadEntriesAccessor(
                    jsonDeserializer = TestDeserializer(),
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                deadEntriesAccessor.addDeadEntry("an-id", testMetaDataProviderConfig)

                // then
                assertThat(removingDcsEntryHasBeenInvoked).isTrue()
                assertThat(invokedId).isEqualTo("an-id")
            }
        }

        @ParameterizedTest
        @ValueSource(classes = [
            AnidbConfig::class,
            AnilistConfig::class,
            AnimenewsnetworkConfig::class,
            KitsuConfig::class,
            MyanimelistConfig::class,
        ])
        fun `triggers initialization if necessary`(configClass: Class<*>) {
            tempDirectory {
                // given
                val id = "123456789"
                val testMetaDataProviderConfig = configClass.kotlin.objectInstance as MetaDataProviderConfig

                var initHasBeenInvoked = false
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        check(!initHasBeenInvoked)
                        initHasBeenInvoked = true
                        return setOf(testMetaDataProviderConfig)
                    }
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) { }
                }

                val testDeserializer = object: Deserializer<RegularFile, DeadEntries> by TestDeserializer() {
                    override suspend fun deserialize(source: RegularFile): DeadEntries = DeadEntries(
                        `$schema` = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/heads/master/dead-entries/dead-entries.schema.json"),
                        lastUpdate = "2024-08-01",
                        deadEntries = emptyList(),
                    )
                }

                val deadEntriesAccessor = DefaultDeadEntriesAccessor(
                    appConfig = testAppConfig,
                    jsonDeserializer = testDeserializer,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                deadEntriesAccessor.addDeadEntry(id, testMetaDataProviderConfig)

                // then
                assertThat(initHasBeenInvoked).isTrue()
            }
        }

        @ParameterizedTest
        @ValueSource(classes = [
            AnidbConfig::class,
            AnilistConfig::class,
            AnimenewsnetworkConfig::class,
            KitsuConfig::class,
            MyanimelistConfig::class,
        ])
        fun `doesn't trigger init if it has already been triggered`(configClass: Class<*>) {
            tempDirectory {
                // given
                val id = "123456789"
                val testMetaDataProviderConfig = configClass.kotlin.objectInstance as MetaDataProviderConfig

                var initHasBeenInvoked = 0
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        initHasBeenInvoked++
                        return setOf(testMetaDataProviderConfig)
                    }
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) { }
                }

                val testDeserializer = object: Deserializer<RegularFile, DeadEntries> by TestDeserializer() {
                    override suspend fun deserialize(source: RegularFile): DeadEntries = DeadEntries(
                        `$schema` = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/heads/master/dead-entries/dead-entries.schema.json"),
                        lastUpdate = "2024-08-01",
                        deadEntries = emptyList(),
                    )
                }

                val deadEntriesAccessor = DefaultDeadEntriesAccessor(
                    appConfig = testAppConfig,
                    jsonDeserializer = testDeserializer,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                deadEntriesAccessor.addDeadEntry(id, testMetaDataProviderConfig)
                val valueBefore = initHasBeenInvoked

                // when
                deadEntriesAccessor.addDeadEntry(id, testMetaDataProviderConfig)

                // then
                assertThat(valueBefore).isOne()
                assertThat(initHasBeenInvoked).isOne()
            }
        }
    }

    @Nested
    inner class DetermineDeadEntriesTests {

        @Test
        fun `throws exception in case of an unknown hostname in sources`() {
            tempDirectory {
                // given
                val ids = listOf(
                    URI("http://example.org/anime/1535"),
                )

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                }

                val deadEntryFinder = DefaultDeadEntriesAccessor(
                    jsonDeserializer = TestDeserializer(),
                    jsonSerializer = TestJsonSerializer(),
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                )

                // when
                val result = assertThrows<IllegalArgumentException> {
                    deadEntryFinder.determineDeadEntries(ids)
                }

                // then
                assertThat(result).hasMessage("Unable to fetch dead entries: No case defined for [example.org].")
            }
        }

        @ParameterizedTest
        @ValueSource(classes = [
            AnidbConfig::class,
            AnilistConfig::class,
            AnimenewsnetworkConfig::class,
            KitsuConfig::class,
            MyanimelistConfig::class,
        ])
        fun `returns a list containing the given ID, because a corresponding id listed in dead entries file`(configClass: Class<*>) {
            tempDirectory {
                // given
                val id = "10001"
                val testMetaDataProviderConfig = configClass.kotlin.objectInstance as MetaDataProviderConfig

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = testMetaDataProviderConfig
                    override fun outputDirectory(): Directory = tempDir
                }

                val ids = listOf(testMetaDataProviderConfig.buildAnimeLink(id))

                DeadEntriesJsonSerializer.instance.serialize(listOf(
                    id,
                )).writeToFile(tempDir.resolve("dead-entries").createDirectory().resolve("${testMetaDataProviderConfig.hostname().substringBefore('.')}-minified.json"))

                val deadEntriesAccessor = DefaultDeadEntriesAccessor(
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    jsonSerializer = TestJsonSerializer(),
                )

                // when
                val result = deadEntriesAccessor.determineDeadEntries(ids)

                // then
                assertThat(result).containsExactly(testMetaDataProviderConfig.buildAnimeLink(id))
            }
        }

        @ParameterizedTest
        @ValueSource(classes = [
            AnimePlanetConfig::class,
            AnisearchConfig::class,
            LivechartConfig::class,
            SimklConfig::class,
        ])
        fun `marks an URI as dead entry for unsupported metadata providers if the corresponding DCS file doesn't exist`(configClass: Class<*>)   {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = configClass.kotlin.objectInstance as MetaDataProviderConfig

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun outputDirectory(): Directory = tempDir
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = testMetaDataProviderConfig
                }

                val existingEntry = testMetaDataProviderConfig.buildAnimeLink("1535")
                val deadEntry = testMetaDataProviderConfig.buildAnimeLink("9999")

                val dcsSubDirectory = testAppConfig.downloadControlStateDirectory().resolve(existingEntry.host).createDirectory()
                val dcsFile = dcsSubDirectory.resolve("1535.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val anime = AnimeRaw(
                    _title = "Test",
                    _sources = hashSetOf(existingEntry),
                )
                Json.toJson(anime).writeToFile(dcsFile)

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = dcsSubDirectory
                }

                val deadEntriesAccessor = DefaultDeadEntriesAccessor(
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                val result = deadEntriesAccessor.determineDeadEntries(
                    setOf(
                        existingEntry,
                        deadEntry,
                    )
                )

                // then
                assertThat(result).containsExactly(deadEntry)
            }
        }

        @ParameterizedTest
        @ValueSource(classes = [
            AnidbConfig::class, // supportd
            AnilistConfig::class, // supportd
            KitsuConfig::class, // supportd
            MyanimelistConfig::class, // supportd
            AnimenewsnetworkConfig::class, // supportd
            AnimePlanetConfig::class, // unsupported
            AnisearchConfig::class, // unsupported
            LivechartConfig::class, // unsupported
            SimklConfig::class, // unsupported
        ])
        fun `triggers initialization if necessary`(configClass: Class<*>) {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = configClass.kotlin.objectInstance as MetaDataProviderConfig

                var initHasBeenInvoked = false
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        check(!initHasBeenInvoked)
                        initHasBeenInvoked = true
                        return setOf(testMetaDataProviderConfig)
                    }
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = testMetaDataProviderConfig
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) { }
                }

                val testDeserializer = object: Deserializer<RegularFile, DeadEntries> by TestDeserializer() {
                    override suspend fun deserialize(source: RegularFile): DeadEntries = DeadEntries(
                        `$schema` = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/heads/master/dead-entries/dead-entries.schema.json"),
                        lastUpdate = "2024-08-01",
                        deadEntries = emptyList(),
                    )
                }

                val deadEntriesAccessor = DefaultDeadEntriesAccessor(
                    appConfig = testAppConfig,
                    jsonDeserializer = testDeserializer,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    jsonSerializer = TestJsonSerializer(),
                )

                // when
                deadEntriesAccessor.determineDeadEntries(setOf(testMetaDataProviderConfig.buildAnimeLink("1535")))

                // then
                assertThat(initHasBeenInvoked).isTrue()
            }
        }

        @ParameterizedTest
        @ValueSource(classes = [
            AnidbConfig::class, // supportd
            AnilistConfig::class, // supportd
            KitsuConfig::class, // supportd
            MyanimelistConfig::class, // supportd
            AnimenewsnetworkConfig::class, // supportd
            AnimePlanetConfig::class, // unsupported
            AnisearchConfig::class, // unsupported
            LivechartConfig::class, // unsupported
            SimklConfig::class, // unsupported
        ])
        fun `doesn't trigger init if it has already been triggered`(configClass: Class<*>) {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = configClass.kotlin.objectInstance as MetaDataProviderConfig

                var initHasBeenInvoked = 0
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        initHasBeenInvoked++
                        return setOf(testMetaDataProviderConfig)
                    }
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = testMetaDataProviderConfig
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) { }
                    override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testDeserializer = object: Deserializer<RegularFile, DeadEntries> by TestDeserializer() {
                    override suspend fun deserialize(source: RegularFile): DeadEntries = DeadEntries(
                        `$schema` = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/heads/master/dead-entries/dead-entries.schema.json"),
                        lastUpdate = "2024-08-01",
                        deadEntries = emptyList(),
                    )
                }

                val deadEntriesAccessor = DefaultDeadEntriesAccessor(
                    appConfig = testAppConfig,
                    jsonDeserializer = testDeserializer,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    jsonSerializer = TestJsonSerializer(),
                )

                val sources = setOf(testMetaDataProviderConfig.buildAnimeLink("1535"))

                deadEntriesAccessor.determineDeadEntries(sources)
                val valueBefore = initHasBeenInvoked

                // when
                deadEntriesAccessor.determineDeadEntries(sources)

                // then
                assertThat(valueBefore).isOne()
                assertThat(initHasBeenInvoked).isOne()
            }
        }
    }

    @Nested
    inner class FetchDeadEntriesTests {

        @ParameterizedTest
        @ValueSource(classes = [
            AnimePlanetConfig::class,
            AnisearchConfig::class,
            LivechartConfig::class,
            SimklConfig::class,
        ])
        fun `throws exception for unsupported metadata provider`(configClass: Class<*>) {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = configClass.kotlin.objectInstance as MetaDataProviderConfig

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                    override fun outputDirectory(): Directory = tempDir
                }

                val deadEntriesAccessor = DefaultDeadEntriesAccessor(
                    jsonDeserializer = TestDeserializer(),
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    jsonSerializer = TestJsonSerializer(),
                )

                // when
                val result = assertThrows<IllegalArgumentException> {
                    deadEntriesAccessor.fetchDeadEntries(testMetaDataProviderConfig)
                }

                // then
                assertThat(result).hasMessage("Metadata provider [${testMetaDataProviderConfig.hostname()}] is not supported.")
            }
        }

        @ParameterizedTest
        @ValueSource(classes = [
            AnidbConfig::class,
            AnilistConfig::class,
            AnimenewsnetworkConfig::class,
            KitsuConfig::class,
            MyanimelistConfig::class,
        ])
        fun `correctly parse file and fetch dead entries`(configClass: Class<*>) {
            tempDirectory {
                // given
                val entry1 = "11"
                val entry2 = "12"
                val testMetaDataProviderConfig = configClass.kotlin.objectInstance as MetaDataProviderConfig

                val deadEntriesFile = tempDir.resolve("dead-entries").createDirectory().resolve("${testMetaDataProviderConfig.hostname().substringBefore('.')}-minified.json")
                DeadEntriesJsonSerializer.instance.serialize(listOf(
                    entry1,
                    entry2,
                )).writeToFile(deadEntriesFile)

                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                }

                val deadEntriesAccessor = DefaultDeadEntriesAccessor(
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = TestDownloadControlStateAccessor,
                    jsonSerializer = TestJsonSerializer(),
                )

                // when
                val result = deadEntriesAccessor.fetchDeadEntries(testMetaDataProviderConfig)

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    entry1,
                    entry2,
                )
            }
        }

        @ParameterizedTest
        @ValueSource(classes = [
            AnidbConfig::class,
            AnilistConfig::class,
            AnimenewsnetworkConfig::class,
            KitsuConfig::class,
            MyanimelistConfig::class,
        ])
        fun `triggers initialization if necessary`(configClass: Class<*>) {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = configClass.kotlin.objectInstance as MetaDataProviderConfig

                var initHasBeenInvoked = false
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        check(!initHasBeenInvoked)
                        initHasBeenInvoked = true
                        return setOf(testMetaDataProviderConfig)
                    }
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) { }
                }

                val testDeserializer = object: Deserializer<RegularFile, DeadEntries> by TestDeserializer() {
                    override suspend fun deserialize(source: RegularFile): DeadEntries = DeadEntries(
                        `$schema` = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/heads/master/dead-entries/dead-entries.schema.json"),
                        lastUpdate = "2024-08-01",
                        deadEntries = emptyList(),
                    )
                }

                val deadEntriesAccessor = DefaultDeadEntriesAccessor(
                    appConfig = testAppConfig,
                    jsonDeserializer = testDeserializer,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    jsonSerializer = TestJsonSerializer(),
                )

                // when
                deadEntriesAccessor.fetchDeadEntries(testMetaDataProviderConfig)

                // then
                assertThat(initHasBeenInvoked).isTrue()
            }
        }

        @ParameterizedTest
        @ValueSource(classes = [
            AnidbConfig::class,
            AnilistConfig::class,
            AnimenewsnetworkConfig::class,
            KitsuConfig::class,
            MyanimelistConfig::class,
        ])
        fun `doesn't trigger init if it has already been triggered`(configClass: Class<*>) {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = configClass.kotlin.objectInstance as MetaDataProviderConfig

                var initHasBeenInvoked = 0
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        initHasBeenInvoked++
                        return setOf(testMetaDataProviderConfig)
                    }
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) { }
                }

                val testDeserializer = object: Deserializer<RegularFile, DeadEntries> by TestDeserializer() {
                    override suspend fun deserialize(source: RegularFile): DeadEntries = DeadEntries(
                        `$schema` = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/heads/master/dead-entries/dead-entries.schema.json"),
                        lastUpdate = "2024-08-01",
                        deadEntries = emptyList(),
                    )
                }

                val deadEntriesAccessor = DefaultDeadEntriesAccessor(
                    appConfig = testAppConfig,
                    jsonDeserializer = testDeserializer,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                    jsonSerializer = TestJsonSerializer(),
                )

                deadEntriesAccessor.fetchDeadEntries(testMetaDataProviderConfig)
                val valueBefore = initHasBeenInvoked

                // when
                deadEntriesAccessor.fetchDeadEntries(testMetaDataProviderConfig)

                // then
                assertThat(valueBefore).isOne()
                assertThat(initHasBeenInvoked).isOne()
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = DefaultDeadEntriesAccessor.instance

                // when
                val result = DefaultDeadEntriesAccessor.instance

                // then
                assertThat(result).isExactlyInstanceOf(DefaultDeadEntriesAccessor::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}