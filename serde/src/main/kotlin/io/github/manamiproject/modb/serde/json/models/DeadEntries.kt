package io.github.manamiproject.modb.serde.json.models

import io.github.manamiproject.modb.core.config.AnimeId

/**
 * Contains dead entries.
 * @since 5.0.0
 * @property license Contains the license as seen on the github repository.
 * @property repository Link to the github repository.
 * @property lastUpdate Day of the last update in the format `yyyy-mm-dd`.
 * @property deadEntries [List] of dead entries represented by their ID as [String]. It's a [List], because it is sorted.
 */
public data class DeadEntries(
    val license: License = License(),
    val repository: String = "https://github.com/manami-project/anime-offline-database",
    val lastUpdate: String,
    val deadEntries: List<AnimeId>,
)
