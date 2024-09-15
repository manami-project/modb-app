package io.github.manamiproject.modb.app.network

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.excludeFromTestContext
import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.httpclient.RequestBody
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.toDuration

/**
 * HttpClient which waits for the network controller to be active again if it is currently inactive.
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property networkController Access to the network controller which allows to its status.
 * @property httpClient Implementation of [HttpClient] which acts as delegate.
 */
class SuspendableHttpClient(
    private val appConfig: Config = AppConfig.instance,
    private val networkController: NetworkController = LinuxNetworkController.instance,
    private val httpClient: HttpClient = DefaultHttpClient(isTestContext = appConfig.isTestContext()),
): HttpClient {

    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
        suspendIfNecessary()
        return httpClient.get(url, headers)
    }

    override suspend fun post(
        url: URL,
        requestBody: RequestBody,
        headers: Map<String, Collection<String>>,
    ): HttpResponse {
        suspendIfNecessary()
        return httpClient.post(url, requestBody, headers)
    }

    private suspend fun suspendIfNecessary() = withContext(Default) {
        while(!networkController.isNetworkActive() && isActive) {
            log.info { "Waiting for network to be active again." }
            excludeFromTestContext(appConfig) { delay(10.toDuration(SECONDS)) }
        }
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [SuspendableHttpClient]
         * @since 1.0.0
         */
        val instance: SuspendableHttpClient by lazy { SuspendableHttpClient() }
    }
}