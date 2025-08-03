package io.github.manamiproject.modb.app.crawlers.anisearch

import io.github.manamiproject.modb.app.convfiles.AlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.convfiles.DefaultAlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.crawlers.PaginationIdRangeSelector
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateScheduler
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateScheduler
import io.github.manamiproject.modb.app.extensions.checkedBody
import io.github.manamiproject.modb.app.network.LinuxNetworkController
import io.github.manamiproject.modb.app.network.NetworkController
import io.github.manamiproject.modb.app.network.SuspendableHttpClient
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.createShuffledList
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
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
 * Implementation of [PaginationIdRangeSelector].
 * Fetches the anime IDs from a given page and removes all IDs which are not scheduled for re-download and those
 * which have been downloaded already. If the crawler downloads the IDs which have been scheduled for re-download
 * first then the resulting list of this class will only contain completely new entries. The list is shuffled.
 * @since 1.0.0
 * @property metaDataProviderConfig Configuration for a specific meta data provider.
 * @property paginationIdRangeSelectorConfig Configuration which allows to download a specific page of a meta data provider.
 * @property httpClient Implementation of [HttpClient] which is used to download the selected pages.
 * @property networkController Access to the network controller which allows to perform a restart.
 * @property extractor Extractor which retrieves the data from raw data.
 * @property downloadControlStateScheduler Allows to check which anime are scheduled for re-download and which are not.
 * @property alreadyDownloadedIdsFinder Fetches all IDs which have already been downloaded.
 */
class AnisearchPaginationIdRangeSelector(
    private val metaDataProviderConfig: MetaDataProviderConfig,
    private val paginationIdRangeSelectorConfig: MetaDataProviderConfig = AnisearchPaginationIdRangeSelectorConfig,
    private val httpClient: HttpClient = SuspendableHttpClient(),
    private val networkController: NetworkController = LinuxNetworkController.instance,
    private val extractor: DataExtractor = XmlDataExtractor,
    private val downloadControlStateScheduler: DownloadControlStateScheduler = DefaultDownloadControlStateScheduler.instance,
    private val alreadyDownloadedIdsFinder: AlreadyDownloadedIdsFinder = DefaultAlreadyDownloadedIdsFinder.instance,
): PaginationIdRangeSelector<Int> {

    private val entriesNotScheduledForCurrentWeek = hashSetOf<AnimeId>()

    init {
        val restart = suspend { networkController.restartAsync().join() }
        httpClient.addRetryCases(ThrowableRetryCase(executeBefore = restart) { it is ConnectException})
        httpClient.addRetryCases(ThrowableRetryCase(executeBefore = restart) { it is UnknownHostException})
        httpClient.addRetryCases(ThrowableRetryCase(executeBefore = restart) { it is NoRouteToHostException})
    }

    override suspend fun idDownloadList(page: Int): List<AnimeId> {
        log.info { "Retrieving IDs for [${metaDataProviderConfig.hostname()}] from page [$page]" }

        if (entriesNotScheduledForCurrentWeek.isEmpty()) {
            entriesNotScheduledForCurrentWeek.addAll(downloadControlStateScheduler.findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig))
        }

        val resonse = downloadPage(page)

        val data = extractor.extract(resonse, mapOf(
            "entriesOnThePage" to "//table[@class='responsive-table mtC']//tbody//th//a/@href",
        ))

        if (data.notFound("entriesOnThePage")) {
            throw IllegalStateException("Unable to extract entriesOnThePage.")
        }

        val entriesOnThePage = data.listNotNull<String>("entriesOnThePage")
            .map { """/\d+,""".toRegex().find(it)?.value ?: EMPTY }
            .filter { it.neitherNullNorBlank() }
            .map { it.remove("/").remove(",") }
            .toHashSet()

        entriesOnThePage.removeAll(entriesNotScheduledForCurrentWeek)
        entriesOnThePage.removeAll(alreadyDownloadedIdsFinder.alreadyDownloadedIds(metaDataProviderConfig))
        return entriesOnThePage.toList().createShuffledList()
    }

    private suspend fun downloadPage(page: Int): String {
        return httpClient.get(
            url = paginationIdRangeSelectorConfig.buildDataDownloadLink(page.toString()).toURL(),
            headers = mapOf("host" to listOf("www.${paginationIdRangeSelectorConfig.hostname()}"))
        ).checkedBody(this::class)
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}