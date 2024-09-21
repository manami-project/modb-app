package io.github.manamiproject.modb.app.crawler.kitsu

import io.github.manamiproject.modb.app.crawler.HighestIdDetector
import io.github.manamiproject.modb.app.extensions.checkedBody
import io.github.manamiproject.modb.app.network.SuspendableHttpClient
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.logging.LoggerDelegate

/**
 * Detects the highest anime id based on the page showing latest additions.
 * @since 1.0.0
 * @property metaDataProviderConfig Configuration for a specific meta data provider.
 * @property httpClient Implementation of [HttpClient] which is used to retrieve the highest id.
 */
class KitsuHighestIdDetector(
    private val metaDataProviderConfig: MetaDataProviderConfig = KitsuHighestIdDetectorConfig,
    private val httpClient: HttpClient = SuspendableHttpClient(),
): HighestIdDetector {

    override suspend fun detectHighestId(): Int {
        log.info { "Fetching highest id for [${metaDataProviderConfig.hostname()}]." }

        val response = httpClient.get(
            url = metaDataProviderConfig.buildDataDownloadLink().toURL(),
        ).checkedBody(this::class)

        val kitsuResponse = Json.parseJson<KitsuResponse>(response)!!

        return kitsuResponse.data.first().id.toInt()
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [KitsuHighestIdDetector]
         * @since 1.0.0
         */
        val instance: KitsuHighestIdDetector by lazy { KitsuHighestIdDetector() }
    }
}

private data class KitsuResponse(
    val data: List<LatestAdditionEntry>
)

private data class LatestAdditionEntry(
    val id: String
)