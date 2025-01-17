package io.github.manamiproject.modb.core.config

/**
 * Adds awareness of the context the code is running in. Either a test or a production context.
 * @since 14.0.0
 */
public interface ContextAware {

    /**
     * Distinguish between a test context and production context.
     * @since 14.0.0
     * @return **true** if the current context is a test context. This is the case for unit tests for example.
     * @see io.github.manamiproject.modb.core.excludeFromTestContext
     */
    public fun isTestContext(): Boolean = false
}