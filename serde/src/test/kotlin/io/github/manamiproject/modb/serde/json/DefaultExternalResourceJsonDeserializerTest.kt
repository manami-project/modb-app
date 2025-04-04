package io.github.manamiproject.modb.serde.json

import com.github.tomakehurst.wiremock.WireMockServer
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeSeason
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.anime.Duration
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.core.anime.ScoreValue
import io.github.manamiproject.modb.serde.TestHttpClient
import io.github.manamiproject.modb.serde.TestJsonDeserializer
import io.github.manamiproject.modb.test.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI
import java.net.URL
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
                        body = loadTestResource<ByteArray>("json/deserialization/test_dataset_for_deserialization.json"),
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
            runBlocking {
                // given
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = loadTestResource<ByteArray>("json/deserialization/test_dataset_for_deserialization.zip"),
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
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = loadTestResource<ByteArray>("json/deserialization/test_dataset_for_deserialization.json"),
                    )
                }

                val externalResourceDeserializer = DefaultExternalResourceJsonDeserializer(
                    httpClient = testHttpClient,
                    deserializer = AnimeListJsonStringDeserializer(),
                )

                val expectedEntries = listOf(
                    Anime(
                        title = "Seikai no Monshou",
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/1"),
                        ),
                        relatedAnime = hashSetOf(
                            URI("https://anidb.net/anime/1623"),
                            URI("https://anidb.net/anime/4"),
                            URI("https://anidb.net/anime/6"),
                        ),
                        type = TV,
                        episodes = 13,
                        picture = URI("https://cdn.anidb.net/images/main/224618.jpg"),
                        thumbnail = URI("https://cdn.anidb.net/images/main/224618.jpg-thumb.jpg"),
                        duration = Duration(
                            value = 25,
                            unit = MINUTES,
                        ),
                        score = ScoreValue(
                            arithmeticGeometricMean = 8.20,
                            arithmeticMean = 8.20,
                            median = 8.20
                        ),
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 1999,
                        ),
                        synonyms = hashSetOf(
                            "CotS",
                            "Crest of the Stars",
                            "Hvězdný erb",
                            "SnM",
                            "星界の紋章",
                            "星界之纹章",
                        ),
                        tags = hashSetOf(
                            "action",
                            "adventure",
                            "genetic modification",
                            "novel",
                            "science fiction",
                            "space travel",
                        ),
                    ),
                    Anime(
                        title = "Cowboy Bebop",
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/1"),
                        ),
                        relatedAnime = hashSetOf(
                            URI("https://myanimelist.net/anime/17205"),
                            URI("https://myanimelist.net/anime/4037"),
                            URI("https://myanimelist.net/anime/5"),
                        ),
                        type = TV,
                        episodes = 26,
                        picture = URI("https://cdn.myanimelist.net/images/anime/4/19644.jpg"),
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/4/19644t.jpg"),
                        duration = Duration(
                            value = 24,
                            unit = MINUTES,
                        ),
                        score = ScoreValue(
                            arithmeticGeometricMean = 8.75,
                            arithmeticMean = 8.75,
                            median = 8.75
                        ),
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 1998,
                        ),
                        synonyms = hashSetOf(
                            "カウボーイビバップ",
                        ),
                        tags = hashSetOf(
                            "action",
                            "adventure",
                            "comedy",
                            "drama",
                            "sci-fi",
                            "space",
                        ),
                    ),
                    Anime(
                        title = "Cowboy Bebop: Tengoku no Tobira",
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/5"),
                        ),
                        relatedAnime = hashSetOf(
                            URI("https://myanimelist.net/anime/1"),
                        ),
                        type = MOVIE,
                        episodes = 1,
                        picture = URI("https://cdn.myanimelist.net/images/anime/1439/93480.jpg"),
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1439/93480t.jpg"),
                        duration = Duration(
                            value = 115,
                            unit = MINUTES,
                        ),
                        score = ScoreValue(
                            arithmeticGeometricMean = 8.38,
                            arithmeticMean = 8.38,
                            median = 8.38
                        ),
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 1998,
                        ),
                        synonyms = hashSetOf(
                            "Cowboy Bebop: Knockin' on Heaven's Door",
                            "Cowboy Bebop: The Movie", "カウボーイビバップ 天国の扉",
                        ),
                        tags = hashSetOf(
                            "action",
                            "drama",
                            "mystery",
                            "sci-fi",
                            "space",
                        ),
                    ),
                    Anime(
                        title = "11 Eyes",
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/6751"),
                        ),
                        type = TV,
                        episodes = 12,
                        picture = URI("https://cdn.anidb.net/images/main/32901.jpg"),
                        thumbnail = URI("https://cdn.anidb.net/images/main/32901.jpg-thumb.jpg"),
                        duration = Duration(
                            value = 25,
                            unit = MINUTES,
                        ),
                        score = ScoreValue(
                            arithmeticGeometricMean = 5.88,
                            arithmeticMean = 5.88,
                            median = 5.88
                        ),
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2009,
                        ),
                        synonyms = hashSetOf(
                            "11 akių",
                            "11 глаз",
                            "11 چشم",
                            "11eyes",
                            "11eyes -罪與罰與贖的少女-",
                            "11eyes: Tsumi to Batsu to Aganai no Shoujo",
                            "أحد عشر عيناً",
                            "イレブンアイズ",
                            "罪与罚与赎的少女",
                        ),
                        tags = hashSetOf(
                            "action",
                            "angst",
                            "contemporary fantasy",
                            "ecchi",
                            "erotic game",
                            "fantasy",
                            "female student",
                            "seinen",
                            "super power",
                            "swordplay",
                            "visual novel",
                        ),
                    ),
                    Anime(
                        title = "11eyes",
                        sources = hashSetOf(
                            URI("https://anilist.co/anime/6682"),
                            URI("https://myanimelist.net/anime/6682"),
                        ),
                        relatedAnime = hashSetOf(
                            URI("https://anilist.co/anime/110465"),
                            URI("https://anilist.co/anime/7739"),
                            URI("https://myanimelist.net/anime/20557"),
                            URI("https://myanimelist.net/anime/7739"),
                        ),
                        type = TV,
                        episodes = 12,
                        picture = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx6682-ZptgLsCCNHjL.jpg"),
                        thumbnail = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/default.jpg"),
                        duration = Duration(
                            value = 25,
                            unit = MINUTES,
                        ),
                        score = ScoreValue(
                            arithmeticGeometricMean = 6.04,
                            arithmeticMean = 6.04,
                            median = 6.04
                        ),
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2009,
                        ),
                        synonyms = hashSetOf(
                            "11eyes -Tsumi to Batsu to Aganai no Shoujo-",
                            "11eyes イレブンアイズ",
                            "イレブンアイズ",
                        ),
                        tags = hashSetOf(
                            "action",
                            "demons",
                            "ecchi",
                            "ensemble cast",
                            "gore",
                            "magic",
                            "male protagonist",
                            "memory manipulation",
                            "revenge",
                            "super power",
                            "supernatural",
                            "survival",
                            "swordplay",
                            "tragedy",
                            "witch",
                        ),
                    ),
                )

                // when
                val result = externalResourceDeserializer.deserialize(URI("http://localhost$port/anime-offline-database.json").toURL())

                // then
                assertThat(result.data).containsExactlyInAnyOrder(*expectedEntries.toTypedArray())
            }
        }

        @Test
        fun `correctly download and deserialize zipped dataset file`() {
            runBlocking {
                // given
                val testHttpClient = object: HttpClient by TestHttpClient {
                    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                        code = 200,
                        body = loadTestResource<ByteArray>("json/deserialization/test_dataset_for_deserialization.zip"),
                        _headers = mutableMapOf("content-type" to setOf("application/zip")),
                    )
                }

                val externalResourceDeserializer = DefaultExternalResourceJsonDeserializer(
                    httpClient = testHttpClient,
                    deserializer = AnimeListJsonStringDeserializer(),
                )

                val expectedEntries = listOf(
                    Anime(
                        title = "Seikai no Monshou",
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/1"),
                        ),
                        relatedAnime = hashSetOf(
                            URI("https://anidb.net/anime/1623"),
                            URI("https://anidb.net/anime/4"),
                            URI("https://anidb.net/anime/6"),
                        ),
                        type = TV,
                        episodes = 13,
                        picture = URI("https://cdn.anidb.net/images/main/224618.jpg"),
                        thumbnail = URI("https://cdn.anidb.net/images/main/224618.jpg-thumb.jpg"),
                        duration = Duration(
                            value = 25,
                            unit = MINUTES,
                        ),
                        score = ScoreValue(
                            arithmeticGeometricMean = 8.20,
                            arithmeticMean = 8.20,
                            median = 8.20
                        ),
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 1999,
                        ),
                        synonyms = hashSetOf(
                            "CotS",
                            "Crest of the Stars",
                            "Hvězdný erb",
                            "SnM",
                            "星界の紋章",
                            "星界之纹章",
                        ),
                        tags = hashSetOf(
                            "action",
                            "adventure",
                            "genetic modification",
                            "novel",
                            "science fiction",
                            "space travel",
                        ),
                    ),
                    Anime(
                        title = "Cowboy Bebop",
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/1"),
                        ),
                        relatedAnime = hashSetOf(
                            URI("https://myanimelist.net/anime/17205"),
                            URI("https://myanimelist.net/anime/4037"),
                            URI("https://myanimelist.net/anime/5"),
                        ),
                        type = TV,
                        episodes = 26,
                        picture = URI("https://cdn.myanimelist.net/images/anime/4/19644.jpg"),
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/4/19644t.jpg"),
                        duration = Duration(
                            value = 24,
                            unit = MINUTES,
                        ),
                        score = ScoreValue(
                            arithmeticGeometricMean = 8.75,
                            arithmeticMean = 8.75,
                            median = 8.75
                        ),
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 1998,
                        ),
                        synonyms = hashSetOf(
                            "カウボーイビバップ",
                        ),
                        tags = hashSetOf(
                            "action",
                            "adventure",
                            "comedy",
                            "drama",
                            "sci-fi",
                            "space",
                        ),
                    ),
                    Anime(
                        title = "Cowboy Bebop: Tengoku no Tobira",
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/5"),
                        ),
                        relatedAnime = hashSetOf(
                            URI("https://myanimelist.net/anime/1"),
                        ),
                        type = MOVIE,
                        episodes = 1,
                        picture = URI("https://cdn.myanimelist.net/images/anime/1439/93480.jpg"),
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1439/93480t.jpg"),
                        duration = Duration(
                            value = 115,
                            unit = MINUTES,
                        ),
                        score = ScoreValue(
                            arithmeticGeometricMean = 8.38,
                            arithmeticMean = 8.38,
                            median = 8.38
                        ),
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 1998,
                        ),
                        synonyms = hashSetOf(
                            "Cowboy Bebop: Knockin' on Heaven's Door",
                            "Cowboy Bebop: The Movie", "カウボーイビバップ 天国の扉",
                        ),
                        tags = hashSetOf(
                            "action",
                            "drama",
                            "mystery",
                            "sci-fi",
                            "space",
                        ),
                    ),
                    Anime(
                        title = "11 Eyes",
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/6751"),
                        ),
                        type = TV,
                        episodes = 12,
                        picture = URI("https://cdn.anidb.net/images/main/32901.jpg"),
                        thumbnail = URI("https://cdn.anidb.net/images/main/32901.jpg-thumb.jpg"),
                        duration = Duration(
                            value = 25,
                            unit = MINUTES,
                        ),
                        score = ScoreValue(
                            arithmeticGeometricMean = 5.88,
                            arithmeticMean = 5.88,
                            median = 5.88
                        ),
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2009,
                        ),
                        synonyms = hashSetOf(
                            "11 akių",
                            "11 глаз",
                            "11 چشم",
                            "11eyes",
                            "11eyes -罪與罰與贖的少女-",
                            "11eyes: Tsumi to Batsu to Aganai no Shoujo",
                            "أحد عشر عيناً",
                            "イレブンアイズ",
                            "罪与罚与赎的少女",
                        ),
                        tags = hashSetOf(
                            "action",
                            "angst",
                            "contemporary fantasy",
                            "ecchi",
                            "erotic game",
                            "fantasy",
                            "female student",
                            "seinen",
                            "super power",
                            "swordplay",
                            "visual novel",
                        ),
                    ),
                    Anime(
                        title = "11eyes",
                        sources = hashSetOf(
                            URI("https://anilist.co/anime/6682"),
                            URI("https://myanimelist.net/anime/6682"),
                        ),
                        relatedAnime = hashSetOf(
                            URI("https://anilist.co/anime/110465"),
                            URI("https://anilist.co/anime/7739"),
                            URI("https://myanimelist.net/anime/20557"),
                            URI("https://myanimelist.net/anime/7739"),
                        ),
                        type = TV,
                        episodes = 12,
                        picture = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx6682-ZptgLsCCNHjL.jpg"),
                        thumbnail = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/default.jpg"),
                        duration = Duration(
                            value = 25,
                            unit = MINUTES,
                        ),
                        score = ScoreValue(
                            arithmeticGeometricMean = 6.04,
                            arithmeticMean = 6.04,
                            median = 6.04
                        ),
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2009,
                        ),
                        synonyms = hashSetOf(
                            "11eyes -Tsumi to Batsu to Aganai no Shoujo-",
                            "11eyes イレブンアイズ",
                            "イレブンアイズ",
                        ),
                        tags = hashSetOf(
                            "action",
                            "demons",
                            "ecchi",
                            "ensemble cast",
                            "gore",
                            "magic",
                            "male protagonist",
                            "memory manipulation",
                            "revenge",
                            "super power",
                            "supernatural",
                            "survival",
                            "swordplay",
                            "tragedy",
                            "witch",
                        ),
                    ),
                )

                // when
                val result = externalResourceDeserializer.deserialize(URI("http://localhost$port/anime-offline-database.zip").toURL())

                // then
                assertThat(result.data).containsExactlyInAnyOrder(*expectedEntries.toTypedArray())
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

                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    externalResourceDeserializer.deserialize(testResource("json/deserialization/non-json.zip"))
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

                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    externalResourceDeserializer.deserialize(testResource("json/deserialization/2_files.zip"))
                }

                // then
                assertThat(result).hasMessage("The zip file contains more than one file.")
            }
        }

        @Test
        fun `correctly deserialize dataset file`() {
            runBlocking {
                // given
                val testDeserializer = object : JsonDeserializer<List<Int>> by TestJsonDeserializer {
                    override suspend fun deserialize(json: String): List<Int> = listOf(1, 2, 4, 5)
                }

                val externalResourceDeserializer = DefaultExternalResourceJsonDeserializer(
                    httpClient = TestHttpClient,
                    deserializer = testDeserializer,
                )

                // when
                val result = externalResourceDeserializer.deserialize(testResource("json/deserialization/test_dataset_for_deserialization.json"))

                // then
                assertThat(result).containsExactlyInAnyOrder(1, 2, 4, 5)
            }
        }

        @Test
        fun `correctly deserialize zipped dataset file`() {
            runBlocking {
                // given
                val expectedEntries = listOf(
                    Anime(
                        title = "Seikai no Monshou",
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/1"),
                        ),
                        relatedAnime = hashSetOf(
                            URI("https://anidb.net/anime/1623"),
                            URI("https://anidb.net/anime/4"),
                            URI("https://anidb.net/anime/6"),
                        ),
                        type = TV,
                        episodes = 13,
                        picture = URI("https://cdn.anidb.net/images/main/224618.jpg"),
                        thumbnail = URI("https://cdn.anidb.net/images/main/224618.jpg-thumb.jpg"),
                        duration = Duration(
                            value = 25,
                            unit = MINUTES,
                        ),
                        score = ScoreValue(
                            arithmeticGeometricMean = 8.20,
                            arithmeticMean = 8.20,
                            median = 8.20
                        ),
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 1999,
                        ),
                        synonyms = hashSetOf(
                            "CotS",
                            "Crest of the Stars",
                            "Hvězdný erb",
                            "SnM",
                            "星界の紋章",
                            "星界之纹章",
                        ),
                        tags = hashSetOf(
                            "action",
                            "adventure",
                            "genetic modification",
                            "novel",
                            "science fiction",
                            "space travel",
                        ),
                    ),
                    Anime(
                        title = "Cowboy Bebop",
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/1"),
                        ),
                        relatedAnime = hashSetOf(
                            URI("https://myanimelist.net/anime/17205"),
                            URI("https://myanimelist.net/anime/4037"),
                            URI("https://myanimelist.net/anime/5"),
                        ),
                        type = TV,
                        episodes = 26,
                        picture = URI("https://cdn.myanimelist.net/images/anime/4/19644.jpg"),
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/4/19644t.jpg"),
                        duration = Duration(
                            value = 24,
                            unit = MINUTES,
                        ),
                        score = ScoreValue(
                            arithmeticGeometricMean = 8.75,
                            arithmeticMean = 8.75,
                            median = 8.75
                        ),
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 1998,
                        ),
                        synonyms = hashSetOf(
                            "カウボーイビバップ",
                        ),
                        tags = hashSetOf(
                            "action",
                            "adventure",
                            "comedy",
                            "drama",
                            "sci-fi",
                            "space",
                        ),
                    ),
                    Anime(
                        title = "Cowboy Bebop: Tengoku no Tobira",
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/5"),
                        ),
                        relatedAnime = hashSetOf(
                            URI("https://myanimelist.net/anime/1"),
                        ),
                        type = MOVIE,
                        episodes = 1,
                        picture = URI("https://cdn.myanimelist.net/images/anime/1439/93480.jpg"),
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1439/93480t.jpg"),
                        duration = Duration(
                            value = 115,
                            unit = MINUTES,
                        ),
                        score = ScoreValue(
                            arithmeticGeometricMean = 8.38,
                            arithmeticMean = 8.38,
                            median = 8.38
                        ),
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 1998,
                        ),
                        synonyms = hashSetOf(
                            "Cowboy Bebop: Knockin' on Heaven's Door",
                            "Cowboy Bebop: The Movie", "カウボーイビバップ 天国の扉",
                        ),
                        tags = hashSetOf(
                            "action",
                            "drama",
                            "mystery",
                            "sci-fi",
                            "space",
                        ),
                    ),
                    Anime(
                        title = "11 Eyes",
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/6751"),
                        ),
                        type = TV,
                        episodes = 12,
                        picture = URI("https://cdn.anidb.net/images/main/32901.jpg"),
                        thumbnail = URI("https://cdn.anidb.net/images/main/32901.jpg-thumb.jpg"),
                        duration = Duration(
                            value = 25,
                            unit = MINUTES,
                        ),
                        score = ScoreValue(
                            arithmeticGeometricMean = 5.88,
                            arithmeticMean = 5.88,
                            median = 5.88
                        ),
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2009,
                        ),
                        synonyms = hashSetOf(
                            "11 akių",
                            "11 глаз",
                            "11 چشم",
                            "11eyes",
                            "11eyes -罪與罰與贖的少女-",
                            "11eyes: Tsumi to Batsu to Aganai no Shoujo",
                            "أحد عشر عيناً",
                            "イレブンアイズ",
                            "罪与罚与赎的少女",
                        ),
                        tags = hashSetOf(
                            "action",
                            "angst",
                            "contemporary fantasy",
                            "ecchi",
                            "erotic game",
                            "fantasy",
                            "female student",
                            "seinen",
                            "super power",
                            "swordplay",
                            "visual novel",
                        ),
                    ),
                    Anime(
                        title = "11eyes",
                        sources = hashSetOf(
                            URI("https://anilist.co/anime/6682"),
                            URI("https://myanimelist.net/anime/6682"),
                        ),
                        relatedAnime = hashSetOf(
                            URI("https://anilist.co/anime/110465"),
                            URI("https://anilist.co/anime/7739"),
                            URI("https://myanimelist.net/anime/20557"),
                            URI("https://myanimelist.net/anime/7739"),
                        ),
                        type = TV,
                        episodes = 12,
                        picture = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx6682-ZptgLsCCNHjL.jpg"),
                        thumbnail = URI("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/default.jpg"),
                        duration = Duration(
                            value = 25,
                            unit = MINUTES,
                        ),
                        score = ScoreValue(
                            arithmeticGeometricMean = 6.04,
                            arithmeticMean = 6.04,
                            median = 6.04
                        ),
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2009,
                        ),
                        synonyms = hashSetOf(
                            "11eyes -Tsumi to Batsu to Aganai no Shoujo-",
                            "11eyes イレブンアイズ",
                            "イレブンアイズ",
                        ),
                        tags = hashSetOf(
                            "action",
                            "demons",
                            "ecchi",
                            "ensemble cast",
                            "gore",
                            "magic",
                            "male protagonist",
                            "memory manipulation",
                            "revenge",
                            "super power",
                            "supernatural",
                            "survival",
                            "swordplay",
                            "tragedy",
                            "witch",
                        ),
                    ),
                )

                val externalResourceDeserializer = DefaultExternalResourceJsonDeserializer(
                    httpClient = TestHttpClient,
                    deserializer = AnimeListJsonStringDeserializer(),
                )

                // when
                val result = externalResourceDeserializer.deserialize(testResource("json/deserialization/test_dataset_for_deserialization.zip"))

                // then
                assertThat(result.data).containsAll(expectedEntries)
            }
        }
    }
}