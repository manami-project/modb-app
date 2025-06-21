package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.app.*
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.convfiles.CONVERTED_FILE_SUFFIX
import io.github.manamiproject.modb.app.merging.lock.MergeLockAccessor
import io.github.manamiproject.modb.core.anime.AnimeRaw
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.date.WeekOfYear
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import io.github.manamiproject.modb.notify.NotifyConfig
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import java.time.LocalDate
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.test.Test

internal class DefaultDownloadControlStateAccessorTest {

    @Nested
    inner class DownloadControlStateDirectoryTests {

        @Test
        fun `creates dcs directory if it doesn't exist yet`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultDownloadControlStateAccessor.downloadControlStateDirectory(MyanimelistConfig)

                // then
                assertThat(result).exists()
                assertThat(result).isDirectory()
            }
        }

        @Test
        fun `doesn't throw an error if the directory already exists`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                testAppConfig.downloadControlStateDirectory().resolve(MyanimelistConfig.hostname()).createDirectories()

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultDownloadControlStateAccessor.downloadControlStateDirectory(MyanimelistConfig)

                // then
                assertThat(result).exists()
                assertThat(result).isDirectory()
            }
        }

        @Test
        fun `each meta data provider is a subdirectory of dcs directory`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                testAppConfig.downloadControlStateDirectory().resolve(MyanimelistConfig.hostname()).createDirectories()

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultDownloadControlStateAccessor.downloadControlStateDirectory(MyanimelistConfig)

                // then
                assertThat(result.fileName()).isEqualTo(MyanimelistConfig.hostname())
                assertThat(result.parent).isEqualTo(testAppConfig.downloadControlStateDirectory())
            }
        }
    }

    @Nested
    inner class AllDcsEntriesTests {

        @Test
        fun `throws exception if the file name and the actual id from the source link don't match`() {
            tempDirectory {
                // given
                // given
                val dir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
                    .writeToFile(dir.resolve("10294.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    defaultDownloadControlStateAccessor.allDcsEntries()
                }

                // then
                assertThat(result).hasMessage("Filename and id don't match for [10294.dcs] of [anilist.co].")
            }
        }

        @Test
        fun `correctly returns all dcs entries`() {
            tempDirectory {
                // given
                val dir1 = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
                    .writeToFile(dir1.resolve("177191.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))


                val dir2 = tempDir.resolve(MyanimelistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.NullableNotSet.serializedPrettyPrint)
                    .writeToFile(dir2.resolve("1535.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig, MyanimelistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val expectedEntry1 = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(LocalDate.now().minusWeeks(1)),
                    _nextDownload = WeekOfYear.currentWeek(),
                    _anime = TestAnimeRawObjects.AllPropertiesSet.obj,
                )
                val expectedEntry2 = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(LocalDate.now().minusWeeks(1)),
                    _nextDownload = WeekOfYear.currentWeek(),
                    _anime =  TestAnimeRawObjects.NullableNotSet.obj,
                )

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                val result = defaultDownloadControlStateAccessor.allDcsEntries()

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    expectedEntry1,
                    expectedEntry2,
                )
            }
        }

        @Test
        fun `triggers initialization if necessary`() {
            tempDirectory {
                // given
                var initHasBeenInvoked = false
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        check(!initHasBeenInvoked)
                        initHasBeenInvoked = true
                        return emptySet()
                    }
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                defaultDownloadControlStateAccessor.allDcsEntries()

                // then
                assertThat(initHasBeenInvoked).isTrue()
            }
        }

        @Test
        fun `doesn't trigger init if it has already been triggered`() {
            tempDirectory {
                // given
                var initHasBeenInvoked = 0
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        initHasBeenInvoked++
                        return emptySet()
                    }
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )
                defaultDownloadControlStateAccessor.allDcsEntries()

                // when
                defaultDownloadControlStateAccessor.allDcsEntries()

                // then
                assertThat(initHasBeenInvoked).isOne()
            }
        }

        @Test
        fun `doesn't allow change by reference`() {
            tempDirectory {
                // given
                val dir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
                    .writeToFile(dir.resolve("177191.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val expectedEntry1 = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(LocalDate.now().minusWeeks(1)),
                    _nextDownload = WeekOfYear.currentWeek(),
                    _anime = TestAnimeRawObjects.AllPropertiesSet.obj,
                )

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                val otherAnime = TestAnimeRawObjects.NullableNotSet.obj

                val inMemoryDcsEntry = defaultDownloadControlStateAccessor.allDcsEntries().first().anime
                inMemoryDcsEntry.mergeWith(otherAnime)

                // when
                val result = defaultDownloadControlStateAccessor.allDcsEntries()

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    expectedEntry1,
                )
            }
        }
    }

    @Nested
    inner class AllDcsEntriesForSpecificMetaDataProviderTests {

        @Test
        fun `throws exception if the file name and the actual id from the source link don't match`() {
            tempDirectory {
                // given
                val dir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
                    .writeToFile(dir.resolve("10294.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    defaultDownloadControlStateAccessor.allDcsEntries(AnilistConfig)
                }

                // then
                assertThat(result).hasMessage("Filename and id don't match for [10294.dcs] of [anilist.co].")
            }
        }

        @Test
        fun `correctly returns only dcs entries for the requested meta data provider`() {
            tempDirectory {
                // given
                val dir1 = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
                    .writeToFile(dir1.resolve("177191.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))

                val dir2 = tempDir.resolve(MyanimelistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.NullableNotSet.serializedPrettyPrint)
                    .writeToFile(dir2.resolve("1535.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig, MyanimelistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val expectedDcsEntry = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(LocalDate.now().minusWeeks(1)),
                    _nextDownload = WeekOfYear.currentWeek(),
                    _anime = TestAnimeRawObjects.AllPropertiesSet.obj,
                )

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                val result = defaultDownloadControlStateAccessor.allDcsEntries(AnilistConfig)

                // then
                assertThat(result).containsExactly(expectedDcsEntry)
            }
        }

        @Test
        fun `triggers initialization if necessary`() {
            tempDirectory {
                // given
                var initHasBeenInvoked = false
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        check(!initHasBeenInvoked)
                        initHasBeenInvoked = true
                        return emptySet()
                    }
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                defaultDownloadControlStateAccessor.allDcsEntries(AnilistConfig)

                // then
                assertThat(initHasBeenInvoked).isTrue()
            }
        }

        @Test
        fun `doesn't trigger init if it has already been triggered`() {
            tempDirectory {
                // given
                var initHasBeenInvoked = 0
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        initHasBeenInvoked++
                        return emptySet()
                    }
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )
                defaultDownloadControlStateAccessor.allDcsEntries(AnilistConfig)

                // when
                defaultDownloadControlStateAccessor.allDcsEntries(AnilistConfig)

                // then
                assertThat(initHasBeenInvoked).isOne()
            }
        }

        @Test
        fun `doesn't allow change by reference`() {
            tempDirectory {
                // given
                val dir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
                    .writeToFile(dir.resolve("177191.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val expectedEntry1 = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(LocalDate.now().minusWeeks(1)),
                    _nextDownload = WeekOfYear.currentWeek(),
                    _anime = TestAnimeRawObjects.AllPropertiesSet.obj,
                )

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                val otherAnime = TestAnimeRawObjects.NullableNotSet.obj

                val inMemoryDcsEntry = defaultDownloadControlStateAccessor.allDcsEntries(AnilistConfig).first().anime
                inMemoryDcsEntry.mergeWith(otherAnime)

                // when
                val result = defaultDownloadControlStateAccessor.allDcsEntries(AnilistConfig)

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    expectedEntry1,
                )
            }
        }
    }

    @Nested
    inner class AllAnimeTests {

        @Test
        fun `throws exception if the file name and the actual id from the source link don't match`() {
            tempDirectory {
                // given
                val dir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
                    .writeToFile(dir.resolve("10294.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    defaultDownloadControlStateAccessor.allAnime()
                }

                // then
                assertThat(result).hasMessage("Filename and id don't match for [10294.dcs] of [anilist.co].")
            }
        }

        @Test
        fun `correctly returns all anime`() {
            tempDirectory {
                // given
                val dir1 = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
                    .writeToFile(dir1.resolve("177191.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))


                val dir2 = tempDir.resolve(MyanimelistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.NullableNotSet.serializedPrettyPrint)
                    .writeToFile(dir2.resolve("1535.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig, MyanimelistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val expectedAnime1 = TestAnimeRawObjects.AllPropertiesSet.obj
                val expectedAnime2 = TestAnimeRawObjects.NullableNotSet.obj

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultDownloadControlStateAccessor.allAnime()

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    expectedAnime1,
                    expectedAnime2,
                )
            }
        }

        @Test
        fun `triggers initialization if necessary`() {
            tempDirectory {
                // given
                var initHasBeenInvoked = false
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        check(!initHasBeenInvoked)
                        initHasBeenInvoked = true
                        return emptySet()
                    }
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                defaultDownloadControlStateAccessor.allAnime()

                // then
                assertThat(initHasBeenInvoked).isTrue()
            }
        }

        @Test
        fun `doesn't trigger init if it has already been triggered`() {
            tempDirectory {
                // given
                var initHasBeenInvoked = 0
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        initHasBeenInvoked++
                        return emptySet()
                    }
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )
                defaultDownloadControlStateAccessor.allAnime()

                // when
                defaultDownloadControlStateAccessor.allAnime()

                // then
                assertThat(initHasBeenInvoked).isOne()
            }
        }

        @Test
        fun `doesn't allow change by reference`() {
            tempDirectory {
                // given
                val dir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
                    .writeToFile(dir.resolve("177191.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val expectedEntry1 = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(LocalDate.now().minusWeeks(1)),
                    _nextDownload = WeekOfYear.currentWeek(),
                    _anime = TestAnimeRawObjects.AllPropertiesSet.obj,
                )

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                val otherAnime = TestAnimeRawObjects.NullableNotSet.obj

                val inMemoryDcsEntry = defaultDownloadControlStateAccessor.allAnime().first()
                inMemoryDcsEntry.mergeWith(otherAnime)

                // when
                val result = defaultDownloadControlStateAccessor.allAnime()

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    expectedEntry1.anime,
                )
            }
        }
    }

    @Nested
    inner class AllAnimeForSpecificMetaDataProviderTests {

        @Test
        fun `throws exception if the file name and the actual id from the source link don't match`() {
            tempDirectory {
                // given
                val dir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
                    .writeToFile(dir.resolve("10294.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    defaultDownloadControlStateAccessor.allAnime(AnilistConfig)
                }

                // then
                assertThat(result).hasMessage("Filename and id don't match for [10294.dcs] of [anilist.co].")
            }
        }

        @Test
        fun `correctly returns only anime for the requested meta data provider`() {
            tempDirectory {
                // given
                val dir1 = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
                    .writeToFile(dir1.resolve("177191.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))

                val dir2 = tempDir.resolve(MyanimelistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.NullableNotSet.serializedPrettyPrint)
                    .writeToFile(dir2.resolve("1535.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig, MyanimelistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultDownloadControlStateAccessor.allAnime(AnilistConfig)

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    TestAnimeRawObjects.AllPropertiesSet.obj,
                )
            }
        }

        @Test
        fun `triggers initialization if necessary`() {
            tempDirectory {
                // given
                var initHasBeenInvoked = false
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        check(!initHasBeenInvoked)
                        initHasBeenInvoked = true
                        return emptySet()
                    }
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                defaultDownloadControlStateAccessor.allAnime(AnilistConfig)

                // then
                assertThat(initHasBeenInvoked).isTrue()
            }
        }

        @Test
        fun `doesn't trigger init if it has already been triggered`() {
            tempDirectory {
                // given
                var initHasBeenInvoked = 0
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        initHasBeenInvoked++
                        return emptySet()
                    }
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )
                defaultDownloadControlStateAccessor.allAnime(AnilistConfig)

                // when
                defaultDownloadControlStateAccessor.allAnime(AnilistConfig)

                // then
                assertThat(initHasBeenInvoked).isOne()
            }
        }

        @Test
        fun `doesn't allow change by reference`() {
            tempDirectory {
                // given
                val dir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
                    .writeToFile(dir.resolve("177191.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val expectedEntry1 = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(LocalDate.now().minusWeeks(1)),
                    _nextDownload = WeekOfYear.currentWeek(),
                    _anime = TestAnimeRawObjects.AllPropertiesSet.obj
                )

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                val otherAnime = TestAnimeRawObjects.NullableNotSet.obj

                val inMemoryDcsEntry = defaultDownloadControlStateAccessor.allAnime(AnilistConfig).first()
                inMemoryDcsEntry.mergeWith(otherAnime)

                // when
                val result = defaultDownloadControlStateAccessor.allAnime(AnilistConfig)

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    expectedEntry1.anime,
                )
            }
        }
    }

    @Nested
    inner class DcsEntryExistsTests {

        @Test
        fun `returns true if an entry exists`() {
            tempDirectory {
                // given
                val dir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
                    .writeToFile(dir.resolve("177191.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig, MyanimelistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                val result = defaultDownloadControlStateAccessor.dcsEntryExists(AnilistConfig, "177191")

                // then
                assertThat(result).isTrue()
            }
        }

        @Test
        fun `returns false if an entry doesn't exists`() {
            tempDirectory {
                // given
                val dir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
                    .writeToFile(dir.resolve("177191.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig, MyanimelistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                val result = defaultDownloadControlStateAccessor.dcsEntryExists(MyanimelistConfig, "1535")

                // then
                assertThat(result).isFalse()
            }
        }

        @Test
        fun `triggers initialization if necessary`() {
            tempDirectory {
                // given
                var initHasBeenInvoked = false
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        check(!initHasBeenInvoked)
                        initHasBeenInvoked = true
                        return emptySet()
                    }
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                val result = defaultDownloadControlStateAccessor.dcsEntryExists(MyanimelistConfig, "1535")

                // then
                assertThat(result).isFalse()
                assertThat(initHasBeenInvoked).isTrue()
            }
        }

        @Test
        fun `doesn't trigger init if it has already been triggered`() {
            tempDirectory {
                // given
                var initHasBeenInvoked = 0
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        initHasBeenInvoked++
                        return emptySet()
                    }
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )
                defaultDownloadControlStateAccessor.dcsEntryExists(MyanimelistConfig, "1535")

                // when
                val result = defaultDownloadControlStateAccessor.dcsEntryExists(MyanimelistConfig, "1535")

                // then
                assertThat(result).isFalse()
                assertThat(initHasBeenInvoked).isOne()
            }
        }
    }

    @Nested
    inner class DcsEntryTests {

        @Test
        fun `correctly returns an existing DCS entry`() {
            tempDirectory {
                // given
                val dir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
                    .writeToFile(dir.resolve("177191.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig, MyanimelistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val expectedDcsEntry = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(LocalDate.now().minusWeeks(1)),
                    _nextDownload = WeekOfYear.currentWeek(),
                    _anime = TestAnimeRawObjects.AllPropertiesSet.obj,
                )

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                val result = defaultDownloadControlStateAccessor.dcsEntry(AnilistConfig, "177191")

                // then
                assertThat(result).isEqualTo(expectedDcsEntry)
            }
        }

        @Test
        fun `throws an exception if the requested entry doesn't exist`() {
            tempDirectory {
                // given
                val dir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
                    .writeToFile(dir.resolve("177191.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig, MyanimelistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    defaultDownloadControlStateAccessor.dcsEntry(MyanimelistConfig, "1029")
                }

                // then
                assertThat(result).hasMessage("Requested DCS entry with internal id [myanimelist.net-1029] doesnt exist.")
            }
        }

        @Test
        fun `triggers initialization if necessary`() {
            tempDirectory {
                // given
                val dir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
                    .writeToFile(dir.resolve("177191.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))

                var initHasBeenInvoked = false
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        check(!initHasBeenInvoked)
                        initHasBeenInvoked = true
                        return setOf(AnilistConfig)
                    }
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = AnilistConfig
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                defaultDownloadControlStateAccessor.dcsEntry(AnilistConfig, "177191")

                // then
                assertThat(initHasBeenInvoked).isTrue()
            }
        }

        @Test
        fun `doesn't trigger init if it has already been triggered`() {
            tempDirectory {
                // given
                val dir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
                    .writeToFile(dir.resolve("177191.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))

                var initHasBeenInvoked = 0
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        initHasBeenInvoked++
                        return setOf(AnilistConfig)
                    }
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = AnilistConfig
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )
                defaultDownloadControlStateAccessor.dcsEntry(AnilistConfig, "177191")

                // when
                defaultDownloadControlStateAccessor.dcsEntry(AnilistConfig, "177191")

                // then
                assertThat(initHasBeenInvoked).isOne()
            }
        }

        @Test
        fun `doesn't allow change by reference`() {
            tempDirectory {
                // given
                val dir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                createExpectedDcsEntry(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
                    .writeToFile(dir.resolve("177191.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX"))

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val expectedEntry1 = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(LocalDate.now().minusWeeks(1)),
                    _nextDownload = WeekOfYear.currentWeek(),
                    _anime = TestAnimeRawObjects.AllPropertiesSet.obj,
                )

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                val otherAnime = TestAnimeRawObjects.NullableNotSet.obj

                val inMemoryDcsEntry = defaultDownloadControlStateAccessor.dcsEntry(AnilistConfig, "177191").anime
                inMemoryDcsEntry.mergeWith(otherAnime)

                // when
                val result = defaultDownloadControlStateAccessor.dcsEntry(AnilistConfig, "177191")

                // then
                assertThat(result).isEqualTo(expectedEntry1)
            }
        }
    }

    @Nested
    inner class CreateOrUpdateTests {

        @Test
        fun `successfully create a new DCS entry`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val dcsEntry = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(LocalDate.now().minusWeeks(1)),
                    _nextDownload = WeekOfYear.currentWeek(),
                    _anime = TestAnimeRawObjects.AllPropertiesSet.obj,
                )

                val expectedFile = createExpectedDcsEntry(
                    TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint,
                )
                val outputDir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                val result = defaultDownloadControlStateAccessor.createOrUpdate(AnilistConfig, "177191", dcsEntry)

                // then
                assertThat(result).isTrue()
                val fileContent = outputDir.resolve("177191.dcs").readFile()
                assertThat(fileContent).isEqualTo(expectedFile)
            }
        }

        @Test
        fun `successfully update an existing DCS entry`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val previousEntry = DownloadControlStateEntry(
                    _weeksWihoutChange = 2,
                    _lastDownloaded = WeekOfYear(LocalDate.now().minusWeeks(1)),
                    _nextDownload = WeekOfYear.currentWeek(),
                    _anime = TestAnimeRawObjects.DefaultAnime.obj,
                )

                val dcsEntry = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(LocalDate.now().minusWeeks(1)),
                    _nextDownload = WeekOfYear.currentWeek(),
                    _anime = TestAnimeRawObjects.AllPropertiesSet.obj,
                )

                val expectedFile = createExpectedDcsEntry(
                    TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint,
                )
                val outputDir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )
                defaultDownloadControlStateAccessor.createOrUpdate(AnilistConfig, "177191", previousEntry)
                val correctForPreviousVersion = defaultDownloadControlStateAccessor.dcsEntry(AnilistConfig, "177191")

                // when
                val result = defaultDownloadControlStateAccessor.createOrUpdate(AnilistConfig, "177191", dcsEntry)

                // then
                assertThat(result).isTrue()
                assertThat(correctForPreviousVersion).isEqualTo(previousEntry)
                val fileContent = outputDir.resolve("177191.dcs").readFile()
                assertThat(fileContent).isEqualTo(expectedFile)
                assertThat(defaultDownloadControlStateAccessor.dcsEntry(AnilistConfig, "177191")).isEqualTo(dcsEntry)
            }
        }

        @Test
        fun `don't do anything if the DCS entry has already been updated`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val dcsEntry = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear.currentWeek(),
                    _nextDownload = WeekOfYear.currentWeek().plusWeeks(1),
                    _anime = TestAnimeRawObjects.AllPropertiesSet.obj,
                )

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )
                defaultDownloadControlStateAccessor.createOrUpdate(AnilistConfig, "177191", dcsEntry)

                // when
                val result = defaultDownloadControlStateAccessor.createOrUpdate(AnilistConfig, "177191", dcsEntry)

                // then
                assertThat(result).isFalse()
            }
        }

        @Test
        fun `triggers initialization if necessary`() {
            tempDirectory {
                // given
                var initHasBeenInvoked = false
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        check(!initHasBeenInvoked)
                        initHasBeenInvoked = true
                        return setOf(AnilistConfig)
                    }
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = AnilistConfig
                }

                val dcsEntry = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(LocalDate.now().minusWeeks(1)),
                    _nextDownload = WeekOfYear.currentWeek(),
                    _anime = TestAnimeRawObjects.AllPropertiesSet.obj,
                )

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                defaultDownloadControlStateAccessor.createOrUpdate(AnilistConfig, "177191", dcsEntry)

                // then
                assertThat(initHasBeenInvoked).isTrue()
            }
        }

        @Test
        fun `doesn't trigger init if it has already been triggered`() {
            tempDirectory {
                // given
                var initHasBeenInvoked = 0
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        initHasBeenInvoked++
                        return setOf(AnilistConfig, MyanimelistConfig)
                    }
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val dcsEntry = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(LocalDate.now().minusWeeks(1)),
                    _nextDownload = WeekOfYear.currentWeek(),
                    _anime = TestAnimeRawObjects.AllPropertiesSet.obj,
                )

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )
                defaultDownloadControlStateAccessor.createOrUpdate(
                    metaDataProviderConfig = MyanimelistConfig,
                    animeId = "1535",
                    downloadControlStateEntry = DownloadControlStateEntry(
                        _weeksWihoutChange = 0,
                        _lastDownloaded = WeekOfYear(LocalDate.now().minusWeeks(1)),
                        _nextDownload = WeekOfYear.currentWeek(),
                        _anime = TestAnimeRawObjects.NullableNotSet.obj,
                    )
                )

                // when
                defaultDownloadControlStateAccessor.createOrUpdate(AnilistConfig, "177191", dcsEntry)

                // then
                assertThat(initHasBeenInvoked).isOne()
            }
        }
    }

    @Nested
    inner class RemoveDeadEntryTests {

        @Test
        fun `successfully removes file`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun buildAnimeLink(id: AnimeId): URI = super.buildAnimeLink(id)
                    override fun extractAnimeId(uri: URI): AnimeId = super.extractAnimeId(uri)
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                }

                val testMergeLockAccess = object: MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun isPartOfMergeLock(uri: URI): Boolean = false
                }

                tempDir.resolve(testMetaDataProviderConfig.hostname()).createDirectory()
                val file = tempDir.resolve(testMetaDataProviderConfig.hostname()).resolve("177191.dcs").createFile()
                val fileExistedPreviously = file.regularFileExists()

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = testMergeLockAccess,
                )

                // when
                defaultDownloadControlStateAccessor.removeDeadEntry(testMetaDataProviderConfig, "177191")

                // then
                assertThat(fileExistedPreviously).isTrue()
                assertThat(file.regularFileExists()).isFalse()
            }
        }

        @Test
        fun `handles removal of the same entry in merge lock`() {
            tempDirectory {
                // given
                val testConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun buildAnimeLink(id: AnimeId): URI = super.buildAnimeLink(id)
                    override fun extractAnimeId(uri: URI): AnimeId = super.extractAnimeId(uri)
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                }

                var hasMergeLockEntryRemovalBeingInvoked = false
                val testMergeLockAccess = object: MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun isPartOfMergeLock(uri: URI): Boolean = true
                    override suspend fun removeEntry(uri: URI) {
                        hasMergeLockEntryRemovalBeingInvoked = true
                    }
                }

                tempDir.resolve(testConfig.hostname()).createDirectory()
                tempDir.resolve(testConfig.hostname()).resolve("177191.dcs").createFile()

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = testMergeLockAccess,
                )

                // when
                defaultDownloadControlStateAccessor.removeDeadEntry(testConfig, "177191")

                // then
                assertThat(hasMergeLockEntryRemovalBeingInvoked).isTrue()
            }
        }

        @Test
        fun `checks merge locks even if dcs file doesn't exist`() {
            tempDirectory {
                // given
                val testConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun buildAnimeLink(id: AnimeId): URI = super.buildAnimeLink(id)
                    override fun extractAnimeId(uri: URI): AnimeId = super.extractAnimeId(uri)
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                }

                var hasMergeLockEntryRemovalBeingInvoked = false
                val testMergeLockAccess = object: MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun isPartOfMergeLock(uri: URI): Boolean = true
                    override suspend fun removeEntry(uri: URI) {
                        hasMergeLockEntryRemovalBeingInvoked = true
                    }
                }

                tempDir.resolve(testConfig.hostname()).createDirectory()

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = testMergeLockAccess,
                )

                // when
                defaultDownloadControlStateAccessor.removeDeadEntry(testConfig, "177191")

                // then
                assertThat(hasMergeLockEntryRemovalBeingInvoked).isTrue()
            }
        }

        @Test
        fun `doesn't do anything if neither dcs file nor merge lock exist`() {
            tempDirectory {
                // given
                val testConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun buildAnimeLink(id: AnimeId): URI = super.buildAnimeLink(id)
                    override fun extractAnimeId(uri: URI): AnimeId = super.extractAnimeId(uri)
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                }

                var hasMergeLockEntryRemovalBeingInvoked = false
                val testMergeLockAccess = object: MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun isPartOfMergeLock(uri: URI): Boolean = false
                    override suspend fun removeEntry(uri: URI) {
                        hasMergeLockEntryRemovalBeingInvoked = true
                    }
                }

                tempDir.resolve(testConfig.hostname()).createDirectory()

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = testMergeLockAccess,
                )

                // when
                defaultDownloadControlStateAccessor.removeDeadEntry(testConfig, "177191")

                // then
                assertThat(hasMergeLockEntryRemovalBeingInvoked).isFalse()
            }
        }

        @Test
        fun `triggers initialization if necessary`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun buildAnimeLink(id: AnimeId): URI = super.buildAnimeLink(id)
                    override fun extractAnimeId(uri: URI): AnimeId = super.extractAnimeId(uri)
                }

                var initHasBeenInvoked = false
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        check(!initHasBeenInvoked)
                        initHasBeenInvoked = true
                        return emptySet()
                    }
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val testMergeLockAccess = object: MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun isPartOfMergeLock(uri: URI): Boolean = false
                }

                tempDir.resolve(testMetaDataProviderConfig.hostname()).createDirectory()
                tempDir.resolve(testMetaDataProviderConfig.hostname()).resolve("177191.dcs").createFile()

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = testMergeLockAccess,
                )

                // when
                defaultDownloadControlStateAccessor.removeDeadEntry(testMetaDataProviderConfig, "177191")

                // then
                assertThat(initHasBeenInvoked).isTrue()
            }
        }

        @Test
        fun `doesn't trigger init if it has already been triggered`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun buildAnimeLink(id: AnimeId): URI = super.buildAnimeLink(id)
                    override fun extractAnimeId(uri: URI): AnimeId = super.extractAnimeId(uri)
                }

                var initHasBeenInvoked = 0
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        initHasBeenInvoked++
                        return emptySet()
                    }
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val testMergeLockAccess = object: MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun isPartOfMergeLock(uri: URI): Boolean = false
                }

                tempDir.resolve(testMetaDataProviderConfig.hostname()).createDirectory()
                tempDir.resolve(testMetaDataProviderConfig.hostname()).resolve("177191.dcs").createFile()

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = testMergeLockAccess,
                )
                defaultDownloadControlStateAccessor.removeDeadEntry(testMetaDataProviderConfig, "177191")

                // when
                defaultDownloadControlStateAccessor.removeDeadEntry(testMetaDataProviderConfig, "177191")

                // then
                assertThat(initHasBeenInvoked).isOne()
            }
        }
    }

    @Nested
    inner class ChangeIdTests {

        @Test
        fun `throws exception if meta data provider is not supported`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun canChangeAnimeIds(metaDataProviderConfig: MetaDataProviderConfig): Boolean = false
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    defaultDownloadControlStateAccessor.changeId("test", "newTest", testMetaDataProviderConfig)
                }

                // then
                assertThat(result).hasMessage("Called changeId for [example.org] which is not configured as a meta data provider that changes IDs.")
            }
        }

        @Test
        fun `throws exception if the dcs file doesn't exist`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun canChangeAnimeIds(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    defaultDownloadControlStateAccessor.changeId("test", "newTest", testMetaDataProviderConfig)
                }

                // then
                assertThat(result).hasMessage("[example.org] file [${tempDir.resolve("test.dcs").fileName()}] doesn't exist.")
            }
        }

        @Test
        fun `successfully renames dcs file`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = AnilistConfig

                val workingDir = tempDir.resolve("workingDir").createDirectory()

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = workingDir
                    override fun canChangeAnimeIds(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testMetaDataProviderConfig)
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = testMetaDataProviderConfig
                }

                val testMergeLockAccess = object : MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun isPartOfMergeLock(uri: URI): Boolean = false
                }

                val dcsSubDirectory = tempDir.resolve(testMetaDataProviderConfig.hostname()).createDirectory()
                val oldFile = dcsSubDirectory.resolve("177191.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")
                createExpectedDcsEntry(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
                    .writeToFile(oldFile)
                val newFile = dcsSubDirectory.resolve("new-id.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")

                val downloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = testMergeLockAccess,
                )

                // when
                downloadControlStateAccessor.changeId("177191", "new-id", testMetaDataProviderConfig)

                // then
                assertThat(oldFile.regularFileExists()).isFalse()
                assertThat(newFile.regularFileExists()).isTrue()
                assertThat(downloadControlStateAccessor.allDcsEntries()).hasSize(1)
                assertThat(downloadControlStateAccessor.dcsEntryExists(testMetaDataProviderConfig, "177191")).isFalse()
                assertThat(downloadControlStateAccessor.dcsEntryExists(testMetaDataProviderConfig, "new-id")).isTrue()
            }
        }

        @Test
        fun `overwrites existing dcs files`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun buildAnimeLink(id: AnimeId): URI = super.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = "html"
                }

                val workingDir = tempDir.resolve("workingDir").createDirectory()

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = workingDir
                    override fun canChangeAnimeIds(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                }

                val testMergeLockAccess = object : MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun isPartOfMergeLock(uri: URI): Boolean = false
                }

                val dcsSubDirectory = tempDir.resolve(testMetaDataProviderConfig.hostname()).createDirectory()
                val oldFile = dcsSubDirectory.resolve("previous-id.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                "override with this".writeToFile(oldFile)
                val newFile = dcsSubDirectory.resolve("new-id.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                "to be overwritten".writeToFile(newFile)

                val downloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = testMergeLockAccess,
                )

                // when
                downloadControlStateAccessor.changeId("previous-id", "new-id", testMetaDataProviderConfig)

                // then
                assertThat(oldFile.regularFileExists()).isFalse()
                assertThat(newFile.regularFileExists()).isTrue()
                assertThat(newFile.readFile()).isEqualTo("override with this")
            }
        }

        @Test
        fun `calls MergeLockAccess to replace ID in merge lock file`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun buildAnimeLink(id: AnimeId): URI = super.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = "html"
                }

                val workingDir = tempDir.resolve("workingDir").createDirectory()

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = workingDir
                    override fun canChangeAnimeIds(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                }

                var hasBeenInvoked = false
                val testMergeLockAccess = object : MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun isPartOfMergeLock(uri: URI): Boolean = true
                    override suspend fun replaceUri(oldUri: URI, newUri: URI) {
                        hasBeenInvoked = true
                    }
                }

                val dcsSubDirectory = tempDir.resolve(testMetaDataProviderConfig.hostname()).createDirectory()
                dcsSubDirectory.resolve("previous-id.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                val newFile = dcsSubDirectory.resolve("new-id.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")

                val downloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = testMergeLockAccess,
                )

                // when
                downloadControlStateAccessor.changeId("previous-id", "new-id", testMetaDataProviderConfig)

                // then
                assertThat(newFile.regularFileExists()).isTrue()
                assertThat(hasBeenInvoked).isTrue()
            }
        }

        @Test
        fun `removes conv file and html source file with old id`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun buildAnimeLink(id: AnimeId): URI = super.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = "html"
                }

                val previousId = "previous-id"
                val newId = "new-id"

                val workingDir = tempDir.resolve("workingDir").createDirectory()
                val previousIdConvFile = workingDir.resolve("$previousId.$CONVERTED_FILE_SUFFIX").createFile()
                val newIdConvFile = workingDir.resolve("$newId.$CONVERTED_FILE_SUFFIX").createFile()
                val previousIdHtmlFile = workingDir.resolve("$previousId.${testMetaDataProviderConfig.fileSuffix()}").createFile()
                val newIdHtmlFile = workingDir.resolve("$newId.${testMetaDataProviderConfig.fileSuffix()}").createFile()

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = workingDir
                    override fun canChangeAnimeIds(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = emptySet()
                }

                val testMergeLockAccess = object : MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun isPartOfMergeLock(uri: URI): Boolean = false
                }

                val dcsSubDirectory = tempDir.resolve(testMetaDataProviderConfig.hostname()).createDirectory()
                dcsSubDirectory.resolve("$previousId.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                dcsSubDirectory.resolve("$newId.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")

                val downloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = testMergeLockAccess,
                )

                // when
                downloadControlStateAccessor.changeId(previousId, newId, testMetaDataProviderConfig)

                // then
                assertThat(previousIdConvFile.regularFileExists()).isFalse()
                assertThat(newIdConvFile.regularFileExists()).isTrue()
                assertThat(previousIdHtmlFile.regularFileExists()).isFalse()
                assertThat(newIdHtmlFile.regularFileExists()).isTrue()
            }
        }

        @Test
        fun `triggers initialization if necessary`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun buildAnimeLink(id: AnimeId): URI = super.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = "html"
                }

                val workingDir = tempDir.resolve("workingDir").createDirectory()

                var initHasBeenInvoked = false
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = workingDir
                    override fun canChangeAnimeIds(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        check(!initHasBeenInvoked)
                        initHasBeenInvoked = true
                        return emptySet()
                    }
                }

                val testMergeLockAccess = object : MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun isPartOfMergeLock(uri: URI): Boolean = false
                }

                val dcsSubDirectory = tempDir.resolve(testMetaDataProviderConfig.hostname()).createDirectory()
                dcsSubDirectory.resolve("previous-id.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val downloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = testMergeLockAccess,
                )

                // when
                downloadControlStateAccessor.changeId("previous-id", "new-id", testMetaDataProviderConfig)

                // then
                assertThat(initHasBeenInvoked).isTrue()
            }
        }

        @Test
        fun `doesn't trigger init if it has already been triggered`() {
            tempDirectory {
                // given
                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun buildAnimeLink(id: AnimeId): URI = super.buildAnimeLink(id)
                    override fun fileSuffix(): FileSuffix = "html"
                }

                val workingDir = tempDir.resolve("workingDir").createDirectory()

                var initHasBeenInvoked = 0
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = workingDir
                    override fun canChangeAnimeIds(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> {
                        initHasBeenInvoked++
                        return emptySet()
                    }
                }

                val testMergeLockAccess = object : MergeLockAccessor by TestMergeLockAccessor {
                    override suspend fun isPartOfMergeLock(uri: URI): Boolean = false
                }

                val dcsSubDirectory = tempDir.resolve(testMetaDataProviderConfig.hostname()).createDirectory()
                dcsSubDirectory.resolve("previous-id1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                dcsSubDirectory.resolve("previous-id2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

                val downloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = testMergeLockAccess,
                )
                downloadControlStateAccessor.changeId("previous-id1", "new-id1", testMetaDataProviderConfig)

                // when
                downloadControlStateAccessor.changeId("previous-id2", "new-id2", testMetaDataProviderConfig)

                // then
                assertThat(initHasBeenInvoked).isOne()
            }
        }
    }

    @Nested
    inner class HighestIdAlreadyInDatasetTests {

        @Test
        fun `return zero if there are no anime`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                val result = defaultDownloadControlStateAccessor.highestIdAlreadyInDataset(testMetaDataProviderConfig)

                // then
                assertThat(result).isZero()
            }
        }

        @Test
        fun `return zero if the meta data provider doesn't use integer for anime IDs`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(NotifyConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                val downloadControlStateEntry = DownloadControlStateEntry(
                    _lastDownloaded = WeekOfYear.currentWeek(),
                    _nextDownload = WeekOfYear.currentWeek().plusWeeks(1),
                    _weeksWihoutChange = 0,
                    _anime = AnimeRaw(
                        _title = "test1",
                        _sources = hashSetOf(NotifyConfig.buildAnimeLink("3g6kj9l26")),
                    ),
                )
                defaultDownloadControlStateAccessor.createOrUpdate(NotifyConfig, "3g6kj9l26", downloadControlStateEntry)

                // when
                val result = defaultDownloadControlStateAccessor.highestIdAlreadyInDataset(NotifyConfig)

                // then
                assertThat(result).isZero()
            }
        }

        @Test
        fun `correctly returns highest ID`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                val downloadControlStateEntry1 = DownloadControlStateEntry(
                    _lastDownloaded = WeekOfYear.currentWeek(),
                    _nextDownload = WeekOfYear.currentWeek().plusWeeks(1),
                    _weeksWihoutChange = 0,
                    _anime = AnimeRaw(
                        _title = "test1",
                        _sources = hashSetOf(AnilistConfig.buildAnimeLink("4")),
                    ),
                )
                defaultDownloadControlStateAccessor.createOrUpdate(AnilistConfig, "4", downloadControlStateEntry1)

                val downloadControlStateEntry2 = DownloadControlStateEntry(
                    _lastDownloaded = WeekOfYear.currentWeek(),
                    _nextDownload = WeekOfYear.currentWeek().plusWeeks(1),
                    _weeksWihoutChange = 0,
                    _anime = AnimeRaw(
                        _title = "test3",
                        _sources = hashSetOf(AnilistConfig.buildAnimeLink("179")),
                    ),
                )
                defaultDownloadControlStateAccessor.createOrUpdate(AnilistConfig, "179", downloadControlStateEntry2)

                val downloadControlStateEntry3 = DownloadControlStateEntry(
                    _lastDownloaded = WeekOfYear.currentWeek(),
                    _nextDownload = WeekOfYear.currentWeek().plusWeeks(1),
                    _weeksWihoutChange = 0,
                    _anime = AnimeRaw(
                        _title = "test2",
                        _sources = hashSetOf(AnilistConfig.buildAnimeLink("25")),
                    ),
                )
                defaultDownloadControlStateAccessor.createOrUpdate(AnilistConfig, "25", downloadControlStateEntry3)


                // when
                val result = defaultDownloadControlStateAccessor.highestIdAlreadyInDataset(AnilistConfig)

                // then
                assertThat(result).isEqualTo(179)
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = DefaultDownloadControlStateAccessor.instance

                // when
                val result = DefaultDownloadControlStateAccessor.instance

                // then
                assertThat(result).isExactlyInstanceOf(DefaultDownloadControlStateAccessor::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}