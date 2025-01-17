package io.github.manamiproject.modb.core.extensions

import io.github.manamiproject.modb.core.config.AnimeId

/**
 * Converts an [Int] to an [AnimeId].
 * @since 1.0.0
 * @return Converted anime id.
 * @receiver Any non-nullable [Int].
 */
public fun Int.toAnimeId(): AnimeId = this.toString()