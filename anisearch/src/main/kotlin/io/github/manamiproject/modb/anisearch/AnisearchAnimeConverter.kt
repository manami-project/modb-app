package io.github.manamiproject.modb.anisearch

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.ExtractionResult
import io.github.manamiproject.modb.core.extractor.JsonDataExtractor
import io.github.manamiproject.modb.core.extractor.XmlDataExtractor
import io.github.manamiproject.modb.core.models.*
import io.github.manamiproject.modb.core.models.Anime.Companion.NO_PICTURE_THUMBNAIL
import io.github.manamiproject.modb.core.models.Anime.Status.*
import io.github.manamiproject.modb.core.models.Anime.Type.*
import io.github.manamiproject.modb.core.models.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.models.Duration.TimeUnit.HOURS
import io.github.manamiproject.modb.core.models.Duration.TimeUnit.MINUTES
import kotlinx.coroutines.withContext
import java.net.URI

/**
 * Converts raw data to an [Anime].
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

    override suspend fun convert(rawContent: String): Anime = withContext(LIMITED_CPU) {
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
        ))
        
        val jsonld = data.listNotNull<String>("jsonld").first()
        val jsonData = jsonExtractor.extract(jsonld, mapOf(
            "title" to "$.name",
            "source" to "$.url",
            "image" to "$.image",
            "episodes" to "$.numberOfEpisodes", // they mess up the type. They use both strings and integer for episodes
            "year" to "startDate",
        ))
        
        val thumbnail = extractThumbnail(data)

        return@withContext Anime(
            _title = extractTitle(jsonData, data),
            episodes = extractEpisodes(jsonData),
            type = extractType(data),
            picture = generatePicture(thumbnail),
            thumbnail = thumbnail,
            status = extractStatus(data),
            duration = extractDuration(data),
            animeSeason = extractAnimeSeason(jsonData),
            sources = extractSourcesEntry(data),
            synonyms = extractSynonyms(data),
            relatedAnime = extractRelatedAnime(data),
            tags = extractTags(data),
        )
    }

    private fun extractTitle(jsonldData: ExtractionResult, data: ExtractionResult): Title {
        return jsonldData.stringOrDefault("title").ifBlank { data.stringOrDefault("title") }
            .trim()
            .remove(" (Anime)")
            .trim()
    }

    private fun extractEpisodes(jsonldData: ExtractionResult): Episodes = jsonldData.intOrDefault("episodes", 1)

    private fun extractType(data: ExtractionResult): Anime.Type {
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
            "other" -> Anime.Type.UNKNOWN
            "ova" -> OVA
            "tv-series" -> TV
            "tv-special" -> TV
            "unknown" -> Anime.Type.UNKNOWN
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

    private fun extractStatus(data: ExtractionResult): Anime.Status {
        val value = if (data.notFound("status")) {
            EMPTY
        } else {
            data.listNotNull<String>("status").first()
        }

        return when(value.trim().lowercase()) {
            "aborted" -> Anime.Status.UNKNOWN
            "completed" -> FINISHED
            "ongoing" -> ONGOING
            "upcoming" -> UPCOMING
            "on hold", EMPTY -> Anime.Status.UNKNOWN
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

    private fun extractTags(data: ExtractionResult): HashSet<Tag> {
        return if (data.notFound("tags")) {
            hashSetOf()
        } else {
            data.listNotNull<Tag>("tags").toHashSet()
        }
    }
}