package io.github.manamiproject.modb.app.crawlers.myanimelist

import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class MyanimelistHighestIdDetectorConfigTest {

    @Test
    fun `isTestContext is false`() {
        // when
        val result = MyanimelistHighestIdDetectorConfig.isTestContext()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `hostname must be correct`() {
        // when
        val result = MyanimelistHighestIdDetectorConfig.hostname()

        // then
        assertThat(result).isEqualTo(MyanimelistConfig.hostname())
    }

    @Test
    fun `build anime link correctly`() {
        // given
        val id = "1535"

        // when
        val result = MyanimelistHighestIdDetectorConfig.buildAnimeLink(id)

        // then
        assertThat(result).isEqualTo(URI("https://${MyanimelistHighestIdDetectorConfig.hostname()}/anime/$id"))
    }

    @Test
    fun `build data download link correctly`() {
        // when
        val result = MyanimelistHighestIdDetectorConfig.buildDataDownloadLink(EMPTY)

        // then
        assertThat(result).isEqualTo(URI("https://${MyanimelistHighestIdDetectorConfig.hostname()}/anime.php?o=9&c%5B0%5D=a&c%5B1%5D=d&cv=2&w=1"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = MyanimelistConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("html")
    }
}