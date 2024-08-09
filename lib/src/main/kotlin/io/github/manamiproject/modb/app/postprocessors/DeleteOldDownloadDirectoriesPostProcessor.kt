package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.AppConfig.Companion.CONFIG_NAMESPACE
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.config.ConfigRegistry
import io.github.manamiproject.modb.core.config.DefaultConfigRegistry
import io.github.manamiproject.modb.core.config.IntPropertyDelegate
import io.github.manamiproject.modb.core.extensions.directoryExists
import io.github.manamiproject.modb.core.extensions.fileName
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlin.io.path.listDirectoryEntries

/**
 * Removes download directories from previous weeks.
 * The number of directories to keep can be configured using `modb.app.keepDownloadDirectories`. Default is `1` which
 * means that only the directory from the current week is kept.
 * @since 1.0.0
 * @param configRegistry Implementation of [ConfigRegistry] used for populating properties. Uses [DefaultConfigRegistry] by default.
 * @param appConfig Application specific configuration. Uses [AppConfig] by default.
 */
class DeleteOldDownloadDirectoriesPostProcessor(
    configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
    private val appConfig: Config = AppConfig.instance,
): PostProcessor {

    private val keepDownloadDirectories: Int by IntPropertyDelegate(
        configRegistry = configRegistry,
        default = 1,
        namespace = CONFIG_NAMESPACE,
        validator = { value -> value >= 1 }
    )

    override suspend fun process(): Boolean {
        log.info { "Deleting old download directories." }

        val downloadDirectory = appConfig.downloadsDirectory()
            .listDirectoryEntries()
            .filter { it.directoryExists() }
            .filter { "^\\d{4}-\\d{2}$".toRegex().matches(it.fileName()) }
            .sorted()
            .toList()

        if (downloadDirectory.size == keepDownloadDirectories) {
            log.info { "Skipping download directories removal, because there are only [${downloadDirectory.size}] directories." }
            return true
        }

        val directoriesToRemain = downloadDirectory.takeLast(keepDownloadDirectories)

        downloadDirectory.filterNot { directoriesToRemain.contains(it) }.forEach {
            log.debug { "Deleting [${it.toAbsolutePath()}]" }
            it.toFile().deleteRecursively()
        }

        return true
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DeleteOldDownloadDirectoriesPostProcessor]
         * @since 1.0.0
         */
        val instance: DeleteOldDownloadDirectoriesPostProcessor by lazy { DeleteOldDownloadDirectoriesPostProcessor() }
    }
}