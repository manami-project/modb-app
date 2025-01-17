package io.github.manamiproject.modb.core.logging

import io.github.manamiproject.modb.core.extensions.EMPTY
import org.assertj.core.api.Assertions.assertThat
import org.slf4j.Logger
import java.net.SocketTimeoutException
import kotlin.test.Test

internal class Slf4jLoggerTest {

    @Test
    fun `delegate logging of error statement`() {
        // given
        var invokedText = EMPTY
        val testImplementation = object: Logger by TestSlf4jImplementation {
            override fun error(msg: String?) {
                invokedText = msg ?: EMPTY
            }
        }

        val logger = Slf4jLogger(
            ref = this::class,
            slf4jLogger = testImplementation,
        )

        // when
        logger.error { "log-statement" }

        // then
        assertThat(invokedText).isEqualTo("log-statement")
    }

    @Test
    fun `simply delegates the message if error statement with exception is called and does nothing with the exception`() {
        // given
        var invokedText = EMPTY
        val testImplementation = object: Logger by TestSlf4jImplementation {
            override fun error(msg: String?) {
                invokedText = msg ?: EMPTY
            }
        }

        val logger = Slf4jLogger(
            ref = this::class,
            slf4jLogger = testImplementation,
        )

        val exception = SocketTimeoutException("custom-message")

        // when
        logger.error(exception) { "log-statement" }

        // then
        assertThat(invokedText).isEqualTo("log-statement")
    }

    @Test
    fun `delegate logging of warn statement`() {
        // given
        var invokedText = EMPTY
        val testImplementation = object: Logger by TestSlf4jImplementation {
            override fun warn(msg: String?) {
                invokedText = msg ?: EMPTY
            }
        }

        val logger = Slf4jLogger(
            ref = this::class,
            slf4jLogger = testImplementation,
        )

        // when
        logger.warn { "log-statement" }

        // then
        assertThat(invokedText).isEqualTo("log-statement")
    }

    @Test
    fun `simply delegates the message if warn statement with exception is called and does nothing with the exception`() {
        // given
        var invokedText = EMPTY
        val testImplementation = object: Logger by TestSlf4jImplementation {
            override fun warn(msg: String?) {
                invokedText = msg ?: EMPTY
            }
        }

        val logger = Slf4jLogger(
            ref = this::class,
            slf4jLogger = testImplementation,
        )

        val exception = SocketTimeoutException("custom-message")

        // when
        logger.warn(exception) { "log-statement" }

        // then
        assertThat(invokedText).isEqualTo("log-statement")
    }

    @Test
    fun `delegate logging of info statement`() {
        // given
        var invokedText = EMPTY
        val testImplementation = object: Logger by TestSlf4jImplementation {
            override fun info(msg: String?) {
                invokedText = msg ?: EMPTY
            }
        }

        val logger = Slf4jLogger(
            ref = this::class,
            slf4jLogger = testImplementation,
        )

        // when
        logger.info { "log-statement" }

        // then
        assertThat(invokedText).isEqualTo("log-statement")
    }

    @Test
    fun `delegate logging of debug statement`() {
        // given
        var invokedText = EMPTY
        val testImplementation = object: Logger by TestSlf4jImplementation {
            override fun debug(msg: String?) {
                invokedText = msg ?: EMPTY
            }
        }

        val logger = Slf4jLogger(
            ref = this::class,
            slf4jLogger = testImplementation,
        )

        // when
        logger.debug { "log-statement" }

        // then
        assertThat(invokedText).isEqualTo("log-statement")
    }

    @Test
    fun `delegate logging of trace statement`() {
        // given
        var invokedText = EMPTY
        val testImplementation = object: Logger by TestSlf4jImplementation {
            override fun trace(msg: String?) {
                invokedText = msg ?: EMPTY
            }
        }

        val logger = Slf4jLogger(
            ref = this::class,
            slf4jLogger = testImplementation,
        )

        // when
        logger.trace { "log-statement" }

        // then
        assertThat(invokedText).isEqualTo("log-statement")
    }
}