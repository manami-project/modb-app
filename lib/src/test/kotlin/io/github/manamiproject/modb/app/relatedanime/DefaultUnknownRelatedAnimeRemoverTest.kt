package io.github.manamiproject.modb.app.relatedanime

import io.github.manamiproject.modb.core.anime.AnimeRaw
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.Test

internal class DefaultUnknownRelatedAnimeRemoverTest {

    @Nested
    inner class RemoveRelatedAnimeWhichHaveNoCorrespondingEntryInSourcesTests {

        @Test
        fun `removes related anime listed which don't have a corresponding anime entry`() {
            // given
            val animeList = listOf(
                AnimeRaw(
                    _title = "1",
                    _sources = hashSetOf(
                        URI("http://config1.example.org/anime/1"),
                        URI("http://config2.example.org/anime/first-entry"),
                    ),
                    _relatedAnime = hashSetOf(
                        URI("http://config1.example.org/anime/2"),
                        URI("http://config1.example.org/anime/25"),
                        URI("http://config2.example.org/anime/second-entry"),
                        URI("http://config2.example.org/anime/non-existent-entry"),
                    )
                ),
                AnimeRaw(
                    _title = "2",
                    _sources = hashSetOf(
                        URI("http://config1.example.org/anime/2"),
                        URI("http://config2.example.org/anime/second-entry"),
                    ),
                    _relatedAnime = hashSetOf(
                        URI("http://config1.example.org/anime/1"),
                        URI("http://config1.example.org/anime/25"),
                        URI("http://config2.example.org/anime/first-entry"),
                    ),
                ),
                AnimeRaw(
                    _title = "3",
                    _sources = hashSetOf(
                        URI("http://config2.example.org/anime/third-entry"),
                    ),
                    _relatedAnime = hashSetOf(
                        URI("http://config2.example.org/anime/non-existent-entry"),
                    ),
                ),
            )

            val defaultUnknownRelatedAnimeRemover = DefaultUnknownRelatedAnimeRemover()

            // when
            val result = defaultUnknownRelatedAnimeRemover.removeRelatedAnimeWhichHaveNoCorrespondingEntryInSources(animeList)

            // then
            assertThat(result.find { it.title == "1" }!!.relatedAnime).containsExactlyInAnyOrder(
                URI("http://config1.example.org/anime/2"),
                URI("http://config2.example.org/anime/second-entry"),
            )
            assertThat(result.find { it.title == "2" }!!.relatedAnime).containsExactlyInAnyOrder(
                URI("http://config1.example.org/anime/1"),
                URI("http://config2.example.org/anime/first-entry"),
            )
            assertThat(result.find { it.title == "3" }!!.relatedAnime).isEmpty()
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = DefaultUnknownRelatedAnimeRemover.instance

                // when
                val result = DefaultUnknownRelatedAnimeRemover.instance

                // then
                assertThat(result).isExactlyInstanceOf(DefaultUnknownRelatedAnimeRemover::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}