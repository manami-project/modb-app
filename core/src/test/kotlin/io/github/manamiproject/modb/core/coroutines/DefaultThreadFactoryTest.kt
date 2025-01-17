package io.github.manamiproject.modb.core.coroutines

import org.assertj.core.api.Assertions.assertThat
import java.lang.Thread.NORM_PRIORITY
import kotlin.test.Test

internal class DefaultThreadFactoryTest {

    @Test
    fun `thread name is created correctly`() {
        // when
        val result = DefaultThreadFactory("custom-test-name").newThread {
            // nothing
        }.name

        // then
        assertThat(result).isEqualTo("custom-test-name-1")
    }

    @Test
    fun `thread priority is norm`() {
        // when
        val result = DefaultThreadFactory("custom-test-name").newThread {
            // nothing
        }.priority

        // then
        assertThat(result).isEqualTo(NORM_PRIORITY)
    }
}