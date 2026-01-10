package io.github.manamiproject.modb.core.httpclient

/**
 * @since 1.0.0
 */
public const val APPLICATION_JSON: String = "application/json"

/**
 * Data representing an HTTP request body.
 * @since 1.0.0
 * @property mediaType Actual media type of the request body which will be used in the content-type header.
 * @property body HTTP request body as [String].
 */
public data class RequestBody(
    val mediaType: String,
    val body: String,
)