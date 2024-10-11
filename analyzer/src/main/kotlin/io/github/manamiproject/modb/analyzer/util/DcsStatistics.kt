package io.github.manamiproject.modb.analyzer.util

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateAccessor
import io.github.manamiproject.modb.core.coverage.KoverIgnore

@KoverIgnore
internal object DcsStatistics {

    suspend fun show() {
        val allEntries = DefaultDownloadControlStateAccessor.instance.allDcsEntries()
            .groupBy { it.nextDownload }
            .toSortedMap(comparator = { o1, o2 -> o1.week.compareTo(o2.week) })

        val heading = "Total number of re-downloads per week"
        println("\n")
        println(heading)
        println("-".repeat(heading.length))

        allEntries.forEach { (nextDownload, entries) ->
            println("$nextDownload: ${entries.size}")
        }

        println("\n")
        val longestMetaDataProviderName = AppConfig.instance.metaDataProviderConfigurations().maxBy { it.hostname().length }

        allEntries.forEach { (nextDownload, dcsEntries) ->
            val weekHeading = "${nextDownload.year}-${nextDownload.week}"
            println(weekHeading)
            println("-".repeat(weekHeading.length))

            dcsEntries.groupBy { it.anime.sources.first().host }.toSortedMap().forEach { (host, entries) ->
                println("%-${longestMetaDataProviderName.hostname().length}s : ${entries.size}".format(host))
            }

            println("\n")
        }
    }
}