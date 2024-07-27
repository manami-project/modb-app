package io.github.manamiproject.modb.app.convfiles

import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import io.github.manamiproject.modb.core.random
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.nio.file.ClosedWatchServiceException
import java.nio.file.WatchKey
import java.nio.file.WatchService
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration
import java.nio.file.WatchService as JavaWatchService

/**
 * Retrieve [WatchKey]s and prevent [ClosedWatchServiceException] to be thrown. Instead of throwing the exception `null` is being returned.
 * @since 1.0.0
 * @return Either a [WatchKey] or `null`.
 * @receiver Any [WatchService].
 */
suspend fun JavaWatchService.longPoll(): WatchKey? {
    val watchService = this
    return withContext(LIMITED_FS) {
        try {
            var key: WatchKey? = null
            var delay = random(100, 200).toDuration(MILLISECONDS)
            while (key == null && isActive) {
                if (delay < 500.toDuration(MILLISECONDS)) delay = delay.plus(100.toDuration(MILLISECONDS))
                delay(delay)
                key = watchService.take()
            }
            key
        } catch (e: ClosedWatchServiceException) {
            null
        }
    }
}