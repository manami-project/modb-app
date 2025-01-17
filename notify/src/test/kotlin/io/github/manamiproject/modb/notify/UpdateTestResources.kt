package io.github.manamiproject.modb.notify

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


private val mainConfigFiles = mapOf(
    "file_converter_tests/anime_season/1989.json" to "w1khcFmig",
    "file_converter_tests/anime_season/fall.json" to "5Vd6nwsiR",
    "file_converter_tests/anime_season/no_year.json" to "_SZLtKimR",
    "file_converter_tests/anime_season/spring.json" to "5K5MBjvmR",
    "file_converter_tests/anime_season/summer.json" to "TNo6W6vmR",
    "file_converter_tests/anime_season/undefined.json" to "_SZLtKimR",
    "file_converter_tests/anime_season/winter.json" to "ohndcHvmg",

    "file_converter_tests/duration/0.json" to "UrvVnSpng",
    "file_converter_tests/duration/120.json" to "tMrEpKiig",
    "file_converter_tests/duration/24.json" to "gLmp5FimR",

    "file_converter_tests/episodes/39.json" to "d_0bcKmmR",

    "file_converter_tests/picture_and_thumbnail/picture_and_thumbnail_available.json" to "IkCdhKimR",

    "file_converter_tests/sources/0-A-5Fimg.json" to "0-A-5Fimg",

    "file_converter_tests/status/current.json" to "CqN9Rja7R",
    "file_converter_tests/status/finished.json" to "Ml2V2KiiR",
    "file_converter_tests/status/tba.json" to "DCzg6xMnR",
    "file_converter_tests/status/upcoming.json" to "xnKYKwHVg",

    "file_converter_tests/synonyms/combine_non_canonical_title_and_synonyms.json" to "DtwM2Kmig",
    "file_converter_tests/synonyms/synonyms_is_null.json" to "rTgwpFmmg",

    "file_converter_tests/tags/genres_is_null.json" to "rTgwpFmmg",
    "file_converter_tests/tags/tags_from_genres.json" to "q9yku_3ig",

    "file_converter_tests/title/special_chars.json" to "MkGrtKmmR",

    "file_converter_tests/type/movie.json" to "FL0V2Kmmg",
    "file_converter_tests/type/music.json" to "Ff1bpKmmR",
    "file_converter_tests/type/ona.json" to "OamVhFmmR",
    "file_converter_tests/type/ova.json" to "MYsOvq7ig",
    "file_converter_tests/type/special.json" to "a8RVhKmmR",
    "file_converter_tests/type/tv.json" to "Ml2V2KiiR",

    "file_converter_tests/related_anime/items_in_relations_file_is_null/anime.json" to "--eZhFiig",
    "file_converter_tests/related_anime/multiple_relations/anime.json" to "uLs5tKiig",
    "file_converter_tests/related_anime/no_relations/anime.json" to "5Vd6nwsiR",
    "file_converter_tests/related_anime/no_relations_file/anime.json" to "5Vd6nwsiR",
)

private val relationsConfigFiles = mapOf(
    "file_converter_tests/related_anime/items_in_relations_file_is_null/relations.json" to "--eZhFiig",
    "file_converter_tests/related_anime/multiple_relations/relations.json" to "uLs5tKiig",
    "file_converter_tests/related_anime/no_relations/relations.json" to "5Vd6nwsiR",
)

internal fun main(): Unit = runCoroutine {
    val downloader = NotifyDownloader(NotifyConfig)
    val relationsDownloader = NotifyDownloader(NotifyRelationsConfig)

    mainConfigFiles.forEach { (file, animeId) ->
        downloader.download(animeId).writeToFile(resourceFile(file))
        delay(random(5000, 10000))
    }

    relationsConfigFiles.forEach { (file, animeId) ->
        relationsDownloader.download(animeId).writeToFile(resourceFile(file))
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
            .filter { it.fileSuffix() == NotifyConfig.fileSuffix() }
            .map { it.toString() }
            .toList()

        // when
        val filesInList = mainConfigFiles.keys.union(relationsConfigFiles.keys).map {
            it.replace(testResourcesFolder, testResource(testResourcesFolder).toString())
        }

        // then
        assertThat(filesInTestResources.sorted()).isEqualTo(filesInList.sorted())
    }
}