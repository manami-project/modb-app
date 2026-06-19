package io.github.manamiproject.modb.analyzer.util

import io.github.manamiproject.kommand.CommandExecutor
import io.github.manamiproject.kommand.CommandLineConfig
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class OpenUriKtTest {

    @Test
    fun `correctly creates command`() {
        // given
        val testCommandExecutor = object : CommandExecutor {
            override var config: CommandLineConfig = CommandLineConfig()
            override fun executeCmd(command: List<String>): String = command.joinToString(" ")
        }

        // when
        val result = openUri(URI("http://locahost")) {
            commandExecutor = testCommandExecutor
        }

        // then
        assertThat(result).isEqualTo("open http://locahost")
    }
}