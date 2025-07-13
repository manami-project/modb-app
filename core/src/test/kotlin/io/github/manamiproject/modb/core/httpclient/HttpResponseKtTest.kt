package io.github.manamiproject.modb.core.httpclient

import io.github.manamiproject.modb.core.TestReadOnceInputStream
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.IOException
import kotlin.test.Test
import kotlin.text.Charsets.UTF_8


internal class HttpResponseKtTest {

    @Nested
    inner class DefaultConstructorTests {

        @Test
        fun `response headers are converted to lower case keys`() {
            // given
            val responseHeaders = mutableMapOf<String, Collection<String>>().apply {
                put("COOKIE", emptyList())
                put("X-CSRF-TOKEN", emptyList())
            }

            // when
            val result = HttpResponse(
                code = 200,
                _headers = responseHeaders,
            )

            // then
            assertThat(result.headers).containsKey("cookie")
            assertThat(result.headers).doesNotContainKey("COOKIE")
            assertThat(result.headers).containsKey("x-csrf-token")
            assertThat(result.headers).doesNotContainKey("X-CSRF-TOKEN")
        }

        @Test
        fun `default response headers are empty`() {
            // when
            val result = HttpResponse(
                code = 200,
            )

            // then
            assertThat(result.headers).isEmpty()
        }

        @Test
        fun `default body is empty`() {
            // when
            val result = HttpResponse(
                code = 200,
            )

            // then
            assertThat(result.bodyAsStream().readAllBytes()).isEmpty()
        }

        @ParameterizedTest
        @ValueSource(ints = [-1, 0, 1, 99, 600])
        fun `throws exception if http response code is not within the valid range`(input: Int) {
            // when
            val result = exceptionExpected<IllegalArgumentException> {
                HttpResponse(
                    code = input,
                )
            }

            // then
            assertThat(result).hasMessage("HTTP response code must be between 100 (inclusive) and 599 (inclusive), but was [$input].")
        }
    }

    @Nested
    inner class ConstructorBodyAsStringTests {

        @Test
        fun `response headers are converted to lower case keys`() {
            // given
            val responseHeaders = mutableMapOf<String, Collection<String>>().apply {
                put("COOKIE", emptyList())
                put("X-CSRF-TOKEN", emptyList())
            }

            // when
            val result = HttpResponse(
                code = 200,
                body = EMPTY,
                headers = responseHeaders,
            )

            // then
            assertThat(result.headers).containsKey("cookie")
            assertThat(result.headers).doesNotContainKey("COOKIE")
            assertThat(result.headers).containsKey("x-csrf-token")
            assertThat(result.headers).doesNotContainKey("X-CSRF-TOKEN")
        }

        @Test
        fun `default response headers are empty`() {
            // when
            val result = HttpResponse(
                code = 200,
                body = EMPTY,
            )

            // then
            assertThat(result.headers).isEmpty()
        }

        @ParameterizedTest
        @ValueSource(ints = [-1, 0, 1, 99, 600])
        fun `throws exception if http response code is not within the valid range`(input: Int) {
            // when
            val result = exceptionExpected<IllegalArgumentException> {
                HttpResponse(
                    code = input,
                    body = EMPTY,
                )
            }

            // then
            assertThat(result).hasMessage("HTTP response code must be between 100 (inclusive) and 599 (inclusive), but was [$input].")
        }

        @Test
        fun `correctly delegates body`() {
            // given
            val body = """
                Here is some text.
                Including multiple lines.
            """.trimIndent()

            // when
            val result = HttpResponse(
                code = 200,
                body = body,
            )

            // then
            assertThat(result.bodyAsStream().use { it.readBytes().toString(UTF_8) }).isEqualTo(body)
        }

        @Test
        fun `correctly delegates response code`() {
            // when
            val result = HttpResponse(
                code = 201,
                body = EMPTY,
            )

            // then
            assertThat(result.code).isEqualTo(201)
        }
    }

    @Nested
    inner class ConstructorBodyAsByteArrayTests {

        @Test
        fun `response headers are converted to lower case keys`() {
            // given
            val responseHeaders = mutableMapOf<String, Collection<String>>().apply {
                put("COOKIE", emptyList())
                put("X-CSRF-TOKEN", emptyList())
            }

            // when
            val result = HttpResponse(
                code = 200,
                body = EMPTY.toByteArray(),
                headers = responseHeaders,
            )

            // then
            assertThat(result.headers).containsKey("cookie")
            assertThat(result.headers).doesNotContainKey("COOKIE")
            assertThat(result.headers).containsKey("x-csrf-token")
            assertThat(result.headers).doesNotContainKey("X-CSRF-TOKEN")
        }

        @Test
        fun `default response headers are empty`() {
            // when
            val result = HttpResponse(
                code = 200,
                body = EMPTY.toByteArray(),
            )

            // then
            assertThat(result.headers).isEmpty()
        }

        @ParameterizedTest
        @ValueSource(ints = [-1, 0, 1, 99, 600])
        fun `throws exception if http response code is not within the valid range`(input: Int) {
            // when
            val result = exceptionExpected<IllegalArgumentException> {
                HttpResponse(
                    code = input,
                    body = EMPTY.toByteArray(),
                )
            }

            // then
            assertThat(result).hasMessage("HTTP response code must be between 100 (inclusive) and 599 (inclusive), but was [$input].")
        }

        @Test
        fun `correctly delegates body`() {
            // given
            val body = """
                Here is some text.
                Including multiple lines.
            """.trimIndent()

            // when
            val result = HttpResponse(
                code = 200,
                body = body.toByteArray(),
            )

            // then
            assertThat(result.bodyAsStream().use { it.readBytes().toString(UTF_8) }).isEqualTo(body)
        }

        @Test
        fun `correctly delegates response code`() {
            // when
            val result = HttpResponse(
                code = 201,
                body = EMPTY.toByteArray(),
            )

            // then
            assertThat(result.code).isEqualTo(201)
        }
    }

    @Nested
    inner class HeadersTests {

        @Test
        fun `cannot modify headers`() {
            // given
            val responseHeaders = mutableMapOf<String, Collection<String>>().apply {
                put("COOKIE", emptyList())
                put("X-CSRF-TOKEN", emptyList())
            }

            val httpResponse = HttpResponse(
                code = 200,
                body = EMPTY.toByteArray(),
                headers = responseHeaders,
            )

            // when
            (httpResponse.headers as MutableMap<*,*>).clear()

            //
            assertThat(httpResponse.headers).isNotEmpty()
        }
    }

    @Nested
    inner class IsOkTests {

        @Test
        fun `returns true if code is 200`() {
            // given
            val httpResponse = HttpResponse(
                code = 200,
            )

            // when
            val result = httpResponse.isOk()

            // then
            assertThat(result).isTrue()
        }

        @ParameterizedTest
        @ValueSource(ints = [201, 300, 400, 500])
        fun `returns false if code is anything but 200`(input: Int) {
            // given
            val httpResponse = HttpResponse(
                code = input,
            )

            // when
            val result = httpResponse.isOk()

            // then
            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class IsNotOkTests {

        @Test
        fun `returns false if code is 200`() {
            // given
            val httpResponse = HttpResponse(
                code = 200,
            )

            // when
            val result = httpResponse.isNotOk()

            // then
            assertThat(result).isFalse()
        }

        @ParameterizedTest
        @ValueSource(ints = [201, 300, 400, 500])
        fun `returns true if code is anything but 200`(input: Int) {
            // given
            val httpResponse = HttpResponse(
                code = input,
            )

            // when
            val result = httpResponse.isNotOk()

            // then
            assertThat(result).isTrue()
        }
    }

    @Nested
    inner class BodyAsStringTests {

        @Test
        fun `correctly returns body`() {
            // given
            val body = """
                Here is some text.
                Including multiple lines.
            """.trimIndent()

            val httpResponse = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream(LifecycleAwareInputStream(TestReadOnceInputStream(body.byteInputStream()))),
            )

            // when
            val result = httpResponse.bodyAsString()

            // then
            assertThat(result).isEqualTo(body)
        }

        @Test
        fun `can be read multiple times`() {
            // given
            val body = """
                Here is some text.
                Including multiple lines.
            """.trimIndent()

            val httpResponse = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream(TestReadOnceInputStream(body.byteInputStream())),
            )
            httpResponse.bodyAsString()

            // when
            val result = httpResponse.bodyAsString()

            // then
            assertThat(result).isEqualTo(body)
        }

        @Test
        fun `throws an error if bodyAsInputStream has been called prior`() {
            // given
            val body = """
                Here is some text.
                Including multiple lines.
            """.trimIndent()

            val httpResponse = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream(TestReadOnceInputStream(body.byteInputStream())),
            )
            httpResponse.bodyAsStream().use { it.readAllBytes() }

            // when
            val result = exceptionExpected<IOException> {
                httpResponse.bodyAsString()
            }

            // then
            assertThat(result).hasMessage("Stream closed")
        }
    }

    @Nested
    inner class BodyAsByteArrayTests {

        @Test
        fun `correctly returns body`() {
            // given
            val body = """
                Here is some text.
                Including multiple lines.
            """.trimIndent()

            val httpResponse = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream(TestReadOnceInputStream(body.byteInputStream())),
            )

            // when
            val result = httpResponse.bodyAsByteArray()

            // then
            assertThat(result).isEqualTo(body.toByteArray())
        }

        @Test
        fun `can be read multiple times`() {
            // given
            val body = """
                Here is some text.
                Including multiple lines.
            """.trimIndent()

            val httpResponse = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream(TestReadOnceInputStream(body.byteInputStream())),
            )
            httpResponse.bodyAsByteArray()

            // when
            val result = httpResponse.bodyAsByteArray()

            // then
            assertThat(result).isEqualTo(body.toByteArray())
        }

        @Test
        fun `throws an error if bodyAsInputStream has been called prior`() {
            // given
            val body = """
                Here is some text.
                Including multiple lines.
            """.trimIndent()

            val httpResponse = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream(TestReadOnceInputStream(body.byteInputStream())),
            )
            httpResponse.bodyAsStream().use { it.readAllBytes() }

            // when
            val result = exceptionExpected<IOException> {
                httpResponse.bodyAsByteArray()
            }

            // then
            assertThat(result).hasMessage("Stream closed")
        }
    }

    @Nested
    inner class BodyAsInputStreamTests {

        @Test
        fun `correctly returns body`() {
            // given
            val body = """
                Here is some text.
                Including multiple lines.
            """.trimIndent()

            val httpResponse = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream(TestReadOnceInputStream(body.byteInputStream())),
            )

            // when
            val result = httpResponse.bodyAsStream().use { it.readAllBytes() }

            // then
            assertThat(result).isEqualTo(body.toByteArray())
        }

        @Test
        fun `cannot be read multiple times`() {
            // given
            val body = """
                Here is some text.
                Including multiple lines.
            """.trimIndent()

            val httpResponse = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream(TestReadOnceInputStream(body.byteInputStream())),
            )
            httpResponse.bodyAsStream().use { it.readAllBytes() }

            println(httpResponse.bodyAsStream()::class.java)

            // when
            val result = exceptionExpected<IOException> {
                httpResponse.bodyAsStream().use { it.readAllBytes() }
            }

            // then
            assertThat(result).hasMessage("Stream closed")
        }
    }

    @Nested
    inner class IsStreamExhaustedTests {

        @Test
        fun `returns true for a stream consumed by bodyAsInputStream`() {
            // given
            val body = """
                Here is some text.
                Including multiple lines.
            """.trimIndent()

            val httpResponse = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream(TestReadOnceInputStream(body.byteInputStream())),
            )

            httpResponse.bodyAsStream().use { it.readAllBytes() }

            // when
            val result = httpResponse.isBodyInputStreamExhausted()

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `returns true for a stream consumed by bodyAsByteArray`() {
            // given
            val body = """
                Here is some text.
                Including multiple lines.
            """.trimIndent()

            val httpResponse = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream(TestReadOnceInputStream(body.byteInputStream())),
            )

            httpResponse.bodyAsByteArray()

            // when
            val result = httpResponse.isBodyInputStreamExhausted()

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `returns true for a stream consumed by bodyAsString`() {
            // given
            val body = """
                Here is some text.
                Including multiple lines.
            """.trimIndent()

            val httpResponse = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream(TestReadOnceInputStream(body.byteInputStream())),
            )

            httpResponse.bodyAsString()

            // when
            val result = httpResponse.isBodyInputStreamExhausted()

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `returns false if the stream hasn't been consumed yet`() {
            // given
            val body = """
                Here is some text.
                Including multiple lines.
            """.trimIndent()

            val httpResponse = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream(TestReadOnceInputStream(body.byteInputStream())),
            )

            // when
            val result = httpResponse.isBodyInputStreamExhausted()

            // then
            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class IsStreamAvailableTests {

        @Test
        fun `returns false for a stream consumed by bodyAsInputStream`() {
            // given
            val body = """
                Here is some text.
                Including multiple lines.
            """.trimIndent()

            val httpResponse = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream(TestReadOnceInputStream(body.byteInputStream())),
            )

            httpResponse.bodyAsStream().use { it.readAllBytes() }

            // when
            val result = httpResponse.isBodyInputStreamAvailable()

            // then
            assertThat(result).isFalse()
        }

        @Test
        fun `returns false for a stream consumed by bodyAsByteArray`() {
            // given
            val body = """
                Here is some text.
                Including multiple lines.
            """.trimIndent()

            val httpResponse = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream(TestReadOnceInputStream(body.byteInputStream())),
            )

            httpResponse.bodyAsByteArray()

            // when
            val result = httpResponse.isBodyInputStreamAvailable()

            // then
            assertThat(result).isFalse()
        }

        @Test
        fun `returns false for a stream consumed by bodyAsString`() {
            // given
            val body = """
                Here is some text.
                Including multiple lines.
            """.trimIndent()

            val httpResponse = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream(TestReadOnceInputStream(body.byteInputStream())),
            )

            httpResponse.bodyAsString()

            // when
            val result = httpResponse.isBodyInputStreamAvailable()

            // then
            assertThat(result).isFalse()
        }

        @Test
        fun `returns true if the stream hasn't been consumed yet`() {
            // given
            val body = """
                Here is some text.
                Including multiple lines.
            """.trimIndent()

            val httpResponse = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream(TestReadOnceInputStream(body.byteInputStream())),
            )

            // when
            val result = httpResponse.isBodyInputStreamAvailable()

            // then
            assertThat(result).isTrue()
        }
    }

    @Nested
    inner class CloseTests {

        @Test
        fun `correctly closes internal InputStream in case close is called directly`() {
            // given
            val body = """
                Here is some text.
                Including multiple lines.
            """.trimIndent()

            val httpResponse = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream(TestReadOnceInputStream(body.byteInputStream())),
            )

            val before = httpResponse.isBodyInputStreamAvailable()

            // when
            httpResponse.close()

            // then
            assertThat(before).isTrue()
            assertThat(httpResponse.isBodyInputStreamExhausted()).isTrue()
        }

        @Test
        fun `correctly closes internal InputStream if you autoclose it by using USE`() {
            // given
            val body = """
                Here is some text.
                Including multiple lines.
            """.trimIndent()

            val httpResponse = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream(TestReadOnceInputStream(body.byteInputStream())),
            )

            val before = httpResponse.isBodyInputStreamAvailable()

            // when
            httpResponse.use { }

            // then
            assertThat(before).isTrue()
            assertThat(httpResponse.isBodyInputStreamExhausted()).isTrue()
        }

        @Test
        fun `can combine bodyAsString with USE`() {
            // given
            val body = """
                Here is some text.
                Including multiple lines.
            """.trimIndent()

            val httpResponse = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream(TestReadOnceInputStream(body.byteInputStream())),
            )

            // when
            val result = httpResponse.use { it.bodyAsString() }

            // then
            assertThat(result).isEqualTo(body)
        }
    }

    @Nested
    inner class EqualityTests {

        @Test
        fun `returns true for same instance`() {
            // given
            val obj = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream("<html></html>".byteInputStream()),
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            )

            // when
            val result = obj.equals(obj)

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `returns false for different class`() {
            // given
            val obj = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream("<html></html>".byteInputStream()),
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            )

            // when
            val result = obj.equals(EMPTY)

            // then
            assertThat(result).isFalse()
        }

        @Test
        fun `returns false if code differs`() {
            // given
            val obj1 = HttpResponse(
                code = 201,
                _body = LifecycleAwareInputStream("<html></html>".byteInputStream()),
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            )

            val obj2 = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream("<html></html>".byteInputStream()),
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            )

            // when
            val result = obj1.equals(obj2)

            // then
            assertThat(result).isFalse()
            assertThat(obj1.hashCode()).isNotEqualTo(obj2.hashCode())
        }

        @Test
        fun `returns false if keys in headers differ - left side`() {
            // given
            val obj1 = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream("<html></html>".byteInputStream()),
                _headers = mutableMapOf("other" to listOf("text/html")),
            )

            val obj2 = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream("<html></html>".byteInputStream()),
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            )

            // when
            val result = obj1.equals(obj2)

            // then
            assertThat(result).isFalse()
            assertThat(obj1.hashCode()).isNotEqualTo(obj2.hashCode())
        }

        @Test
        fun `returns false if keys in headers differ - right side`() {
            // given
            val obj1 = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream("<html></html>".byteInputStream()),
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            )

            val obj2 = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream("<html></html>".byteInputStream()),
                _headers = mutableMapOf("other" to listOf("text/html")),
            )

            // when
            val result = obj1.equals(obj2)

            // then
            assertThat(result).isFalse()
            assertThat(obj1.hashCode()).isNotEqualTo(obj2.hashCode())
        }

        @Test
        fun `returns false if values in headers differ - left side`() {
            // given
            val obj1 = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream("<html></html>".byteInputStream()),
                _headers = mutableMapOf("content-type" to listOf("other")),
            )

            val obj2 = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream("<html></html>".byteInputStream()),
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            )

            // when
            val result = obj1.equals(obj2)

            // then
            assertThat(result).isFalse()
            assertThat(obj1.hashCode()).isNotEqualTo(obj2.hashCode())
        }

        @Test
        fun `returns false if values in headers differ - right side`() {
            // given
            val obj1 = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream("<html></html>".byteInputStream()),
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            )

            val obj2 = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream("<html></html>".byteInputStream()),
                _headers = mutableMapOf("content-type" to listOf("other")),
            )

            // when
            val result = obj1.equals(obj2)

            // then
            assertThat(result).isFalse()
            assertThat(obj1.hashCode()).isNotEqualTo(obj2.hashCode())
        }

        @Test
        fun `returns false if body has been retrieved either using bodyAsByteArray or bodyAsString and bodys differ`() {
            // given
            val obj1 = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream("<html>obj1</html>".byteInputStream()),
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            ).apply {
                bodyAsByteArray()
            }

            val obj2 = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream("<html>obj2</html>".byteInputStream()),
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            ).apply {
                bodyAsByteArray()
            }

            // when
            val result = obj1.equals(obj2)

            // then
            assertThat(result).isFalse()
            assertThat(obj1.hashCode()).isNotEqualTo(obj2.hashCode())
        }

        @Test
        fun `returns false if body hasn't been read yet and instances of the InputStream differ`() {
            // given
            val inputStream1 = LifecycleAwareInputStream("<html></html>".byteInputStream())
            val inputStream2 = LifecycleAwareInputStream("<html></html>".byteInputStream())

            val obj1 = HttpResponse(
                code = 200,
                _body = inputStream1,
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            )

            val obj2 = HttpResponse(
                code = 200,
                _body = inputStream2,
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            )

            // when
            val result = obj1.equals(obj2)

            // then
            assertThat(result).isFalse()
            assertThat(obj1.hashCode()).isNotEqualTo(obj2.hashCode())
        }

        @Test
        fun `returns true if body hasn't been read yet and instance of the InputStream is the same`() {
            // given
            val inputStream = LifecycleAwareInputStream("<html></html>".byteInputStream())

            val obj1 = HttpResponse(
                code = 200,
                _body = inputStream,
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            )

            val obj2 = HttpResponse(
                code = 200,
                _body = inputStream,
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            )

            // when
            val result = obj1.equals(obj2)

            // then
            assertThat(result).isTrue()
            assertThat(obj1.hashCode()).isEqualTo(obj2.hashCode())
        }

        @Test
        fun `returns true if body has been read either using bodyAsByteArray or bodyAsString`() {
            // given
            val obj1 = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream("<html></html>".byteInputStream()),
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            ).apply {
                bodyAsByteArray()
            }

            val obj2 = HttpResponse(
                code = 200,
                _body = LifecycleAwareInputStream("<html></html>".byteInputStream()),
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            ).apply {
                bodyAsByteArray()
            }

            // when
            val result = obj1.equals(obj2)

            // then
            assertThat(result).isTrue()
            assertThat(obj1.hashCode()).isEqualTo(obj2.hashCode())
        }
    }
}