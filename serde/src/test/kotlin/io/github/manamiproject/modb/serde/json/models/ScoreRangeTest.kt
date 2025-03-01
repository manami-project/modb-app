package io.github.manamiproject.modb.serde.json.models

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

internal class ScoreRangeTest {

    @Test
    fun `default values`() {
        // when
        val result = ScoreRange()

        // then
        assertThat(result.minInclusive).isEqualTo(1.0)
        assertThat(result.maxInclusive).isEqualTo(10.0)
    }
}