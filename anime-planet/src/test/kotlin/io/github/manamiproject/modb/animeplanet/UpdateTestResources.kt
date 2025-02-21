package io.github.manamiproject.modb.animeplanet

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
    "AnimePlanetAnimeConverterTest/anime_season/fall.html" to "sleepy-princess-in-the-demon-castle",
    "AnimePlanetAnimeConverterTest/anime_season/spring.html" to "a-whisker-away",
    "AnimePlanetAnimeConverterTest/anime_season/summer.html" to "japan-sinks-2020",
    "AnimePlanetAnimeConverterTest/anime_season/tba.html" to "world-end-economica",
    "AnimePlanetAnimeConverterTest/anime_season/winter.html" to "in-spectre",
    "AnimePlanetAnimeConverterTest/anime_season/year_only.html" to "your-name",

    "AnimePlanetAnimeConverterTest/duration/10_min.html" to "black-jack-heian-sento",
    "AnimePlanetAnimeConverterTest/duration/1_hour.html" to "sore-ike-anpanman-kyouryuu-nosshii-no-daibouken",
    "AnimePlanetAnimeConverterTest/duration/1_min.html" to "shigeru",
    "AnimePlanetAnimeConverterTest/duration/2_hours.html" to "a-letter-to-momo",
    "AnimePlanetAnimeConverterTest/duration/2_hours_15_min.html" to "mobile-suit-gundam-ii-soldiers-of-sorrow",

    "AnimePlanetAnimeConverterTest/episodes/1081+_eps.html" to "detective-conan",
    "AnimePlanetAnimeConverterTest/episodes/1_ep.html" to "pripara-movie-minna-no-akogare-lets-go-prix-paris",
    "AnimePlanetAnimeConverterTest/episodes/25_eps.html" to "code-geass-lelouch-of-the-rebellion-r2",
    "AnimePlanetAnimeConverterTest/episodes/no_episodes.html" to "a-girl-her-guard-dog",

    "AnimePlanetAnimeConverterTest/picture_and_thumbnail/ensure_primary_screenshot_as_picture.html" to "ado-usseewa",
    "AnimePlanetAnimeConverterTest/picture_and_thumbnail/neither_picture_nor_thumbnail.html" to "dog-signal",
    "AnimePlanetAnimeConverterTest/picture_and_thumbnail/picture_and_thumbnail_available.html" to "natsumes-book-of-friends-season-6-specials",

    "AnimePlanetAnimeConverterTest/related_anime/no_related_anime.html" to "mainichi-jk-kikaku",
    "AnimePlanetAnimeConverterTest/related_anime/same_franchise_and_other_franchise.html" to "fullmetal-alchemist-brotherhood",
    "AnimePlanetAnimeConverterTest/related_anime/same_franchise_only.html" to "demon-slayer-kimetsu-no-yaiba",

    "AnimePlanetAnimeConverterTest/scores/no-score.html" to "the-elder-sister-like-one",
    "AnimePlanetAnimeConverterTest/scores/score.html" to "the-promised-neverland",

    "AnimePlanetAnimeConverterTest/sources/id.html" to "code-geass-lelouch-of-the-rebellion-r2",

    "AnimePlanetAnimeConverterTest/status/range_-_ends_in_the_past.html" to "blade-of-the-immortal-2019",
    "AnimePlanetAnimeConverterTest/status/range_-_ends_same_year.html" to "attack-on-titan-the-final-season",
    "AnimePlanetAnimeConverterTest/status/range_to_unknown_-_starts_in_the_past.html" to "amemiya-san",
    "AnimePlanetAnimeConverterTest/status/range_to_unknown_-_starts_same_year.html" to "kubo-wont-let-me-be-invisible",
    "AnimePlanetAnimeConverterTest/status/tba.html" to "the-elder-sister-like-one",
    "AnimePlanetAnimeConverterTest/status/year_in_the_future.html" to "solo-leveling",
    "AnimePlanetAnimeConverterTest/status/year_in_the_past.html" to "banner-of-the-stars",
    "AnimePlanetAnimeConverterTest/status/year_is_current_year.html" to "laid-back-camp-2nd-season",

    "AnimePlanetAnimeConverterTest/synonyms/multiple_synonyms.html" to "star-blazers-space-battleship-yamato-2202-movie",
    "AnimePlanetAnimeConverterTest/synonyms/synonyms_available.html" to "your-name",
    "AnimePlanetAnimeConverterTest/synonyms/synonyms_not_available.html" to "black-clover",
    "AnimePlanetAnimeConverterTest/synonyms/synonyms_replaced_by_email_protected.html" to "gj-bu-at",

    "AnimePlanetAnimeConverterTest/tags/no_tags.html" to "mainichi-jk-kikaku",
    "AnimePlanetAnimeConverterTest/tags/various_tags.html" to "the-promised-neverland",

    "AnimePlanetAnimeConverterTest/title/special_chars.html" to "pripara-movie-minna-no-akogare-lets-go-prix-paris",
    "AnimePlanetAnimeConverterTest/title/title_replaced_by_email_protected.html" to "malice-doll",
    "AnimePlanetAnimeConverterTest/title/title_replaced_by_email_protected_no_jsonld.html" to "the-idolmaster-million-live",

    "AnimePlanetAnimeConverterTest/type/dvd_special.html" to "natsumes-book-of-friends-season-6-specials",
    "AnimePlanetAnimeConverterTest/type/movie.html" to "your-name",
    "AnimePlanetAnimeConverterTest/type/music_video.html" to "shelter",
    "AnimePlanetAnimeConverterTest/type/no_braces.html" to "dog-signal",
    "AnimePlanetAnimeConverterTest/type/other.html" to "gintama-jump-festa-2015",
    "AnimePlanetAnimeConverterTest/type/ova.html" to "rurouni-kenshin-tsuiokuhen",
    "AnimePlanetAnimeConverterTest/type/tv.html" to "fullmetal-alchemist-brotherhood",
    "AnimePlanetAnimeConverterTest/type/tv_special.html" to "hanamonogatari",
    "AnimePlanetAnimeConverterTest/type/web.html" to "planetarian",
)

internal fun main(): Unit = runCoroutine {
    files.forEach { (file, animeId) ->
        AnimePlanetDownloader.instance.download(animeId).writeToFile(resourceFile(file))
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
        val testResourcesFolder = "AnimePlanetAnimeConverterTest"

        val filesInTestResources = Files.walk(testResource(testResourcesFolder))
            .filter { it.isRegularFile() }
            .filter { it.fileSuffix() == AnimePlanetConfig.fileSuffix() }
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