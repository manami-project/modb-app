package io.github.manamiproject.modb.kitsu

import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class KitsuConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = KitsuConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = KitsuConfig.hostname()

        // then
        assertThat(result).isEqualTo("kitsu.app")
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "1376"

        // when
        val result = KitsuConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://kitsu.app/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // given
        val id = "1535"

        // when
        val result = KitsuConfig.buildDataDownloadLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://kitsu.app/api/edge/anime?fields[categories]=title&fields[genres]=name&fields[producers]=name&filter[id]=${id}&include=categories,genres,animeProductions.producer,mediaRelationships.destination"))
    }

    @Test
    fun `file suffix must be json`() {
        // when
        val result = KitsuConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("json")
    }
}
