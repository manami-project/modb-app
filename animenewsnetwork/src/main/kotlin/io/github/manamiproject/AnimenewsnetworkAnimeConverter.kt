package io.github.manamiproject

import io.github.manamiproject.modb.core.anime.*
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE_THUMBNAIL
import io.github.manamiproject.modb.core.anime.AnimeSeason.Companion.UNKNOWN_YEAR
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.HOURS
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.normalize
import io.github.manamiproject.modb.core.extensions.remove
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.ExtractionResult
import io.github.manamiproject.modb.core.extractor.XmlDataExtractor
import kotlinx.coroutines.withContext
import java.net.URI
import java.time.Clock
import java.time.LocalDate
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN as UNKNOWN_STATUS
import io.github.manamiproject.modb.core.anime.AnimeType.UNKNOWN as UNKNOWN_TYPE
import io.github.manamiproject.modb.core.anime.Duration.Companion.UNKNOWN as UNKNOWN_DURATION

/**
 * Converts raw data to an [AnimeRaw].
 * Requires raw HTML from the mobile site version.
 * @since 1.0.0
 * @property metaDataProviderConfig Configuration for converting data.
 * @property extractor Extracts specific data from raw content.
 */
public class AnimenewsnetworkAnimeConverter(
    private val metaDataProviderConfig: MetaDataProviderConfig = AnimenewsnetworkConfig,
    private val extractor: DataExtractor = XmlDataExtractor,
    private val clock: Clock = Clock.systemUTC(),
): AnimeConverter {

    override suspend fun convert(rawContent: String): AnimeRaw = withContext(LIMITED_CPU) {
        val data = extractor.extract(rawContent, mapOf(
            "source" to "//link[@rel='canonical']/@href",
            "title" to "//h1[@id='page_header']/text()",
            "numberOfEpisodes" to "//strong[contains(text(), 'Number of episodes:')]/following-sibling::span/text()",
            "episodeTitles" to "//div[@id='infotype-25']//a/text()",
            "score" to "//b[contains(text(), 'Arithmetic mean:')]/..",
            "picture" to "//div[@id='infotype-19']//img/@src",
            "year" to "//div[@id='infotype-7']//span/text()",
            "year_list_first" to "//div[@id='infotype-7']//div[1]",
            "synonyms" to "//div[@id='infotype-2']//div",
            "genres" to "//div[@id='infotype-30']//a/text()",
            "themes" to "//div[@id='infotype-31']//a/text()",
            "relatedAnime" to "//div[@id='infotype-related']//a/@href",
            "duration" to "//div[@id='infotype-4']//span/text()",
            "year_list_all" to "//div[@id='infotype-7']//div//text()",
        ))

        val japaneseCompanies = rawContent.substringAfter("<nobr>Japanese companies</nobr>")
            .substringBefore("<nobr>")

        val productionData = extractor.extract(japaneseCompanies, mapOf(
            "studios" to "//b[text()='Animation Production']/following-sibling::a/text()",
            "producers" to "//b[text()='Production']/following-sibling::a/text()",
        ))

        val extractedTitle = extractTitle(data)

        return@withContext AnimeRaw(
            _title = extractedTitle.first,
            _sources = extractSourcesEntry(data),
            episodes = extractEpisodes(data),
            type = extractType(data),
            status = extractStatus(data),
            picture = extractPicture(data),
            thumbnail = extractThumbnail(data),
            animeSeason = extractAnimeSeason(data),
            duration = extractDuration(data),
            _synonyms = extractSynonyms(data),
            _relatedAnime = extractRelatedAnime(data),
            _tags = extractTags(data),
            _studios = extractStudios(productionData),
            _producers = extractProducers(productionData),
        ).addScores(extractScore(data))
    }

    private fun extractSourcesEntry(data: ExtractionResult): HashSet<URI> {
        return hashSetOf(URI(data.string("source").remove("www.")))
    }

    private fun extractTitle(data: ExtractionResult): Pair<Title, Title> {
        val rawTitle = data.string("title").normalize()

        return when {
            rawTitle.endsWith(")") -> rawTitle.substringBeforeLast('(') to rawTitle
            else -> rawTitle.substringBeforeLast('(') to EMPTY
        }
    }

    private fun extractEpisodes(data: ExtractionResult): Episodes {
        var episodes = data.intOrDefault("numberOfEpisodes")

        if (episodes == 0 && !data.notFound("episodeTitles")) {
            episodes = data.string("episodeTitles").remove("We have ").toIntOrNull() ?: 0
        }

        if (episodes == 0 && data.stringOrDefault("title").lowercase().contains("movie")) {
            episodes = 1
        }

        return episodes
    }

    private fun extractType(data: ExtractionResult): AnimeType {
        val title = data.stringOrDefault("title").lowercase()
        val type = title.substringAfterLast('(')

        return when {
            type.contains("tv") -> TV
            type.contains("movie") -> MOVIE
            type.contains("oav") -> OVA
            type.contains("ona") -> ONA
            type.contains("special") -> SPECIAL
            else -> when {
                title.contains("motion picture") -> MOVIE
                title.contains("ova") -> OVA
                else -> UNKNOWN_TYPE
            }
        }
    }

    private fun extractScore(data: ExtractionResult): MetaDataProviderScore {
        if (data.notFound("score")) {
            return NoMetaDataProviderScore
        }

        val rawScore = data.listNotNull<String>("score")
            .first { it.contains("std. dev.") }
            .substringBefore('(')
            .normalize()

        return MetaDataProviderScoreValue(
            hostname = metaDataProviderConfig.hostname(),
            value = rawScore.toDoubleOrNull() ?: 0.0,
            range = 1.0..10.0,
        )
    }

    private fun extractThumbnail(data: ExtractionResult): URI {
        if (data.notFound("picture")) {
            return NO_PICTURE_THUMBNAIL
        }

        val link = data.listNotNull<String>("picture").first()
        val id = link.substringAfterLast("encyc/").normalize()
        return URI("https://cdn.${metaDataProviderConfig.hostname()}/thumbnails/fit200x200/encyc/$id")
    }

    private fun extractPicture(data: ExtractionResult): URI {
        if (data.notFound("picture")) {
            return NO_PICTURE
        }

        val link = data.listNotNull<String>("picture").first()
        val id = link.substringAfterLast("encyc/").normalize()
        return URI("https://cdn.${metaDataProviderConfig.hostname()}/thumbnails/max500x600/encyc/$id")
    }

    private fun extractAnimeSeason(data: ExtractionResult): AnimeSeason {
        val yearExists = !data.notFound("year")
        val yearListExists = !data.notFound("year_list_first")

        if (!yearExists && !yearListExists) {
            return unknown_season
        }

        // 1 Extraction from year
        var matchResult = yearMonthRegex.matchEntire(data.string("year").substringBefore("to").normalize())

        if ((matchResult == null || matchResult.groups.isEmpty()) && !yearListExists) {
            return unknown_season
        }

        var year = (matchResult?.groups?.get("year")?.value ?: EMPTY).toIntOrNull() ?: UNKNOWN_YEAR
        var month = matchResult?.groups?.get("month")?.value?.toIntOrNull() ?: 0

        // 2 Unable to extract year and no other options left
        if (year == UNKNOWN_YEAR && !yearListExists) {
            return unknown_season
        }

        // 3 Extract from yearList
        if (year == UNKNOWN_YEAR) {
            matchResult = yearMonthRegex.matchEntire(data.string("year_list_first").substringBefore("to").normalize())

            if (matchResult == null || matchResult.groups.isEmpty()) {
                return unknown_season
            }

            year = (matchResult.groups["year"]?.value ?: EMPTY).toIntOrNull() ?: UNKNOWN_YEAR
            month = matchResult.groups["month"]?.value?.toIntOrNull() ?: 0
        }

        val season = when(month) {
            1, 2, 3 -> WINTER
            4, 5, 6 -> SPRING
            7, 8, 9 -> SUMMER
            10, 11, 12 -> FALL
            else -> UNDEFINED
        }

        return AnimeSeason(
            year = year,
            season = season,
        )
    }

    private fun extractSynonyms(data: ExtractionResult): HashSet<Title> {
        if (data.notFound("synonyms")) {
            return hashSetOf()
        }

        return data.listNotNull<Title>("synonyms")
            .map { it.normalize() }
            .map { title ->
                val endsWithBracket = title.endsWith(')')

                if (endsWithBracket) {
                    var closingBrackets = 1
                    var openingBracketSeen = 0
                    var rangeEnd = title.length - 1

                    for (i in title.length - 2 downTo 0 step 1) {
                        when {
                            title[i] == ')' -> {
                                closingBrackets++
                                rangeEnd = i
                            }
                            title[i] == '(' && openingBracketSeen != closingBrackets -> {
                                openingBracketSeen++
                                rangeEnd = i
                            }
                            openingBracketSeen != closingBrackets -> rangeEnd = i
                            openingBracketSeen == closingBrackets && title[i] == ' ' -> rangeEnd = i
                            openingBracketSeen == closingBrackets && title[i] !in new_language_lock_identifiers -> break
                        }
                    }

                    title.substring(0, rangeEnd)
                } else {
                    title
                }
            }
            .toHashSet()
    }

    private fun extractTags(data: ExtractionResult): HashSet<Tag> {
        val genres = if (data.notFound("genres")) {
            hashSetOf()
        } else {
            data.listNotNull<Tag>("genres")
        }

        val themes = if (data.notFound("themes")) {
            hashSetOf()
        } else {
            data.listNotNull<Tag>("themes")
        }

        return genres.union(themes).toHashSet()
    }

    private fun extractRelatedAnime(data: ExtractionResult): HashSet<URI> {
        val entries = if (data.notFound("relatedAnime")) {
            hashSetOf()
        } else {
            data.listNotNull<String>("relatedAnime")
        }

        return entries.filter { it.contains("anime.php") }
            .map { it.substringAfterLast('=') }
            .map { metaDataProviderConfig.buildAnimeLink(it) }
            .toHashSet()
    }

    private fun extractDuration(data: ExtractionResult): Duration {
        if (data.notFound("duration")) {
            return UNKNOWN_DURATION
        }

        val value = data.string("duration").normalize().lowercase()

        if (durationMinutesRegex.matches(value)) {
            val minutes = durationMinutesRegex.matchEntire(value)
                ?.groups
                ?.get("minutes")
                ?.value
                ?.toIntOrNull()
                ?: 0

            return Duration(
                value = minutes,
                unit = MINUTES,
            )
        }

        if (durationHalfHourTextRegex.matches(value)) {
            return Duration(
                value = 30,
                unit = MINUTES,
            )
        }

        if (durationOneHourTextRegex.matches(value)) {
            return Duration(
                value = 1,
                unit = HOURS,
            )
        }

        throw IllegalStateException("Unknown value [$value] for duration.")
    }

    private fun extractStatus(data: ExtractionResult): AnimeStatus {
        val yearExists = !data.notFound("year")
        val yearListAllExists = !data.notFound("year_list_all")

        if (!yearExists && !yearListAllExists) {
            return UNKNOWN_STATUS
        }

        val year = data.string("year").normalize()
        val yearListAll = data.listNotNull<String>("year_list_all").map { it.normalize() }

        val value = when {
            yearMonthDayRegex.containsMatchIn(year) -> year
            else -> {
                yearListAll.filterNot { it.startsWith("Vintage:") }.firstOrNull { it.contains(" to ") } ?: yearListAll.firstOrNull() ?: EMPTY
            }
        }

        val splitDates = value.split(" to ").map { it.normalize() }.filter { str -> setOf(yearMonthRegex, yearMonthDayRegex).any { it.matches(str) } }
        val now = LocalDate.now(clock)

        // Single date
        if (splitDates.size == 1) {
            val endYear = yearMonthDayRegex.matchEntire(splitDates.first())?.groups?.get("year")?.value?.toIntOrNull() ?: 0
            val endMonth = yearMonthDayRegex.matchEntire(splitDates.first())?.groups?.get("month")?.value?.toIntOrNull() ?: 1
            val endDay = yearMonthDayRegex.matchEntire(splitDates.first())?.groups?.get("day")?.value?.toIntOrNull() ?: 1

            if (endYear == 0) {
                return UNKNOWN_STATUS
            }

            val endDate = LocalDate.of(endYear, endMonth, endDay)

            return when {
                now.isBefore(endDate) -> UPCOMING
                now.isAfter(endDate) -> FINISHED
                else -> ONGOING
            }
        }

        val startYear = yearMonthDayRegex.matchEntire(splitDates.first())?.groups?.get("year")?.value?.toIntOrNull() ?: throw IllegalStateException("Unable to identify startYear.")
        val startMonth = yearMonthDayRegex.matchEntire(splitDates.first())?.groups?.get("month")?.value?.toIntOrNull() ?: 1
        val startDay = yearMonthDayRegex.matchEntire(splitDates.first())?.groups?.get("day")?.value?.toIntOrNull() ?: 1
        val startDate = LocalDate.of(startYear, startMonth, startDay)

        val endYear = yearMonthDayRegex.matchEntire(splitDates.last())?.groups?.get("year")?.value?.toIntOrNull() ?: throw IllegalStateException("Unable to identify endYear.")
        val endMonth = yearMonthDayRegex.matchEntire(splitDates.last())?.groups?.get("month")?.value?.toIntOrNull() ?: 1
        val endDay = yearMonthDayRegex.matchEntire(splitDates.last())?.groups?.get("day")?.value?.toIntOrNull() ?: 1
        val endDate = LocalDate.of(endYear, endMonth, endDay)

        return when {
            now.isBefore(startDate) -> UPCOMING
            now.isAfter(endDate) -> FINISHED
            (now.isEqual(startDate) || now.isAfter(startDate)) && (now.isBefore(endDate) || now.isEqual(endDate)) -> ONGOING
            else -> UNKNOWN_STATUS
        }
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
        /**
         * Singleton of [AnimenewsnetworkAnimeConverter]
         * @since 6.1.0
         */
        public val instance: AnimenewsnetworkAnimeConverter by lazy { AnimenewsnetworkAnimeConverter() }

        private val unknown_season = AnimeSeason(
            year = UNKNOWN_YEAR,
            season = UNDEFINED,
        )

        private val yearMonthRegex = """.*?(?<year>\d{4})(-(?<month>\d{2}))?.*?""".toRegex()
        private val yearMonthDayRegex = """.*?(?<year>\d{4})(-(?<month>\d{2}))?(-(?<day>\d{2}))?.*""".toRegex()
        private val new_language_lock_identifiers = hashSetOf(' ', ')')
        private val durationMinutesRegex = """^.*?(?<minutes>\d+) minutes ?.*?$""".toRegex()
        private val durationHalfHourTextRegex = """^.*?half hour ?.*?$""".toRegex()
        private val durationOneHourTextRegex = """^.*?one hour ?.*?$""".toRegex()
    }
}