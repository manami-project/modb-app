package io.github.manamiproject.modb.app.crawler.livechart

import io.github.manamiproject.modb.livechart.LivechartConfig
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class LivechartNewestYearDetectorConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = LivechartNewestYearDetectorConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = LivechartNewestYearDetectorConfig.hostname()

        // then
        assertThat(result).isEqualTo(LivechartConfig.hostname())
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "1535"

        // when
        val result = LivechartNewestYearDetectorConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${LivechartNewestYearDetectorConfig.hostname()}/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "1535"

        // when
        val result = LivechartNewestYearDetectorConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${LivechartNewestYearDetectorConfig.hostname()}/charts"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = LivechartNewestYearDetectorConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("html")
    }
}