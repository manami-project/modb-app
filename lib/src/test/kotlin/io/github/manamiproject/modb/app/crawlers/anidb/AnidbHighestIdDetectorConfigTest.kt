package io.github.manamiproject.modb.app.crawlers.anidb

import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.core.extensions.EMPTY
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class AnidbHighestIdDetectorConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = AnidbHighestIdDetectorConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = AnidbHighestIdDetectorConfig.hostname()

        // then
        assertThat(result).isEqualTo(AnidbConfig.hostname())
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "4563"

        // when
        val result = AnidbHighestIdDetectorConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${AnidbHighestIdDetectorConfig.hostname()}/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // when
        val result = AnidbHighestIdDetectorConfig.buildDataDownloadLink(EMPTY)

        // then
        assertThat(result).isEqualTo(URI("https://${AnidbHighestIdDetectorConfig.hostname()}/latest/anime"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = AnidbHighestIdDetectorConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("html")
    }
}