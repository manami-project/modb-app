package io.github.manamiproject.modb.core.httpclient

import io.github.manamiproject.modb.core.TestConfigRegistry
import io.github.manamiproject.modb.core.config.ConfigRegistry
import io.github.manamiproject.modb.core.httpclient.Browser.CHROME
import io.github.manamiproject.modb.core.httpclient.Browser.FIREFOX
import io.github.manamiproject.modb.core.httpclient.BrowserType.DESKTOP
import io.github.manamiproject.modb.core.httpclient.BrowserType.MOBILE
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.Test

internal class DefaultHeaderCreatorTest {

    @Nested
    inner class CreateHeadersForUrlAndBrowserType {

        @Test
        fun `get header with default user agent for any browser of browser type DESKTOP`() {
            // given
            val url = URI("http://localhost:8080").toURL()
            val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                override fun <T : Any> list(key: String): List<T>? = null
            }
            val headerCreator = DefaultHeaderCreator(
                configRegistry = testConfigRegistry,
            )

            //when
            val result = headerCreator.createHeadersFor(url, DESKTOP)

            // then
            assertThat(result["user-agent"]).isIn(
                listOf(
                    setOf("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:130.0) Gecko/20100101 Firefox/130.0"),
                    setOf("Mozilla/5.0 (Macintosh; Intel Mac OS X 14.7; rv:130.0) Gecko/20100101 Firefox/130.0"),
                    setOf("Mozilla/5.0 (X11; Linux i686; rv:130.0) Gecko/20100101 Firefox/130.0"),
                    setOf("Mozilla/5.0 (X11; Linux x86_64; rv:130.0) Gecko/20100101 Firefox/130.0"),
                    setOf("Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:130.0) Gecko/20100101 Firefox/130.0"),
                    setOf("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:130.0) Gecko/20100101 Firefox/130.0"),
                    setOf("Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:130.0) Gecko/20100101 Firefox/130.0"),
                    setOf("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36"),
                    setOf("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36"),
                    setOf("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36"),
                )
            )
        }

        @Test
        fun `get header with default user agent for any browser of browser type MOBILE`() {
            // given
            val url = URI("http://localhost:8080").toURL()
            val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                override fun <T : Any> list(key: String): List<T>? = null
            }
            val headerCreator = DefaultHeaderCreator(
                configRegistry = testConfigRegistry,
            )

            //when
            val result = headerCreator.createHeadersFor(url, MOBILE)

            // then
            assertThat(result["user-agent"]).isIn(
                listOf(
                    setOf("Mozilla/5.0 (iPhone; CPU iPhone OS 14_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) FxiOS/130.0 Mobile/15E148 Safari/605.1.15"),
                    setOf("Mozilla/5.0 (iPhone; CPU iPhone OS 17_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/129.0.6668.46 Mobile/15E148 Safari/604.1"),
                )
            )
        }

        @Test
        @Suppress("UNCHECKED_CAST")
        fun `get header with user agent for any browser of browser type DESKTOP`() {
            // given
            val url = URI("http://localhost:8080").toURL()
            val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                override fun <T : Any> list(key: String): List<T> {
                    return listOf(
                        "agent-desktop1",
                        "agent-desktop2",
                        "agent-desktop3",
                        "agent-desktop4",
                    ) as List<T>
                }
            }
            val headerCreator = DefaultHeaderCreator(
                configRegistry = testConfigRegistry,
            )

            //when
            val result = headerCreator.createHeadersFor(url, DESKTOP)

            // then
            assertThat(result["user-agent"]).isIn(
                listOf(
                    setOf("agent-desktop1"),
                    setOf("agent-desktop2"),
                    setOf("agent-desktop3"),
                    setOf("agent-desktop4"),
                )
            )
        }

        @Test
        @Suppress("UNCHECKED_CAST")
        fun `get header with user agent for any browser of browser type MOBILE`() {
            // given
            val url = URI("http://localhost:8080").toURL()
            val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                override fun <T : Any> list(key: String): List<T> {
                    return listOf(
                        "agent-desktop1",
                        "agent-desktop2",
                        "agent-desktop3",
                        "agent-desktop4",
                    ) as List<T>
                }
            }
            val headerCreator = DefaultHeaderCreator(
                configRegistry = testConfigRegistry,
            )

            //when
            val result = headerCreator.createHeadersFor(url, MOBILE)

            // then
            assertThat(result["user-agent"]).isIn(
                listOf(
                    setOf("agent-desktop1"),
                    setOf("agent-desktop2"),
                    setOf("agent-desktop3"),
                    setOf("agent-desktop4"),
                )
            )
        }
    }

    @Nested
    inner class CreateHeadersForUrlAndBrowserAndBrowserType {

        @Test
        fun `get header with default user agent for firefox of browser type DESKTOP`() {
            // given
            val url = URI("http://localhost:8080").toURL()
            val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                override fun <T : Any> list(key: String): List<T>? = null
            }
            val headerCreator = DefaultHeaderCreator(
                configRegistry = testConfigRegistry,
            )

            //when
            val result = headerCreator.createHeadersFor(url, FIREFOX, DESKTOP)

            // then
            assertThat(result["host"]).isEqualTo(setOf("localhost"))
            assertThat(result["connection"]).isEqualTo(setOf("keep-alive"))
            assertThat(result["upgrade-insecure-requests"]).isEqualTo(setOf("1"))
            assertThat(result["accept-language"]).isEqualTo(setOf("en-US,en;q=0.8"))
            assertThat(result["accept"]).isEqualTo(setOf("*/*"))
            assertThat(result["dnt"]).isEqualTo(setOf("1"))
            assertThat(result["pragma"]).isEqualTo(setOf("no-cache"))
            assertThat(result["cache-control"]).isEqualTo(setOf("no-cache"))
            assertThat(result["te"]).isEqualTo(setOf("Trailers"))
            assertThat(result["user-agent"]).isIn(
                listOf(
                    setOf("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:130.0) Gecko/20100101 Firefox/130.0"),
                    setOf("Mozilla/5.0 (Macintosh; Intel Mac OS X 14.7; rv:130.0) Gecko/20100101 Firefox/130.0"),
                    setOf("Mozilla/5.0 (X11; Linux i686; rv:130.0) Gecko/20100101 Firefox/130.0"),
                    setOf("Mozilla/5.0 (X11; Linux x86_64; rv:130.0) Gecko/20100101 Firefox/130.0"),
                    setOf("Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:130.0) Gecko/20100101 Firefox/130.0"),
                    setOf("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:130.0) Gecko/20100101 Firefox/130.0"),
                    setOf("Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:130.0) Gecko/20100101 Firefox/130.0"),
                )
            )
        }

        @Test
        @Suppress("UNCHECKED_CAST")
        fun `get header with user agent for firefox of browser type DESKTOP`() {
            // given
            val url = URI("http://localhost:8080").toURL()
            val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                override fun <T : Any> list(key: String): List<T> {
                    return listOf(
                        "firefox-desktop1",
                        "firefox-desktop2",
                        "firefox-desktop3",
                    ) as List<T>
                }
            }
            val headerCreator = DefaultHeaderCreator(
                configRegistry = testConfigRegistry,
            )

            //when
            val result = headerCreator.createHeadersFor(url, FIREFOX, DESKTOP)

            // then
            assertThat(result["host"]).isEqualTo(setOf("localhost"))
            assertThat(result["connection"]).isEqualTo(setOf("keep-alive"))
            assertThat(result["upgrade-insecure-requests"]).isEqualTo(setOf("1"))
            assertThat(result["accept-language"]).isEqualTo(setOf("en-US,en;q=0.8"))
            assertThat(result["accept"]).isEqualTo(setOf("*/*"))
            assertThat(result["dnt"]).isEqualTo(setOf("1"))
            assertThat(result["pragma"]).isEqualTo(setOf("no-cache"))
            assertThat(result["cache-control"]).isEqualTo(setOf("no-cache"))
            assertThat(result["te"]).isEqualTo(setOf("Trailers"))
            assertThat(result["user-agent"]).isIn(
                listOf(
                    setOf("firefox-desktop1"),
                    setOf("firefox-desktop2"),
                    setOf("firefox-desktop3"),
                )
            )
        }

        @Test
        fun `get header with default user agent for firefox of browser type MOBILE`() {
            // given
            val url = URI("http://localhost:8080").toURL()
            val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                override fun <T : Any> list(key: String): List<T>? = null
            }
            val headerCreator = DefaultHeaderCreator(
                configRegistry = testConfigRegistry,
            )

            //when
            val result = headerCreator.createHeadersFor(url, FIREFOX, MOBILE)

            // then
            assertThat(result["host"]).isEqualTo(setOf("localhost"))
            assertThat(result["connection"]).isEqualTo(setOf("keep-alive"))
            assertThat(result["upgrade-insecure-requests"]).isEqualTo(setOf("1"))
            assertThat(result["accept-language"]).isEqualTo(setOf("en-US,en;q=0.8"))
            assertThat(result["accept"]).isEqualTo(setOf("*/*"))
            assertThat(result["dnt"]).isEqualTo(setOf("1"))
            assertThat(result["pragma"]).isEqualTo(setOf("no-cache"))
            assertThat(result["cache-control"]).isEqualTo(setOf("no-cache"))
            assertThat(result["te"]).isEqualTo(setOf("Trailers"))
            assertThat(result["user-agent"]).isIn(
                listOf(
                    setOf("Mozilla/5.0 (iPhone; CPU iPhone OS 14_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) FxiOS/130.0 Mobile/15E148 Safari/605.1.15"),
                )
            )
        }

        @Test
        @Suppress("UNCHECKED_CAST")
        fun `get header with user agent for firefox of browser type MOBILE`() {
            // given
            val url = URI("http://localhost:8080").toURL()
            val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                override fun <T : Any> list(key: String): List<T> {
                    return listOf(
                        "firefox-mobile1",
                        "firefox-mobile2",
                        "firefox-mobile3",
                    ) as List<T>
                }
            }
            val headerCreator = DefaultHeaderCreator(
                configRegistry = testConfigRegistry,
            )

            //when
            val result = headerCreator.createHeadersFor(url, FIREFOX, MOBILE)

            // then
            assertThat(result["host"]).isEqualTo(setOf("localhost"))
            assertThat(result["connection"]).isEqualTo(setOf("keep-alive"))
            assertThat(result["upgrade-insecure-requests"]).isEqualTo(setOf("1"))
            assertThat(result["accept-language"]).isEqualTo(setOf("en-US,en;q=0.8"))
            assertThat(result["accept"]).isEqualTo(setOf("*/*"))
            assertThat(result["dnt"]).isEqualTo(setOf("1"))
            assertThat(result["pragma"]).isEqualTo(setOf("no-cache"))
            assertThat(result["cache-control"]).isEqualTo(setOf("no-cache"))
            assertThat(result["te"]).isEqualTo(setOf("Trailers"))
            assertThat(result["user-agent"]).isIn(
                listOf(
                    setOf("firefox-mobile1"),
                    setOf("firefox-mobile2"),
                    setOf("firefox-mobile3"),
                )
            )
        }

        @Test
        fun `get header with default user agent for chrome of browser type DESKTOP`() {
            // given
            val url = URI("http://localhost:8080").toURL()
            val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                override fun <T : Any> list(key: String): List<T>? = null
            }
            val headerCreator = DefaultHeaderCreator(
                configRegistry = testConfigRegistry,
            )

            //when
            val result = headerCreator.createHeadersFor(url, CHROME, DESKTOP)

            // then
            assertThat(result["host"]).isEqualTo(setOf("localhost"))
            assertThat(result["connection"]).isEqualTo(setOf("keep-alive"))
            assertThat(result["upgrade-insecure-requests"]).isEqualTo(setOf("1"))
            assertThat(result["accept-language"]).isEqualTo(setOf("en-US,en;q=0.8"))
            assertThat(result["accept"]).isEqualTo(setOf("*/*"))
            assertThat(result["pragma"]).isEqualTo(setOf("no-cache"))
            assertThat(result["cache-control"]).isEqualTo(setOf("no-cache"))
            assertThat(result["te"]).isEqualTo(setOf("Trailers"))
            assertThat(result["user-agent"]).isIn(
                listOf(
                    setOf("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36"),
                    setOf("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36"),
                    setOf("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36"),
                )
            )
        }

        @Test
        @Suppress("UNCHECKED_CAST")
        fun `get header with user agent for chrome of browser type DESKTOP`() {
            // given
            val url = URI("http://localhost:8080").toURL()
            val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                override fun <T : Any> list(key: String): List<T> {
                    return listOf(
                        "chrome-desktop1",
                        "chrome-desktop2",
                        "chrome-desktop3",
                    ) as List<T>
                }
            }
            val headerCreator = DefaultHeaderCreator(
                configRegistry = testConfigRegistry,
            )

            //when
            val result = headerCreator.createHeadersFor(url, CHROME, DESKTOP)

            // then
            assertThat(result["host"]).isEqualTo(setOf("localhost"))
            assertThat(result["connection"]).isEqualTo(setOf("keep-alive"))
            assertThat(result["upgrade-insecure-requests"]).isEqualTo(setOf("1"))
            assertThat(result["accept-language"]).isEqualTo(setOf("en-US,en;q=0.8"))
            assertThat(result["accept"]).isEqualTo(setOf("*/*"))
            assertThat(result["pragma"]).isEqualTo(setOf("no-cache"))
            assertThat(result["cache-control"]).isEqualTo(setOf("no-cache"))
            assertThat(result["te"]).isEqualTo(setOf("Trailers"))
            assertThat(result["user-agent"]).isIn(
                listOf(
                    setOf("chrome-desktop1"),
                    setOf("chrome-desktop2"),
                    setOf("chrome-desktop3"),
                )
            )
        }

        @Test
        fun `get header with default user agent for chrome of browser type MOBILE`() {
            // given
            val url = URI("http://localhost:8080").toURL()
            val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                override fun <T : Any> list(key: String): List<T>? = null
            }
            val headerCreator = DefaultHeaderCreator(
                configRegistry = testConfigRegistry,
            )

            //when
            val result = headerCreator.createHeadersFor(url, CHROME, MOBILE)

            // then
            assertThat(result["host"]).isEqualTo(setOf("localhost"))
            assertThat(result["connection"]).isEqualTo(setOf("keep-alive"))
            assertThat(result["upgrade-insecure-requests"]).isEqualTo(setOf("1"))
            assertThat(result["accept-language"]).isEqualTo(setOf("en-US,en;q=0.8"))
            assertThat(result["accept"]).isEqualTo(setOf("*/*"))
            assertThat(result["pragma"]).isEqualTo(setOf("no-cache"))
            assertThat(result["cache-control"]).isEqualTo(setOf("no-cache"))
            assertThat(result["te"]).isEqualTo(setOf("Trailers"))
            assertThat(result["user-agent"]).isIn(
                listOf(
                    setOf("Mozilla/5.0 (iPhone; CPU iPhone OS 17_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/129.0.6668.46 Mobile/15E148 Safari/604.1"),
                )
            )
        }

        @Test
        @Suppress("UNCHECKED_CAST")
        fun `get header with user agent for chrome of browser type MOBILE`() {
            // given
            val url = URI("http://localhost:8080").toURL()
            val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                override fun <T : Any> list(key: String): List<T> {
                    return listOf(
                        "chrome-mobile1",
                        "chrome-mobile2",
                        "chrome-mobile3",
                    ) as List<T>
                }
            }
            val headerCreator = DefaultHeaderCreator(
                configRegistry = testConfigRegistry,
            )

            //when
            val result = headerCreator.createHeadersFor(url, CHROME, MOBILE)

            // then
            assertThat(result["host"]).isEqualTo(setOf("localhost"))
            assertThat(result["connection"]).isEqualTo(setOf("keep-alive"))
            assertThat(result["upgrade-insecure-requests"]).isEqualTo(setOf("1"))
            assertThat(result["accept-language"]).isEqualTo(setOf("en-US,en;q=0.8"))
            assertThat(result["accept"]).isEqualTo(setOf("*/*"))
            assertThat(result["pragma"]).isEqualTo(setOf("no-cache"))
            assertThat(result["cache-control"]).isEqualTo(setOf("no-cache"))
            assertThat(result["te"]).isEqualTo(setOf("Trailers"))
            assertThat(result["user-agent"]).isIn(
                listOf(
                    setOf("chrome-mobile1"),
                    setOf("chrome-mobile2"),
                    setOf("chrome-mobile3"),
                )
            )
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = DefaultHeaderCreator.instance

                // when
                val result = DefaultHeaderCreator.instance

                // then
                assertThat(result).isExactlyInstanceOf(DefaultHeaderCreator::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}