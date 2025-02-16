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