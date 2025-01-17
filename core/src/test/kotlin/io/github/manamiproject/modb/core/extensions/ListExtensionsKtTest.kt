package io.github.manamiproject.modb.core.extensions

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class ListExtensionsKtTest {

    @Nested
    inner class ShuffleTests {

        @Test
        fun `create a shuffled list`() {
            runBlocking {
                // given
                val sortedList = mutableListOf("A", "B", "C", "D")

                // when
                val result = sortedList.createShuffledList()

                // then
                assertThat(result).containsAll(sortedList)
                assertThat(result).doesNotContainSequence(sortedList)
            }
        }

        @Test
        fun `list having only one element`() {
            runBlocking {
                // given
                val sortedList = mutableListOf("A")

                // when
                val result = sortedList.createShuffledList()

                // then
                assertThat(result).containsExactly("A")
            }
        }

        @Test
        fun `empty list`() {
            runBlocking {
                // given
                val sortedList = emptyList<String>()

                // when
                val result = sortedList.createShuffledList()

                // then
                assertThat(result).isEmpty()
            }
        }
    }

    @Nested
    inner class ContainsExactlyInTheSameOrderTests {

        @Test
        fun `two lists with different size are not equal`() {
            // given
            val list = listOf("one")
            val otherList = listOf("one", "two")

            // when
            val result = list.containsExactlyInTheSameOrder(otherList)

            assertThat(result).isFalse()
        }

        @Test
        fun `two lists with the same element, but different order are not equal`() {
            // given
            val list = listOf("two", "one")
            val otherList = listOf("one", "two")

            // when
            val result = list.containsExactlyInTheSameOrder(otherList)

            assertThat(result).isFalse()
        }

        @Test
        fun `two lists with the same elements in the same order are equal`() {
            // given
            val list = listOf("one", "two")
            val otherList = listOf("one", "two")

            // when
            val result = list.containsExactlyInTheSameOrder(otherList)

            assertThat(result).isTrue()
        }
    }
}