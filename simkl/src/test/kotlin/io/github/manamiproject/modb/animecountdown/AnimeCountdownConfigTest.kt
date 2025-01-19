package io.github.manamiproject.modb.animecountdown

import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class AnimeCountdownConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = AnimeCountdownConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = AnimeCountdownConfig.hostname()

        // then
        assertThat(result).isEqualTo("animecountdown.com")
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "1535"

        // when
        val result = AnimeCountdownConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://animecountdown.com/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "1535"

        // when
        val result = AnimeCountdownConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://animecountdown.com/$id"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = AnimeCountdownConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("html")
    }
}