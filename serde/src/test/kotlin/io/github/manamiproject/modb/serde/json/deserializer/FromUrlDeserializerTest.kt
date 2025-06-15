package io.github.manamiproject.modb.serde.json.deserializer

import com.github.luben.zstd.ZstdOutputStream
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import io.github.manamiproject.modb.serde.TestDeserializer
import io.github.manamiproject.modb.serde.TestHttpClient
import io.github.manamiproject.modb.serde.json.models.Dataset
import io.github.manamiproject.modb.test.exceptionExpected
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URL
import kotlin.test.Test


internal class FromUrlDeserializerTest {

    @Test
    fun `throws exception if the response code is not 200`() {
        // given
        val testHttpClient = object: HttpClient by TestHttpClient {
            override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(500, "ERROR")
        }

        val deserializer = FromUrlDeserializer(
            httpClient = testHttpClient,
            deserializer = TestDeserializer<LifecycleAwareInputStream, Dataset>(),
        )

        // when
        val result = exceptionExpected<IllegalStateException> {
            deserializer.deserialize(URI("http://localhost/anime-offline-database.json").toURL())
        }

        // then
        assertThat(result).hasMessage("Error downloading file: HTTP response code was: [500]")
    }

    @Test
    fun `throws exception if the content-type is unsupported`() {
        // given
        val testHttpClient = object: HttpClient by TestHttpClient {
            override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                code = 200,
                headers = mutableMapOf("content-type" to listOf("plain/text")),
                body = "ERROR",
            )
        }

        val deserializer = FromUrlDeserializer(
            httpClient = testHttpClient,
            deserializer = TestDeserializer<LifecycleAwareInputStream, Dataset>(),
        )

        // when
        val result = exceptionExpected<IllegalStateException> {
            deserializer.deserialize(URI("http://localhost/anime-offline-database.json").toURL())
        }

        // then
        assertThat(result).hasMessage("Unsupported content-type: [plain/text]")
    }

    @Test
    fun `correctly delegates call for JSON`() {
        runBlocking {
            // given
            val testHttpClient = object: HttpClient by TestHttpClient {
                override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                    code = 200,
                    headers = mutableMapOf("content-type" to listOf("application/json")),
                    body = """{ "test": true }""",
                )
            }

            var invokedWith = EMPTY
            val testDeserializer = object : Deserializer<LifecycleAwareInputStream, Dataset> by TestDeserializer() {
                override suspend fun deserialize(source: LifecycleAwareInputStream): Dataset {
                    invokedWith = source.use { it.bufferedReader().readText() }
                    return Dataset(
                        `$schema` = URI("https://example.org"),
                        lastUpdate = "2020-01-01",
                        data = emptyList(),
                    )
                }
            }

            val deserializer = FromUrlDeserializer(
                httpClient = testHttpClient,
                deserializer = testDeserializer,
            )

            // when
            deserializer.deserialize(URI("http://localhost/anime-offline-database.json").toURL())

            // then
            assertThat(invokedWith).isEqualTo("""{ "test": true }""")
        }
    }

    @Test
    fun `correctly delegates call for ZSTD`() {
        runBlocking {
            // given
            val bos = ByteArrayOutputStream()
            bos.use { fos ->
                ZstdOutputStream(fos, 22).use { zstOut ->
                    zstOut.write("compressed data".toByteArray())
                }
            }
            val bais = ByteArrayInputStream(bos.toByteArray())

            val testHttpClient = object: HttpClient by TestHttpClient {
                override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = HttpResponse(
                    code = 200,
                    _headers = mutableMapOf("content-type" to listOf("application/zstd")),
                    _body = LifecycleAwareInputStream(bais),
                )
            }

            var invokedWith = EMPTY
            val testDeserializer = object : Deserializer<LifecycleAwareInputStream, Dataset> by TestDeserializer() {
                override suspend fun deserialize(source: LifecycleAwareInputStream): Dataset {
                    invokedWith = source.use { it.bufferedReader().readText() }
                    return Dataset(
                        `$schema` = URI("https://example.org"),
                        lastUpdate = "2020-01-01",
                        data = emptyList(),
                    )
                }
            }

            val deserializer = FromUrlDeserializer(
                httpClient = testHttpClient,
                deserializer = testDeserializer,
            )

            // when
            deserializer.deserialize(URI("http://localhost/anime-offline-database.json").toURL())

            // then
            assertThat(invokedWith).isEqualTo("compressed data")
        }
    }
}