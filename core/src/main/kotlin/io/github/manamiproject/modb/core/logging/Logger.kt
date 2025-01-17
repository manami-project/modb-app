package io.github.manamiproject.modb.core.logging

/**
 * Interface for creating log statements.
 * @since 7.0.0
 */
public interface Logger {

    /**
     * Log an error.
     * @since 7.0.0
     * @param message Message that will be logged.
     */
    public fun error(message: () -> String)

    /**
     * Log a warning.
     * @since 7.0.0
     * @param message Message that will be logged.
     */
    public fun warn(message: () -> String)

    /**
     * Log an info.
     * @since 7.0.0
     * @param message Message that will be logged.
     */
    public fun info(message: () -> String)

    /**
     * Log a debug statement.
     * @since 7.0.0
     * @param message Message that will be logged.
     */
    public fun debug(message: () -> String)

    /**
     * Log a trace statement.
     * @since 7.0.0
     * @param message Message that will be logged.
     */
    public fun trace(message: () -> String)

    /**
     * Log an error.
     * @since 7.0.0
     * @param exception Exception causing the need to log the error message.
     * @param message Message that will be logged.
     */
    public fun error(exception: Throwable, message: () -> String)

    /**
     * Log a warning.
     * @since 7.0.0
     * @param exception Exception causing the need to log the warning..
     * @param message Message that will be logged.
     */
    public fun warn(exception: Throwable, message: () -> String)
}