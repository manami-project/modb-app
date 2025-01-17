package io.github.manamiproject.modb.core.extensions

import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.security.SecureRandom

/**
 * Randomizes the order of elements in a [List].
 * @since 8.0.0
 * @return The randomized list.
 * @receiver Any list.
 */
public suspend fun <T> List<T>.createShuffledList(): List<T> {
    val list = this

    return withContext(LIMITED_CPU) {
        if (list.isEmpty() || list.size == 1) {
            return@withContext list
        }

        var shuffledList = mutableListOf<T>()

        shuffledList.addAll(list)

        shuffledList.shuffle(SecureRandom())
        shuffledList.shuffle(SecureRandom())
        shuffledList.shuffle(SecureRandom())
        shuffledList.shuffle(SecureRandom())

        while (list.containsExactlyInTheSameOrder(shuffledList) && isActive) {
            shuffledList = list.createShuffledList().toMutableList()
        }

        return@withContext shuffledList
    }
}

/**
 * Check whether two lists are completely identical including the order of elements.
 * @since 1.0.0
 * @return `true` if both lists are completely identical.
 * @receiver Any list.
 */
public fun <T> List<T>.containsExactlyInTheSameOrder(otherList: List<T>): Boolean {
    if (this.size != otherList.size) return false

    this.forEachIndexed { index, value ->
        if (otherList[index] != value) {
            return false
        }
    }

    return true
}