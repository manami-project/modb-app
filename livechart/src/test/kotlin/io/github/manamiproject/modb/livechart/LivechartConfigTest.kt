package io.github.manamiproject.modb.livechart

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import java.net.URI

internal class LivechartConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = LivechartConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = LivechartConfig.hostname()

        // then
        assertThat(result).isEqualTo("livechart.me")
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "1535"

        // when
        val result = LivechartConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://livechart.me/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "1535"

        // when
        val result = LivechartConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://livechart.me/anime/$id"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = LivechartConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("html")
    }
}