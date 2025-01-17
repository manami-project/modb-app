package io.github.manamiproject.modb.core.httpclient

import io.github.manamiproject.modb.core.httpclient.BrowserType.DESKTOP
import java.net.URL

/**
 * Creates headers based on a [Browser] and the [BrowserType].
 * @since 13.0.0
 */
public interface HeaderCreator {

    /**
     * Returns headers for a random [Browser].
     * @since 13.0.0
     * @param url The url to be called.
     * @param browserType Targeted browser type.
     */
    public fun createHeadersFor(
        url: URL,
        browserType: BrowserType = DESKTOP,
    ): Map<String, Collection<String>>

    /**
     * @since 13.0.0
     * @param url The url to be called.
     * @param browser Targeted browser.
     * @param browserType Targeted browser type.
     */
    public fun createHeadersFor(
        url: URL,
        browser: Browser,
        browserType: BrowserType = DESKTOP,
    ): Map<String, Collection<String>>
}
