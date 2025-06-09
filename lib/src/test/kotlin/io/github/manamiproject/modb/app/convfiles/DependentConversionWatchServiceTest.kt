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
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.core.anime.AnimeRaw
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.shouldNotBeInvoked
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


internal class DependentConversionWatchServiceTest {

    @Nested
    inner class ConstructorTests {

        @Test
        fun `throws exception if configurations represent different meta data providers`() {
            tempDirectory {
                // given
                val mainTestConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "main.example.org"
                    override fun fileSuffix(): FileSuffix = "json"
                }

                val dependentTestConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "dependent.example.org"
                    override fun fileSuffix(): FileSuffix = "json"
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    DependentConversionWatchService(
                        converter = TestPathAnimeConverter,
                        appConfig = testAppConfig,
                        mainConfig = mainTestConfig,
                        dependentMetaDataProviderConfigs = listOf(dependentTestConfig),
                    )
                }

                // then
                assertThat(result).hasMessage("All configs must be from the same meta data provider.")
            }
        }
    }

    @Nested
    inner class PrepareTests {

        @Test
        fun `remove existing lock files when starting the watch service`() {
            tempDirectory {
                // given
                val mainTestConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun fileSuffix(): FileSuffix = "json"
                }

                val mainWorkingDir = tempDir.resolve("main").apply {
                    createDirectory()
                    resolve("1535.$LOCK_FILE_SUFFIX").createFile()
                    resolve("1535.${mainTestConfig.fileSuffix()}").createFile()
                    resolve("dir.$LOCK_FILE_SUFFIX").createDirectory()
                }

                val dependentTestConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun fileSuffix(): FileSuffix = "json"
                }

                val dependentWorkingDir = tempDir.resolve("dependent").apply {
                    createDirectory()
                    resolve("1234.$LOCK_FILE_SUFFIX").createFile()
                    resolve("1234.${mainTestConfig.fileSuffix()}").createFile()
                    resolve("dir.$LOCK_FILE_SUFFIX").createDirectory()
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory {
                        return when(metaDataProviderConfig.identityHashCode()) {
                            mainTestConfig.identityHashCode() -> mainWorkingDir
                            dependentTestConfig.identityHashCode() -> dependentWorkingDir
                            else -> shouldNotBeInvoked()
                        }
                    }
                }

                val anime = AnimeRaw("Death Note")

                val testConverter = object: PathAnimeConverter by TestPathAnimeConverter {
                    override suspend fun convert(path: Path): List<AnimeRaw> = listOf(anime)
                }

                val dependentConversionWatchService = DependentConversionWatchService(
                    appConfig = testAppConfig,
                    mainConfig = mainTestConfig,
                    dependentMetaDataProviderConfigs = listOf(dependentTestConfig),
                    converter = testConverter
                )

                // when
                dependentConversionWatchService.prepare()

                val mainDirContent = mainWorkingDir.listDirectoryEntries().map { it.fileName() }
                assertThat(mainDirContent).containsExactlyInAnyOrder(
                    "1535.${mainTestConfig.fileSuffix()}",
                    "dir.$LOCK_FILE_SUFFIX",
                )

                val dependentDirContent = dependentWorkingDir.listDirectoryEntries().map { it.fileName() }
                assertThat(dependentDirContent).containsExactlyInAnyOrder(
                    "1234.${dependentTestConfig.fileSuffix()}",
                    "dir.$LOCK_FILE_SUFFIX",
                )
            }
        }

        @Test
        fun `convert all unconverted files at startup`() {
            tempDirectory {
                // given
                val mainTestConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun fileSuffix(): FileSuffix = "json"
                }

                val mainWorkingDir = tempDir.resolve("main").apply {
                    createDirectory()
                    resolve("1535.${mainTestConfig.fileSuffix()}").createFile()
                    resolve("dir.$LOCK_FILE_SUFFIX").createDirectory()
                }

                val dependentTestConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun fileSuffix(): FileSuffix = "json"
                }

                val dependentWorkingDir = tempDir.resolve("dependent").apply {
                    createDirectory()
                    resolve("1535.${mainTestConfig.fileSuffix()}").createFile()
                    resolve("dir.$LOCK_FILE_SUFFIX").createDirectory()
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory {
                        return when(metaDataProviderConfig.identityHashCode()) {
                            mainTestConfig.identityHashCode() -> mainWorkingDir
                            dependentTestConfig.identityHashCode() -> dependentWorkingDir
                            else -> shouldNotBeInvoked()
                        }
                    }
                }

                val anime = AnimeRaw("Death Note")

                val testConverter = object: PathAnimeConverter by TestPathAnimeConverter {
                    override suspend fun convert(path: Path): List<AnimeRaw> = listOf(anime)
                }

                val dependentConversionWatchService = DependentConversionWatchService(
                    appConfig = testAppConfig,
                    mainConfig = mainTestConfig,
                    dependentMetaDataProviderConfigs = listOf(dependentTestConfig),
                    converter = testConverter
                )

                // when
                dependentConversionWatchService.prepare()

                val mainDirContent = mainWorkingDir.listDirectoryEntries().map { it.fileName() }
                assertThat(mainDirContent).containsExactlyInAnyOrder(
                    "1535.${mainTestConfig.fileSuffix()}",
                    "1535.${CONVERTED_FILE_SUFFIX}",
                    "dir.$LOCK_FILE_SUFFIX",
                )

                val dependentDirContent = dependentWorkingDir.listDirectoryEntries().map { it.fileName() }
                assertThat(dependentDirContent).containsExactlyInAnyOrder(
                    "1535.${dependentTestConfig.fileSuffix()}",
                    "dir.$LOCK_FILE_SUFFIX",
                )
            }
        }
    }

    @Nested
    inner class WatchTests {

        @Test
        fun `don't invoke converter, because the dependent file is missing`() {
            tempDirectory {
                withContext(LIMITED_FS) {
                    // given
                    var converterHasBeenInvoked = false

                    val mainTestConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                        override fun fileSuffix(): FileSuffix = "json"
                    }

                    val mainWorkingDir = tempDir.resolve("main").createDirectory()

                    val dependentTestConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                        override fun fileSuffix(): FileSuffix = "json"
                    }

                    val dependentWorkingDir = tempDir.resolve("dependent").createDirectory()

                    val testAppConfig = object: Config by TestAppConfig {
                        override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory {
                            return when(metaDataProviderConfig.identityHashCode()) {
                                mainTestConfig.identityHashCode() -> mainWorkingDir
                                dependentTestConfig.identityHashCode() -> dependentWorkingDir
                                else -> shouldNotBeInvoked()
                            }
                        }
                    }

                    val testConverter = object: PathAnimeConverter by TestPathAnimeConverter {
                        override suspend fun convert(path: Path): List<AnimeRaw> {
                            converterHasBeenInvoked = true
                            return  emptyList()
                        }
                    }

                    val dependentConversionWatchService = DependentConversionWatchService(
                        appConfig = testAppConfig,
                        mainConfig = mainTestConfig,
                        dependentMetaDataProviderConfigs = listOf(dependentTestConfig),
                        converter = testConverter,
                    )

                    val watchService = launch {
                        dependentConversionWatchService.watch()
                    }
                    delay(1500) // allow WatchService to complete the prepare task

                    val lockFile = mainWorkingDir.resolve("1535.$LOCK_FILE_SUFFIX").createFile()
                    mainWorkingDir.resolve("1535.${mainTestConfig.fileSuffix()}").createFile()

                    // when
                    lockFile.deleteIfExists()

                    // then
                    delay(10.toDuration(SECONDS)) // cannot do it any other way as this is a negative test
                    dependentConversionWatchService.stop()
                    watchService.cancelAndJoin()

                    assertThat(converterHasBeenInvoked).isFalse()
                }
            }
        }

        @Test
        fun `don't invoke converter, because the main file is missing`() {
            tempDirectory {
                withContext(LIMITED_FS) {
                    // given
                    var converterHasBeenInvoked = false

                    val mainTestConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                        override fun fileSuffix(): FileSuffix = "json"
                    }

                    val mainWorkingDir = tempDir.resolve("main").createDirectory()

                    val dependentTestConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                        override fun fileSuffix(): FileSuffix = "json"
                    }

                    val dependentWorkingDir = tempDir.resolve("dependent").createDirectory()

                    val testAppConfig = object: Config by TestAppConfig {
                        override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory {
                            return when(metaDataProviderConfig.identityHashCode()) {
                                mainTestConfig.identityHashCode() -> mainWorkingDir
                                dependentTestConfig.identityHashCode() -> dependentWorkingDir
                                else -> shouldNotBeInvoked()
                            }
                        }
                    }

                    val testConverter = object: PathAnimeConverter by TestPathAnimeConverter {
                        override suspend fun convert(path: Path): List<AnimeRaw> {
                            converterHasBeenInvoked = true
                            return  emptyList()
                        }
                    }

                    val dependentConversionWatchService = DependentConversionWatchService(
                        appConfig = testAppConfig,
                        mainConfig = mainTestConfig,
                        dependentMetaDataProviderConfigs = listOf(dependentTestConfig),
                        converter = testConverter,
                    )

                    val watchService = launch {
                        dependentConversionWatchService.watch()
                    }
                    delay(1500) // allow WatchService to complete the prepare task

                    val lockFile = dependentWorkingDir.resolve("1535.$LOCK_FILE_SUFFIX").createFile()
                    dependentWorkingDir.resolve("1535.${dependentTestConfig.fileSuffix()}").createFile()


                    // when
                    lockFile.deleteIfExists()

                    // then
                    delay(10.toDuration(SECONDS)) // cannot do it any other way as this is a negative test
                    dependentConversionWatchService.stop()
                    watchService.cancelAndJoin()

                    assertThat(converterHasBeenInvoked).isFalse()
                }
            }
        }

        @Test
        fun `invoke converter as soon as the missing dependent file is created`() {
            tempDirectory {
                withContext(LIMITED_FS) {
                    // given
                    var converterHasBeenInvoked = false
                    var canBeInvoked = false

                    val mainTestConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                        override fun fileSuffix(): FileSuffix = "html"
                    }

                    val mainWorkingDir = tempDir.resolve("main").createDirectory()
                    mainWorkingDir.resolve("1535.${mainTestConfig.fileSuffix()}").createFile()

                    val dependentTestConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                        override fun fileSuffix(): FileSuffix = "json"
                    }

                    val dependentWorkingDir = tempDir.resolve("dependent").createDirectory()

                    val testAppConfig = object : Config by TestAppConfig {
                        override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory {
                            return when (metaDataProviderConfig.identityHashCode()) {
                                mainTestConfig.identityHashCode() -> mainWorkingDir
                                dependentTestConfig.identityHashCode() -> dependentWorkingDir
                                else -> shouldNotBeInvoked()
                            }
                        }
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

                    val dependentConversionWatchService = DependentConversionWatchService(
                        appConfig = testAppConfig,
                        mainConfig = mainTestConfig,
                        dependentMetaDataProviderConfigs = listOf(dependentTestConfig),
                        converter = testConverter,
                    )

                    val watchService = launch {
                        dependentConversionWatchService.watch()
                    }
                    delay(1500) // allow WatchService to complete the prepare task

                    val lockFile = dependentWorkingDir.resolve("1535.$LOCK_FILE_SUFFIX").createFile()
                    dependentWorkingDir.resolve("1535.${dependentTestConfig.fileSuffix()}").createFile()

                    // when
                    canBeInvoked = true
                    lockFile.deleteIfExists()

                    // then
                    waitFor(timeout = 10.toDuration(SECONDS)) {
                        mainWorkingDir.resolve("1535.$CONVERTED_FILE_SUFFIX").regularFileExists()
                    }
                    dependentConversionWatchService.stop()
                    watchService.cancelAndJoin()

                    assertThat(converterHasBeenInvoked).isTrue()

                    val mainDirContent = mainWorkingDir.listDirectoryEntries().map { it.fileName() }
                    assertThat(mainDirContent).containsExactlyInAnyOrder(
                        "1535.${mainTestConfig.fileSuffix()}",
                        "1535.$CONVERTED_FILE_SUFFIX"
                    )
                }
            }
        }

        @Test
        fun `invoke converter as soon as the missing main file is created`() {
            tempDirectory {
                withContext(LIMITED_FS) {
                    // given
                    var converterHasBeenInvoked = false
                    var canBeInvoked = false

                    val mainTestConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                        override fun fileSuffix(): FileSuffix = "html"
                    }

                    val mainWorkingDir = tempDir.resolve("main").createDirectory()

                    val dependentTestConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                        override fun fileSuffix(): FileSuffix = "json"
                    }

                    val dependentWorkingDir = tempDir.resolve("dependent").createDirectory()
                    dependentWorkingDir.resolve("1535.${dependentTestConfig.fileSuffix()}").createFile()

                    val testAppConfig = object : Config by TestAppConfig {
                        override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory {
                            return when (metaDataProviderConfig.identityHashCode()) {
                                mainTestConfig.identityHashCode() -> mainWorkingDir
                                dependentTestConfig.identityHashCode() -> dependentWorkingDir
                                else -> shouldNotBeInvoked()
                            }
                        }
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

                    val dependentConversionWatchService = DependentConversionWatchService(
                        appConfig = testAppConfig,
                        mainConfig = mainTestConfig,
                        dependentMetaDataProviderConfigs = listOf(dependentTestConfig),
                        converter = testConverter,
                    )

                    val watchService = launch {
                        dependentConversionWatchService.watch()
                    }
                    delay(1500) // allow WatchService to complete the prepare task

                    val lockFile = mainWorkingDir.resolve("1535.$LOCK_FILE_SUFFIX").createFile()
                    mainWorkingDir.resolve("1535.${mainTestConfig.fileSuffix()}").createFile()

                    // when
                    canBeInvoked = true
                    lockFile.deleteIfExists()

                    // then
                    waitFor(timeout = 10.toDuration(SECONDS)) {
                        mainWorkingDir.resolve("1535.$CONVERTED_FILE_SUFFIX").regularFileExists()
                    }
                    dependentConversionWatchService.stop()
                    watchService.cancelAndJoin()

                    assertThat(converterHasBeenInvoked).isTrue()

                    val mainDirContent = mainWorkingDir.listDirectoryEntries().map { it.fileName() }
                    assertThat(mainDirContent).containsExactlyInAnyOrder(
                        "1535.${mainTestConfig.fileSuffix()}",
                        "1535.$CONVERTED_FILE_SUFFIX"
                    )
                }
            }
        }
    }
}