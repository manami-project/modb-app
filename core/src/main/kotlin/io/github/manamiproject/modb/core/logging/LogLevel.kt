package io.github.manamiproject.modb.core.logging

/**
 * @since 13.0.0
 */
public sealed interface LogLevelValue {

    /**
     * @since 13.0.0
     */
    public data object NotSet: LogLevelValue
}

/**
 * TRACE < DEBUG < INFO < WARN < ERROR
 * @since 7.0.0
 */
public enum class LogLevel: LogLevelValue {

    /**
     * Don't log anything at all.
     * @since 7.0.0
     */
    OFF {
        override fun containsLogLevel(logLevel: LogLevel): Boolean = false
    },
    /**
     * @since 7.0.0
     */
    ERROR {
        override fun containsLogLevel(logLevel: LogLevel): Boolean = logLevel == ERROR
    },
    /**
     * @since 7.0.0
     */
    WARN {
        override fun containsLogLevel(logLevel: LogLevel): Boolean = setOf(WARN, ERROR).contains(logLevel)
    },
    /**
     * @since 7.0.0
     */
    INFO {
        override fun containsLogLevel(logLevel: LogLevel): Boolean = setOf(INFO, WARN, ERROR).contains(logLevel)
    },
    /**
     * @since 7.0.0
     */
    DEBUG {
        override fun containsLogLevel(logLevel: LogLevel): Boolean = setOf(DEBUG, INFO, WARN, ERROR).contains(logLevel)
    },
    /**
     * @since 7.0.0
     */
    TRACE {
        override fun containsLogLevel(logLevel: LogLevel): Boolean = setOf(TRACE, DEBUG, INFO, WARN, ERROR).contains(logLevel)
    };

    /**
     * @since 7.0.0
     * @return `true` if the current [LogLevel] contains the given [logLevel]
     */
    public abstract fun containsLogLevel(logLevel: LogLevel): Boolean

    public companion object {
        /**
         * @since 7.0.0
         * @param value Name of the log level as [String]. The check is case insensitive.
         * @return The respective log level that matches the [value] or `null` if there is no match for the [value].
         */
        public fun of(value: String): LogLevel? {
            return entries.find { it.toString() == value.uppercase() }
        }
    }
}