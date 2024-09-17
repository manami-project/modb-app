package io.github.manamiproject.modb.app.crawler.notify

import io.github.manamiproject.modb.notify.NotifyConfig
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class NotifyIdRangeSelectorConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = NotifyIdRangeSelectorConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = NotifyIdRangeSelectorConfig.hostname()

        // then
        assertThat(result).isEqualTo(NotifyConfig.hostname())
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "0-A-5Fimg"

        // when
        val result = NotifyIdRangeSelectorConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${NotifyIdRangeSelectorConfig.hostname()}/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "1535"

        // when
        val result = NotifyIdRangeSelectorConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${NotifyIdRangeSelectorConfig.hostname()}/explore/anime/any/any/any/1535"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = NotifyIdRangeSelectorConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("json")
    }
}