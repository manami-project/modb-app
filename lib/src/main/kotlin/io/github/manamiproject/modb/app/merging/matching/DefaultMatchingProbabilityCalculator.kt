package io.github.manamiproject.modb.app.merging.matching

import io.github.manamiproject.modb.app.merging.goldenrecords.PotentialGoldenRecord
import io.github.manamiproject.modb.app.weightedProbabilityOfTwoNumbersBeingEqual
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Status
import io.github.manamiproject.modb.core.models.Anime.Type
import io.github.manamiproject.modb.core.models.Anime.Type.ONA
import io.github.manamiproject.modb.core.models.Anime.Type.SPECIAL
import io.github.manamiproject.modb.core.models.Duration
import org.apache.commons.text.similarity.JaroWinklerSimilarity
import kotlin.math.floor
import io.github.manamiproject.modb.core.models.Anime.Status.UNKNOWN as UNKNOWN_STATUS
import io.github.manamiproject.modb.core.models.Duration.Companion.UNKNOWN as UNKNOWN_DURATION

/**
 * This implementation of [MatchingProbabilityCalculator] tries to calculate how likely it is that an [Anime] should
 * be merged into an existing golden record.
 *
 * Each property taking into consideration has a span from `0.0` (not equal, very unlikely) up to `1.0` (value is
 * identical).
 * The basic check starts with 3 properites (`title`, `type` and `episodes`). Therefore the max reachable score is
 * `3.0`. In case the additional properties (`status`, `yearOfPremiere` and `duration`) are available the maximum
 * reachable score increases accordingly.
 *
 * The total probability is made up of the sum of all probabilities of all checkable properties.
 *
 * ## title
 *
 * The score for the title is calculated using "jaro winkler similarity".
 *
 * ## type
 *
 * A type is either equal (`1.0`) or not (`0.0`). But because the data from different sources varies a lot, there is
 * another case. Based on experience there is another case for any combination of `SPECIAL` and `ONA`. For this case the
 * value is `0.4` to indicate that it's somewhat possible that these could be the same.
 *
 * ## episodes
 *
 * Uses [weightedProbabilityOfTwoNumbersBeingEqual] with a factor of `4`. This tolerates only very small differences.
 *
 * ## status
 *
 * Is binary check. Either it's equal or not.
 *
 * ## yearOfPremiere
 *
 * Uses [weightedProbabilityOfTwoNumbersBeingEqual] with a factor of `3`. This tolerates only very small differences.
 *
 * ## duration
 *
 * Does a scaling if possible. The reason is that using the same factor for a comparison of hours long movies on a scale
 * of seconds can return misleading results if they differ in minutes for example.
 * Uses [weightedProbabilityOfTwoNumbersBeingEqual] with a factor of `2` on scaled values. Indicating more tolerance.
 *
 * @since 1.0.0
 */
class DefaultMatchingProbabilityCalculator: MatchingProbabilityCalculator {

    override fun calculate(anime: Anime, potentialGoldenRecord: PotentialGoldenRecord): MatchingProbabilityResult {
        var maxProbability = 3.0
        var currentProbability = 0.0

        currentProbability += calculateProbabilityOfTitle(anime.title, potentialGoldenRecord.anime.title)
        currentProbability += calculateProbabilityOfType(anime.type, potentialGoldenRecord.anime.type)
        currentProbability += calculateProbabilityOfEpisodes(anime.episodes, potentialGoldenRecord.anime.episodes)

        if (anime.status != UNKNOWN_STATUS && potentialGoldenRecord.anime.status != UNKNOWN_STATUS) {
            maxProbability += 1.0
            currentProbability += calculateProbabilityOfStatus(anime.status, potentialGoldenRecord.anime.status)
        }

        if (anime.animeSeason.isYearOfPremiereKnown() && potentialGoldenRecord.anime.animeSeason.isYearOfPremiereKnown()) {
            maxProbability += 1.0
            currentProbability += calculateProbabilityOfYearOfPremiere(anime.animeSeason.year, potentialGoldenRecord.anime.animeSeason.year)
        }

        if (anime.duration != UNKNOWN_DURATION && potentialGoldenRecord.anime.duration != UNKNOWN_DURATION) {
            maxProbability += 1.0
            currentProbability += calculateProbabilityOfDuration(anime.duration, potentialGoldenRecord.anime.duration)
        }

        val actualProbability = currentProbability / maxProbability
        val probabilityCutToTwoDecimals = floor(actualProbability * 100) / 100

        return MatchingProbabilityResult(
            anime = anime,
            potentialGoldenRecord = potentialGoldenRecord,
            matchProbability = probabilityCutToTwoDecimals
        )
    }

    private fun calculateProbabilityOfTitle(recordTitle: String, potentialGoldenRecordTitle: String): Double {
        return jaroWinklerSimilarity.apply(recordTitle, potentialGoldenRecordTitle)
    }

    private fun calculateProbabilityOfEpisodes(recordEpisodes: Int, potentialGoldenRecordEpisodes: Int): Double {
        return weightedProbabilityOfTwoNumbersBeingEqual(recordEpisodes, potentialGoldenRecordEpisodes, 4)
    }

    private fun calculateProbabilityOfType(animeType: Type, potentialGoldenRecordType: Type): Double {
        return when {
            animeType == potentialGoldenRecordType -> 1.0
            specials.contains(animeType) && specials.contains(potentialGoldenRecordType) -> 0.4
            else -> 0.0
        }
    }

    private fun calculateProbabilityOfYearOfPremiere(recordYearOfPremiere: Int, potentialGoldenRecordYearOfPremiere: Int): Double {
        return weightedProbabilityOfTwoNumbersBeingEqual(recordYearOfPremiere, potentialGoldenRecordYearOfPremiere, 3)
    }

    private fun calculateProbabilityOfStatus(recordStatus: Status, potentialGoldenRecordStatus: Status): Double = when (recordStatus) {
        potentialGoldenRecordStatus -> 1.0
        else -> 0.0
    }

    private fun calculateProbabilityOfDuration(animeDuration: Duration, potentialGoldenRecordDuration: Duration): Double {
        // initiate in seconds
        var durationRecord =  animeDuration.duration
        var durationPotentialGoldenRecord =  potentialGoldenRecordDuration.duration

        // scale to hours
        if (durationRecord > HOUR_IN_SECONDS && durationPotentialGoldenRecord > HOUR_IN_SECONDS) {
            durationRecord /= HOUR_IN_SECONDS
            durationPotentialGoldenRecord /= HOUR_IN_SECONDS
        }

        // scale to minutes
        if (durationRecord > MINUTE_IN_SECONDS && durationPotentialGoldenRecord > MINUTE_IN_SECONDS) {
            durationRecord /= MINUTE_IN_SECONDS
            durationPotentialGoldenRecord /= MINUTE_IN_SECONDS
        }

        return weightedProbabilityOfTwoNumbersBeingEqual(durationRecord, durationPotentialGoldenRecord, 2)
    }

    companion object {
        private const val HOUR_IN_SECONDS = 3600
        private const val MINUTE_IN_SECONDS = 60

        private val jaroWinklerSimilarity = JaroWinklerSimilarity()
        private val specials = setOf(ONA, SPECIAL)

        /**
         * Singleton of [DefaultMatchingProbabilityCalculator]
         * @since 1.0.0
         */
        val instance: DefaultMatchingProbabilityCalculator by lazy { DefaultMatchingProbabilityCalculator() }
    }
}