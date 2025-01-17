package io.github.manamiproject.modb.test

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

/**
 * Starts a [WireMockServer] on a random port and stops it after the test.
 * @since 1.0.0
 */
public class WireMockServerCreator : MockServerTestCase<WireMockServer> {

    override val serverInstance: WireMockServer = initServer()

    override val port: Int = serverInstance.port()

    override fun afterEach() {
        serverInstance.stop()
    }

    private fun initServer(): WireMockServer {
        return WireMockServer(
            wireMockConfig()
                .dynamicPort()
                .globalTemplating(true)
        ).apply {
            start()
        }
    }
}