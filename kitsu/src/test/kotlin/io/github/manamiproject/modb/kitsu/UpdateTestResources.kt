package io.github.manamiproject.modb.kitsu

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
import kotlin.test.Test
import kotlin.io.path.isRegularFile


private val files = mapOf(
    "KitsuAnimeConverterTest/anime_season/year_of_premiere/1989.json" to "186",
    "KitsuAnimeConverterTest/anime_season/season/fall.json" to "42328",
    "KitsuAnimeConverterTest/anime_season/invalid_format.json" to "44117",
    "KitsuAnimeConverterTest/anime_season/null.json" to "49913",
    "KitsuAnimeConverterTest/anime_season/season/spring.json" to "41370",
    "KitsuAnimeConverterTest/anime_season/season/summer.json" to "42028",
    "KitsuAnimeConverterTest/anime_season/season/winter.json" to "41312",

    "KitsuAnimeConverterTest/duration/0.json" to "10041",
    "KitsuAnimeConverterTest/duration/120.json" to "10035",
    "KitsuAnimeConverterTest/duration/24.json" to "10",
    "KitsuAnimeConverterTest/duration/null.json" to "1078",

    "KitsuAnimeConverterTest/episodes/39.json" to "1126",
    "KitsuAnimeConverterTest/episodes/null.json" to "44019",

    "KitsuAnimeConverterTest/picture_and_thumbnail/null.json" to "47309",
    "KitsuAnimeConverterTest/picture_and_thumbnail/pictures.json" to "42006",

    "KitsuAnimeConverterTest/scores/no_score.json" to "44117",
    "KitsuAnimeConverterTest/scores/score.json" to "1517",

    "KitsuAnimeConverterTest/sources/1517.json" to "1517",

    "KitsuAnimeConverterTest/status/current.json" to "12",
    "KitsuAnimeConverterTest/status/finished.json" to "10041",
    "KitsuAnimeConverterTest/status/null.json" to "42059",
    "KitsuAnimeConverterTest/status/tba.json" to "49913",
    "KitsuAnimeConverterTest/status/unreleased.json" to "49915",
    "KitsuAnimeConverterTest/status/upcoming.json" to "49909",

    "KitsuAnimeConverterTest/synonyms/abbreviatedTitles_contains_null.json" to "1217",
    "KitsuAnimeConverterTest/synonyms/combine_titles_and_synonyms.json" to "13228",

    "KitsuAnimeConverterTest/title/special_chars.json" to "11260",

    "KitsuAnimeConverterTest/type/movie.json" to "2027",
    "KitsuAnimeConverterTest/type/music.json" to "11791",
    "KitsuAnimeConverterTest/type/ona.json" to "11613",
    "KitsuAnimeConverterTest/type/ova.json" to "11913",
    "KitsuAnimeConverterTest/type/special.json" to "343",
    "KitsuAnimeConverterTest/type/tv.json" to "6266",

    "KitsuAnimeConverterTest/related_anime/has_adaption_but_no_relation.json" to "8641",
    "KitsuAnimeConverterTest/related_anime/has_adaption_multiple_relations.json" to "1415",
    "KitsuAnimeConverterTest/related_anime/no_adaption_multiple_relations.json" to "7664",
    "KitsuAnimeConverterTest/related_anime/no_adaption_no_relations.json" to "5989",
    "KitsuAnimeConverterTest/related_anime/no_property_called_included.json" to "45567",
    "KitsuAnimeConverterTest/related_anime/one_adaption_one_relation.json" to "46232",

    "KitsuAnimeConverterTest/tags/multiple_tags.json" to "1",
    "KitsuAnimeConverterTest/tags/no_tags.json" to "43298",
    "KitsuAnimeConverterTest/tags/no_property_called_included.json" to "45567",

    "KitsuAnimeConverterTest/studios/multiple_studios.json" to "5443",
    "KitsuAnimeConverterTest/studios/no_studios.json" to "6337",
    "KitsuAnimeConverterTest/studios/no_property_called_included.json" to "45567",

    "KitsuAnimeConverterTest/producers/multiple_producers.json" to "1376",
    "KitsuAnimeConverterTest/producers/no_producers.json" to "6337",
    "KitsuAnimeConverterTest/producers/no_property_called_included.json" to "45567",
)

internal fun main(): Unit = runCoroutine {
    files.forEach { (file, animeId) ->
        KitsuDownloader.instance.download(animeId).writeToFile(resourceFile(file))
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
        val testResourcesDirectory = "KitsuAnimeConverterTest"

        val filesInTestResources = Files.walk(testResource(testResourcesDirectory))
            .filter { it.isRegularFile() }
            .filter { it.fileSuffix() == KitsuConfig.fileSuffix() }
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