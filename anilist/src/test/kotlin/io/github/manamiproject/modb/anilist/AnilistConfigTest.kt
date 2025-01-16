package io.github.manamiproject.modb.anilist

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import java.net.URI

internal class AnilistConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = AnilistConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = AnilistConfig.hostname()

        // then
        assertThat(result).isEqualTo("anilist.co")
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "1535"

        // when
        val result = AnilistConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://anilist.co/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "1535"

        // when
        val result = AnilistConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://anilist.co/graphql"))
    }

    @Test
    fun `file suffix must be json`() {
        // when
        val result = AnilistConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("json")
    }
}