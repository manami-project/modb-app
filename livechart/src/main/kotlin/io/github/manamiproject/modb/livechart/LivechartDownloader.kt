package io.github.manamiproject.modb.livechart

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_NETWORK
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.XmlDataExtractor
import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpProtocol.HTTP_1_1
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.withContext

/**
 * Downloads anime data from livechart.me
 * @since 1.0.0
 * @param metaDataProviderConfig Configuration for downloading data.
 * @param httpClient To actually download the anime data.
 */
public class LivechartDownloader(
    private val metaDataProviderConfig: MetaDataProviderConfig = LivechartConfig,
    private val extractor: DataExtractor = XmlDataExtractor,
    private val httpClient: HttpClient = DefaultHttpClient(
        protocols = mutableListOf(HTTP_1_1),
        isTestContext = metaDataProviderConfig.isTestContext(),
    ),
): Downloader {

    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String = withContext(LIMITED_NETWORK) {
        log.debug { "Downloading [livechartId=$id]" }

        val response = httpClient.get(
            url = metaDataProviderConfig.buildDataDownloadLink(id).toURL(),
            headers = mapOf("host" to listOf("www.${metaDataProviderConfig.hostname()}")),
        )

        check(response.bodyAsText.neitherNullNorBlank()) { "Response body was blank for [livechartId=$id] with response code [${response.code}]" }

        val data = extractor.extract(response.bodyAsText, mapOf(
            "pageTitle" to "//title/text()",
        ))

        if (data.string("pageTitle").trim().startsWith("Excluded from the LiveChart.me Database")) {
            onDeadEntry.invoke(id)
            return@withContext EMPTY
        }

        return@withContext when(response.code) {
            200 -> response.bodyAsText
            404 -> {
                onDeadEntry.invoke(id)
                EMPTY
            }
            else -> throw IllegalStateException("Unable to determine the correct case for [livechartId=$id], [responseCode=${response.code}]")
        }
    }

    public companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [LivechartDownloader]
         * @since 3.1.0
         */
        public val instance: LivechartDownloader by lazy { LivechartDownloader() }
    }
}