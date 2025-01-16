package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.extensions.EMPTY
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import org.junit.jupiter.api.assertThrows

internal class AnilistDefaultTokenRepositoryTest {

    @Test
    fun `successfully set token`() {
        // given
        val token = AnilistToken("my-cookie-value", "my-csrf-token-value")

        // when
        AnilistDefaultTokenRepository.token = token

        // then
        assertThat(AnilistDefaultTokenRepository.token).isEqualTo(token)
    }

    @Test
    fun `throws exception if cookie is blank`() {
        // when
        val result = assertThrows<IllegalArgumentException> {
            AnilistDefaultTokenRepository.token = AnilistToken("    ", "test")
        }

        // then
        assertThat(result).hasMessage("Cookie must not blank")
    }

    @Test
    fun `throws exception if cookie is empty`() {
        // when
        val result = assertThrows<IllegalArgumentException> {
            AnilistDefaultTokenRepository.token = AnilistToken(EMPTY, "test")
        }

        // then
        assertThat(result).hasMessage("Cookie must not blank")
    }

    @Test
    fun `throws exception if csrf token is blank`() {
        // when
        val result = assertThrows<IllegalArgumentException> {
            AnilistDefaultTokenRepository.token = AnilistToken("test", "    ")
        }

        // then
        assertThat(result).hasMessage("CSRF token must not blank")
    }

    @Test
    fun `throws exception if csrf token is empty`() {
        // when
        val result = assertThrows<IllegalArgumentException> {
            AnilistDefaultTokenRepository.token = AnilistToken("test", EMPTY)
        }

        // then
        assertThat(result).hasMessage("CSRF token must not blank")
    }
}