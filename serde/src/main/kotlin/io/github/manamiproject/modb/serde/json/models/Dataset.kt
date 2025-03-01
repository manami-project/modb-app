package io.github.manamiproject.modb.serde.json.models

import io.github.manamiproject.modb.core.anime.Anime
import java.net.URI

/**
 * Contains the complete dataset.
 * @since 5.0.0
 * @property `$schema` URI to the schema file.
 * @property license Contains the license as seen on the github repository.
 * @property repository Link to the github repository.
 * @property scoreRange Describes min and max value of the score property.
 * @property lastUpdate Day of the last update in the format `yyyy-mm-dd`.
 * @property data [List] of the anime. It's a [List], because it is sorted.
 */
public data class Dataset(
    val `$schema`: URI,
    val license: License = License(),
    val repository: String = "https://github.com/manami-project/anime-offline-database",
    val scoreRange: ScoreRange = ScoreRange(),
    val lastUpdate: String,
    val data: List<Anime>,
)