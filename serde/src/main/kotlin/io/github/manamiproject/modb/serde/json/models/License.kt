package io.github.manamiproject.modb.serde.json.models

/**
 * License info as seen in the github repository.
 * @since 5.0.0
 * @property name Name of the license.
 * @property url URL to the license file.
 */
public data class License(
    val name: String = "GNU Affero General Public License v3.0",
    val url: String = "https://github.com/manami-project/anime-offline-database/blob/master/LICENSE",
)