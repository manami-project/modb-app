package io.github.manamiproject.modb.app.crawler.anisearch

import io.github.manamiproject.modb.anisearch.AnisearchConfig
import io.github.manamiproject.modb.core.extensions.EMPTY
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class AnisearchLastPageDetectorConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = AnisearchLastPageDetectorConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = AnisearchLastPageDetectorConfig.hostname()

        // then
        assertThat(result).isEqualTo(AnisearchConfig.hostname())
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "1535"

        // when
        val result = AnisearchLastPageDetectorConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${AnisearchConfig.hostname()}/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // when
        val result = AnisearchLastPageDetectorConfig.buildDataDownloadLink(EMPTY)

        // then
        assertThat(result).isEqualTo(AnisearchIdRangeSelectorConfig.buildDataDownloadLink("1"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = AnisearchLastPageDetectorConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("html")
    }
}