package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.date.WeekOfYear
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import java.time.LocalDate

/**
 * Creates a release info file which is used for creating a release in the dataset repo.
 * @since 19.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 */
class ReleaseInfoFileCreatorPostProcessor(private val appConfig: Config = AppConfig.instance): PostProcessor {

    override suspend fun process(): Boolean {
        log.info { "Creating release info file." }
        val weekOfYear = WeekOfYear(LocalDate.now(appConfig.clock()))
        val outputFile = appConfig.outputDirectory().resolve("week.release")
        weekOfYear.toString().writeToFile(outputFile)
        return true
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [ReleaseInfoFileCreatorPostProcessor]
         * @since 19.0.0
         */
        val instance: ReleaseInfoFileCreatorPostProcessor by lazy { ReleaseInfoFileCreatorPostProcessor() }
    }
}