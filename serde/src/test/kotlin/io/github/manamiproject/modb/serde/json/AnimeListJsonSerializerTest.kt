package io.github.manamiproject.modb.serde.json

import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.AnimeSeason
import io.github.manamiproject.modb.core.models.Duration
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset.UTC
import kotlin.test.Test

internal class AnimeListJsonSerializerTest {

    @Nested
    inner class AnimeOfflineDatabaseTests {

        @Test
        fun `serialize default anime pretty print`() {
            runBlocking {
                // given
                val expectedContent = """
                    {
                      "license": {
                        "name": "GNU Affero General Public License v3.0",
                        "url": "https://github.com/manami-project/anime-offline-database/blob/master/LICENSE"
                      },
                      "repository": "https://github.com/manami-project/anime-offline-database",
                      "lastUpdate": "2020-01-01",
                      "data": [
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
                          "synonyms": [],
                          "relatedAnime": [],
                          "tags": []
                        }
                      ]
                    }
                """.trimIndent()

                val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                val serializer = AnimeListJsonSerializer(clock)

                val animeList = listOf(
                    Anime("Death Note"),
                )

                // when
                val result = serializer.serialize(animeList, minify = false)

                // then
                assertThat(result).isEqualTo(expectedContent)
            }
        }

        @Test
        fun `serialize default anime minified`() {
            runBlocking {
                // given
                val expectedContent = """
                    {"license":{"name":"GNU Affero General Public License v3.0","url":"https://github.com/manami-project/anime-offline-database/blob/master/LICENSE"},"repository":"https://github.com/manami-project/anime-offline-database","lastUpdate":"2020-01-01","data":[{"sources":[],"title":"Death Note","type":"UNKNOWN","episodes":0,"status":"UNKNOWN","animeSeason":{"season":"UNDEFINED"},"picture":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png","thumbnail":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png","synonyms":[],"relatedAnime":[],"tags":[]}]}
                """.trimIndent()

                val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                val serializer = AnimeListJsonSerializer(clock)

                val animeList = listOf(
                    Anime("Death Note"),
                )

                // when
                val result = serializer.serialize(
                    obj = animeList,
                    minify = true,
                )

                // then
                assertThat(result).isEqualTo(expectedContent)
            }
        }

        @Test
        fun `serialize anime having all properties set`() {
            runBlocking {
                // given
                val expectedContent = """
                    {
                      "license": {
                        "name": "GNU Affero General Public License v3.0",
                        "url": "https://github.com/manami-project/anime-offline-database/blob/master/LICENSE"
                      },
                      "repository": "https://github.com/manami-project/anime-offline-database",
                      "lastUpdate": "2020-01-01",
                      "data": [
                        {
                          "sources": [
                            "https://myanimelist.net/anime/6351"
                          ],
                          "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                          "type": "SPECIAL",
                          "episodes": 1,
                          "status": "FINISHED",
                          "animeSeason": {
                            "season": "SUMMER",
                            "year": 2009
                          },
                          "picture": "https://cdn.myanimelist.net/images/anime/10/19621.jpg",
                          "thumbnail": "https://cdn.myanimelist.net/images/anime/10/19621t.jpg",
                          "duration": {
                            "value": 3600,
                            "unit": "SECONDS"
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
                            "drama",
                            "romance",
                            "school",
                            "slice of life",
                            "supernatural"
                          ]
                        }
                      ]
                    }
                """.trimIndent()


                val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                val serializer = AnimeListJsonSerializer(clock)

                val animeList = listOf(
                    Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/6351"),
                        ),
                        _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                        type = Anime.Type.SPECIAL,
                        episodes = 1,
                        status = Anime.Status.FINISHED,
                        animeSeason = AnimeSeason(
                            season = AnimeSeason.Season.SUMMER,
                            year = 2009
                        ),
                        picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                        duration = Duration(
                            value = 1,
                            unit = Duration.TimeUnit.HOURS,
                        ),
                        relatedAnime = hashSetOf(
                            URI("https://myanimelist.net/anime/2167"),
                        ),
                        synonyms = hashSetOf(
                            "Clannad ~After Story~: Another World, Kyou Chapter",
                            "Clannad: After Story OVA",
                            "クラナド　アフターストーリー　もうひとつの世界　杏編",
                        ),
                        tags = hashSetOf(
                            "comedy",
                            "drama",
                            "romance",
                            "school",
                            "slice of life",
                            "supernatural",
                        ),
                    )
                )

                // when
                val result = serializer.serialize(animeList, minify = false)

                // then
                assertThat(result).isEqualTo(expectedContent)
            }
        }
    }

    @Nested
    inner class AnimeOfflineDatabaseSortingTests {

        @Test
        fun `prio 1 - sort by title`() {
            runBlocking {
                // given
                val expectedContent = """
                    {
                      "license": {
                        "name": "GNU Affero General Public License v3.0",
                        "url": "https://github.com/manami-project/anime-offline-database/blob/master/LICENSE"
                      },
                      "repository": "https://github.com/manami-project/anime-offline-database",
                      "lastUpdate": "2020-01-01",
                      "data": [
                        {
                          "sources": [],
                          "title": "A",
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
                          "synonyms": [],
                          "relatedAnime": [],
                          "tags": []
                        },
                        {
                          "sources": [],
                          "title": "B",
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
                          "synonyms": [],
                          "relatedAnime": [],
                          "tags": []
                        },
                        {
                          "sources": [],
                          "title": "C",
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
                          "synonyms": [],
                          "relatedAnime": [],
                          "tags": []
                        }
                      ]
                    }
                """.trimIndent()

                val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                val serializer = AnimeListJsonSerializer(clock)

                val animeList = listOf(
                    Anime("B"),
                    Anime("C"),
                    Anime("A"),
                )

                // when
                val result = serializer.serialize(animeList, minify = false)

                // then
                assertThat(result).isEqualTo(expectedContent)
            }
        }

        @Test
        fun `prio 2 - sort by type`() {
            runBlocking {
                // given
                val expectedContent = """
                    {
                      "license": {
                        "name": "GNU Affero General Public License v3.0",
                        "url": "https://github.com/manami-project/anime-offline-database/blob/master/LICENSE"
                      },
                      "repository": "https://github.com/manami-project/anime-offline-database",
                      "lastUpdate": "2020-01-01",
                      "data": [
                        {
                          "sources": [],
                          "title": "test",
                          "type": "MOVIE",
                          "episodes": 0,
                          "status": "UNKNOWN",
                          "animeSeason": {
                            "season": "UNDEFINED",
                            "year": null
                          },
                          "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                          "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                          "duration": null,
                          "synonyms": [],
                          "relatedAnime": [],
                          "tags": []
                        },
                        {
                          "sources": [],
                          "title": "test",
                          "type": "OVA",
                          "episodes": 0,
                          "status": "UNKNOWN",
                          "animeSeason": {
                            "season": "UNDEFINED",
                            "year": null
                          },
                          "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                          "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                          "duration": null,
                          "synonyms": [],
                          "relatedAnime": [],
                          "tags": []
                        },
                        {
                          "sources": [],
                          "title": "test",
                          "type": "SPECIAL",
                          "episodes": 0,
                          "status": "UNKNOWN",
                          "animeSeason": {
                            "season": "UNDEFINED",
                            "year": null
                          },
                          "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                          "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                          "duration": null,
                          "synonyms": [],
                          "relatedAnime": [],
                          "tags": []
                        }
                      ]
                    }
                """.trimIndent()

                val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                val serializer = AnimeListJsonSerializer(clock)

                val animeList = listOf(
                    Anime(
                        _title = "test",
                        type = Anime.Type.OVA,
                    ),
                    Anime(
                        _title = "test",
                        type = Anime.Type.SPECIAL,
                    ),
                    Anime(
                        _title = "test",
                        type = Anime.Type.MOVIE,
                    ),
                )

                // when
                val result = serializer.serialize(animeList, minify = false)

                // then
                assertThat(result).isEqualTo(expectedContent)
            }
        }

        @Test
        fun `prio 3 - sort by episodes`() {
            runBlocking {
                // given
                val expectedContent = """
                    {
                      "license": {
                        "name": "GNU Affero General Public License v3.0",
                        "url": "https://github.com/manami-project/anime-offline-database/blob/master/LICENSE"
                      },
                      "repository": "https://github.com/manami-project/anime-offline-database",
                      "lastUpdate": "2020-01-01",
                      "data": [
                        {
                          "sources": [],
                          "title": "test",
                          "type": "TV",
                          "episodes": 12,
                          "status": "UNKNOWN",
                          "animeSeason": {
                            "season": "UNDEFINED",
                            "year": null
                          },
                          "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                          "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                          "duration": null,
                          "synonyms": [],
                          "relatedAnime": [],
                          "tags": []
                        },
                        {
                          "sources": [],
                          "title": "test",
                          "type": "TV",
                          "episodes": 13,
                          "status": "UNKNOWN",
                          "animeSeason": {
                            "season": "UNDEFINED",
                            "year": null
                          },
                          "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                          "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                          "duration": null,
                          "synonyms": [],
                          "relatedAnime": [],
                          "tags": []
                        },
                        {
                          "sources": [],
                          "title": "test",
                          "type": "TV",
                          "episodes": 24,
                          "status": "UNKNOWN",
                          "animeSeason": {
                            "season": "UNDEFINED",
                            "year": null
                          },
                          "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                          "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                          "duration": null,
                          "synonyms": [],
                          "relatedAnime": [],
                          "tags": []
                        }
                      ]
                    }
                """.trimIndent()

                val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                val serializer = AnimeListJsonSerializer(clock)

                val animeList = listOf(
                    Anime(
                        _title = "test",
                        type = Anime.Type.TV,
                        episodes = 24,
                    ),
                    Anime(
                        _title = "test",
                        type = Anime.Type.TV,
                        episodes = 12,
                    ),
                    Anime(
                        _title = "test",
                        type = Anime.Type.TV,
                        episodes = 13,
                    ),
                )

                // when
                val result = serializer.serialize(animeList, minify = false)

                // then
                assertThat(result).isEqualTo(expectedContent)
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = AnimeListJsonSerializer.instance

            // when
            val result = AnimeListJsonSerializer.instance

            // then
            assertThat(result).isExactlyInstanceOf(AnimeListJsonSerializer::class.java)
            assertThat(result===previous).isTrue()
        }
    }
}