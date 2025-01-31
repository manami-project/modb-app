package io.github.manamiproject.modb.animeplanet

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.eitherNullOrBlank
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.extensions.remove
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.ExtractionResult
import io.github.manamiproject.modb.core.extractor.JsonDataExtractor
import io.github.manamiproject.modb.core.extractor.XmlDataExtractor
import io.github.manamiproject.modb.core.models.*
import io.github.manamiproject.modb.core.models.Anime.Companion.NO_PICTURE
import io.github.manamiproject.modb.core.models.Anime.Companion.NO_PICTURE_THUMBNAIL
import io.github.manamiproject.modb.core.models.Anime.Status.*
import io.github.manamiproject.modb.core.models.Anime.Status.UNKNOWN
import io.github.manamiproject.modb.core.models.Anime.Type
import io.github.manamiproject.modb.core.models.Anime.Type.*
import io.github.manamiproject.modb.core.models.AnimeSeason.Season.UNDEFINED
import io.github.manamiproject.modb.core.models.Duration.TimeUnit.MINUTES
import kotlinx.coroutines.withContext
import java.net.URI
import java.time.Clock
import java.time.LocalDate

/**
 * Converts raw data to an [Anime].
 * @since 1.0.0
 * @param metaDataProviderConfig Configuration for converting data.
 */
public class AnimePlanetAnimeConverter(
    private val metaDataProviderConfig: MetaDataProviderConfig = AnimePlanetConfig,
    private val xmlExtractor: DataExtractor = XmlDataExtractor,
    private val jsonExtractor: DataExtractor = JsonDataExtractor,
    private val clock: Clock = Clock.systemUTC(),
) : AnimeConverter {

    override suspend fun convert(rawContent: String): Anime = withContext(LIMITED_CPU) {
        val data = xmlExtractor.extract(rawContent, mapOf(
            "titleH1" to "//h1[@itemprop='name']/text()",
            "jsonld" to "//script[@type='application/ld+json']/node()",
            "titleMeta" to "//meta[@property='og:title']/@content",
            "thumbnail" to "//img[@itemprop='image']/@src",
            "typeEpisodesDuration" to "//span[@class='type']/text()",
            "iconYear" to "//section[@class='pure-g entryBar']//span[@class='iconYear']/text()",
            "seasonYear" to "//section[@class='pure-g entryBar']//span[@class='iconYear']/following-sibling::*/text()",
            "source" to "//link[@rel='canonical']/@href",
            "alternativeTitle" to "//h2[@class='aka']/text()",
            "relatedAnime" to "//div[@id='tabs--relations--anime']/div//a/@href",
            "tags" to "//div[contains(@class, 'tags')]//a/text()",
        ))

        val jsonld = data.string("jsonld")
        val jsonldData = jsonExtractor.extract(jsonld, mapOf(
            "title" to "$.name",
            "alternateName" to "$.alternateName",
            "source" to "$.url",
            "image" to "$.image",
            "datePublished" to "$.datePublished",
            "genre" to "$.genre",
        ))

        val thumbnail = extractThumbnail(jsonldData, data)

        return@withContext Anime(
            _title = extractTitle(jsonldData, data),
            episodes = extractEpisodes(data),
            type = extractType(data),
            picture = extractPicture(thumbnail),
            thumbnail = thumbnail,
            status = extractStatus(data),
            duration = extractDuration(data),
            animeSeason = extractAnimeSeason(jsonldData, data),
            _sources = extractSourcesEntry(jsonldData, data),
            synonyms = extractSynonyms(jsonldData, data),
            relatedAnime = extractRelatedAnime(data),
            tags = extractTags(jsonldData, data),
        )
    }

    private fun extractStatus(data: ExtractionResult): Anime.Status {
        val value = data.stringOrDefault("iconYear")
        val currentYear = LocalDate.now(clock).year

        // shows only one year
        if (REGEX_YEAR.matches(value)) {
            val year = value.toInt()
            return when {
                year > currentYear -> UPCOMING
                year < currentYear -> FINISHED
                else -> UNKNOWN
            }
        }

        // shows range of two years
        if (Regex("[0-9]{4} - [0-9]{4}").matches(value)) {
            val year = REGEX_YEAR.findAll(value).last().value.toInt()
            return when {
                year > currentYear -> UPCOMING
                year < currentYear -> FINISHED
                else -> UNKNOWN
            }
        }

        // shows range from year to unknown
        if (Regex("[0-9]{4} - \\?").matches(value)) {
            val year = REGEX_YEAR.find(value)!!.value.toInt()
            return when {
                year > currentYear -> UPCOMING
                year < currentYear -> ONGOING
                else -> UNKNOWN
            }
        }

        // shows only TBA
        if (value == "TBA") {
            return UPCOMING
        }

        throw IllegalStateException("Unknown case for status [$value]")
    }

    private fun extractTitle(jsonldData: ExtractionResult, data: ExtractionResult): Title {
        var title = data.stringOrDefault("titleH1")

        if (title.contains(TITLE_CONTAINING_AT_CHAR)) {
            title = jsonldData.stringOrDefault("title")
        }

        if ((title.eitherNullOrBlank() || title.contains(TITLE_CONTAINING_AT_CHAR))) {
            title = data.stringOrDefault("titleMeta")
        }

        return title
    }

    private fun extractEpisodes(data: ExtractionResult): Episodes {
        return data.stringOrDefault("typeEpisodesDuration").let {
            Regex("\\d+").find(it)?.value?.toInt() ?: 0
        }
    }

    private fun extractType(data: ExtractionResult): Type {
        val textValue = data.string("typeEpisodesDuration").substringBefore('(').lowercase().let {
            Regex("([a-z]| )+").find(it)?.value?.trim() ?: EMPTY
        }

        return when(textValue) {
            "dvd special" -> SPECIAL
            "movie" -> MOVIE
            "music video" -> SPECIAL
            "other" -> SPECIAL
            "ova" -> OVA
            "tv" -> TV
            "tv special" -> SPECIAL
            "web" -> ONA
            else -> throw IllegalStateException("Unknown type [$textValue]")
        }
    }

    private fun extractThumbnail(jsonldData: ExtractionResult, data: ExtractionResult): URI {
        val textValue = data.stringOrDefault("thumbnail").ifBlank {
            jsonldData.stringOrDefault("image")
        }

        return if (textValue.neitherNullNorBlank()) {
            URI(textValue.substringBefore("?t="))
        } else {
            NO_PICTURE
        }
    }

    private fun extractPicture(thumbnail: URI): URI {
        return if (thumbnail != NO_PICTURE) {
            URI(thumbnail.toString().replace(Regex("-\\d+x\\d+"), EMPTY))
        } else {
            NO_PICTURE_THUMBNAIL
        }
    }

    private fun extractDuration(data: ExtractionResult): Duration {
        val durationInMinutes = data.string("typeEpisodesDuration").let {
            Regex("\\d+ min").find(it)?.value?.remove("min")?.trim()?.toInt() ?: 0
        }

        return Duration(durationInMinutes, MINUTES)
    }

    private fun extractAnimeSeason(jsonldData: ExtractionResult, data: ExtractionResult): AnimeSeason {
        val yearStr = data.stringOrDefault("iconYear").ifBlank {
            jsonldData.stringOrDefault("datePublished")
        }.trim()

        val year = when {
            REGEX_YEAR.matches(yearStr) -> yearStr.toInt()
            yearStr.contains("-") -> {
                val split = yearStr.split("-").first().trim()
                if (REGEX_YEAR.matches(split)) {
                    split.toInt()
                } else {
                    0
                }
            }
            else -> 0
        }

        val season = data.string("seasonYear").trim().split(' ')

        return when {
            season.size != 2 -> AnimeSeason(season = UNDEFINED, year = year)
            else -> AnimeSeason(season = AnimeSeason.Season.of(season[0]), year = season[1].toInt())
        }
    }

    private fun extractSourcesEntry(jsonldData: ExtractionResult, data: ExtractionResult): HashSet<URI> {
        val uri = data.stringOrDefault("source").ifBlank {
            jsonldData.stringOrDefault("source")
        }.trim().remove("www.")

        return hashSetOf(URI(uri))
    }

    private fun extractSynonyms(jsonldData: ExtractionResult, data: ExtractionResult): HashSet<Title> {
        val heading = data.string("alternativeTitle")

        val alternativeTitles = hashSetOf<String>()

        when {
            heading.startsWith(SINGLE_SYNONYM) -> alternativeTitles.add(heading.remove(SINGLE_SYNONYM).trim())
            heading.startsWith(MULTIPLE_SYNONYMS) -> heading.remove(MULTIPLE_SYNONYMS).
                split(',')
                .map { it.trim() }
                .forEach { alternativeTitles.add(it) }
        }

        if (alternativeTitles.any { it.contains(TITLE_CONTAINING_AT_CHAR) }) {
            alternativeTitles.removeIf { it.contains(TITLE_CONTAINING_AT_CHAR) }
            jsonldData.listNotNull<Title>("alternateName").forEach { alternativeTitles.add(it) }
        }

        return alternativeTitles
    }

    private fun extractTags(jsonldData: ExtractionResult, data: ExtractionResult): HashSet<Tag> {
        val tags = if (jsonldData.notFound("genre")) {
            hashSetOf()
        } else {
            jsonldData.listNotNull<Tag>("genre").map { it.trimEnd(',') }.toHashSet()
        }

        if (!data.notFound("tags")) {
            data.listNotNull<Tag>("tags").map { it.trimEnd(',') }.forEach { tags.add(it) }
        }

        return tags
    }

    private fun extractRelatedAnime(data: ExtractionResult): HashSet<URI> {
        return if (data.notFound("relatedAnime")) {
            hashSetOf()
        } else {
            data.listNotNull<String>("relatedAnime")
                .map { it.remove("/anime/") }
                .map { metaDataProviderConfig.buildAnimeLink(it) }
                .toHashSet()
        }


    }

    public companion object {
        private val REGEX_YEAR = Regex("\\d{4}")
        private const val TITLE_CONTAINING_AT_CHAR= "[email protected]"
        private const val SINGLE_SYNONYM = "Alt title:"
        private const val MULTIPLE_SYNONYMS = "Alt titles:"

        /**
         * Singleton of [AnimePlanetAnimeConverter]
         * @since 1.0.0
         */
        public val instance: AnimePlanetAnimeConverter by lazy { AnimePlanetAnimeConverter() }
    }
}