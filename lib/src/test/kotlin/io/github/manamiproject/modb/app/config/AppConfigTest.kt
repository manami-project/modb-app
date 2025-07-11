package io.github.manamiproject.modb.app.config

import io.github.manamiproject.AnimenewsnetworkConfig
import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import io.github.manamiproject.modb.anisearch.AnisearchConfig
import io.github.manamiproject.modb.anisearch.AnisearchRelationsConfig
import io.github.manamiproject.modb.app.TestConfigRegistry
import io.github.manamiproject.modb.app.TestMetaDataProviderConfig
import io.github.manamiproject.modb.app.crawlers.animeplanet.AnimePlanetPaginationIdRangeSelectorConfig
import io.github.manamiproject.modb.app.crawlers.livechart.LivechartPaginationIdRangeSelectorConfig
import io.github.manamiproject.modb.app.crawlers.notify.NotifyAnimeDatasetDownloaderConfig
import io.github.manamiproject.modb.app.crawlers.notify.NotifyRelationsDatasetDownloaderConfig
import io.github.manamiproject.modb.app.crawlers.simkl.SimklPaginationIdRangeSelectorConfig
import io.github.manamiproject.modb.core.date.WeekOfYear
import io.github.manamiproject.modb.core.config.ConfigRegistry
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.livechart.LivechartConfig
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import io.github.manamiproject.modb.notify.NotifyConfig
import io.github.manamiproject.modb.notify.NotifyRelationsConfig
import io.github.manamiproject.modb.simkl.SimklConfig
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.test.Test

internal class AppConfigTest {

    @Nested
    inner class DownloadsDirectoryTests {

        @Test
        fun `throws an exception if config property directs to a path which doesn't exist`() {
            // given
            val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                override fun string(key: String): String = "non-existent"
            }

            val appConfig = AppConfig(
                configRegistry = testConfigRegistry,
            )

            // when
            val result = exceptionExpected<IllegalStateException> {
                appConfig.downloadsDirectory()
            }

            // then
            assertThat(result).hasMessage("Download directory set by 'downloadsDirectory' to [non-existent] doesn't exist or is not a directory.")
        }

        @Test
        fun `throws an exception if config property directs to a file`() {
            tempDirectory {
                // given
                val testFile = tempDir.resolve("test.txt").createFile()

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = testFile.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    appConfig.downloadsDirectory()
                }

                // then
                assertThat(result).hasMessageStartingWith("Download directory set by 'downloadsDirectory' to [")
                assertThat(result).hasMessageEndingWith("test.txt] doesn't exist or is not a directory.")
            }
        }

        @Test
        fun `correctly returns the download directory`() {
            tempDirectory {
                // given
                val testDir = tempDir.resolve("test").createDirectory()

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = testDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = appConfig.downloadsDirectory()

                // then
                assertThat(result).exists()
                assertThat(result).isDirectory()
            }
        }
    }

    @Nested
    inner class CurrentWeekWorkingDirTests {

        @Test
        fun `correctly generates directory name`() {
            tempDirectory {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = tempDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                val now = WeekOfYear.currentWeek()

                // when
                val result = appConfig.currentWeekWorkingDir()

                // then
                assertThat(result.fileName.toString()).startsWith("${now.year}-")
                assertThat(result.fileName.toString()).endsWith(now.week.toString())
            }
        }
    }

    @Nested
    inner class WorkingDirTests {

        @Test
        fun `throws exception if workingdir is an existing regular file`() {
            tempDirectory {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = tempDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                val testCurrentWeekWorkingDir = appConfig.currentWeekWorkingDir().createDirectory()
                testCurrentWeekWorkingDir.resolve(AnidbConfig.hostname()).createFile()

                // when
                val result = exceptionExpected<IllegalStateException> {
                    appConfig.workingDir(AnidbConfig)
                }

                // then
                assertThat(result).hasMessage("Working directory must not be a regular file.")
            }
        }

        @Test
        fun `works correctly if directory already exists`() {
            tempDirectory {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = tempDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                val testCurrentWeekWorkingDir = appConfig.currentWeekWorkingDir().createDirectory()
                testCurrentWeekWorkingDir.resolve(AnidbConfig.hostname()).createDirectory()

                // when
                val result = appConfig.workingDir(AnidbConfig)

                // then
                assertThat(result).exists()
                assertThat(result).isDirectory()
            }
        }

        @Test
        fun `throws exception if MetaDataProviderConfig is not mapped`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.com"
                }

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = tempDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    appConfig.workingDir(testMetaDataProviderConfig)
                }

                // then
                assertThat(result).hasMessage("No working directory mapping for [${testMetaDataProviderConfig::class.simpleName}]")
            }
        }

        @Test
        fun `check that name of working directory for AnidbConfig is correct`() {
            tempDirectory {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = tempDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = appConfig.workingDir(AnidbConfig)

                // then
                assertThat(result).exists()
                assertThat(result.fileName.toString()).isEqualTo("anidb.net")
            }
        }

        @Test
        fun `check that name of working directory for AnilistConfig is correct`() {
            tempDirectory {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = tempDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = appConfig.workingDir(AnilistConfig)

                // then
                assertThat(result).exists()
                assertThat(result.fileName.toString()).isEqualTo("anilist.co")
            }
        }

        @Test
        fun `check that name of working directory for AnimePlanetConfig is correct`() {
            tempDirectory {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = tempDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = appConfig.workingDir(AnimePlanetConfig)

                // then
                assertThat(result).exists()
                assertThat(result.fileName.toString()).isEqualTo("anime-planet.com")
            }
        }

        @Test
        fun `check that name of working directory for AnimePlanetPaginationIdRangeSelectorConfig is correct`() {
            tempDirectory {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = tempDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = appConfig.workingDir(AnimePlanetPaginationIdRangeSelectorConfig)

                // then
                assertThat(result).exists()
                assertThat(result.fileName.toString()).isEqualTo("anime-planet.com")
            }
        }

        @Test
        fun `check that name of working directory for AnimenewsnetworkConfig is correct`() {
            tempDirectory {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = tempDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = appConfig.workingDir(AnimenewsnetworkConfig)

                // then
                assertThat(result).exists()
                assertThat(result.fileName.toString()).isEqualTo("animenewsnetwork.com")
            }
        }

        @Test
        fun `check that name of working directory for AnisearchConfig is correct`() {
            tempDirectory {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = tempDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = appConfig.workingDir(AnisearchConfig)

                // then
                assertThat(result).exists()
                assertThat(result.fileName.toString()).isEqualTo("anisearch.com")
            }
        }

        @Test
        fun `check that name of working directory for AnisearchRelationsConfig is correct`() {
            tempDirectory {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = tempDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = appConfig.workingDir(AnisearchRelationsConfig)

                // then
                assertThat(result).exists()
                assertThat(result.fileName.toString()).isEqualTo("anisearch.com-relations")
            }
        }

        @Test
        fun `check that name of working directory for KitsuConfig is correct`() {
            tempDirectory {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = tempDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = appConfig.workingDir(KitsuConfig)

                // then
                assertThat(result).exists()
                assertThat(result.fileName.toString()).isEqualTo("kitsu.app")
            }
        }

        @Test
        fun `check that name of working directory for LivechartConfig is correct`() {
            tempDirectory {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = tempDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = appConfig.workingDir(LivechartConfig)

                // then
                assertThat(result).exists()
                assertThat(result.fileName.toString()).isEqualTo("livechart.me")
            }
        }

        @Test
        fun `check that name of working directory for LivechartPaginationIdRangeSelectorConfig is correct`() {
            tempDirectory {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = tempDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = appConfig.workingDir(LivechartPaginationIdRangeSelectorConfig)

                // then
                assertThat(result).exists()
                assertThat(result.fileName.toString()).isEqualTo("livechart.me")
            }
        }

        @Test
        fun `check that name of working directory for MyanimelistConfig is correct`() {
            tempDirectory {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = tempDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = appConfig.workingDir(MyanimelistConfig)

                // then
                assertThat(result).exists()
                assertThat(result.fileName.toString()).isEqualTo("myanimelist.net")
            }
        }

        @Test
        fun `check that name of working directory for NotifyConfig is correct`() {
            tempDirectory {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = tempDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = appConfig.workingDir(NotifyConfig)

                // then
                assertThat(result).exists()
                assertThat(result.fileName.toString()).isEqualTo("notify.moe")
            }
        }

        @Test
        fun `check that name of working directory for NotifyDatasetDownloaderConfig is correct`() {
            tempDirectory {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = tempDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = appConfig.workingDir(NotifyAnimeDatasetDownloaderConfig)

                // then
                assertThat(result).exists()
                assertThat(result.fileName.toString()).isEqualTo("notify.moe")
            }
        }

        @Test
        fun `check that name of working directory for NotifyRelationsConfig is correct`() {
            tempDirectory {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = tempDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = appConfig.workingDir(NotifyRelationsConfig)

                // then
                assertThat(result).exists()
                assertThat(result.fileName.toString()).isEqualTo("notify.moe-relations")
            }
        }

        @Test
        fun `check that name of working directory for NotifyRelationsDatasetDownloaderConfig is correct`() {
            tempDirectory {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = tempDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = appConfig.workingDir(NotifyRelationsDatasetDownloaderConfig)

                // then
                assertThat(result).exists()
                assertThat(result.fileName.toString()).isEqualTo("notify.moe-relations")
            }
        }

        @Test
        fun `check that name of working directory for SimklConfig is correct`() {
            tempDirectory {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = tempDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = appConfig.workingDir(SimklConfig)

                // then
                assertThat(result).exists()
                assertThat(result.fileName.toString()).isEqualTo("simkl.com")
            }
        }

        @Test
        fun `check that name of working directory for SimklPaginationIdRangeSelectorConfig is correct`() {
            tempDirectory {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = tempDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = appConfig.workingDir(SimklPaginationIdRangeSelectorConfig)

                // then
                assertThat(result).exists()
                assertThat(result.fileName.toString()).isEqualTo("simkl.com")
            }
        }
    }

    @Nested
    inner class OutputDirectoryTests {

        @Test
        fun `throws an exception if config property directs to a path which doesn't exist`() {
            // given
            val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                override fun string(key: String): String = "non-existent"
            }

            val appConfig = AppConfig(
                configRegistry = testConfigRegistry,
            )

            // when
            val result = exceptionExpected<IllegalStateException> {
                appConfig.outputDirectory()
            }

            // then
            assertThat(result).hasMessage("Output directory set by 'outputDirectory' to [non-existent] doesn't exist or is not a directory.")
        }

        @Test
        fun `throws an exception if config property directs to a file`() {
            tempDirectory {
                // given
                val testFile = tempDir.resolve("test.txt").createFile()

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = testFile.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    appConfig.outputDirectory()
                }

                // then
                assertThat(result).hasMessageStartingWith("Output directory set by 'outputDirectory' to [")
                assertThat(result).hasMessageEndingWith("test.txt] doesn't exist or is not a directory.")
            }
        }

        @Test
        fun `correctly returns the download directory`() {
            tempDirectory {
                // given
                val testDir = tempDir.resolve("test").createDirectory()

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = testDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = appConfig.outputDirectory()

                // then
                assertThat(result).exists()
                assertThat(result).isDirectory()
            }
        }
    }

    @Nested
    inner class DownloadControlStateDirectoryTests {

        @Test
        fun `throws an exception if config property directs to a path which doesn't exist`() {
            // given
            val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                override fun string(key: String): String = "non-existent"
            }

            val appConfig = AppConfig(
                configRegistry = testConfigRegistry,
            )

            // when
            val result = exceptionExpected<IllegalStateException> {
                appConfig.downloadControlStateDirectory()
            }

            // then
            assertThat(result).hasMessage("Output directory set by 'downloadControlStateDirectory' to [non-existent] doesn't exist or is not a directory.")
        }

        @Test
        fun `throws an exception if config property directs to a file`() {
            tempDirectory {
                // given
                val testFile = tempDir.resolve("test.txt").createFile()

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = testFile.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    appConfig.downloadControlStateDirectory()
                }

                // then
                assertThat(result).hasMessageStartingWith("Output directory set by 'downloadControlStateDirectory' to [")
                assertThat(result).hasMessageEndingWith("test.txt] doesn't exist or is not a directory.")
            }
        }

        @Test
        fun `correctly returns the download directory`() {
            tempDirectory {
                // given
                val testDir = tempDir.resolve("test").createDirectory()

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = testDir.toAbsolutePath().toString()
                }

                val appConfig = AppConfig(
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = appConfig.downloadControlStateDirectory()

                // then
                assertThat(result).exists()
                assertThat(result).isDirectory()
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = AppConfig.instance

                // when
                val result = AppConfig.instance

                // then
                assertThat(result).isExactlyInstanceOf(AppConfig::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}