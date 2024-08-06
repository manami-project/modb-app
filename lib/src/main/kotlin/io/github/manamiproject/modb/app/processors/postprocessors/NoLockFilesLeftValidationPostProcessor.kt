package io.github.manamiproject.modb.app.processors.postprocessors

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import io.github.manamiproject.modb.core.extensions.LOCK_FILE_SUFFIX
import io.github.manamiproject.modb.core.extensions.listRegularFiles
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.withContext

/**
 * Checks if there lock files still exist somewhere which could indicate that there has been a problem when writing a
 * file.
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property downloadControlStateAccessor Access to DCS files.
 */
class NoLockFilesLeftValidationPostProcessor(
    private val appConfig: Config = AppConfig.instance,
    private val downloadControlStateAccessor: DownloadControlStateAccessor = DefaultDownloadControlStateAccessor.instance,
): PostProcessor {

    override suspend fun process() = withContext(LIMITED_FS) {
        log.info { "Checking that there are no lock files left." }

        val numberOfLockFilesInWorkingDir = appConfig.metaDataProviderConfigurations().map { config ->
            appConfig.workingDir(config).listRegularFiles("*.$LOCK_FILE_SUFFIX").count()
        }

        val numberOfLockFilesInDcsDir = appConfig.metaDataProviderConfigurations().map { config ->
            downloadControlStateAccessor.downloadControlStateDirectory(config).listRegularFiles("*.$LOCK_FILE_SUFFIX").count()
        }

        check(numberOfLockFilesInWorkingDir.none { it > 0 }) { "Lock file found in workingdir." }
        check(numberOfLockFilesInDcsDir.none { it > 0 }) { "Lock file found in dcs dir." }
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [NoLockFilesLeftValidationPostProcessor]
         * @since 1.0.0
         */
        val instance: NoLockFilesLeftValidationPostProcessor by lazy { NoLockFilesLeftValidationPostProcessor() }
    }
}