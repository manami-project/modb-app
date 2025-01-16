package io.github.manamiproject.modb.anilist

import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class AnilistDefaultTokenRetrieverConfigTest {

    @Test
    fun `hostname must be correct`() {
        // when
        val result = AnilistDefaultTokenRetrieverConfig.hostname()

        // then
        assertThat(result).isEqualTo("anilist.co")
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "4563"

        // when
        val result = AnilistDefaultTokenRetrieverConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://anilist.co/anime/4563"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val type = "music"

        // when
        val result = AnilistDefaultTokenRetrieverConfig.buildDataDownloadLink(type)

        // then
        assertThat(result).isEqualTo(URI("https://anilist.co"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = AnilistDefaultTokenRetrieverConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("json")
    }
}