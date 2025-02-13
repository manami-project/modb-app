package io.github.manamiproject.modb.core.anime

import io.github.manamiproject.modb.core.anime.AnimeType.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

internal class AnimeTypeTest {

    @Nested
    inner class OfTests {

        @ParameterizedTest
        @ValueSource(strings = ["TV", "Tv", " Tv", "Tv "])
        fun `'TV' by string`(value: String) {
            // when
            val result = AnimeType.of(value)

            // then
            assertThat(result).isEqualTo(TV)
        }

        @ParameterizedTest
        @ValueSource(strings = ["MOVIE", "MoViE", " MoViE", "MoViE "])
        fun `'MOVIE' by string`(value: String) {
            // when
            val result = AnimeType.of(value)

            // then
            assertThat(result).isEqualTo(MOVIE)
        }

        @ParameterizedTest
        @ValueSource(strings = ["OVA", "OvA", " OvA", "OvA "])
        fun `'OVA' by string`(value: String) {
            // when
            val result = AnimeType.of(value)

            // then
            assertThat(result).isEqualTo(OVA)
        }

        @ParameterizedTest
        @ValueSource(strings = ["ONA", "OnA", " OnA", "OnA "])
        fun `'ONA' by string`(value: String) {
            // when
            val result = AnimeType.of(value)

            // then
            assertThat(result).isEqualTo(ONA)
        }

        @ParameterizedTest
        @ValueSource(strings = ["SPECIAL", "SpEcIaL", " SpEcIaL", "SpEcIaL "])
        fun `'SPECIAL' by string`(value: String) {
            // when
            val result = AnimeType.of(value)

            // then
            assertThat(result).isEqualTo(SPECIAL)
        }

        @ParameterizedTest
        @ValueSource(strings = ["UNKNOWN", "UnKnOwN", " UnKnOwN", "UnKnOwN "])
        fun `'UNKNOWN' by string`(value: String) {
            // when
            val result = AnimeType.of(value)

            // then
            assertThat(result).isEqualTo(UNKNOWN)
        }

        @Test
        fun `'UNKNOWN' as failover for any non-matching string`() {
            // given
            val value = "non-matching-string"

            // when
            val result = AnimeType.of(value)

            // then
            assertThat(result).isEqualTo(UNKNOWN)
        }
    }
}