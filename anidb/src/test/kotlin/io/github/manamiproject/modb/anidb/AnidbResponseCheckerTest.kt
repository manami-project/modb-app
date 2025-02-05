package io.github.manamiproject.modb.anidb

import io.github.manamiproject.modb.core.config.ConfigRegistry
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.loadTestResource
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class AnidbResponseCheckerTest {

    @Nested
    inner class CrawlerDetectedTests {

        @Test
        fun `crawler is detected - anti-leech page - config suppresses opening browser`() {
            // given
            val responseBodyAntiLeech = loadTestResource<String>("AnidbDownloaderTest/anti-leech_page.html")

            val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun isTestContext(): Boolean = true
            }

            val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                override fun boolean(key: String): Boolean = false
            }

            val responseChecker = AnidbResponseChecker(
                response = responseBodyAntiLeech,
                configRegistry = testConfigRegistry,
                metaDataProviderConfig = testMetaDataProviderConfig,
            )

            // when
            val result = exceptionExpected<RuntimeException> {
                responseChecker.checkIfCrawlerIsDetected()
            }

            // then
            assertThat(result).hasMessage("Crawler has been detected")
        }

        @Test
        fun `crawler is detected - anti-leech page - config set to open browser`() {
            // given
            val responseBodyAntiLeech = loadTestResource<String>("AnidbDownloaderTest/anti-leech_page.html")

            val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun isTestContext(): Boolean = true
                override fun hostname(): Hostname = "example.org"
            }

            val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                override fun boolean(key: String): Boolean = true
            }

            val responseChecker = AnidbResponseChecker(
                response = responseBodyAntiLeech,
                configRegistry = testConfigRegistry,
                metaDataProviderConfig = testMetaDataProviderConfig,
            )

            // when
            val result = exceptionExpected<RuntimeException> {
                responseChecker.checkIfCrawlerIsDetected()
            }

            // then
            assertThat(result).hasMessage("Crawler has been detected")
        }

        @Test
        fun `crawler is detected - nginx error page - config suppresses opening browser`() {
            // given
            val responseBodyAntiLeech = loadTestResource<String>("AnidbDownloaderTest/nginx_error_page.html")

            val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun isTestContext(): Boolean = true
            }

            val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                override fun boolean(key: String): Boolean = false
            }

            val responseChecker = AnidbResponseChecker(
                response = responseBodyAntiLeech,
                configRegistry = testConfigRegistry,
                metaDataProviderConfig = testMetaDataProviderConfig,
            )

            // when
            val result = exceptionExpected<RuntimeException> {
                responseChecker.checkIfCrawlerIsDetected()
            }

            // then
            assertThat(result).hasMessage("Crawler has been detected")
        }

        @Test
        fun `crawler is detected - nginx error page - config set to open browser`() {
            // given
            val responseBodyAntiLeech = loadTestResource<String>("AnidbDownloaderTest/nginx_error_page.html")

            val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun isTestContext(): Boolean = true
                override fun hostname(): Hostname = "example.org"
            }

            val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                override fun boolean(key: String): Boolean = true
            }

            val responseChecker = AnidbResponseChecker(
                response = responseBodyAntiLeech,
                configRegistry = testConfigRegistry,
                metaDataProviderConfig = testMetaDataProviderConfig,
            )

            // when
            val result = exceptionExpected<RuntimeException> {
                responseChecker.checkIfCrawlerIsDetected()
            }

            // then
            assertThat(result).hasMessage("Crawler has been detected")
        }
    }

    @Nested
    inner class IsHentaiTests {

        @Test
        fun `isHentai returns false on regular entry`() {
            runBlocking {
                // given
                val responseBody = loadTestResource<String>("AnidbAnimeConverterTest/title/special_chars.html")

                val responseChecker = AnidbResponseChecker(responseBody)

                // when
                val result = responseChecker.isHentai()

                // then
                assertThat(result).isFalse()
            }
        }

        @Test
        fun `isHentai returns true`() {
            runBlocking {
                // given
                val responseBody = loadTestResource<String>("AnidbDownloaderTest/hentai.html")

                val responseChecker = AnidbResponseChecker(responseBody)

                // when
                val result = responseChecker.isHentai()

                // then
                assertThat(result).isTrue()
            }
        }
    }

    @Nested
    inner class IsAdditionPendingTests {

        @Test
        fun `isAdditionPending returns false on regular entry`() {
            runBlocking {
                // given
                val responseBody = loadTestResource<String>("AnidbAnimeConverterTest/title/special_chars.html")

                val responseChecker = AnidbResponseChecker(responseBody)

                // when
                val result = responseChecker.isAdditionPending()

                // then
                assertThat(result).isFalse()
            }
        }

        @Test
        fun `isAdditionPending returns true`() {
            runBlocking {
                // given
                val responseBody = loadTestResource<String>("AnidbDownloaderTest/addition_pending.html")

                val responseChecker = AnidbResponseChecker(responseBody)

                // when
                val result = responseChecker.isAdditionPending()

                // then
                assertThat(result).isTrue()
            }
        }
    }

    @Nested
    inner class IsRemovedFromAnidbTests {

        @Test
        fun `isRemovedFromAnidb returns false on regular entry`() {
            runBlocking {
                // given
                val responseBody = loadTestResource<String>("AnidbAnimeConverterTest/title/special_chars.html")

                val responseChecker = AnidbResponseChecker(responseBody)

                // when
                val result = responseChecker.isRemovedFromAnidb()

                // then
                assertThat(result).isFalse()
            }
        }

        @Test
        fun `isRemovedFromAnidb returns true`() {
            runBlocking {
                // given
                val responseBody = loadTestResource<String>("AnidbDownloaderTest/deleted_entry.html")

                val responseChecker = AnidbResponseChecker(responseBody)

                // when
                val result = responseChecker.isRemovedFromAnidb()

                // then
                assertThat(result).isTrue()
            }
        }
    }
}