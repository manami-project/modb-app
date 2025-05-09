package io.github.manamiproject.modb.core.json

import com.squareup.moshi.JsonDataException
import io.github.manamiproject.modb.core.TestAnimeRawObjects
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test
import io.github.manamiproject.modb.core.anime.Duration.Companion.UNKNOWN as UNKNOWN_DURATION

internal class AnimeRawAdapterTest {

    @Nested
    inner class FromJsonTests {

        @Test
        fun `correctly deserialize non-null value`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = adapter.fromJson(TestAnimeRawObjects.ReducedTvNonNull.serializedPrettyPrint)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.ReducedTvNonNull.obj)
        }

        @Test
        fun `correctly deserialize nullable values`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = adapter.fromJson(TestAnimeRawObjects.ReducedTvNull.serializedPrettyPrint)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.ReducedTvNull.obj)
        }

        @Test
        fun `correctly deserialize object with default values`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = adapter.fromJson(TestAnimeRawObjects.DefaultAnime.serializedPrettyPrint)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.DefaultAnime.obj)
        }

        @Test
        fun `throw exception on null value`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""null""")
            }

            // then
            assertThat(result).hasMessage("Expected BEGIN_OBJECT but was NULL at path \$")
        }

        @ParameterizedTest
        @ValueSource(strings = [
            "",
            "   ",
            "\u00A0",
            "\u202F",
            "\u200A",
            "\u205F",
            "\u2000",
            "\u2001",
            "\u2002",
            "\u2003",
            "\u2004",
            "\u2005",
            "\u2006",
            "\u2007",
            "\u2008",
            "\u2009",
            "\uFEFF",
            "\u180E",
            "\u2060",
            "\u200D",
            "\u0090",
            "\u200C",
            "\u200B",
            "\u00AD",
            "\u000C",
            "\u2028",
            "\r",
            "\n",
            "\t",
        ])
        fun `throws exception if title is blank`(value: String) {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                adapter.fromJson("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/1535"
                      ],
                      "title": "$value",
                      "synonyms": [
                        "DN"
                      ],
                      "type": "TV",
                      "episodes": 37,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "FALL",
                        "year": 2006
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2994"
                      ],
                      "tags": [
                        "psychological"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Title cannot be blank.")
        }

        @Test
        fun `throws exception if title is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/1535"
                      ],
                      "title": null,
                      "synonyms": [
                        "DN"
                      ],
                      "type": "TV",
                      "episodes": 37,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "FALL",
                        "year": 2006
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2994"
                      ],
                      "tags": [
                        "psychological"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected a string but was NULL at path \$.title")
        }

        @Test
        fun `throws exception if title is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/1535"
                      ],
                      "synonyms": [
                        "DN"
                      ],
                      "type": "TV",
                      "episodes": 37,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "FALL",
                        "year": 2006
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2994"
                      ],
                      "tags": [
                        "psychological"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'title' is either missing or null.")
        }

        @Test
        fun `throws exception if sources is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": null,
                      "title": "Death Note",
                      "synonyms": [
                        "DN"
                      ],
                      "type": "TV",
                      "episodes": 37,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "FALL",
                        "year": 2006
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2994"
                      ],
                      "tags": [
                        "psychological"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected BEGIN_ARRAY but was NULL at path \$.sources")
        }

        @Test
        fun `throws exception if sources is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
                    {
                      "title": "Death Note",
                      "synonyms": [
                        "DN"
                      ],
                      "type": "TV",
                      "episodes": 37,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "FALL",
                        "year": 2006
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2994"
                      ],
                      "tags": [
                        "psychological"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'sources' is either missing or null.")
        }

        @Test
        fun `throws exception if synonyms is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/1535"
                      ],
                      "title": "Death Note",
                      "synonyms": null,
                      "type": "TV",
                      "episodes": 37,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "FALL",
                        "year": 2006
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2994"
                      ],
                      "tags": [
                        "psychological"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected BEGIN_ARRAY but was NULL at path \$.synonyms")
        }

        @Test
        fun `throws exception if synonyms is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/1535"
                      ],
                      "title": "Death Note",
                      "type": "TV",
                      "episodes": 37,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "FALL",
                        "year": 2006
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2994"
                      ],
                      "tags": [
                        "psychological"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'synonyms' is either missing or null.")
        }

        @Test
        fun `throws exception if type is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/1535"
                      ],
                      "title": "Death Note",
                      "synonyms": [
                        "DN"
                      ],
                      "type": null,
                      "episodes": 37,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "FALL",
                        "year": 2006
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2994"
                      ],
                      "tags": [
                        "psychological"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected a string but was NULL at path \$.type")
        }

        @Test
        fun `throws exception if type is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/1535"
                      ],
                      "title": "Death Note",
                      "synonyms": [
                        "DN"
                      ],
                      "episodes": 37,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "FALL",
                        "year": 2006
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2994"
                      ],
                      "tags": [
                        "psychological"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'type' is either missing or null.")
        }

        @Test
        fun `throws exception if episodes is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/1535"
                      ],
                      "title": "Death Note",
                      "synonyms": [
                        "DN"
                      ],
                      "type": "TV",
                      "episodes": null,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "FALL",
                        "year": 2006
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2994"
                      ],
                      "tags": [
                        "psychological"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected an int but was NULL at path \$.episodes")
        }

        @Test
        fun `throws exception if episodes is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/1535"
                      ],
                      "title": "Death Note",
                      "synonyms": [
                        "DN"
                      ],
                      "type": "TV",
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "FALL",
                        "year": 2006
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2994"
                      ],
                      "tags": [
                        "psychological"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'episodes' is either missing or null.")
        }

        @Test
        fun `throws exception if status is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/1535"
                      ],
                      "title": "Death Note",
                      "synonyms": [
                        "DN"
                      ],
                      "type": "TV",
                      "episodes": 37,
                      "status": null,
                      "animeSeason": {
                        "season": "FALL",
                        "year": 2006
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2994"
                      ],
                      "tags": [
                        "psychological"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected a string but was NULL at path \$.status")
        }

        @Test
        fun `throws exception if status is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/1535"
                      ],
                      "title": "Death Note",
                      "synonyms": [
                        "DN"
                      ],
                      "type": "TV",
                      "episodes": 37,
                      "animeSeason": {
                        "season": "FALL",
                        "year": 2006
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2994"
                      ],
                      "tags": [
                        "psychological"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'status' is either missing or null.")
        }

        @Test
        fun `throws exception if animeSeason is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/1535"
                      ],
                      "title": "Death Note",
                      "synonyms": [
                        "DN"
                      ],
                      "type": "TV",
                      "episodes": 37,
                      "status": "FINISHED",
                      "animeSeason": null,
                      "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2994"
                      ],
                      "tags": [
                        "psychological"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected BEGIN_OBJECT but was NULL at path \$.animeSeason")
        }

        @Test
        fun `throws exception if animeSeason is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/1535"
                      ],
                      "title": "Death Note",
                      "synonyms": [
                        "DN"
                      ],
                      "type": "TV",
                      "episodes": 37,
                      "status": "FINISHED",
                      "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2994"
                      ],
                      "tags": [
                        "psychological"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'animeSeason' is either missing or null.")
        }

        @Test
        fun `throws exception if picture is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/1535"
                      ],
                      "title": "Death Note",
                      "synonyms": [
                        "DN"
                      ],
                      "type": "TV",
                      "episodes": 37,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "FALL",
                        "year": 2006
                      },
                      "picture": null,
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2994"
                      ],
                      "tags": [
                        "psychological"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected a string but was NULL at path \$.picture")
        }

        @Test
        fun `throws exception if picture is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/1535"
                      ],
                      "title": "Death Note",
                      "synonyms": [
                        "DN"
                      ],
                      "type": "TV",
                      "episodes": 37,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "FALL",
                        "year": 2006
                      },
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2994"
                      ],
                      "tags": [
                        "psychological"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'picture' is either missing or null.")
        }

        @Test
        fun `throws exception if thumbnail is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/1535"
                      ],
                      "title": "Death Note",
                      "synonyms": [
                        "DN"
                      ],
                      "type": "TV",
                      "episodes": 37,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "FALL",
                        "year": 2006
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                      "thumbnail": null,
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2994"
                      ],
                      "tags": [
                        "psychological"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected a string but was NULL at path \$.thumbnail")
        }

        @Test
        fun `throws exception if thumbnail is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/1535"
                      ],
                      "title": "Death Note",
                      "synonyms": [
                        "DN"
                      ],
                      "type": "TV",
                      "episodes": 37,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "FALL",
                        "year": 2006
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2994"
                      ],
                      "tags": [
                        "psychological"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'thumbnail' is either missing or null.")
        }

        @Test
        fun `returns anime with unknown duration if duration property is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = adapter.fromJson("""
                {
                  "sources": [
                    "https://myanimelist.net/anime/1535"
                  ],
                  "title": "Death Note",
                  "synonyms": [
                    "DN"
                  ],
                  "type": "TV",
                  "episodes": 37,
                  "status": "FINISHED",
                  "animeSeason": {
                    "season": "FALL",
                    "year": 2006
                  },
                  "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                  "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                  "duration": null,
                  "scores": [
                    {
                      "hostname": "myanimelist.net",
                      "value": 8.62,
                      "range": {
                        "minInclusive": 1.0,
                        "maxInclusive": 10.0
                      }
                    }
                  ],
                  "relatedAnime": [
                    "https://myanimelist.net/anime/2994"
                  ],
                  "tags": [
                    "psychological"
                  ]
                }
            """.trimIndent())!!

            // then
            assertThat(result.duration).isEqualTo(UNKNOWN_DURATION)
        }

        @Test
        fun `returns anime with unknown duration if duration property is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = adapter.fromJson("""
                {
                  "sources": [
                    "https://myanimelist.net/anime/1535"
                  ],
                  "title": "Death Note",
                  "synonyms": [
                    "DN"
                  ],
                  "type": "TV",
                  "episodes": 37,
                  "status": "FINISHED",
                  "animeSeason": {
                    "season": "FALL",
                    "year": 2006
                  },
                  "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                  "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                  "scores": [
                    {
                      "hostname": "myanimelist.net",
                      "value": 8.62,
                      "range": {
                        "minInclusive": 1.0,
                        "maxInclusive": 10.0
                      }
                    }
                  ],
                  "relatedAnime": [
                    "https://myanimelist.net/anime/2994"
                  ],
                  "tags": [
                    "psychological"
                  ]
                }
            """.trimIndent())!!

            // then
            assertThat(result.duration).isEqualTo(UNKNOWN_DURATION)
        }

        @Test
        fun `throws exception if relatedAnime is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/1535"
                      ],
                      "title": "Death Note",
                      "synonyms": [
                        "DN"
                      ],
                      "type": "TV",
                      "episodes": 37,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "FALL",
                        "year": 2006
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": null,
                      "tags": [
                        "psychological"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected BEGIN_ARRAY but was NULL at path \$.relatedAnime")
        }

        @Test
        fun `throws exception if relatedAnime is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/1535"
                      ],
                      "title": "Death Note",
                      "synonyms": [
                        "DN"
                      ],
                      "type": "TV",
                      "episodes": 37,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "FALL",
                        "year": 2006
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "tags": [
                        "psychological"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'relatedAnime' is either missing or null.")
        }

        @Test
        fun `throws exception if tags is null`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/1535"
                      ],
                      "title": "Death Note",
                      "synonyms": [
                        "DN"
                      ],
                      "type": "TV",
                      "episodes": 37,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "FALL",
                        "year": 2006
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2994"
                      ],
                      "tags": null
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected BEGIN_ARRAY but was NULL at path \$.tags")
        }

        @Test
        fun `throws exception if tags is missing`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/1535"
                      ],
                      "title": "Death Note",
                      "synonyms": [
                        "DN"
                      ],
                      "type": "TV",
                      "episodes": 37,
                      "status": "FINISHED",
                      "animeSeason": {
                        "season": "FALL",
                        "year": 2006
                      },
                      "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                      "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [
                        {
                          "hostname": "myanimelist.net",
                          "value": 8.62,
                          "range": {
                            "minInclusive": 1.0,
                            "maxInclusive": 10.0
                          }
                        }
                      ],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2994"
                      ]
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'tags' is either missing or null.")
        }
    }

    @Nested
    inner class ToJsonTests {

        @Test
        fun `correctly serialize non-null value`() {
            // given
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = TestAnimeRawObjects.ReducedTvNonNull.obj

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.ReducedTvNonNull.serializedPrettyPrint)
        }

        @Test
        fun `correctly serialize nullable data if serializeNulls has been set`() {
            // given
            val adapter = AnimeRawAdapter().indent("  ").serializeNulls()
            val obj = TestAnimeRawObjects.ReducedTvNull.obj

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.ReducedTvNull.serializedPrettyPrint)
        }

        @Test
        fun `correctly serialize object with default values`() {
            // given
            val adapter = AnimeRawAdapter().indent("  ").serializeNulls()
            val obj = TestAnimeRawObjects.DefaultAnime.obj

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.DefaultAnime.serializedPrettyPrint)
        }

        @ParameterizedTest
        @ValueSource(strings = [
            "",
            "   ",
            "\u00A0",
            "\u202F",
            "\u200A",
            "\u205F",
            "\u2000",
            "\u2001",
            "\u2002",
            "\u2003",
            "\u2004",
            "\u2005",
            "\u2006",
            "\u2007",
            "\u2008",
            "\u2009",
            "\uFEFF",
            "\u180E",
            "\u2060",
            "\u200D",
            "\u0090",
            "\u200C",
            "\u200B",
            "\u00AD",
            "\u000C",
            "\u2028",
            "\r",
            "\n",
            "\t",
        ])
        fun `runs performChecks if activateChecks is false and fixes title`(value: String) {
            // given
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = TestAnimeRawObjects.ReducedTvNonNull.obj.copy(
                _title = " ${value}Death $value$value${value}Note$value$value ",
                activateChecks = false,
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.ReducedTvNonNull.serializedPrettyPrint)
        }

        @Test
        fun `runs performChecks if activateChecks is false and throws an exception if episodes is negative`() {
            // given
            val adapter = AnimeRawAdapter()
            val obj = TestAnimeRawObjects.ReducedTvNonNull.obj.copy(
                episodes = -1,
                activateChecks = false,
            )

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                adapter.toJson(obj)
            }

            // then
            assertThat(result).hasMessage("Episodes cannot have a negative value.")
        }

        @Test
        fun `runs performChecks if activateChecks is false and removes sources from relatedAnime`() {
            // given
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = TestAnimeRawObjects.ReducedTvNonNull.obj.copy(
                _relatedAnime = TestAnimeRawObjects.ReducedTvNonNull.obj.relatedAnime.union(TestAnimeRawObjects.ReducedTvNonNull.obj.sources).toHashSet(),
                activateChecks = false,
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.ReducedTvNonNull.serializedPrettyPrint)
        }

        @ParameterizedTest
        @ValueSource(strings = [
            "",
            "   ",
            "\u00A0",
            "\u202F",
            "\u200A",
            "\u205F",
            "\u2000",
            "\u2001",
            "\u2002",
            "\u2003",
            "\u2004",
            "\u2005",
            "\u2006",
            "\u2007",
            "\u2008",
            "\u2009",
            "\uFEFF",
            "\u180E",
            "\u2060",
            "\u200D",
            "\u0090",
            "\u200C",
            "\u200B",
            "\u00AD",
            "\u000C",
            "\u2028",
            "\r",
            "\n",
            "\t",
        ])
        fun `runs performChecks if activateChecks is false and fixes synonyms`(value: String) {
            // given
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = TestAnimeRawObjects.SpecialWithMultipleEpisodes.obj.copy(
                _synonyms = TestAnimeRawObjects.SpecialWithMultipleEpisodes.obj.synonyms.union(
                    hashSetOf(" ${value}The $value${value}Quintessential $value$value${value}Quintuplets*$value$value ")
                ).toHashSet(),
                activateChecks = false,
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.SpecialWithMultipleEpisodes.serializedPrettyPrint)
        }

        @ParameterizedTest
        @ValueSource(strings = [
            "",
            "   ",
            "\u00A0",
            "\u202F",
            "\u200A",
            "\u205F",
            "\u2000",
            "\u2001",
            "\u2002",
            "\u2003",
            "\u2004",
            "\u2005",
            "\u2006",
            "\u2007",
            "\u2008",
            "\u2009",
            "\uFEFF",
            "\u180E",
            "\u2060",
            "\u200D",
            "\u0090",
            "\u200C",
            "\u200B",
            "\u00AD",
            "\u000C",
            "\u2028",
            "\r",
            "\n",
            "\t",
        ])
        fun `runs performChecks if activateChecks is false and removes blank entries from synonyms`(value: String) {
            // given
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = TestAnimeRawObjects.ReducedTvNonNull.obj.copy(
                _synonyms = TestAnimeRawObjects.ReducedTvNonNull.obj.synonyms.union(hashSetOf(value)).toHashSet(),
                activateChecks = false,
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.ReducedTvNonNull.serializedPrettyPrint)
        }

        @ParameterizedTest
        @ValueSource(strings = ["   psychological", "psychological    ", "PSYCHOLOGICAL"])
        fun `runs performChecks if activateChecks is false and fixes tags`(value: String) {
            // given
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = TestAnimeRawObjects.ReducedTvNonNull.obj.copy(
                _tags = TestAnimeRawObjects.ReducedTvNonNull.obj.tags.union(hashSetOf(value)).toHashSet(),
                activateChecks = false,
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.ReducedTvNonNull.serializedPrettyPrint)
        }

        @ParameterizedTest
        @ValueSource(strings = [
            "",
            "   ",
            "\u00A0",
            "\u202F",
            "\u200A",
            "\u205F",
            "\u2000",
            "\u2001",
            "\u2002",
            "\u2003",
            "\u2004",
            "\u2005",
            "\u2006",
            "\u2007",
            "\u2008",
            "\u2009",
            "\uFEFF",
            "\u180E",
            "\u2060",
            "\u200D",
            "\u0090",
            "\u200C",
            "\u200B",
            "\u00AD",
            "\u000C",
            "\u2028",
            "\r",
            "\n",
            "\t",
        ])
        fun `runs performChecks if activateChecks is false and removes blank entries from tags`(value: String) {
            // given
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = TestAnimeRawObjects.ReducedTvNonNull.obj.copy(
                _tags = TestAnimeRawObjects.ReducedTvNonNull.obj.tags.union(hashSetOf(value)).toHashSet(),
                activateChecks = false,
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.ReducedTvNonNull.serializedPrettyPrint)
        }

        @Test
        fun `sources, synonyms, relatedAnime and tags are being sorted asc by their string representation`() {
            // given
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = TestAnimeRawObjects.SpecialWithMultipleEpisodes.obj.copy(
                _sources = TestAnimeRawObjects.SpecialWithMultipleEpisodes.obj.sources.toList().shuffled().toHashSet(),
                _relatedAnime = TestAnimeRawObjects.SpecialWithMultipleEpisodes.obj.relatedAnime.toList().shuffled().toHashSet(),
                _synonyms = TestAnimeRawObjects.SpecialWithMultipleEpisodes.obj.synonyms.toList().shuffled().toHashSet(),
                _tags = TestAnimeRawObjects.SpecialWithMultipleEpisodes.obj.tags.toList().shuffled().toHashSet(),
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.SpecialWithMultipleEpisodes.serializedPrettyPrint)
        }

        @Test
        fun `sort scores by hostname`() {
            // given
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = TestAnimeRawObjects.FullyMergedSpecialWithMultipleEpisodes.obj.copy().addScores(
                TestAnimeRawObjects.FullyMergedSpecialWithMultipleEpisodes.obj.scores.shuffled(),
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.FullyMergedSpecialWithMultipleEpisodes.serializedPrettyPrint)
        }

        @Test
        fun `throws exception for a null value`() {
            // given
            val adapter = AnimeRawAdapter().serializeNulls()

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                adapter.toJson(null)
            }

            // then
            assertThat(result).hasMessage("AnimeRawAdapter expects non-nullable value, but received null.")
        }
    }
}