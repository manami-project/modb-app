package io.github.manamiproject.modb.core.httpclient

import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.SocketTimeoutException
import kotlin.test.Test
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.toDuration

internal class RetryBehaviorTest {

    @Nested
    inner class AddCaseTests {

        @Test
        fun `HttpResponse - adding duplicates is not possible`() {
            // given
            val retryIf = { httpResponse: HttpResponse -> httpResponse.code == 403 }
            val retryBehavior = RetryBehavior().apply {
                addCases(
                    HttpResponseRetryCase(waitDuration = { _: Int -> 1.toDuration(SECONDS)}, retryIf=retryIf),
                )
            }

            // when
            retryBehavior.addCases(
                HttpResponseRetryCase(waitDuration = { _: Int -> 2.toDuration(SECONDS)}, retryIf=retryIf),
            )

            // then
            val value = retryBehavior.retryCase(HttpResponse(code = 403, body = EMPTY)).waitDuration.invoke(1)
            assertThat(value).isEqualTo(2.toDuration(SECONDS))
        }

        @Test
        fun `HttpResponse - last added value wins`() {
            // given
            val retryIf = { httpResponse: HttpResponse -> httpResponse.code == 403 }
            val retryBehavior = RetryBehavior()

            // when
            retryBehavior.addCases(
                HttpResponseRetryCase(waitDuration = { _: Int -> 1.toDuration(SECONDS)}, retryIf=retryIf),
                HttpResponseRetryCase(waitDuration = { _: Int -> 2.toDuration(SECONDS)}, retryIf=retryIf),
            )

            // then
            val value = retryBehavior.retryCase(HttpResponse(code = 403, body = EMPTY)).waitDuration.invoke(1)
            assertThat(value).isEqualTo(2.toDuration(SECONDS))
        }

        @Test
        fun `Throwable - adding duplicates is not possible`() {
            // given
            val retryIf = { throwable: Throwable -> throwable is SocketTimeoutException }
            val retryBehavior = RetryBehavior().apply {
                addCases(
                    ThrowableRetryCase(waitDuration = { _: Int -> 1.toDuration(SECONDS)}, retryIf=retryIf),
                )
            }

            // when
            retryBehavior.addCases(
                ThrowableRetryCase(waitDuration = { _: Int -> 2.toDuration(SECONDS)}, retryIf=retryIf),
            )

            // then
            val value = retryBehavior.retryCase(SocketTimeoutException()).waitDuration.invoke(1)
            assertThat(value).isEqualTo(2.toDuration(SECONDS))
        }

        @Test
        fun `Throwable - last added value wins`() {
            // given
            val retryIf = { throwable: Throwable -> throwable is SocketTimeoutException }
            val retryBehavior = RetryBehavior()

            // when
            retryBehavior.addCases(
                ThrowableRetryCase(waitDuration = { _: Int -> 1.toDuration(SECONDS)}, retryIf=retryIf),
                ThrowableRetryCase(waitDuration = { _: Int -> 2.toDuration(SECONDS)}, retryIf=retryIf),
            )

            // then
            val value = retryBehavior.retryCase(SocketTimeoutException()).waitDuration.invoke(1)
            assertThat(value).isEqualTo(2.toDuration(SECONDS))
        }

        @Test
        fun `NoRetry - adding NoRetry doesn nothing`() {
            // given
            val expectedRetryBehavior = RetryBehavior()
            val retryBehavior = RetryBehavior()

            // when
            retryBehavior.addCases(
                NoRetry,
                NoRetry,
                NoRetry,
                NoRetry,
            )

            // then
            assertThat(retryBehavior).usingRecursiveAssertion().isEqualTo(expectedRetryBehavior)
        }
    }

    @Nested
    inner class RequiresRetryHttpResponseRetryCaseTests {

        @Test
        fun `true if a corresponding case was found`() {
            // given
            val retryIf = { httpResponse: HttpResponse -> httpResponse.code == 403 }
            val retryBehavior = RetryBehavior().apply {
                addCases(
                    HttpResponseRetryCase(retryIf=retryIf),
                )
            }

            // when
            val result = retryBehavior.requiresRetry(HttpResponse(code = 403, body = EMPTY))

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `false if a matching case couldn't be found`() {
            // given
            val retryBehavior = RetryBehavior()

            // when
            val result = retryBehavior.requiresRetry(HttpResponse(code = 403, body = EMPTY))

            // then
            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class RequiresRetryThrowableRetryCaseTests {

        @Test
        fun `true if a corresponding case was found`() {
            // given
            val retryIf = { throwable: Throwable -> throwable is SocketTimeoutException }
            val retryBehavior = RetryBehavior().apply {
                addCases(
                    ThrowableRetryCase(retryIf=retryIf),
                )
            }

            // when
            val result = retryBehavior.requiresRetry(SocketTimeoutException())

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `false if a matching case couldn't be found`() {
            // given
            val retryBehavior = RetryBehavior()

            // when
            val result = retryBehavior.requiresRetry(SocketTimeoutException())

            // then
            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class RetryCaseHttpResponseRetryCaseTests {

        @Test
        fun `successfully returns the RetryCase matching the HttpResponse`() {
            // given
            val retryIf = { httpResponse: HttpResponse -> httpResponse.code == 403 }
            val retryCase = HttpResponseRetryCase(waitDuration = { _: Int -> 1.toDuration(SECONDS)}, retryIf=retryIf)
            val retryBehavior = RetryBehavior().apply {
                addCases(retryCase)
            }

            // when
            val result = retryBehavior.retryCase(HttpResponse(code = 403, body = EMPTY))

            // then
            assertThat(result).isEqualTo(retryCase)
        }

        @Test
        fun `throws no such element exception if a matching RetryCase doesn't exist`() {
            // given
            val retryBehavior = RetryBehavior()

            // when
            val result = exceptionExpected<NoSuchElementException> {
                retryBehavior.retryCase(HttpResponse(code = 403, body = EMPTY))
            }

            // then
            assertThat(result).hasMessage("Collection contains no element matching the predicate.")
        }
    }

    @Nested
    inner class RetryCaseHttpThrowableRetryCaseTests {

        @Test
        fun `successfully returns the RetryCase matching the HttpResponse`() {
            // given
            val retryIf = { throwable: Throwable -> throwable is SocketTimeoutException }
            val retryCase = ThrowableRetryCase(retryIf=retryIf)
            val retryBehavior = RetryBehavior().apply {
                addCases(retryCase)
            }

            // when
            val result = retryBehavior.retryCase(SocketTimeoutException())

            // then
            assertThat(result).isEqualTo(retryCase)
        }

        @Test
        fun `throws no such element exception if a matching RetryCase doesn't exist`() {
            // given
            val retryBehavior = RetryBehavior()

            // when
            val result = exceptionExpected<NoSuchElementException> {
                retryBehavior.retryCase(SocketTimeoutException())
            }

            // then
            assertThat(result).hasMessage("Collection contains no element matching the predicate.")
        }
    }
}