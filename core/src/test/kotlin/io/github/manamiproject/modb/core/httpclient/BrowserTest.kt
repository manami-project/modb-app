package io.github.manamiproject.modb.core.httpclient

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

internal class BrowserTest {

    @Test
    fun `picks a random browser`() {
        // given
        val values = mutableListOf<Browser>()

        // when
        repeat(10) {
            values.add(Browser.random())
        }

        // then
        assertThat(Browser.entries).containsAll(values)
    }
}