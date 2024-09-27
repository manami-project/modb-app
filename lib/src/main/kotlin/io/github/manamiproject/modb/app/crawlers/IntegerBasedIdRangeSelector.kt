package io.github.manamiproject.modb.app.crawlers

import io.github.manamiproject.modb.app.convfiles.AlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.convfiles.DefaultAlreadyDownloadedIdsFinder
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.dataset.DefaultDeadEntriesAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateScheduler
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateScheduler
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.createShuffledList
import io.github.manamiproject.modb.core.logging.LoggerDelegate

/**
 * Generates a sequence of anime IDs of type [Int] for a specific meta data provider.
 * 1. First the highest anime ID that the meta data provider has to offer is fetched.
 * 2. A sequence from 1 (inclusive) to the highest id (inclusive) is generated.
 * 3. Dead entries, anime which are not scheduled for the current week as well as already downloaded anime are removed
 * 4. Resulting list is shuffled and returned.
 * @since 1.0.0
 * @property metaDataProviderConfig Configuration for a specific meta data provider.
 * @property highestIdDetector Allows to retrieve the highest anime ID currently available on the meta data provider website.
 * @property deadEntriesAccessor Access to dead entries files.
 * @property downloadControlStateAccessor Access to DCS files.
 * @property downloadControlStateScheduler Allows to check which anime are scheduled for re-download and which are not.
 * @property alreadyDownloadedIdsFinder Fetches all IDs which have already been downloaded.
 */
class IntegerBasedIdRangeSelector(
    private val metaDataProviderConfig: MetaDataProviderConfig,
    private val highestIdDetector: HighestIdDetector,
    private val deadEntriesAccessor: DeadEntriesAccessor = DefaultDeadEntriesAccessor.instance,
    private val downloadControlStateAccessor: DownloadControlStateAccessor = DefaultDownloadControlStateAccessor.instance,
    private val downloadControlStateScheduler: DownloadControlStateScheduler = DefaultDownloadControlStateScheduler.instance,
    private val alreadyDownloadedIdsFinder: AlreadyDownloadedIdsFinder = DefaultAlreadyDownloadedIdsFinder.instance,
): IdRangeSelector<Int> {

    override suspend fun idDownloadList(): List<Int> {
        log.info { "Creating a list of IDs to download for [${metaDataProviderConfig.hostname()}]." }

        val highestId = highestIdDetector.detectHighestId()
        log.debug { "Highest ID for [${metaDataProviderConfig.hostname()}] is [$highestId]" }
        check(highestId > 0) { "Highest ID must be greater than 0" }

        val highestIdAlreadyInDataset = downloadControlStateAccessor.highestIdAlreadyInDataset(metaDataProviderConfig)
        check(highestId >= highestIdAlreadyInDataset) { "Quality assurance problem for [${metaDataProviderConfig.hostname()}]. Detected highest ID [$highestId] is smaller than the highest ID already in dataset [$highestIdAlreadyInDataset]." }

        val possibleIds = hashSetOf<Int>()
        for (i in 1..highestId) {
            possibleIds.add(i)
        }

        log.debug { "Having [${possibleIds.size}] for [${metaDataProviderConfig.hostname()}] before excluding dead entries." }
        possibleIds.removeAll(deadEntriesAccessor.fetchDeadEntries(metaDataProviderConfig).map { it.toInt() }.toSet())
        log.debug { "Having [${possibleIds.size}] for [${metaDataProviderConfig.hostname()}] after excluding dead entries." }

        log.debug { "Having [${possibleIds.size}] for [${metaDataProviderConfig.hostname()}] before excluding entries which are not scheduled for the current week." }
        possibleIds.removeAll(downloadControlStateScheduler.findEntriesNotScheduledForCurrentWeek(metaDataProviderConfig).map { it.toInt() }.toSet())
        log.debug { "Having [${possibleIds.size}] for [${metaDataProviderConfig.hostname()}] after excluding entries which are not scheduled for the current week." }

        log.debug { "Having [${possibleIds.size}] for [${metaDataProviderConfig.hostname()}] before excluding already downloaded entries." }
        possibleIds.removeAll(alreadyDownloadedIdsFinder.alreadyDownloadedIds(metaDataProviderConfig).map { it.toInt() }.toSet())
        log.debug { "Having [${possibleIds.size}] for [${metaDataProviderConfig.hostname()}] after excluding already downloaded entries." }

        return possibleIds.toList().createShuffledList()
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}