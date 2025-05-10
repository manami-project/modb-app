package io.github.manamiproject.modb.core.json

import com.squareup.moshi.JsonDataException
import io.github.manamiproject.modb.core.TestAnimeObjects
import io.github.manamiproject.modb.core.anime.NoScore
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test
import io.github.manamiproject.modb.core.anime.Duration.Companion.UNKNOWN as UNKNOWN_DURATION

internal class AnimeAdapterTest {

    @Nested
    inner class FromJsonTests {

        @Test
        fun `correctly deserialize non-null value`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = adapter.fromJson(TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint)

            // then
            assertThat(result).isEqualTo(TestAnimeObjects.AllPropertiesSet.obj)
        }

        @Test
        fun `correctly deserialize nullable values`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = adapter.fromJson(TestAnimeObjects.NullableNotSet.serializedPrettyPrint)

            // then
            assertThat(result).isEqualTo(TestAnimeObjects.NullableNotSet.obj)
        }

        @Test
        fun `correctly deserialize object with default values`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = adapter.fromJson(TestAnimeObjects.DefaultAnime.serializedPrettyPrint)

            // then
            assertThat(result).isEqualTo(TestAnimeObjects.DefaultAnime.obj)
        }

        @Test
        fun `throw exception on null value`() {
            // given
            val adapter = AnimeAdapter()

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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                  "score": {
                    "arithmeticGeometricMean": 8.62,
                    "arithmeticMean": 8.62,
                    "median": 8.62
                  },
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
            val adapter = AnimeAdapter()

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
                  "score": {
                    "arithmeticGeometricMean": 8.62,
                    "arithmeticMean": 8.62,
                    "median": 8.62
                  },
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
        fun `returns anime with NoScore if score property is null`() {
            // given
            val adapter = AnimeAdapter()

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
                  "duration": {
                    "value": 1380,
                    "unit": "SECONDS"
                  },
                  "score": null,
                  "relatedAnime": [
                    "https://myanimelist.net/anime/2994"
                  ],
                  "tags": [
                    "psychological"
                  ]
                }
            """.trimIndent())!!

            // then
            assertThat(result.score).isEqualTo(NoScore)
        }

        @Test
        fun `returns anime with NoScore if score property is missing`() {
            // given
            val adapter = AnimeAdapter()

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
                  "duration": {
                    "value": 1380,
                    "unit": "SECONDS"
                  },
                  "relatedAnime": [
                    "https://myanimelist.net/anime/2994"
                  ],
                  "tags": [
                    "psychological"
                  ]
                }
            """.trimIndent())!!

            // then
            assertThat(result.score).isEqualTo(NoScore)
        }

        @Test
        fun `throws exception if relatedAnime is null`() {
            // given
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter()

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
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
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
            val adapter = AnimeAdapter().indent("  ")

            // when
            val result = adapter.toJson(TestAnimeObjects.AllPropertiesSet.obj)

            // then
            assertThat(result).isEqualTo(TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint)
        }

        @Test
        fun `correctly serialize nullable data if serializeNulls has been set`() {
            // given
            val adapter = AnimeAdapter().indent("  ").serializeNulls()

            // when
            val result = adapter.toJson(TestAnimeObjects.NullableNotSet.obj)

            // then
            assertThat(result).isEqualTo(TestAnimeObjects.NullableNotSet.serializedPrettyPrint)
        }

        @Test
        fun `correctly serialize object with default values`() {
            // given
            val adapter = AnimeAdapter().indent("  ").serializeNulls()
            val obj = TestAnimeObjects.DefaultAnime.obj

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeObjects.DefaultAnime.serializedPrettyPrint)
        }

        @Test
        fun `sources, synonyms, relatedAnime and tags are being sorted asc by their string representation`() {
            // given
            val adapter = AnimeAdapter().indent("  ")
            val obj = TestAnimeObjects.FullyMergedAllPropertiesSet.obj.copy(
                sources = TestAnimeObjects.FullyMergedAllPropertiesSet.obj.sources.toList().shuffled().toHashSet(),
                relatedAnime = TestAnimeObjects.FullyMergedAllPropertiesSet.obj.relatedAnime.toList().shuffled().toHashSet(),
                synonyms = TestAnimeObjects.FullyMergedAllPropertiesSet.obj.synonyms.toList().shuffled().toHashSet(),
                tags = TestAnimeObjects.FullyMergedAllPropertiesSet.obj.tags.toList().shuffled().toHashSet(),
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeObjects.FullyMergedAllPropertiesSet.serializedPrettyPrint)
        }

        @Test
        fun `throws exception for a null value`() {
            // given
            val adapter = AnimeAdapter().serializeNulls()

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                adapter.toJson(null)
            }

            // then
            assertThat(result).hasMessage("AnimeAdapter expects non-nullable value, but received null.")
        }
    }
}