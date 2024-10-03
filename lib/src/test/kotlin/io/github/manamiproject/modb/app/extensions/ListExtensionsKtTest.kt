package io.github.manamiproject.modb.app.extensions

import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI
import kotlin.test.Test

internal class ListExtensionsKtTest {

    @Nested
    inner class FindDuplicatesTests {

        @Test
        fun `correctly returns duplicates`() {
            // given
            val list = listOf(
                1,
                2,
                3,
                4,
                2,
                5,
                4,
            )

            // when
            val result = list.findDuplicates()

            // then
            assertThat(result).containsExactlyInAnyOrder(
                2,
                4,
            )
        }

        @Test
        fun `returns empty set if the list doesn't contain any duplicates`() {
            // given
            val list = listOf(
                1,
                2,
                3,
                4,
                5,
            )

            // when
            val result = list.findDuplicates()

            // then
            assertThat(result).isEmpty()
        }
    }

    @Nested
    inner class AlertDeletedAnimeByTitleTests {

        @ParameterizedTest
        @ValueSource(strings = ["delete", "deleted", "DELETE", "DELETED", "Delete", "Deleted", "dElEtE", "DeLeTeD"])
        fun `throws exception if marker are within title`(value: String) {
            // given
            val testList = listOf(
                Anime(
                    _title = value,
                    sources = hashSetOf(URI("https://example.org/anime/1")),
                ),
            )

            // when
            val result = exceptionExpected<IllegalStateException> {
                testList.alertDeletedAnimeByTitle()
            }

            // then
            assertThat(result).hasMessage("Probably found a dead entry: [title=${value}, source=https://example.org/anime/1]")
        }

        @Test
        fun `returns input list as-is if marker wasn't found`() {
            // given
            val testList = listOf(
                Anime(
                    _title = "entry",
                    sources = hashSetOf(URI("https://example.org/anime/1")),
                ),
                Anime(
                    _title = "other",
                    sources = hashSetOf(URI("https://example.org/anime/288")),
                ),
            )

            // when
            val result = testList.alertDeletedAnimeByTitle()

            // then
            assertThat(result).containsExactlyInAnyOrder(
                *testList.toTypedArray(),
            )
        }
    }
}