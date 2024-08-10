package io.github.manamiproject.modb.app.merging.lock

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.extensions.findDuplicates
import io.github.manamiproject.modb.core.extensions.readFile
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.runBlocking
import java.net.URI

/**
 * Default implementation for accessing `merge.lock` file.
 * Location of the file is expected to be in the [Config.downloadControlStateDirectory].
 * It lazy loads the file and keeps the data in memory.
 * @since 1.0.0
 * @param appConfig Application specific configuration. Uses [AppConfig] by default.
 */
class DefaultMergeLockAccess(
    appConfig: Config = AppConfig.instance,
): MergeLockAccess {

    private val mergeLockFile by lazy { appConfig.downloadControlStateDirectory().resolve("merge.lock") }
    private val mergeLocks: MutableMap<String, MergeLock> = mutableMapOf()

    override suspend fun isPartOfMergeLock(uri: URI): Boolean {
        if (mergeLocks.isEmpty()) {
            init()
        }

        return mergeLocks.containsKey(uri.toString())
    }

    override suspend fun getMergeLock(uri: URI): MergeLock {
        if (mergeLocks.isEmpty()) {
            init()
        }

        return mergeLocks[uri.toString()] ?: emptySet()
    }

    override suspend fun allSourcesInAllMergeLockEntries(): Set<URI> {
        if (mergeLocks.isEmpty()) {
            init()
        }

        return mergeLocks.values.flatten().toSet()
    }

    override suspend fun hasMergeLock(uris: Set<URI>): Boolean {
        if (mergeLocks.isEmpty()) {
            init()
        }

        return uris.map { it.toString() }
            .map { mergeLocks.containsKey(it) }
            .reduce { acc, value -> acc && value }
    }

    override suspend fun addMergeLock(mergeLock: MergeLock) {
        if (mergeLock.isEmpty()) {
            return
        }

        if (mergeLocks.isEmpty()) {
            init()
        }

        log.debug { "Adding merge lock entry $mergeLock" }

        mergeLock.map { it.toString() }.forEach { uri ->
            mergeLocks[uri] = mergeLock
        }

        mergeLock.forEach { source ->
            check(mergeLocks.values.filter { it.contains(source) }.distinct().size <= 1) { "You were about to add a duplicate to the merge.lock file for [$source]" }
        }

        saveToFile()
    }

    override suspend fun replaceUri(oldUri: URI, newUri: URI) {
        if (mergeLocks.isEmpty()) {
            init()
        }

        log.debug { "Replacing merge lock entry [$oldUri] with [$newUri]" }

        val currentEntry = mergeLocks[oldUri.toString()] ?: return
        val updatedEntry = currentEntry.toMutableSet().apply {
            remove(oldUri)
            add(newUri)
        }

        currentEntry.forEach {
            mergeLocks.remove(it.toString())
        }

        addMergeLock(updatedEntry)
    }

    override suspend fun removeEntry(uri: URI) {
        if (mergeLocks.isEmpty()) {
            init()
        }

        log.debug { "Removing [$uri] from merge.locks" }

        val currentEntry = mergeLocks[uri.toString()] ?: return
        val updatedEntry = currentEntry.toMutableSet().apply {
            remove(uri)
        }

        currentEntry.forEach {
            mergeLocks.remove(it.toString())
        }

        addMergeLock(updatedEntry)
    }

    private suspend fun init() {
        if (mergeLockFile.regularFileExists()) {
            val parsedMergeLocks = runBlocking { parseJsonFile() }
            checkForDuplicates(parsedMergeLocks)
            convertMergeLocksToInMemoryRepresentation(parsedMergeLocks)
        } else {
            log.warn { "Merge lock file does not exists." }
        }
    }

    private suspend fun parseJsonFile(): List<List<String>> {
        log.info { "Loading merge lock file." }
        return Json.parseJson<MergeLockFile>(mergeLockFile.readFile())!!.mergeLocks
    }

    private fun checkForDuplicates(parsedMergeLockEntries: List<List<String>>) {
        log.info { "Checking merge locks for duplicates" }
        val duplicates = parsedMergeLockEntries.flatten().findDuplicates()
        check(duplicates.isEmpty()) { "Duplicates found: $duplicates" }
    }

    private fun convertMergeLocksToInMemoryRepresentation(parsedMergeLocks: List<List<String>>) {
        parsedMergeLocks.forEach { currentMergeLock ->
            val mergeLock = currentMergeLock.map { URI(it) }.toSet()

            currentMergeLock.forEach { uri ->
                mergeLocks[uri] = mergeLock
            }
        }
    }

    private suspend fun saveToFile() {
        val sortedListOfMergeLocks = mergeLocks.values
            .map { currentMergeLock -> currentMergeLock.map { it.toString() } }
            .map { it.sorted() }
            .distinct()
            .sortedBy { it.first() }

        Json.toJson(MergeLockFile(sortedListOfMergeLocks))
            .writeToFile(mergeLockFile, true)
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DefaultMergeLockAccess]
         * @since 1.0.0
         */
        val instance: DefaultMergeLockAccess by lazy { DefaultMergeLockAccess() }
    }
}

private data class MergeLockFile(
    val mergeLocks: List<List<String>>
)