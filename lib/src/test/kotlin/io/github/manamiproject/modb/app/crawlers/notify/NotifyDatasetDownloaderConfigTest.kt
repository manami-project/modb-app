package io.github.manamiproject.modb.app.crawlers.notify

import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class NotifyDatasetDownloaderConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = NotifyAnimeDatasetDownloaderConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = NotifyAnimeDatasetDownloaderConfig.hostname()

        // then
        assertThat(result).isEqualTo("notify.moe")
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "0-A-5Fimg"

        // when
        val result = NotifyAnimeDatasetDownloaderConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${NotifyAnimeDatasetDownloaderConfig.hostname()}/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "1535"

        // when
        val result = NotifyAnimeDatasetDownloaderConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${NotifyAnimeDatasetDownloaderConfig.hostname()}/api/types/Anime/download"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = NotifyAnimeDatasetDownloaderConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("json")
    }
}