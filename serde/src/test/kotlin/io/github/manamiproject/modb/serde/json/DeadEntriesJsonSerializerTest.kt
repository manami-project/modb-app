package io.github.manamiproject.modb.serde.json

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset.UTC
import kotlin.test.Test

internal class DeadEntriesJsonSerializerTest {

    @Test
    fun `correctly serialize minified`() {
        runBlocking {
            // given
            val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
            val serializer = DeadEntriesJsonSerializer(clock = clock)
            val list = setOf(
                "1234",
                "5678",
            )

            // when
            val result = serializer.serialize(list, minify = true)

            // then
            assertThat(result).isEqualTo("""{"license":{"name":"GNU Affero General Public License v3.0","url":"https://github.com/manami-project/anime-offline-database/blob/master/LICENSE"},"repository":"https://github.com/manami-project/anime-offline-database","lastUpdate":"2020-01-01","deadEntries":["1234","5678"]}""".trimIndent())
        }
    }

    @Test
    fun `correctly serialize pretty print`() {
        runBlocking {
            // given
            val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
            val serializer = DeadEntriesJsonSerializer(clock = clock)
            val list = setOf(
                "1234",
                "5678",
            )

            // when
            val result = serializer.serialize(list, minify = false)

            // then
            assertThat(result).isEqualTo("""
                {
                  "license": {
                    "name": "GNU Affero General Public License v3.0",
                    "url": "https://github.com/manami-project/anime-offline-database/blob/master/LICENSE"
                  },
                  "repository": "https://github.com/manami-project/anime-offline-database",
                  "lastUpdate": "2020-01-01",
                  "deadEntries": [
                    "1234",
                    "5678"
                  ]
                }
            """.trimIndent())
        }
    }

    @Test
    fun `results are sorted`() {
        runBlocking {
            // given
            val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
            val serializer = DeadEntriesJsonSerializer(clock = clock)
            val list = setOf(
                "5678",
                "1234",
            )

            // when
            val result = serializer.serialize(list)

            // then
            assertThat(result).isEqualTo("""
                {
                  "license": {
                    "name": "GNU Affero General Public License v3.0",
                    "url": "https://github.com/manami-project/anime-offline-database/blob/master/LICENSE"
                  },
                  "repository": "https://github.com/manami-project/anime-offline-database",
                  "lastUpdate": "2020-01-01",
                  "deadEntries": [
                    "1234",
                    "5678"
                  ]
                }
            """.trimIndent())
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = DeadEntriesJsonSerializer.instance

            // when
            val result = DeadEntriesJsonSerializer.instance

            // then
            assertThat(result).isExactlyInstanceOf(DeadEntriesJsonSerializer::class.java)
            assertThat(result===previous).isTrue()
        }
    }
}