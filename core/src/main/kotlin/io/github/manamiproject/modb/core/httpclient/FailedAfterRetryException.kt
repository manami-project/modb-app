package io.github.manamiproject.modb.core.httpclient

/**
 * Maximum number of retries performed, but the request still failed.
 * @since 9.0.0
 * @param message Individual message.
 * @param cause [Throwable] as cause why this exception has been thrown.
 */
public class FailedAfterRetryException(message: String, cause: Throwable?) : RuntimeException(message, cause) {

    /**
     * @since 9.0.0
     * @param message Individual message
     */
    public constructor(message: String) : this(message, null)
}