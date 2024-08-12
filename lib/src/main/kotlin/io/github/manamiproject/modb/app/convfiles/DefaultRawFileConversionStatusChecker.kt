package io.github.manamiproject.modb.app.convfiles

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.excludeFromTestContext
import io.github.manamiproject.modb.core.extensions.fileSuffix
import io.github.manamiproject.modb.core.extensions.listRegularFiles
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.toDuration

/**
 * Default implementation of [RawFileConversionStatusChecker].
 * @since 1.0.0
 * @param appConfig Application specific configuration. Uses [AppConfig] by default.
 */
class DefaultRawFileConversionStatusChecker(
    private val appConfig: Config = AppConfig.instance,
): RawFileConversionStatusChecker {

    override suspend fun unconvertedFilesExist(): Boolean {
        log.info { "Checking if there are still files which need to be converted." }

        var unconverted = 0
        var converted = 0

        appConfig.metaDataProviderConfigurations().forEach { config ->
            appConfig.workingDir(config)
                .listRegularFiles()
                .forEach { file ->
                    when {
                        file.fileSuffix() == config.fileSuffix() -> unconverted++
                        file.fileSuffix() == CONVERTED_FILE_SUFFIX -> converted++
                    }
                }
        }

        return unconverted != converted
    }

    override suspend fun waitForAllRawFilesToBeConverted() {
        withTimeout(10.toDuration(SECONDS)) {
            while (unconvertedFilesExist()) {
                excludeFromTestContext(appConfig) {
                    delay(2.toDuration(SECONDS))
                }
            }
        }
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DefaultRawFileConversionStatusChecker]
         * @since 1.0.0
         */
        val instance: DefaultRawFileConversionStatusChecker by lazy { DefaultRawFileConversionStatusChecker() }
    }
}