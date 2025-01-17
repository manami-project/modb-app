package io.github.manamiproject.modb.notify

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import java.net.URI

internal class NotifyRelationsConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = NotifyRelationsConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be equal to NotifyConfig`() {
        // when
        val result = NotifyRelationsConfig.hostname()

        // then
        assertThat(result).isEqualTo(NotifyConfig.hostname())
    }

    @Test
    fun `anime link is the same as for NotifyConfig`() {
        // given
        val id = "1376"

        // when
        val result = NotifyRelationsConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(NotifyConfig.buildAnimeLink(id))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "lkj452"

        // when
        val result = NotifyRelationsConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://notify.moe/api/animerelations/$id"))
    }

    @Test
    fun `file suffix must be json`() {
        // when
        val result = NotifyRelationsConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("json")
    }
}
