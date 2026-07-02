package io.github.manamiproject.modb.app.network

import io.github.manamiproject.kommand.CommandExecutor
import io.github.manamiproject.kommand.CommandLineConfig
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset.UTC
import kotlin.test.Test

internal class FlaresolverrKtTest {

    @Nested
    inner class StartFlaresolverrTests {

        @Test
        fun `correctly builds command with default values`() {
            // given
            val fixedClock = Clock.fixed(Instant.parse("2019-11-17T15:00:00.00Z"), UTC)
            val invocation = mutableListOf<String>()
            val testCommandExecutor = object : CommandExecutor {
                override var config: CommandLineConfig = CommandLineConfig()
                override fun executeCmd(command: List<String>): String {
                    invocation.addAll(command)
                    return "abc123"
                }
            }

            // when
            val result = startFlaresolverr {
                commandExecutor = testCommandExecutor
                clock = fixedClock
            }

            // then
            assertThat(result).isEqualTo("abc123")
            assertThat(invocation).containsExactly(
                "docker",
                "run",
                "-d",
                "--rm",
                "--name=flaresolverr-20191117150000",
                "-p",
                "8191:8191",
                "-e",
                "LOG_LEVEL=info",
                "ghcr.io/flaresolverr/flaresolverr:v3.5.0",
            )
        }

        @Test
        fun `correctly builds command with port`() {
            // given
            val fixedClock = Clock.fixed(Instant.parse("2019-11-17T15:00:00.00Z"), UTC)
            val invocation = mutableListOf<String>()
            val testCommandExecutor = object : CommandExecutor {
                override var config: CommandLineConfig = CommandLineConfig()
                override fun executeCmd(command: List<String>): String {
                    invocation.addAll(command)
                    return "abc123"
                }
            }

            // when
            val result = startFlaresolverr {
                commandExecutor = testCommandExecutor
                clock = fixedClock
                port = 9009
            }

            // then
            assertThat(result).isEqualTo("abc123")
            assertThat(invocation).containsExactly(
                "docker",
                "run",
                "-d",
                "--rm",
                "--name=flaresolverr-20191117150000",
                "-p",
                "9009:8191",
                "-e",
                "LOG_LEVEL=info",
                "ghcr.io/flaresolverr/flaresolverr:v3.5.0",
            )
        }

        @Test
        fun `correctly builds command with log level`() {
            // given
            val fixedClock = Clock.fixed(Instant.parse("2019-11-17T15:00:00.00Z"), UTC)
            val invocation = mutableListOf<String>()
            val testCommandExecutor = object : CommandExecutor {
                override var config: CommandLineConfig = CommandLineConfig()
                override fun executeCmd(command: List<String>): String {
                    invocation.addAll(command)
                    return "abc123"
                }
            }

            // when
            val result = startFlaresolverr {
                commandExecutor = testCommandExecutor
                clock = fixedClock
                logLevel = "error"
            }

            // then
            assertThat(result).isEqualTo("abc123")
            assertThat(invocation).containsExactly(
                "docker",
                "run",
                "-d",
                "--rm",
                "--name=flaresolverr-20191117150000",
                "-p",
                "8191:8191",
                "-e",
                "LOG_LEVEL=error",
                "ghcr.io/flaresolverr/flaresolverr:v3.5.0",
            )
        }

        @Test
        fun `successfully fetches container id, because app is already running`() {
            // given
            val fixedClock = Clock.fixed(Instant.parse("2019-11-17T15:00:00.00Z"), UTC)
            val invocation = mutableListOf<String>()
            var invocationCounter = 0
            val testCommandExecutor = object : CommandExecutor {
                override var config: CommandLineConfig = CommandLineConfig()
                override fun executeCmd(command: List<String>): String {
                    invocation.addAll(command)
                    val ret = if (invocationCounter == 0) "port is already allocated" else "abc123"
                    invocationCounter++
                    return ret
                }
            }

            // when
            val result = startFlaresolverr {
                commandExecutor = testCommandExecutor
                clock = fixedClock
            }

            // then
            assertThat(result).isEqualTo("abc123")
            assertThat(invocation).containsExactly(
                "docker",
                "run",
                "-d",
                "--rm",
                "--name=flaresolverr-20191117150000",
                "-p",
                "8191:8191",
                "-e",
                "LOG_LEVEL=info",
                "ghcr.io/flaresolverr/flaresolverr:v3.5.0",
                "docker",
                "ps",
                "--filter",
                "name=flaresolverr-",
                "--no-trunc",
                "--format",
                "\"{{.ID}}\"",
            )
        }

        @ParameterizedTest
        @ValueSource(strings = [
            "there was an error",
            "docker: Failed to do",
        ])
        fun `throws exception for any error indicator`(value: String) {
            // given
            val fixedClock = Clock.fixed(Instant.parse("2019-11-17T15:00:00.00Z"), UTC)
            val testCommandExecutor = object : CommandExecutor {
                override var config: CommandLineConfig = CommandLineConfig()
                override fun executeCmd(command: List<String>): String = value
            }

            // when
            val result = exceptionExpected<IllegalStateException> {
                startFlaresolverr {
                    commandExecutor = testCommandExecutor
                    clock = fixedClock
                }
            }

            // then
            assertThat(result).hasMessage("Error during container start:\n$value")
        }
    }

    @Nested
    inner class StopFlaresolverrTests {

        @Test
        fun `correctly builds command`() {
            // given
            val invocation = mutableListOf<String>()
            val testCommandExecutor = object : CommandExecutor {
                override var config: CommandLineConfig = CommandLineConfig()
                override fun executeCmd(command: List<String>): String {
                    invocation.addAll(command)
                    return "abc123"
                }
            }

            // when
            stopFlaresolverr("abc123") {
                commandExecutor = testCommandExecutor
            }

            // then
            assertThat(invocation).containsExactly(
                "docker",
                "stop",
                "abc123",
            )
        }

        @Test
        fun `throws exception in case of an error`() {
            // given
            val invocation = mutableListOf<String>()
            val testCommandExecutor = object : CommandExecutor {
                override var config: CommandLineConfig = CommandLineConfig()
                override fun executeCmd(command: List<String>): String {
                    invocation.addAll(command)
                    return "anything else"
                }
            }

            // when
            val result = exceptionExpected<IllegalStateException> {
                stopFlaresolverr("abc123") {
                    commandExecutor = testCommandExecutor
                }
            }

            // then
            assertThat(result).hasMessage("Error during container start:\nanything else")
        }
    }
}