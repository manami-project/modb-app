package io.github.manamiproject.modb.analyzer

import io.github.manamiproject.modb.analyzer.Analyzer.Options.*
import io.github.manamiproject.modb.analyzer.cluster.ClusterService
import io.github.manamiproject.modb.analyzer.util.*
import io.github.manamiproject.modb.analyzer.util.AnimeLoader
import io.github.manamiproject.modb.analyzer.util.DcsStatistics
import io.github.manamiproject.modb.analyzer.util.Duplicates
import io.github.manamiproject.modb.analyzer.util.Reprocessor
import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.dataset.DefaultDatasetFileAccessor
import io.github.manamiproject.modb.app.merging.DefaultReviewedIsolatedEntriesAccessor
import io.github.manamiproject.modb.app.merging.lock.DefaultMergeLockAccessor
import io.github.manamiproject.modb.core.coroutines.CoroutineManager.runCoroutine
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.models.Anime
import java.awt.Desktop
import java.net.URI
import kotlin.system.exitProcess

@KoverIgnore
fun main() = runCoroutine {
    Analyzer.start()
}

@KoverIgnore
object Analyzer {

    private val datasetEntries = mutableListOf<Anime>()

    suspend fun start() {
        datasetEntries.addAll(DefaultDatasetFileAccessor.instance.fetchEntries())
        mainMenu()
    }

    private suspend fun mainMenu(): Nothing {
        println("""
            
        MODB - ANALYZER
        ------------------------------------
        [${DUPLICATES.value}] Show unseen duplicates
        [${CLUSTER_SIZES.value}] Show cluster sizes
        [${DCS_STATISTICS.value}] Show DCS statistics
        ------------------------------------
        [${CHECK_MERGE_LOCKS.value}] Check merge locks
        [${MARK_DEAD_ENTRY.value}] Mark as dead entry
        [${LOAD_ENTRY.value}] Load anime manually
        [${CREATE_NEW_MERGE_LOCK.value}] Create a merge lock from scratch
        [${ADD_TO_EXISTING_MERGE_LOCK.value}] Add a URL to an existing merge lock
        ------------------------------------
        [${REPROCESS_MERGING.value}] Reprocess merging
        ------------------------------------
        [${QUIT.value}] quit
        ------------------------------------
        """.trimIndent())

        print("\nSelect: ")

        val userInput = waitForUserInput()

        when (Options.from(userInput)) {
            DUPLICATES -> Duplicates.showDuplicates(datasetEntries)
            CLUSTER_SIZES -> showClusterSizes()
            DCS_STATISTICS -> DcsStatistics.show()
            CHECK_MERGE_LOCKS -> checkMergeLocks()
            MARK_DEAD_ENTRY -> markAsDeadEntry()
            LOAD_ENTRY -> loadAnime()
            CREATE_NEW_MERGE_LOCK -> createMergeLock()
            ADD_TO_EXISTING_MERGE_LOCK -> extendExistingMergeLock()
            REPROCESS_MERGING -> reprocessMerging()
            QUIT -> exitProcess(0)
            else -> {
                println("\nInvalid selection.\n")
            }
        }

        println("\n")

        mainMenu()
    }

    private fun showClusterSizes() {
        val col1 = " Number of sources "
        val col2 = " Number of entries in database "
        val col3 = " Number of unreviewed entries "

        val stringBuilder = StringBuilder("\n\n|$col1|$col2|$col3|")
            .append("\n|")
            .append("-".repeat(col1.length))
            .append("|")
            .append("-".repeat(col2.length))
            .append("|")
            .append("-".repeat(col3.length))
            .append("|")

        ClusterService().clusterDistribution(datasetEntries).forEach {
            stringBuilder.append("\n| %-${col1.length-1}s".format(it.key))
                .append("| %-${col2.length-1}s".format(it.value.first))
                .append("| %-${col3.length-1}s".format(it.value.second))
                .append("|")
        }

        println(stringBuilder.toString())
    }

    private suspend fun checkMergeLocks() {
        print("\nSelect cluster [any number]: ")

        val cluster = waitForUserInput()

        if (!Regex("\\d+").matches(cluster)) {
            checkMergeLocks()
        }

        val result = ClusterService().fetchCluster(datasetEntries, cluster.toInt())
            .filterNot { DefaultMergeLockAccessor.instance.hasMergeLock(it.sources) }

        val clusterEntries = if (cluster.toInt() == 1) {
            result.filterNot { DefaultReviewedIsolatedEntriesAccessor.instance.contains(it.sources.first()) }
        } else {
            result
        }

        repeat(clusterEntries.size) { index ->
            println("\n\n${index + 1} / ${clusterEntries.size}")

            val currentEntry = clusterEntries[index]
            val mergeLock = mergeLockCreation(currentEntry to currentEntry.sources)
            if (mergeLock.isNotEmpty()) {
                DefaultMergeLockAccessor.instance.addMergeLock(mergeLock)
            }
        }
        println("Done checking merge locks. Currently reprocessing merging.")
        reprocessMerging()
    }

    private suspend fun markAsDeadEntry() {
        print("\nSource: ")
        val input = waitForUserInput()

        when {
            isValidUrl(input) -> DeadEntryCreator.markAsDeadEntry(URI(input))
            else -> println("Invalid URL.")
        }
    }

    private suspend fun loadAnime() {
        print("\nSource: ")
        val input = waitForUserInput()

        when {
            isValidUrl(input) -> AnimeLoader.load(URI(input))
            else -> println("Invalid URL.")
        }
    }

    private suspend fun mergeLockCreation(currentEntry: Pair<Anime, Set<URI>>, openUrls: Boolean = true): Set<URI> {
        val currentEntryUris = currentEntry.second.sorted().toMutableList()
        val urisToOpenInBrowser = OpenInBrowserHelper.determineUrisToOpenInBrowser(currentEntry)

        if (openUrls) {
            openUrls(urisToOpenInBrowser)
        }

        print("\nOption: ")
        val input = waitForUserInput()

        return when {
            input == "keep" -> currentEntryUris.toSet()
            input == "skip" -> emptySet()
            input == "exit" -> mainMenu()
            input == "check" && currentEntryUris.size == 1 -> {
                DefaultReviewedIsolatedEntriesAccessor.instance.addCheckedEntry(currentEntryUris.first())
                emptySet()
            }
            isValidUrl(input) -> {
                val urlToBeAdded = URI(input)
                currentEntryUris.add(urlToBeAdded)

                if (DefaultMergeLockAccessor.instance.isPartOfMergeLock(urlToBeAdded)) {
                    currentEntryUris.addAll(DefaultMergeLockAccessor.instance.getMergeLock(urlToBeAdded))
                }

                mergeLockCreation(currentEntry.first to currentEntryUris.toHashSet(), openUrls = false)
            }
            else -> mergeLockCreation(currentEntry, openUrls = false)
        }
    }

    private suspend fun createMergeLock() {
        val mergeLock = createNewMergeLockFromScratch(emptySet())

        if (mergeLock.isNotEmpty()) {
            DefaultMergeLockAccessor.instance.addMergeLock(mergeLock)
        }
    }

    private suspend fun extendExistingMergeLock(urlToBeAdded: String? = null) {
        println("[a valid URL] (= URL to be added to merge lock)    [exit] (= back to main menu)")
        print("URL to be added: ")

        val urlToBeAddedUserInput = urlToBeAdded ?: waitForUserInput()

        when {
            urlToBeAddedUserInput == "exit" -> mainMenu()
            isValidUrl(urlToBeAddedUserInput) -> {
                println("\n[a valid URL] (= Any URL of an existing merge lock)    [exit] (= back to main menu)")
                print("Any URL from the existing merge lock: ")

                val mergeLockUrlUserInput = urlToBeAdded ?: waitForUserInput()

                when {
                    mergeLockUrlUserInput == "exit" -> mainMenu()
                    isValidUrl(mergeLockUrlUserInput) -> {
                        val mergeLockUri = URI(mergeLockUrlUserInput)

                        when {
                            DefaultMergeLockAccessor.instance.isPartOfMergeLock(mergeLockUri) -> {
                                val newMergeLock = DefaultMergeLockAccessor.instance.getMergeLock(mergeLockUri).toHashSet().apply {
                                    add(URI(urlToBeAddedUserInput))
                                }

                                println("\n${Json.toJson(newMergeLock.toList().sorted())}")
                                println("\n[keep] (= add merge lock as is)    [exit] (= back to main menu)")
                                print("Select: ")

                                val userInput = waitForUserInput()
                                when (userInput) {
                                    "keep" -> DefaultMergeLockAccessor.instance.addMergeLock(newMergeLock)
                                    "exit" -> mainMenu()
                                    else -> extendExistingMergeLock(urlToBeAddedUserInput)
                                }
                            }
                            else -> {
                                println("Merge lock doesn't exist for [$mergeLockUrlUserInput]")
                                extendExistingMergeLock(urlToBeAddedUserInput)
                            }
                        }
                    }
                    else -> extendExistingMergeLock(urlToBeAddedUserInput)
                }
            }
            else -> extendExistingMergeLock()
        }
    }

    private suspend fun createNewMergeLockFromScratch(sourcesOfCurrentEntry: Set<URI>): Set<URI> {
        println("[keep] (= add merge lock as is)    [a valid URL] (= add URL to merge lock)    [exit] (= back to main menu)")
        println("\n${Json.toJson(sourcesOfCurrentEntry)}")

        print("\nSelect: ")
        val input = waitForUserInput()

        return when {
            input == "keep" -> sourcesOfCurrentEntry
            input == "exit" -> mainMenu()
            isValidUrl(input) -> {
                createNewMergeLockFromScratch(sourcesOfCurrentEntry + URI(input))
            }
            else -> createNewMergeLockFromScratch(sourcesOfCurrentEntry)
        }
    }

    private suspend fun reprocessMerging() {
        Reprocessor.reprocess()
        datasetEntries.clear()
        datasetEntries.addAll(DefaultDatasetFileAccessor.instance.fetchEntries())
    }

    private fun isValidUrl(value: String): Boolean {
        return AppConfig.instance.metaDataProviderConfigurations()
            .any { value.startsWith(it.buildAnimeLink(EMPTY).toString()) }
    }

    private fun waitForUserInput() = readlnOrNull()?.trim() ?: throw IllegalStateException("Invalid input")

    private fun openUrls(sources: Collection<URI>) = sources.forEach { Desktop.getDesktop().browse(it) }

    private enum class Options(val value: String) {
        DUPLICATES("1"),
        CLUSTER_SIZES("2"),
        DCS_STATISTICS("3"),
        // ----------
        CHECK_MERGE_LOCKS("c"),
        MARK_DEAD_ENTRY("d"),
        LOAD_ENTRY("l"),
        CREATE_NEW_MERGE_LOCK("n"),
        ADD_TO_EXISTING_MERGE_LOCK("a"),
        // ----------
        REPROCESS_MERGING("r"),
        // ----------
        QUIT("q");

        companion object {
            fun from(value: String) = entries.find { it.value == value }
        }
    }
}