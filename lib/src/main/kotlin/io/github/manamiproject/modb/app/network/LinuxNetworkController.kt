package io.github.manamiproject.modb.app.network

import io.github.manamiproject.kommand.CommandExecutor
import io.github.manamiproject.kommand.JavaProcessBuilder
import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_NETWORK
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.excludeFromTestContext
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Seconds
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeoutException
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.toDuration

/**
 * Linux based network controller which allows to restart the network connection.
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property kotlinCommandExecutor Generic CLI command executor.
 * @property timeRangeForMaxRestarts Time range in seconds which in which the number of restarts defined by [maxNumberOfRestarts] is allowed.
 * @property maxNumberOfRestarts Maximum number of restarts than are allowed to occur within the time defined by [timeRangeForMaxRestarts].
 * @property timeout The actual restart is expected to be done within a given time. This property defines this time in seconds.
 * @throws TooManyRestartsException if the number of restarts within [timeRangeForMaxRestarts] exceeds [maxNumberOfRestarts].
 * @throws TimeoutException if the actual restart took longer to process than defined in [timeout].
 */
class LinuxNetworkController(
    private val appConfig: Config = AppConfig.instance,
    private val kotlinCommandExecutor: CommandExecutor = JavaProcessBuilder.instance,
    private val timeRangeForMaxRestarts: Seconds = 600,
    private val maxNumberOfRestarts: Int = timeRangeForMaxRestarts + timeRangeForMaxRestarts / 2,
    private val timeout: Seconds = 15,
): NetworkController {

    private val writeLock = Mutex()
    private var isNetworkActive = true
    private val restarts = mutableListOf<LocalDateTime>()

    /**
     * Sudo password required to either `up` or `down` the network controller.
     * @since 1.0.0
     */
    var sudoPasswordValue = EMPTY

    override suspend fun restartAsync(): Deferred<Boolean> = withContext(LIMITED_NETWORK) {
        return@withContext writeLock.withLock {
            if(!isRestartRequestValid()) {
                log.info {"Ignoring request to restart network, because the network is already restarting or has been restarted within the last minute." }
                return@withContext async { false }
            }

            isNetworkActive = false
            log.info { "Restart for network device has been triggered." }

            async {
                val device = identifyNetworkDevice()
                log.info { "Restarting internet connection by restarting network device [$device]." }
                changeDeviceStatus(device, DeviceStatus.INACTIVE)
                changeDeviceStatus(device, DeviceStatus.ACTIVE)

                isNetworkActive = true
                return@async true
            }
        }
    }

    override fun isNetworkActive(): Boolean = isNetworkActive

    private fun isRestartRequestValid(): Boolean {
        val now = LocalDateTime.now(appConfig.clock())

        if (restarts.isEmpty()) {
            restarts.add(now)
            return true
        }

        val differenceInSeconds = differenceInSeconds(restarts.first(), now)

        if (differenceInSeconds <= 30) {
            return false
        }

        if (restarts.size < maxNumberOfRestarts && differenceInSeconds < timeRangeForMaxRestarts) {
            restarts.add(now)
            return true
        }

        if (restarts.size < maxNumberOfRestarts && differenceInSeconds > timeRangeForMaxRestarts) {
            restarts.clear()
            restarts.add(now)
            return true
        }

        throw TooManyRestartsException(maxNumberOfRestarts, timeRangeForMaxRestarts)
    }

    private fun identifyNetworkDevice(): Device {
        log.info { "Trying to identify network device" }

        val devices = ifconfig {
            commandExecutor = kotlinCommandExecutor
        }.findActive()
        .filter { activeDevice -> activeDevice.value.any { it.contains("temporary") } }
        .keys

        check(devices.size == 1) { "Unable to find active network device." }
        return  devices.first()
    }

    private suspend fun changeDeviceStatus(device: Device, status: DeviceStatus) {
        log.info { "Setting status of device [$device] to [$status]" }

        val option = when(status) {
            DeviceStatus.ACTIVE -> "up"
            DeviceStatus.INACTIVE -> "down"
        }

        ifconfig(device, option) {
            commandExecutor = kotlinCommandExecutor.apply {
                config.useSudo = true
                config.sudoPassword = sudoPasswordValue
            }
        }

        kotlinCommandExecutor.apply {
            config.useSudo = false
            config.sudoPassword = EMPTY
        }

        waitForDeviceToBecome(device, status)
    }

    private fun differenceInSeconds(previous: LocalDateTime, recent: LocalDateTime): Long {
        var difference = 0L
        val seconds = previous.until(recent, ChronoUnit.SECONDS)
        difference += seconds

        return difference
    }

    private suspend fun waitForDeviceToBecome(device: Device, status: DeviceStatus) {
        log.info { "Waiting for device [$device] to become [$status]." }

        val deviceStatus: () -> Boolean = {
            ifconfig(device) {
                commandExecutor = kotlinCommandExecutor
            }.findActive().contains(device)
        }

        val isWaitCondition = when(status) {
            DeviceStatus.ACTIVE -> { isDeviceInListOfActiveDevices: Boolean -> !isDeviceInListOfActiveDevices }
            DeviceStatus.INACTIVE -> { isDeviceInListOfActiveDevices: Boolean -> isDeviceInListOfActiveDevices }
        }

        withTimeoutOrNull(timeout.toDuration(SECONDS)) {
            while (isWaitCondition(deviceStatus())) {
                wait()
            }
        } ?: throw TimeoutException("Timed out waiting waiting for device to change status.")
    }

    @KoverIgnore
    private suspend fun wait() {
        excludeFromTestContext(appConfig) {
            delay(2.toDuration(SECONDS))
        }
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [LinuxNetworkController]
         * @since 1.0.0
         */
        val instance: LinuxNetworkController by lazy { LinuxNetworkController() }
    }
}

private enum class DeviceStatus {
    ACTIVE,
    INACTIVE,
}