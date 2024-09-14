package io.github.manamiproject.modb.app.network

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class TooManyRestartsExceptionTest {

    @Nested
    inner class ConstructorTests {

        @Test
        fun `correctly constructs message`() {
            // given
            val exception = TooManyRestartsException(15, 600)

            // when
            val result = exception.message

            // then
            assertThat(result).isEqualTo("Triggered more than [15] restarts within [600] seconds.")
        }
    }
}