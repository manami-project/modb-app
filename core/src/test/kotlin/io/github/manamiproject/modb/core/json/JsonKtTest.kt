package io.github.manamiproject.modb.core.json

import com.squareup.moshi.JsonDataException
import io.github.manamiproject.modb.core.TestAnimeObjects
import io.github.manamiproject.modb.core.TestAnimeRawObjects
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeRaw
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import io.github.manamiproject.modb.core.json.Json.SerializationOptions.DEACTIVATE_PRETTY_PRINT
import io.github.manamiproject.modb.core.json.Json.SerializationOptions.DEACTIVATE_SERIALIZE_NULL
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.io.path.inputStream
import kotlin.test.Test

internal class JsonKtTest {

    @Nested
    inner class DeserializationTests {

        @Nested
        inner class AnimeRawTests {

            @Test
            fun `deserialize AnimeRaw - using InputStream, all properties set`() {
                tempDirectory {
                    // given
                    val tempFile = tempDir.resolve("${UUID.randomUUID()}.json")
                    TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint.writeToFile(tempFile)

                    val inputStream = LifecycleAwareInputStream(tempFile.inputStream())

                    // when
                    val result = Json.parseJson<AnimeRaw>(inputStream)

                    // then
                    assertThat(result).isEqualTo(TestAnimeRawObjects.AllPropertiesSet.obj)
                }
            }

            @Test
            fun `deserialize AnimeRaw - using string, all properties set`() {
                runTest {
                    // given
                    val json = TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint

                    // when
                    val result = Json.parseJson<AnimeRaw>(json)

                    // then
                    assertThat(result).isEqualTo(TestAnimeRawObjects.AllPropertiesSet.obj)
                }
            }

            @Test
            fun `deserialize AnimeRaw - default properties`() {
                runTest {
                    // given
                    val json = TestAnimeRawObjects.DefaultAnime.serializedPrettyPrint

                    // when
                    val result = Json.parseJson<AnimeRaw>(json)

                    // then
                    assertThat(result).isEqualTo(TestAnimeRawObjects.DefaultAnime.obj)
                }
            }
        }

        @Nested
        inner class AnimeTests {

            @Test
            fun `deserialize Anime - using inputstream, all properties set`() {
                tempDirectory {
                    // given
                    val tempFile = tempDir.resolve("${UUID.randomUUID()}.json")
                    TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint.writeToFile(tempFile)

                    val inputStream = LifecycleAwareInputStream(tempFile.inputStream())

                    // when
                    val result = Json.parseJson<Anime>(inputStream)

                    // then
                    assertThat(result).isEqualTo(TestAnimeObjects.AllPropertiesSet.obj)
                }
            }

            @Test
            fun `deserialize Anime - using string, all properties set`() {
                runTest {
                    // given
                    val json = TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint

                    // when
                    val result = Json.parseJson<Anime>(json)

                    // then
                    assertThat(result).isEqualTo(TestAnimeObjects.AllPropertiesSet.obj)
                }
            }

            @Test
            fun `deserialize Anime - default properties`() {
                runTest {
                    // given
                    val json = TestAnimeObjects.DefaultAnime.serializedPrettyPrint

                    // when
                    val result = Json.parseJson<Anime>(json)

                    // then
                    assertThat(result).isEqualTo(TestAnimeObjects.DefaultAnime.obj)
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
            runTest {
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
            runTest {
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
                    runTest {
                        // given
                        val anime = TestAnimeRawObjects.AllPropertiesSet.obj

                        // when
                        val result = Json.toJson(anime)

                        // then
                        assertThat(result).isEqualTo(TestAnimeRawObjects.AllPropertiesSet.serializedPrettyPrint)
                    }
                }

                @Test
                fun `serialize AnimeRaw - default properties`() {
                    runTest {
                        // given
                        val anime = TestAnimeRawObjects.DefaultAnime.obj

                        // when
                        val result = Json.toJson(anime)

                        // then
                        assertThat(result).isEqualTo(TestAnimeRawObjects.DefaultAnime.serializedPrettyPrint)
                    }
                }
            }

            @Nested
            inner class AnimeTests {

                @Test
                fun `serialize Anime - all properties set`() {
                    runTest {
                        // given
                        val anime = TestAnimeObjects.AllPropertiesSet.obj

                        // when
                        val result = Json.toJson(anime)

                        // then
                        assertThat(result).isEqualTo(TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint)
                    }
                }

                @Test
                fun `serialize Anime - default properties`() {
                    runTest {
                        // given
                        val anime = TestAnimeObjects.DefaultAnime.obj

                        // when
                        val result = Json.toJson(anime)

                        // then
                        assertThat(result).isEqualTo(TestAnimeObjects.DefaultAnime.serializedPrettyPrint)
                    }
                }
            }

            @Test
            fun `serialize - option serialize null is activated by default`() {
                runTest {
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
                    runTest {
                        // given
                        val anime = TestAnimeRawObjects.AllPropertiesSet.obj

                        // when
                        val result = Json.toJson(anime, DEACTIVATE_PRETTY_PRINT)

                        // then
                        assertThat(result).isEqualTo(TestAnimeRawObjects.AllPropertiesSet.serializedMinified)
                    }
                }

                @Test
                fun `serialize AnimeRaw - deactivate serialize null`() {
                    runTest {
                        // given
                        val anime = TestAnimeRawObjects.NullableNotSet.obj

                        val expectedJson = """
                            {
                              "sources": [
                                "https://myanimelist.net/anime/1535"
                              ],
                              "title": "Death Note",
                              "type": "TV",
                              "episodes": 37,
                              "status": "FINISHED",
                              "animeSeason": {
                                "season": "FALL"
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
                              "synonyms": [
                                "DN",
                                "デスノート"
                              ],
                              "studios": [
                                "madhouse"
                              ],
                              "producers": [
                                "d.n. dream partners",
                                "nippon television network",
                                "shueisha",
                                "vap"
                              ],
                              "relatedAnime": [
                                "https://myanimelist.net/anime/2994"
                              ],
                              "tags": [
                                "psychological",
                                "shounen",
                                "supernatural",
                                "suspense"
                              ]
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
                    runTest {
                        // given
                        val anime = TestAnimeObjects.AllPropertiesSet.obj

                        // when
                        val result = Json.toJson(anime, DEACTIVATE_PRETTY_PRINT)

                        // then
                        assertThat(result).isEqualTo(TestAnimeObjects.AllPropertiesSet.serializedMinified)
                    }
                }

                @Test
                fun `serialize Anime - deactivate serialize null`() {
                    runTest {
                        // given
                        val anime = TestAnimeObjects.NullableNotSet.obj

                        val expectedJson = """
                            {
                              "sources": [
                                "https://myanimelist.net/anime/1535"
                              ],
                              "title": "Death Note",
                              "type": "TV",
                              "episodes": 37,
                              "status": "FINISHED",
                              "animeSeason": {
                                "season": "FALL"
                              },
                              "picture": "https://cdn.myanimelist.net/images/anime/1079/138100.jpg",
                              "thumbnail": "https://cdn.myanimelist.net/images/anime/1079/138100t.jpg",
                              "synonyms": [
                                "DN",
                                "デスノート"
                              ],
                              "studios": [
                                "madhouse"
                              ],
                              "producers": [
                                "D.N. Dream Partners",
                                "nippon television network",
                                "shueisha",
                                "vap"
                              ],
                              "relatedAnime": [
                                "https://myanimelist.net/anime/2994"
                              ],
                              "tags": [
                                "psychological",
                                "shounen",
                                "supernatural",
                                "suspense"
                              ]
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
    }
}

private data class NullableTestClass(val nullableString: String? = null, val nonNullableString: String = "test")
private data class ClassWithList(val nonNullableList: List<String> = emptyList())