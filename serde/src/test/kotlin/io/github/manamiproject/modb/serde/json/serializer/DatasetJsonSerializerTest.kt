package io.github.manamiproject.modb.serde.json.serializer

import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeType
import io.github.manamiproject.modb.serde.TestAnimeObjects
import io.github.manamiproject.modb.serde.createExpectedDatasetMinified
import io.github.manamiproject.modb.serde.createExpectedDatasetPrettyPrint
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test

internal class DatasetJsonSerializerTest {

    @Nested
    inner class SerializeTests {

        @Nested
        inner class AnimeOfflineDatabaseTests {

            @Test
            fun `serialize default anime - pretty print`() {
                runTest {
                    // given
                    val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), ZoneOffset.UTC)
                    val serializer = DatasetJsonSerializer(clock)

                    val animeList = listOf(
                        TestAnimeObjects.DefaultAnime.obj,
                    )

                    // when
                    val result = serializer.serialize(animeList, minify = false)

                    // then
                    assertThat(result).isEqualTo(createExpectedDatasetPrettyPrint(TestAnimeObjects.DefaultAnime.serializedPrettyPrint))
                }
            }

            @Test
            fun `serialize default anime - minified`() {
                runTest {
                    // given
                    val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), ZoneOffset.UTC)
                    val serializer = DatasetJsonSerializer(clock)

                    val animeList = listOf(
                        TestAnimeObjects.DefaultAnime.obj,
                    )

                    // when
                    val result = serializer.serialize(
                        obj = animeList,
                        minify = true,
                    )

                    // then
                    assertThat(result).isEqualTo(createExpectedDatasetMinified(TestAnimeObjects.DefaultAnime.serializedMinified))
                }
            }

            @Test
            fun `serialize anime having all properties set - pretty print`() {
                runTest {
                    // given
                    val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), ZoneOffset.UTC)
                    val serializer = DatasetJsonSerializer(clock)

                    val animeList = listOf(
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    // when
                    val result = serializer.serialize(animeList, minify = false)

                    // then
                    assertThat(result).isEqualTo(createExpectedDatasetPrettyPrint(TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint))
                }
            }

            @Test
            fun `serialize anime having all properties set - minified`() {
                runTest {
                    // given
                    val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), ZoneOffset.UTC)
                    val serializer = DatasetJsonSerializer(clock)

                    val animeList = listOf(
                        TestAnimeObjects.AllPropertiesSet.obj,
                    )

                    // when
                    val result = serializer.serialize(animeList, minify = true)

                    // then
                    assertThat(result).isEqualTo(createExpectedDatasetMinified(TestAnimeObjects.AllPropertiesSet.serializedMinified))
                }
            }
        }

        @Nested
        inner class AnimeOfflineDatabaseSortingTests {

            @Test
            fun `prio 1 - sort by title`() {
                runTest {
                    // given
                    val expectedContent = createExpectedDatasetPrettyPrint(
                        """
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
                """.trimIndent()
                    )

                    val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), ZoneOffset.UTC)
                    val serializer = DatasetJsonSerializer(clock)

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
                runTest {
                    // given
                    val expectedContent = createExpectedDatasetPrettyPrint(
                        """
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
                """.trimIndent()
                    )

                    val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), ZoneOffset.UTC)
                    val serializer = DatasetJsonSerializer(clock)

                    val animeList = listOf(
                        Anime(
                            title = "test",
                            type = AnimeType.OVA,
                        ),
                        Anime(
                            title = "test",
                            type = AnimeType.SPECIAL,
                        ),
                        Anime(
                            title = "test",
                            type = AnimeType.MOVIE,
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
                runTest {
                    // given
                    val expectedContent = createExpectedDatasetPrettyPrint(
                        """
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
                """.trimIndent()
                    )

                    val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), ZoneOffset.UTC)
                    val serializer = DatasetJsonSerializer(clock)

                    val animeList = listOf(
                        Anime(
                            title = "test",
                            type = AnimeType.TV,
                            episodes = 24,
                        ),
                        Anime(
                            title = "test",
                            type = AnimeType.TV,
                            episodes = 12,
                        ),
                        Anime(
                            title = "test",
                            type = AnimeType.TV,
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
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = DatasetJsonSerializer.instance

            // when
            val result = DatasetJsonSerializer.instance

            // then
            assertThat(result).isExactlyInstanceOf(DatasetJsonSerializer::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}