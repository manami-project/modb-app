package io.github.manamiproject.modb.core.httpclient

/**
 * Configuration to individualize the behavior of [DefaultHttpClient].
 * You can determine when a retry is performed and how long to wait before the next attempt.
 * @since 9.0.0
 * @property maxAttempts Number of times a request should be retried before failing completly.
 * @property httpResponseRetryCases Contains all cases based on a [HttpResponse] for which a retry will be performed.
 * @property throwableRetryCases Contains all cases based on a [ThrowableRetryCase] for which a retry will be performed.
 */
public data class RetryBehavior(
    val maxAttempts: Int = 5,
    private val httpResponseRetryCases: MutableMap<(HttpResponse) -> Boolean, HttpResponseRetryCase> = mutableMapOf(),
    private val throwableRetryCases: MutableMap<(Throwable) -> Boolean, ThrowableRetryCase> = mutableMapOf(),
) {

    /**
     * Adds cases that describe when to perform a retry.
     * Any case of type [NoRetry] is ignored.
     * @since 9.0.0
     * @param retryCases Can take a single or multiple [HttpResponseRetryCase]s.
     * @return Same instance.
     */
    public fun addCases(vararg retryCases: RetryCase): RetryBehavior {
        retryCases.forEach {
            when (it) {
                is HttpResponseRetryCase -> httpResponseRetryCases[it.retryIf] = it
                is ThrowableRetryCase -> throwableRetryCases[it.retryIf] = it
                NoRetry -> {}
            }
        }

        return this
    }

    /**
     * Checks whether a [HttpResponse] requires to perform a retry.
     * @since 9.0.0
     * @param httpResponse The response object which is used to check whether a retry is necessary or not.
     * @return `true` if the given [HttpResponse] matches one of the cases triggering a retry.
     */
    public fun requiresRetry(httpResponse: HttpResponse): Boolean = httpResponseRetryCases.keys.any { it.invoke(httpResponse) }

    /**
     * Checks whether an Exception (any [Throwable]) requires to perform a retry.
     * @since 18.2.0
     * @param throwable An exception instance that has been thrown.
     * @return `true` if the given exception (any [Throwable]) matches one of the cases triggering a retry.
     */
    public fun requiresRetry(throwable: Throwable): Boolean = throwableRetryCases.keys.any { it.invoke(throwable) }

    /**
     * Fetch the retry based on a [HttpResponse].
     * @since 9.0.0
     * @param httpResponse The response object to make the lookup for.
     * @return [HttpResponseRetryCase] containing the configuration for this specific case based on the given response object.
     * @throws NoSuchElementException in case there is no [RetryCase] for the given [httpResponse]. Use [requiresRetry] to prevent this exception.
     * @see requiresRetry
     */
    public fun retryCase(httpResponse: HttpResponse): HttpResponseRetryCase = httpResponseRetryCases.values.first { it.retryIf.invoke(httpResponse) }


    /**
     * Fetch the retry based on a [Throwable].
     * @since 18.2.0
     * @param throwable The response object to make the lookup for.
     * @return [ThrowableRetryCase] containing the configuration for this specific case based on the given response object.
     * @throws NoSuchElementException in case there is no [RetryCase] for the given [throwable]. Use [requiresRetry] to prevent this exception.
     * @see requiresRetry
     */
    public fun retryCase(throwable: Throwable): ThrowableRetryCase = throwableRetryCases.values.first { it.retryIf.invoke(throwable) }
}