package io.github.manamiproject.modb.core.httpclient

import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.toDuration

internal class RetryBehaviorTest {

    @Nested
    inner class AddCaseTests {

        @Test
        fun `adding duplicates is not possible`() {
            // given
            val retryIf = { httpResponse: HttpResponse -> httpResponse.code == 403 }
            val retryBehavior = RetryBehavior().apply {
                addCases(
                    RetryCase(waitDuration = { _: Int -> 1.toDuration(SECONDS)}, retryIf=retryIf),
                )
            }

            // when
            retryBehavior.addCases(
                RetryCase(waitDuration = { _: Int -> 2.toDuration(SECONDS)}, retryIf=retryIf),
            )

            // then
            val value = retryBehavior.retryCase(HttpResponse(code = 403, body = EMPTY.toByteArray())).waitDuration.invoke(1)
            assertThat(value).isEqualTo(2.toDuration(SECONDS))
        }

        @Test
        fun `last added value wins`() {
            // given
            val retryIf = { httpResponse: HttpResponse -> httpResponse.code == 403 }
            val retryBehavior = RetryBehavior()

            // when
            retryBehavior.addCases(
                RetryCase(waitDuration = { _: Int -> 1.toDuration(SECONDS)}, retryIf=retryIf),
                RetryCase(waitDuration = { _: Int -> 2.toDuration(SECONDS)}, retryIf=retryIf),
            )

            // then
            val value = retryBehavior.retryCase(HttpResponse(code = 403, body = EMPTY.toByteArray())).waitDuration.invoke(1)
            assertThat(value).isEqualTo(2.toDuration(SECONDS))
        }
    }

    @Nested
    inner class RequiresRetryTests {

        @Test
        fun `true if a corresponding case was found`() {
            // given
            val retryIf = { httpResponse: HttpResponse -> httpResponse.code == 403 }
            val retryBehavior = RetryBehavior().apply {
                addCases(
                    RetryCase(waitDuration = { _: Int -> 1.toDuration(SECONDS)}, retryIf=retryIf),
                )
            }

            // when
            val result = retryBehavior.requiresRetry(HttpResponse(code = 403, body = EMPTY.toByteArray()))

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `false if a matching case couldn't be found`() {
            // given
            val retryBehavior = RetryBehavior()

            // when
            val result = retryBehavior.requiresRetry(HttpResponse(code = 403, body = EMPTY.toByteArray()))

            // then
            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class RetryCaseTests {

        @Test
        fun `successfully returns the RetryCase matching the HttpResponse`() {
            // given
            val retryIf = { httpResponse: HttpResponse -> httpResponse.code == 403 }
            val retryCase = RetryCase(waitDuration = { _: Int -> 1.toDuration(SECONDS)}, retryIf=retryIf)
            val retryBehavior = RetryBehavior().apply {
                addCases(retryCase)
            }

            // when
            val result = retryBehavior.retryCase(HttpResponse(code = 403, body = EMPTY.toByteArray()))

            // then
            assertThat(result).isEqualTo(retryCase)
        }

        @Test
        fun `throws no such element exception if a matching RetryCase doesn't exist`() {
            // given
            val retryBehavior = RetryBehavior()

            // when
            val result = exceptionExpected<NoSuchElementException> {
                retryBehavior.retryCase(HttpResponse(code = 403, body = EMPTY.toByteArray()))
            }

            // then
            assertThat(result).hasMessage("Collection contains no element matching the predicate.")
        }
    }
}