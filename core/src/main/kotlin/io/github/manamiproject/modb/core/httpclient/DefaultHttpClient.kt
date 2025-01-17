package io.github.manamiproject.modb.core.httpclient

import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_NETWORK
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.httpclient.BrowserType.DESKTOP
import io.github.manamiproject.modb.core.httpclient.HttpProtocol.HTTP_1_1
import io.github.manamiproject.modb.core.httpclient.HttpProtocol.HTTP_2
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.random
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
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
import java.time.LocalDateTime


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
 * @property retryBehavior [RetryBehavior] to use for each request.
 * @property isTestContext Whether this runs in the unit test context or not.
 */
public class DefaultHttpClient(
    proxy: Proxy = NO_PROXY,
    private val protocols: MutableList<HttpProtocol> = mutableListOf(HTTP_2, HTTP_1_1),
    private var okhttpClient: Call.Factory = sharedOkHttpClient,
    private val isTestContext: Boolean = false,
    private val headerCreator: HeaderCreator = DefaultHeaderCreator.instance,
    public val retryBehavior: RetryBehavior = defaultRetryBehavior,
) : HttpClient {

    init {
        if (okhttpClient is OkHttpClient) {
            okhttpClient = (okhttpClient as OkHttpClient).newBuilder()
                .protocols(mapHttpProtocols())
                .proxy(proxy)
                .build()
        }
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

        executeRetryable(request)
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

        executeRetryable(request)
    }

    private suspend fun executeRetryable(request: Request): HttpResponse = withContext(LIMITED_NETWORK) {
        var attempt = 0
        var response = okhttpClient.newCall(request).execute().toHttpResponse()

        while (attempt < retryBehavior.maxAttempts && isActive && (response.code == 0 || retryBehavior.requiresRetry(response))) {
            log.info { "Performing retry [${attempt+1}/${retryBehavior.maxAttempts}]" }

            if (!isTestContext) {
                val retryCase = retryBehavior.retryCase(response)
                delay(retryCase.waitDuration.invoke(attempt).inWholeMilliseconds)
            }

            if (response.code == 103) {
                log.warn { "Received HTTP status code 103. Deactivating HTTP/2." }
                protocols.remove(HTTP_2)
            }

            attempt++

            try {
                response = okhttpClient.newCall(request).execute().toHttpResponse()
            } catch (e: Throwable) {
                if (attempt == retryBehavior.maxAttempts) {
                    throw FailedAfterRetryException("Execution failed despite [$attempt] retry attempts.", e)
                } else {
                    log.warn { "[${e.javaClass.canonicalName}] was thrown calling [${request.method} ${request.url}]. Performing retry [${attempt +1}] after waiting time." }
                }
            }
        }

        if (retryBehavior.requiresRetry(response)) {
            throw FailedAfterRetryException("Execution failed despite [$attempt] retry attempts. Last invocation of [${request.method} ${request.url}] returned http status code [${response.code}]")
        }

        return@withContext response
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
        private var lastEviction = LocalDateTime.of(2024, 1, 1, 0, 0, 0)

        /**
         * Shared [OkHttpClient]. Useful, because this will result in a shared thread pool between different instances of [DefaultHttpClient].
         * [see](https://square.github.io/okhttp/4.x/okhttp/okhttp3/-ok-http-client/#customize-your-client-with-newbuilder)
         */
        private val sharedOkHttpClient: Call.Factory by lazy {
            OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .addInterceptor { chain ->
                    val request = chain.request()
                    return@addInterceptor try {
                        chain.proceed(request)
                    } catch (e: SocketTimeoutException) {
                        log.warn { "SocketTimeoutException on [${request.url}]. Retrying call." }

                        val difference = java.time.Duration.between(LocalDateTime.now(), lastEviction)

                        if (sharedOkHttpClient is OkHttpClient && difference.seconds >= 60L) {
                            log.info { "Evicting connection pool and performing retry due to SocketTimeoutException." }
                            (sharedOkHttpClient as OkHttpClient).connectionPool.evictAll()
                        } else {
                            runBlocking { delay(random(1500, 2500)) }
                        }

                        chain.proceed(request)
                    }
                }
                .build()
        }

        private val defaultRetryBehavior = RetryBehavior().apply {
            addCases(
                RetryCase { it.code in 500..599 },
                RetryCase { it.code == 425 },
                RetryCase { it.code == 429 },
                RetryCase { it.code == 103 },
            )
        }

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
