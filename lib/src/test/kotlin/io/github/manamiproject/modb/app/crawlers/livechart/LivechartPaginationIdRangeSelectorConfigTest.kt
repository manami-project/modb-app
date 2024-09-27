package io.github.manamiproject.modb.app.crawlers.livechart

import io.github.manamiproject.modb.livechart.LivechartConfig
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class LivechartPaginationIdRangeSelectorConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = LivechartPaginationIdRangeSelectorConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = LivechartPaginationIdRangeSelectorConfig.hostname()

        // then
        assertThat(result).isEqualTo(LivechartConfig.hostname())
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "1535"

        // when
        val result = LivechartPaginationIdRangeSelectorConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${LivechartPaginationIdRangeSelectorConfig.hostname()}/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "1535"

        // when
        val result = LivechartPaginationIdRangeSelectorConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${LivechartPaginationIdRangeSelectorConfig.hostname()}/1535/all"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = LivechartPaginationIdRangeSelectorConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("html")
    }
}