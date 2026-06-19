package io.github.manamiproject.modb.analyzer.util

import io.github.manamiproject.modb.core.anime.AnimeRaw
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.extensions.EMPTY
import java.util.Collections.emptyList
import kotlin.collections.forEachIndexed

@KoverIgnore
internal class AnsiTable {

    private val properties = listOf(
        "Provider" to { e: AnimeRaw -> e.sources.first().host },
        "Title" to { e: AnimeRaw -> e.title },
        "Type" to { e: AnimeRaw -> e.type.toString() },
        "Status" to { e: AnimeRaw -> e.status.toString() },
        "Episodes" to { e: AnimeRaw -> e.episodes.toString() },
        "Season" to { e: AnimeRaw -> e.animeSeason.toString() },
        "Duration" to { e: AnimeRaw -> e.duration.toString() },
    )

    private fun visibleLength(s: String): Int = ANSI_REGEX.replace(s, EMPTY).trim().length

    private fun padAnsi(s: String, width: Int): String = s + " ".repeat(width - visibleLength(s))

    fun printTable(entries: List<AnimeRaw> = emptyList()) {
        val maxWidthColumn = mutableListOf(properties.maxOf { it.first.length })
        entries.forEach { entry ->
            maxWidthColumn.add(properties.maxOf { it.second(entry).length })
        }

        // Header (bold)
        val header = mutableListOf(properties.first().first).apply {
            entries.map { properties.first().second(it) }
                   .forEach { add(it) }
        }
        printRow(header.map { "$BOLD$it$RESET" }, maxWidthColumn)

        // Separator
        val whiteSpacePrefixAndSuffix = 2
        println(maxWidthColumn.joinToString("+", "+", "+") { "-".repeat(it + whiteSpacePrefixAndSuffix) })

        // Rows
        properties.drop(1).forEachIndexed { index, propertyPair ->
            val items = mutableListOf(propertyPair.first)
            val getter = propertyPair.second
            val firstValue = getter(entries.first())

            entries.forEachIndexed { index, entry ->
                val value = getter(entry)
                when {
                    index == 0 -> items.add(value)
                    value == firstValue -> items.add("$FG_GREEN$value$RESET")
                    else -> items.add("$BG_RED$FG_WHITE$value$RESET")
                }
            }

            printRow(items, maxWidthColumn)
        }
    }

    private fun printRow(cells: List<String>, widths: List<Int>) {
        val row = cells.zip(widths).joinToString("|", "|", "|") { (cell, width) ->
            padAnsi(" $cell ", width)
        }
        println(row)
    }

    companion object {
        private const val BOLD = "\u001B[1m"
        private const val RESET = "\u001B[0m"
        private const val FG_GREEN = "\u001B[32m"
        private const val BG_RED = "\u001B[41m"
        private const val FG_WHITE = "\u001B[97m"
        private val ANSI_REGEX = Regex("\u001B\\[[;\\d]*m")

        /**
         * Singleton of [AnsiTable]
         * @since 1.13.0
         */
        val instance: AnsiTable by lazy { AnsiTable() }
    }
}