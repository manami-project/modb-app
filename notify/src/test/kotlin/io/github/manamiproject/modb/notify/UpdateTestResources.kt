package io.github.manamiproject.modb.notify

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


private val mainConfigFiles = mapOf(
    "NotifyAnimeConverterTest/anime_season/1989.json" to "w1khcFmig",
    "NotifyAnimeConverterTest/anime_season/fall.json" to "5Vd6nwsiR",
    "NotifyAnimeConverterTest/anime_season/no_year.json" to "_SZLtKimR",
    "NotifyAnimeConverterTest/anime_season/spring.json" to "5K5MBjvmR",
    "NotifyAnimeConverterTest/anime_season/summer.json" to "TNo6W6vmR",
    "NotifyAnimeConverterTest/anime_season/undefined.json" to "_SZLtKimR",
    "NotifyAnimeConverterTest/anime_season/winter.json" to "ohndcHvmg",

    "NotifyAnimeConverterTest/duration/0.json" to "UrvVnSpng",
    "NotifyAnimeConverterTest/duration/120.json" to "tMrEpKiig",
    "NotifyAnimeConverterTest/duration/24.json" to "gLmp5FimR",

    "NotifyAnimeConverterTest/episodes/39.json" to "d_0bcKmmR",

    "NotifyAnimeConverterTest/picture_and_thumbnail/picture_and_thumbnail_available.json" to "IkCdhKimR",

    "NotifyAnimeConverterTest/scores/no-score.json" to "DCzg6xMnR",
    "NotifyAnimeConverterTest/scores/score.json" to "0-A-5Fimg",

    "NotifyAnimeConverterTest/sources/0-A-5Fimg.json" to "0-A-5Fimg",

    "NotifyAnimeConverterTest/status/current.json" to "CqN9Rja7R",
    "NotifyAnimeConverterTest/status/finished.json" to "Ml2V2KiiR",
    "NotifyAnimeConverterTest/status/tba.json" to "DCzg6xMnR",
    "NotifyAnimeConverterTest/status/upcoming.json" to "xnKYKwHVg",

    "NotifyAnimeConverterTest/synonyms/combine_non_canonical_title_and_synonyms.json" to "DtwM2Kmig",
    "NotifyAnimeConverterTest/synonyms/synonyms_is_null.json" to "rTgwpFmmg",

    "NotifyAnimeConverterTest/tags/genres_is_null.json" to "rTgwpFmmg",
    "NotifyAnimeConverterTest/tags/tags_from_genres.json" to "q9yku_3ig",

    "NotifyAnimeConverterTest/title/special_chars.json" to "MkGrtKmmR",

    "NotifyAnimeConverterTest/type/movie.json" to "FL0V2Kmmg",
    "NotifyAnimeConverterTest/type/music.json" to "Ff1bpKmmR",
    "NotifyAnimeConverterTest/type/ona.json" to "OamVhFmmR",
    "NotifyAnimeConverterTest/type/ova.json" to "MYsOvq7ig",
    "NotifyAnimeConverterTest/type/special.json" to "a8RVhKmmR",
    "NotifyAnimeConverterTest/type/tv.json" to "Ml2V2KiiR",

    "NotifyAnimeConverterTest/related_anime/items_in_relations_file_is_null/anime.json" to "--eZhFiig",
    "NotifyAnimeConverterTest/related_anime/multiple_relations/anime.json" to "uLs5tKiig",
    "NotifyAnimeConverterTest/related_anime/no_relations/anime.json" to "5Vd6nwsiR",
    "NotifyAnimeConverterTest/related_anime/no_relations_file/anime.json" to "5Vd6nwsiR",
)

private val relationsConfigFiles = mapOf(
    "NotifyAnimeConverterTest/related_anime/items_in_relations_file_is_null/relations.json" to "--eZhFiig",
    "NotifyAnimeConverterTest/related_anime/multiple_relations/relations.json" to "uLs5tKiig",
    "NotifyAnimeConverterTest/related_anime/no_relations/relations.json" to "5Vd6nwsiR",
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
        val testResourcesFolder = "NotifyAnimeConverterTest"

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
        assertThat(mainConfigFiles.values.all { it.neitherNullNorBlank() }).isTrue()
        assertThat(relationsConfigFiles.values.all { it.neitherNullNorBlank() }).isTrue()
    }
}