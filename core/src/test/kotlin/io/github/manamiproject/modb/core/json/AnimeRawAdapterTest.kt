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
            val result = adapter.fromJson(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.AllPropertiesSet.obj)
        }

        @Test
        fun `correctly deserialize nullable values`() {
            // given
            val adapter = AnimeRawAdapter()

            // when
            val result = adapter.fromJson(TestAnimeRawObjects.NullableNotSet.serializedPrettyPrint)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.NullableNotSet.obj)
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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                        "season": null,
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                        "year": 2025
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "duration": {
                        "value": 1380,
                        "unit": "SECONDS"
                      },
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
        fun `throws exception if synonyms is null`() {
            // given
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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

        //FIXME: activate after migrating DCS files @Test
        fun `throws exception if studios is missing`() {
            // given
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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

        //FIXME: activate after migrating DCS files @Test
        fun `throws exception if producers is missing`() {
            // given
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                      "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                  "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                  "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                  "scores": [],
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
            val adapter = AnimeRawAdapter()

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
                  "scores": [],
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
    }

    @Nested
    inner class ToJsonTests {

        @Test
        fun `correctly serialize non-null value`() {
            // given
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = TestAnimeRawObjects.AllPropertiesSet.obj

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
        }

        @Test
        fun `correctly serialize nullable data if serializeNulls has been set`() {
            // given
            val adapter = AnimeRawAdapter().indent("  ").serializeNulls()
            val obj = TestAnimeRawObjects.NullableNotSet.obj

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.NullableNotSet.serializedPrettyPrint)
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
            val obj = TestAnimeRawObjects.AllPropertiesSet.obj.copy(
                _title = " ${value}Go-toubun$value no$value$value Hanayome *$value$value ",
                activateChecks = false,
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
        }

        @Test
        fun `runs performChecks if activateChecks is false and throws an exception if episodes is negative`() {
            // given
            val adapter = AnimeRawAdapter()
            val obj = TestAnimeRawObjects.AllPropertiesSet.obj.copy(
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
            val obj = TestAnimeRawObjects.AllPropertiesSet.obj.copy(
                _relatedAnime = TestAnimeRawObjects.AllPropertiesSet.obj.relatedAnime.union(TestAnimeRawObjects.AllPropertiesSet.obj.sources).toHashSet(),
                activateChecks = false,
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
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
            val obj = TestAnimeRawObjects.AllPropertiesSet.obj.copy(
                _synonyms = TestAnimeRawObjects.AllPropertiesSet.obj.synonyms.union(
                    hashSetOf(" ${value}The $value${value}Quintessential $value$value${value}Quintuplets*$value$value ")
                ).toHashSet(),
                activateChecks = false,
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
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
            val obj = TestAnimeRawObjects.AllPropertiesSet.obj.copy(
                _synonyms = TestAnimeRawObjects.AllPropertiesSet.obj.synonyms.union(hashSetOf(value)).toHashSet(),
                activateChecks = false,
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
        }

        @ParameterizedTest
        @ValueSource(strings = ["   romance", "romance    ", "ROMANCE"])
        fun `runs performChecks if activateChecks is false and fixes tags`(value: String) {
            // given
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = TestAnimeRawObjects.AllPropertiesSet.obj.copy(
                _tags = TestAnimeRawObjects.AllPropertiesSet.obj.tags.union(hashSetOf(value)).toHashSet(),
                activateChecks = false,
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
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
            val obj = TestAnimeRawObjects.AllPropertiesSet.obj.copy(
                _tags = TestAnimeRawObjects.AllPropertiesSet.obj.tags.union(hashSetOf(value)).toHashSet(),
                activateChecks = false,
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
        }

        @Test
        fun `sources, synonyms, relatedAnime and tags are being sorted asc by their string representation`() {
            // given
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = TestAnimeRawObjects.AllPropertiesSet.obj.copy(
                _sources = TestAnimeRawObjects.AllPropertiesSet.obj.sources.toList().shuffled().toHashSet(),
                _relatedAnime = TestAnimeRawObjects.AllPropertiesSet.obj.relatedAnime.toList().shuffled().toHashSet(),
                _synonyms = TestAnimeRawObjects.AllPropertiesSet.obj.synonyms.toList().shuffled().toHashSet(),
                _tags = TestAnimeRawObjects.AllPropertiesSet.obj.tags.toList().shuffled().toHashSet(),
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
        }

        @Test
        fun `sort scores by hostname`() {
            // given
            val adapter = AnimeRawAdapter().indent("  ")
            val obj = TestAnimeRawObjects.FullyMergedAllPropertiesSet.obj.copy().addScores(
                TestAnimeRawObjects.FullyMergedAllPropertiesSet.obj.scores.shuffled(),
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(TestAnimeRawObjects.FullyMergedAllPropertiesSet.serializedPrettyPrint)
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