package io.github.manamiproject.modb.serde.json.models

/**
 * License info as seen in the github repository.
 * @since 5.0.0
 * @property name Name of the license.
 * @property url URL to the license file.
 */
public data class License(
    val name: String = "Open Data Commons Open Database License (ODbL) v1.0 + Database Contents License (DbCL) v1.0",
    val url: String = "https://github.com/manami-project/anime-offline-database/blob/master/LICENSE",
)