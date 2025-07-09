package io.github.manamiproject.modb.app.crawlers.livechart

import io.github.manamiproject.modb.app.convfiles.AlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.convfiles.DefaultAlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.crawlers.PaginationIdRangeSelector
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateScheduler
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateScheduler
import io.github.manamiproject.modb.app.extensions.checkedBody
import io.github.manamiproject.modb.app.network.SuspendableHttpClient
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.excludeFromTestContext
import io.github.manamiproject.modb.core.extensions.createShuffledList
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.XmlDataExtractor
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.random
import kotlinx.coroutines.delay
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration

/**
 * Implementation of [PaginationIdRangeSelector].
 * Fetches the anime IDs from a given page and removes all IDs which are not scheduled for re-download and those
 * which have been downloaded already. If the crawler downloads the IDs which have been scheduled for re-download
 * first then the resulting list of this class will only contain completely new entries. The list is shuffled.
 * @since 1.0.0
 * @property metaDataProviderConfig Configuration for a specific meta data provider.
 * @property httpClient Implementation of [HttpClient] which is used to download the selected pages.
 * @property extractor Extractor which retrieves the data from raw data.
 * @property downloadControlStateScheduler Allows to check which anime are scheduled for re-download and which are not.
 * @property alreadyDownloadedIdsFinder Fetches all IDs which have already been downloaded.
 */
class LivechartPaginationIdRangeSelector(
    private val metaDataProviderConfig: MetaDataProviderConfig = LivechartPaginationIdRangeSelectorConfig,
    private val httpClient: HttpClient = SuspendableHttpClient(),
    private val extractor: DataExtractor = XmlDataExtractor,
    private val downloadControlStateScheduler: DownloadControlStateScheduler = DefaultDownloadControlStateScheduler.instance,
    private val alreadyDownloadedIdsFinder: AlreadyDownloadedIdsFinder = DefaultAlreadyDownloadedIdsFinder.instance,
): PaginationIdRangeSelector<String> {

    private val entriesNotScheduledForCurrentWeek = hashSetOf<AnimeId>()

    override suspend fun idDownloadList(page: String): List<AnimeId> {
        log.info { "Retrieving IDs for [${metaDataProviderConfig.hostname()}] from page [$page]" }

        if (entriesNotScheduledForCurrentWeek.isEmpty()) {
            entriesNotScheduledForCurrentWeek.addAll(downloadControlStateScheduler.findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig))
        }

        wait()

        return httpClient.get(
            url = metaDataProviderConfig.buildDataDownloadLink(page).toURL(),
            headers = mapOf("host" to listOf("www.${metaDataProviderConfig.hostname()}")),
        ).use { response ->
            if (response.code == 404) {
                log.warn { "404 on LivechartPaginationIdRangeSelector" }
                return emptyList()
            }

            val data = extractor.extract(response.checkedBody(this::class), mapOf(
                "animeIdList" to "//article[@class='anime']/@data-anime-id",
                "emptyMessage" to "//section[@data-anime-card-list-target='emptyMessage']/text()",
            ))

            if (data.notFound("animeIdList") && !data.notFound("emptyMessage") && data.stringOrDefault("emptyMessage").contains("We don't have any anime to display here.")) {
                return emptyList()
            }

            if (data.notFound("animeIdList")) {
                throw IllegalStateException("Unable to extract animeIdList.")
            }

            val entriesOnThePage = data.listNotNull<String>("animeIdList")
                .map { it.trim() }
                .toHashSet()

            entriesOnThePage.removeAll(entriesNotScheduledForCurrentWeek)
            entriesOnThePage.removeAll(alreadyDownloadedIdsFinder.alreadyDownloadedIds(metaDataProviderConfig))
            entriesOnThePage.toList().createShuffledList()
        }
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
         * Singleton of [LivechartPaginationIdRangeSelector]
         * @since 1.0.0
         */
        val instance: LivechartPaginationIdRangeSelector by lazy { LivechartPaginationIdRangeSelector() }
    }
}