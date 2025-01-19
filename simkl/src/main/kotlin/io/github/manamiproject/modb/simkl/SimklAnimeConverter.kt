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
import io.github.manamiproject.modb.core.models.*
import io.github.manamiproject.modb.core.models.AnimeSeason.Season
import kotlinx.coroutines.withContext
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Converts raw data to an [Anime].
 * @since 1.0.0
 * @param simklMetaDataProviderConfig Configuration for converting data.
 * @param extractor Extracts specific data from raw content.
 */
public class SimklAnimeConverter(
    private val simklMetaDataProviderConfig: MetaDataProviderConfig = SimklConfig,
    private val extractor: DataExtractor = XmlDataExtractor,
): AnimeConverter {

    override suspend fun convert(rawContent: String): Anime = withContext(LIMITED_CPU) {
        val data = extractor.extract(
            rawContent, mapOf(
                "title" to "//img[@id='detailPosterImg']/@alt",
                "episodes" to "//span[@class='episode']/text()",
                "episodesFallback" to "//td[@class='SimklTVAboutYearCountry']/text()",
                "startDate" to "//span[@itemprop='startDate']/text()",
                "source" to "//meta[@property='og:url']/@content",
                "airDate" to "//strong[contains(text(), 'Air Date:')]/../text()",
                "genres" to "//span[@class='TagGenre']/text()",
                "subgenres" to "//span[@class='adGenres']/a/text()",
                "type" to "//strong[contains(text(), 'Type:')]/../text()",
                "duration" to "//meta[@property='og:duration']/@content",
                "picture" to "//meta[@property='og:image']/@content",
                "relatedAnime" to "//detail-related-item[@id='tvdetailrelations']//div[@class='tvdetailrelationsitems']//a/@href",
                "synonyms" to "//td[@itemprop='alternateName']/text()",
            )
        )

        val picture = extractPicture(data)
        val title = extractTitle(data)
        val episodes = extractEpisodes(data)

        return@withContext Anime(
            _title = title,
            episodes = episodes,
            type = extractType(data),
            picture = picture,
            thumbnail = extractThumbnail(picture),
            status = extractStatus(data),
            duration = extractDuration(episodes, data),
            animeSeason = extractAnimeSeason(data),
            sources = extractSourcesEntry(data),
            synonyms = postProcessSynonyms(title, extractSynonyms(data)),
            relatedAnime = extractRelatedAnime(data),
            tags = extractTags(data),
        )
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

    private fun extractType(data: ExtractionResult): Anime.Type {
        val prefix = "Type: "
        val extractedType = data.listNotNull<String>("type")
            .firstOrNull { it.startsWith(prefix) }
            ?.remove(prefix)
            ?.lowercase()
            ?.trim()
            ?: EMPTY

        return when (extractedType) {
            "movie" -> Anime.Type.MOVIE
            "ova" -> Anime.Type.OVA
            "ona" -> Anime.Type.ONA
            "tv" -> Anime.Type.TV
            "special" -> Anime.Type.SPECIAL
            "music video" -> Anime.Type.SPECIAL
            else -> Anime.Type.UNKNOWN
        }
    }

    private fun extractPicture(data: ExtractionResult): URI {
        return when {
            data.notFound("picture") || !data.stringOrDefault("picture", EMPTY).contains("posters") -> Anime.NO_PICTURE
            else -> URI(data.string("picture")
                .trim()
                .remove("https://og.simkl.in/image/details/?poster=")
                .substringBefore('&')
            )
        }
    }

    private fun extractThumbnail(picture: URI): URI {
        return when {
            picture == Anime.NO_PICTURE -> Anime.NO_PICTURE_THUMBNAIL
            else -> picture
        }
    }

    private fun extractStatus(data: ExtractionResult): Anime.Status {
        val prefix = "Air Date: "
        val airDate = if (data.notFound("airDate")) {
            EMPTY
        } else {
            data.listNotNull<String>("airDate")
                    .firstOrNull { it.startsWith(prefix) }
                    ?.remove(prefix)
                    ?.trim()
                    ?.lowercase()
                    ?: EMPTY
        }
        val startDateString = data.stringOrDefault("startDate")

        val today = LocalDate.now()
        var startDate: LocalDate? = null
        if (startDateString.neitherNullNorBlank()) {
            val instant = Instant.parse(startDateString)
            startDate = instant.atZone(ZoneId.of("UTC")).toLocalDate()
        }

        return when {
            airDate.endsWith("now") -> Anime.Status.ONGOING
            startDate?.isAfter(today) ?: false -> Anime.Status.UPCOMING
            airDate.contains("-") -> {
                val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
                val endDate = LocalDate.parse(airDate.substringAfter('-').trim(), formatter)

                when {
                    endDate.isBefore(today) -> Anime.Status.FINISHED
                    startDate?.isBefore(today) ?: false && endDate.isAfter(today) -> Anime.Status.ONGOING
                    else -> Anime.Status.ONGOING
                }
            }
            airDate.contains(YEAR_REGEX) && !airDate.contains("-") -> {
                val year = YEAR_REGEX.find(airDate)?.value?.toInt() ?: 0

                when {
                    year == 0 -> Anime.Status.UNKNOWN
                    year < today.year -> Anime.Status.FINISHED
                    year == today.year && startDate != null && startDate.isBefore(today) -> Anime.Status.FINISHED
                    else -> Anime.Status.ONGOING
                }
            }
            else -> Anime.Status.UNKNOWN
        }
    }

    private fun extractDuration(episodes: Int, data: ExtractionResult): Duration {
        if (data.notFound("duration") || data.int("duration") == 0 || episodes == 0) {
            return Duration.UNKNOWN
        }

        val duration = data.int("duration") / episodes

        return Duration(
            value = duration,
            unit = Duration.TimeUnit.SECONDS,
        )
    }

    private fun extractAnimeSeason(data: ExtractionResult): AnimeSeason {
        if (data.notFound("startDate") || data.string("startDate") == NO_START_DATE) {
            return AnimeSeason(
                season = AnimeSeason.Season.UNDEFINED,
                year = AnimeSeason.UNKNOWN_YEAR,
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
            1, 2, 3 -> Season.WINTER
            4, 5, 6 -> Season.SPRING
            7, 8, 9 -> Season.SUMMER
            10, 11, 12 -> Season.FALL
            else -> Season.UNDEFINED
        }

        return AnimeSeason(
            season = season,
            year = year,
        )
    }

    private fun extractSourcesEntry(data: ExtractionResult): HashSet<URI> {
        val animeId = data.string("source")
            .remove(simklMetaDataProviderConfig.buildAnimeLink(EMPTY).toString())
            .substringBefore('/')

        return hashSetOf(simklMetaDataProviderConfig.buildAnimeLink(animeId))
    }

    private fun extractRelatedAnime(data: ExtractionResult): HashSet<URI> {
        if (data.notFound("relatedAnime")) {
            return hashSetOf()
        }

        val relatedAnimeIds = data.listNotNull<AnimeId>("relatedAnime") {
            it.remove("/anime/").substringBefore('/')
        }

        return relatedAnimeIds.map { simklMetaDataProviderConfig.buildAnimeLink(it) }.toHashSet()
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