package io.github.manamiproject.modb.myanimelist

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
    "MyanimelistAnimeConverterTest/anime_season/season/apr.html" to "1006",
    "MyanimelistAnimeConverterTest/anime_season/season/aug.html" to "1038",
    "MyanimelistAnimeConverterTest/anime_season/season/dec.html" to "10000",
    "MyanimelistAnimeConverterTest/anime_season/season/feb.html" to "10077",
    "MyanimelistAnimeConverterTest/anime_season/season/jan.html" to "10098",
    "MyanimelistAnimeConverterTest/anime_season/season/jul.html" to "10029",
    "MyanimelistAnimeConverterTest/anime_season/season/jun.html" to "10112",
    "MyanimelistAnimeConverterTest/anime_season/season/mar.html" to "10016",
    "MyanimelistAnimeConverterTest/anime_season/season/may.html" to "1015",
    "MyanimelistAnimeConverterTest/anime_season/season/nov.html" to "1024",
    "MyanimelistAnimeConverterTest/anime_season/season/oct.html" to "1002",
    "MyanimelistAnimeConverterTest/anime_season/season/sep.html" to "10045",

    "MyanimelistAnimeConverterTest/anime_season/season/fall.html" to "38483",
    "MyanimelistAnimeConverterTest/anime_season/season/spring.html" to "38000",
    "MyanimelistAnimeConverterTest/anime_season/season/summer.html" to "37347",
    "MyanimelistAnimeConverterTest/anime_season/season/undefined.html" to "26145",
    "MyanimelistAnimeConverterTest/anime_season/season/winter.html" to "37779",

    "MyanimelistAnimeConverterTest/anime_season/year_of_premiere/aired_node_-_exact_day.html" to "33474",
    "MyanimelistAnimeConverterTest/anime_season/year_of_premiere/aired_node_-_exact_day_to_exact_day.html" to "41515",
    "MyanimelistAnimeConverterTest/anime_season/year_of_premiere/aired_node_-_exact_day_to_unknown.html" to "44015",
    "MyanimelistAnimeConverterTest/anime_season/year_of_premiere/aired_node_-_exact_day_to_year.html" to "11307",
    "MyanimelistAnimeConverterTest/anime_season/year_of_premiere/aired_node_-_month_of_year_to_unknown.html" to "43944",
    "MyanimelistAnimeConverterTest/anime_season/year_of_premiere/aired_node_-_month_of_year_to_year.html" to "36736",
    "MyanimelistAnimeConverterTest/anime_season/year_of_premiere/aired_node_-_unavailable.html" to "43314",
    "MyanimelistAnimeConverterTest/anime_season/year_of_premiere/aired_node_-_year_only.html" to "34958",
    "MyanimelistAnimeConverterTest/anime_season/year_of_premiere/aired_node_-_year_to_exact_day.html" to "18007",
    "MyanimelistAnimeConverterTest/anime_season/year_of_premiere/aired_node_-_year_to_unavailable.html" to "43314",
    "MyanimelistAnimeConverterTest/anime_season/year_of_premiere/aired_node_-_year_to_unknown.html" to "45874",
    "MyanimelistAnimeConverterTest/anime_season/year_of_premiere/aired_node_-_year_to_year.html" to "8663",
    "MyanimelistAnimeConverterTest/anime_season/year_of_premiere/premiered.html" to "12659",

    "MyanimelistAnimeConverterTest/duration/10_min.html" to "10055",
    "MyanimelistAnimeConverterTest/duration/10_min_per_episode.html" to "10039",
    "MyanimelistAnimeConverterTest/duration/10_sec.html" to "31686",
    "MyanimelistAnimeConverterTest/duration/10_sec_per_episode.html" to "32737",
    "MyanimelistAnimeConverterTest/duration/1_hour.html" to "10056",
    "MyanimelistAnimeConverterTest/duration/1_hour_11_min.html" to "10821",
    "MyanimelistAnimeConverterTest/duration/1_hour_11_min_per_episode.html" to "10937",
    "MyanimelistAnimeConverterTest/duration/2_hours.html" to "10389",
    "MyanimelistAnimeConverterTest/duration/2_hours_15_minutes.html" to "1091",
    "MyanimelistAnimeConverterTest/duration/unknown.html" to "10506",

    "MyanimelistAnimeConverterTest/episodes/1.html" to "31758",
    "MyanimelistAnimeConverterTest/episodes/10.html" to "851",
    "MyanimelistAnimeConverterTest/episodes/100.html" to "2165",
    "MyanimelistAnimeConverterTest/episodes/1818.html" to "12393",
    "MyanimelistAnimeConverterTest/episodes/unknown.html" to "30088",

    "MyanimelistAnimeConverterTest/picture_and_thumbnail/neither_picture_nor_thumbnail.html" to "55571",
    "MyanimelistAnimeConverterTest/picture_and_thumbnail/picture_and_thumbnail_available.html" to "10163",

    "MyanimelistAnimeConverterTest/related_anime/has_adaption_and_multiple_relations.html" to "1575",
    "MyanimelistAnimeConverterTest/related_anime/has_adaption_but_no_relation.html" to "25397",
    "MyanimelistAnimeConverterTest/related_anime/has_one_adaption_and_one_relation.html" to "8857",
    "MyanimelistAnimeConverterTest/related_anime/no_adaption_multiple_relations.html" to "18507",
    "MyanimelistAnimeConverterTest/related_anime/no_adaption_no_relations.html" to "10003",

    "MyanimelistAnimeConverterTest/scores/no-score.html" to "54915",
    "MyanimelistAnimeConverterTest/scores/score.html" to "1535",

    "MyanimelistAnimeConverterTest/sources/16498.html" to "16498",

    "MyanimelistAnimeConverterTest/status/finished.html" to "21329",
    "MyanimelistAnimeConverterTest/status/ongoing.html" to "40393",
    "MyanimelistAnimeConverterTest/status/upcoming.html" to "53065",

    "MyanimelistAnimeConverterTest/synonyms/multiple_languages_one_each.html" to "100",
    "MyanimelistAnimeConverterTest/synonyms/multiple_synonyms_for_one_language.html" to "22777",
    "MyanimelistAnimeConverterTest/synonyms/multiple_synonyms_with_comma_in_one_language.html" to "15609",
    "MyanimelistAnimeConverterTest/synonyms/no_synonyms.html" to "30559",
    "MyanimelistAnimeConverterTest/synonyms/one_synonym.html" to "10000",
    "MyanimelistAnimeConverterTest/synonyms/one_synonym_with_multiple_commas.html" to "12665",
    "MyanimelistAnimeConverterTest/synonyms/semicolon_in_synonym_wihtout_whitespaces.html" to "35315",
    "MyanimelistAnimeConverterTest/synonyms/semicolon_in_synonym_with_whitespace.html" to "38085",
    "MyanimelistAnimeConverterTest/synonyms/semicolon_in_title_but_not_in_synonyms.html" to "993",
    "MyanimelistAnimeConverterTest/synonyms/synonym_contains_comma_but_title_does_not.html" to "55774",
    "MyanimelistAnimeConverterTest/synonyms/synonym_contains_comma_followed_by_whitespace.html" to "12665",
    "MyanimelistAnimeConverterTest/synonyms/title_contains_comma_and_multiple_synonyms_for_one_language.html" to "15609",

    "MyanimelistAnimeConverterTest/tags/multiple_tags.html" to "5114",
    "MyanimelistAnimeConverterTest/tags/no_tags.html" to "28487",
    "MyanimelistAnimeConverterTest/tags/one_tag.html" to "10077",

    "MyanimelistAnimeConverterTest/title/english_and_original_title.html" to "45",
    "MyanimelistAnimeConverterTest/title/special_chars.html" to "31055",

    "MyanimelistAnimeConverterTest/type/cm.html" to "52834",
    "MyanimelistAnimeConverterTest/type/movie.html" to "28851",
    "MyanimelistAnimeConverterTest/type/movie_case_which_resulted_in_containsOwn.html" to "30097",
    "MyanimelistAnimeConverterTest/type/music.html" to "12659",
    "MyanimelistAnimeConverterTest/type/music_without_link.html" to "57733",
    "MyanimelistAnimeConverterTest/type/ona.html" to "38935",
    "MyanimelistAnimeConverterTest/type/ova.html" to "44",
    "MyanimelistAnimeConverterTest/type/pv.html" to "52811",
    "MyanimelistAnimeConverterTest/type/pv.html" to "52834",
    "MyanimelistAnimeConverterTest/type/special.html" to "21329",
    "MyanimelistAnimeConverterTest/type/tv.html" to "1535",
    "MyanimelistAnimeConverterTest/type/tv_special.html" to "2312",
    "MyanimelistAnimeConverterTest/type/unknown.html" to "55579",

    "MyanimelistAnimeConverterTest/studios/multiple_studios.html" to "39575",
    "MyanimelistAnimeConverterTest/studios/no_studios.html" to "21761",

    "MyanimelistAnimeConverterTest/producers/multiple_producers.html" to "39575",
    "MyanimelistAnimeConverterTest/producers/no_producers.html" to "21761",
)

internal fun main(): Unit = runCoroutine {
    files.forEach { (file, animeId) ->
        MyanimelistDownloader.instance.download(animeId).writeToFile(resourceFile(file))
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
        val testResourcesDirectory = "MyanimelistAnimeConverterTest"

        val filesInTestResources = Files.walk(testResource(testResourcesDirectory))
            .filter { it.isRegularFile() }
            .filter { it.fileSuffix() == MyanimelistConfig.fileSuffix() }
            .map { it.toString() }
            .toList()

        // when
        val filesInList = files.keys.map {
            it.replace(testResourcesDirectory, testResource(testResourcesDirectory).toString())
        }

        // then
        assertThat(filesInTestResources.sorted()).isEqualTo(filesInList.sorted())
        assertThat(files.values.all { it.neitherNullNorBlank() }).isTrue()
    }
}