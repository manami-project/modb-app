package io.github.manamiproject.modb.app.crawlers.anisearch

import io.github.manamiproject.modb.app.crawlers.HighestIdDetector
import io.github.manamiproject.modb.app.extensions.checkedBody
import io.github.manamiproject.modb.app.network.LinuxNetworkController
import io.github.manamiproject.modb.app.network.NetworkController
import io.github.manamiproject.modb.app.network.SuspendableHttpClient
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.XmlDataExtractor
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.ThrowableRetryCase
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.UnknownHostException

/**
 * Determines the total number of pages by finding the number of the last page.
 * @since 1.0.0
 * @property metaDataProviderConfig Configuration for a specific meta data provider.
 * @property httpClient Implementation of [HttpClient] which is used to retrieve the number of the last page.
 * @property networkController Access to the network controller which allows to perform a restart.
 * @property extractor Extractor which retrieves the data from raw data.
 */
class AnisearchLastPageDetector(
    private val metaDataProviderConfig: MetaDataProviderConfig = AnisearchLastPageDetectorConfig,
    private val httpClient: HttpClient = SuspendableHttpClient(),
    private val networkController: NetworkController = LinuxNetworkController.instance,
    private val extractor: DataExtractor = XmlDataExtractor,
): HighestIdDetector {

    init {
        val restart = suspend { networkController.restartAsync().join() }
        httpClient.addRetryCases(ThrowableRetryCase(executeBefore = restart) { it is ConnectException})
        httpClient.addRetryCases(ThrowableRetryCase(executeBefore = restart) { it is UnknownHostException})
        httpClient.addRetryCases(ThrowableRetryCase(executeBefore = restart) { it is NoRouteToHostException})
    }

    override suspend fun detectHighestId(): Int {
        log.info { "Fetching max number of pages for [${metaDataProviderConfig.hostname()}]." }

        val response = download()

        val data = extractor.extract(response, mapOf(
            "lastPageNavEntry" to "//a[@class='pagenav-last']/@title",
        ))

        if (data.notFound("lastPageNavEntry")) {
            throw IllegalStateException("Unable to extract lastPageNavEntry.")
        }

        val lastPageNavEntry = data.listNotNull<String>("lastPageNavEntry").first()

        return """\d+""".toRegex().find(lastPageNavEntry)!!.value.toInt()
    }

    private suspend fun download(): String {
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