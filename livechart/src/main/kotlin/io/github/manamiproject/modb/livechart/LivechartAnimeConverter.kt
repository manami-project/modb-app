package io.github.manamiproject.modb.livechart

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.remove
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.ExtractionResult
import io.github.manamiproject.modb.core.extractor.JsonDataExtractor
import io.github.manamiproject.modb.core.extractor.XmlDataExtractor
import io.github.manamiproject.modb.core.models.*
import io.github.manamiproject.modb.core.models.Anime.Companion.NO_PICTURE
import io.github.manamiproject.modb.core.models.Anime.Companion.NO_PICTURE_THUMBNAIL
import io.github.manamiproject.modb.core.models.Anime.Status.*
import io.github.manamiproject.modb.core.models.Anime.Type.*
import io.github.manamiproject.modb.core.models.Anime.Type.UNKNOWN
import io.github.manamiproject.modb.core.models.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.models.Duration.TimeUnit.SECONDS
import kotlinx.coroutines.withContext
import org.apache.commons.text.StringEscapeUtils
import java.net.URI

/**
 * Converts raw data to an [Anime].
 * Requires raw HTML. -> test
 * @since 1.0.0
 * @param metaDataProviderConfig Configuration for converting data.
 */
public class LivechartAnimeConverter(
    private val metaDataProviderConfig: MetaDataProviderConfig = LivechartConfig,
    private val xmlExtractor: DataExtractor = XmlDataExtractor,
    private val jsonExtractor: DataExtractor = JsonDataExtractor,
): AnimeConverter {

    override suspend fun convert(rawContent: String): Anime = withContext(LIMITED_CPU) {
        val data = xmlExtractor.extract(rawContent, mapOf(
            "jsonld" to "//script[@type='application/ld+json']/node()",
            "title" to "//meta[@property='og:title']/@content",
            "image" to "//meta[@property='og:image']/@content",
            "episodesDiv" to "//div[contains(text(), 'Episodes')]/../text()",
            "episodesCountdown" to "//div[@data-controller='countdown-bar']//div[contains(text(), 'EP')]/text()",
            "type" to "//div[contains(text(), 'Format')]/..",
            "status" to "//div[contains(text(), 'Status')]/..",
            "duration" to "//div[contains(text(), 'Run time')]/..",
            "season" to "//div[contains(text(), 'Season')]/../a/text()",
            "year" to "//div[contains(text(), 'Premiere')]/following-sibling::*",
            "relatedAnime" to "//div[@data-controller='carousel']//article/a/@href",
            "tags" to "//div[contains(text(), 'Tags')]/..//a[@data-anime-details-target='tagChip']",
            "sourceDiv" to "//div[@data-anime-details-id]/@data-anime-details-id",
            "sourceMeta" to "//meta[@property='og:url']/@content",
        ))

        val jsonld = data.listNotNull<String>("jsonld").first()
        val jsonldData = jsonExtractor.extract(jsonld, mapOf(
            "url" to "$.url",
            "genre" to "$.genre",
            "name" to "$.name",
            "image" to "$.image",
            "numberOfEpisodes" to "$.numberOfEpisodes",
            "datePublished" to "$.datePublished",
            "alternateName" to "$.alternateName",
        ))

        val picture = extractPicture(jsonldData, data)

        return@withContext Anime(
            _title = extractTitle(jsonldData, data),
            episodes = extractEpisodes(jsonldData, data),
            type = extractType(data),
            picture = picture,
            thumbnail = findThumbnail(picture),
            status = extractStatus(data),
            duration = extractDuration(data),
            animeSeason = extractAnimeSeason(data),
            sources = extractSourcesEntry(jsonldData, data),
            synonyms = extractSynonyms(jsonldData),
            relatedAnime = extractRelatedAnime(data),
            tags = extractTags(jsonldData, data),
        )
    }

    private fun extractTitle(jsonData: ExtractionResult, data: ExtractionResult): Title {
        val name = jsonData.stringOrDefault("name").ifBlank {
            data.stringOrDefault("title")
        }

        return StringEscapeUtils.unescapeHtml4(name)
    }

    private fun extractEpisodes(jsonData: ExtractionResult, data: ExtractionResult): Episodes {
        // JSON data
        var episodes = jsonData.intOrDefault("numberOfEpisodes")
        var isTableValueUnknown = false

        // regular value in data table
        if (episodes == 0) {
            val episodesValue = data.stringOrDefault("episodesDiv", "?")
                .trim()
                .split('/')
                .last()

            if (episodesValue != "?" && episodesValue != "-") {
                episodes = episodesValue.toIntOrNull() ?: 0
            } else {
                isTableValueUnknown = true
            }
        }

        // current episode from table if the anime is ongoing
        if (episodes == 0) {
            episodes = data.stringOrDefault("episodesCountdown")
                .remove("EP")
                .trim()
                .toIntOrNull()
                ?: 0

            if (episodes != 0 ) {
                episodes -= 1
            }
        }

        return when {
            episodes == 0 && isTableValueUnknown -> 1
            else -> episodes
        }
    }

    private fun extractType(data: ExtractionResult): Anime.Type {
        val value = data.string("type")

        return when(value.trim().lowercase()) {
            "movie" -> MOVIE
            "ova" -> OVA
            "special" -> SPECIAL
            "tv" -> TV
            "tv short" -> TV
            "tv special" -> SPECIAL
            "web" -> ONA
            "web short" -> ONA
            "?" -> UNKNOWN
            "unknown" -> UNKNOWN
            else -> throw IllegalStateException("Unknown type [$value]")
        }
    }

    private fun extractPicture(jsonData: ExtractionResult, data: ExtractionResult): URI {
        val value = jsonData.stringOrDefault("image").ifBlank {
            data.stringOrDefault("image")
        }

        return if (!value.endsWith(LARGE_PICTURE_INDICATOR)) {
            NO_PICTURE
        } else {
            URI(value)
        }
    }

    private fun findThumbnail(uri: URI): URI {
        return if (uri == NO_PICTURE) {
            NO_PICTURE_THUMBNAIL
        } else {
            URI(uri.toString().replace(LARGE_PICTURE_INDICATOR, SMALL_PICTURE_INDICATOR))
        }
    }

    private fun extractStatus(data: ExtractionResult): Anime.Status {
        val statusString = data.stringOrDefault("status")

        return when (statusString.trim().lowercase()){
            "not yet released" -> UPCOMING
            "releasing" -> ONGOING
            "finished" -> FINISHED
            else -> Anime.Status.UNKNOWN
        }
    }

    private fun extractDuration(data: ExtractionResult): Duration {
        val durationString = data.stringOrDefault("duration").trim()

        val seconds = Regex("([0-9]+ ?[aA-zZ]+)+")
            .findAll(durationString)
            .map { it.value }
            .map {
                val value = (Regex("[0-9]+").find(it)?.value?.trim() ?: "0").ifBlank { "0" }.toIntOrNull() ?: 0
                val unit = Regex("[a-z]+").find(it)?.value?.trim()?.lowercase() ?: ""
                value to unit
            }
            .map {
                when(it.second) {
                    "hr", "h" -> it.first * 3600
                    "min", "m" -> it.first * 60
                    "sec", "s" -> it.first
                    else -> throw IllegalStateException("Unknown unit [${it.second}]")
                }
            }
            .sum()

        return Duration(seconds, SECONDS)
    }

    private fun extractAnimeSeason(data: ExtractionResult): AnimeSeason {
        val splitSeasonString = if (data.notFound("season")) {
            listOf(EMPTY)
        } else {
            data.listNotNull<String>("season").first().remove("Season ").split(' ')
        }

        val seasonString = splitSeasonString.first().trim().lowercase()
        val season = when(seasonString) {
            "winter" -> WINTER
            "spring" -> SPRING
            "summer" -> SUMMER
            "fall" -> FALL
            else -> UNDEFINED
        }

        val year = if (splitSeasonString.size == 2) {
            YEAR_REGEX.find(splitSeasonString[1])?.value?.trim()?.ifBlank { "0" }?.toIntOrNull() ?: 0
        } else {
            YEAR_REGEX.find(data.stringOrDefault("year"))?.value?.trim()?.toIntOrNull() ?: 0
        }

        return AnimeSeason(
            season = season,
            year = year,
        )
    }

    private fun extractSourcesEntry(jsonData: ExtractionResult, data: ExtractionResult): HashSet<URI> {
        if (!data.notFound("sourceDiv")) {
            return hashSetOf(metaDataProviderConfig.buildAnimeLink(data.string("sourceDiv")))
        }

        if (jsonData.notFound("url")) {
            val link = jsonData.string("url").trim().remove("www.").ifBlank {
                data.string("sourceMeta").trim().remove("www.")
            }

            return hashSetOf(URI(link))
        }

        throw IllegalStateException("Unable to extract source.")
    }

    private fun extractSynonyms(jsonData: ExtractionResult): HashSet<Title> {
        return jsonData.listNotNull<Title>("alternateName")
            .map { StringEscapeUtils.unescapeHtml4(it) }
            .toHashSet()
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

    private fun extractTags(jsonData: ExtractionResult, data: ExtractionResult): HashSet<Tag> {
        val tags: HashSet<String> = if (jsonData.notFound("genre")) {
            hashSetOf()
        } else {
            jsonData.listNotNull<Tag>("genre")
                .map { it.trim().lowercase() }
                .toHashSet()
        }

        if (!data.notFound("tags")) {
            data.listNotNull<Tag>("tags")
                .map { it.trim().lowercase() }
                .forEach { tags.add(it) }
        }

        return tags
    }

    public companion object {
        private const val LARGE_PICTURE_INDICATOR = "large.jpg"
        private const val SMALL_PICTURE_INDICATOR = "small.jpg"
        private val YEAR_REGEX = """\d{4}""".toRegex()

        /**
         * Singleton of [LivechartAnimeConverter]
         * @since 3.1.0
         */
        public val instance: LivechartAnimeConverter by lazy { LivechartAnimeConverter() }
    }
}