package io.github.manamiproject.modb.app.crawlers.animeplanet

import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class AnimePlanetPaginationIdRangeSelectorConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = AnimePlanetPaginationIdRangeSelectorConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = AnimePlanetPaginationIdRangeSelectorConfig.hostname()

        // then
        assertThat(result).isEqualTo(AnimePlanetConfig.hostname())
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "black-clover"

        // when
        val result = AnimePlanetPaginationIdRangeSelectorConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${AnimePlanetPaginationIdRangeSelectorConfig.hostname()}/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "black-clover"

        // when
        val result = AnimePlanetPaginationIdRangeSelectorConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${AnimePlanetPaginationIdRangeSelectorConfig.hostname()}/anime/all?sort=title&order=asc&page=black-clover&bvm=list"))
    }

    @Test
    fun `file suffix must be json`() {
        // when
        val result = AnimePlanetPaginationIdRangeSelectorConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("html")
    }
}