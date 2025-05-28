package io.github.manamiproject.modb.anisearch

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
    "AnisearchAnimeConverterTest/anime_season/season/01.html" to "14628",
    "AnisearchAnimeConverterTest/anime_season/season/02.html" to "14967",
    "AnisearchAnimeConverterTest/anime_season/season/03.html" to "16251",
    "AnisearchAnimeConverterTest/anime_season/season/04.html" to "14508",
    "AnisearchAnimeConverterTest/anime_season/season/05.html" to "15264",
    "AnisearchAnimeConverterTest/anime_season/season/06.html" to "14935",
    "AnisearchAnimeConverterTest/anime_season/season/07.html" to "14816",
    "AnisearchAnimeConverterTest/anime_season/season/08.html" to "16401",
    "AnisearchAnimeConverterTest/anime_season/season/09.html" to "16401",
    "AnisearchAnimeConverterTest/anime_season/season/10.html" to "15606",
    "AnisearchAnimeConverterTest/anime_season/season/11.html" to "15042",
    "AnisearchAnimeConverterTest/anime_season/season/12.html" to "14484",
    "AnisearchAnimeConverterTest/anime_season/season/unknown.html" to "16275",
    "AnisearchAnimeConverterTest/anime_season/season/year_only.html" to "17467",

    "AnisearchAnimeConverterTest/anime_season/year_of_premiere/1958-11.html" to "5976",
    "AnisearchAnimeConverterTest/anime_season/year_of_premiere/1991.html" to "168",
    "AnisearchAnimeConverterTest/anime_season/year_of_premiere/2021-08-06.html" to "15890",
    "AnisearchAnimeConverterTest/anime_season/year_of_premiere/unknown.html" to "16275",

    "AnisearchAnimeConverterTest/duration/134_minutes.html" to "602",
    "AnisearchAnimeConverterTest/duration/1_hour.html" to "6247",
    "AnisearchAnimeConverterTest/duration/1_minute.html" to "11051",
    "AnisearchAnimeConverterTest/duration/24_minutes_per_episode.html" to "14844",
    "AnisearchAnimeConverterTest/duration/2_hours.html" to "6889",
    "AnisearchAnimeConverterTest/duration/63_minutes_by_6_episodes.html" to "7192",
    "AnisearchAnimeConverterTest/duration/70_minutes.html" to "7163",
    "AnisearchAnimeConverterTest/duration/episodes_and_duration_unknown.html" to "16711",
    "AnisearchAnimeConverterTest/duration/episodes_known_duration_unknown.html" to "17039",

    "AnisearchAnimeConverterTest/episodes/1.html" to "9981",
    "AnisearchAnimeConverterTest/episodes/10.html" to "3135",
    "AnisearchAnimeConverterTest/episodes/100.html" to "4138",
    "AnisearchAnimeConverterTest/episodes/1818.html" to "5801",
    "AnisearchAnimeConverterTest/episodes/type-is-double.html" to "16804",
    "AnisearchAnimeConverterTest/episodes/unknown.html" to "16578",

    "AnisearchAnimeConverterTest/picture_and_thumbnail/neither_picture_nor_thumbnail.html" to "15237",
    "AnisearchAnimeConverterTest/picture_and_thumbnail/picture_and_thumbnail_available.html" to "3633",

    "AnisearchAnimeConverterTest/scores/no-score.html" to "12224",
    "AnisearchAnimeConverterTest/scores/score.html" to "3633",

    "AnisearchAnimeConverterTest/sources/3633.html" to "3633",

    "AnisearchAnimeConverterTest/status/aborted.html" to "12433",
    "AnisearchAnimeConverterTest/status/canceled.html" to "13270",
    "AnisearchAnimeConverterTest/status/completed.html" to "3633",
    "AnisearchAnimeConverterTest/status/completed_in_japan_upcoming_elsewhere.html" to "6222",
    "AnisearchAnimeConverterTest/status/no_status.html" to "14494",
    "AnisearchAnimeConverterTest/status/on_hold.html" to "16925",
    "AnisearchAnimeConverterTest/status/ongoing.html" to "1721",
    "AnisearchAnimeConverterTest/status/upcoming.html" to "12224",

    "AnisearchAnimeConverterTest/synonyms/multiple_synonyms.html" to "1958",
    "AnisearchAnimeConverterTest/synonyms/no_synonyms.html" to "16260",
    "AnisearchAnimeConverterTest/synonyms/romanji_alteration.html" to "13631",
    "AnisearchAnimeConverterTest/synonyms/single_synonym.html" to "14456",

    "AnisearchAnimeConverterTest/synonyms/hidden_synonyms_11197.html" to "11197",
    "AnisearchAnimeConverterTest/synonyms/hidden_synonyms_8724.html" to "8724",
    "AnisearchAnimeConverterTest/synonyms/hidden_synonyms_and_named_parts.html" to "8093",
    "AnisearchAnimeConverterTest/synonyms/italic.html" to "17015",
    "AnisearchAnimeConverterTest/synonyms/multiple_synonyms.html" to "1958",
    "AnisearchAnimeConverterTest/synonyms/no_synonyms.html" to "16260",
    "AnisearchAnimeConverterTest/synonyms/romanji_alteration.html" to "13631",
    "AnisearchAnimeConverterTest/synonyms/single_synonym.html" to "14456",
    "AnisearchAnimeConverterTest/synonyms/synonyms_contain_named_parts.html" to "15599",

    "AnisearchAnimeConverterTest/tags/multiple_tags.html" to "15073",
    "AnisearchAnimeConverterTest/tags/no_tags.html" to "17467",
    "AnisearchAnimeConverterTest/tags/one_tag.html" to "613",

    "AnisearchAnimeConverterTest/title/special_chars.html" to "15159",
    "AnisearchAnimeConverterTest/title/title_not_set_in_jsonld.html" to "4410",

    "AnisearchAnimeConverterTest/type/bonus.html" to "10454",
    "AnisearchAnimeConverterTest/type/cm.html" to "12290",
    "AnisearchAnimeConverterTest/type/movie.html" to "9981",
    "AnisearchAnimeConverterTest/type/music-video.html" to "9830",
    "AnisearchAnimeConverterTest/type/other.html" to "16289",
    "AnisearchAnimeConverterTest/type/ova.html" to "3627",
    "AnisearchAnimeConverterTest/type/tv-series.html" to "4946",
    "AnisearchAnimeConverterTest/type/tv-special.html" to "13250",
    "AnisearchAnimeConverterTest/type/unknown.html" to "17467",
    "AnisearchAnimeConverterTest/type/web.html" to "14935",

    "AnisearchAnimeConverterTest/related_anime/multiple_related_anime_main.html" to "4942",
    "AnisearchAnimeConverterTest/related_anime/no_related_anime_but_adaption_main.html" to "14844",
    "AnisearchAnimeConverterTest/related_anime/no_related_anime_main.html" to "10941",
    "AnisearchAnimeConverterTest/related_anime/related_anime_file_missing_main.html" to "4942",
    "AnisearchAnimeConverterTest/related_anime/single_related_anime_main.html" to "16777",
)

private val relationsConfigFiles = mapOf(
    "AnisearchAnimeConverterTest/related_anime/multiple_related_anime.html" to "4942",
    "AnisearchAnimeConverterTest/related_anime/no_related_anime.html" to "10941",
    "AnisearchAnimeConverterTest/related_anime/no_related_anime_but_adaption.html" to "14844",
    "AnisearchAnimeConverterTest/related_anime/single_related_anime.html" to "16777",
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
        val testResourcesFolder = "AnisearchAnimeConverterTest"

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
        assertThat(mainConfigFiles.values.all { it.neitherNullNorBlank() }).isTrue()
        assertThat(relationsConfigFiles.values.all { it.neitherNullNorBlank() }).isTrue()
    }
}