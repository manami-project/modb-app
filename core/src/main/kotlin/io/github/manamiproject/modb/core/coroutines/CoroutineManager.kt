package io.github.manamiproject.modb.core.coroutines

import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_NETWORK
import io.github.manamiproject.modb.core.logging.Logger
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit.SECONDS

/**
 * Provides functionality for managing coroutines.
 * @since 8.0.0
 */
public object CoroutineManager {

    @PublishedApi
    internal val log: Logger by LoggerDelegate()

    /**
     * Wraps [runBlocking] and executes an [action] in a try-catch block. In case any [Throwable] is thrown it will be
     * logged and the dispatcher pools from [ModbDispatchers] will be shut down.
     *
     * It's recommended to only use this function at the highest level when starting coroutines from main thread.
     *
     * @since 8.0.0
     * @param isTestContext Set this to true in case you use this in unit tests. Otherwise, the tests will fail with a task rejected exception.
     * @param action Any function containing suspend function calls.
     */
    public inline fun <reified T> runCoroutine(isTestContext: Boolean = false, noinline action: suspend CoroutineScope.() -> T): T = runBlocking {
        try {
            action.invoke(this)
        } catch(e: Throwable) {
            log.error(e) { "An error occurred!" }
            throw e
        } finally {
            if (!isTestContext) {
                log.info { "Shutting down coroutine dispatchers." }
                setOf(LIMITED_NETWORK, LIMITED_CPU, LIMITED_FS).forEach {
                    if (!(it.asExecutor() as ExecutorService).isShutdown) {
                        log.debug { "Shutting down:\n$it" }
                        it.close()
                        (it.asExecutor() as ExecutorService).awaitTermination(10, SECONDS)
                    } else {
                        log.debug { "Doing nothing, because shutdown process has already been initiated or dispatcher is already terminated:\n$it" }
                    }
                }
            }
        }
    }
}