package io.github.manamiproject.modb.app.extensions

/**
 * Finds duplicates in a [List].
 * @since 1.0.0
 * @return A [Set] of those elements which appear multiple times in the receiver.
 * @receiver Any non-nullable [List] of any type.
 */
inline fun <reified T> List<T>.findDuplicates(): Set<T> = groupBy { it }.filter { it.value.size > 1 }.keys