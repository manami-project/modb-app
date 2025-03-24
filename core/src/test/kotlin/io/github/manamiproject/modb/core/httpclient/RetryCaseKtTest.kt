package io.github.manamiproject.modb.core.httpclient

import io.github.manamiproject.modb.core.extensions.EMPTY
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.SocketTimeoutException
import kotlin.test.Test

internal class RetryCaseKtTest {

    @Nested
    inner class NoRetryTests {

        @Test
        fun `default duration`() {
            // given
            val retryCase = NoRetry

            // when
            val result = retryCase.waitDuration.invoke(5)

            // then
            assertThat(result.inWholeMilliseconds).isZero()
        }

        @Test
        fun `empty executeBefore`() {
            // given
            val retryCase = NoRetry
            val expected: suspend () -> Unit = {}

            // when
            val result = retryCase.executeBefore

            // then
            assertThat(result.toString()).isEqualTo(expected.toString())
        }

        @Test
        fun `empty executeAfter`() {
            // given
            val retryCase = NoRetry
            val expected: suspend () -> Unit = {}

            // when
            val result = retryCase.executeAfter

            // then
            assertThat(result.toString()).isEqualTo(expected.toString())
        }
    }

    @Nested
    inner class HttpResponseRetryCaseTests {

        @Test
        fun `default duration`() {
            // given
            val retryCase = HttpResponseRetryCase { it.isNotOk() }

            // when
            val result = retryCase.waitDuration.invoke(1)

            // then
            assertThat(result.inWholeMilliseconds).isBetween(120000L, 240000L)
        }

        @Test
        fun `retry if`() {
            // given
            val retryCase = HttpResponseRetryCase { it.isNotOk() }
            val retry = HttpResponse(429, EMPTY)
            val noRetry = HttpResponse(200, EMPTY)

            // when
            val resultTrue = retryCase.retryIf(retry)
            val resultFalse = retryCase.retryIf(noRetry)

            // then
            assertThat(resultTrue).isTrue()
            assertThat(resultFalse).isFalse()
        }

        @Test
        fun `empty executeBefore`() {
            // given
            val retryCase = HttpResponseRetryCase { it.isNotOk() }
            val expected: suspend () -> Unit = {}

            // when
            val result = retryCase.executeBefore

            // then
            assertThat(result.toString()).isEqualTo(expected.toString())
        }

        @Test
        fun `empty executeAfter`() {
            // given
            val retryCase = HttpResponseRetryCase { it.isNotOk() }
            val expected: suspend () -> Unit = {}

            // when
            val result = retryCase.executeAfter

            // then
            assertThat(result.toString()).isEqualTo(expected.toString())
        }
    }

    @Nested
    inner class ThrowableRetryCaseTests {

        @Test
        fun `default duration`() {
            // given
            val retryCase = ThrowableRetryCase { it is SocketTimeoutException }

            // when
            val result = retryCase.waitDuration.invoke(1)

            // then
            assertThat(result.inWholeMilliseconds).isBetween(120000L, 240000L)
        }

        @Test
        fun `retry if`() {
            // given
            val retryCase = ThrowableRetryCase { it is SocketTimeoutException }

            // when
            val resultTrue = retryCase.retryIf(SocketTimeoutException())
            val resultFalse = retryCase.retryIf(NullPointerException())

            // then
            assertThat(resultTrue).isTrue()
            assertThat(resultFalse).isFalse()
        }

        @Test
        fun `empty executeBefore`() {
            // given
            val retryCase = ThrowableRetryCase { it is SocketTimeoutException }
            val expected: suspend () -> Unit = {}

            // when
            val result = retryCase.executeBefore

            // then
            assertThat(result.toString()).isEqualTo(expected.toString())
        }

        @Test
        fun `empty executeAfter`() {
            // given
            val retryCase = ThrowableRetryCase { it is SocketTimeoutException }
            val expected: suspend () -> Unit = {}

            // when
            val result = retryCase.executeAfter

            // then
            assertThat(result.toString()).isEqualTo(expected.toString())
        }
    }
}