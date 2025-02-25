package io.github.manamiproject.modb.anisearch

import io.github.manamiproject.modb.core.anime.*
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE_THUMBNAIL
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.HOURS
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.ExtractionResult
import io.github.manamiproject.modb.core.extractor.JsonDataExtractor
import io.github.manamiproject.modb.core.extractor.XmlDataExtractor
import kotlinx.coroutines.withContext
import java.net.URI
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN as UNKNOWN_STATUS
import io.github.manamiproject.modb.core.anime.AnimeType.UNKNOWN as UNKNOWN_TYPE

/**
 * Converts raw data to an [AnimeRaw].
 * Requires raw HTML.
 * @since 1.0.0
 * @param metaDataProviderConfig Configuration for converting data.
 * @param relationsDir Directory containing the raw files for the related anime.
 * @throws IllegalArgumentException if [relationsDir] doesn't exist or is not a directory.
 */
public class AnisearchAnimeConverter(
    private val metaDataProviderConfig: MetaDataProviderConfig = AnisearchConfig,
    private val xmlExtractor: DataExtractor = XmlDataExtractor,
    private val jsonExtractor: DataExtractor = JsonDataExtractor,
    private val relationsDir: Directory,
) : AnimeConverter {

    init {
        require(relationsDir.directoryExists()) { "Directory for relations [$relationsDir] does not exist or is not a directory." }
    }

    override suspend fun convert(rawContent: String): AnimeRaw = withContext(LIMITED_CPU) {
        val data = xmlExtractor.extract(rawContent, mapOf(
            "jsonld" to "//script[@type='application/ld+json']/node()",
            "image" to "//meta[@property='og:image']/@content",
            "title" to "//meta[@property='og:title']/@content",
            "type" to "//ul[@class='xlist row simple infoblock']//div[@class='type']",
            "status" to "//div[@class='status']",
            "duration" to "//ul[@class='xlist row simple infoblock']//time",
            "tags" to "//section[@id='description']//ul[@class='cloud']//li//a/text()",
            "source" to "//div[@id='content-outer']/@data-id",
            "synonymsByLanguage" to "//div[@class='title']//strong/text()",
            "synonymsBySubheader" to "//div[@class='title']//div[@class='grey']/text()",
            "synonymsDivNoSpan" to "//div[@class='synonyms']",
            "synonymsDivSpan" to "//div[@class='synonyms']//span[@id='text-synonyms']",
            "synonymsItalic" to "//div[@class='synonyms']//i/text()",
            "score" to "//td[contains(text(), 'Calculated Value')]/text()",
        ))
        
        val jsonld = data.listNotNull<String>("jsonld").first()
        val jsonData = jsonExtractor.extract(jsonld, mapOf(
            "title" to "$.name",
            "source" to "$.url",
            "image" to "$.image",
            "episodes" to "$.numberOfEpisodes", // they mess up the type. They use both strings and integer for episodes
            "year" to "startDate",
            "ratingValue" to "$.aggregateRating.ratingValue",
            "worstRating" to "$.aggregateRating.worstRating",
            "bestRating" to "$.aggregateRating.bestRating",
            "ratingCount" to ".aggregateRating.ratingCount",
        ))
        
        val thumbnail = extractThumbnail(data)

        return@withContext AnimeRaw(
            _title = extractTitle(jsonData, data),
            episodes = extractEpisodes(jsonData),
            type = extractType(data),
            picture = generatePicture(thumbnail),
            thumbnail = thumbnail,
            status = extractStatus(data),
            duration = extractDuration(data),
            animeSeason = extractAnimeSeason(jsonData),
            _sources = extractSourcesEntry(data),
            _synonyms = extractSynonyms(data),
            _relatedAnime = extractRelatedAnime(data),
            _tags = extractTags(data),
        ).addScores(extractScore(jsonData, data))
    }

    private fun extractTitle(jsonldData: ExtractionResult, data: ExtractionResult): Title {
        return jsonldData.stringOrDefault("title").ifBlank { data.stringOrDefault("title") }
            .trim()
            .remove(" (Anime)")
            .trim()
    }

    private fun extractEpisodes(jsonldData: ExtractionResult): Episodes = jsonldData.intOrDefault("episodes", 1)

    private fun extractType(data: ExtractionResult): AnimeType {
        val type = data.string("type")
            .split(',')
            .first()
            .trim()
            .trimStart('[')
            .lowercase()

        return when(type) {
            "bonus" -> SPECIAL
            "cm" -> SPECIAL
            "movie" -> MOVIE
            "music video" -> SPECIAL
            "other" -> UNKNOWN_TYPE
            "ova" -> OVA
            "tv-series" -> TV
            "tv-special" -> TV
            "unknown" -> UNKNOWN_TYPE
            "web" -> ONA
            else -> throw IllegalStateException("Unmapped type [$type]")
        }
    }

    private fun extractThumbnail(data: ExtractionResult): URI {
        // links in JSON are invalid (http 404) for a few weeks now. Have to solely rely on meta tag again
        return if (data.notFound("image")) {
            NO_PICTURE_THUMBNAIL
        } else {
            URI(data.string("image").trim())
        }
    }

    private fun generatePicture(picture: URI): URI {
        val value = picture.toString()
            .remove("/full")
            .replace(".webp", "_300.webp")
        return URI(value)
    }

    private fun extractStatus(data: ExtractionResult): AnimeStatus {
        val value = if (data.notFound("status")) {
            EMPTY
        } else {
            data.listNotNull<String>("status").first()
        }

        return when(value.trim().lowercase()) {
            "aborted" -> UNKNOWN_STATUS
            "completed" -> FINISHED
            "ongoing" -> ONGOING
            "upcoming" -> UPCOMING
            "on hold", EMPTY -> UNKNOWN_STATUS
            else -> throw IllegalStateException("Unmapped status [$value]")
        }
    }

    private fun extractDuration(data: ExtractionResult): Duration {
        val textValue = data.stringOrDefault("duration", "0")
            .trim()
            .lowercase()

        val value = Regex("\\d+").find(textValue)!!.value.toInt()
        val fallbackUnit = "unknown"
        val extractedUnit = Regex("[aA-zZ]+").find(textValue)?.value ?: fallbackUnit

        when {
            value > 0 && extractedUnit == fallbackUnit -> throw IllegalStateException("Value for duration is present [], but unit is unknown")
            value == 0 && extractedUnit == fallbackUnit -> return Duration.UNKNOWN
        }

        val unit = when(extractedUnit) {
            "hrs" -> HOURS
            "min" -> MINUTES
            else -> throw IllegalStateException("Unmapped duration unit [$extractedUnit]")
        }

        return Duration(value, unit)
    }

    private fun extractAnimeSeason(jsonldData: ExtractionResult): AnimeSeason {
        val date = jsonldData.stringOrDefault("year")
        if (date.eitherNullOrBlank()) {
            return AnimeSeason()
        }

        val year = Regex("[0-9]{4}").find(date)!!.value.toInt()
        val fallback = "0"
        val month = (Regex("-[0-9]{2}-").find(date)?.value?.replace(Regex("-"), EMPTY)?.ifBlank { fallback } ?: fallback).toInt()

        val season = when(month) {
            1, 2, 3 -> WINTER
            4, 5, 6 -> SPRING
            7, 8, 9 -> SUMMER
            10, 11, 12 -> FALL
            else -> UNDEFINED
        }

        return AnimeSeason(
            year = year,
            season = season
        )
    }

    private fun extractSourcesEntry(data: ExtractionResult): HashSet<URI> {
        return hashSetOf(metaDataProviderConfig.buildAnimeLink(data.string("source")))
    }

    private fun extractSynonyms(data: ExtractionResult): HashSet<Title> {
        val synonyms = if (data.notFound("synonymsByLanguage")) {
            hashSetOf()
        } else {
            data.listNotNull<Title>("synonymsByLanguage").toHashSet()
        }

        if (!data.notFound("synonymsBySubheader")) {
            data.listNotNull<Title>("synonymsBySubheader").forEach { synonyms.add(it) }
        }

        if (!data.notFound("synonymsDivNoSpan")) {
            data.listNotNull<Title>("synonymsDivNoSpan")
                .forEach { synonyms.add(it) }
        }

        if (!data.notFound("synonymsDivSpan")) {
            data.listNotNull<Title>("synonymsDivSpan")
                .forEach { synonyms.add(it) }
        }

        if (!data.notFound("synonymsItalic")) {
            data.listNotNull<Title>("synonymsItalic")
                .forEach { synonyms.add(it) }
        }

        return synonyms.map { it.trimStart(',').trimEnd(',') }.toHashSet()
    }

    private suspend fun extractRelatedAnime(data: ExtractionResult): HashSet<URI> = withContext(LIMITED_CPU) {
        val id = data.string("source")
        val relationsFile = relationsDir.resolve("$id.${metaDataProviderConfig.fileSuffix()}")

        check(relationsFile.regularFileExists()) { "Relations file is missing for [$id]." }

        val relatedAnimeData = xmlExtractor.extract(relationsFile.readFile(), mapOf(
            "relatedAnime" to "//section[@id='relations_anime']//table//tbody//a/@href",
        ))

        if (relatedAnimeData.notFound("relatedAnime")) {
            hashSetOf()
        } else {
            relatedAnimeData.listNotNull<String>("relatedAnime")
                .map { it.remove("anime/") }
                .map { it.substring(0, it.indexOf(',')) }
                .map { metaDataProviderConfig.buildAnimeLink(it) }
                .toHashSet()
        }
    }

    private fun extractScore(jsonData: ExtractionResult, data: ExtractionResult): MetaDataProviderScore {
        val from = jsonData.doubleOrDefault("worstRating", 0.1)
        val to = jsonData.doubleOrDefault("bestRating", 5.0)
        val rawScore = when {
            !jsonData.notFound("ratingValue") -> {
                // if there is effectively no rating they set the rating to 2.5 (more or less the middle of the range and set count to 1
                jsonData.doubleOrDefault("ratingValue")
                    .takeUnless { jsonData.int("ratingCount") == 1 && jsonData.doubleOrDefault("ratingValue") == 2.5 }
                    ?: 0.0
            }
            !data.notFound("score") && jsonData.notFound("ratingValue") -> {
                data.string("score")
                    .remove("Calculated Value")
                    .substringBefore('=')
                    .trim()
                    .toDoubleOrNull() ?: 0.0
            }
            else -> 0.0
        }

        return if (rawScore == 0.0) {
            NoMetaDataProviderScore
        } else {
            MetaDataProviderScoreValue(
                hostname = metaDataProviderConfig.hostname(),
                value = rawScore,
                range = from..to,
            )
        }
    }

    private fun extractTags(data: ExtractionResult): HashSet<Tag> {
        return if (data.notFound("tags")) {
            hashSetOf()
        } else {
            data.listNotNull<Tag>("tags").toHashSet()
        }
    }
}