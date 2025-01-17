package io.github.manamiproject.modb.core.httpclient

import io.github.manamiproject.modb.core.random
import kotlin.time.Duration
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration

/**
 * Defines when a retry will take place and how long to wait before the next retry.
 * @since 9.0.0
 * @param waitDuration [Duration] to wait before the retry is actually executed.
 * @param retryIf The function that defines when to trigger a retry based on a [HttpResponse].
 */
public data class RetryCase(
    val waitDuration: (Int) -> Duration = { currentAttempt ->
        (random(120000, 240000) * currentAttempt).toDuration(MILLISECONDS)
    },
    val retryIf: (HttpResponse) -> Boolean,
)