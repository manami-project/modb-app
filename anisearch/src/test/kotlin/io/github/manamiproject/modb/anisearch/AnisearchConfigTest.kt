package io.github.manamiproject.modb.anisearch

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import java.net.URI

internal class AnisearchConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = AnisearchConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = AnisearchConfig.hostname()

        // then
        assertThat(result).isEqualTo("anisearch.com")
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "1535"

        // when
        val result = AnisearchConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://anisearch.com/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "1535"

        // when
        val result = AnisearchConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://anisearch.com/anime/$id"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = AnisearchConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("html")
    }
}