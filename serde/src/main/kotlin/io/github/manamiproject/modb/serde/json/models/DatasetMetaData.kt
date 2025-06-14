package io.github.manamiproject.modb.serde.json.models

import java.net.URI

/**
 * Meta data for JSON line files.
 * @since 6.0.0
 * @property `$schema` URI to the schema file for each JSON object..
 * @property license Contains the license as seen on the github repository.
 * @property repository Link to the github repository.
 * @property scoreRange Describes min and max value of the score property.
 * @property lastUpdate Day of the last update in the format `yyyy-mm-dd`.
 */
public data class DatasetMetaData(
    val `$schema`: URI,
    val license: License = License(),
    val repository: String = "https://github.com/manami-project/anime-offline-database",
    val scoreRange: ScoreRange = ScoreRange(),
    val lastUpdate: String,
)