package io.github.manamiproject.modb.app

import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

internal suspend fun waitFor(timeout: Duration, action: () -> Boolean) = withContext(Unconfined) {
    withTimeout(timeout) {
        while (!action.invoke() && isActive) {
            delay(100)
        }
    }
}