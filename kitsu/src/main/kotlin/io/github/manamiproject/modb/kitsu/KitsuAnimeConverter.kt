package io.github.manamiproject.modb.kitsu

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.ExtractionResult
import io.github.manamiproject.modb.core.extractor.JsonDataExtractor
import io.github.manamiproject.modb.core.models.*
import io.github.manamiproject.modb.core.models.Anime.Companion.NO_PICTURE
import io.github.manamiproject.modb.core.models.Anime.Companion.NO_PICTURE_THUMBNAIL
import io.github.manamiproject.modb.core.models.Anime.Status
import io.github.manamiproject.modb.core.models.Anime.Status.*
import io.github.manamiproject.modb.core.models.Anime.Type
import io.github.manamiproject.modb.core.models.Anime.Type.*
import io.github.manamiproject.modb.core.models.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.models.Duration.TimeUnit.MINUTES
import kotlinx.coroutines.withContext
import java.net.URI

/**
 * Converts raw data to an [Anime].
 * @since 1.0.0
 * @param metaDataProviderConfig Configuration for converting data.
 * @param relationsDir Directory containing the raw files for the related anime.
 * @param tagsDir Directory containing the raw files for the tags.
 * @throws IllegalArgumentException if either [relationsDir] or [tagsDir] doesn't exist or is not a directory.
 */
public class KitsuAnimeConverter(
    private val metaDataProviderConfig: MetaDataProviderConfig = KitsuConfig,
    private val extractor: DataExtractor = JsonDataExtractor,
    private val relationsDir: Directory,
    private val tagsDir: Directory,
) : AnimeConverter {

    init {
        require(relationsDir.directoryExists()) { "Directory for relations [$relationsDir] does not exist or is not a directory." }
        require(tagsDir.directoryExists()) { "Directory for tags [$tagsDir] does not exist or is not a directory." }
    }

    override suspend fun convert(rawContent: String): Anime = withContext(LIMITED_CPU) {
        val data = extractor.extract(rawContent, mapOf(
            "title" to "$.data.attributes.canonicalTitle",
            "episodeCount" to "$.data.attributes.episodeCount",
            "subtype" to "$.data.attributes.subtype",
            "id" to "$.data.id",
            "pictureSmall" to "$.data.attributes.posterImage.small",
            "pictureTiny" to "$.data.attributes.posterImage.tiny",
            "abbreviatedTitles" to "$.data.attributes.abbreviatedTitles",
            "titles" to "$.data.attributes.titles",
            "status" to "$.data.attributes.status",
            "episodeLength" to "$.data.attributes.episodeLength",
            "startDate" to "$.data.attributes.startDate",
        ))

        return@withContext Anime(
            _title = extractTitle(data),
            episodes = extractEpisodes(data),
            type = extractType(data),
            picture = extractPicture(data),
            thumbnail = extractThumbnail(data),
            status = extractStatus(data),
            duration = extractDuration(data),
            animeSeason = extractAnimeSeason(data),
            sources = extractSourcesEntry(data),
            synonyms = extractSynonyms(data),
            relatedAnime = extractRelatedAnime(data),
            tags = extractTags(data),
        )
    }

    private fun extractTitle(data: ExtractionResult): Title = data.string("title")

    private fun extractEpisodes(data: ExtractionResult): Episodes = data.intOrDefault("episodeCount")

    private fun extractPicture(data: ExtractionResult): URI {
        return if (data.notFound("pictureSmall")) {
            NO_PICTURE
        } else {
            URI(data.string("pictureSmall").trim())
        }
    }

    private fun extractThumbnail(data: ExtractionResult): URI {
        return if (data.notFound("pictureTiny")) {
            NO_PICTURE_THUMBNAIL
        } else {
            URI(data.string("pictureTiny").trim())
        }
    }

    private fun extractSourcesEntry(data: ExtractionResult): HashSet<URI> = hashSetOf(metaDataProviderConfig.buildAnimeLink(data.string("id").trim()))

    private fun extractType(data: ExtractionResult): Type {
        return when(data.string("subtype").trim().lowercase()) {
            "tv" -> TV
            "ona" -> ONA
            "movie" -> MOVIE
            "ova" -> OVA
            "special" -> SPECIAL
            "music" -> SPECIAL
            else -> throw IllegalStateException("Unknown type [${data.string("subtype")}]")
        }
    }

    private fun extractSynonyms(data: ExtractionResult): HashSet<Title> {
        return data.listNotNull<Title>("abbreviatedTitles").union(
               data.listNotNull<LinkedHashMap<String, Title>>("titles").first().values.filterNotNull()).toHashSet()
    }

    private suspend fun extractRelatedAnime(data: ExtractionResult): HashSet<URI> = withContext(LIMITED_CPU) {
        val relationsFile = relationsDir.resolve("${data.string("id")}.${metaDataProviderConfig.fileSuffix()}")

        check(relationsFile.regularFileExists()) { "Relations file is missing" }

        val relatedAnimeData = extractor.extract(relationsFile.readFile(), mapOf(
            "relatedAnime" to "$.included"
        ))

        if (relatedAnimeData.notFound("relatedAnime")) {
            return@withContext hashSetOf()
        }

        return@withContext relatedAnimeData.listNotNull<LinkedHashMap<String, Any>>("relatedAnime")
             .filter { it["type"].toString() == "anime"}
             .map { it["id"] }
             .map { metaDataProviderConfig.buildAnimeLink(it.toString()) }
             .toHashSet()
    }

    private fun extractStatus(data: ExtractionResult): Status {
        if (data.notFound("status")) {
            return Status.UNKNOWN
        }

        return when(data.string("status").trim().lowercase()) {
            "finished" -> FINISHED
            "current" -> ONGOING
            "unreleased" -> UPCOMING
            "upcoming" -> UPCOMING
            "tba" -> Status.UNKNOWN
            else -> throw IllegalStateException("Unknown status [${data.string("status")}]")
        }
    }

    private fun extractDuration(data: ExtractionResult): Duration {
        val durationInMinutes = data.intOrDefault("episodeLength")
        return Duration(durationInMinutes, MINUTES)
    }

    private suspend fun extractTags(data: ExtractionResult): HashSet<Tag> = withContext(LIMITED_CPU) {
        val tagsFile = tagsDir.resolve("${data.string("id")}.${metaDataProviderConfig.fileSuffix()}")

        check(tagsFile.regularFileExists()) { "Tags file is missing" }

        val tagsData = extractor.extract(tagsFile.readFile(), mapOf(
            "attributes" to "$.data.*.attributes.title"
        ))

        return@withContext tagsData.listNotNull<Tag>("attributes").toHashSet()
    }

    private fun extractAnimeSeason(data: ExtractionResult): AnimeSeason {
        val startDate = data.stringOrDefault("startDate", EMPTY)
        val month = Regex("-[0-9]{2}-").findAll(startDate).firstOrNull()?.value?.remove("-")?.toInt() ?: 0
        val year = Regex("[0-9]{4}").find(startDate)?.value?.let { if (it.startsWith("0")) "0" else it }?.toInt() ?: 0

        val season = when(month) {
            12, 1, 2 -> WINTER
            3, 4, 5 -> SPRING
            6, 7, 8 -> SUMMER
            9, 10, 11 -> FALL
            else -> UNDEFINED
        }

        return AnimeSeason(
            season = season,
            year = year
        )
    }
}