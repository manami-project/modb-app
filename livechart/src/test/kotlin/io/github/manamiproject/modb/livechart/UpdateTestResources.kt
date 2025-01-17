package io.github.manamiproject.modb.livechart

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
    "file_converter_tests/anime_season/season/fall.html" to "9818",
    "file_converter_tests/anime_season/season/no_season_element.html" to "2685",
    "file_converter_tests/anime_season/season/spring.html" to "9649",
    "file_converter_tests/anime_season/season/summer.html" to "9627",
    "file_converter_tests/anime_season/season/undefined.html" to "10449",
    "file_converter_tests/anime_season/season/winter.html" to "8230",

    "file_converter_tests/anime_season/year_of_premiere/season_set.html" to "11084",
    "file_converter_tests/anime_season/year_of_premiere/season_tba_premiere_not_set.html" to "11988",
    "file_converter_tests/anime_season/year_of_premiere/season_tba_premiere_set.html" to "11758",

    "file_converter_tests/duration/10_min.html" to "6767",
    "file_converter_tests/duration/1_hour.html" to "2026",
    "file_converter_tests/duration/1_hour_11_min.html" to "5937",
    "file_converter_tests/duration/2_hours.html" to "986",
    "file_converter_tests/duration/2_hours_15_minutes.html" to "5926",
    "file_converter_tests/duration/30_sec.html" to "10429",
    "file_converter_tests/duration/unknown.html" to "11982",

    "file_converter_tests/episodes/1.html" to "1855",
    "file_converter_tests/episodes/10.html" to "6239",
    "file_converter_tests/episodes/100.html" to "8817",
    "file_converter_tests/episodes/number_of_episodes_known_and_running.html" to "11115",
    "file_converter_tests/episodes/number_of_episodes_unknown_but_currently_running.html" to "319",
    "file_converter_tests/episodes/unknown.html" to "11873",

    "file_converter_tests/picture_and_thumbnail/neither_picture_nor_thumbnail.html" to "2301",
    "file_converter_tests/picture_and_thumbnail/picture_and_thumbnail_available.html" to "3437",

    "file_converter_tests/related_anime/multiple_relations_no_scrolling.html" to "3366",
    "file_converter_tests/related_anime/multiple_relations_with_scrolling.html" to "3607",
    "file_converter_tests/related_anime/no_relations.html" to "9741",
    "file_converter_tests/related_anime/one_relation.html" to "3437",

    "file_converter_tests/sources/3437.html" to "3437",

    "file_converter_tests/status/finished.html" to "9818",
    "file_converter_tests/status/no_yet_released.html" to "10384",
    "file_converter_tests/status/ongoing.html" to "321",

    "file_converter_tests/synonyms/encoded_special_chars.html" to "8081",
    "file_converter_tests/synonyms/multiple_synonyms.html" to "2805",
    "file_converter_tests/synonyms/no_synonyms.html" to "10484",
    "file_converter_tests/synonyms/one_synonym.html" to "12289",

    "file_converter_tests/tags/multiple_tags.html" to "10450",
    "file_converter_tests/tags/no_tags.html" to "10959",
    "file_converter_tests/tags/one_tag.html" to "2388",

    "file_converter_tests/title/encoded_special_char.html" to "7907",
    "file_converter_tests/title/special_chars.html" to "10186",

    "file_converter_tests/type/movie.html" to "1296",
    "file_converter_tests/type/ova.html" to "3796",
    "file_converter_tests/type/special.html" to "9548",
    "file_converter_tests/type/tv.html" to "3437",
    "file_converter_tests/type/tv_short.html" to "10429",
    "file_converter_tests/type/unknown.html" to "11982",
    "file_converter_tests/type/web.html" to "8110",
    "file_converter_tests/type/web_short.html" to "8695",
)

internal fun main(): Unit = runCoroutine {
    files.forEach { (file, animeId) ->
        LivechartDownloader.instance.download(animeId).writeToFile(resourceFile(file))
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
            .filter { it.fileSuffix() == LivechartConfig.fileSuffix() }
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