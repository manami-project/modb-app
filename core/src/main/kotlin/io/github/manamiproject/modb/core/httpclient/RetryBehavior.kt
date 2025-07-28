package io.github.manamiproject.modb.core.httpclient

/**
 * Configuration to individualize the behavior of [DefaultHttpClient].
 * You can determine when a retry is performed and how long to wait before the next attempt.
 * @since 9.0.0
 * @property maxAttempts Number of times a request should be retried before failing completly.
 * @property httpResponseRetryCases Contains all cases based on a [HttpResponse] for which a retry will be performed.
 * @property throwableRetryCases Contains all cases based on a [ThrowableRetryCase] for which a retry will be performed.
 */
internal data class RetryBehavior(
    val maxAttempts: Int = 5,
    private val httpResponseRetryCases: MutableList<HttpResponseRetryCase> = mutableListOf(),
    private val throwableRetryCases: MutableList<ThrowableRetryCase> = mutableListOf(),
) {

    /**
     * Adds cases that describe when to perform a retry.
     * Any case of type [NoRetry] is ignored.
     * @since 9.0.0
     * @param retryCases Can take a single or multiple [HttpResponseRetryCase]s.
     * @return Same instance.
     */
    fun addCases(vararg retryCases: RetryCase): RetryBehavior {
        retryCases.forEach {
            when (it) {
                is HttpResponseRetryCase -> httpResponseRetryCases.add(it)
                is ThrowableRetryCase -> throwableRetryCases.add(it)
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
    fun requiresRetry(httpResponse: HttpResponse): Boolean = httpResponseRetryCases.reversed().any { it.retryIf.invoke(httpResponse) }

    /**
     * Checks whether an Exception (any [Throwable]) requires to perform a retry.
     * @since 18.2.0
     * @param throwable An exception instance that has been thrown.
     * @return `true` if the given exception (any [Throwable]) matches one of the cases triggering a retry.
     */
    fun requiresRetry(throwable: Throwable): Boolean = throwableRetryCases.reversed().any { it.retryIf.invoke(throwable) }

    /**
     * Fetch the retry based on a [HttpResponse].
     * @since 9.0.0
     * @param httpResponse The response object to make the lookup for.
     * @return [HttpResponseRetryCase] containing the configuration for this specific case based on the given response object.
     * @throws NoSuchElementException in case there is no [RetryCase] for the given [httpResponse]. Use [requiresRetry] to prevent this exception.
     * @see requiresRetry
     */
    fun retryCase(httpResponse: HttpResponse): HttpResponseRetryCase = httpResponseRetryCases.reversed().first { it.retryIf.invoke(httpResponse) }


    /**
     * Fetch the retry based on a [Throwable].
     * @since 18.2.0
     * @param throwable The response object to make the lookup for.
     * @return [ThrowableRetryCase] containing the configuration for this specific case based on the given response object.
     * @throws NoSuchElementException in case there is no [RetryCase] for the given [throwable]. Use [requiresRetry] to prevent this exception.
     * @see requiresRetry
     */
    fun retryCase(throwable: Throwable): ThrowableRetryCase = throwableRetryCases.reversed().first { it.retryIf.invoke(throwable) }
}