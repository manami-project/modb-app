package io.github.manamiproject.modb.test

import org.junit.jupiter.api.AfterEach

/**
 * Starts a mock server for each test case and stops the server afterwards.
 * @since 1.0.0
 * @param T Instance type of the mock server.
 * @sample WireMockServerCreator
 */
public interface MockServerTestCase<T> {

    /**
     * Called after each test.
     * @since 1.0.0
     */
    @AfterEach
    public fun afterEach()

    /**
     * Concrete server instance.
     * @since 1.0.0
     */
    public val serverInstance: T

    /**
     * Port on which the mock server is listening.
     * @since 1.0.0
     */
    public val port: Int
}