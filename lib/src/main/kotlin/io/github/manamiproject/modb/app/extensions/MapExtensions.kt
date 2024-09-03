package io.github.manamiproject.modb.app.extensions

/**
 * Retrieves the first value which is not null from a map.
 * @since 1.0.0
 * @param list A list of keys to lookup. The function will iterate the entries in order and try to find an entry which
 * is not `null`. The list allows multiple key lookups in one go.
 * @receiver Any [Map] with a non-nullable key and a nullable value.
 * @return The first value which is not `null` and matches one of the keys presented in [list].
 */
internal fun <K: Any, V: Any> Map<K, V?>.firstNotNullResult(list: Iterable<K>): V? {
    list.forEach { lookupKey ->
        val result = get(lookupKey)

        if (result != null) {
            return result
        }
    }

    return null
}