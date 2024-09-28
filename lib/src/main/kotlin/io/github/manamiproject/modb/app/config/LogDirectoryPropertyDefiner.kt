package io.github.manamiproject.modb.app.config

import ch.qos.logback.core.PropertyDefinerBase
import io.github.manamiproject.modb.core.config.ConfigRegistry
import io.github.manamiproject.modb.core.config.DefaultConfigRegistry
import io.github.manamiproject.modb.core.config.StringPropertyDelegate
import io.github.manamiproject.modb.core.coverage.KoverIgnore

/**
 * Specifies the log directory for logback implementation.
 * Uses a property which can be set using key `modb.app.logFileDirectory`.
 * @since 1.0.0
 * @param appConfig Application specific configuration. Uses [AppConfig] by default.
 * @param configRegistry Implementation of [ConfigRegistry] used for populating properties. Uses [DefaultConfigRegistry] by default.
 */
internal class LogDirectoryPropertyDefiner @KoverIgnore constructor(
    appConfig: Config = AppConfig.instance,
    configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
) : PropertyDefinerBase() {

    private val logFileDirectory by StringPropertyDelegate(
        namespace = "modb.app",
        configRegistry = configRegistry,
        default = appConfig.currentWeekWorkingDir().resolve("logs").toAbsolutePath().toString()
    )

    override fun getPropertyValue(): String = logFileDirectory
}