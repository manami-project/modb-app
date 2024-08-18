package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestDownloadControlStateAccessor
import io.github.manamiproject.modb.app.TestMetaDataProviderConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.LOCK_FILE_SUFFIX
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.test.Test

internal class NoLockFilesLeftValidationPostProcessorTest {

    @Nested
    inner class ProcessTests {

        @Test
        fun `throws exception if there is a lock file in workdir`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val workdir = tempDir.resolve("workdir").createDirectory()
                workdir.resolve("1535.$LOCK_FILE_SUFFIX").createFile()

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = workdir
                }

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val noLockFilesLeftValidationPostProcessor = NoLockFilesLeftValidationPostProcessor(
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    noLockFilesLeftValidationPostProcessor.process()
                }

                // then
                assertThat(result).hasMessage("Lock file found in workingdir.")
            }
        }

        @Test
        fun `throws exception if there is a lock file in downloadcontrolstatedir`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val dcsDir = tempDir.resolve("dcsDir").createDirectory()
                dcsDir.resolve("1535.$LOCK_FILE_SUFFIX").createFile()

                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = dcsDir
                }

                val noLockFilesLeftValidationPostProcessor = NoLockFilesLeftValidationPostProcessor(
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    noLockFilesLeftValidationPostProcessor.process()
                }

                // then
                assertThat(result).hasMessage("Lock file found in dcs dir.")
            }
        }

        @Test
        fun `returns true if there are no lock files`() {
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
                    override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val noLockFilesLeftValidationPostProcessor = NoLockFilesLeftValidationPostProcessor(
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                val result = noLockFilesLeftValidationPostProcessor.process()

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
                val previous = NoLockFilesLeftValidationPostProcessor.instance

                // when
                val result = NoLockFilesLeftValidationPostProcessor.instance

                // then
                assertThat(result).isExactlyInstanceOf(NoLockFilesLeftValidationPostProcessor::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}