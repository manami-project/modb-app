package io.github.manamiproject.modb.core.extensions

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

internal class CollectionExtensionsKtTest {

    @Nested
    inner class PickRandomTests {

        @Test
        fun `throw an error if the list is empty and pickRandom() is called`() {
            // when
            val result = assertThrows<IllegalStateException> {
                emptyList<String>().pickRandom()
            }

            // then
            assertThat(result).hasMessage("Cannot pick random element from empty list.")
        }

        @Test
        fun `always return the first element if the list contains exactly one element`() {
            // given
            val list = listOf("one")

            // when
            val result = list.pickRandom()

            assertThat(result).isEqualTo(list.first())
        }

        @Test
        fun `pick random element`() {
            // given
            val list = (1..25).toList()

            // when
            val result = listOf(list.pickRandom(), list.pickRandom(), list.pickRandom(), list.pickRandom())

            // then
            assertThat(list).containsAll(result)

            val firstElementDiffers = result[0] != result[1] || result[0] != result[2] || result[0] != result[3]
            val secondElementDiffers = result[1] != result[2] || result[1] != result[3]
            val thirdElementDiffers = result[2] != result[3]
            val elementsAreNotAllTheSame = firstElementDiffers || secondElementDiffers || thirdElementDiffers
            assertThat(elementsAreNotAllTheSame).isTrue()
        }
    }

    @Nested
    inner class CreateShuffledListTests {

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
}