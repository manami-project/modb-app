package io.github.manamiproject.modb.core.extensions

import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.io.InputStream
import kotlin.test.Test

internal class InputStreamExtensionsKtTest {

    @Nested
    inner class ToLifecycleAwareInputStream() {

        @Test
        fun `correctly converts an InputStream to a LifecycleAwareInputStream`() {
            // given
            val inputStream = InputStream.nullInputStream()

            // when
            val result = inputStream.toLifecycleAwareInputStream()

            // then
            assertThat(result).isInstanceOf(LifecycleAwareInputStream::class.java)
        }
    }
}