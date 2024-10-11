package io.github.manamiproject.modb.analyzer

import io.github.manamiproject.modb.app.merging.ReviewedIsolatedEntriesAccessor
import io.github.manamiproject.modb.app.merging.lock.MergeLock
import io.github.manamiproject.modb.app.merging.lock.MergeLockAccessor
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import java.net.URI

internal object TestMergeLockAccessor: MergeLockAccessor {
    override suspend fun addMergeLock(mergeLock: MergeLock) = shouldNotBeInvoked()
    override suspend fun allSourcesInAllMergeLockEntries(): Set<URI> = shouldNotBeInvoked()
    override suspend fun getMergeLock(uri: URI): MergeLock = shouldNotBeInvoked()
    override suspend fun hasMergeLock(uris: Set<URI>): Boolean = shouldNotBeInvoked()
    override suspend fun isPartOfMergeLock(uri: URI): Boolean = shouldNotBeInvoked()
    override suspend fun removeEntry(uri: URI) = shouldNotBeInvoked()
    override suspend fun replaceUri(oldUri: URI, newUri: URI) = shouldNotBeInvoked()
}

internal object TestReviewedIsolatedEntriesAccessor: ReviewedIsolatedEntriesAccessor {
    override suspend fun addCheckedEntry(uri: URI) = shouldNotBeInvoked()
    override fun contains(uri: URI): Boolean = shouldNotBeInvoked()
}