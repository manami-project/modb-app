package io.github.manamiproject.modb.core.anime

import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.UNDEFINED
import java.time.LocalDate

/**
 * @since 1.0.0
 */
public typealias Year = Int

/**
 * Year of the first japanese anime. See [Wikipedia](https://en.wikipedia.org/wiki/Katsud%C5%8D_Shashin).
 * @since 5.1.0
 */
public const val YEAR_OF_THE_FIRST_ANIME: Year = 1907

/**
 * Defines the season in which an anime premiered or has been published.
 * @since 1.0.0
 * @property season **Default** is [UNDEFINED]
 * @property year Year in the format `YYYY`. Requires a value between [YEAR_OF_THE_FIRST_ANIME] and the current year + 5. Otherwise an exception is thrown.
 * **Default** is `0` indicating unknown year.
 */
public data class AnimeSeason(
    val season: Season = UNDEFINED,
    val year: Year = UNKNOWN_YEAR,
) {

    init {
        validateYear(year)
    }

    /**
     * @since 1.0.0
     * @return `true` if the year is unknown which means that it's `0`.
     */
    public fun isYearOfPremiereUnknown(): Boolean = year == UNKNOWN_YEAR

    /**
     * @since 1.0.0
     * @return `true` if the year is known which means that it's `0`.
     */
    public fun isYearOfPremiereKnown(): Boolean = year != UNKNOWN_YEAR

    private fun validateYear(value: Year) {
        val isYearAfterVeryFirstAnimeRelease = value >= YEAR_OF_THE_FIRST_ANIME
        val isYearNotTooFarInTheFuture = value <= LocalDate.now().year + 10
        val isYearWithinRange = isYearAfterVeryFirstAnimeRelease && isYearNotTooFarInTheFuture
        val isYearUnknown = value == UNKNOWN_YEAR

        require(isYearUnknown || isYearWithinRange) { "Year of premiere [$value] is not valid" }
    }

    public companion object {
        public const val UNKNOWN_YEAR: Year = 0
    }

    /**
     * @since 1.0.0
     */
    public enum class Season {
        /**
         * @since 1.0.0
         */
        SPRING,
        /**
         * @since 1.0.0
         */
        SUMMER,
        /**
         * @since 1.0.0
         */
        FALL,
        /**
         * @since 1.0.0
         */
        WINTER,
        /**
         * Season is unknown.
         * @since 1.0.0
         */
        UNDEFINED;

        public companion object {
            /**
             * Creates [AnimeSeason.Season] from a [String]. Tolerant by ignoreing leading and trailing whitespaces as well as case.
             * @since 1.0.0
             * @param value The value being mapped to a [AnimeSeason.Season].
             */
            public fun of(value: String): Season {
                return entries.find { it.toString().equals(value.trim(), ignoreCase = true) } ?: UNDEFINED
            }
        }
    }
}