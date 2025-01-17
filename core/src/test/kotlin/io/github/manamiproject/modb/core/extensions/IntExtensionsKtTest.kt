package io.github.manamiproject.modb.core.extensions

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

internal class IntExtensionsKtTest {

    @Test
    fun `converts an Int to an AnimeId which is a typealias for String`() {
        // given
        val id = 1535

        // when
        val result = id.toAnimeId()

        // then
        assertThat(result).isEqualTo("1535")
    }
}