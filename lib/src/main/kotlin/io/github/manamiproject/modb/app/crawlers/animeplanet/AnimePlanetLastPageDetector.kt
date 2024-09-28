package io.github.manamiproject.modb.app.crawlers.animeplanet

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
 * Determines the total number of pages by finding the number of the last page.
 * @since 1.0.0
 * @property metaDataProviderConfig Configuration for a specific meta data provider.
 * @property httpClient Implementation of [HttpClient] which is used to retrieve the number of the last page.
 * @property extractor Extractor which retrieves the data from raw data.
 */
class AnimePlanetLastPageDetector(
    private val metaDataProviderConfig: MetaDataProviderConfig = AnimePlanetHighestIdDetectorConfig,
    private val httpClient: HttpClient = SuspendableHttpClient(),
    private val extractor: DataExtractor = XmlDataExtractor,
): HighestIdDetector {

    override suspend fun detectHighestId(): Int {
        log.info { "Fetching max number of pages for [${metaDataProviderConfig.hostname()}]." }

        wait()

        val response = httpClient.get(
            url = metaDataProviderConfig.buildDataDownloadLink().toURL(),
            headers = mapOf("host" to listOf("www.${metaDataProviderConfig.hostname()}")),
        ).checkedBody(this::class)

        val data = extractor.extract(response, mapOf(
            "lastPage" to "//div[@class='pagination aligncenter']//ul//li//a",
        ))

        if (data.notFound("lastPage")) {
            throw IllegalStateException("Couldn't find lastPage.")
        }

        return data.listNotNull<String>("lastPage")
            .mapNotNull { it.toIntOrNull() }
            .maxOrNull() ?: throw IllegalStateException("Couldn't extract the last page.")
    }

    @KoverIgnore
    private suspend fun wait() {
        excludeFromTestContext(metaDataProviderConfig) {
            delay(random(1000, 1200).toDuration(MILLISECONDS))
        }
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [AnimePlanetLastPageDetector]
         * @since 1.0.0
         */
        val instance: AnimePlanetLastPageDetector by lazy { AnimePlanetLastPageDetector() }
    }
}