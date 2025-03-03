package io.github.manamiproject.modb.app

import io.github.manamiproject.modb.core.date.WeekOfYear
import io.github.manamiproject.modb.core.date.weekOfYear
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

internal suspend fun waitFor(timeout: Duration, action: () -> Boolean) = withContext(Unconfined) {
    withTimeout(timeout) {
        while (!action.invoke() && isActive) {
            delay(100)
        }
    }
}

internal fun WeekOfYear.minusWeeks(value: Int): WeekOfYear {
    val dateOfCurrentWeek = toLocalDate()
    val newValue = dateOfCurrentWeek.minusWeeks(value.toLong())

    return WeekOfYear(
        year = newValue.year,
        week = newValue.weekOfYear().week,
    )
}