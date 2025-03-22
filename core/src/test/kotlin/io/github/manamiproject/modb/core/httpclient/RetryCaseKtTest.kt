package io.github.manamiproject.modb.core.httpclient

import io.github.manamiproject.modb.core.extensions.EMPTY
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.SocketTimeoutException
import kotlin.test.Test

internal class RetryCaseKtTest {

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
    }
}