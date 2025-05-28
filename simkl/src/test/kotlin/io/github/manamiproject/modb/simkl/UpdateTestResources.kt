package io.github.manamiproject.modb.simkl

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
    "SimklAnimeConverterTest/animeSeason/season/1.html" to "1032077",
    "SimklAnimeConverterTest/animeSeason/season/2.html" to "812917",
    "SimklAnimeConverterTest/animeSeason/season/3.html" to "2136076",
    "SimklAnimeConverterTest/animeSeason/season/4.html" to "2153944",
    "SimklAnimeConverterTest/animeSeason/season/5.html" to "2010090",
    "SimklAnimeConverterTest/animeSeason/season/6.html" to "2125690",
    "SimklAnimeConverterTest/animeSeason/season/7.html" to "2231846",
    "SimklAnimeConverterTest/animeSeason/season/8.html" to "1925052",
    "SimklAnimeConverterTest/animeSeason/season/9.html" to "1870072",
    "SimklAnimeConverterTest/animeSeason/season/10.html" to "2605642",
    "SimklAnimeConverterTest/animeSeason/season/11.html" to "1560057",
    "SimklAnimeConverterTest/animeSeason/season/12.html" to "2257510",

    "SimklAnimeConverterTest/animeSeason/yearOfPremiere/overlapping_years.html" to "41586",
    "SimklAnimeConverterTest/animeSeason/yearOfPremiere/release-prior-first-ever-anime.html" to "37210",
    "SimklAnimeConverterTest/animeSeason/yearOfPremiere/same_year.html" to "2257510",

    "SimklAnimeConverterTest/animeSeason/unknown.html" to "1721823",

    "SimklAnimeConverterTest/duration/hours.html" to "436948",
    "SimklAnimeConverterTest/duration/minutes.html" to "1377443",

    "SimklAnimeConverterTest/episodes/1.html" to "436948",
    "SimklAnimeConverterTest/episodes/10.html" to "38356",
    "SimklAnimeConverterTest/episodes/100.html" to "38590",
    "SimklAnimeConverterTest/episodes/130_episodes_while_ongoing.html" to "2360180",
    "SimklAnimeConverterTest/episodes/1818.html" to "40927",
    "SimklAnimeConverterTest/episodes/fallback.html" to "424578",
    "SimklAnimeConverterTest/episodes/series_ended_missing_info.html" to "2478182",
    "SimklAnimeConverterTest/episodes/unknown.html" to "2451970",

    "SimklAnimeConverterTest/picture_and_thumbnail/neither_picture_nor_thumbnail.html" to "642681",
    "SimklAnimeConverterTest/picture_and_thumbnail/picture_and_thumbnail_available.html" to "436948",

    "SimklAnimeConverterTest/relatedAnime/directly_and_indirectly_related.html" to "41586",

    "SimklAnimeConverterTest/scores/no-score.html" to "40239",
    "SimklAnimeConverterTest/scores/score.html" to "40190",

    "SimklAnimeConverterTest/sources/41586.html" to "41586",

    "SimklAnimeConverterTest/status/airdate-year-only-indicating-finished.html" to "2445240",
    "SimklAnimeConverterTest/status/finished.html" to "41586",
    "SimklAnimeConverterTest/status/ongoing.html" to "2276538",
    "SimklAnimeConverterTest/status/upcoming.html" to "2451970",

    "SimklAnimeConverterTest/synonyms/multiple_synonyms.html" to "41586",
    "SimklAnimeConverterTest/synonyms/no_synonyms.html" to "1377443",

    "SimklAnimeConverterTest/tags/genres_and_subgenres.html" to "2132152",

    "SimklAnimeConverterTest/title/37171.html" to "37171",
    "SimklAnimeConverterTest/title/primary_title.html" to "41586",
    "SimklAnimeConverterTest/title/special_chars.html" to "2132152",

    "SimklAnimeConverterTest/type/movie.html" to "436948",
    "SimklAnimeConverterTest/type/music_video.html" to "37236",
    "SimklAnimeConverterTest/type/ona.html" to "41514",
    "SimklAnimeConverterTest/type/ova.html" to "38356",
    "SimklAnimeConverterTest/type/special.html" to "37301",
    "SimklAnimeConverterTest/type/tv.html" to "38590",

    "SimklAnimeConverterTest/studios/multiple_studios.html" to "1062567",
    "SimklAnimeConverterTest/studios/no_studios.html" to "39311",

    "SimklAnimeConverterTest/producers/multiple_producers.html" to "1062567",
    "SimklAnimeConverterTest/producers/no_producers.html" to "39311",
)

internal fun main(): Unit = runCoroutine {
    files.forEach { (file, animeId) ->
        SimklDownloader.instance.download(animeId).writeToFile(resourceFile(file))
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
        val testResourcesFolder = "SimklAnimeConverterTest"

        val filesInTestResources = Files.walk(testResource(testResourcesFolder))
            .filter { it.isRegularFile() }
            .filter { it.fileSuffix() == SimklConfig.fileSuffix() }
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