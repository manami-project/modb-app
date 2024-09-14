package io.github.manamiproject.modb.app.network

import kotlinx.coroutines.Deferred

/**
 * Allows interaction with the system's network controller.
 * @since 1.0.0
 */
interface NetworkController {

    /**
     * Restart the network controller.
     * @since 1.0.0
     * @return Coroutine job of a [Boolean] which indicates that the restart was performed successfully by returning `true`.
     */
    suspend fun restartAsync(): Deferred<Boolean>

    /**
     * Checks if the network controller is active.
     * @since 1.0.0
     * @return `true` if the network controller is active.
     */
    fun isNetworkActive(): Boolean
}