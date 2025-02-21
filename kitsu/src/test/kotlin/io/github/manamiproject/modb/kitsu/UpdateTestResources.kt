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


private val mainConfigFiles = mapOf(
    "KitsuAnimeConverterTest/anime_season/1989.json" to "186",
    "KitsuAnimeConverterTest/anime_season/fall.json" to "42328",
    "KitsuAnimeConverterTest/anime_season/invalid_format.json" to "44117",
    "KitsuAnimeConverterTest/anime_season/null.json" to "10613",
    "KitsuAnimeConverterTest/anime_season/spring.json" to "41370",
    "KitsuAnimeConverterTest/anime_season/summer.json" to "42028",
    "KitsuAnimeConverterTest/anime_season/winter.json" to "41312",

    "KitsuAnimeConverterTest/duration/0.json" to "10041",
    "KitsuAnimeConverterTest/duration/120.json" to "10035",
    "KitsuAnimeConverterTest/duration/24.json" to "10",
    "KitsuAnimeConverterTest/duration/null.json" to "46530",

    "KitsuAnimeConverterTest/episodes/39.json" to "1126",
    "KitsuAnimeConverterTest/episodes/null.json" to "44019",

    "KitsuAnimeConverterTest/picture_and_thumbnail/null.json" to "6334",
    "KitsuAnimeConverterTest/picture_and_thumbnail/pictures.json" to "42006",

    "KitsuAnimeConverterTest/scores/no-score.json" to "44117",
    "KitsuAnimeConverterTest/scores/score.json" to "1517",

    "KitsuAnimeConverterTest/sources/1517.json" to "1517",

    "KitsuAnimeConverterTest/status/current.json" to "12",
    "KitsuAnimeConverterTest/status/finished.json" to "10041",
    "KitsuAnimeConverterTest/status/null.json" to "42059",
    "KitsuAnimeConverterTest/status/tba.json" to "45557",
    "KitsuAnimeConverterTest/status/unreleased.json" to "46873",
    "KitsuAnimeConverterTest/status/upcoming.json" to "46358",

    "KitsuAnimeConverterTest/synonyms/abbreviatedTitles_contains_null.json" to "1217",
    "KitsuAnimeConverterTest/synonyms/combine_titles_and_synonyms.json" to "13228",

    "KitsuAnimeConverterTest/title/special_chars.json" to "11260",

    "KitsuAnimeConverterTest/type/movie.json" to "2027",
    "KitsuAnimeConverterTest/type/music.json" to "11791",
    "KitsuAnimeConverterTest/type/ona.json" to "11613",
    "KitsuAnimeConverterTest/type/ova.json" to "11913",
    "KitsuAnimeConverterTest/type/special.json" to "343",
    "KitsuAnimeConverterTest/type/tv.json" to "6266",

    "KitsuAnimeConverterTest/related_anime/has_adaption_but_no_relation/8641.json" to "8641",
    "KitsuAnimeConverterTest/related_anime/has_adaption_multiple_relations/1415.json" to "1415",
    "KitsuAnimeConverterTest/related_anime/no_adaption_multiple_relations/7664.json" to "7664",
    "KitsuAnimeConverterTest/related_anime/no_adaption_no_relations/5989.json" to "5989",
    "KitsuAnimeConverterTest/related_anime/one_adaption_one_relation/46232.json" to "46232",
    "KitsuAnimeConverterTest/tags/1.json" to "1",
    "KitsuAnimeConverterTest/tags/43298.json" to "43298",
)

private val relationsConfigFiles = mapOf(
    "KitsuAnimeConverterTest/related_anime/has_adaption_but_no_relation/8641_relations.json" to "8641",
    "KitsuAnimeConverterTest/related_anime/has_adaption_multiple_relations/1415_relations.json" to "1415",
    "KitsuAnimeConverterTest/related_anime/no_adaption_multiple_relations/7664_relations.json" to "7664",
    "KitsuAnimeConverterTest/related_anime/no_adaption_no_relations/5989_relations.json" to "5989",
    "KitsuAnimeConverterTest/related_anime/one_adaption_one_relation/46232_relations.json" to "46232",
    "KitsuAnimeConverterTest/no_adaption_no_relations_default_file.json" to "0", // TODO: get new id
)
private val tagsConfigFiles = mapOf(
    "KitsuAnimeConverterTest/no_tags_default_file.json" to "0", // TODO: get new id
    "KitsuAnimeConverterTest/tags/1_tags.json" to "1",
    "KitsuAnimeConverterTest/tags/43298_tags.json" to "43298",
)

internal fun main(): Unit = runCoroutine {
    val downloader = KitsuDownloader(KitsuConfig)
    val relationsDownloader = KitsuDownloader(KitsuRelationsConfig)
    val tagssDownloader = KitsuDownloader(KitsuTagsConfig)

    mainConfigFiles.forEach { (file, animeId) ->
        downloader.download(animeId).writeToFile(resourceFile(file))
        delay(random(5000, 10000))
    }

    relationsConfigFiles.forEach { (file, animeId) ->
        relationsDownloader.download(animeId).writeToFile(resourceFile(file))
        delay(random(5000, 10000))
    }

    tagsConfigFiles.forEach { (file, animeId) ->
        tagssDownloader.download(animeId).writeToFile(resourceFile(file))
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
        val testResourcesFolder = "KitsuAnimeConverterTest"

        val filesInTestResources = Files.walk(testResource(testResourcesFolder))
            .filter { it.isRegularFile() }
            .filter { it.fileSuffix() == KitsuConfig.fileSuffix() }
            .map { it.toString() }
            .toList()

        // when
        val filesInList = mainConfigFiles.keys
            .union(relationsConfigFiles.keys)
            .union(tagsConfigFiles.keys).map {
                it.replace(testResourcesFolder, testResource(testResourcesFolder).toString())
            }

        // then
        assertThat(filesInTestResources.sorted()).isEqualTo(filesInList.sorted())
        assertThat(mainConfigFiles.values.all { it.neitherNullNorBlank() }).isTrue()
        assertThat(relationsConfigFiles.values.all { it.neitherNullNorBlank() }).isTrue()
        assertThat(tagsConfigFiles.values.all { it.neitherNullNorBlank() }).isTrue()
    }
}