package io.github.manamiproject.modb.serde.json.models

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

internal class LicenseTest {

    @Test
    fun `default values`() {
        // when
        val result = License()

        // then
        assertThat(result.name).isEqualTo("Open Data Commons Open Database License (ODbL) v1.0 + Database Contents License (DbCL) v1.0")
        assertThat(result.url).isEqualTo("https://github.com/manami-project/anime-offline-database/blob/master/LICENSE")
    }
}