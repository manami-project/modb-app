package io.github.manamiproject.modb.app.network

import io.github.manamiproject.kommand.CommandExecutor
import io.github.manamiproject.kommand.JavaProcessBuilder
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank

private typealias Device = String
private typealias Lines = List<String>

/**
 * Configuration for [ifconfig].
 * @since 1.0.0
 * @property commandExecutor Execution platform for commands.
 */
internal data class IfconfigConfig(
    var commandExecutor: CommandExecutor = JavaProcessBuilder.instance,
)

/**
 * Output of `ifconfig` as seen on the command line, but split into a [Map].
 * @since 1.0.0
 * @property devices List of devices with the device name as key and the details as value.
 */
internal data class IfconfigOutput(val devices: Map<Device, Lines> = mapOf()) {

    /**
     * Returns only devices in status `active`.
     * @since 1.0.0
     * @return All devices with status `active`. Return object is a [Map] with device name as key and details as value.
     */
    fun findActive(): Map<Device, Lines> {
        return devices.filter { device -> device.value.any { line -> line == "status: active" }}
    }
}

/**
 * Returns the output of `ifconfig` without any parameters.
 * @since 1.0.0
 * @param config Configuration.
 * @return Command line output wrapped in [IfconfigOutput].
 */
internal fun ifconfig(config: IfconfigConfig.() -> Unit = { }): IfconfigOutput {
    val currentConfig = IfconfigConfig().apply(config)
    val output = currentConfig.commandExecutor.executeCmd(listOf("ifconfig"))

    return parseOutput(output)
}

/**
 * Either returns the details for a specific device or applies [options] like `up` and `down` on the respective device.
 * @since 1.0.0
 * @param device The device to retrieve or on which you want to apply the [options].
 * @param options Additional options like `up` and `down`.
 * @param config Configuration.
 * @return Command line output wrapped in [IfconfigOutput].
 */
internal fun ifconfig(device: Device, vararg options: String, config: IfconfigConfig.() -> Unit = { }): IfconfigOutput {
    val currentConfig = IfconfigConfig().apply(config)
    val commandBuilder = mutableListOf<String>()

    if (options.contains("up") || options.contains("down")) {
        commandBuilder.add("sudo")
    }
    commandBuilder.add("ifconfig")
    commandBuilder.add(device)
    commandBuilder.addAll(options)

    val output = currentConfig.commandExecutor.executeCmd(commandBuilder)

    if (output.equals("Password:", ignoreCase = true)) {
        return ifconfig(config)
    }

    return parseOutput(output)
}

private fun parseOutput(output: String): IfconfigOutput {
    val parsedOutput = mutableMapOf<Device, Lines>()
    var currentDevice = EMPTY
    val currentLines = mutableListOf<String>()

    output.lines().forEach { line ->
        when {
            !line.startsWith("\t") && !line.startsWith(" ") -> {
                if (currentDevice.neitherNullNorBlank() && currentLines.isNotEmpty()) {
                    parsedOutput[currentDevice] = currentLines.toList()
                    currentLines.clear()
                }

                currentDevice = Regex("[aA-zZ]+[0-9]+:").find(line)?.value?.trimEnd(':') ?: throw IllegalStateException("Error: Unexpected output.")
                currentLines.add(line.substringAfter(':').trim())
            }
            else -> currentLines.add(line.trim())
        }
    }

    parsedOutput[currentDevice] = currentLines.toList()

    return IfconfigOutput(devices = parsedOutput)
}