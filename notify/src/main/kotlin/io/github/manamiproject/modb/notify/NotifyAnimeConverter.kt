package io.github.manamiproject.modb.notify

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.ExtractionResult
import io.github.manamiproject.modb.core.extractor.JsonDataExtractor
import io.github.manamiproject.modb.core.anime.*
import io.github.manamiproject.modb.core.anime.AnimeSeason.Companion.UNKNOWN_YEAR
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN as UNKNOWN_STATUS
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import kotlinx.coroutines.withContext
import java.net.URI

/**
 * The conversion requires two files. The file with all main data and a second file containing related anime.
 * IDs are always identical. If an anime doesn't provide any related anime it still has to have a file for related anime.
 * @since 1.0.0
 * @property metaDataProviderConfig Configuration for converting data.
 * @property extractor Extractor which retrieves the data from raw data.
 * @property relationsDir Directory containing the raw files for the related anime.
 * @throws IllegalArgumentException if the [relationsDir] doesn't exist or is not a directory.
 */
public class NotifyAnimeConverter(
    private val metaDataProviderConfig: MetaDataProviderConfig = NotifyConfig,
    private val extractor: DataExtractor = JsonDataExtractor,
    private val relationsDir: Directory,
) : AnimeConverter {

    init {
        require(relationsDir.directoryExists()) { "Directory for relations [$relationsDir] does not exist or is not a directory." }
    }

    override suspend fun convert(rawContent: String): AnimeRaw = withContext(LIMITED_CPU) {
        val data = extractor.extract(rawContent, mapOf(
            "title" to "$.title.canonical",
            "titles" to "$.title",
            "synonyms" to "$.title.synonyms",
            "episodes" to "$.episodeCount",
            "type" to "$.type",
            "id" to "$.id",
            "imageExtension" to "$.image.extension",
            "episodeLength" to "$.episodeLength",
            "status" to "$.status",
            "startDate" to "$.startDate",
            "genres" to "$.genres",
            "score" to "$.rating.overall",
        ))

        return@withContext AnimeRaw(
            _title = extractTitle(data),
            episodes = extractEpisodes(data),
            type = extractType(data),
            picture = extractPicture(data),
            thumbnail = extractThumbnail(data),
            status = extractStatus(data),
            duration = extractDuration(data),
            animeSeason = extractAnimeSeason(data),
            _sources = extractSourcesEntry(data),
            _synonyms = extractSynonyms(data),
            _relatedAnime = extractRelatedAnime(data),
            _tags = extractTags(data),
            _studios = hashSetOf(), // notify uses foreign key relations which is not supported
            _producers = hashSetOf(), // notify uses foreign key relations which is not supported
        ).addScores(extractScore(data))
    }

    private fun extractTitle(data: ExtractionResult) = data.string("title")

    private fun extractEpisodes(data: ExtractionResult) = data.int("episodes")

    private fun extractType(data: ExtractionResult): AnimeType {
        return when(data.string("type").trim().lowercase()) {
            "tv" -> TV
            "movie" -> MOVIE
            "ova" -> OVA
            "ona" -> ONA
            "special" -> SPECIAL
            "music" -> SPECIAL
            else -> throw IllegalStateException("Unknown type [${data.string("type")}]")
        }
    }

    private fun extractPicture(data: ExtractionResult) = URI("https://media.notify.moe/images/anime/large/${data.string("id").trim()}${data.string("imageExtension").trim()}")

    private fun extractThumbnail(data: ExtractionResult) = URI("https://media.notify.moe/images/anime/small/${data.string("id").trim()}${data.string("imageExtension").trim()}")

    private fun extractSynonyms(data: ExtractionResult): HashSet<Title> {
        val titles = data.listNotNull<HashMap<String, Title>>("titles")
            .first()
            .filterNot { it.key == "canonical" }
            .filterNot { it.key == "synonyms" }
            .values
            .filterNot { it == data.string("title") }

        val synonyms = if (data.notFound("synonyms")) {
            emptyList()
        } else {
            data.listNotNull<Title>("synonyms")
                .filterNot { it == data.string("title") }
        }

        return titles.union(synonyms).toHashSet()
    }

    private fun extractSourcesEntry(data: ExtractionResult) = hashSetOf(metaDataProviderConfig.buildAnimeLink(data.string("id").trim()))

    private suspend fun extractRelatedAnime(data: ExtractionResult): HashSet<URI> = withContext(LIMITED_CPU) {
        val relationsFile = relationsDir.resolve("${data.string("id")}.${metaDataProviderConfig.fileSuffix()}")

        return@withContext if (relationsFile.regularFileExists()) {
            val relatedAnimeData = extractor.extract(relationsFile.readFile(), mapOf(
                "relatedAnimeIds" to "$.items.*.animeId"
            ))

            if (relatedAnimeData.notFound("relatedAnimeIds")) {
                hashSetOf()
            } else {
                relatedAnimeData.listNotNull<URI>("relatedAnimeIds") { metaDataProviderConfig.buildAnimeLink(it.trim()) }.toHashSet()
            }
        } else {
            hashSetOf()
        }
    }

    private fun extractStatus(data: ExtractionResult): AnimeStatus {
        return when(data.string("status").trim().lowercase()) {
            "finished" -> FINISHED
            "current" -> ONGOING
            "upcoming" -> UPCOMING
            "tba" -> UNKNOWN_STATUS
            else -> throw IllegalStateException("Unknown status [${data.string("status")}]")
        }
    }

    private fun extractDuration(data: ExtractionResult) = Duration(data.int("episodeLength"), MINUTES)

    private fun extractAnimeSeason(data: ExtractionResult): AnimeSeason {
        val month = """-\d{2}-""".toRegex().findAll(data.string("startDate")).firstOrNull()?.value?.remove("-")?.toInt() ?: 0
        val year = """\d{4}""".toRegex().findAll(data.string("startDate")).firstOrNull()?.value?.toInt() ?: UNKNOWN_YEAR

        val season = when(month) {
            1, 2, 3 -> WINTER
            4, 5, 6 -> SPRING
            7, 8, 9 -> SUMMER
            10, 11, 12 -> FALL
            else -> UNDEFINED
        }

        return AnimeSeason(
            season = season,
            year = year
        )
    }

    private fun extractTags(data: ExtractionResult): HashSet<Tag> {
        return if (data.notFound("genres")) {
            hashSetOf()
        } else {
            data.listNotNull<Title>("genres").toHashSet()
        }
    }

    private fun extractScore(data: ExtractionResult): MetaDataProviderScore {
        if (data.notFound("score")) {
            return NoMetaDataProviderScore
        }

        val rawScore = data.double("score")

        return if (rawScore == 0.0) {
            NoMetaDataProviderScore
        } else {
            MetaDataProviderScoreValue(
                hostname = metaDataProviderConfig.hostname(),
                value = rawScore,
                range = 1.0..10.0,
            )
        }
    }
}