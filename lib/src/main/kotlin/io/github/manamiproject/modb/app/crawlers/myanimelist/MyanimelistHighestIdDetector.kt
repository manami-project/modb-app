package io.github.manamiproject.modb.app.crawlers.myanimelist

import io.github.manamiproject.modb.app.crawlers.HighestIdDetector
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
 * @property httpClient Implementation of [HttpClient] which is used to retrieve the highest id.
 * @property extractor Extractor which retrieves the data from raw data.
 */
class MyanimelistHighestIdDetector(
    private val metaDataProviderConfig: MetaDataProviderConfig = MyanimelistHighestIdDetectorConfig,
    private val httpClient: HttpClient = SuspendableHttpClient(),
    private val extractor: DataExtractor = XmlDataExtractor,
): HighestIdDetector {

    override suspend fun detectHighestId(): Int {
        log.info { "Fetching highest id for [${metaDataProviderConfig.hostname()}]." }

        val response = httpClient.get(
            url = metaDataProviderConfig.buildDataDownloadLink().toURL(),
        ).checkedBody(this::class)

        val data = extractor.extract(response, mapOf(
            "urlOfTheMostRecentAddition" to "//a[contains(@class, 'hoverinfo_trigger')]/@href",
        ))

        if (data.notFound("urlOfTheMostRecentAddition")) {
            throw IllegalStateException("Unable to extract urlOfTheMostRecentAddition.")
        }

        val urlOfTheMostRecentAddition = data.listNotNull<String>("urlOfTheMostRecentAddition").first()

        return Regex("/[0-9]+/").find(urlOfTheMostRecentAddition)
            ?.value
            ?.remove("/")
            ?.toIntOrNull()
            ?: throw IllegalStateException("Unable to detect highest id for myanimelist.")
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [MyanimelistHighestIdDetector]
         * @since 1.0.0
         */
        val instance: MyanimelistHighestIdDetector by lazy { MyanimelistHighestIdDetector() }
    }
}