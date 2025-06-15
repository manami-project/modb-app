package io.github.manamiproject.modb.serde.json.models

import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.Test

internal class DatasetTest {

    @Test
    fun `default values`() {
        // when
        val result = Dataset(
            `$schema` = URI("https://example.org"),
            lastUpdate = "2020-01-01",
            data = emptyList(),
        )

        // then
        assertThat(result.license.name).isEqualTo("Open Data Commons Open Database License (ODbL) v1.0 + Database Contents License (DbCL) v1.0")
        assertThat(result.license.url).isEqualTo(URI("https://github.com/manami-project/anime-offline-database/blob/master/LICENSE"))
        assertThat(result.repository).isEqualTo("https://github.com/manami-project/anime-offline-database")
        assertThat(result.scoreRange.minInclusive).isEqualTo(1.0)
        assertThat(result.scoreRange.maxInclusive).isEqualTo(10.0)
    }
}