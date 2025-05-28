package io.github.manamiproject.modb.livechart

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
    "LivechartAnimeConverterTest/anime_season/season/fall.html" to "9818",
    "LivechartAnimeConverterTest/anime_season/season/no_season_element.html" to "2685",
    "LivechartAnimeConverterTest/anime_season/season/spring.html" to "9649",
    "LivechartAnimeConverterTest/anime_season/season/summer.html" to "9627",
    "LivechartAnimeConverterTest/anime_season/season/undefined.html" to "10449",
    "LivechartAnimeConverterTest/anime_season/season/winter.html" to "8230",

    "LivechartAnimeConverterTest/anime_season/year_of_premiere/season_set.html" to "11084",
    "LivechartAnimeConverterTest/anime_season/year_of_premiere/season_tba_premiere_not_set.html" to "11988",
    "LivechartAnimeConverterTest/anime_season/year_of_premiere/season_tba_premiere_set.html" to "11758",

    "LivechartAnimeConverterTest/duration/10_min.html" to "6767",
    "LivechartAnimeConverterTest/duration/1_hour.html" to "2026",
    "LivechartAnimeConverterTest/duration/1_hour_11_min.html" to "5937",
    "LivechartAnimeConverterTest/duration/2_hours.html" to "986",
    "LivechartAnimeConverterTest/duration/2_hours_15_minutes.html" to "5926",
    "LivechartAnimeConverterTest/duration/30_sec.html" to "10429",
    "LivechartAnimeConverterTest/duration/unknown.html" to "11982",

    "LivechartAnimeConverterTest/episodes/1.html" to "1855",
    "LivechartAnimeConverterTest/episodes/10.html" to "6239",
    "LivechartAnimeConverterTest/episodes/100.html" to "8817",
    "LivechartAnimeConverterTest/episodes/number_of_episodes_known_and_running.html" to "11115",
    "LivechartAnimeConverterTest/episodes/number_of_episodes_unknown_but_currently_running.html" to "319",
    "LivechartAnimeConverterTest/episodes/unknown.html" to "11873",

    "LivechartAnimeConverterTest/picture_and_thumbnail/neither_picture_nor_thumbnail.html" to "2301",
    "LivechartAnimeConverterTest/picture_and_thumbnail/picture_and_thumbnail_available.html" to "3437",

    "LivechartAnimeConverterTest/related_anime/multiple_relations_no_scrolling.html" to "3366",
    "LivechartAnimeConverterTest/related_anime/multiple_relations_with_scrolling.html" to "3607",
    "LivechartAnimeConverterTest/related_anime/no_relations.html" to "9741",
    "LivechartAnimeConverterTest/related_anime/one_relation.html" to "3437",

    "LivechartAnimeConverterTest/scores/no-score.html" to "10384",
    "LivechartAnimeConverterTest/scores/score.html" to "3437",

    "LivechartAnimeConverterTest/sources/3437.html" to "3437",

    "LivechartAnimeConverterTest/status/finished.html" to "9818",
    "LivechartAnimeConverterTest/status/no_yet_released.html" to "10384",
    "LivechartAnimeConverterTest/status/ongoing.html" to "321",

    "LivechartAnimeConverterTest/synonyms/encoded_special_chars.html" to "8081",
    "LivechartAnimeConverterTest/synonyms/multiple_synonyms.html" to "2805",
    "LivechartAnimeConverterTest/synonyms/no_synonyms.html" to "10484",
    "LivechartAnimeConverterTest/synonyms/one_synonym.html" to "12289",

    "LivechartAnimeConverterTest/tags/multiple_tags.html" to "10450",
    "LivechartAnimeConverterTest/tags/no_tags.html" to "10959",
    "LivechartAnimeConverterTest/tags/one_tag.html" to "2388",

    "LivechartAnimeConverterTest/title/encoded_special_char.html" to "7907",
    "LivechartAnimeConverterTest/title/special_chars.html" to "10186",

    "LivechartAnimeConverterTest/type/movie.html" to "1296",
    "LivechartAnimeConverterTest/type/ova.html" to "3796",
    "LivechartAnimeConverterTest/type/special.html" to "9548",
    "LivechartAnimeConverterTest/type/tv.html" to "3437",
    "LivechartAnimeConverterTest/type/tv_short.html" to "10429",
    "LivechartAnimeConverterTest/type/unknown.html" to "11982",
    "LivechartAnimeConverterTest/type/web.html" to "8110",
    "LivechartAnimeConverterTest/type/web_short.html" to "8695",
)

internal fun main(): Unit = runCoroutine {
    files.forEach { (file, animeId) ->
        LivechartDownloader.instance.download(animeId).writeToFile(resourceFile(file))
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
        val testResourcesFolder = "LivechartAnimeConverterTest"

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
        assertThat(files.values.all { it.neitherNullNorBlank() }).isTrue()
    }
}