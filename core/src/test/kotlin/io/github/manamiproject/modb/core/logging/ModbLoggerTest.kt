package io.github.manamiproject.modb.core.logging

import io.github.manamiproject.modb.core.TestLoggerImplementation
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.logging.LogLevel.OFF
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.net.SocketTimeoutException
import kotlin.test.Test

internal class ModbLoggerTest {

    @Nested
    inner class OffTests {

        @Test
        fun `don't log an error if loglevel is OFF`() {
            // given
            var invokedStatement = EMPTY
            val testLogger = object: Logger by TestLoggerImplementation {
                override fun error(message: () -> String) {
                    invokedStatement = message.invoke()
                }
            }

            val modbLogger = ModbLogger(
                ref = this::class,
                logLevel = OFF,
                delegate = testLogger,
            )

            // when
            modbLogger.error { "test-log-statement" }

            // then
            assertThat(invokedStatement).isEmpty()
        }

        @Test
        fun `don't log an error with exception if loglevel is OFF`() {
            // given
            var invokedStatement = EMPTY
            val testLogger = object: Logger by TestLoggerImplementation {
                override fun error(message: () -> String) {
                    invokedStatement = message.invoke()
                }
            }

            val modbLogger = ModbLogger(
                ref = this::class,
                logLevel = OFF,
                delegate = testLogger,
            )

            // when
            modbLogger.error(SocketTimeoutException("custom-message-here")) { "test-log-statement" }

            // then
            assertThat(invokedStatement).isEmpty()
        }

        @Test
        fun `don't log a warning if loglevel is OFF`() {
            // given
            var invokedStatement = EMPTY
            val testLogger = object: Logger by TestLoggerImplementation {
                override fun warn(message: () -> String) {
                    invokedStatement = message.invoke()
                }
            }

            val modbLogger = ModbLogger(
                ref = this::class,
                logLevel = OFF,
                delegate = testLogger,
            )

            // when
            modbLogger.warn { "test-log-statement" }

            // then
            assertThat(invokedStatement).isEmpty()
        }

        @Test
        fun `don't log a warning with exception if loglevel is OFF`() {
            // given
            var invokedStatement = EMPTY
            val testLogger = object: Logger by TestLoggerImplementation {
                override fun warn(message: () -> String) {
                    invokedStatement = message.invoke()
                }
            }

            val modbLogger = ModbLogger(
                ref = this::class,
                logLevel = OFF,
                delegate = testLogger,
            )

            // when
            modbLogger.warn(SocketTimeoutException("custom-message-here")) { "test-log-statement" }

            // then
            assertThat(invokedStatement).isEmpty()
        }

        @Test
        fun `don't log an info if loglevel is OFF`() {
            // given
            var invokedStatement = EMPTY
            val testLogger = object: Logger by TestLoggerImplementation {
                override fun info(message: () -> String) {
                    invokedStatement = message.invoke()
                }
            }

            val modbLogger = ModbLogger(
                ref = this::class,
                logLevel = OFF,
                delegate = testLogger,
            )

            // when
            modbLogger.info { "test-log-statement" }

            // then
            assertThat(invokedStatement).isEmpty()
        }

        @Test
        fun `don't log a debug statement if loglevel is OFF`() {
            // given
            var invokedStatement = EMPTY
            val testLogger = object: Logger by TestLoggerImplementation {
                override fun debug(message: () -> String) {
                    invokedStatement = message.invoke()
                }
            }

            val modbLogger = ModbLogger(
                ref = this::class,
                logLevel = OFF,
                delegate = testLogger,
            )

            // when
            modbLogger.debug { "test-log-statement" }

            // then
            assertThat(invokedStatement).isEmpty()
        }

        @Test
        fun `don't log a trace statement if loglevel is OFF`() {
            // given
            var invokedStatement = EMPTY
            val testLogger = object: Logger by TestLoggerImplementation {
                override fun trace(message: () -> String) {
                    invokedStatement = message.invoke()
                }
            }

            val modbLogger = ModbLogger(
                ref = this::class,
                logLevel = OFF,
                delegate = testLogger,
            )

            // when
            modbLogger.trace { "test-log-statement" }

            // then
            assertThat(invokedStatement).isEmpty()
        }
    }

    @Nested
    inner class ErrorTests {

        @ParameterizedTest
        @EnumSource(value = LogLevel::class, names = ["ERROR", "WARN", "INFO", "DEBUG", "TRACE"])
        fun `delegate log statement if the log level fits`(value: LogLevel) {
            // given
            var invokedStatement = EMPTY
            val testLogger = object: Logger by TestLoggerImplementation {
                override fun error(message: () -> String) {
                    invokedStatement = message.invoke()
                }
            }

            val modbLogger = ModbLogger(
                ref = this::class,
                logLevel = value,
                delegate = testLogger,
            )

            // when
            modbLogger.error { "test-log-statement" }

            // then
            assertThat(invokedStatement).isEqualTo("test-log-statement")
        }

        @ParameterizedTest
        @EnumSource(value = LogLevel::class, names = ["ERROR", "WARN", "INFO", "DEBUG", "TRACE"])
        fun `delegate log statement with exception if the log level fits`(value: LogLevel) {
            // given
            var invokedStatement = EMPTY
            val testLogger = object: Logger by TestLoggerImplementation {
                override fun error(message: () -> String) {
                    invokedStatement = message.invoke()
                }
            }

            val modbLogger = ModbLogger(
                ref = this::class,
                logLevel = value,
                delegate = testLogger,
            )

            val exception = SocketTimeoutException("custom-message-here")

            // when
            modbLogger.error(exception) { "test-log-statement" }

            // then
            assertThat(invokedStatement.split('\n').take(2).joinToString("\n")).isEqualTo("""
                test-log-statement
                java.net.SocketTimeoutException: custom-message-here
            """.trimIndent())
        }

        @ParameterizedTest
        @EnumSource(value = LogLevel::class, names = ["OFF"])
        fun `don't do anything if loglevel is doesn't fit`(value: LogLevel) {
            // given
            var invokedStatement = EMPTY
            val testLogger = object: Logger by TestLoggerImplementation {
                override fun error(message: () -> String) {
                    invokedStatement = message.invoke()
                }
            }

            val modbLogger = ModbLogger(
                ref = this::class,
                logLevel = value,
                delegate = testLogger,
            )

            // when
            modbLogger.error { "test-log-statement" }

            // then
            assertThat(invokedStatement).isEmpty()
        }
    }

    @Nested
    inner class WarnTests {

        @ParameterizedTest
        @EnumSource(value = LogLevel::class, names = ["WARN", "INFO", "DEBUG", "TRACE"])
        fun `delegate log statement if the log level fits`(value: LogLevel) {
            // given
            var invokedStatement = EMPTY
            val testLogger = object: Logger by TestLoggerImplementation {
                override fun warn(message: () -> String) {
                    invokedStatement = message.invoke()
                }
            }

            val modbLogger = ModbLogger(
                ref = this::class,
                logLevel = value,
                delegate = testLogger,
            )

            // when
            modbLogger.warn { "test-log-statement" }

            // then
            assertThat(invokedStatement).isEqualTo("test-log-statement")
        }

        @ParameterizedTest
        @EnumSource(value = LogLevel::class, names = ["WARN", "INFO", "DEBUG", "TRACE"])
        fun `delegate log statement with exception if the log level fits`(value: LogLevel) {
            // given
            var invokedStatement = EMPTY
            val testLogger = object: Logger by TestLoggerImplementation {
                override fun warn(message: () -> String) {
                    invokedStatement = message.invoke()
                }
            }

            val modbLogger = ModbLogger(
                ref = this::class,
                logLevel = value,
                delegate = testLogger,
            )

            val exception = SocketTimeoutException("custom-message-here")

            // when
            modbLogger.warn(exception) { "test-log-statement" }

            // then
            assertThat(invokedStatement.split('\n').take(2).joinToString("\n")).isEqualTo("""
                test-log-statement
                java.net.SocketTimeoutException: custom-message-here
            """.trimIndent())
        }

        @ParameterizedTest
        @EnumSource(value = LogLevel::class, names = ["OFF", "ERROR"])
        fun `don't do anything if loglevel is doesn't fit`(value: LogLevel) {
            // given
            var invokedStatement = EMPTY
            val testLogger = object: Logger by TestLoggerImplementation {
                override fun warn(message: () -> String) {
                    invokedStatement = message.invoke()
                }
            }

            val modbLogger = ModbLogger(
                ref = this::class,
                logLevel = value,
                delegate = testLogger,
            )

            // when
            modbLogger.warn { "test-log-statement" }

            // then
            assertThat(invokedStatement).isEmpty()
        }
    }

    @Nested
    inner class InfoTests {

        @ParameterizedTest
        @EnumSource(value = LogLevel::class, names = ["INFO", "DEBUG", "TRACE"])
        fun `delegate log statement if the log level fits`(value: LogLevel) {
            // given
            var invokedStatement = EMPTY
            val testLogger = object: Logger by TestLoggerImplementation {
                override fun info(message: () -> String) {
                    invokedStatement = message.invoke()
                }
            }

            val modbLogger = ModbLogger(
                ref = this::class,
                logLevel = value,
                delegate = testLogger,
            )

            // when
            modbLogger.info { "test-log-statement" }

            // then
            assertThat(invokedStatement).isEqualTo("test-log-statement")
        }

        @ParameterizedTest
        @EnumSource(value = LogLevel::class, names = ["OFF", "ERROR", "WARN"])
        fun `don't do anything if loglevel is doesn't fit`(value: LogLevel) {
            // given
            var invokedStatement = EMPTY
            val testLogger = object: Logger by TestLoggerImplementation {
                override fun info(message: () -> String) {
                    invokedStatement = message.invoke()
                }
            }

            val modbLogger = ModbLogger(
                ref = this::class,
                logLevel = value,
                delegate = testLogger,
            )

            // when
            modbLogger.info { "test-log-statement" }

            // then
            assertThat(invokedStatement).isEmpty()
        }
    }

    @Nested
    inner class DebugTests {

        @ParameterizedTest
        @EnumSource(value = LogLevel::class, names = ["DEBUG", "TRACE"])
        fun `delegate log statement if the log level fits`(value: LogLevel) {
            // given
            var invokedStatement = EMPTY
            val testLogger = object: Logger by TestLoggerImplementation {
                override fun debug(message: () -> String) {
                    invokedStatement = message.invoke()
                }
            }

            val modbLogger = ModbLogger(
                ref = this::class,
                logLevel = value,
                delegate = testLogger,
            )

            // when
            modbLogger.debug { "test-log-statement" }

            // then
            assertThat(invokedStatement).isEqualTo("test-log-statement")
        }

        @ParameterizedTest
        @EnumSource(value = LogLevel::class, names = ["OFF", "ERROR", "WARN", "INFO"])
        fun `don't do anything if loglevel is doesn't fit`(value: LogLevel) {
            // given
            var invokedStatement = EMPTY
            val testLogger = object: Logger by TestLoggerImplementation {
                override fun debug(message: () -> String) {
                    invokedStatement = message.invoke()
                }
            }

            val modbLogger = ModbLogger(
                ref = this::class,
                logLevel = value,
                delegate = testLogger,
            )

            // when
            modbLogger.debug { "test-log-statement" }

            // then
            assertThat(invokedStatement).isEmpty()
        }
    }

    @Nested
    inner class TraceTests {

        @ParameterizedTest
        @EnumSource(value = LogLevel::class, names = ["TRACE"])
        fun `delegate log statement if the log level fits`(value: LogLevel) {
            // given
            var invokedStatement = EMPTY
            val testLogger = object: Logger by TestLoggerImplementation {
                override fun trace(message: () -> String) {
                    invokedStatement = message.invoke()
                }
            }

            val modbLogger = ModbLogger(
                ref = this::class,
                logLevel = value,
                delegate = testLogger,
            )

            // when
            modbLogger.trace { "test-log-statement" }

            // then
            assertThat(invokedStatement).isEqualTo("test-log-statement")
        }

        @ParameterizedTest
        @EnumSource(value = LogLevel::class, names = ["OFF", "ERROR", "WARN", "INFO", "DEBUG"])
        fun `don't do anything if loglevel is doesn't fit`(value: LogLevel) {
            // given
            var invokedStatement = EMPTY
            val testLogger = object: Logger by TestLoggerImplementation {
                override fun trace(message: () -> String) {
                    invokedStatement = message.invoke()
                }
            }

            val modbLogger = ModbLogger(
                ref = this::class,
                logLevel = value,
                delegate = testLogger,
            )

            // when
            modbLogger.trace { "test-log-statement" }

            // then
            assertThat(invokedStatement).isEmpty()
        }
    }
}