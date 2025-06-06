package io.github.manamiproject.modb.core.extensions

import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
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

/**
 * Randomizes the order of elements in a [Collection].
 * @since 8.0.0
 * @return The randomized list.
 * @receiver Any collection.
 */
public suspend fun <T> Collection<T>.createShuffledList(): List<T> {
    val originalList = this.toList()

    return withContext(LIMITED_CPU) {
        if (originalList.isEmpty() || originalList.size == 1) {
            return@withContext originalList
        }

        val shuffledList = originalList.toMutableList().apply {
            shuffle(SecureRandom())
            shuffle(SecureRandom())
            shuffle(SecureRandom())
            shuffle(SecureRandom())
        }

        while (originalList.containsExactlyInTheSameOrder(shuffledList) && isActive) {
            shuffledList.shuffle(SecureRandom())
        }

        return@withContext shuffledList
    }
}