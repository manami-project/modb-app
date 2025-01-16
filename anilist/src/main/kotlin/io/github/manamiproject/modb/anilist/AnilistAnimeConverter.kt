package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
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
import io.github.manamiproject.modb.core.models.Duration.TimeUnit.MINUTES
import kotlinx.coroutines.withContext
import java.net.URI

/**
 * Converts raw data to an [Anime].
 * @since 1.0.0
 * @param metaDataProviderConfig Configuration for converting data.
 */
public class AnilistAnimeConverter(
    private val metaDataProviderConfig: MetaDataProviderConfig = AnilistConfig,
    private val extractor: DataExtractor = JsonDataExtractor,
) : AnimeConverter {

    override suspend fun convert(rawContent: String): Anime = withContext(LIMITED_CPU) {

        val data: ExtractionResult = extractor.extract(rawContent, mapOf(
            "userPreferred" to "$.data.Media.title.userPreferred",
            "titles" to "$.data.Media.title",
            "episodes" to "$.data.Media.episodes",
            "nextAiringEpisode" to "$.data.Media.nextAiringEpisode.episode",
            "format" to "$.data.Media.format",
            "picture" to "$.data.Media.coverImage.large",
            "synonyms" to "$.data.Media.synonyms",
            "duration" to "$.data.Media.duration",
            "season" to "$.data.Media.season",
            "year" to "$.data.Media.seasonYear",
            "startYear" to "$.data.Media.startDate.year",
            "genres" to "$.data.Media.genres",
            "id" to "$.data.Media.id",
            "status" to "$.data.Media.status",
            "tags" to "$.data.Media.tags.*.name",
            "relatedAnime" to "$.data.Media.relations.edges.*.node"
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
            tags = extractTags(data),
            relatedAnime = extractRelatedAnime(data),
        )
    }

    private fun extractTitle(data: ExtractionResult): String = data.stringOrDefault("userPreferred")

    private fun extractEpisodes(data: ExtractionResult): Int {
        if (!data.notFound("episodes")) {
            return data.int("episodes")
        }

        return data.intOrDefault("nextAiringEpisode")
    }

    private fun extractType(data: ExtractionResult): Type {
        if (data.notFound("format")) {
            return Type.UNKNOWN
        }

        return when(data.string("format").trim().uppercase()) {
            "TV" -> TV
            "TV_SHORT" -> TV
            "MOVIE" -> MOVIE
            "ONA" -> ONA
            "OVA" -> OVA
            "SPECIAL" -> SPECIAL
            "MUSIC" -> SPECIAL
            else -> throw IllegalStateException("Unknown type [${data.string("format")}]")
        }
    }

    private fun extractPicture(data: ExtractionResult): URI {
        return if (data.notFound("picture")) {
            NO_PICTURE
        } else {
            URI(data.string("picture").trim())
        }
    }

    private fun extractThumbnail(data: ExtractionResult): URI {
        return if (data.notFound("picture")) {
            NO_PICTURE_THUMBNAIL
        } else {
            URI(data.string("picture").trim())
        }
    }

    private fun extractSynonyms(data: ExtractionResult): HashSet<Title> {
        return data.listNotNull<LinkedHashMap<String, Title?>>("titles")
            .flatMap { it.values }
            .union(data.listNotNull<Title>("synonyms"))
            .asSequence()
            .filterNot { it == data.string("userPreferred") }
            .filterNotNull()
            .toHashSet()
    }

    private fun extractSourcesEntry(data: ExtractionResult): HashSet<URI> {
        return hashSetOf(metaDataProviderConfig.buildAnimeLink(data.string("id").trim()))
    }

    private fun extractRelatedAnime(data: ExtractionResult): HashSet<URI> {
        return data.listNotNull<LinkedHashMap<String, Any>>("relatedAnime")
            .filter { it["type"] == "ANIME" }
            .mapNotNull { it["id"] }
            .map { metaDataProviderConfig.buildAnimeLink(it.toString().trim()) }
            .toHashSet()
    }

    private fun extractStatus(data: ExtractionResult): Status {
        if (data.notFound("status")) {
            return Status.UNKNOWN
        }

        return when(data.string("status").trim().uppercase()) {
            "FINISHED" -> FINISHED
            "RELEASING" -> ONGOING
            "NOT_YET_RELEASED" -> UPCOMING
            "CANCELLED" -> Status.UNKNOWN
            else -> throw IllegalStateException("Unknown status [${data.string("status")}]")
        }
    }

    private fun extractDuration(data: ExtractionResult): Duration {
        val durationInMinutes = data.intOrDefault("duration")
        return Duration(durationInMinutes, MINUTES)
    }

    private fun extractAnimeSeason(data: ExtractionResult): AnimeSeason {
        val seasonValue = data.stringOrDefault("season").trim()
        val season = AnimeSeason.Season.of(seasonValue)

        val year = if (data.notFound("year")) {
            data.intOrDefault("startYear")
        } else {
            data.int("year")
        }

        return AnimeSeason(
            season = season,
            year = year,
        )
    }

    private fun extractTags(data: ExtractionResult): HashSet<Tag> {
        val genres = data.listNotNull<Tag>("genres")
        val tags = data.listNotNull<Tag>("tags")

        return genres.plus(tags).toHashSet()
    }

    public companion object {
        /**
         * Singleton of [AnilistAnimeConverter]
         * @since 6.1.0
         */
        public val instance: AnilistAnimeConverter by lazy { AnilistAnimeConverter() }
    }
}