package io.github.manamiproject.modb.myanimelist

import io.github.manamiproject.modb.core.coroutines.CoroutineManager.runCoroutine
import io.github.manamiproject.modb.core.extensions.fileSuffix
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
    "file_converter_tests/anime_season/season/apr.html" to "1006",
    "file_converter_tests/anime_season/season/aug.html" to "1038",
    "file_converter_tests/anime_season/season/dec.html" to "10000",
    "file_converter_tests/anime_season/season/feb.html" to "10077",
    "file_converter_tests/anime_season/season/jan.html" to "10098",
    "file_converter_tests/anime_season/season/jul.html" to "10029",
    "file_converter_tests/anime_season/season/jun.html" to "10112",
    "file_converter_tests/anime_season/season/mar.html" to "10016",
    "file_converter_tests/anime_season/season/may.html" to "1015",
    "file_converter_tests/anime_season/season/nov.html" to "1024",
    "file_converter_tests/anime_season/season/oct.html" to "1002",
    "file_converter_tests/anime_season/season/sep.html" to "10045",

    "file_converter_tests/anime_season/season/fall.html" to "38483",
    "file_converter_tests/anime_season/season/spring.html" to "38000",
    "file_converter_tests/anime_season/season/summer.html" to "37347",
    "file_converter_tests/anime_season/season/undefined.html" to "26145",
    "file_converter_tests/anime_season/season/winter.html" to "37779",

    "file_converter_tests/anime_season/year_of_premiere/aired_node_-_exact_day.html" to "33474",
    "file_converter_tests/anime_season/year_of_premiere/aired_node_-_exact_day_to_exact_day.html" to "41515",
    "file_converter_tests/anime_season/year_of_premiere/aired_node_-_exact_day_to_unknown.html" to "44015",
    "file_converter_tests/anime_season/year_of_premiere/aired_node_-_exact_day_to_year.html" to "11307",
    "file_converter_tests/anime_season/year_of_premiere/aired_node_-_month_of_year_to_unknown.html" to "43944",
    "file_converter_tests/anime_season/year_of_premiere/aired_node_-_month_of_year_to_year.html" to "36736",
    "file_converter_tests/anime_season/year_of_premiere/aired_node_-_unavailable.html" to "43314",
    "file_converter_tests/anime_season/year_of_premiere/aired_node_-_year_only.html" to "34958",
    "file_converter_tests/anime_season/year_of_premiere/aired_node_-_year_to_exact_day.html" to "18007",
    "file_converter_tests/anime_season/year_of_premiere/aired_node_-_year_to_unavailable.html" to "43314",
    "file_converter_tests/anime_season/year_of_premiere/aired_node_-_year_to_unknown.html" to "45874",
    "file_converter_tests/anime_season/year_of_premiere/aired_node_-_year_to_year.html" to "8663",
    "file_converter_tests/anime_season/year_of_premiere/premiered.html" to "12659",

    "file_converter_tests/duration/10_min.html" to "10055",
    "file_converter_tests/duration/10_min_per_episode.html" to "10039",
    "file_converter_tests/duration/10_sec.html" to "31686",
    "file_converter_tests/duration/10_sec_per_episode.html" to "32737",
    "file_converter_tests/duration/1_hour.html" to "10056",
    "file_converter_tests/duration/1_hour_11_min.html" to "10821",
    "file_converter_tests/duration/1_hour_11_min_per_episode.html" to "10937",
    "file_converter_tests/duration/2_hours.html" to "10389",
    "file_converter_tests/duration/2_hours_15_minutes.html" to "1091",
    "file_converter_tests/duration/unknown.html" to "10506",

    "file_converter_tests/episodes/1.html" to "31758",
    "file_converter_tests/episodes/10.html" to "851",
    "file_converter_tests/episodes/100.html" to "2165",
    "file_converter_tests/episodes/1818.html" to "12393",
    "file_converter_tests/episodes/unknown.html" to "30088",

    "file_converter_tests/picture_and_thumbnail/neither_picture_nor_thumbnail.html" to "55571",
    "file_converter_tests/picture_and_thumbnail/picture_and_thumbnail_available.html" to "10163",

    "file_converter_tests/related_anime/has_adaption_and_multiple_relations.html" to "1575",
    "file_converter_tests/related_anime/has_adaption_but_no_relation.html" to "25397",
    "file_converter_tests/related_anime/has_one_adaption_and_one_relation.html" to "8857",
    "file_converter_tests/related_anime/no_adaption_multiple_relations.html" to "18507",
    "file_converter_tests/related_anime/no_adaption_no_relations.html" to "10003",

    "file_converter_tests/sources/16498.html" to "16498",

    "file_converter_tests/status/finished.html" to "21329",
    "file_converter_tests/status/ongoing.html" to "40393",
    "file_converter_tests/status/upcoming.html" to "53065",

    "file_converter_tests/synonyms/multiple_languages_one_each.html" to "100",
    "file_converter_tests/synonyms/multiple_synonyms_for_one_language.html" to "22777",
    "file_converter_tests/synonyms/multiple_synonyms_with_comma_in_one_language.html" to "15609",
    "file_converter_tests/synonyms/no_synonyms.html" to "30559",
    "file_converter_tests/synonyms/one_synonym.html" to "10000",
    "file_converter_tests/synonyms/one_synonym_with_multiple_commas.html" to "12665",
    "file_converter_tests/synonyms/semicolon_in_synonym_wihtout_whitespaces.html" to "35315",
    "file_converter_tests/synonyms/semicolon_in_synonym_with_whitespace.html" to "38085",
    "file_converter_tests/synonyms/semicolon_in_title_but_not_in_synonyms.html" to "993",
    "file_converter_tests/synonyms/synonym_contains_comma_but_title_does_not.html" to "55774",
    "file_converter_tests/synonyms/synonym_contains_comma_followed_by_whitespace.html" to "12665",
    "file_converter_tests/synonyms/title_contains_comma_and_multiple_synonyms_for_one_language.html" to "15609",

    "file_converter_tests/tags/multiple_tags.html" to "5114",
    "file_converter_tests/tags/no_tags.html" to "28487",
    "file_converter_tests/tags/one_tag.html" to "10077",

    "file_converter_tests/title/english_and_original_title.html" to "45",
    "file_converter_tests/title/special_chars.html" to "31055",

    "file_converter_tests/type/cm.html" to "52834",
    "file_converter_tests/type/movie.html" to "28851",
    "file_converter_tests/type/movie_case_which_resulted_in_containsOwn.html" to "30097",
    "file_converter_tests/type/music.html" to "12659",
    "file_converter_tests/type/music_without_link.html" to "57733",
    "file_converter_tests/type/ona.html" to "38935",
    "file_converter_tests/type/ova.html" to "44",
    "file_converter_tests/type/pv.html" to "52811",
    "file_converter_tests/type/pv.html" to "52834",
    "file_converter_tests/type/special.html" to "21329",
    "file_converter_tests/type/tv.html" to "1535",
    "file_converter_tests/type/tv_special.html" to "2312",
    "file_converter_tests/type/unknown.html" to "55579",
)

internal fun main(): Unit = runCoroutine {
    files.forEach { (file, animeId) ->
        MyanimelistDownloader.instance.download(animeId).writeToFile(resourceFile(file))
        delay(random(5000, 10000))
    }

    print("Done")
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
        val testResourcesFolder = "file_converter_tests"

        val filesInTestResources = Files.walk(testResource(testResourcesFolder))
            .filter { it.isRegularFile() }
            .filter { it.fileSuffix() == MyanimelistConfig.fileSuffix() }
            .map { it.toString() }
            .toList()

        // when
        val filesInList = files.keys.map {
            it.replace(testResourcesFolder, testResource(testResourcesFolder).toString())
        }

        // then
        assertThat(filesInTestResources.sorted()).isEqualTo(filesInList.sorted())
    }
}