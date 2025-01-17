package io.github.manamiproject.modb.kitsu

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import java.net.URI

internal class KitsuTagsConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = KitsuTagsConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be equal to KitsuConfig`() {
        // when
        val result = KitsuTagsConfig.hostname()

        // then
        assertThat(result).isEqualTo(KitsuConfig.hostname())
    }

    @Test
    fun `anime link is the same as for KitsuConfig`() {
        // given
        val id = "1376"

        // when
        val result = KitsuTagsConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(KitsuConfig.buildAnimeLink(id))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "1376"

        // when
        val result = KitsuTagsConfig.buildDataDownloadLink(id)

        // then
        assertThat(result)
            .isEqualTo(URI("https://kitsu.app/api/edge/anime/$id/categories"))
    }

    @Test
    fun `file suffix must be json`() {
        // when
        val result = KitsuTagsConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("json")
    }
}