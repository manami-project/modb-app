package io.github.manamiproject.modb.core.config

import io.github.manamiproject.modb.test.shouldNotBeInvoked
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import java.net.URI
import kotlin.test.Test

internal class MetaDataProviderConfigKtTest {

    @Test
    fun `default anime uri link is hostname -slash- anime -slash- id`() {
        // given
        val testConfig = object: MetaDataProviderConfig {
            override fun hostname(): Hostname = "example.org"
            override fun fileSuffix(): FileSuffix = shouldNotBeInvoked()
        }

        val id = "4hf57"

        // when
        val result = testConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://example.org/anime/4hf57"))
    }

    @Test
    fun `buildDataDownloadLink creates the same uri as buildAnimeLink`() {
        // given
        val testConfig = object: MetaDataProviderConfig {
            override fun hostname(): Hostname = "example.org"
            override fun fileSuffix(): FileSuffix = shouldNotBeInvoked()
        }

        val id = "4hf57"

        // when
        val result = testConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(testConfig.buildAnimeLink(id))
    }

    @Nested
    inner class ExtractAnimeIdTests {

        @Test
        fun `throws exception if the uri does not contain the hostname of the config`() {
            // given
            val config = object: MetaDataProviderConfig {
                override fun hostname(): Hostname = "example.org"
                override fun buildAnimeLink(id: AnimeId): URI = URI("https://${hostname()}/some/other/path/$id")
                override fun fileSuffix(): FileSuffix = shouldNotBeInvoked()
            }

            // when
            val result = assertThrows<IllegalArgumentException> {
                config.extractAnimeId(URI("https://myanimelist.net/anime/1535"))
            }

            // then
            assertThat(result).hasMessage("URI doesn't contain hostname [example.org]")
        }

        @Test
        fun `correctly extracts anime id`() {
            // given
            val config = object: MetaDataProviderConfig {
                override fun hostname(): Hostname = "example.org"
                override fun buildAnimeLink(id: AnimeId): URI = URI("https://${hostname()}/some/other/path/$id")
                override fun fileSuffix(): FileSuffix = shouldNotBeInvoked()
            }

            val id = "3j4--21f"
            val uri = config.buildAnimeLink(id)

            // when
            val result = config.extractAnimeId(uri)

            // then
            assertThat(result).isEqualTo(id)
        }
    }
}