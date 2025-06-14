package io.github.manamiproject.modb.core.io

import java.io.Closeable

/**
 * Addition to [Closeable] which lets the caller keep track of the lifecycle. Not only allowing to close, but also to
 * check if the implementation has been closed already.
 * @since 19.0.0
 */
public interface LifecycleAwareCloseable: Closeable {

    /**
     * Lets the caller check if [close] has already been called.
     * @since 19.0.0
     * @return `true` if the resource has already been closed.
     */
    public fun isClosed(): Boolean

    /**
     * Lets the caller check if [close] has already been called.
     * @since 19.0.0
     * @return `false` if the resource has already been closed.
     */
    public fun isNotClosed(): Boolean
}

