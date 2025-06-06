package io.github.manamiproject.modb.core.extensions

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