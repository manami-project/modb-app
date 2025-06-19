package io.github.manamiproject.modb.serde.json.serializer

import io.github.manamiproject.modb.serde.createExpectedDeadEntriesMinified
import io.github.manamiproject.modb.serde.createExpectedDeadEntriesPrettyPrint
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test

internal class DeadEntriesJsonSerializerTest {

    @Nested
    inner class SerializeTests {

        @Test
        fun `correctly serialize minified`() {
            runBlocking {
                // given
                val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), ZoneOffset.UTC)
                val serializer = DeadEntriesJsonSerializer(clock = clock)

                val expected = createExpectedDeadEntriesMinified(
                    """
                    "1234","5678"
            """.trimIndent()
                )

                val list = setOf(
                    "1234",
                    "5678",
                )

                // when
                val result = serializer.serialize(list, minify = true)

                // then
                assertThat(result).isEqualTo(expected)
            }
        }

        @Test
        fun `correctly serialize pretty print`() {
            runBlocking {
                // given
                val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), ZoneOffset.UTC)
                val serializer = DeadEntriesJsonSerializer(clock = clock)

                val expected = createExpectedDeadEntriesPrettyPrint(
                    """
                "1234",
                "5678"
            """.trimIndent()
                )

                val list = setOf(
                    "1234",
                    "5678",
                )

                // when
                val result = serializer.serialize(list, minify = false)

                // then
                assertThat(result).isEqualTo(expected)
            }
        }

        @Test
        fun `results are sorted`() {
            runBlocking {
                // given
                val clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), ZoneOffset.UTC)
                val serializer = DeadEntriesJsonSerializer(clock = clock)

                val expected = createExpectedDeadEntriesPrettyPrint(
                    """
                "1234",
                "5678"
            """.trimIndent()
                )

                val list = setOf(
                    "5678",
                    "1234",
                )

                // when
                val result = serializer.serialize(list)

                // then
                assertThat(result).isEqualTo(expected)
            }
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
            assertThat(result === previous).isTrue()
        }
    }
}