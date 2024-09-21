package io.github.manamiproject.modb.app.crawler.notify

import io.github.manamiproject.modb.app.convfiles.AlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.convfiles.DefaultAlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.crawler.IdRangeSelector
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateScheduler
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateScheduler
import io.github.manamiproject.modb.app.extensions.checkedBody
import io.github.manamiproject.modb.app.network.SuspendableHttpClient
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.createShuffledList
import io.github.manamiproject.modb.core.extractor.DataExtractor
import io.github.manamiproject.modb.core.extractor.XmlDataExtractor
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.logging.LoggerDelegate

/**
 * Creates a download list for notify.
 * Entries of type music are not included in type 'any' so there are two requests being made.
 * 1. Fetch the overview page for type 'any' and get all the anime IDs.
 * 2. Fetch the overview page for type 'music' and get all the anime IDs.
 * 3. Add all anime IDs scheduled for re-download this week. (just in case they might not be listed on the overview pages)
 * 4. Remove all anime IDs which are not scheduled to be re-downloaded this week.
 * 5. Remove all anime IDs which have already been downloaded.
 * 6. Shuffle the resulting list and return it.
 * @since 1.0.0
 * @property metaDataProviderConfig Configuration for a specific meta data provider.
 * @property httpClient Implementation of [HttpClient] used to retrieve the overview sites for types 'any' and 'music'.
 * @property extractor Extractor which retrieves the data from raw data.
 * @property downloadControlStateScheduler Allows to check which anime are scheduled for re-download and which are not.
 * @property alreadyDownloadedIdsFinder Fetch all IDs which have already been downloaded.
 */
class NotifyIdRangeSelector(
    private val metaDataProviderConfig: MetaDataProviderConfig = NotifyIdRangeSelectorConfig,
    private val httpClient: HttpClient = SuspendableHttpClient(),
    private val extractor: DataExtractor = XmlDataExtractor,
    private val downloadControlStateScheduler: DownloadControlStateScheduler = DefaultDownloadControlStateScheduler.instance,
    private val alreadyDownloadedIdsFinder: AlreadyDownloadedIdsFinder = DefaultAlreadyDownloadedIdsFinder.instance,
): IdRangeSelector<String> {

    override suspend fun idDownloadList(): List<String> {
        val possibleIds = mutableSetOf<String>()

        log.debug { "Checking page of type ANY for anime IDs" }
        possibleIds.addAll(identifyIds("any"))

        log.debug { "Checking page of type MUSIC for year" }
        possibleIds.addAll(identifyIds("music"))

        log.debug { "Having [${possibleIds.size}] for [${metaDataProviderConfig.hostname()}] before adding entries which are scheduled for the current week." }
        possibleIds.addAll(downloadControlStateScheduler.findEntriesScheduledForCurrentWeek(metaDataProviderConfig))
        log.debug { "Having [${possibleIds.size}] for [${metaDataProviderConfig.hostname()}] after adding entries which are scheduled for the current week." }

        log.debug { "Having [${possibleIds.size}] for [${metaDataProviderConfig.hostname()}] before excluding entries which are not scheduled for the current week." }
        possibleIds.removeAll(downloadControlStateScheduler.findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig))
        log.debug { "Having [${possibleIds.size}] for [${metaDataProviderConfig.hostname()}] after excluding entries which are not scheduled for the current week." }

        log.debug { "Having [${possibleIds.size}] for [${metaDataProviderConfig.hostname()}] before excluding already downloaded entries." }
        possibleIds.removeAll(alreadyDownloadedIdsFinder.alreadyDownloadedIds(metaDataProviderConfig))
        log.debug { "Having [${possibleIds.size}] for [${metaDataProviderConfig.hostname()}] after excluding already downloaded entries." }

        return possibleIds.toList().createShuffledList()
    }

    private suspend fun identifyIds(type: String): List<String> {
        val response = httpClient.get(metaDataProviderConfig.buildDataDownloadLink(type).toURL())
            .checkedBody(this::class)

        val data = extractor.extract(response, mapOf(
            "idRange" to "//div[@class='anime-grid']/div/a/@href",
        ))

        if (data.notFound("idRange")) {
            throw IllegalStateException("Unable to extract idRange.")
        }

        return data.listNotNull<String>("idRange").map { it.replace("/anime/", EMPTY) }
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [NotifyIdRangeSelector]
         * @since 1.0.0
         */
        val instance: NotifyIdRangeSelector by lazy { NotifyIdRangeSelector() }
    }
}