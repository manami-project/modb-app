package io.github.manamiproject.modb.simkl

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.eitherNullOrBlank
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.extensions.remove
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.ExtractionResult
import io.github.manamiproject.modb.core.extractor.XmlDataExtractor
import io.github.manamiproject.modb.core.anime.*
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE_THUMBNAIL
import io.github.manamiproject.modb.core.anime.AnimeSeason.Companion.UNKNOWN_YEAR
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN as UNKNOWN_STATUS
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.anime.AnimeType.UNKNOWN as UNKNOWN_TYPE
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.*
import io.github.manamiproject.modb.core.extensions.normalize
import io.github.manamiproject.modb.core.anime.Duration.Companion.UNKNOWN as UNKNOWN_DURATION
import kotlinx.coroutines.withContext
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Converts raw data to an [AnimeRaw].
 * @since 1.0.0
 * @property metaDataProviderConfig Configuration for converting data.
 * @property extractor Extracts specific data from raw content.
 */
public class SimklAnimeConverter(
    private val metaDataProviderConfig: MetaDataProviderConfig = SimklConfig,
    private val extractor: DataExtractor = XmlDataExtractor,
): AnimeConverter {

    override suspend fun convert(rawContent: String): AnimeRaw = withContext(LIMITED_CPU) {
        val data = extractor.extract(
            rawContent, mapOf(
                "title" to "//img[@id='detailPosterImg']/@alt",
                "episodes" to "//span[@class='episode']/text()",
                "episodesFallback" to "//td[@class='SimklTVAboutYearCountry']/text()",
                "startDate" to "//span[@itemprop='startDate']/text()",
                "source" to "//meta[@property='og:url']/@content",
                "airDate" to "//strong[contains(text(),'Air')][contains(text(),'Date')]/../following-sibling::td/text()",
                "genres" to "//span[@class='TagGenre']/text()",
                "subgenres" to "//span[@class='adGenres']/a/text()",
                "type" to "//strong[text()='Type:']/..//following-sibling::*/text()",
                "duration" to "//meta[@property='og:duration']/@content",
                "picture" to "//meta[@property='og:image']/@content",
                "relatedAnime" to "//detail-related-item[@id='tvdetailrelations']//div[@class='tvdetailrelationsitems']//a/@href",
                "synonyms" to "//td[@itemprop='alternateName']/text()",
                "score" to "//div[@itemprop='aggregateRating']//span[@class='SimklTVRatingAverage'][@itemprop='ratingValue']/text()",
                "bestRating" to "//div[@itemprop='aggregateRating']//span[@itemprop='bestRating']/text()",
                "worstRating" to "//div[@itemprop='aggregateRating']//span[@itemprop='worstRating']/text()",
                "studios" to "//strong[text()='Studios:']/..//following-sibling::td//a/text()",
                "producers" to "//strong[text()='Producers:']/..//following-sibling::td//a/text()",
            )
        )

        val picture = extractPicture(data)
        val title = extractTitle(data)
        val episodes = extractEpisodes(data)

        return@withContext AnimeRaw(
            _title = title,
            episodes = episodes,
            type = extractType(data),
            picture = picture,
            thumbnail = extractThumbnail(picture),
            status = extractStatus(data),
            duration = extractDuration(episodes, data),
            animeSeason = extractAnimeSeason(data),
            _sources = extractSourcesEntry(data),
            _synonyms = postProcessSynonyms(title, extractSynonyms(data)),
            _relatedAnime = extractRelatedAnime(data),
            _tags = extractTags(data),
            _studios = extractStudios(data),
            _producers = extractProducers(data),
        ).addScores(extractScore(data))
    }

    private fun extractTitle(data: ExtractionResult) = data.string("title").trim()

    private fun extractEpisodes(data: ExtractionResult): Int {
        var numberOfEpisodes = if (!data.notFound("episodes")) {
            val totalEpisodes = data.listNotNull<String>("episodes").firstOrNull { it.contains("episodes total") || it.contains("episode total") } ?: EMPTY
            totalEpisodes.remove("episodes total")
                .remove("episode total")
                .trim().toIntOrNull() ?: 0
        } else {
            0
        }

        val fallback = data.stringOrDefault("episodesFallback")
        val episodesRegex = "\\d+ episode".toRegex()
        if (numberOfEpisodes == 0 && fallback.contains(episodesRegex)) {
            numberOfEpisodes = episodesRegex.find(fallback)?.value
                ?.remove("episode")
                ?.remove("episodes")
                ?.trim()
                ?.toIntOrNull()
                ?: 0
        }

        if (numberOfEpisodes != 0) {
            return numberOfEpisodes
        }

        val startDate = data.stringOrDefault("startDate")

        if (startDate.eitherNullOrBlank()) {
            return 0
        }

        val instant = Instant.parse(startDate)
        val localDate = instant.atZone(ZoneId.of("UTC")).toLocalDate()

        if (localDate.isBefore(LocalDate.now())) {
            return 1
        }

        return 0
    }

    private fun extractType(data: ExtractionResult): AnimeType {
        val extractedType = data.stringOrDefault("type").normalize().lowercase()

        return when (extractedType) {
            "movie" -> MOVIE
            "ova" -> OVA
            "ona" -> ONA
            "tv" -> TV
            "special" -> SPECIAL
            "music video" -> SPECIAL
            else -> UNKNOWN_TYPE
        }
    }

    private fun extractPicture(data: ExtractionResult): URI {
        return when {
            data.notFound("picture") || !data.stringOrDefault("picture").contains("posters") -> NO_PICTURE
            else -> URI(data.string("picture")
                .trim()
                .remove("https://og.simkl.in/image/details/?poster=")
                .substringBefore('&')
            )
        }
    }

    private fun extractThumbnail(picture: URI): URI {
        return when {
            picture == NO_PICTURE -> NO_PICTURE_THUMBNAIL
            else -> picture
        }
    }

    private fun extractStatus(data: ExtractionResult): AnimeStatus {
        val prefix = "Air Date: "
        val airDate = if (data.notFound("airDate")) {
            EMPTY
        } else {
            data.stringOrDefault("airDate")
                .remove(prefix)
                .trim()
                .lowercase()
        }
        val startDateString = data.stringOrDefault("startDate")

        val today = LocalDate.now()
        var startDate: LocalDate? = null
        if (startDateString.neitherNullNorBlank()) {
            val instant = Instant.parse(startDateString)
            startDate = instant.atZone(ZoneId.of("UTC")).toLocalDate()
        }

        return when {
            airDate.endsWith("now") -> ONGOING
            startDate?.isAfter(today) ?: false -> UPCOMING
            airDate.contains("-") -> {
                val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
                val endDate = LocalDate.parse(airDate.substringAfter('-').trim(), formatter)

                when {
                    endDate.isBefore(today) -> FINISHED
                    startDate?.isBefore(today) ?: false && endDate.isAfter(today) -> ONGOING
                    else -> ONGOING
                }
            }
            airDate.contains(YEAR_REGEX) && !airDate.contains("-") -> {
                val year = YEAR_REGEX.find(airDate)?.value?.toInt() ?: 0

                when {
                    year == 0 -> UNKNOWN_STATUS
                    year < today.year -> FINISHED
                    year == today.year && startDate != null && startDate.isBefore(today) -> FINISHED
                    else -> ONGOING
                }
            }
            startDate?.isBefore(today) ?: false -> FINISHED
            else -> UNKNOWN_STATUS
        }
    }

    private fun extractDuration(episodes: Int, data: ExtractionResult): Duration {
        if (data.notFound("duration") || data.int("duration") == 0 || episodes == 0) {
            return UNKNOWN_DURATION
        }

        val duration = data.int("duration") / episodes

        return Duration(
            value = duration,
            unit = SECONDS,
        )
    }

    private fun extractAnimeSeason(data: ExtractionResult): AnimeSeason {
        if (data.notFound("startDate") || data.string("startDate") == NO_START_DATE) {
            return AnimeSeason(
                season = UNDEFINED,
                year = UNKNOWN_YEAR,
            )
        }

        val instant = Instant.parse(data.string("startDate"))
        val localDate = instant.atZone(ZoneId.of("UTC")).toLocalDate()
        val year = if (localDate.year <= YEAR_OF_THE_FIRST_ANIME) {
            YEAR_OF_THE_FIRST_ANIME
        } else {
            localDate.year
        }

        val season = when (localDate.monthValue) {
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

    private fun extractSourcesEntry(data: ExtractionResult): HashSet<URI> {
        val animeId = data.string("source")
            .remove(metaDataProviderConfig.buildAnimeLink(EMPTY).toString())
            .substringBefore('/')

        return hashSetOf(metaDataProviderConfig.buildAnimeLink(animeId))
    }

    private fun extractRelatedAnime(data: ExtractionResult): HashSet<URI> {
        if (data.notFound("relatedAnime")) {
            return hashSetOf()
        }

        val relatedAnimeIds = data.listNotNull<AnimeId>("relatedAnime") {
            it.remove("/anime/").substringBefore('/')
        }

        return relatedAnimeIds.map { metaDataProviderConfig.buildAnimeLink(it) }.toHashSet()
    }

    private fun extractSynonyms(data: ExtractionResult): HashSet<String> {
        return data.stringOrDefault("synonyms").split(',').map { it.trim() }.toHashSet()
    }

    private fun postProcessSynonyms(title: String, synonyms: HashSet<String>): HashSet<Title> {
        return synonyms.filterNot { it == title }.toHashSet()
    }

    private fun extractTags(data: ExtractionResult): HashSet<Tag> {
        val genres = when {
            data.notFound("genres") -> emptySet()
            else -> data.listNotNull<Tag>("genres") { it.trim() }
        }

        val subgenres = when {
            data.notFound("subgenres") -> emptySet()
            else -> data.listNotNull<Tag>("subgenres") { it.trim() }
        }

        return genres.union(subgenres).toHashSet()
    }

    private fun extractScore(data: ExtractionResult): MetaDataProviderScore {
        if (data.notFound("score")) {
            return NoMetaDataProviderScore
        }

        val rawScore = data.double("score")
        val bestRating = data.double("bestRating")
        val worstRating = data.double("worstRating")

        return MetaDataProviderScoreValue(
            hostname = metaDataProviderConfig.hostname(),
            value = rawScore,
            range = worstRating..bestRating,
        )
    }

    private fun extractStudios(data: ExtractionResult): HashSet<Studio> {
        return if (data.notFound("studios")) {
            hashSetOf()
        } else {
            data.listNotNull<Studio>("studios").toHashSet()
        }
    }

    private fun extractProducers(data: ExtractionResult): HashSet<Producer> {
        return if (data.notFound("producers")) {
            hashSetOf()
        } else {
            data.listNotNull<Producer>("producers").toHashSet()
        }
    }

    public companion object {
        private const val NO_START_DATE = "1970-01-01T00:00:00Z"
        private val YEAR_REGEX = "\\d{4}".toRegex()

        /**
         * Singleton of [SimklAnimeConverter]
         * @since 1.0.0
         */
        public val instance: SimklAnimeConverter by lazy { SimklAnimeConverter() }
    }
}