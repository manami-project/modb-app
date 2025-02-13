package io.github.manamiproject.modb.app.convfiles

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestMetaDataProviderConfig
import io.github.manamiproject.modb.app.TestPathAnimeConverter
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.waitFor
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.PathAnimeConverter
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.LOCK_FILE_SUFFIX
import io.github.manamiproject.modb.core.extensions.fileName
import io.github.manamiproject.modb.core.anime.AnimeRaw
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.listDirectoryEntries
import kotlin.test.Test
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.toDuration

internal class SimpleConversionWatchServiceTest {

    @Nested
    inner class PrepareTests {

        @Test
        fun `remove existing lock files when starting the watch service`() {
            tempDirectory {
                // given
                val anime = AnimeRaw("Death Note")

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun fileSuffix(): FileSuffix = "html"
                }

                val testConverter = object: PathAnimeConverter by TestPathAnimeConverter {
                    override suspend fun convert(path: Path): List<AnimeRaw> = listOf(anime)
                }

                tempDir.resolve("1535.$LOCK_FILE_SUFFIX").createFile()
                tempDir.resolve("1535.${testConfig.fileSuffix()}").createFile()
                tempDir.resolve("dir.$LOCK_FILE_SUFFIX").createDirectory()

                val simpleConversionWatchService = SimpleConversionWatchService(
                    appConfig = testAppConfig,
                    converter = testConverter,
                    metaDataProviderConfig = testConfig,
                )

                // when
                simpleConversionWatchService.prepare()

                // then
                val dirContent = tempDir.listDirectoryEntries().map { it.fileName() }

                assertThat(dirContent).containsExactlyInAnyOrder(
                    "1535.$CONVERTED_FILE_SUFFIX",
                    "1535.${testConfig.fileSuffix()}",
                    "dir.$LOCK_FILE_SUFFIX",
                )
            }
        }

        @Test
        fun `convert all unconverted files at startup`() {
            tempDirectory {
                // given
                val anime = AnimeRaw("Death Note")

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun fileSuffix(): FileSuffix = "html"
                }

                val testConverter = object: PathAnimeConverter by TestPathAnimeConverter {
                    override suspend fun convert(path: Path): List<AnimeRaw> = listOf(anime)
                }

                tempDir.resolve("1535.${testConfig.fileSuffix()}").createFile()

                val simpleConversionWatchService = SimpleConversionWatchService(
                    appConfig = testAppConfig,
                    converter = testConverter,
                    metaDataProviderConfig = testConfig,
                )

                // when
                simpleConversionWatchService.prepare()

                // then
                val dirContent = tempDir.listDirectoryEntries().map { it.fileName() }

                assertThat(dirContent).containsExactlyInAnyOrder(
                    "1535.$CONVERTED_FILE_SUFFIX",
                    "1535.${testConfig.fileSuffix()}",
                )
            }
        }
    }

    @Nested
    inner class WatchTests {

        @Test
        fun `invoke converter as soon as the lock file is gone`() {
            tempDirectory {
                withContext(LIMITED_FS) {
                    // given
                    var converterHasBeenInvoked = false
                    var canBeInvoked = false

                    val testAppConfig = object : Config by TestAppConfig {
                        override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                    }

                    val testConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                        override fun fileSuffix(): FileSuffix = "html"
                    }

                    val anime = AnimeRaw("Death Note")

                    val testConverter = object : PathAnimeConverter by TestPathAnimeConverter {
                        override suspend fun convert(path: Path): List<AnimeRaw> {
                            if (canBeInvoked) {
                                converterHasBeenInvoked = true
                            }
                            return listOf(anime)
                        }
                    }

                    val simpleConversionWatchService = SimpleConversionWatchService(
                        appConfig = testAppConfig,
                        converter = testConverter,
                        metaDataProviderConfig = testConfig,
                    )

                    val watchService = launch {
                        simpleConversionWatchService.watch()
                    }
                    delay(1500) // allow WatchService to complete the prepare task

                    val lockFile = tempDir.resolve("1535.$LOCK_FILE_SUFFIX").createFile()
                    tempDir.resolve("1535.${testConfig.fileSuffix()}").createFile()

                    // when
                    canBeInvoked = true
                    lockFile.deleteIfExists()

                    // then
                    waitFor(timeout = 10.toDuration(SECONDS)) { converterHasBeenInvoked }
                    simpleConversionWatchService.stop()
                    watchService.cancelAndJoin()

                    assertThat(converterHasBeenInvoked).isTrue()
                }
            }
        }
    }
}