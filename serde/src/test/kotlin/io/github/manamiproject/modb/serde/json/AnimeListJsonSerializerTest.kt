package io.github.manamiproject.modb.serde.json

import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.serde.TestAnimeObjects
import io.github.manamiproject.modb.serde.createExpectedDatasetMinified
import io.github.manamiproject.modb.serde.createExpectedDatasetPrettyPrint
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset.UTC
import kotlin.test.Test

internal class AnimeListJsonSerializerTest {

    @Nested
    inner class SerializeJsonTests {

        @Nested
        inner class AnimeOfflineDatabaseTests {

            @Test
            fun `serialize default anime - pretty print`() {
                runBlocking {
                    // given
                    val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                    val serializer = AnimeListJsonSerializer(clock)

                    val animeList = listOf(
                        TestAnimeObjects.DefaultAnime.obj,
                    )

                    // when
                    val result = serializer.serializeJson(animeList, minify = false)

                    // then
                    assertThat(result).isEqualTo(createExpectedDatasetPrettyPrint(TestAnimeObjects.DefaultAnime.serializedPrettyPrint))
                }
            }

            @Test
            fun `serialize default anime - minified`() {
                runBlocking {
                    // given
                    val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                    val serializer = AnimeListJsonSerializer(clock)

                    val animeList = listOf(
                        TestAnimeObjects.DefaultAnime.obj,
                    )

                    // when
                    val result = serializer.serializeJson(
                        obj = animeList,
                        minify = true,
                    )

                    // then
                    assertThat(result).isEqualTo(createExpectedDatasetMinified(TestAnimeObjects.DefaultAnime.serializedMinified))
                }
            }

            @Test
            fun `serialize anime having all properties set - pretty print`() {
                runBlocking {
                    // given
                    val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                    val serializer = AnimeListJsonSerializer(clock)

                    val animeList = listOf(
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    // when
                    val result = serializer.serializeJson(animeList, minify = false)

                    // then
                    assertThat(result).isEqualTo(createExpectedDatasetPrettyPrint(TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint))
                }
            }

            @Test
            fun `serialize anime having all properties set - minified`() {
                runBlocking {
                    // given
                    val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                    val serializer = AnimeListJsonSerializer(clock)

                    val animeList = listOf(
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    // when
                    val result = serializer.serializeJson(animeList, minify = true)

                    // then
                    assertThat(result).isEqualTo(createExpectedDatasetMinified(TestAnimeObjects.AllPropertiesSet.serializedMinified))
                }
            }
        }

        @Nested
        inner class AnimeOfflineDatabaseSortingTests {

            @Test
            fun `prio 1 - sort by title`() {
                runBlocking {
                    // given
                    val expectedContent = createExpectedDatasetPrettyPrint("""
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
                      "score": null,
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
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
                      "score": null,
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
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
                      "score": null,
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())

                    val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                    val serializer = AnimeListJsonSerializer(clock)

                    val animeList = listOf(
                        Anime("B"),
                        Anime("C"),
                        Anime("A"),
                    )

                    // when
                    val result = serializer.serializeJson(animeList, minify = false)

                    // then
                    assertThat(result).isEqualTo(expectedContent)
                }
            }

            @Test
            fun `prio 2 - sort by type`() {
                runBlocking {
                    // given
                    val expectedContent = createExpectedDatasetPrettyPrint("""
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
                      "score": null,
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
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
                      "score": null,
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
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
                      "score": null,
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())

                    val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                    val serializer = AnimeListJsonSerializer(clock)

                    val animeList = listOf(
                        Anime(
                            title = "test",
                            type = OVA,
                        ),
                        Anime(
                            title = "test",
                            type = SPECIAL,
                        ),
                        Anime(
                            title = "test",
                            type = MOVIE,
                        ),
                    )

                    // when
                    val result = serializer.serializeJson(animeList, minify = false)

                    // then
                    assertThat(result).isEqualTo(expectedContent)
                }
            }

            @Test
            fun `prio 3 - sort by episodes`() {
                runBlocking {
                    // given
                    val expectedContent = createExpectedDatasetPrettyPrint("""
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
                      "score": null,
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
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
                      "score": null,
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
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
                      "score": null,
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())

                    val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                    val serializer = AnimeListJsonSerializer(clock)

                    val animeList = listOf(
                        Anime(
                            title = "test",
                            type = TV,
                            episodes = 24,
                        ),
                        Anime(
                            title = "test",
                            type = TV,
                            episodes = 12,
                        ),
                        Anime(
                            title = "test",
                            type = TV,
                            episodes = 13,
                        ),
                    )

                    // when
                    val result = serializer.serializeJson(animeList, minify = false)

                    // then
                    assertThat(result).isEqualTo(expectedContent)
                }
            }
        }
    }

    @Nested
    inner class SerializeJsonLineTests {

        @Test
        fun `correctly serialize anime`() {
            runBlocking {
                // given
                val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                val serializer = AnimeListJsonSerializer(clock)
                val animeList = listOf(
                    TestAnimeObjects.DefaultAnime.obj,
                    TestAnimeObjects.NullableNotSet.obj,
                    TestAnimeObjects.AllPropertiesSet.obj,
                )

                val expectedOutput = """
                    {"${"$"}schema":"","license":{"name":"Open Data Commons Open Database License (ODbL) v1.0 + Database Contents License (DbCL) v1.0","url":"https://github.com/manami-project/anime-offline-database/blob/2020-01/LICENSE"},"repository":"https://github.com/manami-project/anime-offline-database","scoreRange":{"minInclusive":1.0,"maxInclusive":10.0},"lastUpdate":"2020-01-01"}
                    ${TestAnimeObjects.AllPropertiesSet.serializedMinified}
                    ${TestAnimeObjects.NullableNotSet.serializedMinified}
                    ${TestAnimeObjects.DefaultAnime.serializedMinified}
                """.trimIndent()

                // when
                val result = serializer.serializeJsonLine(animeList)

                // then
                assertThat(result).isEqualTo(expectedOutput)
            }
        }

        @Nested
        inner class AnimeOfflineDatabaseSortingTests {

            @Test
            fun `prio 1 - sort by title`() {
                runBlocking {
                    // given
                    val expectedContent = """
                        {"${"$"}schema":"","license":{"name":"Open Data Commons Open Database License (ODbL) v1.0 + Database Contents License (DbCL) v1.0","url":"https://github.com/manami-project/anime-offline-database/blob/2020-01/LICENSE"},"repository":"https://github.com/manami-project/anime-offline-database","scoreRange":{"minInclusive":1.0,"maxInclusive":10.0},"lastUpdate":"2020-01-01"}
                        {"sources":[],"title":"A","type":"UNKNOWN","episodes":0,"status":"UNKNOWN","animeSeason":{"season":"UNDEFINED"},"picture":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png","thumbnail":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png","synonyms":[],"studios":[],"producers":[],"relatedAnime":[],"tags":[]}
                        {"sources":[],"title":"B","type":"UNKNOWN","episodes":0,"status":"UNKNOWN","animeSeason":{"season":"UNDEFINED"},"picture":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png","thumbnail":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png","synonyms":[],"studios":[],"producers":[],"relatedAnime":[],"tags":[]}
                        {"sources":[],"title":"C","type":"UNKNOWN","episodes":0,"status":"UNKNOWN","animeSeason":{"season":"UNDEFINED"},"picture":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png","thumbnail":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png","synonyms":[],"studios":[],"producers":[],"relatedAnime":[],"tags":[]}
                    """.trimIndent()

                    val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                    val serializer = AnimeListJsonSerializer(clock)

                    val animeList = listOf(
                        Anime("B"),
                        Anime("C"),
                        Anime("A"),
                    )

                    // when
                    val result = serializer.serializeJsonLine(animeList)

                    // then
                    assertThat(result).isEqualTo(expectedContent)
                }
            }

            @Test
            fun `prio 2 - sort by type`() {
                runBlocking {
                    // given
                    val expectedContent = """
                        {"${"$"}schema":"","license":{"name":"Open Data Commons Open Database License (ODbL) v1.0 + Database Contents License (DbCL) v1.0","url":"https://github.com/manami-project/anime-offline-database/blob/2020-01/LICENSE"},"repository":"https://github.com/manami-project/anime-offline-database","scoreRange":{"minInclusive":1.0,"maxInclusive":10.0},"lastUpdate":"2020-01-01"}
                        {"sources":[],"title":"test","type":"MOVIE","episodes":0,"status":"UNKNOWN","animeSeason":{"season":"UNDEFINED"},"picture":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png","thumbnail":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png","synonyms":[],"studios":[],"producers":[],"relatedAnime":[],"tags":[]}
                        {"sources":[],"title":"test","type":"OVA","episodes":0,"status":"UNKNOWN","animeSeason":{"season":"UNDEFINED"},"picture":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png","thumbnail":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png","synonyms":[],"studios":[],"producers":[],"relatedAnime":[],"tags":[]}
                        {"sources":[],"title":"test","type":"SPECIAL","episodes":0,"status":"UNKNOWN","animeSeason":{"season":"UNDEFINED"},"picture":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png","thumbnail":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png","synonyms":[],"studios":[],"producers":[],"relatedAnime":[],"tags":[]}
                    """.trimIndent()

                    val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                    val serializer = AnimeListJsonSerializer(clock)

                    val animeList = listOf(
                        Anime(
                            title = "test",
                            type = OVA,
                        ),
                        Anime(
                            title = "test",
                            type = SPECIAL,
                        ),
                        Anime(
                            title = "test",
                            type = MOVIE,
                        ),
                    )

                    // when
                    val result = serializer.serializeJsonLine(animeList)

                    // then
                    assertThat(result).isEqualTo(expectedContent)
                }
            }

            @Test
            fun `prio 3 - sort by episodes`() {
                runBlocking {
                    // given
                    val expectedContent = """
                        {"${"$"}schema":"","license":{"name":"Open Data Commons Open Database License (ODbL) v1.0 + Database Contents License (DbCL) v1.0","url":"https://github.com/manami-project/anime-offline-database/blob/2020-01/LICENSE"},"repository":"https://github.com/manami-project/anime-offline-database","scoreRange":{"minInclusive":1.0,"maxInclusive":10.0},"lastUpdate":"2020-01-01"}
                        {"sources":[],"title":"test","type":"TV","episodes":12,"status":"UNKNOWN","animeSeason":{"season":"UNDEFINED"},"picture":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png","thumbnail":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png","synonyms":[],"studios":[],"producers":[],"relatedAnime":[],"tags":[]}
                        {"sources":[],"title":"test","type":"TV","episodes":13,"status":"UNKNOWN","animeSeason":{"season":"UNDEFINED"},"picture":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png","thumbnail":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png","synonyms":[],"studios":[],"producers":[],"relatedAnime":[],"tags":[]}
                        {"sources":[],"title":"test","type":"TV","episodes":24,"status":"UNKNOWN","animeSeason":{"season":"UNDEFINED"},"picture":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png","thumbnail":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png","synonyms":[],"studios":[],"producers":[],"relatedAnime":[],"tags":[]}
                    """.trimIndent()

                    val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                    val serializer = AnimeListJsonSerializer(clock)

                    val animeList = listOf(
                        Anime(
                            title = "test",
                            type = TV,
                            episodes = 24,
                        ),
                        Anime(
                            title = "test",
                            type = TV,
                            episodes = 12,
                        ),
                        Anime(
                            title = "test",
                            type = TV,
                            episodes = 13,
                        ),
                    )

                    // when
                    val result = serializer.serializeJsonLine(animeList)

                    // then
                    assertThat(result).isEqualTo(expectedContent)
                }
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