package io.github.manamiproject.modb.app.extensions

import io.github.manamiproject.modb.core.models.Anime

/**
 * Finds duplicates in a [List].
 * @since 1.0.0
 * @return A [Set] of those elements which appear multiple times in the receiver.
 * @receiver Any non-nullable [List] of any type.
 */
inline fun <reified T> List<T>.findDuplicates(): Set<T> = groupBy { it }.filter { it.value.size > 1 }.keys

/**
 * Throws an exception if the title is either `delete` or `deleted`.
 * This is often done by kitsu.
 * @since 1.0.0
 * @return The original list, no changes made.
 * @receiver Any non-nullable [List] of type [Anime].
 */
fun List<Anime>.alertDeletedAnimeByTitle(): List<Anime> {
    val deletedMarker = setOf("delete", "deleted")
    forEach {
        check(it.title.lowercase() !in deletedMarker) {
            "Probably found a dead entry: [title=${it.title}, source=${it.sources.first()}]"
        }
    }
    return this
}