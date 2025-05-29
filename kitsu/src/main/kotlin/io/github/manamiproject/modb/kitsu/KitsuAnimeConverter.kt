package io.github.manamiproject.modb.kitsu

import io.github.manamiproject.modb.core.anime.*
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE_THUMBNAIL
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.extensions.*
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.ExtractionResult
import io.github.manamiproject.modb.core.extractor.JsonDataExtractor
import kotlinx.coroutines.withContext
import java.net.URI
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN as UNKNOWN_STATUS

/**
 * Converts raw data to an [AnimeRaw].
 * @since 1.0.0
 * @property metaDataProviderConfig Configuration for converting data.
 * @property extractor Extractor which retrieves the data from raw data.
 */
public class KitsuAnimeConverter(
    private val metaDataProviderConfig: MetaDataProviderConfig = KitsuConfig,
    private val extractor: DataExtractor = JsonDataExtractor,
) : AnimeConverter {

    override suspend fun convert(rawContent: String): AnimeRaw = withContext(LIMITED_CPU) {
        val data = extractor.extract(rawContent, mapOf(
            "title" to "$.data[0].attributes.canonicalTitle",
            "episodeCount" to "$.data[0].attributes.episodeCount",
            "subtype" to "$.data[0].attributes.subtype",
            "id" to "$.data[0].id",
            "pictureSmall" to "$.data[0].attributes.posterImage.small",
            "pictureTiny" to "$.data[0].attributes.posterImage.tiny",
            "abbreviatedTitles" to "$.data[0].attributes.abbreviatedTitles",
            "titles" to "$.data[0].attributes.titles",
            "status" to "$.data[0].attributes.status",
            "episodeLength" to "$.data[0].attributes.episodeLength",
            "startDate" to "$.data[0].attributes.startDate",
            "score" to "$.data[0].attributes.averageRating",
            "included" to "$.included"
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
            _studios = extractStudios(data),
            _producers = extractProducers(data),
        ).addScores(extractScore(data))
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

    private fun extractType(data: ExtractionResult): AnimeType {
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

    private fun extractRelatedAnime(data: ExtractionResult): HashSet<URI> {
        val included = data.listNotNull<Map<String, Any>>("included")

        return included.filter { it["type"] == "mediaRelationships" }
            .map { it as Map<*,*> }
            .map { it["relationships"] as Map<*,*> }
            .map { it["destination"] as Map<*,*> }
            .map { it["data"] as Map<*,*> }
            .filter { it["type"] as String == "anime" }
            .map { it["id"] as AnimeId }
            .map { metaDataProviderConfig.buildAnimeLink(it) }
            .toHashSet()
    }

    private fun extractStatus(data: ExtractionResult): AnimeStatus {
        if (data.notFound("status")) {
            return UNKNOWN_STATUS
        }

        return when(data.string("status").trim().lowercase()) {
            "finished" -> FINISHED
            "current" -> ONGOING
            "unreleased" -> UPCOMING
            "upcoming" -> UPCOMING
            "tba" -> UNKNOWN_STATUS
            else -> throw IllegalStateException("Unknown status [${data.string("status")}]")
        }
    }

    private fun extractDuration(data: ExtractionResult): Duration {
        val durationInMinutes = data.intOrDefault("episodeLength")
        return Duration(durationInMinutes, MINUTES)
    }

    private fun extractTags(data: ExtractionResult): HashSet<Tag> {
        val included = data.listNotNull<Map<String, Any>>("included")

        val genres = included.filter { it["type"] == "genres" }
            .map { it as Map<*,*> }
            .map { it["attributes"] as Map<*,*> }
            .map { it["name"] as Tag }

        val categories = included.filter { it["type"] == "categories" }
            .map { it as Map<*,*> }
            .map { it["attributes"] as Map<*,*> }
            .map { it["title"] as Tag }

        return genres.union(categories).toHashSet()
    }

    private fun extractAnimeSeason(data: ExtractionResult): AnimeSeason {
        val startDate = data.stringOrDefault("startDate", EMPTY)
        val month = """-\d{2}-""".toRegex().findAll(startDate).firstOrNull()?.value?.remove("-")?.toInt() ?: 0
        val year = """\d{4}""".toRegex().find(startDate)?.value?.let { if (it.startsWith("0")) "0" else it }?.toInt() ?: 0

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

    private fun extractScore(data: ExtractionResult): MetaDataProviderScore {
        if (data.notFound("score")) {
            return NoMetaDataProviderScore
        }

        val rawScore = data.double("score")

        return MetaDataProviderScoreValue(
            hostname = metaDataProviderConfig.hostname(),
            value = rawScore,
            range = 1.0..100.0,
        )
    }

    private fun extractStudios(data: ExtractionResult): HashSet<Studio> {
        val included = data.listNotNull<Map<String, Any>>("included")

        val producers = included.filter { it["type"] == "producers" }
            .associate { (it["id"] as String) to ((it["attributes"] as Map<*,*>)["name"] as String) }

        val animeProductionsRelationDestinationIds = included.filter { it["type"] == "animeProductions" }
            .map { it as Map<*,*> }
            .filter { (it["attributes"] as Map<*,*>)["role"] as String == "studio" }
            .map { it["relationships"] as Map<*,*> }
            .map { it["producer"] as Map<*,*> }
            .map { it["data"] as Map<*,*> }
            .map { it["id"] as String }

        return animeProductionsRelationDestinationIds.mapNotNull { producers[it] }.toHashSet()
    }

    private fun extractProducers(data: ExtractionResult): HashSet<Producer> {
        val included = data.listNotNull<Map<String, Any>>("included")

        val producers = included.filter { it["type"] == "producers" }
            .associate { (it["id"] as String) to ((it["attributes"] as Map<*,*>)["name"] as String) }

        val animeProductionsRelationDestinationIds = included.filter { it["type"] == "animeProductions" }
            .map { it as Map<*,*> }
            .filter { (it["attributes"] as Map<*,*>)["role"] as String == "producer" }
            .map { it["relationships"] as Map<*,*> }
            .map { it["producer"] as Map<*,*> }
            .map { it["data"] as Map<*,*> }
            .map { it["id"] as String }

        return animeProductionsRelationDestinationIds.mapNotNull { producers[it] }.toHashSet()
    }

    public companion object {
        /**
         * Singleton of [KitsuAnimeConverter]
         * @since 7.0.0
         */
        public val instance: KitsuAnimeConverter by lazy { KitsuAnimeConverter() }
    }
}