package io.github.manamiproject.modb.anisearch

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import java.net.URI

internal class AnisearchRelationsConfigTest {
    
    @Test
    fun `isTestContext is false`() {
        // when
        val result = AnisearchRelationsConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be equal to AnisearchConfig`() {
        // when
        val result = AnisearchRelationsConfig.hostname()

        // then
        assertThat(result).isEqualTo(AnisearchConfig.hostname())
    }

    @Test
    fun `anime link URL is the same as for AnisearchConfig`() {
        // given
        val id = "1376"

        // when
        val result = AnisearchRelationsConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(AnisearchConfig.buildAnimeLink(id))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "1376"

        // when
        val result = AnisearchRelationsConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://anisearch.com/anime/1376/relations"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = AnisearchRelationsConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("html")
    }
}