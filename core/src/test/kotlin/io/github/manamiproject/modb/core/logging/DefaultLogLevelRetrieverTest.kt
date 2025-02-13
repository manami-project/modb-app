package io.github.manamiproject.modb.core.logging

import io.github.manamiproject.modb.core.TestConfigRegistry
import io.github.manamiproject.modb.core.config.ConfigRegistry
import io.github.manamiproject.modb.core.logging.LogLevel.*
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

internal class DefaultLogLevelRetrieverTest {

    @Test
    fun `local override will be used first if set`() {
        // given
        val logLevelRetriever = DefaultLogLevelRetriever(localLogLevelOverride = OFF)

        // when
        val result = logLevelRetriever.logLevel()

        // then
        assertThat(result).isEqualTo(OFF)
    }

    @Test
    fun `value from configRepository will be used when local override is not set`() {
        // given
        val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
            override fun string(key: String): String = if (key == LOG_LEVEL_CONFIG_PROPERTY_NAME) {
                "TRACE"
            } else {
                shouldNotBeInvoked()
            }
        }

        val logLevelRetriever = DefaultLogLevelRetriever(
            configRegistry = testConfigRegistry,
        )

        // when
        val result = logLevelRetriever.logLevel()

        // then
        assertThat(result).isEqualTo(TRACE)
    }

    @Test
    fun `if log level is nowhere defined use INFO as hard coded default`() {
        // given
        val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
            override fun string(key: String): String? = null
        }

        val logLevelRetriever = DefaultLogLevelRetriever(
            localLogLevelOverride = LogLevelValue.NotSet,
            configRegistry = testConfigRegistry,
        )

        // when
        val result = logLevelRetriever.logLevel()

        // then
        assertThat(result).isEqualTo(INFO)
    }

    companion object {
        private const val LOG_LEVEL_CONFIG_PROPERTY_NAME = "modb.core.logging.logLevel"
    }
}