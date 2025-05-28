package io.github.manamiproject

import io.github.manamiproject.modb.core.coroutines.CoroutineManager.runCoroutine
import io.github.manamiproject.modb.core.extensions.fileSuffix
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.random
import io.github.manamiproject.modb.test.testResource
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isRegularFile
import kotlin.test.Test

private val files = mapOf(
    "AnimenewsnetworkConverterTest/sources/6592.html" to "16498",

    "AnimenewsnetworkConverterTest/title/special_chars.html" to "16498",
    "AnimenewsnetworkConverterTest/title/title_with_type.html" to "182",
    "AnimenewsnetworkConverterTest/title/title_with_type_and_number.html" to "15693",

    "AnimenewsnetworkConverterTest/episodes/12.html" to "386",
    "AnimenewsnetworkConverterTest/episodes/no_episodes.html" to "29052",
    "AnimenewsnetworkConverterTest/episodes/no_episodes_but_movie_in_title.html" to "34100",
    "AnimenewsnetworkConverterTest/episodes/number_of_episodes_missing_use_episodes_titles_instead.html" to "26737",

    "AnimenewsnetworkConverterTest/type/motion-picture-in-title.html" to "6074",
    "AnimenewsnetworkConverterTest/type/movie.html" to "19256",
    "AnimenewsnetworkConverterTest/type/ona.html" to "26846",
    "AnimenewsnetworkConverterTest/type/ova-in-title.html" to "10352",
    "AnimenewsnetworkConverterTest/type/ova.html" to "23717",
    "AnimenewsnetworkConverterTest/type/special.html" to "7921",
    "AnimenewsnetworkConverterTest/type/tv.html" to "1375",

    "AnimenewsnetworkConverterTest/score/no-score.html" to "22002",
    "AnimenewsnetworkConverterTest/score/score.html" to "6074",

    "AnimenewsnetworkConverterTest/picture_and_thumbnail/neither_picture_nor_thumbnail.html" to "4874",
    "AnimenewsnetworkConverterTest/picture_and_thumbnail/picture_and_thumbnail_available.html" to "6074",

    "AnimenewsnetworkConverterTest/anime_season/year/vintage-missing.html" to "32556",
    "AnimenewsnetworkConverterTest/anime_season/year/vintage-multiple-entries-range.html" to "1024",
    "AnimenewsnetworkConverterTest/anime_season/year/vintage-multiple-entries-single-date.html" to "19743",
    "AnimenewsnetworkConverterTest/anime_season/year/vintage-single-date-range.html" to "4359",
    "AnimenewsnetworkConverterTest/anime_season/year/vintage-year-followed-by-text.html" to "14289",
    "AnimenewsnetworkConverterTest/anime_season/year/vintage-year-month-day.html" to "26334",
    "AnimenewsnetworkConverterTest/anime_season/year/vintage-year.html" to "1725",

    "AnimenewsnetworkConverterTest/anime_season/season/01.html" to "26145",
    "AnimenewsnetworkConverterTest/anime_season/season/02.html" to "6263",
    "AnimenewsnetworkConverterTest/anime_season/season/03.html" to "19256",
    "AnimenewsnetworkConverterTest/anime_season/season/04.html" to "340",
    "AnimenewsnetworkConverterTest/anime_season/season/05.html" to "23717",
    "AnimenewsnetworkConverterTest/anime_season/season/06.html" to "25440",
    "AnimenewsnetworkConverterTest/anime_season/season/07.html" to "18447",
    "AnimenewsnetworkConverterTest/anime_season/season/08.html" to "8504",
    "AnimenewsnetworkConverterTest/anime_season/season/09.html" to "23347",
    "AnimenewsnetworkConverterTest/anime_season/season/10.html" to "1375",
    "AnimenewsnetworkConverterTest/anime_season/season/11.html" to "17618",
    "AnimenewsnetworkConverterTest/anime_season/season/12.html" to "22940",
    "AnimenewsnetworkConverterTest/anime_season/season/undefined.html" to "32556",

    "AnimenewsnetworkConverterTest/synonyms/multiple-meta-info-brackets.html" to "439",
    "AnimenewsnetworkConverterTest/synonyms/multiple.html" to "9701",
    "AnimenewsnetworkConverterTest/synonyms/nestesd-brackets.html" to "340",
    "AnimenewsnetworkConverterTest/synonyms/no-synonyms.html" to "30584",
    "AnimenewsnetworkConverterTest/synonyms/synonym-with-brackets-in-title-with-language-info.html" to "8421",
    "AnimenewsnetworkConverterTest/synonyms/synonym-with-brackets-in-title-without-language-info.html" to "26407",

    "AnimenewsnetworkConverterTest/tags/multiple-genres-and-multiple-themes.html" to "18171",
    "AnimenewsnetworkConverterTest/tags/neither-genres-nor-themes.html" to "30584",

    "AnimenewsnetworkConverterTest/related_anime/manga-in-related-anime.html" to "22934",
    "AnimenewsnetworkConverterTest/related_anime/no-related-anime-but-relation-in-brackets.html" to "9701",
    "AnimenewsnetworkConverterTest/related_anime/no-related-anime-no-relation-in-brackets-but-an-adaption.html" to "11199",
    "AnimenewsnetworkConverterTest/related_anime/related-anime-and-relation-in-brackets.html" to "10216",
    "AnimenewsnetworkConverterTest/related_anime/related-anime.html" to "26334",

    "AnimenewsnetworkConverterTest/duration/24-minutes-per-episode.html" to "11827",
    "AnimenewsnetworkConverterTest/duration/80-minutes.html" to "1725",
    "AnimenewsnetworkConverterTest/duration/half-hour-per-episode.html" to "20285",
    "AnimenewsnetworkConverterTest/duration/half-hour.html" to "3234",
    "AnimenewsnetworkConverterTest/duration/numeric-with-error.html" to "7389",
    "AnimenewsnetworkConverterTest/duration/one-hour-per-episode.html" to "3932",
    "AnimenewsnetworkConverterTest/duration/one-hour.html" to "10716",
    "AnimenewsnetworkConverterTest/duration/text-with-error.html" to "1920",

    "AnimenewsnetworkConverterTest/status/list-range.html" to "4",
    "AnimenewsnetworkConverterTest/status/vintage-range.html" to "7279",
    "AnimenewsnetworkConverterTest/status/vintage-year-month-day.html" to "7171",
    "AnimenewsnetworkConverterTest/status/vintage-year-month.html" to "22540",
    "AnimenewsnetworkConverterTest/status/vintage-year.html" to "301",
    "AnimenewsnetworkConverterTest/status/vintage-year-month-special-case.html" to "31510",
)

internal fun main(): Unit = runCoroutine {
    files.forEach { (file, animeId) ->
        AnimenewsnetworkDownloader.instance.download(animeId).writeToFile(resourceFile(file))
        delay(random(5000, 10000))
    }

    println("Done")
}

private fun resourceFile(file: String): Path {
    return Paths.get(
        testResource(file).toAbsolutePath()
            .toString()
            .replace("/build/resources/test/", "/src/test/resources/")
    )
}

internal class UpdateTestResourcesTest {

    @Test
    fun `verify that all test resources a part of the update sequence`() {
        // given
        val testResourcesFolder = "AnimenewsnetworkConverterTest"

        val filesInTestResources = Files.walk(testResource(testResourcesFolder))
            .filter { it.isRegularFile() }
            .filter { it.fileSuffix() == AnimenewsnetworkConfig.fileSuffix() }
            .map { it.toString() }
            .toList()

        // when
        val filesInList = files.keys.map {
            it.replace(testResourcesFolder, testResource(testResourcesFolder).toString())
        }

        // then
        assertThat(filesInTestResources.sorted()).isEqualTo(filesInList.sorted())
        assertThat(files.values.all { it.neitherNullNorBlank() }).isTrue()
    }
}