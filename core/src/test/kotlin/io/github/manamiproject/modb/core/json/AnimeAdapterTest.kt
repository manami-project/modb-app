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
                      "sources": [],
                      "title": "$value",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
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
                      "sources": [],
                      "title": null,
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
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
                      "sources": [],
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
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
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
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
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'sources' is either missing or null.")
        }

        @Test
        fun `throws exception if type is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": [],
                      "title": "default",
                      "type": null,
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
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
                      "sources": [],
                      "title": "default",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
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
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": null,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
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
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
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
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": null,
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
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
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
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
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": null,
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
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
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'animeSeason' is either missing or null.")
        }

        @Test
        fun `throws exception if season in animeSeason is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "animeSeason": {
                        "season": null,
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected a string but was NULL at path \$.animeSeason.season")
        }

        @Test
        fun `throws exception if season in animeSeason is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
                    {
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "animeSeason": {
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'season' is either missing or null.")
        }

        @Test
        fun `throws exception if picture is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                                        {
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": null,
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
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
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
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
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
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
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
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
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'thumbnail' is either missing or null.")
        }

        @Test
        fun `throws exception if value in duration is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": null,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected an int but was NULL at path \$.duration.value")
        }

        @Test
        fun `throws exception if value in duration is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
                    {
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'value' is either missing or null.")
        }

        @Test
        fun `throws exception if unit in duration is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": null
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected a string but was NULL at path \$.duration.unit")
        }

        @Test
        fun `throws exception if unit in duration is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
                    {
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'unit' is either missing or null.")
        }

        @Test
        fun `throws exception if arithmeticGeometricMean in score is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": null,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected a double but was NULL at path \$.score.arithmeticGeometricMean")
        }

        @Test
        fun `throws exception if arithmeticGeometricMean in score is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
                    {
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Properties for 'score' are missing: [arithmeticGeometricMean]")
        }

        @Test
        fun `throws exception if arithmeticMean in score is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": null,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected a double but was NULL at path \$.score.arithmeticMean")
        }

        @Test
        fun `throws exception if arithmeticMean in score is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
                    {
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Properties for 'score' are missing: [arithmeticMean]")
        }

        @Test
        fun `throws exception if median in score is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": null
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected a double but was NULL at path \$.score.median")
        }

        @Test
        fun `throws exception if median in score is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
                    {
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Properties for 'score' are missing: [median]")
        }

        @Test
        fun `throws exception if synonyms is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": null,
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
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
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'synonyms' is either missing or null.")
        }

        @Test
        fun `throws exception if studios is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": null,
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected BEGIN_ARRAY but was NULL at path \$.studios")
        }

        @Test
        fun `throws exception if studios is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
                    {
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "producers": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'studios' is either missing or null.")
        }

        @Test
        fun `throws exception if producers is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": null,
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Expected BEGIN_ARRAY but was NULL at path \$.producers")
        }

        @Test
        fun `throws exception if producers is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""
                    {
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "relatedAnime": [],
                      "tags": []
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'producers' is either missing or null.")
        }

        @Test
        fun `throws exception if relatedAnime is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""
                    {
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": null,
                      "tags": []
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
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "tags": []
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
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": [],
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
                      "sources": [],
                      "title": "default",
                      "type": "UNKNOWN",
                      "episodes": 1,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED",
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "score": {
                        "arithmeticGeometricMean": 8.62,
                        "arithmeticMean": 8.62,
                        "median": 8.62
                      },
                      "synonyms": [],
                      "studios": [],
                      "producers": [],
                      "relatedAnime": []
                    }
                """.trimIndent())
            }

            // then
            assertThat(result).hasMessage("Property 'tags' is either missing or null.")
        }

        @Test
        fun `returns anime with year 0 if year in animeSeason is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = adapter.fromJson("""
                {
                  "sources": [],
                  "title": "default",
                  "type": "UNKNOWN",
                  "episodes": 1,
                  "status": "UNKNOWN",
                  "animeSeason": {
                    "season": "UNDEFINED",
                    "year": null
                  },
                  "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                  "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                  "duration": {
                    "value": 1380,
                    "unit": "SECONDS"
                  },
                  "score": {
                    "arithmeticGeometricMean": 8.62,
                    "arithmeticMean": 8.62,
                    "median": 8.62
                  },
                  "synonyms": [],
                  "studios": [],
                  "producers": [],
                  "relatedAnime": [],
                  "tags": []
                }
            """.trimIndent())!!

            // then
            assertThat(result.animeSeason.year).isZero()
        }

        @Test
        fun `returns anime with year 0 if year in animeSeason is missing`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = adapter.fromJson("""
                {
                  "sources": [],
                  "title": "default",
                  "type": "UNKNOWN",
                  "episodes": 1,
                  "status": "UNKNOWN",
                  "animeSeason": {
                    "season": "UNDEFINED"
                  },
                  "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                  "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                  "duration": {
                    "value": 1380,
                    "unit": "SECONDS"
                  },
                  "score": {
                    "arithmeticGeometricMean": 8.62,
                    "arithmeticMean": 8.62,
                    "median": 8.62
                  },
                  "synonyms": [],
                  "studios": [],
                  "producers": [],
                  "relatedAnime": [],
                  "tags": []
                }
            """.trimIndent())!!

            // then
            assertThat(result.animeSeason.year).isZero()
        }

        @Test
        fun `returns anime with unknown duration if duration property is null`() {
            // given
            val adapter = AnimeAdapter()

            // when
            val result = adapter.fromJson("""
                {
                  "sources": [],
                  "title": "default",
                  "type": "UNKNOWN",
                  "episodes": 1,
                  "status": "UNKNOWN",
                  "animeSeason": {
                    "season": "UNDEFINED",
                    "year": 2025
                  },
                  "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                  "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                  "duration": null,
                  "score": {
                    "arithmeticGeometricMean": 8.62,
                    "arithmeticMean": 8.62,
                    "median": 8.62
                  },
                  "synonyms": [],
                  "studios": [],
                  "producers": [],
                  "relatedAnime": [],
                  "tags": []
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
                  "sources": [],
                  "title": "default",
                  "type": "UNKNOWN",
                  "episodes": 1,
                  "status": "UNKNOWN",
                  "animeSeason": {
                    "season": "UNDEFINED",
                    "year": 2025
                  },
                  "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                  "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                  "score": {
                    "arithmeticGeometricMean": 8.62,
                    "arithmeticMean": 8.62,
                    "median": 8.62
                  },
                  "synonyms": [],
                  "studios": [],
                  "producers": [],
                  "relatedAnime": [],
                  "tags": []
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
                  "sources": [],
                  "title": "default",
                  "type": "UNKNOWN",
                  "episodes": 1,
                  "status": "UNKNOWN",
                  "animeSeason": {
                    "season": "UNDEFINED",
                    "year": 2025
                  },
                  "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                  "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                  "duration": {
                    "value": 1380,
                    "unit": "SECONDS"
                  },
                  "score": null,
                  "synonyms": [],
                  "studios": [],
                  "producers": [],
                  "relatedAnime": [],
                  "tags": []
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
                  "sources": [],
                  "title": "default",
                  "type": "UNKNOWN",
                  "episodes": 1,
                  "status": "UNKNOWN",
                  "animeSeason": {
                    "season": "UNDEFINED",
                    "year": 2025
                  },
                  "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                  "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                  "duration": {
                    "value": 1380,
                    "unit": "SECONDS"
                  },
                  "synonyms": [],
                  "studios": [],
                  "producers": [],
                  "relatedAnime": [],
                  "tags": []
                }
            """.trimIndent())!!

            // then
            assertThat(result.score).isEqualTo(NoScore)
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