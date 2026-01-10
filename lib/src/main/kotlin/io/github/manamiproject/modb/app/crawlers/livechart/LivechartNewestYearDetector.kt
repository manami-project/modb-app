package io.github.manamiproject.modb.app.crawlers.livechart

import io.github.manamiproject.modb.app.crawlers.HighestIdDetector
import io.github.manamiproject.modb.app.extensions.checkedBody
import io.github.manamiproject.modb.app.network.SuspendableHttpClient
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.excludeFromTestContext
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.XmlDataExtractor
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.random
import kotlinx.coroutines.delay
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration

/**
 * Detects the furthest future year for which anime releases are scheduled.
 * @since 1.0.0
 * @property metaDataProviderConfig Configuration for a specific metadata provider.
 * @property httpClient Implementation of [HttpClient] which is used to download the site containing an overview of available years.
 * @property extractor Extractor which retrieves the data from raw data.
 */
class LivechartNewestYearDetector(
    private val metaDataProviderConfig: MetaDataProviderConfig = LivechartNewestYearDetectorConfig,
    private val httpClient: HttpClient = SuspendableHttpClient(),
    private val extractor: DataExtractor = XmlDataExtractor,
): HighestIdDetector {

    override suspend fun detectHighestId(): Int {
        log.info { "Fetching highest id for [${metaDataProviderConfig.hostname()}]." }

        wait()

        val response = httpClient.get(
            url = metaDataProviderConfig.buildDataDownloadLink().toURL(),
            headers = mapOf("host" to listOf("www.${metaDataProviderConfig.hostname()}")),
        ).checkedBody(this::class)

        val data = extractor.extract(response, mapOf(
            "headers" to "//ul[@class='grouped-list']/li//h5",
        ))

        if (data.notFound("headers")) {
            throw IllegalStateException("Unable to extract headers.")
        }

        val headers = data.listNotNull<String>("headers")

        return headers[1].trim().toInt()
    }

    @KoverIgnore
    private suspend fun wait() {
        excludeFromTestContext(metaDataProviderConfig) {
            delay(random(2000, 3500).toDuration(MILLISECONDS))
        }
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [LivechartNewestYearDetector]
         * @since 1.0.0
         */
        val instance: LivechartNewestYearDetector by lazy { LivechartNewestYearDetector() }
    }
}