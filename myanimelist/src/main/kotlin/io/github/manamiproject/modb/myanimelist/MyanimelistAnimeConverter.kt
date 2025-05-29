package io.github.manamiproject.modb.myanimelist

import io.github.manamiproject.modb.core.anime.*
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE_THUMBNAIL
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.SECONDS
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.remove
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.ExtractionResult
import io.github.manamiproject.modb.core.extractor.XmlDataExtractor
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.withContext
import java.net.URI
import io.github.manamiproject.modb.core.anime.AnimeType.UNKNOWN as UNKNOWN_TYPE
import io.github.manamiproject.modb.core.anime.Duration.Companion.UNKNOWN as UNKNOWN_DURATION

/**
 * Converts raw data to an [AnimeRaw].
 * Requires raw HTML from the mobile site version.
 * @since 1.0.0
 * @property metaDataProviderConfig Configuration for converting data.
 * @property extractor Extracts specific data from raw content.
 */
public class MyanimelistAnimeConverter(
    private val metaDataProviderConfig: MetaDataProviderConfig = MyanimelistConfig,
    private val extractor: DataExtractor = XmlDataExtractor,
) : AnimeConverter {

    override suspend fun convert(rawContent: String): AnimeRaw = withContext(LIMITED_CPU) {
        val data = extractor.extract(rawContent, mapOf(
            "title" to "//meta[@property='og:title']/@content",
            "episodes" to "//td[text()='Episodes']/following-sibling::td/a/text()",
            "source" to "//meta[@property='og:url']/@content",
            "status" to "//td[text()='Status']/following-sibling::*/text()",
            "tags" to "//span[@itemprop='genre']/text()",
            "type" to "//td[text()='Type']/following-sibling::*/text()",
            "duration" to "//td[text()='Duration']/following-sibling::*/text()",
            "premiered" to "//td[text()='Premiered']/following-sibling::*/text()",
            "aired" to "//td[text()='Aired']/following-sibling::*/text()",
            "picture" to "//div[contains(@class, 'status-block')]/div[@itemprop='image']/@content",
            "relatedAnime" to "//table[contains(@class, 'entries-table')]//a/@href",
            "relatedAnimeDetails" to "//div[contains(@class, 'anime-detail-related-entries')]//a/@href",
            "synonyms" to "//h2[text()='Information']/following-sibling::*//tr[0]/td[1]",
            "score" to "//span[@itemprop='ratingValue']/span/text()",
            "studios" to "//td[text()='Studios']/following-sibling::td/a/text()",
            "producers" to "//td[text()='Producers']/following-sibling::td/a/text()",
        ))

        val picture = extractPicture(data)
        val title = extractTitle(data)

        return@withContext AnimeRaw(
            _title = title,
            episodes = extractEpisodes(data),
            type = extractType(data),
            picture = picture,
            thumbnail = findThumbnail(picture),
            status = extractStatus(data),
            duration = extractDuration(data),
            animeSeason = extractAnimeSeason(data),
            _sources = extractSourcesEntry(data),
            _synonyms = postProcessSynonyms(title, extractSynonyms(data)),
            _relatedAnime = extractRelatedAnime(data),
            _tags = extractTags(data),
            _studios = extractStudios(data),
            _producers = extractProducers(data),
        ).addScores(extractScore(data))
    }

    private fun postProcessSynonyms(title: String, synonyms: HashSet<String>): HashSet<String> {
        val processedSynonyms = hashSetOf<String>()

        when {
            !title.contains(';')  -> {
                processedSynonyms.addAll(
                    synonyms.flatMap { it.split("; ") }
                        .map { it.remove(";") }
                        .map { it.trim() }
                )
            }
            title.contains("""[^ ];[^ ]""".toRegex()) or title.endsWith(";") -> {
                processedSynonyms.addAll(
                    synonyms.flatMap { it.split("; ") }
                        .map { it.trim() }
                )
            }
            else -> processedSynonyms.addAll(synonyms.map { it.trim() })
        }

        return processedSynonyms
    }

    private fun extractTitle(data: ExtractionResult) = data.string("title")

    private fun extractEpisodes(data: ExtractionResult): Int {
        return if (data.notFound("episodes")) {
            0
        } else {
            val matchResult = """\d+""".toRegex().find(data.string("episodes"))
            return matchResult?.value?.toInt() ?: 0
        }
    }

    private fun extractType(data: ExtractionResult): AnimeType {
        return when(data.string("type").trim().lowercase()) {
            "tv" -> TV
            "unknown" -> UNKNOWN_TYPE
            "movie" -> MOVIE
            "ova" -> OVA
            "ona" -> ONA
            "special" -> SPECIAL
            "music" -> SPECIAL
            "pv" -> SPECIAL
            "cm" -> SPECIAL
            "tv special" -> SPECIAL
            else -> throw IllegalStateException("Unknown type [${data.string("type")}]")
        }
    }

    private fun extractPicture(data: ExtractionResult): URI {
        val text = data.string("picture").trim()

        return if (text in setOf("https://cdn.myanimelist.net/img/sp/icon/apple-touch-icon-256.png", "https://cdn.myanimelist.net/images/qm_50.gif")) {
            NO_PICTURE
        } else {
            URI(text)
        }
    }

    private fun findThumbnail(picture: URI): URI {
        return if (NO_PICTURE != picture) {
            URI(picture.toString().replace(".jpg", "t.jpg"))
        } else {
            NO_PICTURE_THUMBNAIL
        }
    }

    private fun extractSynonyms(data: ExtractionResult): HashSet<Title> {
        return if (data.notFound("synonyms")) {
            hashSetOf()
        } else {
            data.listNotNull<Title>("synonyms").toHashSet()
        }
    }

    private fun extractSourcesEntry(data: ExtractionResult): HashSet<URI> {
        val text = data.string("source")
        val matchResult = """/\d+/""".toRegex().find(text)
        val rawId = matchResult?.value ?: throw IllegalStateException("Unable to extract source")
        val id = rawId.trimStart('/').trimEnd('/')
        return hashSetOf(metaDataProviderConfig.buildAnimeLink(id))
    }

    private fun extractRelatedAnime(data: ExtractionResult): HashSet<URI> {
        val relatedAnimeDetails = if (data.notFound("relatedAnimeDetails")) {
            hashSetOf()
        } else {
            data.listNotNull<String>("relatedAnimeDetails")
                .filter { it.trim().startsWith(metaDataProviderConfig.buildAnimeLink(EMPTY).toString()) }
                .mapNotNull { """\d+""".toRegex().find(it)?.value }
                .map { metaDataProviderConfig.buildAnimeLink(it) }
                .toHashSet()
        }

        val relatedAnime = if (data.notFound("relatedAnime")) {
            hashSetOf()
        } else {
            data.listNotNull<String>("relatedAnime")
                .filter { it.trim().startsWith(metaDataProviderConfig.buildAnimeLink(EMPTY).toString()) }
                .mapNotNull { """\d+""".toRegex().find(it)?.value }
                .map { metaDataProviderConfig.buildAnimeLink(it) }
                .toHashSet()
        }

        return relatedAnime.union(relatedAnimeDetails).toHashSet()
    }

    private fun extractStatus(data: ExtractionResult): AnimeStatus {
        return when(data.string("status").trim().lowercase()) {
            "finished airing" -> FINISHED
            "currently airing" -> ONGOING
            "not yet aired" -> UPCOMING
            else -> throw IllegalStateException("Unknown status [${data.string("status")}]")
        }
    }

    private fun extractDuration(data: ExtractionResult): Duration {
        if (data.notFound("duration")) {
            return UNKNOWN_DURATION
        }

        val text = data.string("duration").trim()

        val values = """\d+""".toRegex().findAll(text).toList().map { it.value }
        val units = """(hr|min|sec)""".toRegex().findAll(text)
            .toList()
            .map { it.value }
            .map { it.trim() }
            .map { it.lowercase() }

        if (values.count() != units.count()) {
            log.warn { "The amount of values [${values.count()}] does not match the amount of units [${units.count()}]." }
            return Duration(0, SECONDS)
        }

        val valueUnitPairs = mutableListOf<Pair<Int, String>>()

        for (index in values.indices) {
            valueUnitPairs.add(Pair(values[index].toInt(), units[index]))
        }

        var durationInSeconds = 0

        valueUnitPairs.forEach {
            durationInSeconds += when (it.second) {
                "sec" -> it.first
                "min" -> it.first * 60
                "hr" -> it.first * 3600
                else -> throw IllegalStateException("[${it.second} is an unknown unit.")
            }
        }

        return Duration(durationInSeconds, SECONDS)
    }

    private fun extractAnimeSeason(data: ExtractionResult): AnimeSeason {
        val premiered = data.string("premiered").trim()
        val aired = data.string("aired").trim()

        val seasonText = """[aA-zZ]+""".toRegex().find(premiered)?.value ?: EMPTY
        var season =  Season.of(seasonText)
        if (season == Season.UNDEFINED) {
            season = when("""[aA-zZ]+""".toRegex().find(aired)?.value?.lowercase() ?: EMPTY) {
                "jan", "feb", "mar" -> Season.WINTER
                "apr", "may", "jun" -> Season.SPRING
                "jul", "aug", "sep" -> Season.SUMMER
                "oct", "nov", "dec" -> Season.FALL
                else -> Season.UNDEFINED
            }
        }

        val yearPremiered = """\d{4}""".toRegex().find(premiered)?.value?.toInt() ?: 0
        val year = if (yearPremiered != 0) {
            yearPremiered
        } else {
            """\d{4}""".toRegex().findAll(aired).firstOrNull()?.value?.toInt() ?: 0
        }

        return AnimeSeason(
            season = season,
            year = year
        )
    }

    private fun extractTags(data: ExtractionResult): HashSet<Tag> {
        return if (data.notFound("tags")) {
            hashSetOf()
        } else {
            data.listNotNull<Tag>("tags")
                .filterNot { it == data.string("title") }
                .toHashSet()
        }
    }

    private fun extractScore(data: ExtractionResult): MetaDataProviderScore {
        if (data.notFound("score")) {
            return NoMetaDataProviderScore
        }

        val rawScore = data.double("score")

        return MetaDataProviderScoreValue(
            hostname = metaDataProviderConfig.hostname(),
            value = rawScore,
            range = 1.0..10.0,
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
        private val log by LoggerDelegate()

        /**
         * Singleton of [MyanimelistAnimeConverter]
         * @since 6.1.0
         */
        public val instance: MyanimelistAnimeConverter by lazy { MyanimelistAnimeConverter() }
    }
}