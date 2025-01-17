package io.github.manamiproject.modb.core.logging

import io.github.manamiproject.modb.core.logging.LogLevel.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

internal class LogLevelTest {

    @Nested
    inner class OfTests {

        @ParameterizedTest
        @ValueSource(strings = ["off", "OFF", "OfF"])
        fun `find OFF by string`(value: String) {
            // when
            val result = LogLevel.of(value)

            // then
            assertThat(result).isEqualTo(OFF)
        }

        @ParameterizedTest
        @ValueSource(strings = ["error", "ERROR", "eRRoR"])
        fun `find ERROR by string`(value: String) {
            // when
            val result = LogLevel.of(value)

            // then
            assertThat(result).isEqualTo(ERROR)
        }

        @ParameterizedTest
        @ValueSource(strings = ["warn", "WARN", "wArN"])
        fun `find WARN by string`(value: String) {
            // when
            val result = LogLevel.of(value)

            // then
            assertThat(result).isEqualTo(WARN)
        }

        @ParameterizedTest
        @ValueSource(strings = ["info", "INFO", "iNfO"])
        fun `find INFO by string`(value: String) {
            // when
            val result = LogLevel.of(value)

            // then
            assertThat(result).isEqualTo(INFO)
        }

        @ParameterizedTest
        @ValueSource(strings = ["debug", "DEBUG", "dEbUg"])
        fun `find DEBUG by string`(value: String) {
            // when
            val result = LogLevel.of(value)

            // then
            assertThat(result).isEqualTo(DEBUG)
        }

        @ParameterizedTest
        @ValueSource(strings = ["trace", "TRACE", "tRaCe"])
        fun `find TRACE by string`(value: String) {
            // when
            val result = LogLevel.of(value)

            // then
            assertThat(result).isEqualTo(TRACE)
        }

        @ParameterizedTest
        @ValueSource(strings = ["", "   ", "non-matching-value"])
        fun `returns null of nothing matches`(value: String) {
            // when
            val result = LogLevel.of(value)

            // then
            assertThat(result).isNull()
        }
    }

    @Nested
    inner class ContainsLogLevel {

        @ParameterizedTest
        @EnumSource(value = LogLevel::class, names = ["OFF", "ERROR", "WARN", "INFO", "DEBUG", "TRACE"])
        fun `OFF always returns false`(value: LogLevel) {
            // given
            val logLevel = OFF

            // when
            val result = logLevel.containsLogLevel(value)

            // then
            assertThat(result).isFalse()
        }

        @ParameterizedTest
        @EnumSource(value = LogLevel::class, names = ["OFF", "WARN", "INFO", "DEBUG", "TRACE"])
        fun `ERROR doesn't contain any other level`(value: LogLevel) {
            // given
            val logLevel = ERROR

            // when
            val result = logLevel.containsLogLevel(value)

            // then
            assertThat(result).isFalse()
        }

        @Test
        fun `ERROR contains itself`() {
            // given
            val logLevel = ERROR

            // when
            val result = logLevel.containsLogLevel(ERROR)

            // then
            assertThat(result).isTrue()
        }

        @ParameterizedTest
        @EnumSource(value = LogLevel::class, names = ["OFF", "INFO", "DEBUG", "TRACE"])
        fun `WARN returns false for log levels which it doesn't contain`(value: LogLevel) {
            // given
            val logLevel = WARN

            // when
            val result = logLevel.containsLogLevel(value)

            // then
            assertThat(result).isFalse()
        }

        @ParameterizedTest
        @EnumSource(value = LogLevel::class, names = ["ERROR", "WARN"])
        fun `WARN returns true for log levels it contains`(value: LogLevel) {
            // given
            val logLevel = WARN

            // when
            val result = logLevel.containsLogLevel(value)

            // then
            assertThat(result).isTrue()
        }

        @ParameterizedTest
        @EnumSource(value = LogLevel::class, names = ["OFF", "DEBUG", "TRACE"])
        fun `INFO returns false for log levels which it doesn't contain`(value: LogLevel) {
            // given
            val logLevel = INFO

            // when
            val result = logLevel.containsLogLevel(value)

            // then
            assertThat(result).isFalse()
        }

        @ParameterizedTest
        @EnumSource(value = LogLevel::class, names = ["ERROR", "WARN", "INFO"])
        fun `INFO returns true for log levels it contains`(value: LogLevel) {
            // given
            val logLevel = INFO

            // when
            val result = logLevel.containsLogLevel(value)

            // then
            assertThat(result).isTrue()
        }

        @ParameterizedTest
        @EnumSource(value = LogLevel::class, names = ["OFF", "TRACE"])
        fun `DEBUG returns false for log levels which it doesn't contain`(value: LogLevel) {
            // given
            val logLevel = DEBUG

            // when
            val result = logLevel.containsLogLevel(value)

            // then
            assertThat(result).isFalse()
        }

        @ParameterizedTest
        @EnumSource(value = LogLevel::class, names = ["ERROR", "WARN", "INFO", "DEBUG"])
        fun `DEBUG returns true for log levels it contains`(value: LogLevel) {
            // given
            val logLevel = DEBUG

            // when
            val result = logLevel.containsLogLevel(value)

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `TRACE returns false for OFF`() {
            // given
            val logLevel = TRACE

            // when
            val result = logLevel.containsLogLevel(OFF)

            // then
            assertThat(result).isFalse()
        }

        @ParameterizedTest
        @EnumSource(value = LogLevel::class, names = ["ERROR", "WARN", "INFO", "DEBUG", "TRACE"])
        fun `TRACE returns true for log levels it contains`(value: LogLevel) {
            // given
            val logLevel = TRACE

            // when
            val result = logLevel.containsLogLevel(value)

            // then
            assertThat(result).isTrue()
        }
    }
}