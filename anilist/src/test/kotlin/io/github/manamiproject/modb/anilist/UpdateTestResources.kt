package io.github.manamiproject.modb.anilist

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
    "file_converter_tests/anime_season/fall.json" to "104464",
    "file_converter_tests/anime_season/season_is_null_and_start_date_is_2006.json" to "1998",
    "file_converter_tests/anime_season/season_is_null_and_start_date_is_null.json" to "100050",
    "file_converter_tests/anime_season/seasonyear_set.json" to "141208",
    "file_converter_tests/anime_season/spring.json" to "101922",
    "file_converter_tests/anime_season/summer.json" to "106286",
    "file_converter_tests/anime_season/winter.json" to "101759",

    "file_converter_tests/duration/0.json" to "114196",
    "file_converter_tests/duration/120.json" to "100290",
    "file_converter_tests/duration/24.json" to "1",
    "file_converter_tests/duration/min_duration.json" to "102655",
    "file_converter_tests/duration/null.json" to "10004",

    "file_converter_tests/episodes/39.json" to "1251",
    "file_converter_tests/episodes/neither_episodes_nor_nextairingepisode_is_set.json" to "114441",
    "file_converter_tests/episodes/ongoing.json" to "235",

    "file_converter_tests/picture_and_thumbnail/picture_available.json" to "2167",
    "file_converter_tests/picture_and_thumbnail/picture_unavailable.json" to "157371",

    "file_converter_tests/related_anime/has_adaption_and_multiple_relations.json" to "1000",
    "file_converter_tests/related_anime/has_adaption_but_no_relation.json" to "100",
    "file_converter_tests/related_anime/has_one_adaption_and_one_relation.json" to "10005",
    "file_converter_tests/related_anime/no_adaption_multiple_relations.json" to "100133",
    "file_converter_tests/related_anime/no_adaption_no_relations.json" to "10003",

    "file_converter_tests/sources/15689.json" to "15689",

    "file_converter_tests/status/cancelled.json" to "101704",
    "file_converter_tests/status/finished.json" to "1",
    "file_converter_tests/status/not_yet_released.json" to "100081",
    "file_converter_tests/status/null.json" to "135643",
    "file_converter_tests/status/releasing.json" to "118123",

    "file_converter_tests/synonyms/synonyms_from_titles_and_synonyms.json" to "21453",

    "file_converter_tests/tags/tags.json" to "1",

    "file_converter_tests/title/special_chars.json" to "21453",

    "file_converter_tests/type/movie.json" to "20954",
    "file_converter_tests/type/music.json" to "97731",
    "file_converter_tests/type/null.json" to "109731",
    "file_converter_tests/type/ona.json" to "3167",
    "file_converter_tests/type/ova.json" to "2685",
    "file_converter_tests/type/special.json" to "106169",
    "file_converter_tests/type/tv.json" to "5114",
    "file_converter_tests/type/tv_short.json" to "98291",
)

internal fun main(): Unit = runCoroutine {
    files.forEach { (file, animeId) ->
        AnilistDownloader.instance.download(animeId).writeToFile(resourceFile(file))
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
            .filter { it.fileSuffix() == AnilistConfig.fileSuffix() }
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