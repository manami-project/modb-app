package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestDownloadControlStateAccessor
import io.github.manamiproject.modb.app.TestMetaDataProviderConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.convfiles.CONVERTED_FILE_SUFFIX
import io.github.manamiproject.modb.app.minusWeeks
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Status.FINISHED
import io.github.manamiproject.modb.core.models.Anime.Type.MOVIE
import io.github.manamiproject.modb.core.models.AnimeSeason
import io.github.manamiproject.modb.core.models.Duration
import io.github.manamiproject.modb.core.models.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.test.Test

internal class DefaultDownloadControlStateUpdaterTest {

    @Nested
    inner class UpdateAllTests {

        @Nested
        inner class CheckForExtractionProblemsTests {

            @Test
            fun `don't trigger anything if there is no conv file`() {
                tempDirectory {
                    // given
                    val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                        override fun buildAnimeLink(id: AnimeId): URI = super.buildAnimeLink(id)
                    }

                    val rawdata = tempDir.resolve("rawdata").createDirectory()
                    val dcs = tempDir.resolve("dcs").createDirectory()

                    val testAppConfig = object: Config by TestAppConfig {
                        override fun downloadControlStateDirectory(): Directory = dcs
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testConfig)
                        override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = rawdata
                        override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                    }

                    val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                        override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                    }

                    val downloadControlStateUpdater = DefaultDownloadControlStateUpdater(
                        appConfig = testAppConfig,
                        downloadControlStateAccessor = testDownloadControlStateAccessor,
                    )

                    // when
                    downloadControlStateUpdater.updateAll()
                }
            }

            @Test
            fun `don't throw an exception if the threshold hasn't been reached`() {
                tempDirectory {
                    // given
                    val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                        override fun buildAnimeLink(id: AnimeId): URI = super.buildAnimeLink(id)
                        override fun extractAnimeId(uri: URI): AnimeId = super.extractAnimeId(uri)
                    }

                    val rawdata = tempDir.resolve("rawdata").createDirectory()
                    val updatedAnime = Anime(
                        _title = "Test",
                        sources = hashSetOf(testConfig.buildAnimeLink("100"))
                    )
                    val rawFile = rawdata.resolve("100.$CONVERTED_FILE_SUFFIX").createFile()
                    Json.toJson(updatedAnime).writeToFile(rawFile)

                    val dcs = tempDir.resolve("dcs").createDirectory()
                    val initialAnime = Anime(
                        _title = "Test",
                        sources = hashSetOf(testConfig.buildAnimeLink("100"))
                    )
                    val downloadControlStateEntry = DownloadControlStateEntry(
                        _weeksWihoutChange = 2,
                        _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(4),
                        _nextDownload = WeekOfYear.currentWeek(),
                        _anime = initialAnime,
                    )
                    val dcsFile = dcs.resolve(testConfig.hostname()).createDirectory().resolve("100.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                    Json.toJson(downloadControlStateEntry).writeToFile(dcsFile)

                    val testAppConfig = object: Config by TestAppConfig {
                        override fun downloadControlStateDirectory(): Directory = dcs
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testConfig)
                        override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = rawdata
                        override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                        override fun canChangeAnimeIds(metaDataProviderConfig: MetaDataProviderConfig): Boolean = false
                    }

                    val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                        override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                        override suspend fun dcsEntryExists(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): Boolean = true
                        override suspend fun dcsEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): DownloadControlStateEntry = downloadControlStateEntry
                        override suspend fun createOrUpdate(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId, downloadControlStateEntry: DownloadControlStateEntry): Boolean = false
                    }

                    val downloadControlStateUpdater = DefaultDownloadControlStateUpdater(
                        appConfig = testAppConfig,
                        downloadControlStateAccessor = testDownloadControlStateAccessor,
                    )

                    // when
                    downloadControlStateUpdater.updateAll()
                }
            }

            @Test
            fun `triggers alarm at 50 percent differences - score summed up by changes in files`() {
                tempDirectory {
                    // given
                    val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                        override fun buildAnimeLink(id: AnimeId): URI = super.buildAnimeLink(id)
                        override fun extractAnimeId(uri: URI): AnimeId = super.extractAnimeId(uri)
                    }

                    val rawdata = tempDir.resolve("rawdata").createDirectory()
                    val dcs = tempDir.resolve("dcs").createDirectory()
                    val metaDataProviderDcsDir = dcs.resolve(testConfig.hostname()).createDirectory()

                    val testAppConfig = object: Config by TestAppConfig {
                        override fun downloadControlStateDirectory(): Directory = dcs
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testConfig)
                        override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = rawdata
                        override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                        override fun canChangeAnimeIds(metaDataProviderConfig: MetaDataProviderConfig): Boolean = false
                    }

                    // Unchanged anime
                    val unchangedAnimeId = "100"
                    val unchangedAnime = Anime(
                        _title = "Unchanged anime",
                        sources = hashSetOf(testConfig.buildAnimeLink(unchangedAnimeId))
                    )
                    val rawFileUnchangedAnime = rawdata.resolve("$unchangedAnimeId.$CONVERTED_FILE_SUFFIX").createFile()
                    Json.toJson(unchangedAnime).writeToFile(rawFileUnchangedAnime)

                    val dcsUnchangedAnime = DownloadControlStateEntry(
                        _weeksWihoutChange = 2,
                        _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(4),
                        _nextDownload = WeekOfYear.currentWeek(),
                        _anime = Anime(
                            _title = "Unchanged anime",
                            sources = hashSetOf(testConfig.buildAnimeLink(unchangedAnimeId)),
                        ),
                    )
                    val dcsFileUnchangedAnime = metaDataProviderDcsDir.resolve("$unchangedAnimeId.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                    Json.toJson(dcsUnchangedAnime).writeToFile(dcsFileUnchangedAnime)

                    // Changed anime
                    val changedAnimeId = "200"
                    val changedAnime = Anime(
                        _title = "Changed Anime",
                        sources = hashSetOf(testConfig.buildAnimeLink(changedAnimeId))
                    )
                    val rawFileChangedAnime = rawdata.resolve("$changedAnimeId.$CONVERTED_FILE_SUFFIX").createFile()
                    Json.toJson(changedAnime).writeToFile(rawFileChangedAnime)

                    val dcsChangedAnime = DownloadControlStateEntry(
                        _weeksWihoutChange = 2,
                        _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(4),
                        _nextDownload = WeekOfYear.currentWeek(),
                        _anime = Anime(
                            _title = "Changed Anime",
                            episodes = 12,
                            sources = hashSetOf(testConfig.buildAnimeLink(changedAnimeId)),
                        ),
                    )
                    val dcsFileChangedAnime = metaDataProviderDcsDir.resolve("$changedAnimeId.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                    Json.toJson(dcsChangedAnime).writeToFile(dcsFileChangedAnime)

                    val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                        override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                        override suspend fun dcsEntryExists(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): Boolean = true
                        override suspend fun dcsEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): DownloadControlStateEntry = when {
                            animeId == "100" -> dcsUnchangedAnime
                            animeId == "200" -> dcsChangedAnime
                            else -> shouldNotBeInvoked()
                        }
                        override suspend fun createOrUpdate(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId, downloadControlStateEntry: DownloadControlStateEntry): Boolean = false
                    }

                    val downloadControlStateUpdater = DefaultDownloadControlStateUpdater(
                        appConfig = testAppConfig,
                        downloadControlStateAccessor = testDownloadControlStateAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        downloadControlStateUpdater.updateAll()
                    }

                    // then
                    assertThat(result).hasMessage("""
                        Possibly found a problem in the extraction of data:
                          * example.org with a percentage of 50
                    """.trimIndent())
                }
            }
        }

        @Nested
        inner class UpdateChangedIdsTests {

            @Test
            fun `throws exception if id changed although id change is not supported for this meta data provider`() {
                tempDirectory {
                    // given
                    val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                        override fun buildAnimeLink(id: AnimeId): URI = super.buildAnimeLink(id)
                        override fun extractAnimeId(uri: URI): AnimeId = super.extractAnimeId(uri)
                    }

                    val rawdata = tempDir.resolve("rawdata").createDirectory()
                    val updatedAnime = Anime(
                        _title = "Test",
                        episodes = 12,
                        sources = hashSetOf(testConfig.buildAnimeLink("new-test-id"))
                    )
                    val rawFile = rawdata.resolve("previous-test-id.$CONVERTED_FILE_SUFFIX").createFile()
                    Json.toJson(updatedAnime).writeToFile(rawFile)

                    val dcs = tempDir.resolve("dcs").createDirectory()
                    val initialAnime = Anime(
                        _title = "Test",
                        sources = hashSetOf(testConfig.buildAnimeLink("previous-test-id"))
                    )

                    val downloadControlStateEntry = DownloadControlStateEntry(
                        _weeksWihoutChange = 2,
                        _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(4),
                        _nextDownload = WeekOfYear.currentWeek(),
                        _anime = initialAnime,
                    )
                    val metaDataProviderDcsDir = dcs.resolve(testConfig.hostname()).createDirectory()
                    val dcsFile = metaDataProviderDcsDir.resolve("previous-test-id.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                    Json.toJson(downloadControlStateEntry).writeToFile(dcsFile)

                    val testAppConfig = object: Config by TestAppConfig {
                        override fun downloadControlStateDirectory(): Directory = dcs
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testConfig)
                        override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = rawdata
                        override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                        override fun canChangeAnimeIds(metaDataProviderConfig: MetaDataProviderConfig): Boolean = false
                    }

                    val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                        override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                        override suspend fun dcsEntryExists(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): Boolean = true
                        override suspend fun dcsEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): DownloadControlStateEntry = downloadControlStateEntry
                        override suspend fun createOrUpdate(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId, downloadControlStateEntry: DownloadControlStateEntry): Boolean = false
                        override suspend fun changeId(oldId: AnimeId, newId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig): RegularFile {
                            return dcsFile.parent.resolve("new-test-id.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")
                        }
                    }

                    val downloadControlStateUpdater = DefaultDownloadControlStateUpdater(
                        appConfig = testAppConfig,
                        downloadControlStateAccessor = testDownloadControlStateAccessor,
                    )

                    // when
                    val result = exceptionExpected<IllegalStateException> {
                        downloadControlStateUpdater.updateAll()
                    }

                    // then
                    assertThat(result).hasMessage("Detected ID change from [previous-test-id] to [new-test-id] although [example.org] doesn't support changing IDs.")
                }
            }

            @Test
            fun `don't throw an exception if the threshold hasn't been reached`() {
                tempDirectory {
                    // given
                    val testConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                        override fun hostname(): Hostname = "example.org"
                        override fun buildAnimeLink(id: AnimeId): URI = super.buildAnimeLink(id)
                        override fun extractAnimeId(uri: URI): AnimeId = super.extractAnimeId(uri)
                    }

                    val rawdata = tempDir.resolve("rawdata").createDirectory()
                    val updatedAnime = Anime(
                        _title = "Test",
                        episodes = 12,
                        sources = hashSetOf(testConfig.buildAnimeLink("new-test-id"))
                    )
                    val rawFile = rawdata.resolve("previous-test-id.$CONVERTED_FILE_SUFFIX").createFile()
                    Json.toJson(updatedAnime).writeToFile(rawFile)

                    val dcs = tempDir.resolve("dcs").createDirectory()
                    val initialAnime = Anime(
                        _title = "Test",
                        sources = hashSetOf(testConfig.buildAnimeLink("previous-test-id"))
                    )

                    val downloadControlStateEntry = DownloadControlStateEntry(
                        _weeksWihoutChange = 2,
                        _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(4),
                        _nextDownload = WeekOfYear.currentWeek(),
                        _anime = initialAnime,
                    )
                    val metaDataProviderDcsDir = dcs.resolve(testConfig.hostname()).createDirectory()
                    val dcsFile = metaDataProviderDcsDir.resolve("previous-test-id.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX").createFile()
                    Json.toJson(downloadControlStateEntry).writeToFile(dcsFile)

                    val testAppConfig = object: Config by TestAppConfig {
                        override fun downloadControlStateDirectory(): Directory = dcs
                        override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(testConfig)
                        override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = rawdata
                        override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = super.findMetaDataProviderConfig(host)
                        override fun canChangeAnimeIds(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                    }

                    var updaterHasBeenExecuted = false
                    val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                        override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                        override suspend fun dcsEntryExists(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): Boolean = true
                        override suspend fun dcsEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): DownloadControlStateEntry = downloadControlStateEntry
                        override suspend fun createOrUpdate(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId, downloadControlStateEntry: DownloadControlStateEntry): Boolean = false
                        override suspend fun changeId(oldId: AnimeId, newId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig): RegularFile {
                            updaterHasBeenExecuted = true
                            return dcsFile.parent.resolve("new-test-id.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX")
                        }
                    }

                    val downloadControlStateUpdater = DefaultDownloadControlStateUpdater(
                        appConfig = testAppConfig,
                        downloadControlStateAccessor = testDownloadControlStateAccessor,
                    )

                    // when
                    downloadControlStateUpdater.updateAll()

                    // then
                    assertThat(updaterHasBeenExecuted).isTrue()
                }
            }
        }

        @Test
        fun `creates dcs file if it doesn't exist`() {
            tempDirectory {
                // given
                val rawdata = tempDir.resolve("rawdata").createDirectory()
                val dcs = tempDir.resolve("dcs").createDirectory()

                val anime = Anime(
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

                val expectedDcsEntry = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear.currentWeek(),
                    _nextDownload = WeekOfYear.currentWeek().plusWeeks(1),
                    _anime = anime,
                )

                val file = rawdata.resolve("32.$CONVERTED_FILE_SUFFIX").createFile()
                Json.toJson(anime).writeToFile(file)

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = dcs
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig)
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = rawdata
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = AnilistConfig
                    override fun clock(): Clock = Clock.fixed(Instant.parse("2021-11-02T17:55:43.035Z"), ZoneId.systemDefault())
                }

                var receivedDcsEntry: DownloadControlStateEntry? = null
                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                    override suspend fun dcsEntryExists(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): Boolean = false
                    override suspend fun createOrUpdate(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId, downloadControlStateEntry: DownloadControlStateEntry): Boolean {
                        receivedDcsEntry = downloadControlStateEntry
                        return true
                    }
                }

                val downloadControlStateUpdater = DefaultDownloadControlStateUpdater(
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                downloadControlStateUpdater.updateAll()

                // then
                assertThat(receivedDcsEntry).isEqualTo(expectedDcsEntry)
            }
        }

        @Test
        fun `update existing dcs file`() {
            tempDirectory {
                // given
                val rawdata = tempDir.resolve("rawdata").createDirectory()
                val dcs = tempDir.resolve("dcs").createDirectory()

                val updatedAnime = Anime(
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

                val updatedDcsEntry = DownloadControlStateEntry(
                    _weeksWihoutChange = 0,
                    _lastDownloaded = WeekOfYear.currentWeek(),
                    _nextDownload = WeekOfYear.currentWeek().plusWeeks(1),
                    _anime = updatedAnime,
                )

                val initialDcsEntry = DownloadControlStateEntry(
                    _weeksWihoutChange = 2,
                    _lastDownloaded = WeekOfYear.currentWeek().minusWeeks(5),
                    _nextDownload = WeekOfYear.currentWeek(),
                    _anime = updatedAnime.copy(
                        duration = Duration.UNKNOWN,
                        animeSeason = AnimeSeason(
                            year = AnimeSeason.UNKNOWN_YEAR,
                        ),
                        tags = hashSetOf(
                            "action",
                            "mecha",
                            "sci-fi",
                        ),
                    ),
                )

                val file = rawdata.resolve("32.$CONVERTED_FILE_SUFFIX").createFile()
                Json.toJson(updatedAnime).writeToFile(file)

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = dcs
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig)
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = rawdata
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = AnilistConfig
                    override fun clock(): Clock = Clock.fixed(Instant.parse("2021-11-02T17:55:43.035Z"), ZoneId.systemDefault())
                }

                var receivedDcsEntry: DownloadControlStateEntry? = null
                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                    override suspend fun dcsEntryExists(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): Boolean = true
                    override suspend fun dcsEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): DownloadControlStateEntry = initialDcsEntry
                    override suspend fun createOrUpdate(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId, downloadControlStateEntry: DownloadControlStateEntry): Boolean {
                        receivedDcsEntry = downloadControlStateEntry
                        return true
                    }
                }

                val downloadControlStateUpdater = DefaultDownloadControlStateUpdater(
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                downloadControlStateUpdater.updateAll()

                // then
                assertThat(receivedDcsEntry).isEqualTo(updatedDcsEntry)
            }
        }

        @Test
        fun `can handle same entries with different IDs - the case in which the anime id has changed`() {
            tempDirectory {
                // given
                val rawdata = tempDir.resolve("rawdata").createDirectory()
                val dcs = tempDir.resolve("dcs").createDirectory()

                val anime = Anime(
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

                val file1 = rawdata.resolve("32.$CONVERTED_FILE_SUFFIX").createFile()
                Json.toJson(anime).writeToFile(file1)

                val file2 = rawdata.resolve("64.$CONVERTED_FILE_SUFFIX").createFile()
                Json.toJson(anime).writeToFile(file2)

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = dcs
                    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(AnilistConfig)
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = rawdata
                    override fun findMetaDataProviderConfig(host: Hostname): MetaDataProviderConfig = AnilistConfig
                    override fun canChangeAnimeIds(metaDataProviderConfig: MetaDataProviderConfig): Boolean = true
                    override fun clock(): Clock = Clock.fixed(Instant.parse("2021-11-02T17:55:43.035Z"), ZoneId.systemDefault())
                }

                var invocations = 0
                var hasBeenInvoked = false
                val testDownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                    override suspend fun changeId(oldId: AnimeId, newId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig): RegularFile {
                        hasBeenInvoked = true
                        return file1
                    }
                    override suspend fun dcsEntryExists(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): Boolean = false
                    override suspend fun createOrUpdate(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId, downloadControlStateEntry: DownloadControlStateEntry): Boolean {
                        invocations++
                        return true
                    }
                }

                val downloadControlStateUpdater = DefaultDownloadControlStateUpdater(
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                downloadControlStateUpdater.updateAll()

                // then
                assertThat(invocations).isEqualTo(2)
                assertThat(hasBeenInvoked).isTrue()
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = DefaultDownloadControlStateUpdater.instance

                // when
                val result = DefaultDownloadControlStateUpdater.instance

                // then
                assertThat(result).isExactlyInstanceOf(DefaultDownloadControlStateUpdater::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}