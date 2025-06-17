package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.dataset.DatasetFileType.JSON_MINIFIED
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.app.dataset.DefaultDeadEntriesAccessor
import io.github.manamiproject.modb.core.extensions.changeSuffix
import io.github.manamiproject.modb.core.extensions.readFile
import io.github.manamiproject.modb.core.extensions.writeToZstandardFile
import io.github.manamiproject.modb.core.logging.LoggerDelegate

/**
 * Creates Zstandard files for dead entries files.
 * @since 1.10.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property deadEntriesAccessor Access to dead entries files.
 */
class ZstandardFilesForDeadEntriesCreatorPostProcessor(
    private val appConfig: Config = AppConfig.instance,
    private val deadEntriesAccessor: DeadEntriesAccessor = DefaultDeadEntriesAccessor.instance,
): PostProcessor {

    override suspend fun process(): Boolean {
        log.info { "Creating Zstandard files for dead entries files." }

        appConfig.metaDataProviderConfigurations()
            .filter { appConfig.deadEntriesSupported(it) }
            .forEach { config ->
                with(deadEntriesAccessor.deadEntriesFile(config, JSON_MINIFIED)) {
                    readFile().writeToZstandardFile(this.parent.resolve(this.fileName.changeSuffix("json.zst")))
                }
            }

        return true
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [ZstandardFilesForDeadEntriesCreatorPostProcessor]
         * @since 1.0.0
         */
        val instance: ZstandardFilesForDeadEntriesCreatorPostProcessor by lazy { ZstandardFilesForDeadEntriesCreatorPostProcessor() }
    }
}