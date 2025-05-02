package io.github.manamiproject.modb.app.crawlers.animenewsnetwork

import io.github.manamiproject.modb.app.crawlers.animenewsnetwork.AnimenewsnetworkPaginationIdRangeSelectorConfig.hostname
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class AnimenewsnetworkPaginationIdRangeSelectorConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = AnimenewsnetworkPaginationIdRangeSelectorConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = AnimenewsnetworkPaginationIdRangeSelectorConfig.hostname()

        // then
        assertThat(result).isEqualTo("animenewsnetwork.com")
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "1535"

        // when
        val result = AnimenewsnetworkPaginationIdRangeSelectorConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://animenewsnetwork.com/encyclopedia/anime.php?id=$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "1535"

        // when
        val result = AnimenewsnetworkPaginationIdRangeSelectorConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${hostname()}/encyclopedia/anime.php?list=$id"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = AnimenewsnetworkPaginationIdRangeSelectorConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("html")
    }
}