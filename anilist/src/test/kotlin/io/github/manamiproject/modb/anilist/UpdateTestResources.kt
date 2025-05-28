package io.github.manamiproject.modb.anilist

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
    "AnilistAnimeConverterTest/anime_season/fall.json" to "104464",
    "AnilistAnimeConverterTest/anime_season/season_is_null_and_start_date_is_2006.json" to "1998",
    "AnilistAnimeConverterTest/anime_season/season_is_null_and_start_date_is_null.json" to "100050",
    "AnilistAnimeConverterTest/anime_season/seasonyear_set.json" to "141208",
    "AnilistAnimeConverterTest/anime_season/spring.json" to "101922",
    "AnilistAnimeConverterTest/anime_season/summer.json" to "106286",
    "AnilistAnimeConverterTest/anime_season/winter.json" to "101759",

    "AnilistAnimeConverterTest/duration/0.json" to "114196",
    "AnilistAnimeConverterTest/duration/120.json" to "100290",
    "AnilistAnimeConverterTest/duration/24.json" to "1",
    "AnilistAnimeConverterTest/duration/min_duration.json" to "102655",
    "AnilistAnimeConverterTest/duration/null.json" to "10004",

    "AnilistAnimeConverterTest/episodes/39.json" to "1251",
    "AnilistAnimeConverterTest/episodes/neither_episodes_nor_nextairingepisode_is_set.json" to "114441",
    "AnilistAnimeConverterTest/episodes/ongoing.json" to "235",

    "AnilistAnimeConverterTest/picture_and_thumbnail/picture_available.json" to "2167",
    "AnilistAnimeConverterTest/picture_and_thumbnail/picture_unavailable.json" to "157371",

    "AnilistAnimeConverterTest/related_anime/has_adaption_and_multiple_relations.json" to "1000",
    "AnilistAnimeConverterTest/related_anime/has_adaption_but_no_relation.json" to "100",
    "AnilistAnimeConverterTest/related_anime/has_one_adaption_and_one_relation.json" to "10005",
    "AnilistAnimeConverterTest/related_anime/no_adaption_multiple_relations.json" to "100133",
    "AnilistAnimeConverterTest/related_anime/no_adaption_no_relations.json" to "10003",

    "AnilistAnimeConverterTest/scores/no-score.json" to "100081",
    "AnilistAnimeConverterTest/scores/score.json" to "5114",

    "AnilistAnimeConverterTest/sources/15689.json" to "15689",

    "AnilistAnimeConverterTest/status/cancelled.json" to "101704",
    "AnilistAnimeConverterTest/status/finished.json" to "1",
    "AnilistAnimeConverterTest/status/not_yet_released.json" to "100081",
    "AnilistAnimeConverterTest/status/null.json" to "135643",
    "AnilistAnimeConverterTest/status/releasing.json" to "118123",

    "AnilistAnimeConverterTest/synonyms/synonyms_from_titles_and_synonyms.json" to "21453",

    "AnilistAnimeConverterTest/tags/tags.json" to "1",

    "AnilistAnimeConverterTest/title/special_chars.json" to "21453",

    "AnilistAnimeConverterTest/type/movie.json" to "20954",
    "AnilistAnimeConverterTest/type/music.json" to "97731",
    "AnilistAnimeConverterTest/type/null.json" to "109731",
    "AnilistAnimeConverterTest/type/ona.json" to "3167",
    "AnilistAnimeConverterTest/type/ova.json" to "2685",
    "AnilistAnimeConverterTest/type/special.json" to "106169",
    "AnilistAnimeConverterTest/type/tv.json" to "5114",
    "AnilistAnimeConverterTest/type/tv_short.json" to "98291",

    "AnilistAnimeConverterTest/studios/multiple_studios.json" to "108617",
    "AnilistAnimeConverterTest/studios/no_studios.json" to "104189",

    "AnilistAnimeConverterTest/producers/multiple_producers.json" to "108617",
    "AnilistAnimeConverterTest/producers/no_producers.json" to "104189",
)

internal fun main(): Unit = runCoroutine {
    files.forEach { (file, animeId) ->
        AnilistDownloader.instance.download(animeId).writeToFile(resourceFile(file))
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
        val testResourcesFolder = "AnilistAnimeConverterTest"

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
        assertThat(files.values.all { it.neitherNullNorBlank() }).isTrue()
    }
}