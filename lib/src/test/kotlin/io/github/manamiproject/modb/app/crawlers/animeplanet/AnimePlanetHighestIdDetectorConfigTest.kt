package io.github.manamiproject.modb.app.crawlers.animeplanet

import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class AnimePlanetHighestIdDetectorConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = AnimePlanetHighestIdDetectorConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = AnimePlanetHighestIdDetectorConfig.hostname()

        // then
        assertThat(result).isEqualTo(AnimePlanetConfig.hostname())
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "black-clover"

        // when
        val result = AnimePlanetHighestIdDetectorConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${AnimePlanetHighestIdDetectorConfig.hostname()}/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "black-clover"

        // when
        val result = AnimePlanetHighestIdDetectorConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${AnimePlanetHighestIdDetectorConfig.hostname()}/anime/all"))
    }

    @Test
    fun `file suffix must be json`() {
        // when
        val result = AnimePlanetHighestIdDetectorConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("html")
    }
}