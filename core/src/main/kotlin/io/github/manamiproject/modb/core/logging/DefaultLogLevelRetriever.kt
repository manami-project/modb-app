package io.github.manamiproject.modb.core.logging

import io.github.manamiproject.modb.core.config.ConfigRegistry
import io.github.manamiproject.modb.core.config.DefaultConfigRegistry
import io.github.manamiproject.modb.core.config.StringPropertyDelegate

internal class DefaultLogLevelRetriever(
    private val localLogLevelOverride: LogLevelValue = LogLevelValue.NotSet,
    configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
): LogLevelRetriever {

    private val logLevel by StringPropertyDelegate(
        namespace = "modb.core.logging",
        default = LogLevel.INFO.toString(),
        configRegistry = configRegistry,
    )

    override fun logLevel(): LogLevel = when (localLogLevelOverride) {
        LogLevel.OFF -> LogLevel.OFF
        LogLevel.ERROR -> LogLevel.ERROR
        LogLevel.WARN -> LogLevel.WARN
        LogLevel.INFO -> LogLevel.INFO
        LogLevel.DEBUG -> LogLevel.DEBUG
        LogLevel.TRACE -> LogLevel.TRACE
        LogLevelValue.NotSet -> LogLevel.of(logLevel) ?: LogLevel.INFO
    }
}