package io.github.manamiproject.modb.core.coroutines

import java.lang.Thread.NORM_PRIORITY
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * Creates threads with [NORM_PRIORITY] and the name set in [namePrefix] followed by the thread number.
 * @since 8.0.0
 * @param namePrefix Prefix set for each thread name.
 */
internal class DefaultThreadFactory(
    private val namePrefix: String,
) : ThreadFactory {

    private val group: ThreadGroup = Thread.currentThread().threadGroup
    private val threadNumber = AtomicInteger(1)

    override fun newThread(r: Runnable) = Thread(
        group,
        r,
        "$namePrefix-${threadNumber.getAndIncrement()}",
        0,
    ).apply {
        isDaemon = false
        priority = NORM_PRIORITY
    }
}