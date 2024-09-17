package io.github.manamiproject.modb.app.crawler.animeplanet

import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class AnimePlanetIdRangeSelectorConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = AnimePlanetIdRangeSelectorConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = AnimePlanetIdRangeSelectorConfig.hostname()

        // then
        assertThat(result).isEqualTo(AnimePlanetConfig.hostname())
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "black-clover"

        // when
        val result = AnimePlanetIdRangeSelectorConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${AnimePlanetIdRangeSelectorConfig.hostname()}/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "black-clover"

        // when
        val result = AnimePlanetIdRangeSelectorConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${AnimePlanetIdRangeSelectorConfig.hostname()}/anime/all?sort=title&order=asc&page=black-clover&bvm=list"))
    }

    @Test
    fun `file suffix must be json`() {
        // when
        val result = AnimePlanetIdRangeSelectorConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("html")
    }
}