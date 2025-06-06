package io.github.manamiproject.modb.core.extensions

import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import java.io.InputStream

/**
 * Converts any type of [InputStream] to a [LifecycleAwareInputStream].
 * @since 19.0.0
 * @return Instance of [LifecycleAwareInputStream].
 * @receiver Any [InputStream]
 */
public fun InputStream.toLifecycleAwareInputStream(): LifecycleAwareInputStream = LifecycleAwareInputStream(this)