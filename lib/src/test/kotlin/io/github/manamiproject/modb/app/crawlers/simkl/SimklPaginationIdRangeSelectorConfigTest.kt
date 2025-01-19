package io.github.manamiproject.modb.app.crawlers.simkl

import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.simkl.SimklConfig
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal object SimklPaginationIdRangeSelectorConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = SimklPaginationIdRangeSelectorConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = SimklPaginationIdRangeSelectorConfig.hostname()

        // then
        assertThat(result).isEqualTo(SimklConfig.hostname())
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "1535"

        // when
        val result = SimklPaginationIdRangeSelectorConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${SimklConfig.hostname()}/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // when
        val result = SimklPaginationIdRangeSelectorConfig.buildDataDownloadLink(EMPTY)

        // then
        assertThat(result).isEqualTo(URI("https://${SimklConfig.hostname()}/ajax/full/anime.php"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = SimklPaginationIdRangeSelectorConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("html")
    }
}