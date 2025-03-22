package io.github.manamiproject.modb.core.httpclient

import okio.ByteString.Companion.toByteString

/**
 * @since 1.0.0
 */
public typealias HttpResponseCode = Int

/**
 * Data representing a HTTP response.
 * @since 10.0.0
 * @property code Numerical HTTP response code.
 * @property body Raw response body as [ByteArray]. You can access this property for binary payload. Alternatively use [HttpResponse.bodyAsText].
 * @property _headers All HTTP header sent by the server.
 */
public data class HttpResponse(
    public val code: HttpResponseCode,
    public val body: ByteArray,
    private val _headers: MutableMap<String, Collection<String>> = mutableMapOf(),
) {

    /**
     * Data representing a HTTP response.
     * @since 18.2.0
     * @param code Numerical HTTP response code.
     * @param body Raw response body as [String]. Internal representation is in form of a [ByteArray]. Use [body] to read the property as-is and [bodyAsText] to access the [String] representation.
     * @param headers All HTTP header sent by the server.
     */
    public constructor(
        code: HttpResponseCode,
        body: String,
        headers: MutableMap<String, Collection<String>> = mutableMapOf(),
    ) : this(code, body.toByteArray(), headers)

    /**
     * HTTP headers sent by the server in lower case.
     * @since 1.0.0
     */
    public val headers: Map<String, Collection<String>>
        get() = _headers

    /**
     * Returns the respone body as text. Use this to retrieve JSON or HTML as [String]. To retrieve binary payload use [HttpResponse.body].
     * @since 10.0.0
     */
    public val bodyAsText: String = body.toByteString().utf8()

    init {
        require(code in 100..599) { "HTTP response code must be between 100 (inclusive) and 599 (inclusive), but was [$code]." }
        lowerCaseHeaders()
    }

    private fun lowerCaseHeaders() {
        val lowerCaseKeyMap = _headers.map {
            it.key.lowercase() to it.value
        }

        _headers.clear()
        _headers.putAll(lowerCaseKeyMap)
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpResponse

        if (code != other.code) return false
        if (!body.contentEquals(other.body)) return false
        if (_headers != other._headers) return false

        return true
    }

    override fun hashCode(): Int {
        var result = code
        result = 31 * result + body.contentHashCode()
        result = 31 * result + _headers.hashCode()
        return result
    }
}