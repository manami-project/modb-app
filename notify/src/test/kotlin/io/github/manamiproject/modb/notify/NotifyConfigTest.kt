package io.github.manamiproject.modb.notify

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import java.net.URI

internal class NotifyConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = NotifyConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = NotifyConfig.hostname()

        // then
        assertThat(result).isEqualTo("notify.moe")
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "0-A-5Fimg"

        // when
        val result = NotifyConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${NotifyConfig.hostname()}/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "1535"

        // when
        val result = NotifyConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${NotifyConfig.hostname()}/api/anime/$id"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = NotifyConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("json")
    }
}
