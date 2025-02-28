package io.github.manamiproject.modb.app.crawlers.notify

import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class NotifyRelationsDatasetDownloaderConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = NotifyRelationsDatasetDownloaderConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = NotifyRelationsDatasetDownloaderConfig.hostname()

        // then
        assertThat(result).isEqualTo("notify.moe")
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "0-A-5Fimg"

        // when
        val result = NotifyRelationsDatasetDownloaderConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${NotifyRelationsDatasetDownloaderConfig.hostname()}/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "1535"

        // when
        val result = NotifyRelationsDatasetDownloaderConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${NotifyRelationsDatasetDownloaderConfig.hostname()}/api/types/AnimeRelations/download"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = NotifyRelationsDatasetDownloaderConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("json")
    }
}