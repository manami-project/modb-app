package io.github.manamiproject.modb.core.logging

import kotlin.reflect.KProperty

/**
 * Creates a [Logger] for the class in which it is being instantiated.
 * Implementation uses SLF4J. You use bridges to redirect log statements.
 *
 * Global log level can be set either by configuration parameter **modb.logging.logLevel**.
 *
 * **Usage:**
 * ```
 * companion object {
 *   private val log by LoggerDelegate()
 * }
 * ```
 * @since 7.0.0
 * @param logLevel Local override for the log level. This log level will only apply to this specific logger instance.
 * If nothing is set then the global configuration will be used.
 */
public class LoggerDelegate(private val logLevel: LogLevelValue = LogLevelValue.NotSet) {

    /**
     * Creates the [Logger] for use in delegation.
     * @since 7.0.0
     * @return Actual instance of the logger to use.
     */
    public operator fun getValue(thisRef: Any, property: KProperty<*>): Logger {
        return ModbLogger(ref = thisRef::class, logLevel = logLevel)
    }
}