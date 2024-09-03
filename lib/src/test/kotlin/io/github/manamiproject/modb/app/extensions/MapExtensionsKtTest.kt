package io.github.manamiproject.modb.app.extensions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class MapExtensionsKtTest {

    @Nested
    inner class FirstNotNullResultTests {

        @Test
        fun `return null if there is no element which can return a non-null result via lambda`() {
            // given
            val list = listOf("one", "two", "three", "four")
            val map = mapOf(
                "one" to null,
                "two" to null,
                "three" to null,
                "four" to null
            )

            // when
            val result: String? = map.firstNotNullResult(list)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `return null if the list is empty`() {
            // given
            val list = emptyList<String>()
            val map = mapOf(
                "one" to null,
                "two" to null,
                "three" to null,
                "four" to null
            )

            // when
            val result: String? = map.firstNotNullResult(list)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `returns the first non-null object`() {
            // given
            val list = listOf("one", "two", "three", "four")
            val map = mapOf(
                "four" to "other",
                "one" to null,
                "three" to "value",
                "two" to null
            )

            // when
            val result: String? = map.firstNotNullResult(list)

            // then
            assertThat(result).isEqualTo("value")
        }
    }
}