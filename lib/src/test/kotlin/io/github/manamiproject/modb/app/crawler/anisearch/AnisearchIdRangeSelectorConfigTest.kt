package io.github.manamiproject.modb.app.crawler.anisearch

import io.github.manamiproject.modb.anisearch.AnisearchConfig
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class AnisearchIdRangeSelectorConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = AnisearchIdRangeSelectorConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = AnisearchIdRangeSelectorConfig.hostname()

        // then
        assertThat(result).isEqualTo(AnisearchConfig.hostname())
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "1535"

        // when
        val result = AnisearchIdRangeSelectorConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${AnisearchConfig.hostname()}/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "1535"

        // when
        val result = AnisearchIdRangeSelectorConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${AnisearchConfig.hostname()}/anime/index/page-1535?char=all&sort=title&order=asc&view=2&limit=100&title=&titlex="))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = AnisearchIdRangeSelectorConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("html")
    }
}