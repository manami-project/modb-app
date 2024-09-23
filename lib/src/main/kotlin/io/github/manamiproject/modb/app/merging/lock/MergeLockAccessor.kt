package io.github.manamiproject.modb.app.merging.lock

import io.github.manamiproject.modb.core.models.Anime
import java.net.URI

/**
 * Represents a single entry within the `merge.lock` file.
 * This corresponds with what you find in [Anime.sources].
 * @since 1.0.0
 */
typealias MergeLock = Set<URI>

/**
 * Allows to access the `merge.lock` file.
 * The file contains manual fixations or overrides on how anime entries are being merged together.
 * It takes influence on the merge process.
 * @since 1.0.0
 */
interface MergeLockAccessor {

    /**
     * Checks if this exact combination of URIs exists in a merge lock or is part of a merge lock.
     * Example: If you run a check for `[a, b, c]` then you need a merge lock that exactly matches this or
     * contains is as a subset.
     * + `[a, b]` would return `false`
     * + `[a, b, c]` would return `true`
     * + `[a, b, c, d, e]` would return `true`
     * @since 1.0.0
     * @uris A set of URIs for which you want to check if a merge lock exists.
     * @return `true` if a merge lock was found.
     * @see isPartOfMergeLock
     */
    suspend fun hasMergeLock(uris: Set<URI>): Boolean

    /**
     * Checks if a single URI exists in a merge lock.
     * @since 1.0.0
     * @param uri URI to check.
     * @return `true` if any merge lock contains this URI.
     * @see hasMergeLock
     * @see getMergeLock
     */
    suspend fun isPartOfMergeLock(uri: URI): Boolean

    /**
     * Retrieves a whole merge lock for a single URI.
     * @since 1.0.0
     * @param uri URI for which you want to retrieve the whole merge lock.
     * @return Either the merge lock or an empty [Set].
     * @see isPartOfMergeLock
     */
    suspend fun getMergeLock(uri: URI): MergeLock

    /**
     * Creates a new merge lock.
     * Does nothing if you try to add the exact same merge lock multiple times.
     * @since 1.0.0
     * @param mergeLock Merge lock to create.
     * @throws IllegalStateException if you try add a duplicate. In this case this means a merge lock containing a URI that is already part of another merge lock.
     */
    suspend fun addMergeLock(mergeLock: MergeLock)

    /**
     * Can replace a single URI in a merge lock without the user having to know the exact merge lock.
     * If there is no merge lock containing [oldUri] then the function does nothing.
     * @since 1.0.0
     * @param oldUri The URI that is currently part of a merge lock
     * @param newUri The URI with which you want to replace [oldUri]
     */
    suspend fun replaceUri(oldUri: URI, newUri: URI)

    /**
     * Removes a single URI from a merge lock if it is part of a merge lock.
     * If not then nothing happens.
     * The rest of the merge lock is kept as-is.
     * @since 1.0.0
     * @param uri URI to be removed from a merge lock
     */
    suspend fun removeEntry(uri: URI)

    /**
     * Returns a list with all URIs that are part of a merge lock.
     * This function can be useful of you otherwise would have to make multiple calls to [isPartOfMergeLock].
     * @since 1.0.0
     * @return A [Set] containing all URIs wich are part of a merge lock
     */
    suspend fun allSourcesInAllMergeLockEntries(): Set<URI>
}