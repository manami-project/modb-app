package io.github.manamiproject.modb.serde.json

import com.github.tomakehurst.wiremock.WireMockServer
import io.github.manamiproject.modb.core.extensions.createZipOf
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.serde.TestAnimeObjects
import io.github.manamiproject.modb.serde.TestHttpClient
import io.github.manamiproject.modb.serde.TestJsonDeserializer
import io.github.manamiproject.modb.serde.createExpectedDatasetMinified
import io.github.manamiproject.modb.serde.createExpectedDatasetPrettyPrint
import io.github.manamiproject.modb.test.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI
import java.net.URL
import kotlin.io.path.createFile
import kotlin.io.path.readBytes
import kotlin.test.Test

internal class DefaultExternalResourceJsonDeserializerTest : MockServerTestCase<WireMockServer> by WireMockServerCreator() {

    @Nested
    inner class DeserializeUrlTests {

        @Test
        fun `throws exception if the response code is not 200`() {
            // given
            val testHttpClient = object: HttpClient by TestHttpClient {
                override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(500, "ERROR")
            }

            val externalResourceDeserializer = DefaultExternalResourceJsonDeserializer(
                httpClient = testHttpClient,
                deserializer = TestJsonDeserializer,
            )

            // when
            val result = exceptionExpected<IllegalStateException> {
                externalResourceDeserializer.deserialize(URI("http://localhost$port/anime-offline-database.json").toURL())
            }

            // then
            assertThat(result).hasMessage("Error downloading file: HTTP response code was: [500]")
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
        fun `throws exception if the response body is blank`(value: String) {
            // given
            val testHttpClient = object: HttpClient by TestHttpClient {
                override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(200, value)
            }

            val externalResourceDeserializer = DefaultExternalResourceJsonDeserializer(
                httpClient = testHttpClient,
                deserializer = TestJsonDeserializer,
            )

            // when
            val result = exceptionExpected<IllegalStateException> {
                externalResourceDeserializer.deserialize(URI("http://localhost$port/anime-offline-database.json").toURL())
            }

            // then
            assertThat(result).hasMessage("Error downloading file: The response body was blank.")
        }

        @Test
        fun `correctly download and call deserializer`() {
            runBlocking {
                // given
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = "[1,2,3,4,5]".toByteArray(),
                    )
                }

                val testDeserializer = object: JsonDeserializer<List<Int>> by TestJsonDeserializer {
                    override suspend fun deserialize(json: String): List<Int> = listOf(1, 2, 3, 4, 5)
                }

                val externalResourceDeserializer = DefaultExternalResourceJsonDeserializer(
                    httpClient = testHttpClient,
                    deserializer = testDeserializer,
                )

                // when
                val result = externalResourceDeserializer.deserialize(URI("http://localhost$port/anime-offline-database.json").toURL())

                // then
                assertThat(result).containsExactlyInAnyOrder(1, 2, 3, 4, 5)
            }
        }

        @Test
        fun `correctly download, unzip and call deserializer`() {
            tempDirectory {
                // given
                val jsonFile = tempDir.resolve("file.json")
                createExpectedDatasetMinified(
                    TestAnimeObjects.DefaultAnime.serializedMinified,
                    TestAnimeObjects.NullableNotSet.serializedMinified,
                    TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    TestAnimeObjects.FullyMergedAllPropertiesSet.serializedMinified,
                ).writeToFile(jsonFile)

                val zipFile = tempDir.resolve("test.zip").createZipOf(jsonFile)

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = zipFile.readBytes(),
                        _headers = mutableMapOf("content-type" to setOf("application/zip"))
                    )
                }

                val testDeserializer = object: JsonDeserializer<List<Int>> by TestJsonDeserializer {
                    override suspend fun deserialize(json: String): List<Int> = listOf(1, 2, 3, 4, 5)
                }

                val externalResourceDeserializer = DefaultExternalResourceJsonDeserializer(
                    httpClient = testHttpClient,
                    deserializer = testDeserializer,
                )

                // when
                val result = externalResourceDeserializer.deserialize(URI("http://localhost$port/anime-offline-database.zip").toURL())

                // then
                assertThat(result).containsExactlyInAnyOrder(1, 2, 3, 4, 5)
            }
        }

        @Test
        fun `correctly download and deserialize dataset file`() {
            runBlocking {
                // given
                val json = createExpectedDatasetPrettyPrint(
                    TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                    TestAnimeObjects.NullableNotSet.serializedPrettyPrint,
                    TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint,
                    TestAnimeObjects.FullyMergedAllPropertiesSet.serializedPrettyPrint,
                )

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = json,
                    )
                }

                val externalResourceDeserializer = DefaultExternalResourceJsonDeserializer(
                    httpClient = testHttpClient,
                    deserializer = AnimeListJsonStringDeserializer(),
                )

                // when
                val result = externalResourceDeserializer.deserialize(URI("http://localhost$port/anime-offline-database.json").toURL())

                // then
                assertThat(result.data).containsExactlyInAnyOrder(
                    TestAnimeObjects.DefaultAnime.obj,
                    TestAnimeObjects.NullableNotSet.obj,
                    TestAnimeObjects.AllPropertiesSet.obj,
                    TestAnimeObjects.FullyMergedAllPropertiesSet.obj,
                )
            }
        }

        @Test
        fun `correctly download and deserialize zipped dataset file`() {
            tempDirectory {
                // given
                val jsonFile = tempDir.resolve("file.json")
                createExpectedDatasetMinified(
                    TestAnimeObjects.DefaultAnime.serializedMinified,
                    TestAnimeObjects.NullableNotSet.serializedMinified,
                    TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    TestAnimeObjects.FullyMergedAllPropertiesSet.serializedMinified,
                ).writeToFile(jsonFile)

                val zipFile = tempDir.resolve("test.zip").createZipOf(jsonFile)

                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = zipFile.readBytes(),
                        _headers = mutableMapOf("content-type" to setOf("application/zip")),
                    )
                }

                val externalResourceDeserializer = DefaultExternalResourceJsonDeserializer(
                    httpClient = testHttpClient,
                    deserializer = AnimeListJsonStringDeserializer(),
                )

                // when
                val result = externalResourceDeserializer.deserialize(URI("http://localhost$port/anime-offline-database.zip").toURL())

                // then
                assertThat(result.data).containsExactly(
                    TestAnimeObjects.DefaultAnime.obj,
                    TestAnimeObjects.NullableNotSet.obj,
                    TestAnimeObjects.AllPropertiesSet.obj,
                    TestAnimeObjects.FullyMergedAllPropertiesSet.obj,
                )
            }
        }
    }

    @Nested
    inner class DeserializeRegularFileTests {

        @Test
        fun `throws exception if the given path is a directory`() {
            tempDirectory {
                // given
                val externalResourceDeserializer = DefaultExternalResourceJsonDeserializer(
                    httpClient = TestHttpClient,
                    deserializer = TestJsonDeserializer,
                )

                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    externalResourceDeserializer.deserialize(tempDir)
                }

                // then
                assertThat(result).hasMessage("The given path does not exist or is not a regular file: [${tempDir.toAbsolutePath()}]")
            }
        }

        @Test
        fun `throws exception if the given file does not exist`() {
            tempDirectory {
                // given
                val externalResourceDeserializer = DefaultExternalResourceJsonDeserializer(
                    httpClient = TestHttpClient,
                    deserializer = TestJsonDeserializer,
                )
                val testFile = tempDir.resolve("anime-offline-database.json")

                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    externalResourceDeserializer.deserialize(testFile)
                }

                // then
                assertThat(result).hasMessage("The given path does not exist or is not a regular file: [${testFile.toAbsolutePath()}]")
            }
        }

        @Test
        fun `throws exception if the given file is neither json nor zip file`() {
            tempDirectory {
                // given
                val externalResourceDeserializer = DefaultExternalResourceJsonDeserializer(
                    httpClient = TestHttpClient,
                    deserializer = TestJsonDeserializer,
                )

                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    externalResourceDeserializer.deserialize(testResource("logback-test.xml"))
                }

                // then
                assertThat(result).hasMessage("File is neither JSON nor zip file")
            }
        }

        @Test
        fun `throws exception if the given zip file doesn't contain a JSON file`() {
            tempDirectory {
                // given
                val externalResourceDeserializer = DefaultExternalResourceJsonDeserializer(
                    httpClient = TestHttpClient,
                    deserializer = TestJsonDeserializer,
                )

                val textFile = tempDir.resolve("file.txt")
                "text".writeToFile(textFile)
                val zipFile = tempDir.resolve("test.zip").createZipOf(textFile)

                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    externalResourceDeserializer.deserialize(zipFile)
                }

                // then
                assertThat(result).hasMessage("File inside zip archive is not a JSON file.")
            }
        }

        @Test
        fun `throws exception if the given zip file contains more than one file`() {
            tempDirectory {
                // given
                val externalResourceDeserializer = DefaultExternalResourceJsonDeserializer(
                    httpClient = TestHttpClient,
                    deserializer = TestJsonDeserializer,
                )

                val jsonFile1 = tempDir.resolve("file1.json")
                "{}".writeToFile(jsonFile1)

                val jsonFile2 = tempDir.resolve("file2.json")
                "{}".writeToFile(jsonFile2)

                val zipFile = tempDir.resolve("test.zip").createZipOf(jsonFile1, jsonFile2)

                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    externalResourceDeserializer.deserialize(zipFile)
                }

                // then
                assertThat(result).hasMessage("The zip file contains more than one file.")
            }
        }

        @Test
        fun `correctly deserialize dataset file`() {
            tempDirectory {
                // given
                val testDeserializer = object : JsonDeserializer<List<Int>> by TestJsonDeserializer {
                    override suspend fun deserialize(json: String): List<Int> = listOf(1, 2, 4, 5)
                }

                val jsonFile = tempDir.resolve("test.json").createFile()

                val externalResourceDeserializer = DefaultExternalResourceJsonDeserializer(
                    httpClient = TestHttpClient,
                    deserializer = testDeserializer,
                )

                // when
                val result = externalResourceDeserializer.deserialize(jsonFile)

                // then
                assertThat(result).containsExactlyInAnyOrder(1, 2, 4, 5)
            }
        }

        @Test
        fun `correctly deserialize zipped dataset file`() {
            tempDirectory {
                // given
                val jsonFile = tempDir.resolve("file.json")
                createExpectedDatasetMinified(
                    TestAnimeObjects.DefaultAnime.serializedMinified,
                    TestAnimeObjects.NullableNotSet.serializedMinified,
                    TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    TestAnimeObjects.FullyMergedAllPropertiesSet.serializedMinified,
                ).writeToFile(jsonFile)

                val zipFile = tempDir.resolve("test.zip").createZipOf(jsonFile)

                val externalResourceDeserializer = DefaultExternalResourceJsonDeserializer(
                    httpClient = TestHttpClient,
                    deserializer = AnimeListJsonStringDeserializer(),
                )

                // when
                val result = externalResourceDeserializer.deserialize(zipFile)

                // then
                assertThat(result.data).containsExactly(
                    TestAnimeObjects.DefaultAnime.obj,
                    TestAnimeObjects.NullableNotSet.obj,
                    TestAnimeObjects.AllPropertiesSet.obj,
                    TestAnimeObjects.FullyMergedAllPropertiesSet.obj,
                )
            }
        }
    }
}