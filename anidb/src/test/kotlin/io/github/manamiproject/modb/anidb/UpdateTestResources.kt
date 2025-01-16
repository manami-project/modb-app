package io.github.manamiproject.modb.anidb

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
    "file_converter_tests/anime_season/season/date_published_cell_apr.html" to "7537",
    "file_converter_tests/anime_season/season/date_published_cell_aug.html" to "14464",
    "file_converter_tests/anime_season/season/date_published_cell_dec.html" to "6849",
    "file_converter_tests/anime_season/season/date_published_cell_feb.html" to "2365",
    "file_converter_tests/anime_season/season/date_published_cell_jan.html" to "10077",
    "file_converter_tests/anime_season/season/date_published_cell_jul.html" to "9600",
    //TODO currently no case. Find another: "file_converter_tests/anime_season/season/date_published_cell_jun.html" to "",
    "file_converter_tests/anime_season/season/date_published_cell_mar.html" to "7075",
    "file_converter_tests/anime_season/season/date_published_cell_may.html" to "8912",
    "file_converter_tests/anime_season/season/date_published_cell_nov.html" to "5789",
    "file_converter_tests/anime_season/season/date_published_cell_oct.html" to "13933",
    "file_converter_tests/anime_season/season/date_published_cell_sep.html" to "7167",
    "file_converter_tests/anime_season/season/season_cell_autumn.html" to "4527",
    "file_converter_tests/anime_season/season/season_cell_spring.html" to "8857",
    "file_converter_tests/anime_season/season/season_cell_summer.html" to "2109",
    "file_converter_tests/anime_season/season/season_cell_winter.html" to "3348",
    "file_converter_tests/anime_season/season/start_date_cell_apr.html" to "9745",
    "file_converter_tests/anime_season/season/start_date_cell_aug.html" to "194",
    "file_converter_tests/anime_season/season/start_date_cell_dec.html" to "11723",
    "file_converter_tests/anime_season/season/start_date_cell_feb.html" to "8441",
    "file_converter_tests/anime_season/season/start_date_cell_jan.html" to "4177",
    "file_converter_tests/anime_season/season/start_date_cell_jul.html" to "14937",
    "file_converter_tests/anime_season/season/start_date_cell_jun.html" to "14171",
    //TODO currently no case. Find another: "file_converter_tests/anime_season/season/start_date_cell_mar.html" to "",
    "file_converter_tests/anime_season/season/start_date_cell_may.html" to "13821",
    "file_converter_tests/anime_season/season/start_date_cell_nov.html" to "3325",
    "file_converter_tests/anime_season/season/start_date_cell_oct.html" to "15625",
    "file_converter_tests/anime_season/season/start_date_cell_sep.html" to "14126",
    "file_converter_tests/anime_season/season/undefined.html" to "10075",

    "file_converter_tests/anime_season/year_of_premiere/1986-06.html" to "12676",
    "file_converter_tests/anime_season/year_of_premiere/2004.html" to "10077",
    "file_converter_tests/anime_season/year_of_premiere/2017-10-03_-_unknown.html" to "12665",
    "file_converter_tests/anime_season/year_of_premiere/2017-10-14_-_2020.html" to "10755",
    "file_converter_tests/anime_season/year_of_premiere/2019-07-07_-_2019-09-22.html" to "14591",
    "file_converter_tests/anime_season/year_of_premiere/2019-08-23.html" to "14679",
    "file_converter_tests/anime_season/year_of_premiere/2019-10-05_-_2020-03.html" to "14238",
    "file_converter_tests/anime_season/year_of_premiere/2020_-_unknown.html" to "14988",
    "file_converter_tests/anime_season/year_of_premiere/2022.html" to "10060",
    "file_converter_tests/anime_season/year_of_premiere/date_published_but_with_time_period.html" to "9987",
    "file_converter_tests/anime_season/year_of_premiere/not_available.html" to "10075",
    "file_converter_tests/anime_season/year_of_premiere/unknown_date_of_year_-_unknown.html" to "15911",

    "file_converter_tests/duration/0_minutes.html" to "10052",
    "file_converter_tests/duration/1_hour.html" to "10150",
    "file_converter_tests/duration/1_minute.html" to "10284",
    "file_converter_tests/duration/25_minutes.html" to "1",
    "file_converter_tests/duration/2_hours.html" to "10008",
    "file_converter_tests/duration/missing.html" to "10060",

    "file_converter_tests/episodes/1.html" to "5391",
    "file_converter_tests/episodes/10.html" to "1560",
    "file_converter_tests/episodes/100.html" to "5049",
    "file_converter_tests/episodes/1818.html" to "6657",
    "file_converter_tests/episodes/1_but_more_entries.html" to "7",
    "file_converter_tests/episodes/unknown.html" to "15625",

    "file_converter_tests/picture_and_thumbnail/eu_cdn_replaced_by_default_cdn.html" to "14785",
    "file_converter_tests/picture_and_thumbnail/neither_picture_nor_thumbnail.html" to "10137",
    "file_converter_tests/picture_and_thumbnail/picture_and_thumbnail_available.html" to "10538",
    "file_converter_tests/picture_and_thumbnail/us_cdn_replaced_by_default_cdn.html" to "14785",

    "file_converter_tests/related_anime/multiple_related_anime.html" to "193",
    "file_converter_tests/related_anime/no_related_anime.html" to "14453",

    "file_converter_tests/sources/11221.html" to "11221",

    "file_converter_tests/status/date_published.html" to "8857",
    "file_converter_tests/status/start_to_end.html" to "3348",
    "file_converter_tests/status/start_to_unknown.html" to "15625",
    "file_converter_tests/status/unknown.html" to "17862",

    "file_converter_tests/synonyms/all_types.html" to "4563",

    "file_converter_tests/tags/multiple_tags_with_similar.html" to "4563",
    "file_converter_tests/tags/multiple_tags_without_similar.html" to "15085",
    "file_converter_tests/tags/no_tags.html" to "15458",
    "file_converter_tests/tags/one_tag.html" to "12876",

    "file_converter_tests/title/special_chars.html" to "5459",

    "file_converter_tests/type/movie.html" to "112",
    "file_converter_tests/type/music_video.html" to "13077",
    "file_converter_tests/type/other.html" to "9907",
    "file_converter_tests/type/ova.html" to "13248",
    "file_converter_tests/type/tv_series.html" to "13246",
    "file_converter_tests/type/tv_special.html" to "12519",
    "file_converter_tests/type/unknown.html" to "7608",
    "file_converter_tests/type/web.html" to "11788",
)

internal fun main(): Unit = runCoroutine {
    files.forEach { (file, animeId) ->
        AnidbDownloader.instance.download(animeId).writeToFile(resourceFile(file))
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
            .filter { it.fileSuffix() == AnidbConfig.fileSuffix() }
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