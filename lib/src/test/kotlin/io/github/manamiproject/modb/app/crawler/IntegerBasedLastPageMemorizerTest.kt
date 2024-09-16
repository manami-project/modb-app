package io.github.manamiproject.modb.app.crawler

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestMetaDataProviderConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.readFile
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class IntegerBasedLastPageMemorizerTest {

    @Nested
    inner class MemorizeLastPageTests {

        @Test
        fun `memorize last page in workingDir for the specifc meta data provider`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val defaultLastPageMemorizer = IntegerBasedLastPageMemorizer(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testConfig
                )

                val lastPageFile = tempDir.resolve(LAST_PAGE_MEMORIZER_FILE_NAME)

                // when
                defaultLastPageMemorizer.memorizeLastPage(4)

                // then
                assertThat(lastPageFile).exists()
                assertThat(lastPageFile.readFile()).isEqualTo("4")
            }
        }

        @Test
        fun `override previous value`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val defaultLastPageMemorizer = IntegerBasedLastPageMemorizer(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testConfig
                )

                val lastPageFile = tempDir.resolve(LAST_PAGE_MEMORIZER_FILE_NAME)
                defaultLastPageMemorizer.memorizeLastPage(3)

                // when
                defaultLastPageMemorizer.memorizeLastPage(4)

                // then
                assertThat(lastPageFile).exists()
                assertThat(lastPageFile.readFile()).isEqualTo("4")
            }
        }
    }

    @Nested
    inner class RetrieveLastPageTest {

        @Test
        fun `return 1 if the file which is supposed to contain the last page doesn't exist`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val defaultLastPageMemorizer = IntegerBasedLastPageMemorizer(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testConfig,
                )

                // when
                val result = defaultLastPageMemorizer.retrieveLastPage()

                // then
                assertThat(result).isEqualTo(1)
            }
        }

        @Test
        fun `correctly retrieves the last page`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val defaultLastPageMemorizer = IntegerBasedLastPageMemorizer(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testConfig,
                )

                val lastPageFile = tempDir.resolve(LAST_PAGE_MEMORIZER_FILE_NAME)

                "22".writeToFile(lastPageFile)

                // when
                val result = defaultLastPageMemorizer.retrieveLastPage()

                // then
                assertThat(result).isEqualTo(22)
            }
        }

        @Test
        fun `only returns first line if there are multiple lines`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val defaultLastPageMemorizer = IntegerBasedLastPageMemorizer(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testConfig,
                )

                val lastPageFile = tempDir.resolve(LAST_PAGE_MEMORIZER_FILE_NAME)

                "5\n32".writeToFile(lastPageFile)

                // when
                val result = defaultLastPageMemorizer.retrieveLastPage()

                // then
                assertThat(result).isEqualTo(5)
            }
        }

        @Test
        fun `throws exception if the content of the file which is supposed to contain the last page doesn't contain a number`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val defaultLastPageMemorizer = IntegerBasedLastPageMemorizer(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testConfig,
                )

                val lastPageFile = tempDir.resolve(LAST_PAGE_MEMORIZER_FILE_NAME)

                "content".writeToFile(lastPageFile)

                // when
                val result = exceptionExpected<IllegalStateException> {
                    defaultLastPageMemorizer.retrieveLastPage()
                }

                // then
                assertThat(result).hasMessage("Unable to retrieve last page for [example.org]")
            }
        }
    }
}