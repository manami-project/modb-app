package io.github.manamiproject.modb.core.httpclient

import io.github.manamiproject.modb.core.extensions.pickRandom

/**
 * Defines different browsers.
 * @since 4.0.0
 */
public enum class Browser {
    FIREFOX,
    CHROME;

    public companion object {
        public fun random(): Browser {
            return entries.pickRandom()
        }
    }
}

/**
 * Defines the visualization type of a [Browser].
 * @since 4.0.0
 */
public enum class BrowserType {
    MOBILE,
    DESKTOP;
}