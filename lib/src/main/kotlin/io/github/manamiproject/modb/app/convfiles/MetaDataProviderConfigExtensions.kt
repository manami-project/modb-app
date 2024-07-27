package io.github.manamiproject.modb.app.convfiles

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig

/**
 * Creates a hash code which can be used as an identifier to differentiate between [MetaDataProviderConfig]
 * implementations having the same [MetaDataProviderConfig.hostname].
 * @since 1.0.0
 * @receiver Any non-nullable [MetaDataProviderConfig].
 * @return The identity hash code as provided by [System.identityHashCode].
 */
internal fun MetaDataProviderConfig.identityHashCode() = System.identityHashCode(this).toString()