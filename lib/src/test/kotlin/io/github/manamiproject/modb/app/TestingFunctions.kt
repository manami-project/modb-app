package io.github.manamiproject.modb.app

import io.github.manamiproject.modb.core.date.WeekOfYear
import io.github.manamiproject.modb.core.date.weekOfYear
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.time.LocalDate
import kotlin.collections.joinToString
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

internal fun createExpectedDatasetPrettyPrint(vararg data: String): String {
    val joinedData = data.joinToString(",\n") { entry ->
        entry.lineSequence().map { line -> "            $line" }.joinToString("\n")
    }

    return """
        {
          "${'$'}schema": "https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/tags/2020-01/anime-offline-database.schema.json",
          "license": {
            "name": "Open Data Commons Open Database License (ODbL) v1.0 + Database Contents License (DbCL) v1.0",
            "url": "https://github.com/manami-project/anime-offline-database/blob/2020-01/LICENSE"
          },
          "repository": "https://github.com/manami-project/anime-offline-database",
          "scoreRange": {
            "minInclusive": 1.0,
            "maxInclusive": 10.0
          },
          "lastUpdate": "2020-01-01",
          "data": [
$joinedData
          ]
        }
    """.trimIndent()
}

internal fun createExpectedDatasetMinified(vararg data: String): String {
    val joinedData = data.joinToString(",")

    return """
        {"${'$'}schema":"https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/tags/2020-01/anime-offline-database-minified.schema.json","license":{"name":"Open Data Commons Open Database License (ODbL) v1.0 + Database Contents License (DbCL) v1.0","url":"https://github.com/manami-project/anime-offline-database/blob/2020-01/LICENSE"},"repository":"https://github.com/manami-project/anime-offline-database","scoreRange":{"minInclusive":1.0,"maxInclusive":10.0},"lastUpdate":"2020-01-01","data":[$joinedData]}
    """.trimIndent()
}

internal fun createExpectedDeadEntriesPrettyPrint(vararg data: String): String {
    val joinedData = data.joinToString(",\n") { entry ->
        entry.lineSequence().map { line -> "            $line" }.joinToString("\n")
    }

    return """
        {
          "${'$'}schema": "https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/tags/2020-01/dead-entries/dead-entries.schema.json",
          "license": {
            "name": "Open Data Commons Open Database License (ODbL) v1.0 + Database Contents License (DbCL) v1.0",
            "url": "https://github.com/manami-project/anime-offline-database/blob/2020-01/LICENSE"
          },
          "repository": "https://github.com/manami-project/anime-offline-database",
          "lastUpdate": "2020-01-01",
          "deadEntries": [
$joinedData
          ]
        }
    """.trimIndent()
}

internal fun createExpectedDeadEntriesMinified(vararg data: String): String {
    val joinedData = data.joinToString(",")

    return """
        {"${'$'}schema":"https://raw.githubusercontent.com/manami-project/anime-offline-database/refs/tags/2020-01/dead-entries/dead-entries.schema.json","license":{"name":"Open Data Commons Open Database License (ODbL) v1.0 + Database Contents License (DbCL) v1.0","url":"https://github.com/manami-project/anime-offline-database/blob/2020-01/LICENSE"},"repository":"https://github.com/manami-project/anime-offline-database","lastUpdate":"2020-01-01","deadEntries":[$joinedData]}
    """.trimIndent()
}

internal fun createExpectedDcsEntry(
    data: String,
    lastDownload: WeekOfYear = WeekOfYear(LocalDate.now().minusWeeks(1)),
    nextDownload: WeekOfYear = WeekOfYear.currentWeek(),
): String {
    val joinedData = data.lineSequence().drop(1).map { line -> "          $line" }.joinToString("\n")

    return """
        {
          "_weeksWihoutChange": 0,
          "_lastDownloaded": {
            "year": ${lastDownload.year},
            "week": ${lastDownload.week}
          },
          "_nextDownload": {
            "year": ${nextDownload.year},
            "week": ${nextDownload.week}
          },
          "_anime": {
$joinedData
        }
    """.trimIndent()
}