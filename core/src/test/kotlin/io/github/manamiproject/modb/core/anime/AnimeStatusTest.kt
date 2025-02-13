package io.github.manamiproject.modb.core.anime

import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test


internal class AnimeStatusTest {

    @Nested
    inner class OfTests {
        @ParameterizedTest
        @ValueSource(strings = ["FINISHED", "FiNiShEd", " FiNiShEd", "FiNiShEd "])
        fun `'FINISHED' by string`(value: String) {
            // when
            val result = AnimeStatus.of(value)

            // then
            assertThat(result).isEqualTo(FINISHED)
        }

        @ParameterizedTest
        @ValueSource(strings = ["ONGOING", "OnGoInG", " OnGoInG", "OnGoInG "])
        fun `'ONGOING' by string`(value: String) {
            // when
            val result = AnimeStatus.of(value)

            // then
            assertThat(result).isEqualTo(ONGOING)
        }

        @ParameterizedTest
        @ValueSource(strings = ["UPCOMING", "UpCoMiNg", " UpCoMiNg", "UpCoMiNg "])
        fun `'UPCOMING' by string`(value: String) {
            // when
            val result = AnimeStatus.of(value)

            // then
            assertThat(result).isEqualTo(UPCOMING)
        }

        @ParameterizedTest
        @ValueSource(strings = ["UNKNOWN", "UnKnOwN", " UnKnOwN", "UnKnOwN "])
        fun `'UNKNOWN' by string`(value: String) {
            // when
            val result = AnimeStatus.of(value)

            // then
            assertThat(result).isEqualTo(UNKNOWN)
        }

        @Test
        fun `'UNKNOWN' as failover for any non-matching string`() {
            // given
            val value = "non-matching-string"

            // when
            val result = AnimeStatus.of(value)

            // then
            assertThat(result).isEqualTo(UNKNOWN)
        }
    }
}