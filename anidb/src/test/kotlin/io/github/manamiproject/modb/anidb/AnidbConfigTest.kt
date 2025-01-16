package io.github.manamiproject.modb.anidb

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import java.net.URI

internal class AnidbConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = AnidbConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = AnidbConfig.hostname()

        // then
        assertThat(result).isEqualTo("anidb.net")
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "4563"

        // when
        val result = AnidbConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://anidb.net/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "1535"

        // when
        val result = AnidbConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://anidb.net/anime/$id"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = AnidbConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("html")
    }
}
