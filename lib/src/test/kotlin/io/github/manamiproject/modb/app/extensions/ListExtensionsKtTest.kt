package io.github.manamiproject.modb.app.extensions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
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
}