package io.github.manamiproject.modb.analyzer.util

import io.github.manamiproject.kommand.CommandExecutor
import io.github.manamiproject.kommand.JavaProcessBuilder
import java.net.URI

/**
 * @since 1.13.0
 */
internal data class OpenUriConfig(
    var commandExecutor: CommandExecutor = JavaProcessBuilder(),
)

/**
 * @since 1.13.0
 */
internal fun openUri(uri: URI, config: OpenUriConfig.() -> Unit = { }): String {
    val currentConfig = OpenUriConfig().apply(config)

    val cmdBuilder = listOf(
        "open",
        uri.toString(),
    )

    return currentConfig.commandExecutor.executeCmd(cmdBuilder)
}