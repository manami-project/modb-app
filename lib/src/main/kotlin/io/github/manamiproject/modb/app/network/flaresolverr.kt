package io.github.manamiproject.modb.app.network

import io.github.manamiproject.kommand.CommandExecutor
import io.github.manamiproject.kommand.JavaProcessBuilder
import io.github.manamiproject.modb.core.config.ConfigRegistry
import io.github.manamiproject.modb.core.config.DefaultConfigRegistry
import io.github.manamiproject.modb.core.config.IntPropertyDelegate
import io.github.manamiproject.modb.core.config.StringPropertyDelegate
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


/**
 * Configuration flaresolverr.
 * @since 1.13.0
 * @property configRegistry Handles the retrieval of the value.
 */
class FlaresolverrConfig(
    configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
) {

    val port by IntPropertyDelegate(
        namespace = "modb.app.flaresolverr",
        default = 8191,
        configRegistry = configRegistry,
    )

    val logLevel by StringPropertyDelegate(
        namespace = "modb.app.flaresolverr",
        default = "info",
        configRegistry = configRegistry,
    )

    companion object {
        /**
         * Singleton of [FlaresolverrConfig]
         * @since 1.13.0
         */
        val instance: FlaresolverrConfig by lazy { FlaresolverrConfig() }
    }
}

/**
 * Configuration for [startFlaresolverr] and [stopFlaresolverr].
 * @since 1.13.0
 * @property commandExecutor Execution platform for commands.
 */
data class FlaresolverrActionConfig(
    var commandExecutor: CommandExecutor = JavaProcessBuilder.instance,
    var port: Int = FlaresolverrConfig.instance.port,
    var logLevel: String = FlaresolverrConfig.instance.logLevel,
    var clock: Clock = Clock.systemUTC(),
)

/**
 * Starts flaresolverr via docker.
 * @since 1.13.0
 * @param config Configuration.
 * @return The container ID if the container was able to start successfully.
 */
fun startFlaresolverr(config: FlaresolverrActionConfig.() -> Unit = { }): String {
    val currentConfig = FlaresolverrActionConfig().apply(config)
    var output = currentConfig.commandExecutor.executeCmd(listOf(
        "docker",
        "run",
        "-d",
        "--rm",
        "--name=flaresolverr-${LocalDateTime.now(currentConfig.clock).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))}",
        "-p",
        "${currentConfig.port}:8191",
        "-e",
        "LOG_LEVEL=${currentConfig.logLevel}",
        "ghcr.io/flaresolverr/flaresolverr:v3.5.0",
    ))

    if (output.contains("port is already allocated")) {
        output = currentConfig.commandExecutor.executeCmd(listOf(
            "docker",
            "ps",
            "--filter",
            "name=flaresolverr-",
            "--no-trunc",
            "--format",
            "\"{{.ID}}\"",
        ))
    }

    if (output.lowercase().contains("error") || output.startsWith("docker:")) throw IllegalStateException("Error during container start:\n$output")

    return output
}

/**
 * Stops the flaresolverr docker container.
 * @since 1.13.0
 * @param config Configuration.
 * @return The container ID if the container was able to start successfully.
 */
fun stopFlaresolverr(containerId: String, config: FlaresolverrActionConfig.() -> Unit = { }) {
    val currentConfig = FlaresolverrActionConfig().apply(config)
    val output = currentConfig.commandExecutor.executeCmd(listOf(
        "docker",
        "stop",
        containerId
    ))

    if (!containerId.startsWith(output)) throw IllegalStateException("Error during container start:\n$output")
}