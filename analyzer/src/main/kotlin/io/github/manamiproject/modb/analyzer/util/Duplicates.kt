package io.github.manamiproject.modb.analyzer.util

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.merging.lock.DefaultMergeLockAccessor
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.anime.Anime
import java.net.URI

@KoverIgnore
internal object Duplicates {

    suspend fun showDuplicates(mergedAnime: List<Anime>) {
        println("\nEntries with multiple sources of the same metadata provider:")

        val duplicates: MutableList<List<URI>> = mutableListOf()

        mergedAnime.forEach { anime ->
            AppConfig.instance.metaDataProviderConfigurations().forEach { config ->
                val result = anime.sources.filter { it.toString().contains(config.hostname()) }

                if (result.size >= 2) {
                    duplicates.add(result)
                }
            }
        }

        duplicates.map { it.sortedBy { url -> url.toString() } }
            .filterNot { DefaultMergeLockAccessor.instance.hasMergeLock(it.toSet()) }
            .forEach { println(it) }
    }
}