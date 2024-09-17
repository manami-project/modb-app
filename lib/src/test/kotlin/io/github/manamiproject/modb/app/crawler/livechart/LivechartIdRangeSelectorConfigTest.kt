package io.github.manamiproject.modb.app.crawler.livechart

import io.github.manamiproject.modb.livechart.LivechartConfig
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class LivechartIdRangeSelectorConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = LivechartIdRangeSelectorConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = LivechartIdRangeSelectorConfig.hostname()

        // then
        assertThat(result).isEqualTo(LivechartConfig.hostname())
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "1535"

        // when
        val result = LivechartIdRangeSelectorConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${LivechartIdRangeSelectorConfig.hostname()}/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "1535"

        // when
        val result = LivechartIdRangeSelectorConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${LivechartIdRangeSelectorConfig.hostname()}/1535/all"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = LivechartIdRangeSelectorConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("html")
    }
}