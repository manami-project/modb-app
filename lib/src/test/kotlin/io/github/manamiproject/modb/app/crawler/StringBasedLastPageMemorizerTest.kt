package io.github.manamiproject.modb.app.crawler

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestMetaDataProviderConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.readFile
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class StringBasedLastPageMemorizerTest {

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

                val lastPageMemorizer = StringBasedLastPageMemorizer(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testConfig,
                )

                val lastPageFile = tempDir.resolve(LAST_PAGE_MEMORIZER_FILE_NAME)

                // when
                lastPageMemorizer.memorizeLastPage("spring-2021")

                // then
                assertThat(lastPageFile).exists()
                assertThat(lastPageFile.readFile()).isEqualTo("spring-2021")
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

                val lastPageMemorizer = StringBasedLastPageMemorizer(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testConfig,
                )

                val lastPageFile = tempDir.resolve(LAST_PAGE_MEMORIZER_FILE_NAME)
                lastPageMemorizer.memorizeLastPage("spring-2021")

                // when
                lastPageMemorizer.memorizeLastPage("spring-2022")

                // then
                assertThat(lastPageFile).exists()
                assertThat(lastPageFile.readFile()).isEqualTo("spring-2022")
            }
        }
    }

    @Nested
    inner class RetrieveLastPageTest {

        @Test
        fun `return empty string if the file which is supposed to contain the last page doesn't exist`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val defaultLastPageMemorizer = StringBasedLastPageMemorizer(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testConfig,
                )

                // when
                val result = defaultLastPageMemorizer.retrieveLastPage()

                // then
                assertThat(result).isEmpty()
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

                val defaultLastPageMemorizer = StringBasedLastPageMemorizer(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testConfig,
                )

                val lastPageFile = tempDir.resolve(LAST_PAGE_MEMORIZER_FILE_NAME)
                "fall-1975".writeToFile(lastPageFile)

                // when
                val result = defaultLastPageMemorizer.retrieveLastPage()

                // then
                assertThat(result).isEqualTo("fall-1975")
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

                val defaultLastPageMemorizer = StringBasedLastPageMemorizer(
                    appConfig = testAppConfig,
                    metaDataProviderConfig = testConfig,
                )

                val lastPageFile = tempDir.resolve(LAST_PAGE_MEMORIZER_FILE_NAME)

                "content line 1\nline2".writeToFile(lastPageFile)

                // when
                val result = defaultLastPageMemorizer.retrieveLastPage()

                // then
                assertThat(result).isEqualTo("content line 1")
            }
        }
    }
}