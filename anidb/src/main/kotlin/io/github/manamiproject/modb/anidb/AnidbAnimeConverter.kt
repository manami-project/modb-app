package io.github.manamiproject.modb.anidb

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.extensions.remove
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.ExtractionResult
import io.github.manamiproject.modb.core.extractor.XmlDataExtractor
import io.github.manamiproject.modb.core.anime.*
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE_THUMBNAIL
import io.github.manamiproject.modb.core.anime.AnimeSeason.Companion.UNKNOWN_YEAR
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN as UNKNOWN_STATUS
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeType.UNKNOWN as UNKNOWN_TYPE
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.Duration.Companion.UNKNOWN as UNKNOWN_DURATION
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import kotlinx.coroutines.withContext
import java.net.URI
import java.time.Clock
import java.time.LocalDate
import kotlin.math.nextDown

/**
 * Converts raw data to an [AnimeRaw]
 * @since 1.0.0
 * @property metaDataProviderConfig Configuration for converting data.
 * @property extractor Extractor which retrieves the data from raw data.
 * @param clock Used to determine the current date. **Default:** `Clock.systemDefaultZone()`
 */
public class AnidbAnimeConverter(
    private val metaDataProviderConfig: MetaDataProviderConfig = AnidbConfig,
    private val extractor: DataExtractor = XmlDataExtractor,
    clock: Clock = Clock.systemDefaultZone(),
) : AnimeConverter {

    private val currentDate = LocalDate.now(clock)

    override suspend fun convert(rawContent: String): AnimeRaw = withContext(LIMITED_CPU) {
        val data = extractor.extract(rawContent, mapOf(
            "title" to "//anime/titles/title[@type='main']/text()",
            "alternateNames" to "//anime/titles//title/text()",
            "episodeCount" to "//anime/episodecount/text()",
            "source" to "//anime/@id",
            "type" to "//anime/type/text()",
            "image" to "//anime/picture/text()",
            "score" to "//anime/ratings/temporary/text()",
            "startDate" to "//anime/startdate/text()",
            "endDate" to "//anime/enddate/text()",
            "relatedAnime" to "//anime/relatedanime/anime/@id",
            "tags" to "//anime/tags//tag[@verified='true']/name/text()",
            "duration" to "//anime/episodes//episode/epno[@type='1']/../length/text()",
        ))

        val picture = extractPicture(data)
        val episodes = extractEpisodes(data)

        return@withContext AnimeRaw(
            _title = extractTitle(data),
            episodes = episodes,
            type = extractType(data),
            picture = picture,
            thumbnail = extractThumbnail(picture),
            status = extractStatus(data, episodes),
            duration = extractDuration(data),
            animeSeason = extractAnimeSeason(data),
            _sources = extractSourcesEntry(data),
            _synonyms = extractSynonyms(data),
            _relatedAnime = extractRelatedAnime(data),
            _tags = extractTags(data),
            _studios = hashSetOf(), // often missing, can be found under different names or is mixed up with persons
            _producers = hashSetOf(), // often missing, can be found under different names or is mixed up with persons
        ).addScores(extractScore(data))
    }

    private fun extractTitle(data: ExtractionResult) = data.string("title")

    private fun extractEpisodes(data: ExtractionResult): Int {
        val episodeString = data.stringOrDefault("episodeCount", "1")
        return episodeString.toIntOrNull().takeIf { it != null && it > 0 } ?: 1
    }

    private fun extractType(data: ExtractionResult): AnimeType {
        val rawType = data.stringOrDefault("type")

        return when(rawType.trim().lowercase()) {
            "movie" -> MOVIE
            "ova" -> OVA
            "web" -> ONA
            "tv special" -> SPECIAL
            "music video" -> SPECIAL
            "other" -> SPECIAL
            "tv series" -> TV
            "unknown" -> UNKNOWN_TYPE
            else -> throw IllegalStateException("Unknown type [$rawType]")
        }
    }

    private fun extractPicture(data: ExtractionResult): URI {
        val src = data.stringOrDefault("image").trim()

        return if (src.neitherNullNorBlank()) {
            URI("${CDN}/images/main/${src}")
        } else {
            NO_PICTURE
        }
    }

    private fun extractThumbnail(picture: URI): URI {
        return if (picture == NO_PICTURE) {
            NO_PICTURE_THUMBNAIL
        } else {
            URI("$picture-thumb.jpg")
        }
    }

    private fun extractSynonyms(data: ExtractionResult): HashSet<Title> {
        val synonyms = hashSetOf<Title>()

        if (!data.notFound("alternateNames")) {
            data.listNotNull<Title>("alternateNames").forEach { synonyms.add(it) }
        }

        return synonyms
    }

    private fun extractSourcesEntry(data: ExtractionResult): HashSet<URI> {
        val id = data.listNotNull<String>("source").first().trim()

        check(id.neitherNullNorBlank()) { "Sources link must not be blank" }

        return hashSetOf(metaDataProviderConfig.buildAnimeLink(id))
    }

    private fun extractRelatedAnime(data: ExtractionResult): HashSet<URI> {
        if (data.notFound("relatedAnime")) {
            return hashSetOf()
        }

        return data.listNotNull<String>("relatedAnime")
            .asSequence()
            .filterNot { it.contains("relation") }
            .map { it.remove("/anime/") }
            .distinct()
            .map { metaDataProviderConfig.buildAnimeLink(it) }
            .toHashSet()
    }

    private fun extractStatus(data: ExtractionResult, episodes: Episodes): AnimeStatus {
        val startDateRaw = data.stringOrDefault("startDate")
        val endDateRaw = data.stringOrDefault("endDate")
        
        val dateOrNull: (String) -> LocalDate? = { rawValue ->
            when (DATE_FORMAT.matches(rawValue)) {
                true -> {
                    val startDateMatch = DATE_FORMAT.find(rawValue)!!
                    LocalDate.of(
                        startDateMatch.groups["year"]!!.value.toInt(),
                        startDateMatch.groups["month"]?.value?.toIntOrNull() ?: 1,
                        startDateMatch.groups["day"]?.value?.toIntOrNull() ?: 1,
                    )
                }
                false -> null
            }
        }

        val startDate = dateOrNull(startDateRaw)
        val endDate = dateOrNull(endDateRaw)

        return releaseDateToStatus(startDate, endDate, episodes)
    }

    private fun releaseDateToStatus(startDate: LocalDate?, endDate: LocalDate?, episodes: Episodes): AnimeStatus {
        if (startDate == null && endDate == null) return UNKNOWN_STATUS

        if (startDate != null && endDate == null) {
            return when {
                (startDate.isBefore(currentDate) || startDate.isEqual(currentDate)) && episodes > 1 -> ONGOING
                (startDate.isBefore(currentDate) || startDate.isEqual(currentDate)) && episodes == 1 -> FINISHED
                else -> UPCOMING
            }
        }

        if (startDate == null && endDate != null) {
            return when {
                endDate.isBefore(currentDate) || endDate.isEqual(currentDate) -> FINISHED
                else -> ONGOING
            }
        }

        if (startDate != null && endDate != null && !startDate.isEqual(endDate)) {
            return when {
                endDate.isBefore(currentDate) -> FINISHED
                startDate.isBefore(currentDate) && endDate.isAfter(currentDate) -> ONGOING
                else -> UPCOMING
            }
        }

        return when {
            startDate!!.isBefore(currentDate) -> FINISHED
            startDate.isAfter(currentDate) -> UPCOMING
            else -> ONGOING
        }
    }

    private fun extractDuration(data: ExtractionResult): Duration {
        if (data.notFound("duration")) {
            return UNKNOWN_DURATION
        }

        val duration = data.listNotNull<String>("duration")
            .mapNotNull { it.toIntOrNull() }
            .average()
            .toInt()

        return Duration(duration, MINUTES)
    }

    private fun extractAnimeSeason(data: ExtractionResult): AnimeSeason {
        val seasonCell = data.stringOrDefault("startDate")

        if (seasonCell.neitherNullNorBlank() && SEASON_REGEX.matches(seasonCell)) {
            val grouped = SEASON_REGEX.find(seasonCell)
            val year = grouped?.groups["year"]?.value?.toInt() ?: 0
            val month = grouped?.groups["month"]?.value?.toInt() ?: 0

            val season = when(month) {
                1, 2, 3 -> WINTER
                4, 5, 6 -> SPRING
                7, 8, 9 -> SUMMER
                10, 11, 12 -> FALL
                else -> UNDEFINED
            }

            return AnimeSeason(
                season = season,
                year = year,
            )
        }

        if (YEAR_REGEX.matches(seasonCell)) {
            return AnimeSeason(
                year = YEAR_REGEX.find(seasonCell)!!.value.toIntOrNull() ?: UNKNOWN_YEAR,
            )
        }

        return AnimeSeason()
    }

    private fun extractTags(data: ExtractionResult): HashSet<Tag> {
        return if (data.notFound("tags")) {
            hashSetOf()
        } else {
            data.listNotNull<Tag>("tags").toHashSet()
        }
    }

    private fun extractScore(data: ExtractionResult): MetaDataProviderScore {
        if (data.notFound("score") || data.stringOrDefault("score") == "N/A") {
            return NoMetaDataProviderScore
        }

        val rawScore = data.double("score")

        return MetaDataProviderScoreValue(
            hostname = metaDataProviderConfig.hostname(),
            value = rawScore,
            range = 1.0..10.0,
        )
    }

    public companion object {
        private const val CDN = "https://cdn.anidb.net"
        private val DATE_FORMAT = """(?<year>\d{4})-?(?<month>\d{2})?-?(?<day>\d{2})?""".toRegex()
        private val SEASON_REGEX = """(?<year>\d{4})-(?<month>\d{2}).*?""".toRegex()
        private val YEAR_REGEX = """\d{4}""".toRegex()

        /**
         * Singleton of [AnidbAnimeConverter]
         * @since 5.2.0
         */
        public val instance: AnidbAnimeConverter by lazy { AnidbAnimeConverter() }
    }
}
