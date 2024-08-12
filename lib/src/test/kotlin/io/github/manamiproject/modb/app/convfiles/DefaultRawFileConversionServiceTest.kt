package io.github.manamiproject.modb.app.convfiles

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestMetaDataProviderConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.test.Test
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.toDuration

internal class DefaultRawFileConversionServiceTest {

    @Nested
    inner class UnconvertedFilesExistTests {

        @Test
        fun `returns true if there are files which still need to be converted`() {
            tempDirectory {
                // given
                val testConfig1: MetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun fileSuffix(): FileSuffix = "json"
                }

                val dir1 = tempDir.resolve("provider1").createDirectory()
                dir1.resolve("1.${testConfig1.fileSuffix()}").createFile()
                dir1.resolve("2.${testConfig1.fileSuffix()}").createFile()
                dir1.resolve("2.$CONVERTED_FILE_SUFFIX").createFile()

                val testConfig2: MetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "other-example.com"
                    override fun fileSuffix(): FileSuffix = "html"
                }

                val dir2 = tempDir.resolve("provider2").createDirectory()
                dir2.resolve("3.${testConfig2.fileSuffix()}").createFile()

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations() = setOf(testConfig1, testConfig2)
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory {
                        return when(metaDataProviderConfig.hostname()) {
                            testConfig1.hostname() -> dir1
                            testConfig2.hostname() -> dir2
                            else -> shouldNotBeInvoked()
                        }
                    }
                }

                val defaultRawFileConversionStatusChecker = DefaultRawFileConversionService(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultRawFileConversionStatusChecker.unconvertedFilesExist()

                // then
                assertThat(result).isTrue()
            }
        }

        @Test
        fun `returns false if all files have been converted`() {
            tempDirectory {
                // given
                val dir1 = tempDir.resolve("provider1").createDirectory()

                val testConfig1: MetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun fileSuffix(): FileSuffix = "json"
                }

                dir1.resolve("1.${testConfig1.fileSuffix()}").createFile()
                dir1.resolve("1.$CONVERTED_FILE_SUFFIX").createFile()

                val dir2 = tempDir.resolve("provider2").createDirectory()

                val testConfig2: MetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "other-example.com"
                    override fun fileSuffix(): FileSuffix = "html"
                }

                dir2.resolve("2.${testConfig2.fileSuffix()}").createFile()
                dir2.resolve("2.$CONVERTED_FILE_SUFFIX").createFile()

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations() = setOf(testConfig1, testConfig2)
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory {
                        return when(metaDataProviderConfig.hostname()) {
                            testConfig1.hostname() -> dir1
                            testConfig2.hostname() -> dir2
                            else -> shouldNotBeInvoked()
                        }
                    }
                }

                val defaultRawFileConversionStatusChecker = DefaultRawFileConversionService(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultRawFileConversionStatusChecker.unconvertedFilesExist()

                // then
                assertThat(result).isFalse()
            }
        }
    }

    @Nested
    inner class WaitForAllRawFilesToBeConvertedTests {

        @Test
        fun `blocking call which waits for all files to be converted`() {
            tempDirectory {
                // given
                val testConfig1: MetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun fileSuffix(): FileSuffix = "json"
                }

                val dir1 = tempDir.resolve("provider1").createDirectory()
                dir1.resolve("1.${testConfig1.fileSuffix()}").createFile()
                dir1.resolve("2.${testConfig1.fileSuffix()}").createFile()
                dir1.resolve("2.$CONVERTED_FILE_SUFFIX").createFile()

                val testConfig2: MetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "other-example.com"
                    override fun fileSuffix(): FileSuffix = "html"
                }

                val dir2 = tempDir.resolve("provider2").createDirectory()
                dir2.resolve("3.${testConfig2.fileSuffix()}").createFile()

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations() = setOf(testConfig1, testConfig2)
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory {
                        return when(metaDataProviderConfig.hostname()) {
                            testConfig1.hostname() -> dir1
                            testConfig2.hostname() -> dir2
                            else -> shouldNotBeInvoked()
                        }
                    }
                }

                val defaultRawFileConversionStatusChecker = DefaultRawFileConversionService(
                    appConfig = testAppConfig,
                )

                val expectedFile1 = dir1.resolve("1.$CONVERTED_FILE_SUFFIX")
                val expectedFile2 = dir2.resolve("3.$CONVERTED_FILE_SUFFIX")

                // when
                withContext(IO) {
                    launch {
                        defaultRawFileConversionStatusChecker.waitForAllRawFilesToBeConverted()
                    }
                    launch {
                        delay(1.toDuration(SECONDS))
                        expectedFile1.createFile()
                    }
                    launch {
                        delay(2.toDuration(SECONDS))
                        expectedFile2.createFile()
                    }
                }

                // then
                assertThat(expectedFile1).exists()
                assertThat(expectedFile2).exists()
            }
        }

        @Test
        fun `throws exception in case the the timeout hits`() {
            tempDirectory {
                // given
                val testConfig1: MetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun fileSuffix(): FileSuffix = "json"
                }

                val dir1 = tempDir.resolve("provider1").createDirectory()
                dir1.resolve("1.${testConfig1.fileSuffix()}").createFile()
                dir1.resolve("2.${testConfig1.fileSuffix()}").createFile()
                dir1.resolve("2.$CONVERTED_FILE_SUFFIX").createFile()

                val testConfig2: MetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "other-example.com"
                    override fun fileSuffix(): FileSuffix = "html"
                }

                val dir2 = tempDir.resolve("provider2").createDirectory()
                dir2.resolve("3.${testConfig2.fileSuffix()}").createFile()

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations() = setOf(testConfig1, testConfig2)
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory {
                        return when(metaDataProviderConfig.hostname()) {
                            testConfig1.hostname() -> dir1
                            testConfig2.hostname() -> dir2
                            else -> shouldNotBeInvoked()
                        }
                    }
                }

                val defaultRawFileConversionStatusChecker = DefaultRawFileConversionService(
                    appConfig = testAppConfig,
                )

                // when
                val result = assertThrows<TimeoutCancellationException> { // exceptionExpected cannot catch TimeoutCancellationException
                    defaultRawFileConversionStatusChecker.waitForAllRawFilesToBeConverted()
                }

                // then
                assertThat(result).hasMessage("Timed out waiting for 10000 ms")
            }
        }
    }

    @Nested
    inner class StartTests {

        @Test
        fun `returns false if you try to start it twice`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val defaultRawFileConversionService = DefaultRawFileConversionService(
                    appConfig = testAppConfig,
                )

                val initallyStarted = defaultRawFileConversionService.start()

                // when
                val result = defaultRawFileConversionService.start()

                // then
                defaultRawFileConversionService.shutdown()
                assertThat(initallyStarted).isTrue()
                assertThat(result).isFalse()
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = DefaultRawFileConversionService.instance

                // when
                val result = DefaultRawFileConversionService.instance

                // then
                assertThat(result).isExactlyInstanceOf(DefaultRawFileConversionService::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}