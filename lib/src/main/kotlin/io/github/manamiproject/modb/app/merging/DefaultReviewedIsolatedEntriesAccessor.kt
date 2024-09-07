package io.github.manamiproject.modb.app.merging

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.URI
import kotlin.io.path.appendLines
import kotlin.io.path.createFile
import kotlin.io.path.readLines

/**
 * In-memory implementation of [ReviewedIsolatedEntriesAccessor].
 * Creates the corresponding file it doesn't exist.
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 */
class DefaultReviewedIsolatedEntriesAccessor(
    private val appConfig: Config = AppConfig.instance,
): ReviewedIsolatedEntriesAccessor {

    private val checkedIsolatedEntriesFile = "checked-isolated-entries.txt"
    private val entries = mutableSetOf<URI>()
    private val initializationMutex = Mutex()
    private val writeAccess = Mutex()
    private var isInitialized = false

    override fun contains(uri: URI): Boolean {
        if (!isInitialized) {
            runBlocking {
                init()
            }
        }

        return entries.contains(uri)
    }

    override suspend fun addCheckedEntry(uri: URI) {
        if (!isInitialized) {
            init()
        }

        writeAccess.withLock {
            if (!entries.contains(uri)) {
                log.debug { "Adding reviewed isolated entry [$uri]." }
                entries.add(uri)
                appConfig.downloadControlStateDirectory().resolve(checkedIsolatedEntriesFile).appendLines(setOf(uri.toString()))
            }
        }
    }

    private suspend fun init() {
        initializationMutex.withLock {
            if (!isInitialized) {
                log.info { "Initializing DefaultReviewedIsolatedEntriesAccessor." }

                val file = appConfig.downloadControlStateDirectory().resolve(checkedIsolatedEntriesFile)

                if (!file.regularFileExists()) {
                    file.createFile()
                }

                entries.addAll(file.readLines().filter { it.neitherNullNorBlank() }.map { URI(it) })

                isInitialized = true
            }
        }
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DefaultReviewedIsolatedEntriesAccessor]
         * @since 1.0.0
         */
        val instance: DefaultReviewedIsolatedEntriesAccessor by lazy { DefaultReviewedIsolatedEntriesAccessor() }
    }
}