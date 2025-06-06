package io.github.manamiproject.modb.core.httpclient

import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import java.io.InputStream
import java.io.InputStream.nullInputStream
import java.security.MessageDigest
import kotlin.text.Charsets.UTF_8

/**
 * @since 1.0.0
 */
public typealias HttpResponseCode = Int

/**
 * Data representing a HTTP response.
 * You can access the repsponse body either as [InputStream] for which the caller are responsible.
 * Or you can access it lazy loaded as [ByteArray] or [String]. The latter two options load the response into a
 * property of the class, allowing it to access it multiple times. But this also leads to the content being held in
 * memory. You can only use one of the two options [InputStream] vs [ByteArray]/[String], because the latter will read
 * the [InputStream] and then close it. If you read the [InputStream] yourself then the conent cannot be loaded
 * internally into memory anmyore. Possibly throwing an exception stating that the stream has already been closed.
 * @since 19.0.0
 * @property code Numerical HTTP response code.
 * @property _body Raw response body as [InputStream]. You have different options to access the responde body. Either as a [bodyAsString], [bodyAsByteArray] and [bodyAsStream].
 * @property _headers All HTTP header sent by the server.
 */
public data class HttpResponse(
    public val code: HttpResponseCode,
    private val _body: LifecycleAwareInputStream = LifecycleAwareInputStream(nullInputStream()),
    private val _headers: MutableMap<String, Collection<String>> = mutableMapOf(),
) {

    /**
     * Data representing a HTTP response.
     * @since 18.2.0
     * @param code Numerical HTTP response code.
     * @param body Raw response body as [String].
     * @param headers All HTTP header sent by the server.
     */
    public constructor(
        code: HttpResponseCode,
        body: String,
        headers: MutableMap<String, Collection<String>> = mutableMapOf(),
    ) : this(code, LifecycleAwareInputStream(body.byteInputStream()), headers)

    /**
     * Data representing a HTTP response.
     * @since 18.2.0
     * @param code Numerical HTTP response code.
     * @param body Raw response body as [ByteArray].
     * @param headers All HTTP header sent by the server.
     */
    public constructor(
        code: HttpResponseCode,
        body: ByteArray,
        headers: MutableMap<String, Collection<String>> = mutableMapOf(),
    ) : this(code, LifecycleAwareInputStream(body.inputStream()), headers)

    /**
     * HTTP headers sent by the server in lower case.
     * @since 1.0.0
     */
    public val headers: Map<String, Collection<String>>
        get() = _headers.toMap()

    init {
        require(code in 100..599) { "HTTP response code must be between 100 (inclusive) and 599 (inclusive), but was [$code]." }
        lowerCaseHeaders()
    }

    private var streamExhausted = false

    private val body: Lazy<ByteArray> = lazy {
        _body.use {
            streamExhausted = true
            it.readBytes()
        }
    }

    /**
     * Convenience function to indicate the status based on [code].
     * @since 1.0.0
     * @return `true` if the response code is 200.
     */
    public fun isOk(): Boolean = code == 200

    /**
     * Convenience function to indicate the status based on [code].
     * @since 18.2.0
     * @return `true` if the response code is anything but 200.
     */
    public fun isNotOk(): Boolean = code != 200

    /**
     * Returns the respone body as [String]. Use this to retrieve JSON or HTML as [String]. To retrieve binary payload use [bodyAsByteArray] or [bodyAsStream] to process it as [InputStream].
     * @since 19.0.0
     * @return Response body as [String].
     * @throws java.io.IOException if the stream has been consumed using [bodyAsStream].
     */
    public fun bodyAsString(): String = body.value.toString(UTF_8)

    /**
     * Returns the respone body as [ByteArray]. Use this to retrieve binary files. To retrieve textual payload use [bodyAsString] or [bodyAsStream] to process it as [InputStream].
     * @since 19.0.0
     * @return Response body as [ByteArray].
     * @throws java.io.IOException if the stream has been consumed using [bodyAsStream].
     */
    public fun bodyAsByteArray(): ByteArray = body.value

    /**
     * Check if the [InputStream] is still available for consumption.
     * You are free to consume the stream either using [bodyAsStream], [bodyAsByteArray] or [bodyAsString].
     * @since 19.0.0
     * @return `true` if the stream can still be consumed.
     */
    public fun isBodyInputStreamAvailable(): Boolean = !_body.isClosed()

    /**
     * Check if the [InputStream] has already been consumed.
     * If you consumed the stream implicitly by calling [bodyAsByteArray] or [bodyAsString] you can still call these
     * to retrieve the content. If you handled the stream yourself callig [bodyAsStream] then calling any of the
     * mentioned functions will throw an exception.
     * @since 19.0.0
     * @return `true` if the stream has already been closed..
     */
    public fun isBodyInputStreamExhausted(): Boolean = _body.isClosed()

    /**
     * Returns the respone body as [InputStream].
     * The stream can be read multiple times. This function works on the same stream, but it also handles the reset of
     * the stream for the caller.
     * Use this to process the stream on your own. To retrieve textual payload use [bodyAsString] or [bodyAsByteArray] for binary data.
     * @since 19.0.0
     * @return Response body as [InputStream]
     * @throws java.io.IOException if the stream has been consumed either using [bodyAsByteArray] or [bodyAsString].
     */
    public fun bodyAsStream(): InputStream = _body

    private fun lowerCaseHeaders() {
        val lowerCaseKeyMap = _headers.map {
            it.key.lowercase() to it.value
        }

        _headers.clear()
        _headers.putAll(lowerCaseKeyMap)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpResponse

        if (code != other.code) return false
        if (_headers != other._headers) return false
        if (_body.isClosed() && body.isInitialized()) {
            if (!MessageDigest.getInstance("SHA-256").digest(body.value).contentEquals(MessageDigest.getInstance("SHA-256").digest(other.bodyAsByteArray()))) return false
        } else if (_body !== other._body) return false

        return true
    }

    override fun hashCode(): Int {
        var result = code
        if (_body.isClosed() && body.isInitialized()) {
            result = 31 * result + MessageDigest.getInstance("SHA-256").digest(body.value).contentHashCode()
        }
        result = 31 * result + _headers.hashCode()
        return result
    }
}