package io.github.manamiproject.modb.core.anime

import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.*
import java.util.*

/**
 * @since 1.0.0
 */
public typealias Seconds = Int

/**
 * Duration of an anime.
 * @since 1.0.0
 * @property value The duration.
 * @property unit Indicates whether the [value] refers to seconds, minutes or hours.
 */
public data class Duration(
    private val value: Int,
    private val unit: TimeUnit,
) {
    /**
     * @since 1.0.0
     * @return Duration in seconds.
     */
    val duration: Seconds
        get() = when (unit) {
            SECONDS -> value
            MINUTES -> value * 60
            HOURS ->  value * 3600
        }

    /**
     * Returns the duration in seconds as formatted [String].
     *
     * **Example output:** `120 seconds`.
     * @since 1.0.0
     * @return Value in seconds followed by `seconds`.
     */
    override fun toString(): String = "$duration seconds"

    override fun equals(other: Any?): Boolean {
        if (other !is Duration) {
            return false
        }

        return duration == other.duration
    }

    override fun hashCode(): Int = Objects.hash(duration)

    /**
     * @since 1.0.0
     */
    public enum class TimeUnit {
        HOURS,
        MINUTES,
        SECONDS
    }

    public companion object {
        public val UNKNOWN: Duration = Duration(0, SECONDS)
    }
}