package io.github.manamiproject.modb.core.httpclient

import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_NETWORK
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.httpclient.BrowserType.DESKTOP
import io.github.manamiproject.modb.core.httpclient.HttpProtocol.HTTP_1_1
import io.github.manamiproject.modb.core.httpclient.HttpProtocol.HTTP_2
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.ByteString.Companion.encodeUtf8
import java.net.Proxy
import java.net.Proxy.NO_PROXY
import java.net.SocketTimeoutException
import java.net.URL


/**
 * Default HTTP client based on OKHTTP.
 * Handles individual retry behavior for a HTTP request.
 *
 * # Trigger
 * A retry will always execute the request first.
 * If this request failed based on the cases in the given [RetryBehavior] then the request will be retried the number of times
 * defined in [RetryBehavior.maxAttempts]. So for the worst case a request will be executed initial request + [RetryBehavior.maxAttempts] times.
 * **Example:** if [RetryBehavior.maxAttempts] is set to `3` worst case would be `4` executions in total.
 *
 * # Retry
 * + Waits for the amount of time defined in [RetryCase.waitDuration].
 * + Executes request again.
 * @since 9.0.0
 * @param proxy **Default** is [NO_PROXY]
 * @property protocols List of supported HTTP protocol versions in the order of preference. Default is `HTTP/2, HTTP/1.1`.
 * @property okhttpClient Instance of the OKHTTP client on which this client is based.
 * @property isTestContext Whether this runs in the unit test context or not.
 * @property headerCreator Creates default headers based on the selected browser type.
 * @property retryBehavior [RetryBehavior] to use for each request.
 */
public class DefaultHttpClient(
    proxy: Proxy = NO_PROXY,
    private val protocols: MutableList<HttpProtocol> = mutableListOf(HTTP_2, HTTP_1_1),
    private var okhttpClient: Call.Factory = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .build(),
    private val isTestContext: Boolean = false,
    private val headerCreator: HeaderCreator = DefaultHeaderCreator.instance,
    public val retryBehavior: RetryBehavior = RetryBehavior(),
) : HttpClient {

    init {
        if (okhttpClient is OkHttpClient) {
            okhttpClient = (okhttpClient as OkHttpClient).newBuilder()
                .protocols(mapHttpProtocols())
                .proxy(proxy)
                .build()
        }

        retryBehavior.addCases(
            HttpResponseRetryCase { it.code in 500..599 },
            HttpResponseRetryCase { it.code == 425 },
            HttpResponseRetryCase { it.code == 429 },
            HttpResponseRetryCase { it.code == 103 },
            ThrowableRetryCase { it is SocketTimeoutException },
        )
    }

    override suspend fun post(
        url: URL,
        requestBody: RequestBody,
        headers: Map<String, Collection<String>>,
    ): HttpResponse = withContext(LIMITED_NETWORK) {
        val requestHeaders = mutableMapOf<String, String>()
        requestHeaders.putAll(headerCreator.createHeadersFor(url, DESKTOP).mapKeys { it.key.lowercase() }.map { it.key to it.value.joinToString(",") })
        requestHeaders.putAll(headers.mapKeys { it.key.lowercase() }.map { it.key to it.value.joinToString(",") })
        requestHeaders["content-type"] = requestBody.mediaType

        require(requestBody.mediaType.neitherNullNorBlank()) { "MediaType must not be blank." }
        require(requestBody.body.neitherNullNorBlank()) { "The request's body must not be blank." }

        val request = Request.Builder()
            .post(requestBody.body.encodeUtf8().toRequestBody(requestBody.mediaType.toMediaType()))
            .url(url)
            .headers(requestHeaders.toHeaders())
            .build()

        retry(request)
    }

    override suspend fun get(
        url: URL,
        headers: Map<String, Collection<String>>,
    ): HttpResponse = withContext(LIMITED_NETWORK) {
        val requestHeaders = mutableMapOf<String, String>()
        requestHeaders.putAll(headerCreator.createHeadersFor(url, DESKTOP).mapKeys { it.key.lowercase() }.map { it.key to it.value.joinToString(",") })
        requestHeaders.putAll(headers.mapKeys { it.key.lowercase() }.map { it.key to it.value.joinToString(",") })

        val request = Request.Builder()
            .get()
            .url(url)
            .headers(requestHeaders.toHeaders())
            .build()

        retry(request)
    }

    private suspend fun retry(request: Request): HttpResponse = withContext(LIMITED_NETWORK) {
        var attempt = 0
        var retryCase: RetryCase = NoRetry
        var responseOrException: Any = HttpResponse(100, EMPTY)

        do {
            if (retryCase !is NoRetry) {
                log.debug { "Retry [$attempt/${retryBehavior.maxAttempts}] for [${request.method} ${request.url}]." }

                if ((responseOrException is HttpResponse) && (responseOrException.code == 103)) {
                    log.warn { "Received HTTP status code 103. Deactivating HTTP/2." }
                    protocols.remove(HTTP_2)
                }

                log.trace { "Executing statement prior to the retry of [${request.method} ${request.url}] if set." }
                retryCase.executeBefore()

                if (!isTestContext) {
                    log.trace { "Initiating waiting time for retry of [${request.method} ${request.url}]." }
                    delay(retryCase.waitDuration(attempt))
                }
            }

            responseOrException = safelyExecute {
                okhttpClient.newCall(request).execute().toHttpResponse()
            }

            if (retryCase !is NoRetry) {
                log.trace { "Executing statement after the retry of [${request.method} ${request.url}] if set." }
                retryCase.executeAfter()
            }

            if (requiresRetry(responseOrException)) {
                retryCase = fetchRetryCase(responseOrException)
            }
            attempt++
        } while (retryCase !is NoRetry && attempt <= retryBehavior.maxAttempts && isActive)

        return@withContext when (responseOrException) {
            is HttpResponse -> {
                if (responseOrException.isNotOk() && retryCase !is NoRetry && attempt >= retryBehavior.maxAttempts) {
                    throw FailedAfterRetryException("Execution failed despite [${attempt-1}] retry attempts. Last invocation of [${request.method} ${request.url}] returned http status code [${responseOrException.code}]")
                }
                responseOrException
            }
            is Throwable -> throw responseOrException
            else -> throw IllegalStateException("Unexpected type [${responseOrException.javaClass}] during call [${request.method} ${request.url}].")
        }
    }

    private fun safelyExecute(run: () -> Any): Any {
        return try {
            run()
        } catch (e: Throwable) {
            e
        }
    }

    private fun requiresRetry(obj: Any): Boolean {
        return when (obj) {
            is HttpResponse -> retryBehavior.requiresRetry(obj)
            is Throwable -> retryBehavior.requiresRetry(obj)
            else -> {
                log.warn { "Unexpected type [${obj.javaClass}]. Assuming that no retry is required." }
                false
            }
        }
    }

    private fun fetchRetryCase(obj: Any): RetryCase {
        return when (obj) {
            is HttpResponse -> retryBehavior.retryCase(obj)
            is Throwable -> retryBehavior.retryCase(obj)
            else -> {
                log.warn { "Unexpected type [${obj.javaClass}]. Returning NoRetry as RetryCase." }
                NoRetry
            }
        }
    }

    private fun mapHttpProtocols(): List<Protocol> {
        require(protocols.isNotEmpty()) { "Requires at least one http protocol version." }

        return protocols.map {
            when(it) {
                HTTP_2 -> okhttp3.Protocol.HTTP_2
                HTTP_1_1 -> okhttp3.Protocol.HTTP_1_1
            }
        }
    }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DefaultHttpClient]
         * @since 15.0.0
         */
        public val instance: DefaultHttpClient by lazy { DefaultHttpClient() }
    }
}

private fun Response.toHttpResponse() = HttpResponse(
    code = this.code,
    body = this.body?.bytes() ?: EMPTY.toByteArray(),
    _headers = this.headers.toMultimap().toMutableMap()
)
