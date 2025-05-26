package io.github.manamiproject.modb.core

import io.github.manamiproject.modb.core.anime.*
import io.github.manamiproject.modb.core.anime.AnimeSeason.Companion.UNKNOWN_YEAR
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.anime.Duration.Companion.UNKNOWN as UNKNOWN_DURATION
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.*
import io.github.manamiproject.modb.core.config.*
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.ExtractionResult
import io.github.manamiproject.modb.core.extractor.OutputKey
import io.github.manamiproject.modb.core.extractor.Selector
import io.github.manamiproject.modb.core.logging.Logger
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import org.slf4j.Marker
import org.slf4j.Logger as Slf4jLogger
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import kotlin.reflect.*

internal object TestMetaDataProviderConfig : MetaDataProviderConfig {
    override fun isTestContext(): Boolean = true
    override fun hostname(): Hostname = shouldNotBeInvoked()
    override fun buildAnimeLink(id: AnimeId): URI = shouldNotBeInvoked()
    override fun buildDataDownloadLink(id: String): URI = shouldNotBeInvoked()
    override fun fileSuffix(): FileSuffix = shouldNotBeInvoked()
}

internal object TestAnimeConverter : AnimeConverter {
    override suspend fun convert(rawContent: String) = shouldNotBeInvoked()
}

internal object TestDataExtractor : DataExtractor {
    override suspend fun extract(rawContent: String, selection: Map<OutputKey, Selector>): ExtractionResult = shouldNotBeInvoked()
}

internal object TestConfigRegistry: ConfigRegistry {
    override fun string(key: String): String = shouldNotBeInvoked()
    override fun long(key: String): Long = shouldNotBeInvoked()
    override fun int(key: String): Int = shouldNotBeInvoked()
    override fun <T: Any> list(key: String): List<T> = shouldNotBeInvoked()
    override fun boolean(key: String): Boolean = shouldNotBeInvoked()
    override fun double(key: String): Double = shouldNotBeInvoked()
    override fun localDate(key: String): LocalDate = shouldNotBeInvoked()
    override fun localDateTime(key: String): LocalDateTime = shouldNotBeInvoked()
    override fun offsetDateTime(key: String): OffsetDateTime = shouldNotBeInvoked()
    override fun <T: Any> map(key: String): Map<String, T> = shouldNotBeInvoked()
}

internal class TestKProperty<T>: KProperty<T> {
    override val annotations: List<Annotation>
        get() = shouldNotBeInvoked()
    override val getter: KProperty.Getter<T>
        get() = shouldNotBeInvoked()
    override val isAbstract: Boolean
        get() = shouldNotBeInvoked()
    override val isConst: Boolean
        get() = shouldNotBeInvoked()
    override val isFinal: Boolean
        get() = shouldNotBeInvoked()
    override val isLateinit: Boolean
        get() = shouldNotBeInvoked()
    override val isOpen: Boolean
        get() = shouldNotBeInvoked()
    override val isSuspend: Boolean
        get() = shouldNotBeInvoked()
    override val name: String
        get() = shouldNotBeInvoked()
    override val parameters: List<KParameter>
        get() = shouldNotBeInvoked()
    override val returnType: KType
        get() = shouldNotBeInvoked()
    override val typeParameters: List<KTypeParameter>
        get() = shouldNotBeInvoked()
    override val visibility: KVisibility
        get() = shouldNotBeInvoked()
    override fun call(vararg args: Any?): T = shouldNotBeInvoked()
    override fun callBy(args: Map<KParameter, Any?>): T = shouldNotBeInvoked()
}

internal object TestScoreCalculator: ScoreCalculator {
    override fun calculateScore(scores: Collection<MetaDataProviderScore>): Score = shouldNotBeInvoked()
}

internal object TestLoggerImplementation: Logger {
    override fun error(message: () -> String) = shouldNotBeInvoked()
    override fun error(exception: Throwable, message: () -> String) = shouldNotBeInvoked()
    override fun warn(message: () -> String) = shouldNotBeInvoked()
    override fun warn(exception: Throwable, message: () -> String) = shouldNotBeInvoked()
    override fun info(message: () -> String) = shouldNotBeInvoked()
    override fun debug(message: () -> String) = shouldNotBeInvoked()
    override fun trace(message: () -> String) = shouldNotBeInvoked()
}

internal object TestSlf4jImplementation: Slf4jLogger {
    override fun getName(): String = shouldNotBeInvoked()
    override fun isTraceEnabled(): Boolean = shouldNotBeInvoked()
    override fun isTraceEnabled(marker: Marker?): Boolean = shouldNotBeInvoked()
    override fun trace(msg: String?) = shouldNotBeInvoked()
    override fun trace(format: String?, arg: Any?) = shouldNotBeInvoked()
    override fun trace(format: String?, arg1: Any?, arg2: Any?) = shouldNotBeInvoked()
    override fun trace(format: String?, vararg arguments: Any?) = shouldNotBeInvoked()
    override fun trace(msg: String?, t: Throwable?) = shouldNotBeInvoked()
    override fun trace(marker: Marker?, msg: String?) = shouldNotBeInvoked()
    override fun trace(marker: Marker?, format: String?, arg: Any?) = shouldNotBeInvoked()
    override fun trace(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) = shouldNotBeInvoked()
    override fun trace(marker: Marker?, format: String?, vararg argArray: Any?) = shouldNotBeInvoked()
    override fun trace(marker: Marker?, msg: String?, t: Throwable?) = shouldNotBeInvoked()
    override fun isDebugEnabled(): Boolean = shouldNotBeInvoked()
    override fun isDebugEnabled(marker: Marker?): Boolean = shouldNotBeInvoked()
    override fun debug(msg: String?) = shouldNotBeInvoked()
    override fun debug(format: String?, arg: Any?) = shouldNotBeInvoked()
    override fun debug(format: String?, arg1: Any?, arg2: Any?) = shouldNotBeInvoked()
    override fun debug(format: String?, vararg arguments: Any?) = shouldNotBeInvoked()
    override fun debug(msg: String?, t: Throwable?) = shouldNotBeInvoked()
    override fun debug(marker: Marker?, msg: String?) = shouldNotBeInvoked()
    override fun debug(marker: Marker?, format: String?, arg: Any?) = shouldNotBeInvoked()
    override fun debug(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) = shouldNotBeInvoked()
    override fun debug(marker: Marker?, format: String?, vararg arguments: Any?) = shouldNotBeInvoked()
    override fun debug(marker: Marker?, msg: String?, t: Throwable?) = shouldNotBeInvoked()
    override fun isInfoEnabled(): Boolean = shouldNotBeInvoked()
    override fun isInfoEnabled(marker: Marker?): Boolean = shouldNotBeInvoked()
    override fun info(msg: String?) = shouldNotBeInvoked()
    override fun info(format: String?, arg: Any?) = shouldNotBeInvoked()
    override fun info(format: String?, arg1: Any?, arg2: Any?) = shouldNotBeInvoked()
    override fun info(format: String?, vararg arguments: Any?) = shouldNotBeInvoked()
    override fun info(msg: String?, t: Throwable?) = shouldNotBeInvoked()
    override fun info(marker: Marker?, msg: String?) = shouldNotBeInvoked()
    override fun info(marker: Marker?, format: String?, arg: Any?) = shouldNotBeInvoked()
    override fun info(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) = shouldNotBeInvoked()
    override fun info(marker: Marker?, format: String?, vararg arguments: Any?) = shouldNotBeInvoked()
    override fun info(marker: Marker?, msg: String?, t: Throwable?) = shouldNotBeInvoked()
    override fun isWarnEnabled(): Boolean = shouldNotBeInvoked()
    override fun isWarnEnabled(marker: Marker?): Boolean = shouldNotBeInvoked()
    override fun warn(msg: String?) = shouldNotBeInvoked()
    override fun warn(format: String?, arg: Any?) = shouldNotBeInvoked()
    override fun warn(format: String?, vararg arguments: Any?) = shouldNotBeInvoked()
    override fun warn(format: String?, arg1: Any?, arg2: Any?) = shouldNotBeInvoked()
    override fun warn(msg: String?, t: Throwable?) = shouldNotBeInvoked()
    override fun warn(marker: Marker?, msg: String?) = shouldNotBeInvoked()
    override fun warn(marker: Marker?, format: String?, arg: Any?) = shouldNotBeInvoked()
    override fun warn(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) = shouldNotBeInvoked()
    override fun warn(marker: Marker?, format: String?, vararg arguments: Any?) = shouldNotBeInvoked()
    override fun warn(marker: Marker?, msg: String?, t: Throwable?) = shouldNotBeInvoked()
    override fun isErrorEnabled(): Boolean = shouldNotBeInvoked()
    override fun isErrorEnabled(marker: Marker?): Boolean = shouldNotBeInvoked()
    override fun error(msg: String?) = shouldNotBeInvoked()
    override fun error(format: String?, arg: Any?) = shouldNotBeInvoked()
    override fun error(format: String?, arg1: Any?, arg2: Any?) = shouldNotBeInvoked()
    override fun error(format: String?, vararg arguments: Any?) = shouldNotBeInvoked()
    override fun error(msg: String?, t: Throwable?) = shouldNotBeInvoked()
    override fun error(marker: Marker?, msg: String?) = shouldNotBeInvoked()
    override fun error(marker: Marker?, format: String?, arg: Any?) = shouldNotBeInvoked()
    override fun error(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) = shouldNotBeInvoked()
    override fun error(marker: Marker?, format: String?, vararg arguments: Any?) = shouldNotBeInvoked()
    override fun error(marker: Marker?, msg: String?, t: Throwable?) = shouldNotBeInvoked()
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