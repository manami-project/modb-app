package io.github.manamiproject.modb.animeplanet

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import java.net.URI

internal class AnimePlanetConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = AnimePlanetConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = AnimePlanetConfig.hostname()

        // then
        assertThat(result).isEqualTo("anime-planet.com")
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "black-clover"

        // when
        val result = AnimePlanetConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://anime-planet.com/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "black-clover"

        // when
        val result = AnimePlanetConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://anime-planet.com/anime/$id"))
    }

    @Test
    fun `file suffix must be json`() {
        // when
        val result = AnimePlanetConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("html")
    }
}