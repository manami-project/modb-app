package io.github.manamiproject.modb.app.merging

import java.net.URI

/**
 * Entries with only one source for which no other entries could be found after a manual check.
 * @since 1.0.0
 */
interface ReviewedIsolatedEntriesAccessor {

    /**
     * Check if a [URI] has been added to the list of reviewed isolated entries.
     * @since 1.0.0
     * @param uri The source to check.
     * @return `true` if the [URI] has been added to the list of reviewed isolated entries.
     */
    fun contains(uri: URI): Boolean

    /**
     * Add a source to the list of reviewed isolated entries. This means that source which makes up a single entry in
     * the final dataset cannot be merged with anything else which has been verified by a manual review.
     * @since 1.0.0
     * @param uri The source to add to the list of reviewed isolated entries.
     */
    suspend fun addCheckedEntry(uri: URI)
}