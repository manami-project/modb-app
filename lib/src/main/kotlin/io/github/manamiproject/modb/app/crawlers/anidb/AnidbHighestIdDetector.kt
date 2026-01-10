package io.github.manamiproject.modb.app.crawlers.anidb

import io.github.manamiproject.modb.anidb.AnidbResponseChecker
import io.github.manamiproject.modb.anidb.CrawlerDetectedException
import io.github.manamiproject.modb.app.crawlers.HighestIdDetector
import io.github.manamiproject.modb.app.extensions.checkedBody
import io.github.manamiproject.modb.app.network.LinuxNetworkController
import io.github.manamiproject.modb.app.network.NetworkController
import io.github.manamiproject.modb.app.network.SuspendableHttpClient
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.remove
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.XmlDataExtractor
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.ThrowableRetryCase
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.UnknownHostException

/**
 * Detects the highest anime id based on the page showing latest additions.
 * @since 1.0.0
 * @property metaDataProviderConfig Configuration for a specific metadata provider.
 * @property httpClient Implementation of [HttpClient] which is used to retrieve the highest id.
 * @property extractor Extractor which retrieves the data from raw data.
 * @property networkController Access to the network controller which allows to perform a restart.
 */
class AnidbHighestIdDetector(
    private val metaDataProviderConfig: MetaDataProviderConfig = AnidbHighestIdDetectorConfig,
    private val httpClient: HttpClient = SuspendableHttpClient(),
    private val extractor: DataExtractor = XmlDataExtractor,
    private val networkController: NetworkController = LinuxNetworkController.instance,
): HighestIdDetector {

    init {
        val restart = suspend { networkController.restartAsync().join() }
        httpClient.addRetryCases(ThrowableRetryCase(executeBefore = restart){ it is ConnectException})
        httpClient.addRetryCases(ThrowableRetryCase(executeBefore = restart){ it is UnknownHostException})
        httpClient.addRetryCases(ThrowableRetryCase(executeBefore = restart){ it is NoRouteToHostException})
    }

    override suspend fun detectHighestId(): Int {
        log.info { "Fetching highest id for [${metaDataProviderConfig.hostname()}]." }

        val response = try {
            val response = httpClient.get(metaDataProviderConfig.buildDataDownloadLink().toURL()).checkedBody(this::class)
            AnidbResponseChecker(response).checkIfCrawlerIsDetected()
            response
        } catch (e: Throwable) {
            when(e) {
                is CrawlerDetectedException -> {
                    networkController.restartAsync().await()
                    val response = httpClient.get(metaDataProviderConfig.buildDataDownloadLink().toURL()).checkedBody(this::class)
                    AnidbResponseChecker(response).checkIfCrawlerIsDetected()
                    response
                }
                else -> throw e
            }
        }

        val data = extractor.extract(response, mapOf(
            "highestId" to "//div[contains(@class, 'latest_anime')]//table[@class='animelist']//td[contains(@class, 'name')]//a/@href",
        ))

        if (data.notFound("highestId")) {
            throw IllegalStateException("Unable to find highestId.")
        }

        return data.listNotNull<String>("highestId")
            .mapNotNull { it.remove("/anime/").toIntOrNull() }
            .maxOfOrNull { it } ?: throw IllegalStateException("Unable to extract highest id.")
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [AnidbHighestIdDetector]
         * @since 1.0.0
         */
        val instance: AnidbHighestIdDetector by lazy { AnidbHighestIdDetector() }
    }
}