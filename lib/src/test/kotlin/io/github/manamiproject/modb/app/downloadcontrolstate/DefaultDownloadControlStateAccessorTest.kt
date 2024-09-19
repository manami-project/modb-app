package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestMergeLockAccessor
import io.github.manamiproject.modb.app.TestMetaDataProviderConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.convfiles.CONVERTED_FILE_SUFFIX
import io.github.manamiproject.modb.app.merging.lock.MergeLockAccessor
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.*
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
import io.github.manamiproject.modb.test.loadTestResource
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
                    mergeLockAccess = TestMergeLockAccessor,
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
        fun `correctly returns content all dcs entries`() {
            tempDirectory {
                // given
                val dir1 = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                testResource("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/success/32.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").copyTo(dir1)

                val dir2 = tempDir.resolve(KitsuConfig.hostname()).createDirectory()
                testResource("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/success/99.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").copyTo(dir2)

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig, KitsuConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val expectedEntry1 = DownloadControlStateEntry(
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
                        ),
                    )
                )
                val expectedEntry2 = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear(2021, 44),
                    _nextDownload = WeekOfYear(2021, 45),
                    _anime = Anime(
                        _title = "Fruits Basket",
                        type = TV,
                        episodes = 26,
                        picture = URI("https://media.kitsu.app/anime/poster_images/99/small.jpg?1474922066"),
                        thumbnail = URI("https://media.kitsu.app/anime/poster_images/99/tiny.jpg?1474922066"),
                        status = FINISHED,
                        duration = Duration(24, MINUTES),
                        animeSeason = AnimeSeason(
                            year = 2001,
                        ),
                        sources = hashSetOf(URI("https://kitsu.app/anime/99")),
                        synonyms = hashSetOf(
                            "Furuba",
                            "フルーツバスケット",
                        ),
                        relatedAnime = hashSetOf(
                            URI("https://kitsu.app/anime/41995"),
                        ),
                    ),
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
    }

    @Nested
    inner class AllDcsEntriesForSpecificMetaDataProviderTests {

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
                testResource("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/success/32.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").copyTo(dir1)

                val dir2 = tempDir.resolve(KitsuConfig.hostname()).createDirectory()
                testResource("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/success/99.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").copyTo(dir2)

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig, KitsuConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val expectedDcsEntry = DownloadControlStateEntry(
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
                        ),
                    ),
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
        fun `correctly returns all anime`() {
            tempDirectory {
                // given
                val dir1 = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                testResource("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/success/32.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").copyTo(dir1)


                val dir2 = tempDir.resolve(KitsuConfig.hostname()).createDirectory()
                testResource("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/success/99.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").copyTo(dir2)

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig, KitsuConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val expectedAnime1 = Anime(
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
                    ),
                )
                val expectedAnime2 = Anime(
                    _title = "Fruits Basket",
                    type = TV,
                    episodes = 26,
                    picture = URI("https://media.kitsu.app/anime/poster_images/99/small.jpg?1474922066"),
                    thumbnail = URI("https://media.kitsu.app/anime/poster_images/99/tiny.jpg?1474922066"),
                    status = FINISHED,
                    duration = Duration(24, MINUTES),
                    animeSeason = AnimeSeason(
                        year = 2001,
                    ),
                    sources = hashSetOf(URI("https://kitsu.app/anime/99")),
                    synonyms = hashSetOf(
                        "Furuba",
                        "フルーツバスケット",
                    ),
                    relatedAnime = hashSetOf(
                        URI("https://kitsu.app/anime/41995"),
                    ),
                )

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
    }

    @Nested
    inner class AllAnimeForSpecificMetaDataProviderTests {

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
                testResource("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/success/32.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").copyTo(dir1)


                val dir2 = tempDir.resolve(KitsuConfig.hostname()).createDirectory()
                testResource("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/success/99.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").copyTo(dir2)

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig, KitsuConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val expectedAnime = Anime(
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
                    ),
                )

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultDownloadControlStateAccessor.allAnime(AnilistConfig)

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    expectedAnime,
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
    }

    @Nested
    inner class DcsEntryExistsTests {

        @Test
        fun `returns true if an entry exists`() {
            tempDirectory {
                // given
                val dir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                testResource("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/success/32.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").copyTo(dir)

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig, KitsuConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                val result = defaultDownloadControlStateAccessor.dcsEntryExists(AnilistConfig, "32")

                // then
                assertThat(result).isTrue()
            }
        }

        @Test
        fun `returns false if an entry doesn't exists`() {
            tempDirectory {
                // given
                val dir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                testResource("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/success/32.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").copyTo(dir)

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig, KitsuConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                val result = defaultDownloadControlStateAccessor.dcsEntryExists(KitsuConfig, "99")

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
                val result = defaultDownloadControlStateAccessor.dcsEntryExists(KitsuConfig, "99")

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
                defaultDownloadControlStateAccessor.dcsEntryExists(KitsuConfig, "99")

                // when
                val result = defaultDownloadControlStateAccessor.dcsEntryExists(KitsuConfig, "99")

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
                testResource("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/success/32.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").copyTo(dir)

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig, KitsuConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val expectedDcsEntry = DownloadControlStateEntry(
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
                        ),
                    ),
                )

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                val result = defaultDownloadControlStateAccessor.dcsEntry(AnilistConfig, "32")

                // then
                assertThat(result).isEqualTo(expectedDcsEntry)
            }
        }

        @Test
        fun `throws an exception if the requested entry doesn't exist`() {
            tempDirectory {
                // given
                val dir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                testResource("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/success/32.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").copyTo(dir)

                val testAppConfig = object: Config by TestAppConfig {
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig, KitsuConfig)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    defaultDownloadControlStateAccessor.dcsEntry(KitsuConfig, "99")
                }

                // then
                assertThat(result).hasMessage("Requested DCS entry with internal id [kitsu.app-99] doesnt exist.")
            }
        }

        @Test
        fun `triggers initialization if necessary`() {
            tempDirectory {
                // given
                val dir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                testResource("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/success/32.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").copyTo(dir)

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
                defaultDownloadControlStateAccessor.dcsEntry(AnilistConfig, "32")

                // then
                assertThat(initHasBeenInvoked).isTrue()
            }
        }

        @Test
        fun `doesn't trigger init if it has already been triggered`() {
            tempDirectory {
                // given
                val dir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()
                testResource("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/success/32.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").copyTo(dir)

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
                defaultDownloadControlStateAccessor.dcsEntry(AnilistConfig, "32")

                // when
                defaultDownloadControlStateAccessor.dcsEntry(AnilistConfig, "32")

                // then
                assertThat(initHasBeenInvoked).isOne()
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
                        ),
                    ),
                )

                val expectedFile = loadTestResource<String>("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/success/32.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")
                val outputDir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                val result = defaultDownloadControlStateAccessor.createOrUpdate(AnilistConfig, "32", dcsEntry)

                // then
                assertThat(result).isTrue()
                val fileContent = outputDir.resolve("32.dcs").readFile()
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
                    _lastDownloaded = WeekOfYear(2021, 39),
                    _nextDownload = WeekOfYear(2021, 44),
                    _anime = Anime(
                        _title = "Shin Seiki Evangelion Movie",
                        type = MOVIE,
                        episodes = 1,
                        picture = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/32-7YrdcGEX1FP3.png"),
                        thumbnail = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/default.jpg"),
                        status = FINISHED,
                        duration = Duration.UNKNOWN,
                        animeSeason = AnimeSeason(
                            year = AnimeSeason.UNKNOWN_YEAR,
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
                            "mecha",
                            "sci-fi",
                        ),
                    ),
                )

                val dcsEntry = DownloadControlStateEntry(
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
                        ),
                    ),
                )

                val expectedFile = loadTestResource<String>("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/success/32.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")
                val outputDir = tempDir.resolve(AnilistConfig.hostname()).createDirectory()

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )
                defaultDownloadControlStateAccessor.createOrUpdate(AnilistConfig, "32", previousEntry)
                val correctForPreviousVersion = defaultDownloadControlStateAccessor.dcsEntry(AnilistConfig, "32")

                // when
                val result = defaultDownloadControlStateAccessor.createOrUpdate(AnilistConfig, "32", dcsEntry)

                // then
                assertThat(result).isTrue()
                assertThat(correctForPreviousVersion).isEqualTo(previousEntry)
                val fileContent = outputDir.resolve("32.dcs").readFile()
                assertThat(fileContent).isEqualTo(expectedFile)
                assertThat(defaultDownloadControlStateAccessor.dcsEntry(AnilistConfig, "32")).isEqualTo(dcsEntry)
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
                        ),
                    ),
                )

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )
                defaultDownloadControlStateAccessor.createOrUpdate(AnilistConfig, "32", dcsEntry)

                // when
                val result = defaultDownloadControlStateAccessor.createOrUpdate(AnilistConfig, "32", dcsEntry)

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
                        ),
                    ),
                )

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )

                // when
                defaultDownloadControlStateAccessor.createOrUpdate(AnilistConfig, "32", dcsEntry)

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
                        return setOf(AnilistConfig, KitsuConfig)
                    }
                    override fun downloadControlStateDirectory(): Directory = tempDir
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                }

                val dcsEntry = DownloadControlStateEntry(
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
                        ),
                    ),
                )

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = TestMergeLockAccessor,
                )
                defaultDownloadControlStateAccessor.createOrUpdate(
                    metaDataProviderConfig = KitsuConfig,
                    animeId = "99",
                    downloadControlStateEntry = DownloadControlStateEntry(
                        _weeksWihoutChange = 0,
                        _lastDownloaded = WeekOfYear(2021, 44),
                        _nextDownload = WeekOfYear(2021, 45),
                        _anime = Anime(
                            _title = "Fruits Basket",
                            type = TV,
                            episodes = 26,
                            picture = URI("https://media.kitsu.app/anime/poster_images/99/small.jpg?1474922066"),
                            thumbnail = URI("https://media.kitsu.app/anime/poster_images/99/tiny.jpg?1474922066"),
                            status = FINISHED,
                            duration = Duration(24, MINUTES),
                            animeSeason = AnimeSeason(
                                year = 2001,
                            ),
                            sources = hashSetOf(URI("https://kitsu.app/anime/99")),
                            synonyms = hashSetOf(
                                "Furuba",
                                "フルーツバスケット",
                            ),
                            relatedAnime = hashSetOf(
                                URI("https://kitsu.app/anime/41995"),
                            ),
                        ),
                    )
                )

                // when
                defaultDownloadControlStateAccessor.createOrUpdate(AnilistConfig, "32", dcsEntry)

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
                val file = tempDir.resolve(testMetaDataProviderConfig.hostname()).resolve("99.dcs").createFile()
                val fileExistedPreviously = file.regularFileExists()

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = testMergeLockAccess,
                )

                // when
                defaultDownloadControlStateAccessor.removeDeadEntry(testMetaDataProviderConfig, "99")

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
                tempDir.resolve(testConfig.hostname()).resolve("99.dcs").createFile()

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = testMergeLockAccess,
                )

                // when
                defaultDownloadControlStateAccessor.removeDeadEntry(testConfig, "99")

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
                defaultDownloadControlStateAccessor.removeDeadEntry(testConfig, "99")

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
                defaultDownloadControlStateAccessor.removeDeadEntry(testConfig, "99")

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
                tempDir.resolve(testMetaDataProviderConfig.hostname()).resolve("99.dcs").createFile()

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = testMergeLockAccess,
                )

                // when
                defaultDownloadControlStateAccessor.removeDeadEntry(testMetaDataProviderConfig, "99")

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
                tempDir.resolve(testMetaDataProviderConfig.hostname()).resolve("99.dcs").createFile()

                val defaultDownloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = testMergeLockAccess,
                )
                defaultDownloadControlStateAccessor.removeDeadEntry(testMetaDataProviderConfig, "99")

                // when
                defaultDownloadControlStateAccessor.removeDeadEntry(testMetaDataProviderConfig, "99")

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

                val dcsSubFolder = tempDir.resolve(testMetaDataProviderConfig.hostname()).createDirectory()
                val oldFile = dcsSubFolder.resolve("32.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")
                testResource("downloadcontrolstate/DefaultDownloadControlStateAccessorTest/success/32.dcs")
                    .copyTo(oldFile)
                val newFile = dcsSubFolder.resolve("new-id.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")

                val downloadControlStateAccessor = DefaultDownloadControlStateAccessor(
                    appConfig = testAppConfig,
                    mergeLockAccess = testMergeLockAccess,
                )

                // when
                downloadControlStateAccessor.changeId("32", "new-id", testMetaDataProviderConfig)

                // then
                assertThat(oldFile.regularFileExists()).isFalse()
                assertThat(newFile.regularFileExists()).isTrue()
                assertThat(downloadControlStateAccessor.allDcsEntries()).hasSize(1)
                assertThat(downloadControlStateAccessor.dcsEntryExists(testMetaDataProviderConfig, "32")).isFalse()
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

                val dcsSubFolder = tempDir.resolve(testMetaDataProviderConfig.hostname()).createDirectory()
                val oldFile = dcsSubFolder.resolve("previous-id.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                "override with this".writeToFile(oldFile)
                val newFile = dcsSubFolder.resolve("new-id.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
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

                val dcsSubFolder = tempDir.resolve(testMetaDataProviderConfig.hostname()).createDirectory()
                dcsSubFolder.resolve("previous-id.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                val newFile = dcsSubFolder.resolve("new-id.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")

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

                val dcsSubFolder = tempDir.resolve(testMetaDataProviderConfig.hostname()).createDirectory()
                dcsSubFolder.resolve("$previousId.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                dcsSubFolder.resolve("$newId.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")

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

                val dcsSubFolder = tempDir.resolve(testMetaDataProviderConfig.hostname()).createDirectory()
                dcsSubFolder.resolve("previous-id.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

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

                val dcsSubFolder = tempDir.resolve(testMetaDataProviderConfig.hostname()).createDirectory()
                dcsSubFolder.resolve("previous-id1.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                dcsSubFolder.resolve("previous-id2.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()

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