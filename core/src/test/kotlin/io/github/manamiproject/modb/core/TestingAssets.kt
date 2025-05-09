package io.github.manamiproject.modb.core

import io.github.manamiproject.modb.core.anime.*
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.*
import io.github.manamiproject.modb.core.config.*
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.ExtractionResult
import io.github.manamiproject.modb.core.extractor.OutputKey
import io.github.manamiproject.modb.core.extractor.Selector
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import kotlin.reflect.*

internal object TestMetaDataProviderConfig : MetaDataProviderConfig {
    override fun isTestContext(): Boolean = true
    override fun hostname(): Hostname = shouldNotBeInvoked()
    override fun buildAnimeLink(id: AnimeId): URI = shouldNotBeInvoked()
    override fun buildDataDownloadLink(id: String): URI = shouldNotBeInvoked()
    override fun fileSuffix(): FileSuffix = shouldNotBeInvoked()
}

internal object TestAnimeConverter : AnimeConverter {
    override suspend fun convert(rawContent: String) = shouldNotBeInvoked()
}

internal object TestDataExtractor : DataExtractor {
    override suspend fun extract(rawContent: String, selection: Map<OutputKey, Selector>): ExtractionResult = shouldNotBeInvoked()
}

internal object TestConfigRegistry: ConfigRegistry {
    override fun string(key: String): String = shouldNotBeInvoked()
    override fun long(key: String): Long = shouldNotBeInvoked()
    override fun int(key: String): Int = shouldNotBeInvoked()
    override fun <T: Any> list(key: String): List<T> = shouldNotBeInvoked()
    override fun boolean(key: String): Boolean = shouldNotBeInvoked()
    override fun double(key: String): Double = shouldNotBeInvoked()
    override fun localDate(key: String): LocalDate = shouldNotBeInvoked()
    override fun localDateTime(key: String): LocalDateTime = shouldNotBeInvoked()
    override fun offsetDateTime(key: String): OffsetDateTime = shouldNotBeInvoked()
    override fun <T: Any> map(key: String): Map<String, T> = shouldNotBeInvoked()
}

internal class TestKProperty<T>: KProperty<T> {
    override val annotations: List<Annotation>
        get() = shouldNotBeInvoked()
    override val getter: KProperty.Getter<T>
        get() = shouldNotBeInvoked()
    override val isAbstract: Boolean
        get() = shouldNotBeInvoked()
    override val isConst: Boolean
        get() = shouldNotBeInvoked()
    override val isFinal: Boolean
        get() = shouldNotBeInvoked()
    override val isLateinit: Boolean
        get() = shouldNotBeInvoked()
    override val isOpen: Boolean
        get() = shouldNotBeInvoked()
    override val isSuspend: Boolean
        get() = shouldNotBeInvoked()
    override val name: String
        get() = shouldNotBeInvoked()
    override val parameters: List<KParameter>
        get() = shouldNotBeInvoked()
    override val returnType: KType
        get() = shouldNotBeInvoked()
    override val typeParameters: List<KTypeParameter>
        get() = shouldNotBeInvoked()
    override val visibility: KVisibility
        get() = shouldNotBeInvoked()
    override fun call(vararg args: Any?): T = shouldNotBeInvoked()
    override fun callBy(args: Map<KParameter, Any?>): T = shouldNotBeInvoked()
}

internal object TestScoreCalculator: ScoreCalculator {
    override fun calculateScore(scores: Collection<MetaDataProviderScore>): Score = shouldNotBeInvoked()
}

object TestAnimeRawObjects {

    val defaultTv = AnimeRaw(
        _title = "Death Note",
        _sources = hashSetOf(
            URI("https://myanimelist.net/anime/1535"),
        ),
        type = TV,
        episodes = 37,
        status = FINISHED,
        animeSeason = AnimeSeason(
            season = FALL,
            year = 2006,
        ),
        picture = URI("https://cdn.myanimelist.net/images/anime/1079/138100.jpg"),
        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1079/138100t.jpg"),
        duration = Duration(
            value = 23,
            unit = MINUTES,
        ),
        _synonyms = hashSetOf(
            "DN",
            "デスノート",
        ),
        _relatedAnime = hashSetOf(
            URI("https://myanimelist.net/anime/2994"),
        ),
        _tags = hashSetOf(
            "psychological",
            "shounen",
            "supernatural",
            "suspense",
        ),
    ).apply {
        addScores(
            MetaDataProviderScoreValue(
                hostname = "myanimelist.net",
                value = 8.62,
                range = 1.0..10.0,
            ),
        )
    }

    val specialWithMultipleEpisodes = AnimeRaw(
        _title = "5-toubun no Hanayome*",
        _sources = hashSetOf(
            URI("https://myanimelist.net/anime/58755"),
        ),
        type = SPECIAL,
        episodes = 2,
        status = FINISHED,
        animeSeason = AnimeSeason(
            season = FALL,
            year = 2024,
        ),
        picture = URI("https://cdn.myanimelist.net/images/anime/1915/145336.jpg"),
        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1915/145336t.jpg"),
        duration = Duration(
            value = 24,
            unit = MINUTES,
        ),
        _synonyms = hashSetOf(
            "The Quintessential Quintuplets*",
            "五等分の花嫁*",
        ),
        _relatedAnime = hashSetOf(
            URI("https://myanimelist.net/anime/48548"),
        ),
        _tags = hashSetOf(
            "comedy",
            "harem",
            "romance",
            "school",
            "shounen",
        ),
    ).apply {
        addScores(
            MetaDataProviderScoreValue(
                hostname = "myanimelist.net",
                value = 7.44,
                range = 1.0..10.0,
            ),
        )
    }

    val specialWithMultipleEpisodesFullyMerged = AnimeRaw(
        _title = "5-toubun no Hanayome*",
        _sources = hashSetOf(
            URI("https://anidb.net/anime/18603"),
            URI("https://anilist.co/anime/177191"),
            URI("https://anime-planet.com/anime/the-quintessential-quintuplets-specials-2"),
            URI("https://anisearch.com/anime/19242"),
            URI("https://kitsu.app/anime/48807"),
            URI("https://livechart.me/anime/12646"),
            URI("https://myanimelist.net/anime/58755"),
            URI("https://notify.moe/anime/Gk-oD9uIR"),
            URI("https://simkl.com/anime/2448488"),
        ),
        type = SPECIAL,
        episodes = 2,
        status = FINISHED,
        animeSeason = AnimeSeason(
            season = FALL,
            year = 2024,
        ),
        picture = URI("https://cdn.myanimelist.net/images/anime/1915/145336.jpg"),
        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1915/145336t.jpg"),
        duration = Duration(
            value = 24,
            unit = MINUTES,
        ),
        _synonyms = hashSetOf(
            "5-toubun no Hanayome *",
            "Go-Toubun no Hanayome *",
            "Go-Tōbun no Hanayome *",
            "Go-toubun no Hanayome *",
            "The Quintessential Quintuplets *",
            "The Quintessential Quintuplets Movie*",
            "The Quintessential Quintuplets Special 2",
            "The Quintessential Quintuplets Specials 2",
            "The Quintessential Quintuplets*",
            "The Quintessential Quintuplets: Honeymoon Arc",
            "五等分の花嫁*",
            "五等分の花嫁＊",
            "五等分的新娘＊",
        ),
        _relatedAnime = hashSetOf(
            URI("https://anidb.net/anime/16165"),
            URI("https://anilist.co/anime/131520"),
            URI("https://anime-planet.com/anime/the-quintessential-quintuplets-movie"),
            URI("https://animecountdown.com/1577789"),
            URI("https://anisearch.com/anime/16091"),
            URI("https://kitsu.app/anime/44229"),
            URI("https://livechart.me/anime/10488"),
            URI("https://livechart.me/anime/11921"),
            URI("https://livechart.me/anime/3448"),
            URI("https://livechart.me/anime/9428"),
            URI("https://myanimelist.net/anime/48548"),
            URI("https://notify.moe/anime/e7lfM8QMg"),
            URI("https://simkl.com/anime/1577789"),
        ),
        _tags = hashSetOf(
            "america",
            "based on a manga",
            "comedy",
            "drama",
            "ensemble cast",
            "female harem",
            "harem",
            "heterosexual",
            "japanese production",
            "language barrier",
            "male protagonist",
            "marriage",
            "new",
            "predominantly female cast",
            "present",
            "primarily female cast",
            "romance",
            "school",
            "sequel",
            "shounen",
            "siblings",
            "time",
            "twins",
        ),
    ).apply {
        addScores(
            MetaDataProviderScoreValue(
                hostname = "anidb.net",
                value = 6.63,
                range = 1.0..10.0,
            ),
            MetaDataProviderScoreValue(
                hostname = "anilist.co",
                value = 75.0,
                range = 1.0..100.0,
            ),
            MetaDataProviderScoreValue(
                hostname = "anime-planet.com",
                value = 3.845,
                range = 0.5..5.0,
            ),
            MetaDataProviderScoreValue(
                hostname = "anisearch.com",
                value = 3.9,
                range = 0.1..5.0,
            ),
            MetaDataProviderScoreValue(
                hostname = "kitsu.app",
                value = 76.95,
                range = 1.0..100.0,
            ),
            MetaDataProviderScoreValue(
                hostname = "livechart.me",
                value = 8.35,
                range = 1.0..10.0,
            ),
            MetaDataProviderScoreValue(
                hostname = "myanimelist.net",
                value = 8.62,
                range = 1.0..10.0,
            ),
            MetaDataProviderScoreValue(
                hostname = "notify.moe",
                value = 7.0,
                range = 1.0..10.0,
            ),
            MetaDataProviderScoreValue(
                hostname = "simkl.com",
                value = 7.9,
                range = 1.0..10.0,
            ),
        )
    }
}

object TestAnimeObjects {

    val defaultTv = Anime(
        title = "Death Note",
        sources = hashSetOf(
            URI("https://myanimelist.net/anime/1535"),
        ),
        type = TV,
        episodes = 37,
        status = FINISHED,
        animeSeason = AnimeSeason(
            season = FALL,
            year = 2006,
        ),
        picture = URI("https://cdn.myanimelist.net/images/anime/1079/138100.jpg"),
        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1079/138100t.jpg"),
        duration = Duration(
            value = 23,
            unit = MINUTES,
        ),
        synonyms = hashSetOf(
            "DN",
            "デスノート",
        ),
        relatedAnime = hashSetOf(
            URI("https://myanimelist.net/anime/2994"),
        ),
        tags = hashSetOf(
            "psychological",
            "shounen",
            "supernatural",
            "suspense",
        ),
        score = ScoreValue(
            arithmeticGeometricMean = 8.62,
            arithmeticMean = 8.62,
            median = 8.62,
        ),
    )

    val specialWithMultipleEpisodes = Anime(
        title = "5-toubun no Hanayome*",
        sources = hashSetOf(
            URI("https://myanimelist.net/anime/58755"),
        ),
        type = SPECIAL,
        episodes = 2,
        status = FINISHED,
        animeSeason = AnimeSeason(
            season = FALL,
            year = 2024,
        ),
        picture = URI("https://cdn.myanimelist.net/images/anime/1915/145336.jpg"),
        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1915/145336t.jpg"),
        duration = Duration(
            value = 24,
            unit = MINUTES,
        ),
        synonyms = hashSetOf(
            "The Quintessential Quintuplets*",
            "五等分の花嫁*",
        ),
        relatedAnime = hashSetOf(
            URI("https://myanimelist.net/anime/48548"),
        ),
        tags = hashSetOf(
            "comedy",
            "harem",
            "romance",
            "school",
            "shounen",
        ),
        score = ScoreValue(
            arithmeticGeometricMean = 7.44,
            arithmeticMean = 7.44,
            median = 7.44,
        )
    )

    val specialWithMultipleEpisodesFullyMerged = Anime(
        title = "5-toubun no Hanayome*",
        sources = hashSetOf(
            URI("https://anidb.net/anime/18603"),
            URI("https://anilist.co/anime/177191"),
            URI("https://anime-planet.com/anime/the-quintessential-quintuplets-specials-2"),
            URI("https://anisearch.com/anime/19242"),
            URI("https://kitsu.app/anime/48807"),
            URI("https://livechart.me/anime/12646"),
            URI("https://myanimelist.net/anime/58755"),
            URI("https://notify.moe/anime/Gk-oD9uIR"),
            URI("https://simkl.com/anime/2448488"),
        ),
        type = SPECIAL,
        episodes = 2,
        status = FINISHED,
        animeSeason = AnimeSeason(
            season = FALL,
            year = 2024,
        ),
        picture = URI("https://cdn.myanimelist.net/images/anime/1915/145336.jpg"),
        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1915/145336t.jpg"),
        duration = Duration(
            value = 24,
            unit = MINUTES,
        ),
        synonyms = hashSetOf(
            "5-toubun no Hanayome *",
            "Go-Toubun no Hanayome *",
            "Go-Tōbun no Hanayome *",
            "Go-toubun no Hanayome *",
            "The Quintessential Quintuplets *",
            "The Quintessential Quintuplets Movie*",
            "The Quintessential Quintuplets Special 2",
            "The Quintessential Quintuplets Specials 2",
            "The Quintessential Quintuplets*",
            "The Quintessential Quintuplets: Honeymoon Arc",
            "五等分の花嫁*",
            "五等分の花嫁＊",
            "五等分的新娘＊",
        ),
        relatedAnime = hashSetOf(
            URI("https://anidb.net/anime/16165"),
            URI("https://anilist.co/anime/131520"),
            URI("https://anime-planet.com/anime/the-quintessential-quintuplets-movie"),
            URI("https://animecountdown.com/1577789"),
            URI("https://anisearch.com/anime/16091"),
            URI("https://kitsu.app/anime/44229"),
            URI("https://livechart.me/anime/10488"),
            URI("https://livechart.me/anime/11921"),
            URI("https://livechart.me/anime/3448"),
            URI("https://livechart.me/anime/9428"),
            URI("https://myanimelist.net/anime/48548"),
            URI("https://notify.moe/anime/e7lfM8QMg"),
            URI("https://simkl.com/anime/1577789"),
        ),
        tags = hashSetOf(
            "america",
            "based on a manga",
            "comedy",
            "drama",
            "ensemble cast",
            "female harem",
            "harem",
            "heterosexual",
            "japanese production",
            "language barrier",
            "male protagonist",
            "marriage",
            "new",
            "predominantly female cast",
            "present",
            "primarily female cast",
            "romance",
            "school",
            "sequel",
            "shounen",
            "siblings",
            "time",
            "twins",
        ),
        score = ScoreValue(
            arithmeticGeometricMean = 7.616175305477198,
            arithmeticMean = 7.624601113172541,
            median = 7.7272727272727275,
        ),
    )
}