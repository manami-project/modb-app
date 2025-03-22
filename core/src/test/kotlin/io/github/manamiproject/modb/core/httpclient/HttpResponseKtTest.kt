package io.github.manamiproject.modb.core.httpclient

import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

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
                body = EMPTY.toByteArray(),
                _headers = responseHeaders,
            )

            // then
            assertThat(result.headers).containsKey("cookie")
            assertThat(result.headers).doesNotContainKey("COOKIE")
            assertThat(result.headers).containsKey("x-csrf-token")
            assertThat(result.headers).doesNotContainKey("X-CSRF-TOKEN")
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
    }

    @Nested
    inner class BodyAsStringConstructorTests {

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
        fun `correctly converts body`() {
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
            assertThat(result.body).isEqualTo(body.toByteArray())
            assertThat(result.bodyAsText).isEqualTo(body)
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
    inner class  IsOkTests {

        @Test
        fun `returns true if code is 200`() {
            // given
            val httpResponse = HttpResponse(
                code = 200,
                body = EMPTY,
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
                body = EMPTY,
            )

            // when
            val result = httpResponse.isOk()

            // then
            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class  IsNotOkTests {

        @Test
        fun `returns false if code is 200`() {
            // given
            val httpResponse = HttpResponse(
                code = 200,
                body = EMPTY,
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
                body = EMPTY,
            )

            // when
            val result = httpResponse.isNotOk()

            // then
            assertThat(result).isTrue()
        }
    }

    @Nested
    inner class EqualityTests {

        @Test
        fun `returns true if objects are equal`() {
            // given
            val obj1 = HttpResponse(
                code = 200,
                body = "<html></html>".toByteArray(),
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            )

            val obj2 = HttpResponse(
                code = 200,
                body = "<html></html>".toByteArray(),
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            )

            // when
            val result = obj1 == obj2

            // then
            assertThat(result).isTrue()
            assertThat(obj1.hashCode()).isEqualTo(obj2.hashCode())
        }

        @Test
        fun `returns false if code differs`() {
            // given
            val obj1 = HttpResponse(
                code = 201,
                body = "<html></html>".toByteArray(),
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            )

            val obj2 = HttpResponse(
                code = 200,
                body = "<html></html>".toByteArray(),
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            )

            // when
            val result = obj1 == obj2

            // then
            assertThat(result).isFalse()
            assertThat(obj1.hashCode()).isNotEqualTo(obj2.hashCode())
        }

        @Test
        fun `returns false if body differs`() {
            // given
            val obj1 = HttpResponse(
                code = 200,
                body = "<html></header></html>".toByteArray(),
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            )

            val obj2 = HttpResponse(
                code = 200,
                body = "<html></html>".toByteArray(),
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            )

            // when
            val result = obj1 == obj2

            // then
            assertThat(result).isFalse()
            assertThat(obj1.hashCode()).isNotEqualTo(obj2.hashCode())
        }

        @Test
        fun `returns false if headers differ`() {
            // given
            val obj1 = HttpResponse(
                code = 200,
                body = "<html></html>".toByteArray(),
                _headers = mutableMapOf("content-type" to listOf("text/xhtml")),
            )

            val obj2 = HttpResponse(
                code = 200,
                body = "<html></html>".toByteArray(),
                _headers = mutableMapOf("content-type" to listOf("text/html")),
            )

            // when
            val result = obj1 == obj2

            // then
            assertThat(result).isFalse()
            assertThat(obj1.hashCode()).isNotEqualTo(obj2.hashCode())
        }
    }

    @Nested
    inner class BodyAsTextTests {

        @Test
        fun `bodyAsText returns the correct value`() {
            // given
            val bodyValue = "<html></html>"

            // when
            val result = HttpResponse(
                code = 200,
                body = bodyValue.toByteArray(),
            )

            // then
            assertThat(result.bodyAsText).isEqualTo(bodyValue)
        }
    }
}