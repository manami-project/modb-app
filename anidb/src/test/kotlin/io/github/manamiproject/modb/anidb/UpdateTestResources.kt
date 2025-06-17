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
    "AnidbAnimeConverterTest/anime_season/season/date_published_cell_apr.html" to "7537",
    "AnidbAnimeConverterTest/anime_season/season/date_published_cell_aug.html" to "14464",
    "AnidbAnimeConverterTest/anime_season/season/date_published_cell_dec.html" to "6849",
    "AnidbAnimeConverterTest/anime_season/season/date_published_cell_feb.html" to "2365",
    "AnidbAnimeConverterTest/anime_season/season/date_published_cell_jan.html" to "10077",
    "AnidbAnimeConverterTest/anime_season/season/date_published_cell_jul.html" to "9600",
    //TODO currently no case. Find another: "AnidbAnimeConverterTest/anime_season/season/date_published_cell_jun.html" to "",
    "AnidbAnimeConverterTest/anime_season/season/date_published_cell_mar.html" to "7075",
    "AnidbAnimeConverterTest/anime_season/season/date_published_cell_may.html" to "8912",
    "AnidbAnimeConverterTest/anime_season/season/date_published_cell_nov.html" to "5789",
    "AnidbAnimeConverterTest/anime_season/season/date_published_cell_oct.html" to "13933",
    "AnidbAnimeConverterTest/anime_season/season/date_published_cell_sep.html" to "7167",
    "AnidbAnimeConverterTest/anime_season/season/season_cell_autumn.html" to "4527",
    "AnidbAnimeConverterTest/anime_season/season/season_cell_spring.html" to "8857",
    "AnidbAnimeConverterTest/anime_season/season/season_cell_summer.html" to "2109",
    "AnidbAnimeConverterTest/anime_season/season/season_cell_winter.html" to "3348",
    "AnidbAnimeConverterTest/anime_season/season/start_date_cell_apr.html" to "9745",
    "AnidbAnimeConverterTest/anime_season/season/start_date_cell_aug.html" to "194",
    "AnidbAnimeConverterTest/anime_season/season/start_date_cell_dec.html" to "11723",
    "AnidbAnimeConverterTest/anime_season/season/start_date_cell_feb.html" to "8441",
    "AnidbAnimeConverterTest/anime_season/season/start_date_cell_jan.html" to "4177",
    "AnidbAnimeConverterTest/anime_season/season/start_date_cell_jul.html" to "14937",
    "AnidbAnimeConverterTest/anime_season/season/start_date_cell_jun.html" to "14171",
    //TODO currently no case. Find another: "AnidbAnimeConverterTest/anime_season/season/start_date_cell_mar.html" to "",
    "AnidbAnimeConverterTest/anime_season/season/start_date_cell_may.html" to "13821",
    "AnidbAnimeConverterTest/anime_season/season/start_date_cell_nov.html" to "3325",
    "AnidbAnimeConverterTest/anime_season/season/start_date_cell_oct.html" to "15625",
    "AnidbAnimeConverterTest/anime_season/season/start_date_cell_sep.html" to "14126",
    "AnidbAnimeConverterTest/anime_season/season/undefined.html" to "10075",

    "AnidbAnimeConverterTest/anime_season/year_of_premiere/1986-06.html" to "12676",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/2004.html" to "10077",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/2017-10-03_-_unknown.html" to "12665",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/2017-10-14_-_2020.html" to "10755",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/2019-07-07_-_2019-09-22.html" to "14591",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/2019-08-23.html" to "14679",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/2019-10-05_-_2020-03.html" to "14238",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/2020_-_unknown.html" to "14988",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/2022.html" to "10060",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/date_published_but_with_time_period.html" to "9987",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/not_available.html" to "10075",
    "AnidbAnimeConverterTest/anime_season/year_of_premiere/unknown_date_of_year_-_unknown.html" to "15911",

    "AnidbAnimeConverterTest/duration/0_minutes.html" to "10052",
    "AnidbAnimeConverterTest/duration/1_hour.html" to "10150",
    "AnidbAnimeConverterTest/duration/1_minute.html" to "10284",
    "AnidbAnimeConverterTest/duration/25_minutes.html" to "1",
    "AnidbAnimeConverterTest/duration/2_hours.html" to "10008",
    "AnidbAnimeConverterTest/duration/missing.html" to "10060",

    "AnidbAnimeConverterTest/episodes/1.html" to "5391",
    "AnidbAnimeConverterTest/episodes/10.html" to "1560",
    "AnidbAnimeConverterTest/episodes/100.html" to "5049",
    "AnidbAnimeConverterTest/episodes/1818.html" to "6657",
    "AnidbAnimeConverterTest/episodes/1_but_more_entries.html" to "7",
    "AnidbAnimeConverterTest/episodes/unknown.html" to "15625",

    "AnidbAnimeConverterTest/picture_and_thumbnail/eu_cdn_replaced_by_default_cdn.html" to "14785",
    "AnidbAnimeConverterTest/picture_and_thumbnail/neither_picture_nor_thumbnail.html" to "10137",
    "AnidbAnimeConverterTest/picture_and_thumbnail/picture_and_thumbnail_available.html" to "10538",
    "AnidbAnimeConverterTest/picture_and_thumbnail/us_cdn_replaced_by_default_cdn.html" to "14785",

    "AnidbAnimeConverterTest/related_anime/multiple_related_anime.html" to "193",
    "AnidbAnimeConverterTest/related_anime/no_related_anime.html" to "14453",

    "AnidbAnimeConverterTest/scores/no-score.html" to "4563",
    "AnidbAnimeConverterTest/scores/score.html" to "17862",

    "AnidbAnimeConverterTest/sources/11221.html" to "11221",

    "AnidbAnimeConverterTest/status/date_published.html" to "8857",
    "AnidbAnimeConverterTest/status/start_to_end.html" to "3348",
    "AnidbAnimeConverterTest/status/start_to_unknown.html" to "15625",
    "AnidbAnimeConverterTest/status/unknown.html" to "17862",

    "AnidbAnimeConverterTest/synonyms/all_types.html" to "4563",

    "AnidbAnimeConverterTest/tags/multiple_tags_with_similar.html" to "4563",
    "AnidbAnimeConverterTest/tags/multiple_tags_without_similar.html" to "15085",
    "AnidbAnimeConverterTest/tags/no_tags.html" to "15458",
    "AnidbAnimeConverterTest/tags/one_tag.html" to "12876",

    "AnidbAnimeConverterTest/title/special_chars.html" to "5459",

    "AnidbAnimeConverterTest/type/movie.html" to "112",
    "AnidbAnimeConverterTest/type/music_video.html" to "13077",
    "AnidbAnimeConverterTest/type/other.html" to "9907",
    "AnidbAnimeConverterTest/type/ova.html" to "13248",
    "AnidbAnimeConverterTest/type/tv_series.html" to "13246",
    "AnidbAnimeConverterTest/type/tv_special.html" to "12519",
    "AnidbAnimeConverterTest/type/unknown.html" to "7608",
    "AnidbAnimeConverterTest/type/web.html" to "11788",

    "AnidbAnimeConverterTest/studios/multiple_studios.html" to "14785",
    "AnidbAnimeConverterTest/studios/no_studios.html" to "12881",
    "AnidbAnimeConverterTest/studios/single-person-for-animation-works.html" to "6993",
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