package io.github.manamiproject.modb.notify

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_NETWORK
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.withContext

/**
 * Downloads anime data from notify.moe
 * @since 1.0.0
 * @param metaDataProviderConfig Configuration for downloading data.
 * @param httpClient To actually download the anime data.
 */
public class NotifyDownloader(
    private val metaDataProviderConfig: MetaDataProviderConfig,
    private val httpClient: HttpClient = DefaultHttpClient(isTestContext = metaDataProviderConfig.isTestContext()),
) : Downloader {

    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String = withContext(LIMITED_NETWORK) {
        log.debug { "Downloading [notifyId=$id]" }

        val response = httpClient.get(metaDataProviderConfig.buildDataDownloadLink(id).toURL())

        check(response.bodyAsText.neitherNullNorBlank()) { "Response body was blank for [notifyId=$id] with response code [${response.code}]" }

        return@withContext when (response.code) {
            200 -> response.bodyAsText
            404 -> {
                onDeadEntry.invoke(id)
                EMPTY
            }
            else -> throw IllegalStateException("Unable to determine the correct case for [notifyId=$id], [responseCode=${response.code}]")
        }
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}