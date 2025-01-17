package io.github.manamiproject.modb.core.httpclient

/**
 * Configuration to individualize the behavior of [DefaultHttpClient].
 * You can determine when a retry is performed and how long to wait before the next attempt.
 * @since 9.0.0
 * @param maxAttempts Number of times a request should be retried before failing completly.
 * @param cases Contains all cases for which a retry will be performed.
 */
public data class RetryBehavior(
    val maxAttempts: Int = 5,
    private val cases: MutableMap<(HttpResponse) -> Boolean, RetryCase> = mutableMapOf(),
) {

    /**
     * Adds cases that describe when to perform a retry.
     * @since 9.0.0
     * @param retryCases Can take a single or multiple [RetryCase]s.
     */
    public fun addCases(vararg retryCases: RetryCase) {
        retryCases.forEach {
            cases[it.retryIf] = it
        }
    }

    /**
     * Checks whether a [HttpResponse] requires to perform a retry.
     * @since 9.0.0
     * @param httpResponse The response object which is used to check whether a retry is necessary or not.
     * @return `true` if the given [HttpResponse] matches one of the cases triggering a retry.
     */
    public fun requiresRetry(httpResponse: HttpResponse): Boolean = cases.keys.any { it.invoke(httpResponse) }

    /**
     * Fetch the retry based on a [HttpResponse].
     * @since 9.0.0
     * @param httpResponse The response object to make the lookup for.
     * @return [RetryCase] containing the configuration for this specific case based on the given response object.
     */
    public fun retryCase(httpResponse: HttpResponse): RetryCase = cases.values.first { it.retryIf.invoke(httpResponse) }
}