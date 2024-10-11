package io.github.manamiproject.modb.analyzer.util

import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import io.github.manamiproject.modb.anisearch.AnisearchConfig
import io.github.manamiproject.modb.app.merging.lock.DefaultMergeLockAccessor
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.livechart.LivechartConfig
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import io.github.manamiproject.modb.notify.NotifyConfig
import java.net.URI
import java.net.URLEncoder

@KoverIgnore
internal object OpenInBrowserHelper {

    suspend fun determineUrisToOpenInBrowser(currentEntry: Pair<Anime, Set<URI>>): List<URI> {
        val currentEntryUris = currentEntry.second.sorted().toMutableList()

        val options = mutableListOf(
            "[keep] (= add merge lock as is)",
            "[skip] (= don't create merge lock for this entry)",
            "[a valid URL] (= add URL to merge lock)",
        )

        if (currentEntryUris.size == 1) {
            options.add("[check] (= checked having no additional entries)")
        }

        options.add("[exit] (= back to main menu)")

        println(options.joinToString("    "))
        println("\n${Json.toJson(currentEntryUris)}")

        val entriesBeingPartOfMergeLock = currentEntryUris.filter { DefaultMergeLockAccessor.instance.isPartOfMergeLock(it) }.toSet()
        val searchLinkForMissingMetaDataProvider = with(currentEntryUris.map { it.host }.toSet()) {
            val searchLink = mutableSetOf<URI>()
            val urlEncodedTitle = URLEncoder.encode(currentEntry.first.title, "UTF-8").replace(Regex("\\+"), "%20")

            if (!this.contains(AnidbConfig.hostname())) {
                searchLink.add(URI("https://${AnidbConfig.hostname()}/anime/?adb.search=$urlEncodedTitle&do.search=1"))
            }
            if (!this.contains(AnilistConfig.hostname())) {
                searchLink.add(URI("https://${AnilistConfig.hostname()}/search/anime?search=$urlEncodedTitle"))
            }
            if (!this.contains(AnimePlanetConfig.hostname())) {
                searchLink.add(URI("https://www.${AnimePlanetConfig.hostname()}/anime/all?name=$urlEncodedTitle"))
            }
            if (!this.contains(AnisearchConfig.hostname())) {
                //searchLink.add(URI("https://www.${AnisearchConfig.hostname()}.com/anime/index/?char=all&text=$urlEncodedTitle&q=true&$anisearchDataKey"))
            }
            if (!this.contains(KitsuConfig.hostname())) {
                searchLink.add(URI("https://${KitsuConfig.hostname()}/anime?text=$urlEncodedTitle"))
            }
            if (!this.contains(LivechartConfig.hostname())) {
                searchLink.add(URI("https://${LivechartConfig.hostname()}/search?q=$urlEncodedTitle"))
            }
            if (!this.contains(MyanimelistConfig.hostname())) {
                searchLink.add(URI("https://${MyanimelistConfig.hostname()}/anime.php?q=$urlEncodedTitle&cat=anime"))
            }
            if (!this.contains(NotifyConfig.hostname())) {
                searchLink.add(URI("https://${NotifyConfig.hostname()}/search/$urlEncodedTitle"))
            }

            return@with searchLink
        }

        val urisToOpenInBrowser = if (entriesBeingPartOfMergeLock.isNotEmpty()) {
            val malLink = entriesBeingPartOfMergeLock.find { it.toString().contains(MyanimelistConfig.hostname()) } ?: entriesBeingPartOfMergeLock.first()
            currentEntryUris - entriesBeingPartOfMergeLock + malLink
        } else {
            currentEntryUris
        } + searchLinkForMissingMetaDataProvider

        return urisToOpenInBrowser
    }
}