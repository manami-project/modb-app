package io.github.manamiproject.modb.core.httpclient

import io.github.manamiproject.modb.core.random
import kotlin.time.Duration
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration

/**
 * Defines if a retry will take place and how long to wait before the next retry.
 * This is a sealed class which doesn't define the condition. See the respective implemenations for the possible
 * conditions.
 * @since 18.2.0
 * @property waitDuration [Duration] to wait before the retry is actually executed.
 * @property executeBefore This code is executed before the the retry takes place. **Default:** no operation
 * @property executeAfter This code is executed after the the retry took place. **Default:** no operation
 * @see NoRetry
 * @see HttpResponseRetryCase
 * @see ThrowableRetryCase
 */
public sealed class RetryCase(
    public open val waitDuration: (Int) -> Duration = { currentAttempt ->
        (random(120000, 240000) * currentAttempt).toDuration(MILLISECONDS)
    },
    public open val executeBefore: suspend () -> Unit = {},
    public open val executeAfter: suspend () -> Unit = {},
)

/**
 * Indicates that no retry should take place.
 * @since 18.2.0
 * @see RetryCase
 */
public data object NoRetry: RetryCase()

/**
 * Defines if a retry will take place and how long to wait before the next retry.
 * @since 9.0.0
 * @property waitDuration [Duration] to wait before the retry is actually executed.
 * @property executeBefore This code is executed before the the retry takes place. **Default:** no operation
 * @property executeAfter This code is executed after the the retry took place. **Default:** no operation
 * @property retryIf The function that defines when to trigger a retry based on a [HttpResponse].
 * @see RetryCase
 */
public data class HttpResponseRetryCase(
    override val waitDuration: (Int) -> Duration = { currentAttempt ->
        (random(120000, 240000) * currentAttempt).toDuration(MILLISECONDS)
    },
    override val executeBefore: suspend () -> Unit = {},
    override val executeAfter: suspend () -> Unit = {},
    val retryIf: (HttpResponse) -> Boolean,
): RetryCase(waitDuration, executeBefore, executeAfter)

/**
 * Defines if a retry will take place and how long to wait before the next retry.
 * @since 18.2.0
 * @property waitDuration [Duration] to wait before the retry is actually executed.
 * @property executeBefore This code is executed before the the retry takes place. **Default:** no operation
 * @property executeAfter This code is executed after the the retry took place. **Default:** no operation
 * @property retryIf The function that defines when to trigger a retry based on a [Throwable].
 * @see RetryCase
 */
public data class ThrowableRetryCase(
    override val waitDuration: (Int) -> Duration = { currentAttempt ->
        (random(120000, 240000) * currentAttempt).toDuration(MILLISECONDS)
    },
    override val executeBefore: suspend () -> Unit = {},
    override val executeAfter: suspend () -> Unit = {},
    val retryIf: (Throwable) -> Boolean,
): RetryCase(waitDuration, executeBefore, executeAfter)