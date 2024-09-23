package io.github.manamiproject.modb.app.crawler.kitsu

import io.github.manamiproject.modb.kitsu.KitsuConfig
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class KitsuHighestIdDetectorConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = KitsuHighestIdDetectorConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = KitsuHighestIdDetectorConfig.hostname()

        // then
        assertThat(result).isEqualTo(KitsuConfig.hostname())
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "1376"

        // when
        val result = KitsuHighestIdDetectorConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${KitsuHighestIdDetectorConfig.hostname()}/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "1535"

        // when
        val result = KitsuHighestIdDetectorConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${KitsuHighestIdDetectorConfig.hostname()}/api/edge/anime?fields[anime]=slug,canonicalTitle,titles,posterImage,description,averageRating,startDate,popularityRank,ratingRank,youtubeVideoId&page[offset]=0&page[limit]=20&sort=-created_at"))
    }

    @Test
    fun `file suffix must be json`() {
        // when
        val result = KitsuHighestIdDetectorConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("json")
    }
}