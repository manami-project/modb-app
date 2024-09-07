package io.github.manamiproject.modb.app.extensions

import io.github.manamiproject.modb.core.httpclient.HttpResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

internal class HttpResponseExtensionsKtTest {

    @Test
    fun `successfully return response body`() {
        // given
        val response = HttpResponse(
            code = 200,
            body = "response body".toByteArray(),
        )

        // when
        val result = response.checkedBody(this::class)

        //then
        assertThat(result).isEqualTo("response body")
    }

    @Test
    fun `throws exceptions if response code is not 200`() {
        // given
        val response = HttpResponse(
            code = 500,
            body = "response body".toByteArray(),
        )

        // when
        val result = assertThrows<IllegalStateException> {
            response.checkedBody(this::class)
        }

        //then
        assertThat(result).hasMessage("HttpResponseExtensionsKtTest: Response code [500] is not 200.")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  "])
    fun `throws exceptions if response body is blank`(value: String) {
        // given
        val response = HttpResponse(
            code = 200,
            body = value.toByteArray(),
        )

        // when
        val result = assertThrows<IllegalStateException> {
            response.checkedBody(this::class)
        }

        //then
        assertThat(result).hasMessage("HttpResponseExtensionsKtTest: Response body was blank.")
    }
}