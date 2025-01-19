package io.github.manamiproject.modb.simkl

import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class SimklConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = SimklConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = SimklConfig.hostname()

        // then
        assertThat(result).isEqualTo("simkl.com")
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "1535"

        // when
        val result = SimklConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://simkl.com/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "1535"

        // when
        val result = SimklConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://simkl.com/anime/$id"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = SimklConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("html")
    }
}