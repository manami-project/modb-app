package io.github.manamiproject.modb.app.merging

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.merging.goldenrecords.DefaultGoldenRecordAccessor
import io.github.manamiproject.modb.app.merging.goldenrecords.GoldenRecordAccessor
import io.github.manamiproject.modb.app.merging.goldenrecords.PotentialGoldenRecord
import io.github.manamiproject.modb.app.merging.lock.DefaultMergeLockAccessor
import io.github.manamiproject.modb.app.merging.lock.MergeLockAccessor
import io.github.manamiproject.modb.app.merging.matching.DefaultMatchingProbabilityCalculator
import io.github.manamiproject.modb.app.merging.matching.MatchingProbabilityCalculator
import io.github.manamiproject.modb.app.merging.matching.MatchingProbabilityResult
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime

/**
 * Implementation of [MergingService].
 *
 * Merging is done in multiple run-throughs.
 * First run-through is done with the anime of the meta data provider which has the most anime.
 * This initially populates the golden records list. Next run-throughs check anime per meta data provider sorted by
 * the number of anime in descending order.
 *
 * Either a merge lock exists which means that the merging logic is skipped or a list of potential golden records is
 * retrieved based on the title. Doing a lookup based on titles scales a lot better than calculating the matching
 * probability for all entries. This means that the matching probability is calculated for all the potential golden
 * records retrieved based on the title. If the probability is higher than 80% then the anime is merged. If not then the
 * anime will be reprocessed in the next run.
 *
 * This implementation uses 3 additional runs to reprocess anime. The idea behind this is that a golden record might
 * contain additional data after merging with another anime which will then positively influence the probability for an
 * anime in the reprocess queue.
 *
 * If an anime hasn't been merged after the maximum number of run-throughs then a separate golden record will be created
 * for this anime.
 *
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property goldenRecordAccessor Access to golden records.
 * @property mergeLockAccessor Access to merge locks.
 * @property matchingProbabilityCalculator Calculates matching probability between an [Anime] and a [PotentialGoldenRecord].
 */
class DefaultMergingService(
    private val appConfig: Config = AppConfig.instance,
    private val goldenRecordAccessor: GoldenRecordAccessor = DefaultGoldenRecordAccessor.instance,
    private val mergeLockAccessor: MergeLockAccessor = DefaultMergeLockAccessor.instance,
    private val matchingProbabilityCalculator: MatchingProbabilityCalculator = DefaultMatchingProbabilityCalculator.instance,
): MergingService {

    private var runThrough = 1
    private val reCheck = mutableListOf<Anime>()

    override suspend fun merge(unmergedAnime: List<Anime>): List<Anime> {
        runThrough = 1
        goldenRecordAccessor.clear()
        reCheck.clear()

        val metaDataProvidersSortedByNumberOfAnime = sortMetaDataProvidersByNumberOfAnime(unmergedAnime)
        val iterateAnime: suspend (MetaDataProviderConfig) -> Unit = { metaDataProviderConfig->
            log.info { "Merging [${metaDataProviderConfig.hostname()}] entries" }

            unmergedAnime.filter { anime ->
                anime.sources.first().toString().contains(metaDataProviderConfig.hostname())
            }
            .sortedBy { it.title }
            .map { mergeEntry(it) }
        }

        val firstMetaDataProvider = metaDataProvidersSortedByNumberOfAnime.first()
        iterateAnime(firstMetaDataProvider)

        runThrough++

        metaDataProvidersSortedByNumberOfAnime.subList(1, metaDataProvidersSortedByNumberOfAnime.size).forEach {
            iterateAnime(it)
        }

        while (runThrough <= MAX_RUNS) {
            log.info { "Run: $runThrough/$MAX_RUNS - merging [${reCheck.size}] entries" }

            val currentRun = reCheck.toList()
            reCheck.clear()

            currentRun.forEach { mergeEntry(it) }

            runThrough++
        }

        return goldenRecordAccessor.allEntries()
    }

    private fun sortMetaDataProvidersByNumberOfAnime(unmergedAnime: List<Anime>): List<MetaDataProviderConfig> {
        val cluster: MutableMap<MetaDataProviderConfig, Int> = mutableMapOf()

        appConfig.metaDataProviderConfigurations().forEach { currentConfig ->
            val numberOfAnime = unmergedAnime.count { it.sources.first().toString().contains(currentConfig.hostname()) }
            cluster[currentConfig] = numberOfAnime
        }

        return cluster.toList().sortedBy { (_, value) -> value }.map { it.first }
    }

    private suspend fun mergeEntry(anime: Anime) {
        log.debug { "Merging [${anime.sources}]" }

        if (mergeLockAccessor.hasMergeLock(anime.sources)) {
            handleMergeLock(anime)
            return
        }

        val possibleGoldenRecords = goldenRecordAccessor.findPossibleGoldenRecords(anime)

        log.debug { "Found [${possibleGoldenRecords.size}] possible golden records for [${anime.sources}]: [${possibleGoldenRecords.map { it.anime.sources }}]" }

        val potentialGoldenRecords = possibleGoldenRecords.map { matchingProbabilityCalculator.calculate(anime, it) }

        when {
            potentialGoldenRecords.isEmpty() -> reCheckOrCreateGoldenRecordEntry(anime)
            potentialGoldenRecords.size == 1 -> foundExactlyOneGoldenRecord(potentialGoldenRecords.first())
            else -> foundMultipleGoldenRecords(potentialGoldenRecords)
        }
    }

    private suspend fun handleMergeLock(anime: Anime) {
        val mergeLock = mergeLockAccessor.getMergeLock(anime.sources.first())

        log.info { "Found merge lock [$mergeLock] for [${anime.sources}]" }

        val goldenRecord = goldenRecordAccessor.findGoldenRecordBySource(mergeLock)

        if (goldenRecord == null) {
            reCheckOrCreateGoldenRecordEntry(anime)
        } else {
            goldenRecordAccessor.merge(goldenRecord.id, anime)
        }
    }

    private fun reCheckOrCreateGoldenRecordEntry(anime: Anime) {
        if (setOf(1, MAX_RUNS).contains(runThrough)) {
            log.debug { "Unable to identify golden record after analysis. Therefore creating a new one." }
            goldenRecordAccessor.createGoldenRecord(anime)
        } else {
            log.debug { "Adding anime to re-check list" }
            reCheck.add(anime)
        }
    }

    private fun foundExactlyOneGoldenRecord(matchingProbabilityResult: MatchingProbabilityResult) {
        log.debug { "Found golden record [${matchingProbabilityResult.potentialGoldenRecord.anime.sources}] for [${matchingProbabilityResult.anime.sources}] with probability [${matchingProbabilityResult.matchProbability}]" }

        if (matchingProbabilityResult.matchProbability >= EQUALITY_THRESHOLD) {
            goldenRecordAccessor.merge(matchingProbabilityResult.potentialGoldenRecord.id, matchingProbabilityResult.anime)
        } else {
            reCheckOrCreateGoldenRecordEntry(matchingProbabilityResult.anime)
        }
    }

    private fun foundMultipleGoldenRecords(potentialGoldenRecords: List<MatchingProbabilityResult>) {
        log.debug { "Found [${potentialGoldenRecords.size}] possible golden records after in-depth analysis" }

        val identifiedGoldenRecord = potentialGoldenRecords.filter { it.matchProbability >= EQUALITY_THRESHOLD }
            .maxByOrNull { it.matchProbability }

        if (identifiedGoldenRecord == null) {
            reCheckOrCreateGoldenRecordEntry(potentialGoldenRecords.first().anime)
        } else {
            log.debug { "Identified golden record [${identifiedGoldenRecord.potentialGoldenRecord.anime.sources}] for [${identifiedGoldenRecord.anime.sources}] with a probability of [${identifiedGoldenRecord.matchProbability}]" }
            goldenRecordAccessor.merge(identifiedGoldenRecord.potentialGoldenRecord.id, identifiedGoldenRecord.anime)
        }
    }

    companion object {
        private val log by LoggerDelegate()
        private const val MAX_RUNS = 4
        private const val EQUALITY_THRESHOLD = 0.8

        /**
         * Singleton of [DefaultMergingService]
         * @since 1.0.0
         */
        val instance: DefaultMergingService by lazy { DefaultMergingService() }
    }
}