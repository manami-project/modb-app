package io.github.manamiproject.modb.anisearch

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
    "file_converter_tests/anime_season/season/01.html" to "14628",
    "file_converter_tests/anime_season/season/02.html" to "14967",
    "file_converter_tests/anime_season/season/03.html" to "16251",
    "file_converter_tests/anime_season/season/04.html" to "14508",
    "file_converter_tests/anime_season/season/05.html" to "15264",
    "file_converter_tests/anime_season/season/06.html" to "14935",
    "file_converter_tests/anime_season/season/07.html" to "14816",
    "file_converter_tests/anime_season/season/08.html" to "16401",
    "file_converter_tests/anime_season/season/09.html" to "16401",
    "file_converter_tests/anime_season/season/10.html" to "15606",
    "file_converter_tests/anime_season/season/11.html" to "15042",
    "file_converter_tests/anime_season/season/12.html" to "14484",
    "file_converter_tests/anime_season/season/unknown.html" to "16275",
    "file_converter_tests/anime_season/season/year_only.html" to "17467",

    "file_converter_tests/anime_season/year_of_premiere/1958-11.html" to "5976",
    "file_converter_tests/anime_season/year_of_premiere/1991.html" to "168",
    "file_converter_tests/anime_season/year_of_premiere/2021-08-06.html" to "15890",
    "file_converter_tests/anime_season/year_of_premiere/unknown.html" to "16275",

    "file_converter_tests/duration/134_minutes.html" to "602",
    "file_converter_tests/duration/1_hour.html" to "6247",
    "file_converter_tests/duration/1_minute.html" to "11051",
    "file_converter_tests/duration/24_minutes_per_episode.html" to "14844",
    "file_converter_tests/duration/2_hours.html" to "6889",
    "file_converter_tests/duration/63_minutes_by_6_episodes.html" to "7192",
    "file_converter_tests/duration/70_minutes.html" to "7163",
    "file_converter_tests/duration/episodes_and_duration_unknown.html" to "16711",
    "file_converter_tests/duration/episodes_known_duration_unknown.html" to "17039",

    "file_converter_tests/episodes/1.html" to "9981",
    "file_converter_tests/episodes/10.html" to "3135",
    "file_converter_tests/episodes/100.html" to "4138",
    "file_converter_tests/episodes/1818.html" to "5801",
    "file_converter_tests/episodes/type-is-double.html" to "16804",
    "file_converter_tests/episodes/unknown.html" to "16578",

    "file_converter_tests/picture_and_thumbnail/neither_picture_nor_thumbnail.html" to "15237",
    "file_converter_tests/picture_and_thumbnail/picture_and_thumbnail_available.html" to "3633",

    "file_converter_tests/sources/3633.html" to "3633",

    "file_converter_tests/status/aborted.html" to "12433",
    "file_converter_tests/status/completed.html" to "3633",
    "file_converter_tests/status/completed_in_japan_upcoming_elsewhere.html" to "6222",
    "file_converter_tests/status/no_status.html" to "14494",
    "file_converter_tests/status/on_hold.html" to "16925",
    "file_converter_tests/status/ongoing.html" to "1721",
    "file_converter_tests/status/upcoming.html" to "12224",

    "file_converter_tests/synonyms/multiple_synonyms.html" to "1958",
    "file_converter_tests/synonyms/no_synonyms.html" to "16260",
    "file_converter_tests/synonyms/romanji_alteration.html" to "13631",
    "file_converter_tests/synonyms/single_synonym.html" to "14456",

    "file_converter_tests/synonyms/hidden_synonyms_11197.html" to "11197",
    "file_converter_tests/synonyms/hidden_synonyms_8724.html" to "8724",
    "file_converter_tests/synonyms/hidden_synonyms_and_named_parts.html" to "8093",
    "file_converter_tests/synonyms/italic.html" to "17015",
    "file_converter_tests/synonyms/multiple_synonyms.html" to "1958",
    "file_converter_tests/synonyms/no_synonyms.html" to "16260",
    "file_converter_tests/synonyms/romanji_alteration.html" to "13631",
    "file_converter_tests/synonyms/single_synonym.html" to "14456",
    "file_converter_tests/synonyms/synonyms_contain_named_parts.html" to "15599",

    "file_converter_tests/tags/multiple_tags.html" to "15073",
    "file_converter_tests/tags/no_tags.html" to "17467",
    "file_converter_tests/tags/one_tag.html" to "613",

    "file_converter_tests/title/special_chars.html" to "15159",
    "file_converter_tests/title/title_not_set_in_jsonld.html" to "4410",

    "file_converter_tests/type/bonus.html" to "10454",
    "file_converter_tests/type/cm.html" to "12290",
    "file_converter_tests/type/movie.html" to "9981",
    "file_converter_tests/type/music-video.html" to "9830",
    "file_converter_tests/type/other.html" to "16289",
    "file_converter_tests/type/ova.html" to "3627",
    "file_converter_tests/type/tv-series.html" to "4946",
    "file_converter_tests/type/tv-special.html" to "13250",
    "file_converter_tests/type/unknown.html" to "17467",
    "file_converter_tests/type/web.html" to "14935",

    "file_converter_tests/related_anime/multiple_related_anime_main.html" to "4942",
    "file_converter_tests/related_anime/no_related_anime_but_adaption_main.html" to "14844",
    "file_converter_tests/related_anime/no_related_anime_main.html" to "10941",
    "file_converter_tests/related_anime/related_anime_file_missing_main.html" to "4942",
    "file_converter_tests/related_anime/single_related_anime_main.html" to "16777",
)

private val relationsConfigFiles = mapOf(
    "file_converter_tests/related_anime/multiple_related_anime.html" to "4942",
    "file_converter_tests/related_anime/no_related_anime.html" to "10941",
    "file_converter_tests/related_anime/no_related_anime_but_adaption.html" to "14844",
    "file_converter_tests/related_anime/single_related_anime.html" to "16777",
)

internal fun main(): Unit = runCoroutine {
    val downloader = AnisearchDownloader(AnisearchConfig)
    val relationsDownloader = AnisearchDownloader(AnisearchRelationsConfig)

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
            .filter { it.fileSuffix() == AnisearchConfig.fileSuffix() }
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