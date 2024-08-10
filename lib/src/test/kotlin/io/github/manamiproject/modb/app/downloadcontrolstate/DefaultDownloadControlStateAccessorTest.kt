package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestMergeLockAccess
import io.github.manamiproject.modb.app.TestMetaDataProviderConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.merging.lock.MergeLockAccess
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.copyTo
import io.github.manamiproject.modb.core.extensions.fileName
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Status.FINISHED
import io.github.manamiproject.modb.core.models.Anime.Type.MOVIE
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import io.github.manamiproject.modb.core.models.AnimeSeason
import io.github.manamiproject.modb.core.models.Duration
import io.github.manamiproject.modb.core.models.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.tempDirectory
import io.github.manamiproject.modb.test.testResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
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
                val dir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                testResource("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/ids_dont_match/10294.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").copyTo(dir)

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
        fun `correctly parses dcs files`() {
            tempDirectory {
                // given
                val dir1 = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                testResource("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/success/32.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").copyTo(dir1)

                val dir2 = tempDir.resolve(KitsuConfig.hostname()).createDirectory()
                testResource("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/success/99.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").copyTo(dir2)

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig, KitsuConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val expectedDcsEntries = setOf(
                    DownloadControlStateEntry(
                        _weeksWihoutChange = 0,
                        _lastDownloaded = WeekOfYear(2021, 44),
                        _nextDownload = WeekOfYear(2021, 45),
                        _anime = Anime(
                            _title = "Shin Seiki Evangelion Movie: THE END OF EVANGELION",
                            type = MOVIE,
                            episodes = 1,
                            picture = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/32-7YrdcGEX1FP3.png"),
                            thumbnail = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/default.jpg"),
                            status = FINISHED,
                            duration = Duration(87, MINUTES),
                            animeSeason = AnimeSeason(
                                year = 1997,
                            ),
                            sources = hashSetOf(URI("https://anilist.co/anime/32")),
                            synonyms = hashSetOf(
                                "Neon Genesis Evangelion: The End of Evangelion",
                                "新世紀エヴァンゲリオン劇場版 THE END OF EVANGELION",
                            ),
                            relatedAnime = hashSetOf(
                                URI("https://anilist.co/anime/30"),
                            ),
                            tags = hashSetOf(
                                "action",
                                "drama",
                                "mecha",
                                "psychological",
                                "sci-fi",
                            )
                        ),
                    ),
                    DownloadControlStateEntry(
                        _weeksWihoutChange = 0,
                        _lastDownloaded = WeekOfYear(2021, 44),
                        _nextDownload = WeekOfYear(2021, 45),
                        _anime = Anime(
                            _title = "Fruits Basket",
                            type = TV,
                            episodes = 26,
                            picture = URI("https://media.kitsu.io/anime/poster_images/99/small.jpg?1474922066"),
                            thumbnail = URI("https://media.kitsu.io/anime/poster_images/99/tiny.jpg?1474922066"),
                            status = FINISHED,
                            duration = Duration(24, MINUTES),
                            animeSeason = AnimeSeason(
                                year = 2001,
                            ),
                            sources = hashSetOf(URI("https://kitsu.io/anime/99")),
                            synonyms = hashSetOf(
                                "Furuba",
                                "フルーツバスケット",
                            ),
                            relatedAnime = hashSetOf(
                                URI("https://kitsu.io/anime/41995"),
                            ),
                        ),
                    ),
                )

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultDownloadControlStateAccessor.allDcsEntries()

                // then
                assertThat(result).containsAll(expectedDcsEntries)
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
                testResource("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/ids_dont_match/10294.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").copyTo(dir)

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
        fun `correctly parses dcs files`() {
            tempDirectory {
                // given
                val dir1 = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                testResource("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/success/32.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").copyTo(dir1)


                val dir2 = tempDir.resolve(KitsuConfig.hostname()).createDirectory()
                testResource("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/success/99.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").copyTo(dir2)

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig, KitsuConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val expectedAnime = setOf(
                    Anime(
                        _title = "Shin Seiki Evangelion Movie: THE END OF EVANGELION",
                        type = MOVIE,
                        episodes = 1,
                        picture = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/32-7YrdcGEX1FP3.png"),
                        thumbnail = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/default.jpg"),
                        status = FINISHED,
                        duration = Duration(87, MINUTES),
                        animeSeason = AnimeSeason(
                            year = 1997,
                        ),
                        sources = hashSetOf(URI("https://anilist.co/anime/32")),
                        synonyms = hashSetOf(
                            "Neon Genesis Evangelion: The End of Evangelion",
                            "新世紀エヴァンゲリオン劇場版 THE END OF EVANGELION",
                        ),
                        relatedAnime = hashSetOf(
                            URI("https://anilist.co/anime/30"),
                        ),
                        tags = hashSetOf(
                            "action",
                            "drama",
                            "mecha",
                            "psychological",
                            "sci-fi",
                        )
                    ),
                    Anime(
                        _title = "Fruits Basket",
                        type = TV,
                        episodes = 26,
                        picture = URI("https://media.kitsu.io/anime/poster_images/99/small.jpg?1474922066"),
                        thumbnail = URI("https://media.kitsu.io/anime/poster_images/99/tiny.jpg?1474922066"),
                        status = FINISHED,
                        duration = Duration(24, MINUTES),
                        animeSeason = AnimeSeason(
                            year = 2001,
                        ),
                        sources = hashSetOf(URI("https://kitsu.io/anime/99")),
                        synonyms = hashSetOf(
                            "Furuba",
                            "フルーツバスケット",
                        ),
                        relatedAnime = hashSetOf(
                            URI("https://kitsu.io/anime/41995"),
                        ),
                    ),
                )

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultDownloadControlStateAccessor.allAnime()

                // then
                assertThat(result).containsAll(expectedAnime)
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
                }

                val testMergeLockAccess = object: MergeLockAccess by TestMergeLockAccess {
                    override suspend fun isPartOfMergeLock(uri: URI): Boolean = false
                }

                tempDir.resolve(testMetaDataProviderConfig.hostname()).createDirectory()
                val file = tempDir.resolve(testMetaDataProviderConfig.hostname()).resolve("99.dcs").createFile()
                val fileExistedPreviously = file.regularFileExists()

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = testMergeLockAccess,
                )

                // when
                defaultDownloadControlStateAccessor.removeDeadEntry("99", testMetaDataProviderConfig)

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
                }

                var hasMergeLockEntryRemovalBeingInvoked = false
                val testMergeLockAccess = object: MergeLockAccess by TestMergeLockAccess {
                    override suspend fun isPartOfMergeLock(uri: URI): Boolean = true
                    override suspend fun removeEntry(uri: URI) {
                        hasMergeLockEntryRemovalBeingInvoked = true
                    }
                }

                tempDir.resolve(testConfig.hostname()).createDirectory()
                tempDir.resolve(testConfig.hostname()).resolve("99.dcs").createFile()

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = testMergeLockAccess,
                )

                // when
                defaultDownloadControlStateAccessor.removeDeadEntry("99", testConfig)

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
                }

                var hasMergeLockEntryRemovalBeingInvoked = false
                val testMergeLockAccess = object: MergeLockAccess by TestMergeLockAccess {
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
                defaultDownloadControlStateAccessor.removeDeadEntry("99", testConfig)

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
                }

                var hasMergeLockEntryRemovalBeingInvoked = false
                val testMergeLockAccess = object: MergeLockAccess by TestMergeLockAccess {
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
                defaultDownloadControlStateAccessor.removeDeadEntry("99", testConfig)

                // then
                assertThat(hasMergeLockEntryRemovalBeingInvoked).isFalse()
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
                assertThat(result===previous).isTrue()
            }
        }
    }
}