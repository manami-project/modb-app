package io.github.manamiproject.modb.core.httpclient

import io.github.manamiproject.modb.core.extensions.EMPTY
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class HttpResponseKtTest {

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

    @Nested
    inner class  IsOkTests {

        @Test
        fun `returns true if code is 200`() {
            // given
            val httpResponse = HttpResponse(
                code = 200,
                body = EMPTY.toByteArray(),
            )

            // when
            val result = httpResponse.isOk()

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `returns false if code is anything but 200`() {
            // given
            val httpResponse = HttpResponse(
                code = 201,
                body = EMPTY.toByteArray(),
            )

            // when
            val result = httpResponse.isOk()

            // then
            assertThat(result).isFalse()
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