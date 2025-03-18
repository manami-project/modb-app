package io.github.manamiproject

import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class AnimenewsnetworkConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = AnimenewsnetworkConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = AnimenewsnetworkConfig.hostname()

        // then
        assertThat(result).isEqualTo("animenewsnetwork.com")
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "1535"

        // when
        val result = AnimenewsnetworkConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://animenewsnetwork.com/encyclopedia/anime.php?id=$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "1535"

        // when
        val result = AnimenewsnetworkConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://animenewsnetwork.com/encyclopedia/anime.php?id=$id"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = AnimenewsnetworkConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("html")
    }
}