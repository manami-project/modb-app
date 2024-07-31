package io.github.manamiproject.modb.app.processors.postprocessors

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.AppConfig.Companion.CONFIG_NAMESPACE
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.config.ConfigRegistry
import io.github.manamiproject.modb.core.config.DefaultConfigRegistry
import io.github.manamiproject.modb.core.config.LongPropertyDelegate
import io.github.manamiproject.modb.core.extensions.directoryExists
import io.github.manamiproject.modb.core.extensions.fileName
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlin.io.path.listDirectoryEntries

class DeleteOldDownloadDirectoriesPostProcessor(
    configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
    private val appConfig: Config = AppConfig.instance,
): PostProcessor {

    private val keepDownloadDirectories: Long by LongPropertyDelegate(
        configRegistry = configRegistry,
        default = 1L,
        namespace = CONFIG_NAMESPACE,
    )

    override fun process() {
        log.info { "Deleting old download directories." }

        val downloadDirectory = appConfig.downloadsDirectory()
            .listDirectoryEntries()
            .filter { it.directoryExists() }
            .filter { "^\\d{4}-\\d{2}$".toRegex().matches(it.fileName()) }
            .sorted()
            .toList()

        if (downloadDirectory.size == keepDownloadDirectories.toInt()) {
            log.info { "Skipping download directories removal, because there are only [${downloadDirectory.size}] directories." }
            return
        }

        val directoriesToRemain = downloadDirectory.takeLast(keepDownloadDirectories.toInt())

        downloadDirectory.filterNot { directoriesToRemain.contains(it) }.forEach {
            log.info { "Deleting [${it.toAbsolutePath()}]" }
            it.toFile().deleteRecursively()
        }
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DeleteOldDownloadDirectoriesPostProcessor]
         * @since 1.0.0
         */
        val instance = DeleteOldDownloadDirectoriesPostProcessor()
    }
}