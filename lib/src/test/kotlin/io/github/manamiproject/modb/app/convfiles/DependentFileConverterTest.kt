package io.github.manamiproject.modb.app.convfiles

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestMetaDataProviderConfig
import io.github.manamiproject.modb.app.TestPathAnimeConverter
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.PathAnimeConverter
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.fileName
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.listDirectoryEntries
import kotlin.test.Test

internal class DependentFileConverterTest {

    @Nested
    inner class ConstructorTests {

        @Test
        fun `throws exception if configurations represent different meta data providers`() {
            tempDirectory {
                // given
                val mainTestConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun fileSuffix(): FileSuffix = "html"
                    override fun hostname(): Hostname = "main.example.org"
                }

                val dependentTestConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun fileSuffix(): FileSuffix = "json"
                    override fun hostname(): Hostname = "dependent.example.org"
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    DependentFileConverter(
                        appConfig = testAppConfig,
                        dependentMetaDataProciderConfigs = listOf(dependentTestConfig),
                        metaDataProviderConfig = mainTestConfig,
                        converter = TestPathAnimeConverter,
                    )
                }

                // then
                assertThat(result).hasMessage("All configs must be from the same meta data provider.")
            }
        }
    }

    @Nested
    inner class ConvertUnconvertedFilesTests {

        @Test
        fun `convert all files which have a file in main and dependent working dir and haven't been converted yet`() {
            tempDirectory {
                // given
                val mainTestConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun fileSuffix(): FileSuffix = "html"
                    override fun hostname(): Hostname = "example.org"
                }

                val mainWorkingDir = tempDir.resolve("main").createDirectory()

                mainWorkingDir.resolve("123.${mainTestConfig.fileSuffix()}").createFile()
                mainWorkingDir.resolve("987.${mainTestConfig.fileSuffix()}").createFile()
                mainWorkingDir.resolve("369.${mainTestConfig.fileSuffix()}").createFile()
                mainWorkingDir.resolve("369.${CONVERTED_FILE_SUFFIX}").createFile()
                mainWorkingDir.resolve("456.BAK").createFile()
                mainWorkingDir.resolve("folder").createDirectory()

                val dependentTestConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun fileSuffix(): FileSuffix = "json"
                    override fun hostname(): Hostname = "example.org"
                }

                val dependentWorkingDir = tempDir.resolve("dependent").createDirectory()

                dependentWorkingDir.resolve("123.${dependentTestConfig.fileSuffix()}").createFile()
                dependentWorkingDir.resolve("369.${dependentTestConfig.fileSuffix()}").createFile()
                dependentWorkingDir.resolve("411.${dependentTestConfig.fileSuffix()}").createFile()
                dependentWorkingDir.resolve("456.BAK").createFile()
                dependentWorkingDir.resolve("another_folder").createDirectory()

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory {
                        return when(metaDataProviderConfig.identityHashCode()) {
                            mainTestConfig.identityHashCode() -> mainWorkingDir
                            dependentTestConfig.identityHashCode() -> dependentWorkingDir
                            else -> shouldNotBeInvoked()
                        }
                    }
                }

                val testConverter = object: PathAnimeConverter {
                    override suspend fun convert(path: Path): Collection<Anime> = listOf(Anime(path.fileName()))
                }

                val testAbstractDependentFileConverter = DependentFileConverter(
                    appConfig = testAppConfig,
                    dependentMetaDataProciderConfigs = listOf(dependentTestConfig),
                    metaDataProviderConfig = mainTestConfig,
                    converter = testConverter,
                )

                // when
                testAbstractDependentFileConverter.convertUnconvertedFiles()

                // then
                val convFiles = mainWorkingDir.listDirectoryEntries("*.$CONVERTED_FILE_SUFFIX").map { it.fileName() }
                assertThat(convFiles).containsExactlyInAnyOrder(
                    "123.${CONVERTED_FILE_SUFFIX}",
                    "369.${CONVERTED_FILE_SUFFIX}",
                )
            }
        }
    }
    
    @Nested
    inner class ConvertFileToConvFileTests {

        @Test
        fun `successfully convert file`() {
            tempDirectory {
                // given
                val mainTestConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun fileSuffix(): FileSuffix = "html"
                    override fun hostname(): Hostname = "example.org"
                }

                val mainWorkingDir = tempDir.resolve("main").createDirectory()

                val mainFile = mainWorkingDir.resolve("123.${mainTestConfig.fileSuffix()}").createFile()

                val dependentTestConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun fileSuffix(): FileSuffix = "json"
                    override fun hostname(): Hostname = "example.org"
                }

                val dependentWorkingDir = tempDir.resolve("dependent").createDirectory()

                dependentWorkingDir.resolve("123.${dependentTestConfig.fileSuffix()}").createFile()

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory {
                        return when(metaDataProviderConfig.identityHashCode()) {
                            mainTestConfig.identityHashCode() -> mainWorkingDir
                            dependentTestConfig.identityHashCode() -> dependentWorkingDir
                            else -> shouldNotBeInvoked()
                        }
                    }
                }

                val testConverter = object: PathAnimeConverter {
                    override suspend fun convert(path: Path): Collection<Anime> = listOf(Anime(path.fileName()))
                }

                val testAbstractDependentFileConverter = DependentFileConverter(
                    appConfig = testAppConfig,
                    dependentMetaDataProciderConfigs = listOf(dependentTestConfig),
                    metaDataProviderConfig = mainTestConfig,
                    converter = testConverter,
                )

                // when
                testAbstractDependentFileConverter.convertFileToConvFile(mainFile)

                // then
                val convFiles = mainWorkingDir.listDirectoryEntries("*.$CONVERTED_FILE_SUFFIX").map { it.fileName() }
                assertThat(convFiles).containsExactly("123.${CONVERTED_FILE_SUFFIX}")
            }
        }

        @Test
        fun `don't convert on missing dependent file`() {
            tempDirectory {
                // given
                val mainTestConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun fileSuffix(): FileSuffix = "html"
                    override fun hostname(): Hostname = "example.org"
                }

                val mainWorkingDir = tempDir.resolve("main").createDirectory()

                val mainFile = mainWorkingDir.resolve("123.${mainTestConfig.fileSuffix()}").createFile()

                val dependentTestConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun fileSuffix(): FileSuffix = "json"
                    override fun hostname(): Hostname = "example.org"
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

                val testAbstractDependentFileConverter = DependentFileConverter(
                    appConfig = testAppConfig,
                    dependentMetaDataProciderConfigs = listOf(dependentTestConfig),
                    metaDataProviderConfig = mainTestConfig,
                    converter = TestPathAnimeConverter,
                )

                // when
                testAbstractDependentFileConverter.convertFileToConvFile(mainFile)

                // then
                val convFiles = mainWorkingDir.listDirectoryEntries("*.$CONVERTED_FILE_SUFFIX").map { it.fileName() }
                assertThat(convFiles).isEmpty()
            }
        }

        @Test
        fun `don't convert if file has already been converted`() {
            tempDirectory {
                // given
                val mainTestConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun fileSuffix(): FileSuffix = "html"
                    override fun hostname(): Hostname = "example.org"
                }

                val mainWorkingDir = tempDir.resolve("main").createDirectory()

                val mainFile = mainWorkingDir.resolve("123.${mainTestConfig.fileSuffix()}").createFile()

                val dependentTestConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun fileSuffix(): FileSuffix = "json"
                    override fun hostname(): Hostname = "example.org"
                }

                val dependentWorkingDir = tempDir.resolve("dependent").createDirectory()

                dependentWorkingDir.resolve("123.${dependentTestConfig.fileSuffix()}").createFile()

                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory {
                        return when(metaDataProviderConfig.identityHashCode()) {
                            mainTestConfig.identityHashCode() -> mainWorkingDir
                            dependentTestConfig.identityHashCode() -> dependentWorkingDir
                            else -> shouldNotBeInvoked()
                        }
                    }
                }

                var invocations = 0
                val testConverter = object: PathAnimeConverter {
                    override suspend fun convert(path: Path): Collection<Anime> {
                        invocations++
                        return listOf(Anime(path.fileName()))
                    }
                }

                val testAbstractDependentFileConverter = DependentFileConverter(
                    appConfig = testAppConfig,
                    dependentMetaDataProciderConfigs = listOf(dependentTestConfig),
                    metaDataProviderConfig = mainTestConfig,
                    converter = testConverter,
                )

                testAbstractDependentFileConverter.convertFileToConvFile(mainFile)

                // when
                testAbstractDependentFileConverter.convertFileToConvFile(mainFile)

                // then
                val convFiles = mainWorkingDir.listDirectoryEntries("*.$CONVERTED_FILE_SUFFIX").map { it.fileName() }
                assertThat(convFiles).containsExactly("123.${CONVERTED_FILE_SUFFIX}")
                assertThat(invocations).isOne()
            }
        }
    }
}