package io.github.manamiproject.modb.app.crawler.anidb

import io.github.manamiproject.modb.anidb.AnidbResponseChecker
import io.github.manamiproject.modb.app.crawler.HighestIdDetector
import io.github.manamiproject.modb.app.extensions.checkedBody
import io.github.manamiproject.modb.app.network.SuspendableHttpClient
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.remove
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.XmlDataExtractor
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.logging.LoggerDelegate

/**
 * Detects the highest anime id based on the page showing latest additions.
 * @since 1.0.0
 * @property metaDataProviderConfig Configuration for a specific meta data provider.
 * @property httpClient Implementation of [HttpClient] which acts as delegate.
 * @property extractor Extractor which retrieves the data from raw data.
 */
class AnidbHighestIdDetector(
    private val metaDataProviderConfig: MetaDataProviderConfig = AnidbHighestIdDetectorConfig,
    private val httpClient: HttpClient = SuspendableHttpClient(),
    private val extractor: DataExtractor = XmlDataExtractor,
): HighestIdDetector {

    override suspend fun detectHighestId(): Int {
        log.info { "Fetching highest id for [${metaDataProviderConfig.hostname()}]." }

        val response = httpClient.get(metaDataProviderConfig.buildDataDownloadLink().toURL()).checkedBody(this::class)

        AnidbResponseChecker(response).checkIfCrawlerIsDetected()

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