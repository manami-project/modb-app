package io.github.manamiproject.modb.core.extensions

import java.security.SecureRandom

/**
 * Picks a random element from a [Collection].
 * @since 4.0.0
 * @return A random element from the given [Collection].
 * @throws IllegalStateException if the collection is empty.
 * @receiver Any collection.
 */
public fun <T> Collection<T>.pickRandom(): T {
    val internal = this.toList()
    return when (internal.size) {
        0 -> throw IllegalStateException("Cannot pick random element from empty list.")
        1 -> internal.first()
        else -> internal[SecureRandom().nextInt(internal.size).apply { if (this != 0) this - 1 }]
    }
}