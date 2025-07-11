package io.github.manamiproject.modb.core.httpclient

import io.github.manamiproject.modb.core.config.ConfigRegistry
import io.github.manamiproject.modb.core.config.DefaultConfigRegistry
import io.github.manamiproject.modb.core.config.SetPropertyDelegate
import io.github.manamiproject.modb.core.extensions.pickRandom
import io.github.manamiproject.modb.core.httpclient.Browser.CHROME
import io.github.manamiproject.modb.core.httpclient.Browser.FIREFOX
import io.github.manamiproject.modb.core.httpclient.BrowserType.DESKTOP
import io.github.manamiproject.modb.core.httpclient.BrowserType.MOBILE
import java.net.URL

/**
 * Creates headers based on a [Browser] and the [BrowserType].
 *
 * **See also:**
 * + [https://www.whatismybrowser.com/guides/the-latest-user-agent/firefox](https://www.whatismybrowser.com/guides/the-latest-user-agent/firefox)
 * + [https://www.whatismybrowser.com/guides/the-latest-user-agent/chrome](https://www.whatismybrowser.com/guides/the-latest-user-agent/chrome)
 * @since 13.0.0
 * @param configRegistry Handles the retrieval of the value. **Default:** [DefaultConfigRegistry]
 */
public class DefaultHeaderCreator(
    configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
): HeaderCreator {

    private val firefoxDesktop: Set<String> by SetPropertyDelegate(
        namespace = NAMESPACE,
        default = defaultFirefoxDesktopUserAgents,
        configRegistry = configRegistry,
    )
    private val firefoxMobile: Set<String> by SetPropertyDelegate(
        namespace = NAMESPACE,
        default = defaultFirefoxMobileUserAgents,
        configRegistry = configRegistry,
    )
    private val chromeDesktop: Set<String> by SetPropertyDelegate(
        namespace = NAMESPACE,
        default = defaultChromeDesktopUserAgents,
        configRegistry = configRegistry,
    )
    private val chromeMobile: Set<String> by SetPropertyDelegate(
        namespace = NAMESPACE,
        default = defaultChromeMobileUserAgents,
        configRegistry = configRegistry,
    )

    override fun createHeadersFor(url: URL, browserType: BrowserType): Map<String, Collection<String>> = createHeadersFor(
        url = url,
        browser = Browser.random(),
        browserType = browserType,
    )

    override fun createHeadersFor(url: URL, browser: Browser, browserType: BrowserType): Map<String, Collection<String>> {
        val headers = mutableMapOf<String, Collection<String>>(
            "Host" to setOf(url.host),
            "Connection" to setOf("keep-alive"),
            "Upgrade-Insecure-Requests" to setOf("1"),
            "Pragma" to setOf("no-cache"),
            "Cache-Control" to setOf("no-cache"),
            "TE" to setOf("Trailers"),
        )

        headers.putAll(addBrowserUnspecificHeaders())
        headers.putAll(
            when(browser) {
                FIREFOX -> addFirefoxSpecificHeaders(browserType)
                CHROME -> addChromeSpecificHeaders(browserType)
            }
        )

        return headers.mapKeys { it.key.lowercase() }
    }

    private fun addBrowserUnspecificHeaders(): Map<String, Collection<String>> {
        return mapOf(
            "Accept" to setOf("*/*"),
            "Accept-Language" to setOf("en-US,en;q=0.8"),
        )
    }

    private fun addFirefoxSpecificHeaders(browserType: BrowserType): Map<String, Collection<String>> {
        val userAgent = when(browserType) {
            MOBILE -> firefoxMobile
            DESKTOP -> firefoxDesktop
        }.pickRandom()

        return mutableMapOf(
            "DNT" to setOf("1"),
            "User-Agent" to setOf(userAgent),
        )
    }

    private fun addChromeSpecificHeaders(browserType: BrowserType): Map<String, Collection<String>> {
        val userAgent = when(browserType) {
            MOBILE -> chromeMobile
            DESKTOP -> chromeDesktop
        }.pickRandom()

        return mutableMapOf(
            "User-Agent" to setOf(userAgent),
        )
    }

    public companion object {
        private const val NAMESPACE = "modb.core.httpclient.useragents"
        private val defaultFirefoxDesktopUserAgents = setOf(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 15.5; rv:140.0) Gecko/20100101 Firefox/140.0",
            "Mozilla/5.0 (X11; Linux i686; rv:140.0) Gecko/20100101 Firefox/140.0",
            "Mozilla/5.0 (X11; Linux x86_64; rv:140.0) Gecko/20100101 Firefox/140.0",
            "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:140.0) Gecko/20100101 Firefox/140.0",
            "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:140.0) Gecko/20100101 Firefox/140.0",
            "Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:140.0) Gecko/20100101 Firefox/140.0",
        )
        private val defaultFirefoxMobileUserAgents = setOf(
            "Mozilla/5.0 (iPhone; CPU iPhone OS 15_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) FxiOS/140.0 Mobile/15E148 Safari/605.1.15",
        )
        private val defaultChromeDesktopUserAgents = setOf(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36",
        )
        private val defaultChromeMobileUserAgents = setOf(
            "Mozilla/5.0 (iPhone; CPU iPhone OS 18_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/138.0.7204.119 Mobile/15E148 Safari/604.1",
        )

        /**
         * Singleton of [DefaultHeaderCreator]
         * @since 15.0.0
         */
        public val instance: DefaultHeaderCreator by lazy { DefaultHeaderCreator() }
    }
}