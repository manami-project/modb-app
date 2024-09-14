package io.github.manamiproject.modb.app.network

import io.github.manamiproject.modb.core.models.Seconds

/**
 * Exception which is thrown if too many restarts took place.
 * @since 1.0.0
 * @param maxNumberOfRestarts Maximum number of restarts which were allowed.
 * @param timeRangeForMaxRestarts Time range in seconds in which the maximum number of restarts were allowed.
 */
class TooManyRestartsException(
    maxNumberOfRestarts: Int,
    timeRangeForMaxRestarts: Seconds,
): RuntimeException("Triggered more than [$maxNumberOfRestarts] restarts within [$timeRangeForMaxRestarts] seconds.")