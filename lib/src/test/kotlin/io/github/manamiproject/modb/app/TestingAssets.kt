package io.github.manamiproject.modb.app

import io.github.manamiproject.kommand.CommandExecutor
import io.github.manamiproject.kommand.CommandLineConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.convfiles.AlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.crawlers.HighestIdDetector
import io.github.manamiproject.modb.app.crawlers.LastPageMemorizer
import io.github.manamiproject.modb.app.crawlers.PaginationIdRangeSelector
import io.github.manamiproject.modb.app.dataset.DatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DatasetFileType
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateEntry
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateScheduler
import io.github.manamiproject.modb.app.merging.ReviewedIsolatedEntriesAccessor
import io.github.manamiproject.modb.app.merging.lock.MergeLock
import io.github.manamiproject.modb.app.merging.lock.MergeLockAccessor
import io.github.manamiproject.modb.app.network.NetworkController
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.ConfigRegistry
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.PathAnimeConverter
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.httpclient.RequestBody
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeRaw
import io.github.manamiproject.modb.core.anime.AnimeSeason
import io.github.manamiproject.modb.core.anime.AnimeSeason.Companion.UNKNOWN_YEAR
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.FALL
import io.github.manamiproject.modb.core.anime.AnimeStatus.FINISHED
import io.github.manamiproject.modb.core.anime.AnimeType.SPECIAL
import io.github.manamiproject.modb.core.anime.AnimeType.TV
import io.github.manamiproject.modb.core.anime.Duration
import io.github.manamiproject.modb.core.anime.Duration.Companion.UNKNOWN as UNKNOWN_DURATION
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.core.anime.MetaDataProviderScoreValue
import io.github.manamiproject.modb.core.anime.NoScore
import io.github.manamiproject.modb.core.anime.ScoreValue
import io.github.manamiproject.modb.serde.json.ExternalResourceJsonDeserializer
import io.github.manamiproject.modb.serde.json.JsonSerializer
import io.github.manamiproject.modb.serde.json.models.Dataset
import io.github.manamiproject.modb.serde.json.models.DeadEntries
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import kotlinx.coroutines.Deferred
import java.net.URI
import java.net.URL
import java.nio.file.Path
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.Watchable
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit
import java.nio.file.WatchService as JavaWatchService

internal object TestConfigRegistry: ConfigRegistry {
    override fun boolean(key: String): Boolean = shouldNotBeInvoked()
    override fun double(key: String): Double = shouldNotBeInvoked()
    override fun int(key: String): Int = shouldNotBeInvoked()
    override fun <T : Any> list(key: String): List<T> = shouldNotBeInvoked()
    override fun localDate(key: String): LocalDate = shouldNotBeInvoked()
    override fun localDateTime(key: String): LocalDateTime = shouldNotBeInvoked()
    override fun long(key: String): Long = shouldNotBeInvoked()
    override fun <T : Any> map(key: String): Map<String, T> = shouldNotBeInvoked()
    override fun offsetDateTime(key: String): OffsetDateTime = shouldNotBeInvoked()
    override fun string(key: String): String = shouldNotBeInvoked()
}

internal object TestMetaDataProviderConfig: MetaDataProviderConfig {
    override fun hostname() = shouldNotBeInvoked()
    override fun buildAnimeLink(id: AnimeId): URI = shouldNotBeInvoked()
    override fun buildDataDownloadLink(id: String): URI = shouldNotBeInvoked()
    override fun extractAnimeId(uri: URI): AnimeId = shouldNotBeInvoked()
    override fun fileSuffix(): FileSuffix = shouldNotBeInvoked()
}

internal object TestAppConfig: Config {
    override fun isTestContext(): Boolean = true
    override fun currentWeekWorkingDir(): Directory = shouldNotBeInvoked()
    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = shouldNotBeInvoked()
    override fun outputDirectory(): Directory = shouldNotBeInvoked()
    override fun downloadControlStateDirectory(): Directory = shouldNotBeInvoked()
    override fun downloadsDirectory(): Directory = shouldNotBeInvoked()
    override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = shouldNotBeInvoked()
    override fun clock(): Clock = shouldNotBeInvoked()
    override fun canChangeAnimeIds(metaDataProviderConfig: MetaDataProviderConfig): Boolean = shouldNotBeInvoked()
}

internal object TestPathAnimeConverter: PathAnimeConverter {
    override suspend fun convert(path: Path): Collection<AnimeRaw> = shouldNotBeInvoked()
}

internal object TestExternalResourceJsonDeserializerDataset: ExternalResourceJsonDeserializer<Dataset> {
    override suspend fun deserialize(url: URL): Dataset = shouldNotBeInvoked()
    override suspend fun deserialize(file: RegularFile): Dataset = shouldNotBeInvoked()
}

internal object TestJsonSerializerCollectionAnime: JsonSerializer<Collection<Anime>> {
    override suspend fun serialize(obj: Collection<Anime>, minify: Boolean): String = shouldNotBeInvoked()
}

internal object TestJavaWatchService: JavaWatchService {
    override fun close() = shouldNotBeInvoked()
    override fun poll(): WatchKey = shouldNotBeInvoked()
    override fun poll(timeout: Long, unit: TimeUnit?): WatchKey = shouldNotBeInvoked()
    override fun take(): WatchKey = shouldNotBeInvoked()
}

internal object TestWatchKey: WatchKey {
    override fun isValid(): Boolean = shouldNotBeInvoked()
    override fun pollEvents(): MutableList<WatchEvent<*>> = shouldNotBeInvoked()
    override fun reset(): Boolean = shouldNotBeInvoked()
    override fun cancel() = shouldNotBeInvoked()
    override fun watchable(): Watchable = shouldNotBeInvoked()
}

internal object TestDownloadControlStateAccessor: DownloadControlStateAccessor {
    override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = shouldNotBeInvoked()
    override suspend fun allAnime(): List<AnimeRaw> = shouldNotBeInvoked()
    override suspend fun allAnime(metaDataProviderConfig: MetaDataProviderConfig): List<AnimeRaw> = shouldNotBeInvoked()
    override suspend fun allDcsEntries(): List<DownloadControlStateEntry> = shouldNotBeInvoked()
    override suspend fun allDcsEntries(metaDataProviderConfig: MetaDataProviderConfig): List<DownloadControlStateEntry> = shouldNotBeInvoked()
    override suspend fun dcsEntryExists(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): Boolean = shouldNotBeInvoked()
    override suspend fun dcsEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId): DownloadControlStateEntry = shouldNotBeInvoked()
    override suspend fun createOrUpdate(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId, downloadControlStateEntry: DownloadControlStateEntry) = shouldNotBeInvoked()
    override suspend fun removeDeadEntry(metaDataProviderConfig: MetaDataProviderConfig, animeId: AnimeId) = shouldNotBeInvoked()
    override suspend fun changeId(oldId: AnimeId, newId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig): RegularFile = shouldNotBeInvoked()
    override suspend fun highestIdAlreadyInDataset(metaDataProviderConfig: MetaDataProviderConfig): Int = shouldNotBeInvoked()
}

internal object TestDatasetFileAccessor: DatasetFileAccessor {
    override suspend fun fetchEntries(): List<Anime> = shouldNotBeInvoked()
    override suspend fun saveEntries(anime: List<Anime>) = shouldNotBeInvoked()
    override fun offlineDatabaseFile(type: DatasetFileType): RegularFile = shouldNotBeInvoked()
}

internal object TestMergeLockAccessor: MergeLockAccessor {
    override suspend fun hasMergeLock(uris: Set<URI>): Boolean = shouldNotBeInvoked()
    override suspend fun isPartOfMergeLock(uri: URI): Boolean = shouldNotBeInvoked()
    override suspend fun getMergeLock(uri: URI): MergeLock = shouldNotBeInvoked()
    override suspend fun addMergeLock(mergeLock: MergeLock) = shouldNotBeInvoked()
    override suspend fun replaceUri(oldUri: URI, newUri: URI) = shouldNotBeInvoked()
    override suspend fun removeEntry(uri: URI) = shouldNotBeInvoked()
    override suspend fun allSourcesInAllMergeLockEntries(): Set<URI> = shouldNotBeInvoked()
}

internal object TestExternalResourceJsonDeserializerDeadEntries: ExternalResourceJsonDeserializer<DeadEntries> {
    override suspend fun deserialize(url: URL): DeadEntries = shouldNotBeInvoked()
    override suspend fun deserialize(file: RegularFile): DeadEntries = shouldNotBeInvoked()
}

internal object TestJsonSerializerCollectionAnimeId: JsonSerializer<Collection<AnimeId>> {
    override suspend fun serialize(obj: Collection<AnimeId>, minify: Boolean): String = shouldNotBeInvoked()
}

internal object TestDeadEntriesAccessor: DeadEntriesAccessor {
    override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = shouldNotBeInvoked()
    override suspend fun addDeadEntry(animeId: AnimeId, metaDataProviderConfig: MetaDataProviderConfig) = shouldNotBeInvoked()
    override suspend fun determineDeadEntries(sources: Collection<URI>): Set<URI> = shouldNotBeInvoked()
    override suspend fun fetchDeadEntries(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = shouldNotBeInvoked()
}

internal object TestReviewedIsolatedEntriesAccessor: ReviewedIsolatedEntriesAccessor {
    override fun contains(uri: URI): Boolean = shouldNotBeInvoked()
    override suspend fun addCheckedEntry(uri: URI) = shouldNotBeInvoked()
}

internal object TestCommandExecutor: CommandExecutor {
    override var config: CommandLineConfig
        get() = shouldNotBeInvoked()
        set(_) = shouldNotBeInvoked()
    override fun executeCmd(command: List<String>): String = shouldNotBeInvoked()
}

internal object TestHttpClient: HttpClient {
    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = shouldNotBeInvoked()
    override suspend fun post(url: URL, requestBody: RequestBody, headers: Map<String, Collection<String>>): HttpResponse = shouldNotBeInvoked()
}

internal object TestNetworkController: NetworkController {
    override suspend fun restartAsync(): Deferred<Boolean> = shouldNotBeInvoked()
    override fun isNetworkActive(): Boolean = shouldNotBeInvoked()
}

internal object TestDownloadControlStateScheduler: DownloadControlStateScheduler {
    override suspend fun findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = shouldNotBeInvoked()
    override suspend fun findEntriesScheduledForCurrentWeek(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = shouldNotBeInvoked()
}

internal object TestAlreadyDownloadedIdsFinder: AlreadyDownloadedIdsFinder {
    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = shouldNotBeInvoked()
}

internal object TestDownloader: Downloader {
    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String = shouldNotBeInvoked()
}

internal object TestLastPageMemorizerInt: LastPageMemorizer<Int> {
    override suspend fun memorizeLastPage(page: Int) = shouldNotBeInvoked()
    override suspend fun retrieveLastPage(): Int = shouldNotBeInvoked()
}

internal object TestLastPageMemorizerString: LastPageMemorizer<String> {
    override suspend fun memorizeLastPage(page: String) = shouldNotBeInvoked()
    override suspend fun retrieveLastPage(): String = shouldNotBeInvoked()
}

internal object TestHighestIdDetector: HighestIdDetector {
    override suspend fun detectHighestId(): Int = shouldNotBeInvoked()
}

internal object TestPaginationIdRangeSelectorInt: PaginationIdRangeSelector<Int> {
    override suspend fun idDownloadList(page: Int): List<AnimeId> = shouldNotBeInvoked()
}

internal object TestPaginationIdRangeSelectorString: PaginationIdRangeSelector<String> {
    override suspend fun idDownloadList(page: String): List<AnimeId> = shouldNotBeInvoked()
}

@Suppress("unused")
internal object TestAnimeRawObjects {

    /**
     * Default anime. Only default values are set.
     */
    object DefaultAnime {

        val obj: AnimeRaw
            get() = AnimeRaw("default")

        val serializedPrettyPrint = """
            {
              "sources": [],
              "title": "default",
              "type": "UNKNOWN",
              "episodes": 0,
              "status": "UNKNOWN",
              "animeSeason": {
                "season": "UNDEFINED",
                "year": null
              },
              "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
              "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
              "duration": null,
              "scores": [],
              "synonyms": [],
              "studios": [],
              "producers": [],
              "relatedAnime": [],
              "tags": []
            }
        """.trimIndent()

        val serializedMinified = """
            {"sources":[],"title":"default","type":"UNKNOWN","episodes":0,"status":"UNKNOWN","animeSeason":{"season":"UNDEFINED"},"picture":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png","thumbnail":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png","scores":[],"synonyms":[],"studios":[],"producers":[],"relatedAnime":[],"tags":[]}
        """.trimIndent()
    }

    /**
     * Default anime of type TV with all optional fields not set to enforce `null` in serialization.
     */
    object NullableNotSet {

        val obj: AnimeRaw
            get() = AnimeRaw(
                _title = "Death Note",
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/1535"),
                ),
                type = TV,
                episodes = 37,
                status = FINISHED,
                animeSeason = AnimeSeason(
                    season = FALL,
                    year = UNKNOWN_YEAR,
                ),
                picture = URI("https://cdn.myanimelist.net/images/anime/1079/138100.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1079/138100t.jpg"),
                duration = UNKNOWN_DURATION,
                _synonyms = hashSetOf(
                    "DN",
                    "デスノート",
                ),
                _studios = hashSetOf(
                    "madhouse",
                ),
                _producers = hashSetOf(
                    "d.n. dream partners",
                    "nippon television network",
                    "shueisha",
                    "vap",
                ),
                _relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/2994"),
                ),
                _tags = hashSetOf(
                    "psychological",
                    "shounen",
                    "supernatural",
                    "suspense",
                ),
            ).apply {
                addScores(
                    MetaDataProviderScoreValue(
                        hostname = "myanimelist.net",
                        value = 8.62,
                        range = 1.0..10.0,
                    ),
                )
            }

        val serializedPrettyPrint = """
            {
              "sources": [
                "https://myanimelist.net/anime/1535"
              ],
              "title": "Death Note",
              "type": "TV",
              "episodes": 37,
              "status": "FINISHED",
              "animeSeason": {
                "season": "FALL",
                "year": null
              },
              "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
              "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
              "duration": null,
              "scores": [
                {
                  "hostname": "myanimelist.net",
                  "value": 8.62,
                  "range": {
                    "minInclusive": 1.0,
                    "maxInclusive": 10.0
                  }
                }
              ],
              "synonyms": [
                "DN",
                "デスノート"
              ],
              "studios": [
                "madhouse"
              ],
              "producers": [
                "d.n. dream partners",
                "nippon television network",
                "shueisha",
                "vap"
              ],
              "relatedAnime": [
                "https://myanimelist.net/anime/2994"
              ],
              "tags": [
                "psychological",
                "shounen",
                "supernatural",
                "suspense"
              ]
            }
        """.trimIndent()

        val serializedMinified = """
            {"sources":["https://myanimelist.net/anime/1535"],"title":"Death Note","type":"TV","episodes":37,"status":"FINISHED","animeSeason":{"season":"FALL","year":null},"picture":"https://cdn.myanimelist.net/images/anime/1079/138100.jpg","thumbnail":"https://cdn.myanimelist.net/images/anime/1079/138100t.jpg","duration":null,"scores":[{"hostname":"myanimelist.net","value":8.62,"range":{"minInclusive":1.0,"maxInclusive":10.0}}],"synonyms":["DN","デスノート"],"studios":["madhouse"],"producers":["d.n. dream partners","nippon television network","shueisha","vap"],"relatedAnime":["https://myanimelist.net/anime/2994"],"tags":["psychological","shounen","supernatural","suspense"]}
        """.trimIndent()
    }

    /**
     * A special with multiple episodes. Single meta data provider only.
     */
    object AllPropertiesSet {

        val obj: AnimeRaw
            get() = AnimeRaw(
                _title = "Go-toubun no Hanayome *",
                _sources = hashSetOf(
                    URI("https://anilist.co/anime/177191"),
                ),
                type = SPECIAL,
                episodes = 2,
                status = FINISHED,
                animeSeason = AnimeSeason(
                    season = FALL,
                    year = 2024,
                ),
                picture = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx177191-ovNZsq8EbIPY.jpg"),
                thumbnail = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx177191-ovNZsq8EbIPY.jpg"),
                duration = Duration(
                    value = 24,
                    unit = MINUTES,
                ),
                _synonyms = hashSetOf(
                    "The Quintessential Quintuplets*",
                    "五等分の花嫁*",
                ),
                _studios = hashSetOf(
                    "bibury animation studios",
                ),
                _producers = hashSetOf(
                    "dax production",
                    "nichion",
                    "pony canyon",
                ),
                _relatedAnime = hashSetOf(
                    URI("https://anilist.co/anime/131520"),
                ),
                _tags = hashSetOf(
                    "comedy",
                    "drama",
                    "ensemble cast",
                    "female harem",
                    "heterosexual",
                    "language barrier",
                    "male protagonist",
                    "marriage",
                    "primarily female cast",
                    "romance",
                    "school",
                    "shounen",
                    "twins",
                ),
            ).apply {
                addScores(
                    MetaDataProviderScoreValue(
                        hostname = "anilist.co",
                        value = 75.0,
                        range = 1.0..100.0,
                    ),
                )
            }

        val serializedPrettyPrint = """
            {
              "sources": [
                "https://anilist.co/anime/177191"
              ],
              "title": "Go-toubun no Hanayome *",
              "type": "SPECIAL",
              "episodes": 2,
              "status": "FINISHED",
              "animeSeason": {
                "season": "FALL",
                "year": 2024
              },
              "picture": "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx177191-ovNZsq8EbIPY.jpg",
              "thumbnail": "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx177191-ovNZsq8EbIPY.jpg",
              "duration": {
                "value": 1440,
                "unit": "SECONDS"
              },
              "scores": [
                {
                  "hostname": "anilist.co",
                  "value": 75.0,
                  "range": {
                    "minInclusive": 1.0,
                    "maxInclusive": 100.0
                  }
                }
              ],
              "synonyms": [
                "The Quintessential Quintuplets*",
                "五等分の花嫁*"
              ],
              "studios": [
                "bibury animation studios"
              ],
              "producers": [
                "dax production",
                "nichion",
                "pony canyon"
              ],
              "relatedAnime": [
                "https://anilist.co/anime/131520"
              ],
              "tags": [
                "comedy",
                "drama",
                "ensemble cast",
                "female harem",
                "heterosexual",
                "language barrier",
                "male protagonist",
                "marriage",
                "primarily female cast",
                "romance",
                "school",
                "shounen",
                "twins"
              ]
            }
        """.trimIndent()

        val serializedMinified = """
            {"sources":["https://anilist.co/anime/177191"],"title":"Go-toubun no Hanayome *","type":"SPECIAL","episodes":2,"status":"FINISHED","animeSeason":{"season":"FALL","year":2024},"picture":"https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx177191-ovNZsq8EbIPY.jpg","thumbnail":"https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx177191-ovNZsq8EbIPY.jpg","duration":{"value":1440,"unit":"SECONDS"},"scores":[{"hostname":"anilist.co","value":75.0,"range":{"minInclusive":1.0,"maxInclusive":100.0}}],"synonyms":["The Quintessential Quintuplets*","五等分の花嫁*"],"studios":["bibury animation studios"],"producers":["dax production","nichion","pony canyon"],"relatedAnime":["https://anilist.co/anime/131520"],"tags":["comedy","drama","ensemble cast","female harem","heterosexual","language barrier","male protagonist","marriage","primarily female cast","romance","school","shounen","twins"]}
        """.trimIndent()
    }

    /**
     * A special with multiple episodes. Contains a fully merged anime object with all meta data providers.
     */
    object FullyMergedAllPropertiesSet {

        val obj: AnimeRaw
            get() = AnimeRaw(
                _title = "5-toubun no Hanayome*",
                _sources = hashSetOf(
                    URI("https://anidb.net/anime/18603"),
                    URI("https://anilist.co/anime/177191"),
                    URI("https://anime-planet.com/anime/the-quintessential-quintuplets-specials-2"),
                    URI("https://anisearch.com/anime/19242"),
                    URI("https://kitsu.app/anime/48807"),
                    URI("https://livechart.me/anime/12646"),
                    URI("https://myanimelist.net/anime/58755"),
                    URI("https://notify.moe/anime/Gk-oD9uIR"),
                    URI("https://simkl.com/anime/2448488"),
                ),
                type = SPECIAL,
                episodes = 2,
                status = FINISHED,
                animeSeason = AnimeSeason(
                    season = FALL,
                    year = 2024,
                ),
                picture = URI("https://cdn.myanimelist.net/images/anime/1915/145336.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1915/145336t.jpg"),
                duration = Duration(
                    value = 24,
                    unit = MINUTES,
                ),
                _synonyms = hashSetOf(
                    "5-toubun no Hanayome *",
                    "Go-Toubun no Hanayome *",
                    "Go-Tōbun no Hanayome *",
                    "Go-toubun no Hanayome *",
                    "The Quintessential Quintuplets *",
                    "The Quintessential Quintuplets Movie*",
                    "The Quintessential Quintuplets Special 2",
                    "The Quintessential Quintuplets Specials 2",
                    "The Quintessential Quintuplets*",
                    "The Quintessential Quintuplets: Honeymoon Arc",
                    "五等分の花嫁*",
                    "五等分の花嫁＊",
                    "五等分的新娘＊",
                ),
                _studios = hashSetOf(
                    "bibury animation studios",
                ),
                _producers = hashSetOf(
                    "dax production",
                    "nichion",
                    "pony canyon",
                ),
                _relatedAnime = hashSetOf(
                    URI("https://anidb.net/anime/16165"),
                    URI("https://anilist.co/anime/131520"),
                    URI("https://anime-planet.com/anime/the-quintessential-quintuplets-movie"),
                    URI("https://animecountdown.com/1577789"),
                    URI("https://anisearch.com/anime/16091"),
                    URI("https://kitsu.app/anime/44229"),
                    URI("https://livechart.me/anime/10488"),
                    URI("https://livechart.me/anime/11921"),
                    URI("https://livechart.me/anime/3448"),
                    URI("https://livechart.me/anime/9428"),
                    URI("https://myanimelist.net/anime/48548"),
                    URI("https://notify.moe/anime/e7lfM8QMg"),
                    URI("https://simkl.com/anime/1577789"),
                ),
                _tags = hashSetOf(
                    "america",
                    "based on a manga",
                    "comedy",
                    "drama",
                    "ensemble cast",
                    "female harem",
                    "harem",
                    "heterosexual",
                    "japanese production",
                    "language barrier",
                    "male protagonist",
                    "marriage",
                    "new",
                    "predominantly female cast",
                    "present",
                    "primarily female cast",
                    "romance",
                    "school",
                    "sequel",
                    "shounen",
                    "siblings",
                    "time",
                    "twins",
                ),
            ).apply {
                addScores(
                    MetaDataProviderScoreValue(
                        hostname = "anidb.net",
                        value = 6.63,
                        range = 1.0..10.0,
                    ),
                    MetaDataProviderScoreValue(
                        hostname = "anilist.co",
                        value = 75.0,
                        range = 1.0..100.0,
                    ),
                    MetaDataProviderScoreValue(
                        hostname = "anime-planet.com",
                        value = 3.845,
                        range = 0.5..5.0,
                    ),
                    MetaDataProviderScoreValue(
                        hostname = "anisearch.com",
                        value = 3.9,
                        range = 0.1..5.0,
                    ),
                    MetaDataProviderScoreValue(
                        hostname = "kitsu.app",
                        value = 76.95,
                        range = 1.0..100.0,
                    ),
                    MetaDataProviderScoreValue(
                        hostname = "livechart.me",
                        value = 8.35,
                        range = 1.0..10.0,
                    ),
                    MetaDataProviderScoreValue(
                        hostname = "myanimelist.net",
                        value = 8.62,
                        range = 1.0..10.0,
                    ),
                    MetaDataProviderScoreValue(
                        hostname = "notify.moe",
                        value = 7.0,
                        range = 1.0..10.0,
                    ),
                    MetaDataProviderScoreValue(
                        hostname = "simkl.com",
                        value = 7.9,
                        range = 1.0..10.0,
                    ),
                )
            }

        val serializedPrettyPrint = """
            {
              "sources": [
                "https://anidb.net/anime/18603",
                "https://anilist.co/anime/177191",
                "https://anime-planet.com/anime/the-quintessential-quintuplets-specials-2",
                "https://anisearch.com/anime/19242",
                "https://kitsu.app/anime/48807",
                "https://livechart.me/anime/12646",
                "https://myanimelist.net/anime/58755",
                "https://notify.moe/anime/Gk-oD9uIR",
                "https://simkl.com/anime/2448488"
              ],
              "title": "5-toubun no Hanayome*",
              "type": "SPECIAL",
              "episodes": 2,
              "status": "FINISHED",
              "animeSeason": {
                "season": "FALL",
                "year": 2024
              },
              "picture": "https://cdn.myanimelist.net/images/anime/1915/145336.jpg",
              "thumbnail": "https://cdn.myanimelist.net/images/anime/1915/145336t.jpg",
              "duration": {
                "value": 1440,
                "unit": "SECONDS"
              },
              "scores": [
                {
                  "hostname": "anidb.net",
                  "value": 6.63,
                  "range": {
                    "minInclusive": 1.0,
                    "maxInclusive": 10.0
                  }
                },
                {
                  "hostname": "anilist.co",
                  "value": 75.0,
                  "range": {
                    "minInclusive": 1.0,
                    "maxInclusive": 100.0
                  }
                },
                {
                  "hostname": "anime-planet.com",
                  "value": 3.845,
                  "range": {
                    "minInclusive": 0.5,
                    "maxInclusive": 5.0
                  }
                },
                {
                  "hostname": "anisearch.com",
                  "value": 3.9,
                  "range": {
                    "minInclusive": 0.1,
                    "maxInclusive": 5.0
                  }
                },
                {
                  "hostname": "kitsu.app",
                  "value": 76.95,
                  "range": {
                    "minInclusive": 1.0,
                    "maxInclusive": 100.0
                  }
                },
                {
                  "hostname": "livechart.me",
                  "value": 8.35,
                  "range": {
                    "minInclusive": 1.0,
                    "maxInclusive": 10.0
                  }
                },
                {
                  "hostname": "myanimelist.net",
                  "value": 8.62,
                  "range": {
                    "minInclusive": 1.0,
                    "maxInclusive": 10.0
                  }
                },
                {
                  "hostname": "notify.moe",
                  "value": 7.0,
                  "range": {
                    "minInclusive": 1.0,
                    "maxInclusive": 10.0
                  }
                },
                {
                  "hostname": "simkl.com",
                  "value": 7.9,
                  "range": {
                    "minInclusive": 1.0,
                    "maxInclusive": 10.0
                  }
                }
              ],
              "synonyms": [
                "5-toubun no Hanayome *",
                "Go-Toubun no Hanayome *",
                "Go-Tōbun no Hanayome *",
                "Go-toubun no Hanayome *",
                "The Quintessential Quintuplets *",
                "The Quintessential Quintuplets Movie*",
                "The Quintessential Quintuplets Special 2",
                "The Quintessential Quintuplets Specials 2",
                "The Quintessential Quintuplets*",
                "The Quintessential Quintuplets: Honeymoon Arc",
                "五等分の花嫁*",
                "五等分の花嫁＊",
                "五等分的新娘＊"
              ],
              "studios": [
                "bibury animation studios"
              ],
              "producers": [
                "dax production",
                "nichion",
                "pony canyon"
              ],
              "relatedAnime": [
                "https://anidb.net/anime/16165",
                "https://anilist.co/anime/131520",
                "https://anime-planet.com/anime/the-quintessential-quintuplets-movie",
                "https://animecountdown.com/1577789",
                "https://anisearch.com/anime/16091",
                "https://kitsu.app/anime/44229",
                "https://livechart.me/anime/10488",
                "https://livechart.me/anime/11921",
                "https://livechart.me/anime/3448",
                "https://livechart.me/anime/9428",
                "https://myanimelist.net/anime/48548",
                "https://notify.moe/anime/e7lfM8QMg",
                "https://simkl.com/anime/1577789"
              ],
              "tags": [
                "america",
                "based on a manga",
                "comedy",
                "drama",
                "ensemble cast",
                "female harem",
                "harem",
                "heterosexual",
                "japanese production",
                "language barrier",
                "male protagonist",
                "marriage",
                "new",
                "predominantly female cast",
                "present",
                "primarily female cast",
                "romance",
                "school",
                "sequel",
                "shounen",
                "siblings",
                "time",
                "twins"
              ]
            }
        """.trimIndent()

        val serializedMinified = """
            {"sources":["https://anidb.net/anime/18603","https://anilist.co/anime/177191","https://anime-planet.com/anime/the-quintessential-quintuplets-specials-2","https://anisearch.com/anime/19242","https://kitsu.app/anime/48807","https://livechart.me/anime/12646","https://myanimelist.net/anime/58755","https://notify.moe/anime/Gk-oD9uIR","https://simkl.com/anime/2448488"],"title":"5-toubun no Hanayome*","type":"SPECIAL","episodes":2,"status":"FINISHED","animeSeason":{"season":"FALL","year":2024},"picture":"https://cdn.myanimelist.net/images/anime/1915/145336.jpg","thumbnail":"https://cdn.myanimelist.net/images/anime/1915/145336t.jpg","duration":{"value":1440,"unit":"SECONDS"},"scores":[{"hostname":"anidb.net","value":6.63,"range":{"minInclusive":1,"maxInclusive":10}},{"hostname":"anilist.co","value":75,"range":{"minInclusive":1,"maxInclusive":100}},{"hostname":"anime-planet.com","value":3.845,"range":{"minInclusive":0.5,"maxInclusive":5.0}},{"hostname":"anisearch.com","value":3.9,"range":{"minInclusive":0.1,"maxInclusive":5}},{"hostname":"kitsu.app","value":76.95,"range":{"minInclusive":1,"maxInclusive":100}},{"hostname":"livechart.me","value":8.35,"range":{"minInclusive":1,"maxInclusive":10}},{"hostname":"myanimelist.net","value":8.62,"range":{"minInclusive":1,"maxInclusive":10}},{"hostname":"notify.moe","value":7,"range":{"minInclusive":1,"maxInclusive":10}},{"hostname":"simkl.com","value":7.9,"range":{"minInclusive":1,"maxInclusive":10}}],"synonyms":["5-toubun no Hanayome *","Go-Toubun no Hanayome *","Go-Tōbun no Hanayome *","Go-toubun no Hanayome *","The Quintessential Quintuplets *","The Quintessential Quintuplets Movie*","The Quintessential Quintuplets Special 2","The Quintessential Quintuplets Specials 2","The Quintessential Quintuplets*","The Quintessential Quintuplets: Honeymoon Arc","五等分の花嫁*","五等分の花嫁＊","五等分的新娘＊"],"studios":["bibury animation studios"],"producers":["dax production","nichion","pony canyon"],"relatedAnime":["https://anidb.net/anime/16165","https://anilist.co/anime/131520","https://anime-planet.com/anime/the-quintessential-quintuplets-movie","https://animecountdown.com/1577789","https://anisearch.com/anime/16091","https://kitsu.app/anime/44229","https://livechart.me/anime/10488","https://livechart.me/anime/11921","https://livechart.me/anime/3448","https://livechart.me/anime/9428","https://myanimelist.net/anime/48548","https://notify.moe/anime/e7lfM8QMg","https://simkl.com/anime/1577789"],"tags":["america","based on a manga","comedy","drama","ensemble cast","female harem","harem","heterosexual","japanese production","language barrier","male protagonist","marriage","new","predominantly female cast","present","primarily female cast","romance","school","sequel","shounen","siblings","time","twins"]}
        """.trimIndent()
    }
}

@Suppress("unused")
internal object TestAnimeObjects {

    /**
     * Default anime. Only default values are set.
     */
    object DefaultAnime {

        val obj: Anime
            get() = Anime("default")

        val serializedPrettyPrint = """
            {
              "sources": [],
              "title": "default",
              "type": "UNKNOWN",
              "episodes": 0,
              "status": "UNKNOWN",
              "animeSeason": {
                "season": "UNDEFINED",
                "year": null
              },
              "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
              "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
              "duration": null,
              "score": null,
              "synonyms": [],
              "studios": [],
              "producers": [],
              "relatedAnime": [],
              "tags": []
            }
        """.trimIndent()

        val serializedMinified = """
            {"sources":[],"title":"default","type":"UNKNOWN","episodes":0,"status":"UNKNOWN","animeSeason":{"season":"UNDEFINED"},"picture":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png","thumbnail":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png","synonyms":[],"studios":[],"producers":[],"relatedAnime":[],"tags":[]}
        """.trimIndent()
    }

    /**
     * Default anime of type TV with all optional fields not set to enforce `null` in serialization.
     */
    object NullableNotSet {

        val obj: Anime
            get() = Anime(
                title = "Death Note",
                sources = hashSetOf(
                    URI("https://myanimelist.net/anime/1535"),
                ),
                type = TV,
                episodes = 37,
                status = FINISHED,
                animeSeason = AnimeSeason(
                    season = FALL,
                    year = UNKNOWN_YEAR,
                ),
                picture = URI("https://cdn.myanimelist.net/images/anime/1079/138100.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1079/138100t.jpg"),
                duration = UNKNOWN_DURATION,
                score = NoScore,
                synonyms = hashSetOf(
                    "DN",
                    "デスノート",
                ),
                studios = hashSetOf(
                    "madhouse",
                ),
                producers = hashSetOf(
                    "D.N. Dream Partners",
                    "nippon television network",
                    "shueisha",
                    "vap",
                ),
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/2994"),
                ),
                tags = hashSetOf(
                    "psychological",
                    "shounen",
                    "supernatural",
                    "suspense",
                ),
            )

        val serializedPrettyPrint = """
            {
              "sources": [
                "https://myanimelist.net/anime/1535"
              ],
              "title": "Death Note",
              "type": "TV",
              "episodes": 37,
              "status": "FINISHED",
              "animeSeason": {
                "season": "FALL",
                "year": null
              },
              "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
              "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
              "duration": null,
              "score": null,
              "synonyms": [
                "DN",
                "デスノート"
              ],
              "studios": [
                "madhouse"
              ],
              "producers": [
                "D.N. Dream Partners",
                "nippon television network",
                "shueisha",
                "vap"
              ],
              "relatedAnime": [
                "https://myanimelist.net/anime/2994"
              ],
              "tags": [
                "psychological",
                "shounen",
                "supernatural",
                "suspense"
              ]
            }
        """.trimIndent()

        val serializedMinified = """
            {"sources":["https://myanimelist.net/anime/1535"],"title":"Death Note","type":"TV","episodes":37,"status":"FINISHED","animeSeason":{"season":"FALL","year":null},"picture":"https://cdn.myanimelist.net/images/anime/1079/138100.jpg","thumbnail":"https://cdn.myanimelist.net/images/anime/1079/138100t.jpg","duration":null,"score":null,"synonyms":["DN","デスノート"],"studios":["madhouse"],"producers":["D.N. Dream Partners","nippon television network","shueisha","vap"],"relatedAnime":["https://myanimelist.net/anime/2994"],"tags":["psychological","shounen","supernatural","suspense"]}
        """.trimIndent()
    }

    /**
     * A special with multiple episodes. Single meta data provider only.
     */
    object AllPropertiesSet {

        val obj: Anime
            get() = Anime(
                title = "5-toubun no Hanayome*",
                sources = hashSetOf(
                    URI("https://myanimelist.net/anime/58755"),
                ),
                type = SPECIAL,
                episodes = 2,
                status = FINISHED,
                animeSeason = AnimeSeason(
                    season = FALL,
                    year = 2024,
                ),
                picture = URI("https://cdn.myanimelist.net/images/anime/1915/145336.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1915/145336t.jpg"),
                duration = Duration(
                    value = 24,
                    unit = MINUTES,
                ),
                score = ScoreValue(
                    arithmeticGeometricMean = 7.44,
                    arithmeticMean = 7.44,
                    median = 7.44,
                ),
                synonyms = hashSetOf(
                    "The Quintessential Quintuplets*",
                    "五等分の花嫁*",
                ),
                studios = hashSetOf(
                    "bibury animation studios",
                ),
                producers = hashSetOf(
                    "dax production",
                    "nichion",
                    "pony canyon",
                ),
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/48548"),
                ),
                tags = hashSetOf(
                    "comedy",
                    "harem",
                    "romance",
                    "school",
                    "shounen",
                ),
            )

        val serializedPrettyPrint = """
            {
              "sources": [
                "https://myanimelist.net/anime/58755"
              ],
              "title": "5-toubun no Hanayome*",
              "type": "SPECIAL",
              "episodes": 2,
              "status": "FINISHED",
              "animeSeason": {
                "season": "FALL",
                "year": 2024
              },
              "picture": "https://cdn.myanimelist.net/images/anime/1915/145336.jpg",
              "thumbnail": "https://cdn.myanimelist.net/images/anime/1915/145336t.jpg",
              "duration": {
                "value": 1440,
                "unit": "SECONDS"
              },
              "score": {
                "arithmeticGeometricMean": 7.44,
                "arithmeticMean": 7.44,
                "median": 7.44
              },
              "synonyms": [
                "The Quintessential Quintuplets*",
                "五等分の花嫁*"
              ],
              "studios": [
                "bibury animation studios"
              ],
              "producers": [
                "dax production",
                "nichion",
                "pony canyon"
              ],
              "relatedAnime": [
                "https://myanimelist.net/anime/48548"
              ],
              "tags": [
                "comedy",
                "harem",
                "romance",
                "school",
                "shounen"
              ]
            }
        """.trimIndent()

        val serializedMinified = """
            {"sources":["https://myanimelist.net/anime/58755"],"title":"5-toubun no Hanayome*","type":"SPECIAL","episodes":2,"status":"FINISHED","animeSeason":{"season":"FALL","year":2024},"picture":"https://cdn.myanimelist.net/images/anime/1915/145336.jpg","thumbnail":"https://cdn.myanimelist.net/images/anime/1915/145336t.jpg","duration":{"value":1440,"unit":"SECONDS"},"score":{"arithmeticGeometricMean":7.44,"arithmeticMean":7.44,"median":7.44},"synonyms":["The Quintessential Quintuplets*","五等分の花嫁*"],"studios":["bibury animation studios"],"producers":["dax production","nichion","pony canyon"],"relatedAnime":["https://myanimelist.net/anime/48548"],"tags":["comedy","harem","romance","school","shounen"]}
        """.trimIndent()
    }

    /**
     * A special with multiple episodes. Contains a fully merged anime object with all meta data providers.
     */
    object FullyMergedAllPropertiesSet {

        val obj: Anime
            get() = Anime(
                title = "5-toubun no Hanayome*",
                sources = hashSetOf(
                    URI("https://anidb.net/anime/18603"),
                    URI("https://anilist.co/anime/177191"),
                    URI("https://anime-planet.com/anime/the-quintessential-quintuplets-specials-2"),
                    URI("https://anisearch.com/anime/19242"),
                    URI("https://kitsu.app/anime/48807"),
                    URI("https://livechart.me/anime/12646"),
                    URI("https://myanimelist.net/anime/58755"),
                    URI("https://notify.moe/anime/Gk-oD9uIR"),
                    URI("https://simkl.com/anime/2448488"),
                ),
                type = SPECIAL,
                episodes = 2,
                status = FINISHED,
                animeSeason = AnimeSeason(
                    season = FALL,
                    year = 2024,
                ),
                picture = URI("https://cdn.myanimelist.net/images/anime/1915/145336.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1915/145336t.jpg"),
                duration = Duration(
                    value = 24,
                    unit = MINUTES,
                ),
                score = ScoreValue(
                    arithmeticGeometricMean = 7.616175305477198,
                    arithmeticMean = 7.624601113172541,
                    median = 7.7272727272727275,
                ),
                synonyms = hashSetOf(
                    "5-toubun no Hanayome *",
                    "Go-Toubun no Hanayome *",
                    "Go-Tōbun no Hanayome *",
                    "Go-toubun no Hanayome *",
                    "The Quintessential Quintuplets *",
                    "The Quintessential Quintuplets Movie*",
                    "The Quintessential Quintuplets Special 2",
                    "The Quintessential Quintuplets Specials 2",
                    "The Quintessential Quintuplets*",
                    "The Quintessential Quintuplets: Honeymoon Arc",
                    "五等分の花嫁*",
                    "五等分の花嫁＊",
                    "五等分的新娘＊",
                ),
                studios = hashSetOf(
                    "bibury animation studios",
                ),
                producers = hashSetOf(
                    "dax production",
                    "nichion",
                    "pony canyon",
                    "tbs",
                ),
                relatedAnime = hashSetOf(
                    URI("https://anidb.net/anime/16165"),
                    URI("https://anilist.co/anime/131520"),
                    URI("https://anime-planet.com/anime/the-quintessential-quintuplets-movie"),
                    URI("https://animecountdown.com/1577789"),
                    URI("https://anisearch.com/anime/16091"),
                    URI("https://kitsu.app/anime/44229"),
                    URI("https://livechart.me/anime/10488"),
                    URI("https://livechart.me/anime/11921"),
                    URI("https://livechart.me/anime/3448"),
                    URI("https://livechart.me/anime/9428"),
                    URI("https://myanimelist.net/anime/48548"),
                    URI("https://notify.moe/anime/e7lfM8QMg"),
                    URI("https://simkl.com/anime/1577789"),
                ),
                tags = hashSetOf(
                    "america",
                    "based on a manga",
                    "comedy",
                    "drama",
                    "ensemble cast",
                    "female harem",
                    "harem",
                    "heterosexual",
                    "japanese production",
                    "language barrier",
                    "male protagonist",
                    "marriage",
                    "new",
                    "predominantly female cast",
                    "present",
                    "primarily female cast",
                    "romance",
                    "school",
                    "sequel",
                    "shounen",
                    "siblings",
                    "time",
                    "twins",
                ),
            )


        val serializedPrettyPrint = """
            {
              "sources": [
                "https://anidb.net/anime/18603",
                "https://anilist.co/anime/177191",
                "https://anime-planet.com/anime/the-quintessential-quintuplets-specials-2",
                "https://anisearch.com/anime/19242",
                "https://kitsu.app/anime/48807",
                "https://livechart.me/anime/12646",
                "https://myanimelist.net/anime/58755",
                "https://notify.moe/anime/Gk-oD9uIR",
                "https://simkl.com/anime/2448488"
              ],
              "title": "5-toubun no Hanayome*",
              "type": "SPECIAL",
              "episodes": 2,
              "status": "FINISHED",
              "animeSeason": {
                "season": "FALL",
                "year": 2024
              },
              "picture": "https://cdn.myanimelist.net/images/anime/1915/145336.jpg",
              "thumbnail": "https://cdn.myanimelist.net/images/anime/1915/145336t.jpg",
              "duration": {
                "value": 1440,
                "unit": "SECONDS"
              },
              "score": {
                "arithmeticGeometricMean": 7.616175305477198,
                "arithmeticMean": 7.624601113172541,
                "median": 7.7272727272727275
              },
              "synonyms": [
                "5-toubun no Hanayome *",
                "Go-Toubun no Hanayome *",
                "Go-Tōbun no Hanayome *",
                "Go-toubun no Hanayome *",
                "The Quintessential Quintuplets *",
                "The Quintessential Quintuplets Movie*",
                "The Quintessential Quintuplets Special 2",
                "The Quintessential Quintuplets Specials 2",
                "The Quintessential Quintuplets*",
                "The Quintessential Quintuplets: Honeymoon Arc",
                "五等分の花嫁*",
                "五等分の花嫁＊",
                "五等分的新娘＊"
              ],
              "studios": [
                "bibury animation studios"
              ],
              "producers": [
                "dax production",
                "nichion",
                "pony canyon",
                "tbs"
              ],
              "relatedAnime": [
                "https://anidb.net/anime/16165",
                "https://anilist.co/anime/131520",
                "https://anime-planet.com/anime/the-quintessential-quintuplets-movie",
                "https://animecountdown.com/1577789",
                "https://anisearch.com/anime/16091",
                "https://kitsu.app/anime/44229",
                "https://livechart.me/anime/10488",
                "https://livechart.me/anime/11921",
                "https://livechart.me/anime/3448",
                "https://livechart.me/anime/9428",
                "https://myanimelist.net/anime/48548",
                "https://notify.moe/anime/e7lfM8QMg",
                "https://simkl.com/anime/1577789"
              ],
              "tags": [
                "america",
                "based on a manga",
                "comedy",
                "drama",
                "ensemble cast",
                "female harem",
                "harem",
                "heterosexual",
                "japanese production",
                "language barrier",
                "male protagonist",
                "marriage",
                "new",
                "predominantly female cast",
                "present",
                "primarily female cast",
                "romance",
                "school",
                "sequel",
                "shounen",
                "siblings",
                "time",
                "twins"
              ]
            }
        """.trimIndent()

        val serializedMinified = """
            {"sources":["https://anidb.net/anime/18603","https://anilist.co/anime/177191","https://anime-planet.com/anime/the-quintessential-quintuplets-specials-2","https://anisearch.com/anime/19242","https://kitsu.app/anime/48807","https://livechart.me/anime/12646","https://myanimelist.net/anime/58755","https://notify.moe/anime/Gk-oD9uIR","https://simkl.com/anime/2448488"],"title":"5-toubun no Hanayome*","type":"SPECIAL","episodes":2,"status":"FINISHED","animeSeason":{"season":"FALL","year":2024},"picture":"https://cdn.myanimelist.net/images/anime/1915/145336.jpg","thumbnail":"https://cdn.myanimelist.net/images/anime/1915/145336t.jpg","duration":{"value":1440,"unit":"SECONDS"},"score":{"arithmeticGeometricMean":7.616175305477198,"arithmeticMean":7.624601113172541,"median":7.7272727272727275},"synonyms":["5-toubun no Hanayome *","Go-Toubun no Hanayome *","Go-Tōbun no Hanayome *","Go-toubun no Hanayome *","The Quintessential Quintuplets *","The Quintessential Quintuplets Movie*","The Quintessential Quintuplets Special 2","The Quintessential Quintuplets Specials 2","The Quintessential Quintuplets*","The Quintessential Quintuplets: Honeymoon Arc","五等分の花嫁*","五等分の花嫁＊","五等分的新娘＊"],"studios":["bibury animation studios"],"producers":["dax production","nichion","pony canyon","tbs"],"relatedAnime":["https://anidb.net/anime/16165","https://anilist.co/anime/131520","https://anime-planet.com/anime/the-quintessential-quintuplets-movie","https://animecountdown.com/1577789","https://anisearch.com/anime/16091","https://kitsu.app/anime/44229","https://livechart.me/anime/10488","https://livechart.me/anime/11921","https://livechart.me/anime/3448","https://livechart.me/anime/9428","https://myanimelist.net/anime/48548","https://notify.moe/anime/e7lfM8QMg","https://simkl.com/anime/1577789"],"tags":["america","based on a manga","comedy","drama","ensemble cast","female harem","harem","heterosexual","japanese production","language barrier","male protagonist","marriage","new","predominantly female cast","present","primarily female cast","romance","school","sequel","shounen","siblings","time","twins"]}
        """.trimIndent()
    }
}