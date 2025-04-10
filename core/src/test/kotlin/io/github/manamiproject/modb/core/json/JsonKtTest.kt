package io.github.manamiproject.modb.core.json

import com.squareup.moshi.JsonDataException
import io.github.manamiproject.modb.core.anime.*
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.SUMMER
import io.github.manamiproject.modb.core.anime.AnimeStatus.FINISHED
import io.github.manamiproject.modb.core.anime.AnimeType.TV
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.core.json.Json.SerializationOptions.DEACTIVATE_PRETTY_PRINT
import io.github.manamiproject.modb.core.json.Json.SerializationOptions.DEACTIVATE_SERIALIZE_NULL
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.testResource
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.io.path.inputStream
import kotlin.test.Test

internal class JsonKtTest {

    @Nested
    inner class DeserializationTests {

        @Nested
        inner class AnimeRawTests {

            @Test
            fun `deserialize AnimeRaw - using inputstream`() {
                runBlocking {
                    // given
                    val expectedAnime = AnimeRaw(
                        _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                        _sources = hashSetOf(
                            URI("https://myanimelist.net/anime/6351"),
                        ),
                        _relatedAnime = hashSetOf(
                            URI("https://myanimelist.net/anime/2167"),
                        ),
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SUMMER,
                            year = 2009,
                        ),
                        picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                        duration = Duration(24, MINUTES),
                        _synonyms = hashSetOf(
                            "Clannad ~After Story~: Another World, Kyou Chapter",
                            "Clannad: After Story OVA",
                            "クラナド　アフターストーリー　もうひとつの世界　杏編",
                        ),
                        _tags = hashSetOf(
                            "comedy",
                            "romance",
                        ),
                    ).addScores(
                        MetaDataProviderScoreValue(
                            hostname = "myanimelist.net",
                            value = 7.77,
                            range = 1.0..10.0,
                        ),
                    )

                    val inputStream = testResource("JsonKtTest/animeraw_all_properties_set.json").inputStream()

                    // when
                    val result = Json.parseJson<AnimeRaw>(inputStream)

                    // then
                    assertThat(result).isEqualTo(expectedAnime)
                }
            }

            @Test
            fun `deserialize AnimeRaw - all properties set`() {
                runBlocking {
                    // given
                    val expectedAnime = AnimeRaw(
                        _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                        _sources = hashSetOf(
                            URI("https://myanimelist.net/anime/6351"),
                        ),
                        _relatedAnime = hashSetOf(
                            URI("https://myanimelist.net/anime/2167"),
                        ),
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SUMMER,
                            year = 2009,
                        ),
                        picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                        duration = Duration(24, MINUTES),
                        _synonyms = hashSetOf(
                            "Clannad ~After Story~: Another World, Kyou Chapter",
                            "Clannad: After Story OVA",
                            "クラナド　アフターストーリー　もうひとつの世界　杏編",
                        ),
                        _tags = hashSetOf(
                            "comedy",
                            "romance",
                        ),
                    ).addScores(
                        MetaDataProviderScoreValue(
                            hostname = "myanimelist.net",
                            value = 7.77,
                            range = 1.0..10.0,
                        ),
                    )

                    val json = loadTestResource<String>("JsonKtTest/animeraw_all_properties_set.json")

                    // when
                    val result = Json.parseJson<AnimeRaw>(json)

                    // then
                    assertThat(result).isEqualTo(expectedAnime)
                }
            }

            @Test
            fun `deserialize AnimeRaw - default properties`() {
                runBlocking {
                    // given
                    val expectedAnime = AnimeRaw("Death Note")

                    val json = loadTestResource<String>("JsonKtTest/anime_default_values.json")

                    // when
                    val result = Json.parseJson<AnimeRaw>(json)

                    // then
                    assertThat(result).isEqualTo(expectedAnime)
                }
            }
        }

        @Nested
        inner class AnimeTests {

            @Test
            fun `deserialize Anime - using inputstream`() {
                runBlocking {
                    // given
                    val expectedAnime = Anime(
                        title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/6351"),
                        ),
                        relatedAnime = hashSetOf(
                            URI("https://myanimelist.net/anime/2167"),
                        ),
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SUMMER,
                            year = 2009,
                        ),
                        picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                        duration = Duration(24, MINUTES),
                        score = ScoreValue(
                            arithmeticGeometricMean = 1.29,
                            arithmeticMean = 2.38,
                            median = 3.47,
                        ),
                        synonyms = hashSetOf(
                            "Clannad ~After Story~: Another World, Kyou Chapter",
                            "Clannad: After Story OVA",
                            "クラナド　アフターストーリー　もうひとつの世界　杏編",
                        ),
                        tags = hashSetOf(
                            "comedy",
                            "romance",
                        ),
                    )

                    val inputStream = testResource("JsonKtTest/anime_all_properties_set.json").inputStream()

                    // when
                    val result = Json.parseJson<Anime>(inputStream)

                    // then
                    assertThat(result).isEqualTo(expectedAnime)
                }
            }

            @Test
            fun `deserialize Anime - all properties set`() {
                runBlocking {
                    // given
                    val expectedAnime = Anime(
                        title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/6351"),
                        ),
                        relatedAnime = hashSetOf(
                            URI("https://myanimelist.net/anime/2167"),
                        ),
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SUMMER,
                            year = 2009,
                        ),
                        picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                        duration = Duration(24, MINUTES),
                        score = ScoreValue(
                            arithmeticGeometricMean = 1.29,
                            arithmeticMean = 2.38,
                            median = 3.47,
                        ),
                        synonyms = hashSetOf(
                            "Clannad ~After Story~: Another World, Kyou Chapter",
                            "Clannad: After Story OVA",
                            "クラナド　アフターストーリー　もうひとつの世界　杏編",
                        ),
                        tags = hashSetOf(
                            "comedy",
                            "romance",
                        ),
                    )

                    val json = loadTestResource<String>("JsonKtTest/anime_all_properties_set.json")

                    // when
                    val result = Json.parseJson<Anime>(json)

                    // then
                    assertThat(result).isEqualTo(expectedAnime)
                }
            }

            @Test
            fun `deserialize Anime - default properties`() {
                runBlocking {
                    // given
                    val expectedAnime = Anime("Death Note")

                    val json = loadTestResource<String>("JsonKtTest/anime_default_values.json")

                    // when
                    val result = Json.parseJson<Anime>(json)

                    // then
                    assertThat(result).isEqualTo(expectedAnime)
                }
            }
        }

        @Test
        fun `deserialize object - throws exception if a property having a non-nullable type is mapped to null`() {
            // given
            val json = """
                {
                  "nullableString": null,
                  "nonNullableString": null
                }
            """.trimIndent()

            // when
            val result = exceptionExpected<JsonDataException> {
                Json.parseJson<NullableTestClass>(json)?.copy()
            }

            // then
            assertThat(result).hasMessage("Non-null value 'nonNullableString' was null at \$.nonNullableString")
        }

        @Test
        fun `deserialize an array - non nullable types with default value can contain null`() {
            runBlocking {
                // given
                val json = """
                    {
                      "nonNullableList": [
                        "value1",
                        null,
                        "value3"
                      ]
                    }
                """.trimIndent()

                // when
                val result = Json.parseJson<ClassWithList>(json)

                // then
                assertThat(result?.nonNullableList).containsNull()
            }
        }

        @Test
        fun `deserialize an array - Although the type of the list is non-nullable and copy is called on a list containing null, no exception is being thrown`() {
            runBlocking {
                // given
                val json = """
                    {
                      "nonNullableList": [
                        "value1",
                        null,
                        "value3"
                      ]
                    }
                """.trimIndent()

                // when
                val result = Json.parseJson<ClassWithList>(json)?.copy()!!

                // then
                assertThat(result.nonNullableList).containsNull()
            }
        }
    }

    @Nested
    inner class SerializationTests {

        @Nested
        inner class DefaultOptionsTests {

            @Nested
            inner class AnimeRawTests {

                @Test
                fun `serialize AnimeRaw - all properties set`() {
                    runBlocking {
                        // given
                        val anime = AnimeRaw(
                            _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                            _sources = hashSetOf(
                                URI("https://livechart.me/anime/3681"),
                                URI("https://anisearch.com/anime/6826"),
                                URI("https://kitsu.io/anime/4529"),
                                URI("https://anime-planet.com/anime/clannad-another-world-kyou-chapter"),
                                URI("https://anilist.co/anime/6351"),
                                URI("https://notify.moe/anime/3L63cKimg"),
                                URI("https://myanimelist.net/anime/6351"),
                            ),
                            _relatedAnime = hashSetOf(
                                URI("https://myanimelist.net/anime/4181"),
                                URI("https://anilist.co/anime/2167"),
                                URI("https://anime-planet.com/anime/clannad"),
                                URI("https://livechart.me/anime/10537"),
                                URI("https://anime-planet.com/anime/clannad-another-world-tomoyo-chapter"),
                                URI("https://livechart.me/anime/10976"),
                                URI("https://anime-planet.com/anime/clannad-movie"),
                                URI("https://anisearch.com/anime/4199"),
                                URI("https://notify.moe/anime/F2eY5Fmig"),
                                URI("https://livechart.me/anime/3581"),
                                URI("https://anime-planet.com/anime/clannad-after-story"),
                                URI("https://livechart.me/anime/3588"),
                                URI("https://myanimelist.net/anime/2167"),
                                URI("https://livechart.me/anime/3657"),
                                URI("https://anilist.co/anime/4059"),
                                URI("https://livechart.me/anime/3822"),
                                URI("https://kitsu.io/anime/1962"),
                            ),
                            type = TV,
                            episodes = 24,
                            status = FINISHED,
                            animeSeason = AnimeSeason(
                                season = SUMMER,
                                year = 2009,
                            ),
                            picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                            duration = Duration(24, MINUTES),
                            _synonyms = hashSetOf(
                                "Clannad (TV)",
                                "Kuranado",
                                "Clannad TV",
                                "CLANNAD",
                                "クラナド",
                                "Кланнад",
                                "Кланад",
                                "كلاناد",
                                "Clannad 1",
                                "클라나드",
                                "خانواده",
                                "کلاناد",
                                "แคลนนาด",
                                "くらなど",
                                "ＣＬＡＮＮＡＤ -クラナド-",
                            ),
                            _tags = hashSetOf(
                                "baseball",
                                "based on a visual novel",
                                "basketball",
                                "amnesia",
                                "coming of age",
                                "asia",
                                "daily life",
                                "comedy",
                                "delinquents",
                                "earth",
                                "romance",
                                "ensemble cast",
                                "drama",
                            ),
                        ).addScores(
                            MetaDataProviderScoreValue(
                                hostname = "myanimelist.net",
                                value = 7.77,
                                range = 1.0..10.0,
                            ),
                        )

                        val expectedJson = """
                        {
                          "sources": [
                            "https://anilist.co/anime/6351",
                            "https://anime-planet.com/anime/clannad-another-world-kyou-chapter",
                            "https://anisearch.com/anime/6826",
                            "https://kitsu.io/anime/4529",
                            "https://livechart.me/anime/3681",
                            "https://myanimelist.net/anime/6351",
                            "https://notify.moe/anime/3L63cKimg"
                          ],
                          "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                          "type": "TV",
                          "episodes": 24,
                          "status": "FINISHED",
                          "animeSeason": {
                            "season": "SUMMER",
                            "year": 2009
                          },
                          "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                          "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                          "duration": {
                            "value": 1440,
                            "unit": "SECONDS"
                          },
                          "scores": [
                            {
                              "hostname": "myanimelist.net",
                              "value": 7.77,
                              "range": {
                                "minInclusive": 1.0,
                                "maxInclusive": 10.0
                              }
                            }
                          ],
                          "synonyms": [
                            "CLANNAD",
                            "Clannad (TV)",
                            "Clannad 1",
                            "Clannad TV",
                            "Kuranado",
                            "Кланад",
                            "Кланнад",
                            "خانواده",
                            "كلاناد",
                            "کلاناد",
                            "แคลนนาด",
                            "くらなど",
                            "クラナド",
                            "클라나드",
                            "ＣＬＡＮＮＡＤ -クラナド-"
                          ],
                          "relatedAnime": [
                            "https://anilist.co/anime/2167",
                            "https://anilist.co/anime/4059",
                            "https://anime-planet.com/anime/clannad",
                            "https://anime-planet.com/anime/clannad-after-story",
                            "https://anime-planet.com/anime/clannad-another-world-tomoyo-chapter",
                            "https://anime-planet.com/anime/clannad-movie",
                            "https://anisearch.com/anime/4199",
                            "https://kitsu.io/anime/1962",
                            "https://livechart.me/anime/10537",
                            "https://livechart.me/anime/10976",
                            "https://livechart.me/anime/3581",
                            "https://livechart.me/anime/3588",
                            "https://livechart.me/anime/3657",
                            "https://livechart.me/anime/3822",
                            "https://myanimelist.net/anime/2167",
                            "https://myanimelist.net/anime/4181",
                            "https://notify.moe/anime/F2eY5Fmig"
                          ],
                          "tags": [
                            "amnesia",
                            "asia",
                            "baseball",
                            "based on a visual novel",
                            "basketball",
                            "comedy",
                            "coming of age",
                            "daily life",
                            "delinquents",
                            "drama",
                            "earth",
                            "ensemble cast",
                            "romance"
                          ]
                        }
                    """.trimIndent()

                        // when
                        val result = Json.toJson(anime)

                        // then
                        assertThat(result).isEqualTo(expectedJson)
                    }
                }

                @Test
                fun `serialize AnimeRaw - default properties`() {
                    runBlocking {
                        // given
                        val anime = AnimeRaw("Death Note")

                        val expectedJson = """
                        {
                          "sources": [],
                          "title": "Death Note",
                          "type": "UNKNOWN",
                          "episodes": 0,
                          "status": "UNKNOWN",
                          "animeSeason": {
                            "season": "UNDEFINED",
                            "year": null
                          },
                          "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                          "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                          "duration": null,
                          "scores": [],
                          "synonyms": [],
                          "relatedAnime": [],
                          "tags": []
                        }
                    """.trimIndent()

                        // when
                        val result = Json.toJson(anime)

                        // then
                        assertThat(result).isEqualTo(expectedJson)
                    }
                }
            }

            @Nested
            inner class AnimeTests {

                @Test
                fun `serialize Anime - all properties set`() {
                    runBlocking {
                        // given
                        val anime = Anime(
                            title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                            sources = hashSetOf(
                                URI("https://livechart.me/anime/3681"),
                                URI("https://anisearch.com/anime/6826"),
                                URI("https://kitsu.io/anime/4529"),
                                URI("https://anime-planet.com/anime/clannad-another-world-kyou-chapter"),
                                URI("https://anilist.co/anime/6351"),
                                URI("https://notify.moe/anime/3L63cKimg"),
                                URI("https://myanimelist.net/anime/6351"),
                            ),
                            relatedAnime = hashSetOf(
                                URI("https://myanimelist.net/anime/4181"),
                                URI("https://anilist.co/anime/2167"),
                                URI("https://anime-planet.com/anime/clannad"),
                                URI("https://livechart.me/anime/10537"),
                                URI("https://anime-planet.com/anime/clannad-another-world-tomoyo-chapter"),
                                URI("https://livechart.me/anime/10976"),
                                URI("https://anime-planet.com/anime/clannad-movie"),
                                URI("https://anisearch.com/anime/4199"),
                                URI("https://notify.moe/anime/F2eY5Fmig"),
                                URI("https://livechart.me/anime/3581"),
                                URI("https://anime-planet.com/anime/clannad-after-story"),
                                URI("https://livechart.me/anime/3588"),
                                URI("https://myanimelist.net/anime/2167"),
                                URI("https://livechart.me/anime/3657"),
                                URI("https://anilist.co/anime/4059"),
                                URI("https://livechart.me/anime/3822"),
                                URI("https://kitsu.io/anime/1962"),
                            ),
                            type = TV,
                            episodes = 24,
                            status = FINISHED,
                            animeSeason = AnimeSeason(
                                season = SUMMER,
                                year = 2009,
                            ),
                            picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                            duration = Duration(24, MINUTES),
                            score = ScoreValue(
                                arithmeticGeometricMean = 1.29,
                                arithmeticMean = 2.38,
                                median = 3.47,
                            ),
                            synonyms = hashSetOf(
                                "Clannad (TV)",
                                "Kuranado",
                                "Clannad TV",
                                "CLANNAD",
                                "クラナド",
                                "Кланнад",
                                "Кланад",
                                "كلاناد",
                                "Clannad 1",
                                "클라나드",
                                "خانواده",
                                "کلاناد",
                                "แคลนนาด",
                                "くらなど",
                                "ＣＬＡＮＮＡＤ -クラナド-",
                            ),
                            tags = hashSetOf(
                                "baseball",
                                "based on a visual novel",
                                "basketball",
                                "amnesia",
                                "coming of age",
                                "asia",
                                "daily life",
                                "comedy",
                                "delinquents",
                                "earth",
                                "romance",
                                "ensemble cast",
                                "drama",
                            ),
                        )

                        val expectedJson = """
                        {
                          "sources": [
                            "https://anilist.co/anime/6351",
                            "https://anime-planet.com/anime/clannad-another-world-kyou-chapter",
                            "https://anisearch.com/anime/6826",
                            "https://kitsu.io/anime/4529",
                            "https://livechart.me/anime/3681",
                            "https://myanimelist.net/anime/6351",
                            "https://notify.moe/anime/3L63cKimg"
                          ],
                          "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                          "type": "TV",
                          "episodes": 24,
                          "status": "FINISHED",
                          "animeSeason": {
                            "season": "SUMMER",
                            "year": 2009
                          },
                          "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                          "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                          "duration": {
                            "value": 1440,
                            "unit": "SECONDS"
                          },
                          "score": {
                            "arithmeticGeometricMean": 1.29,
                            "arithmeticMean": 2.38,
                            "median": 3.47
                          },
                          "synonyms": [
                            "CLANNAD",
                            "Clannad (TV)",
                            "Clannad 1",
                            "Clannad TV",
                            "Kuranado",
                            "Кланад",
                            "Кланнад",
                            "خانواده",
                            "كلاناد",
                            "کلاناد",
                            "แคลนนาด",
                            "くらなど",
                            "クラナド",
                            "클라나드",
                            "ＣＬＡＮＮＡＤ -クラナド-"
                          ],
                          "relatedAnime": [
                            "https://anilist.co/anime/2167",
                            "https://anilist.co/anime/4059",
                            "https://anime-planet.com/anime/clannad",
                            "https://anime-planet.com/anime/clannad-after-story",
                            "https://anime-planet.com/anime/clannad-another-world-tomoyo-chapter",
                            "https://anime-planet.com/anime/clannad-movie",
                            "https://anisearch.com/anime/4199",
                            "https://kitsu.io/anime/1962",
                            "https://livechart.me/anime/10537",
                            "https://livechart.me/anime/10976",
                            "https://livechart.me/anime/3581",
                            "https://livechart.me/anime/3588",
                            "https://livechart.me/anime/3657",
                            "https://livechart.me/anime/3822",
                            "https://myanimelist.net/anime/2167",
                            "https://myanimelist.net/anime/4181",
                            "https://notify.moe/anime/F2eY5Fmig"
                          ],
                          "tags": [
                            "amnesia",
                            "asia",
                            "baseball",
                            "based on a visual novel",
                            "basketball",
                            "comedy",
                            "coming of age",
                            "daily life",
                            "delinquents",
                            "drama",
                            "earth",
                            "ensemble cast",
                            "romance"
                          ]
                        }
                    """.trimIndent()

                        // when
                        val result = Json.toJson(anime)

                        // then
                        assertThat(result).isEqualTo(expectedJson)
                    }
                }

                @Test
                fun `serialize Anime - default properties`() {
                    runBlocking {
                        // given
                        val anime = Anime("Death Note")

                        val expectedJson = """
                        {
                          "sources": [],
                          "title": "Death Note",
                          "type": "UNKNOWN",
                          "episodes": 0,
                          "status": "UNKNOWN",
                          "animeSeason": {
                            "season": "UNDEFINED",
                            "year": null
                          },
                          "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                          "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                          "duration": null,
                          "score": null,
                          "synonyms": [],
                          "relatedAnime": [],
                          "tags": []
                        }
                    """.trimIndent()

                        // when
                        val result = Json.toJson(anime)

                        // then
                        assertThat(result).isEqualTo(expectedJson)
                    }
                }
            }

            @Test
            fun `serialize - option serialize null is activated by default`() {
                runBlocking {
                    // given
                    val expectedJson = """
                        {
                          "nullableString": null,
                          "nonNullableString": "test"
                        }
                    """.trimIndent()

                    // when
                    val result = Json.toJson(NullableTestClass())

                    // then
                    assertThat(result).isEqualTo(expectedJson)
                }
            }
        }

        @Nested
        inner class CustomOptionsTests {

            @Nested
            inner class AnimeRawTests {

                @Test
                fun `serialize AnimeRaw - deactivate pretty print`() {
                    runBlocking {
                        // given
                        val anime = AnimeRaw(
                            _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                            _sources = hashSetOf(
                                URI("https://myanimelist.net/anime/6351"),
                            ),
                            _relatedAnime = hashSetOf(
                                URI("https://myanimelist.net/anime/2167")
                            ),
                            type = TV,
                            episodes = 24,
                            status = FINISHED,
                            animeSeason = AnimeSeason(
                                season = SUMMER,
                                year = 2009,
                            ),
                            picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                            duration = Duration(24, MINUTES),
                            _synonyms = hashSetOf(
                                "Clannad ~After Story~: Another World, Kyou Chapter",
                                "Clannad: After Story OVA",
                                "クラナド　アフターストーリー　もうひとつの世界　杏編",
                            ),
                            _tags = hashSetOf(
                                "comedy",
                                "romance",
                            ),
                        ).addScores(
                            MetaDataProviderScoreValue(
                                hostname = "myanimelist.net",
                                value = 7.77,
                                range = 1.0..10.0,
                            ),
                        )

                        val expectedJson = """{"sources":["https://myanimelist.net/anime/6351"],"title":"Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen","type":"TV","episodes":24,"status":"FINISHED","animeSeason":{"season":"SUMMER","year":2009},"picture":"https://cdn.myanimelist.net/images/anime/10/19621.jpg","thumbnail":"https://cdn.myanimelist.net/images/anime/10/19621t.jpg","duration":{"value":1440,"unit":"SECONDS"},"scores":[{"hostname":"myanimelist.net","value":7.77,"range":{"minInclusive":1.0,"maxInclusive":10.0}}],"synonyms":["Clannad ~After Story~: Another World, Kyou Chapter","Clannad: After Story OVA","クラナド　アフターストーリー　もうひとつの世界　杏編"],"relatedAnime":["https://myanimelist.net/anime/2167"],"tags":["comedy","romance"]}""".trimIndent()

                        // when
                        val result = Json.toJson(anime, DEACTIVATE_PRETTY_PRINT)

                        // then
                        assertThat(result).isEqualTo(expectedJson)
                    }
                }

                @Test
                fun `serialize AnimeRaw - deactivate serialize null`() {
                    runBlocking {
                        // given
                        val anime = AnimeRaw(
                            _title = "Title",
                            animeSeason = AnimeSeason(
                                season = SUMMER,
                                year = 0,
                            ),
                        )

                        val expectedJson = """
                        {
                          "sources": [],
                          "title": "Title",
                          "type": "UNKNOWN",
                          "episodes": 0,
                          "status": "UNKNOWN",
                          "animeSeason": {
                            "season": "SUMMER"
                          },
                          "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                          "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                          "scores": [],
                          "synonyms": [],
                          "relatedAnime": [],
                          "tags": []
                        }
                    """.trimIndent()

                        // when
                        val result = Json.toJson(anime, DEACTIVATE_SERIALIZE_NULL)

                        // then
                        assertThat(result).isEqualTo(expectedJson)
                    }
                }
            }

            @Nested
            inner class AnimeTests {

                @Test
                fun `serialize Anime - deactivate pretty print`() {
                    runBlocking {
                        // given
                        val anime = Anime(
                            title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                            sources = hashSetOf(
                                URI("https://myanimelist.net/anime/6351"),
                            ),
                            relatedAnime = hashSetOf(
                                URI("https://myanimelist.net/anime/2167")
                            ),
                            type = TV,
                            episodes = 24,
                            status = FINISHED,
                            animeSeason = AnimeSeason(
                                season = SUMMER,
                                year = 2009,
                            ),
                            picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                            duration = Duration(24, MINUTES),
                            score = ScoreValue(
                                arithmeticGeometricMean = 1.29,
                                arithmeticMean = 2.38,
                                median = 3.47,
                            ),
                            synonyms = hashSetOf(
                                "Clannad ~After Story~: Another World, Kyou Chapter",
                                "Clannad: After Story OVA",
                                "クラナド　アフターストーリー　もうひとつの世界　杏編",
                            ),
                            tags = hashSetOf(
                                "comedy",
                                "romance",
                            ),
                        )

                        val expectedJson = """{"sources":["https://myanimelist.net/anime/6351"],"title":"Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen","type":"TV","episodes":24,"status":"FINISHED","animeSeason":{"season":"SUMMER","year":2009},"picture":"https://cdn.myanimelist.net/images/anime/10/19621.jpg","thumbnail":"https://cdn.myanimelist.net/images/anime/10/19621t.jpg","duration":{"value":1440,"unit":"SECONDS"},"score":{"arithmeticGeometricMean":1.29,"arithmeticMean":2.38,"median":3.47},"synonyms":["Clannad ~After Story~: Another World, Kyou Chapter","Clannad: After Story OVA","クラナド　アフターストーリー　もうひとつの世界　杏編"],"relatedAnime":["https://myanimelist.net/anime/2167"],"tags":["comedy","romance"]}""".trimIndent()

                        // when
                        val result = Json.toJson(anime, DEACTIVATE_PRETTY_PRINT)

                        // then
                        assertThat(result).isEqualTo(expectedJson)
                    }
                }

                @Test
                fun `serialize Anime - deactivate serialize null`() {
                    runBlocking {
                        // given
                        val anime = Anime(
                            title = "Title",
                            animeSeason = AnimeSeason(
                                season = SUMMER,
                                year = 0,
                            ),
                            score = NoScore,
                        )

                        val expectedJson = """
                        {
                          "sources": [],
                          "title": "Title",
                          "type": "UNKNOWN",
                          "episodes": 0,
                          "status": "UNKNOWN",
                          "animeSeason": {
                            "season": "SUMMER"
                          },
                          "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                          "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                          "synonyms": [],
                          "relatedAnime": [],
                          "tags": []
                        }
                    """.trimIndent()

                        // when
                        val result = Json.toJson(anime, DEACTIVATE_SERIALIZE_NULL)

                        // then
                        assertThat(result).isEqualTo(expectedJson)
                    }
                }
            }
        }

        @Nested
        inner class DefaultFormatTests {

            @Test
            fun `serialize Anime - default dataset`() {
                runBlocking {
                    // given
                    val anime = Anime(
                        title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/6351"),
                        ),
                        relatedAnime = hashSetOf(
                            URI("https://myanimelist.net/anime/2167")
                        ),
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SUMMER,
                            year = 2009,
                        ),
                        picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                        duration = Duration(24, MINUTES),
                        score = ScoreValue(
                            arithmeticGeometricMean = 1.29,
                            arithmeticMean = 2.38,
                            median = 3.47,
                        ),
                        synonyms = hashSetOf(
                            "Clannad ~After Story~: Another World, Kyou Chapter",
                            "Clannad: After Story OVA",
                            "クラナド　アフターストーリー　もうひとつの世界　杏編",
                        ),
                        tags = hashSetOf(
                            "comedy",
                            "romance",
                        ),
                    )

                    val expectedJson = """
                        {
                          "sources": [
                            "https://myanimelist.net/anime/6351"
                          ],
                          "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                          "type": "TV",
                          "episodes": 24,
                          "status": "FINISHED",
                          "animeSeason": {
                            "season": "SUMMER",
                            "year": 2009
                          },
                          "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                          "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                          "duration": {
                            "value": 1440,
                            "unit": "SECONDS"
                          },
                          "score": {
                            "arithmeticGeometricMean": 1.29,
                            "arithmeticMean": 2.38,
                            "median": 3.47
                          },
                          "synonyms": [
                            "Clannad ~After Story~: Another World, Kyou Chapter",
                            "Clannad: After Story OVA",
                            "クラナド　アフターストーリー　もうひとつの世界　杏編"
                          ],
                          "relatedAnime": [
                            "https://myanimelist.net/anime/2167"
                          ],
                          "tags": [
                            "comedy",
                            "romance"
                          ]
                        }
                    """.trimIndent()

                    // when
                    val result = Json.toJson(anime)

                    // then
                    assertThat(result).isEqualTo(expectedJson)
                }
            }

            @Test
            fun `serialize Anime - minified dataset`() {
                runBlocking {
                    // given
                    val anime = Anime(
                        title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/6351"),
                        ),
                        relatedAnime = hashSetOf(
                            URI("https://myanimelist.net/anime/2167")
                        ),
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SUMMER,
                            year = 2009,
                        ),
                        picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                        duration = Duration(24, MINUTES),
                        score = ScoreValue(
                            arithmeticGeometricMean = 1.29,
                            arithmeticMean = 2.38,
                            median = 3.47,
                        ),
                        synonyms = hashSetOf(
                            "Clannad ~After Story~: Another World, Kyou Chapter",
                            "Clannad: After Story OVA",
                            "クラナド　アフターストーリー　もうひとつの世界　杏編",
                        ),
                        tags = hashSetOf(
                            "comedy",
                            "romance",
                        ),
                    )

                    val expectedJson = """{"sources":["https://myanimelist.net/anime/6351"],"title":"Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen","type":"TV","episodes":24,"status":"FINISHED","animeSeason":{"season":"SUMMER","year":2009},"picture":"https://cdn.myanimelist.net/images/anime/10/19621.jpg","thumbnail":"https://cdn.myanimelist.net/images/anime/10/19621t.jpg","duration":{"value":1440,"unit":"SECONDS"},"score":{"arithmeticGeometricMean":1.29,"arithmeticMean":2.38,"median":3.47},"synonyms":["Clannad ~After Story~: Another World, Kyou Chapter","Clannad: After Story OVA","クラナド　アフターストーリー　もうひとつの世界　杏編"],"relatedAnime":["https://myanimelist.net/anime/2167"],"tags":["comedy","romance"]}""".trimIndent()

                    // when
                    val result = Json.toJson(anime, DEACTIVATE_PRETTY_PRINT)

                    // then
                    assertThat(result).isEqualTo(expectedJson)
                }
            }

            @Test
            fun `serialize AnimeRaw - DCS`() {
                runBlocking {
                    // given
                    val anime = AnimeRaw(
                        _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                        _sources = hashSetOf(
                            URI("https://myanimelist.net/anime/6351"),
                        ),
                        _relatedAnime = hashSetOf(
                            URI("https://myanimelist.net/anime/2167")
                        ),
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SUMMER,
                            year = 2009,
                        ),
                        picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                        duration = Duration(24, MINUTES),
                        _synonyms = hashSetOf(
                            "Clannad ~After Story~: Another World, Kyou Chapter",
                            "Clannad: After Story OVA",
                            "クラナド　アフターストーリー　もうひとつの世界　杏編",
                        ),
                        _tags = hashSetOf(
                            "comedy",
                            "romance",
                        ),
                    ).addScores(
                        MetaDataProviderScoreValue(
                            hostname = "myanimelist.net",
                            value = 7.77,
                            range = 1.0..10.0,
                        ),
                    )

                    val expectedJson = """
                        {
                          "sources": [
                            "https://myanimelist.net/anime/6351"
                          ],
                          "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                          "type": "TV",
                          "episodes": 24,
                          "status": "FINISHED",
                          "animeSeason": {
                            "season": "SUMMER",
                            "year": 2009
                          },
                          "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                          "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                          "duration": {
                            "value": 1440,
                            "unit": "SECONDS"
                          },
                          "scores": [
                            {
                              "hostname": "myanimelist.net",
                              "value": 7.77,
                              "range": {
                                "minInclusive": 1.0,
                                "maxInclusive": 10.0
                              }
                            }
                          ],
                          "synonyms": [
                            "Clannad ~After Story~: Another World, Kyou Chapter",
                            "Clannad: After Story OVA",
                            "クラナド　アフターストーリー　もうひとつの世界　杏編"
                          ],
                          "relatedAnime": [
                            "https://myanimelist.net/anime/2167"
                          ],
                          "tags": [
                            "comedy",
                            "romance"
                          ]
                        }
                    """.trimIndent()

                    // when
                    val result = Json.toJson(anime)

                    // then
                    assertThat(result).isEqualTo(expectedJson)
                }
            }
        }
    }
}

private data class NullableTestClass(val nullableString: String? = null, val nonNullableString: String = "test")
private data class ClassWithList(val nonNullableList: List<String> = emptyList())