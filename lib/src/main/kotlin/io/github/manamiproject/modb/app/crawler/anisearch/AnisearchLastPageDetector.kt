package io.github.manamiproject.modb.app.crawler.anisearch

import io.github.manamiproject.modb.app.crawler.HighestIdDetector
import io.github.manamiproject.modb.app.extensions.checkedBody
import io.github.manamiproject.modb.app.network.LinuxNetworkController
import io.github.manamiproject.modb.app.network.NetworkController
import io.github.manamiproject.modb.app.network.SuspendableHttpClient
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.XmlDataExtractor
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.UnknownHostException

/**
 * Determines the total number of pages by finding the number of the last page.
 * @since 1.0.0
 * @property metaDataProviderConfig Configuration for a specific meta data provider.
 * @property httpClient Implementation of [HttpClient] which is used to retrieve the number of the last page.
 * @property networkController Access to the network controller which allows perform a restart.
 * @property extractor Extractor which retrieves the data from raw data.
 */
class AnisearchLastPageDetector(
    private val metaDataProviderConfig: MetaDataProviderConfig = AnisearchLastPageDetectorConfig,
    private val httpClient: HttpClient = SuspendableHttpClient(),
    private val networkController: NetworkController = LinuxNetworkController.instance,
    private val extractor: DataExtractor = XmlDataExtractor,
): HighestIdDetector {

    override suspend fun detectHighestId(): Int {
        log.info { "Fetching max number of pages for [${metaDataProviderConfig.hostname()}]." }

        val response = try {
            downloadPage()
        } catch (e: Throwable) {
            when(e) {
                is ConnectException,
                is UnknownHostException,
                is NoRouteToHostException -> {
                    networkController.restartAsync().await()
                    downloadPage()
                }
                else -> throw e
            }
        }

        val data = extractor.extract(response, mapOf(
            "lastPageNavEntry" to "//a[@class='pagenav-last']/@title",
        ))

        if (data.notFound("lastPageNavEntry")) {
            throw IllegalStateException("Unable to extract lastPageNavEntry.")
        }

        val lastPageNavEntry = data.listNotNull<String>("lastPageNavEntry").first()

        return Regex("[0-9]+").find(lastPageNavEntry)!!.value.toInt()
    }

    private suspend fun downloadPage(): String {
        return httpClient.get(
            url = metaDataProviderConfig.buildDataDownloadLink().toURL(),
            headers = mapOf("host" to listOf("www.${metaDataProviderConfig.hostname()}"))
        ).checkedBody(this::class)
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [AnisearchLastPageDetector]
         * @since 1.0.0
         */
        val instance: AnisearchLastPageDetector by lazy { AnisearchLastPageDetector() }
    }
}