package io.github.manamiproject.modb.anidb

import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class AnidbWebViewConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = AnidbWebViewConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = AnidbWebViewConfig.hostname()

        // then
        assertThat(result).isEqualTo("anidb.net")
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "4563"

        // when
        val result = AnidbWebViewConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://anidb.net/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "1535"

        // when
        val result = AnidbWebViewConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://anidb.net/anime/$id"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = AnidbWebViewConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("html")
    }
}