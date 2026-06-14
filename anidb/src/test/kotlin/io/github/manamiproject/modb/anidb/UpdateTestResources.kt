package io.github.manamiproject.modb.anidb

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
    "AnidbAnimeConverterTest/anime_season/season/jan.xml" to "3348",
    "AnidbAnimeConverterTest/anime_season/season/feb.xml" to "2365",
    "AnidbAnimeConverterTest/anime_season/season/mar.xml" to "7075",
    "AnidbAnimeConverterTest/anime_season/season/apr.xml" to "7537",
    "AnidbAnimeConverterTest/anime_season/season/may.xml" to "8912",
    "AnidbAnimeConverterTest/anime_season/season/jun.xml" to "8857",
    "AnidbAnimeConverterTest/anime_season/season/jul.xml" to "9600",
    "AnidbAnimeConverterTest/anime_season/season/aug.xml" to "14464",
    "AnidbAnimeConverterTest/anime_season/season/sep.xml" to "7167",
    "AnidbAnimeConverterTest/anime_season/season/oct.xml" to "13933",
    "AnidbAnimeConverterTest/anime_season/season/nov.xml" to "5789",
    "AnidbAnimeConverterTest/anime_season/season/dec.xml" to "6849",
    "AnidbAnimeConverterTest/anime_season/season/unknown_-_year_only.xml" to "10077",

    "AnidbAnimeConverterTest/anime_season/year_of_premiere/1986-06.xml" to "12676",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/2004.xml" to "10077",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/2017-10-03_-_unknown.xml" to "12665",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/2017-10-14_-_2020.xml" to "10755",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/2019-07-07_-_2019-09-22.xml" to "14591",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/2019-08-23.xml" to "14679",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/2019-10-05_-_2020-03.xml" to "14238",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/2020_-_unknown.xml" to "14988",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/2022.xml" to "10060",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/date_published_but_with_time_period.xml" to "9987",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/not_available.xml" to "10075",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/unknown_date_of_year_-_unknown.xml" to "15911",

    "AnidbAnimeConverterTest/duration/0_minutes.xml" to "10052",
    "AnidbAnimeConverterTest/duration/1_hour.xml" to "10150",
    "AnidbAnimeConverterTest/duration/1_minute.xml" to "10284",
    "AnidbAnimeConverterTest/duration/25_minutes.xml" to "1",
    "AnidbAnimeConverterTest/duration/2_hours.xml" to "10008",
    "AnidbAnimeConverterTest/duration/missing.xml" to "10060",

    "AnidbAnimeConverterTest/episodes/1.xml" to "5391",
    "AnidbAnimeConverterTest/episodes/10.xml" to "1560",
    "AnidbAnimeConverterTest/episodes/100.xml" to "5049",
    "AnidbAnimeConverterTest/episodes/1818.xml" to "6657",
    "AnidbAnimeConverterTest/episodes/1_but_more_entries.xml" to "7",
    "AnidbAnimeConverterTest/episodes/unknown.xml" to "19977",

    "AnidbAnimeConverterTest/picture_and_thumbnail/neither_picture_nor_thumbnail.xml" to "10137",
    "AnidbAnimeConverterTest/picture_and_thumbnail/picture_and_thumbnail_available.xml" to "10538",

    "AnidbAnimeConverterTest/related_anime/multiple_related_anime.xml" to "193",
    "AnidbAnimeConverterTest/related_anime/no_related_anime.xml" to "14453",

    "AnidbAnimeConverterTest/scores/no-score.xml" to "19977",
    "AnidbAnimeConverterTest/scores/score.xml" to "17862",

    "AnidbAnimeConverterTest/sources/11221.xml" to "11221",

    "AnidbAnimeConverterTest/status/finished_-_startdate_and_enddate_identical_and_in_the_past.xml" to "8857",
    "AnidbAnimeConverterTest/status/finished_-_startdate_and_enddate_differing_and_in_the_past.xml" to "3348",
    "AnidbAnimeConverterTest/status/finished_-_enddate_is_missing_but_its_a_movie_released_in_the_past.xml" to "10075",
    "AnidbAnimeConverterTest/status/ongoing_-_startdate_in_the_past_and_no_enddate.xml" to "14171",
    "AnidbAnimeConverterTest/status/ongoing_-_startdate_in_the_past_and_enddate_in_the_future.xml" to "19206",
    "AnidbAnimeConverterTest/status/upcoming_-_both_startdate_and_enddate_in_the_future.xml" to "18325",
    "AnidbAnimeConverterTest/status/upcoming_-_startdate_in_the_future_and_no_enddate.xml" to "19977",

    "AnidbAnimeConverterTest/synonyms/all_types.xml" to "4563",

    "AnidbAnimeConverterTest/tags/multiple_tags.xml" to "4563",

    "AnidbAnimeConverterTest/title/special_chars.xml" to "5459",

    "AnidbAnimeConverterTest/type/movie.xml" to "112",
    "AnidbAnimeConverterTest/type/music_video.xml" to "13077",
    "AnidbAnimeConverterTest/type/other.xml" to "9907",
    "AnidbAnimeConverterTest/type/ova.xml" to "13248",
    "AnidbAnimeConverterTest/type/tv_series.xml" to "13246",
    "AnidbAnimeConverterTest/type/tv_special.xml" to "12519",
    "AnidbAnimeConverterTest/type/unknown.xml" to "7608",
    "AnidbAnimeConverterTest/type/web.xml" to "11788",
)

internal fun main(): Unit = runCoroutine {
    files.forEach { (file, animeId) ->
        AnidbDownloader.instance.download(animeId).writeToFile(resourceFile(file))
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
        val testResourcesDirectory = "AnidbAnimeConverterTest"

        val filesInTestResources = Files.walk(testResource(testResourcesDirectory))
            .filter { it.isRegularFile() }
            .filter { it.fileSuffix() == AnidbConfig.fileSuffix() }
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