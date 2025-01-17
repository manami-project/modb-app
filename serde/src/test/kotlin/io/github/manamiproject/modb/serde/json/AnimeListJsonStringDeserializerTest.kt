package io.github.manamiproject.modb.serde.json

import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.AnimeSeason
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.loadTestResource
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.Test

internal class AnimeListJsonStringDeserializerTest {

    @Test
    fun `throws exception if the given string is empty`() {
        // given
        val deserializer = AnimeListJsonStringDeserializer()

        // when
        val result = exceptionExpected<IllegalArgumentException> {
            deserializer.deserialize(EMPTY)
        }

        // then
        assertThat(result).hasMessage("Given JSON string must not be blank.")
    }

    @Test
    fun `throws exception if the given string is blank`() {
        // given
        val deserializer = AnimeListJsonStringDeserializer()

        // when
        val result = exceptionExpected<IllegalArgumentException> {
            deserializer.deserialize("    ")
        }

        // then
        assertThat(result).hasMessage("Given JSON string must not be blank.")
    }

    @Test
    fun `correctly deserialize dataset string`() {
        runBlocking {
            // given
            val deserializer = AnimeListJsonStringDeserializer()

            val expectedEntries = listOf(
                Anime(
                    _title = "Seikai no Monshou",
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/1"),
                    ),
                    relatedAnime = hashSetOf(
                        URI("https://anidb.net/anime/1623"),
                        URI("https://anidb.net/anime/4"),
                        URI("https://anidb.net/anime/6"),
                    ),
                    type = Anime.Type.TV,
                    episodes = 13,
                    picture = URI("https://cdn.anidb.net/images/main/224618.jpg"),
                    thumbnail = URI("https://cdn.anidb.net/images/main/224618.jpg-thumb.jpg"),
                    status = Anime.Status.FINISHED,
                    animeSeason = AnimeSeason(
                        season = AnimeSeason.Season.UNDEFINED,
                        year = 1999,
                    ),
                    synonyms = hashSetOf(
                        "CotS",
                        "Crest of the Stars",
                        "Hvězdný erb",
                        "SnM",
                        "星界の紋章",
                        "星界之纹章",
                    ),
                    tags = hashSetOf(
                        "action",
                        "adventure",
                        "genetic modification",
                        "novel",
                        "science fiction",
                        "space travel",
                    ),
                ),
                Anime(
                    _title = "Cowboy Bebop",
                    sources = hashSetOf(
                        URI("https://myanimelist.net/anime/1"),
                    ),
                    relatedAnime = hashSetOf(
                        URI("https://myanimelist.net/anime/17205"),
                        URI("https://myanimelist.net/anime/4037"),
                        URI("https://myanimelist.net/anime/5"),
                    ),
                    type = Anime.Type.TV,
                    episodes = 26,
                    picture = URI("https://cdn.myanimelist.net/images/anime/4/19644.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/4/19644t.jpg"),
                    status = Anime.Status.FINISHED,
                    animeSeason = AnimeSeason(
                        season = AnimeSeason.Season.SPRING,
                        year = 1998,
                    ),
                    synonyms = hashSetOf(
                        "カウボーイビバップ",
                    ),
                    tags = hashSetOf(
                        "action",
                        "adventure",
                        "comedy",
                        "drama",
                        "sci-fi",
                        "space",
                    ),
                ),
                Anime(
                    _title = "Cowboy Bebop: Tengoku no Tobira",
                    sources = hashSetOf(
                        URI("https://myanimelist.net/anime/5"),
                    ),
                    relatedAnime = hashSetOf(
                        URI("https://myanimelist.net/anime/1"),
                    ),
                    type = Anime.Type.MOVIE,
                    episodes = 1,
                    picture = URI("https://cdn.myanimelist.net/images/anime/1439/93480.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1439/93480t.jpg"),
                    status = Anime.Status.FINISHED,
                    animeSeason = AnimeSeason(
                        season = AnimeSeason.Season.SPRING,
                        year = 1998,
                    ),
                    synonyms = hashSetOf(
                        "Cowboy Bebop: Knockin' on Heaven's Door",
                        "Cowboy Bebop: The Movie", "カウボーイビバップ 天国の扉",
                    ),
                    tags = hashSetOf(
                        "action",
                        "drama",
                        "mystery",
                        "sci-fi",
                        "space",
                    ),
                ),
                Anime(
                    _title = "11 Eyes",
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/6751"),
                    ),
                    type = Anime.Type.TV,
                    episodes = 12,
                    picture = URI("https://cdn.anidb.net/images/main/32901.jpg"),
                    thumbnail = URI("https://cdn.anidb.net/images/main/32901.jpg-thumb.jpg"),
                    status = Anime.Status.FINISHED,
                    animeSeason = AnimeSeason(
                        season = AnimeSeason.Season.UNDEFINED,
                        year = 2009,
                    ),
                    synonyms = hashSetOf(
                        "11 akių",
                        "11 глаз",
                        "11 چشم",
                        "11eyes",
                        "11eyes -罪與罰與贖的少女-",
                        "11eyes: Tsumi to Batsu to Aganai no Shoujo",
                        "أحد عشر عيناً",
                        "イレブンアイズ",
                        "罪与罚与赎的少女",
                    ),
                    tags = hashSetOf(
                        "action",
                        "angst",
                        "contemporary fantasy",
                        "ecchi",
                        "erotic game",
                        "fantasy",
                        "female student",
                        "seinen",
                        "super power",
                        "swordplay",
                        "visual novel",
                    ),
                ),
                Anime(
                    _title = "11eyes",
                    sources = hashSetOf(
                        URI("https://anilist.co/anime/6682"),
                        URI("https://myanimelist.net/anime/6682"),
                    ),
                    relatedAnime = hashSetOf(
                        URI("https://anilist.co/anime/110465"),
                        URI("https://anilist.co/anime/7739"),
                        URI("https://myanimelist.net/anime/20557"),
                        URI("https://myanimelist.net/anime/7739"),
                    ),
                    type = Anime.Type.TV,
                    episodes = 12,
                    picture = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx6682-ZptgLsCCNHjL.jpg"),
                    thumbnail = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/default.jpg"),
                    status = Anime.Status.FINISHED,
                    animeSeason = AnimeSeason(
                        season = AnimeSeason.Season.FALL,
                        year = 2009,
                    ),
                    synonyms = hashSetOf(
                        "11eyes -Tsumi to Batsu to Aganai no Shoujo-",
                        "11eyes イレブンアイズ",
                        "イレブンアイズ",
                    ),
                    tags = hashSetOf(
                        "action",
                        "demons",
                        "ecchi",
                        "ensemble cast",
                        "gore",
                        "magic",
                        "male protagonist",
                        "memory manipulation",
                        "revenge",
                        "super power",
                        "supernatural",
                        "survival",
                        "swordplay",
                        "tragedy",
                        "witch",
                    ),
                ),
            )

            // when
            val result = deserializer.deserialize(loadTestResource("json/deserialization/test_dataset_for_deserialization.json"))

            // then
            assertThat(result.data).containsAll(expectedEntries)
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = AnimeListJsonStringDeserializer.instance

            // when
            val result = AnimeListJsonStringDeserializer.instance

            // then
            assertThat(result).isExactlyInstanceOf(AnimeListJsonStringDeserializer::class.java)
            assertThat(result===previous).isTrue()
        }
    }
}