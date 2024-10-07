package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.core.models.YEAR_OF_THE_FIRST_ANIME
import io.github.manamiproject.modb.core.models.Year
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.GregorianCalendar.WEEK_OF_YEAR

/**
 * Number of week within a year.
 * @since 1.0.0
 */
typealias Week = Int

/**
 * Determines the week of a date.
 * @since 1.0.0
 * @receiver Any LocaDate.
 * @return An instance of [WeekOfYear] matching the date of the receiver.
 */
fun LocalDate.weekOfYear(): WeekOfYear = WeekOfYear(this)

/**
 * Represents the week of a year.
 * @since 1.0.0
 * @param year Year
 * @param week Week of the year.
 * @throws IllegalArgumentException if corresponding date is not within range of [YEAR_OF_THE_FIRST_ANIME] <= date <= `today + 5 years` or if values don't represent a valid week.
 */
data class WeekOfYear(
    val year: Year,
    val week: Week,
) {

    /**
     * Create an instance of [WeekOfYear] based on a date.
     * @since 1.0.0
     * @param localDate Date for which the week of the year should be determined.
     * @param zoneId Id of the time zone used for the date.
     */
    constructor(localDate: LocalDate, zoneId: ZoneId = ZoneId.systemDefault()): this(
        year = determineYear(localDate, zoneId),
        week = GregorianCalendar.from(localDate.atStartOfDay(zoneId)).get(WEEK_OF_YEAR)
    )

    init {
        require(year > YEAR_OF_THE_FIRST_ANIME) { "Invalid value [$year]: Year is set before the year of the first anime." }
        require(year <= LocalDate.now().year + 5) { "Invalid value [$year]: Year is set too far on the future." }
        require(week <= 53) { "Week [$week] exceeds number of weeks." }
        require(week >= 1) { "Week [$week] must not be zero or negative." }
    }

    /**
     * Add a number of weeks.
     * @since 1.0.0
     * @param value Number of weeks to add.
     * @return A new instance containing the week of year.
     */
    fun plusWeeks(value: Int): WeekOfYear {
        val dateOfCurrentWeek = toLocalDate()
        return dateOfCurrentWeek.plusWeeks(value.toLong()).weekOfYear()
    }

    /**
     * Determines the difference in weeks. This function takes changing years into consideration.
     * @since 1.0.0
     * @param other Other instance of [WeekOfYear] to compare to this instance.
     * @return A new instance containing the week of year.
     */
    fun difference(other: WeekOfYear): Int {
        val currentDate = toLocalDate()
        val otherDate = other.toLocalDate()

        val diff = ChronoUnit.WEEKS.between(currentDate, otherDate).toInt()

        return diff.takeIf { it > 0 } ?: (diff * -1)
    }

    /**
     * Creates an instance of [LocalDate] an uses the first day of the week.
     * @since 1.0.0
     * @return An instance of LocalDate matching this week.
     */
    fun toLocalDate(): LocalDate {
        val firstDayOfCurrentYear = localDateOfFirstWeekOfYear(year)

        return if (week == 1) {
            firstDayOfCurrentYear
        } else {
            firstDayOfCurrentYear.plusWeeks(week.toLong() - 1L)
        }
    }

    override fun toString(): String {
        val zeroBasedWeek = week.takeIf { it > 9 }?.toString() ?: "0$week"
        return "$year-$zeroBasedWeek"
    }

    companion object {

        /**
         * Determines the current week of year and returns it.
         * @since 1.0.0
         * @return An instance representing the current week of year.
         */
        fun currentWeek(): WeekOfYear = WeekOfYear(LocalDate.now())
    }
}

/**
 * Allows to compare two instances of [WeekOfYear].
 * @since 1.0.0
 * @param other Other instance of [WeekOfYear] to compare to this instance.
 * @return The comparator value, that is the comparison of this [WeekOfYear] with the other [WeekOfYear].
 */
operator fun WeekOfYear.compareTo(other: WeekOfYear): Int {
    val currentDate = toLocalDate()
    val otherDate = other.toLocalDate()

    return currentDate.compareTo(otherDate)
}

private fun localDateOfFirstWeekOfYear(year: Year): LocalDate {
    var currentDate = LocalDate.of(year, 1, 1)

    while (WeekOfYear(currentDate).week > 50) {
        currentDate = currentDate.plusDays(1L)
    }

    return currentDate
}

private fun determineYear(localDate: LocalDate, zoneId: ZoneId): Int {
    val week = GregorianCalendar.from(localDate.atStartOfDay(zoneId)).get(WEEK_OF_YEAR)

    return if (week <= 2 && localDate.monthValue == 12) {
        localDate.year + 1
    } else {
        localDate.year
    }
}