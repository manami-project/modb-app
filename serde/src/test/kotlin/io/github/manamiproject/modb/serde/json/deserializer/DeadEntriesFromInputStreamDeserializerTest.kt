package io.github.manamiproject.modb.serde.json.deserializer

import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import io.github.manamiproject.modb.serde.TestReadOnceInputStream
import io.github.manamiproject.modb.serde.createExpectedDeadEntriesMinified
import io.github.manamiproject.modb.serde.createExpectedDeadEntriesPrettyPrint
import io.github.manamiproject.modb.test.exceptionExpected
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class DeadEntriesFromInputStreamDeserializerTest {

    @Nested
    inner class DeserializeLifecycleAwareInputStreamTests {

        @Test
        fun `throws exception if is closed`() {
            // given
            val deserializer = DeadEntriesFromInputStreamDeserializer()
            val inputStream = LifecycleAwareInputStream(TestReadOnceInputStream("test".byteInputStream()))
            inputStream.close()

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                deserializer.deserialize(inputStream)
            }

            // then
            assertThat(result).hasMessage("Stream must not be closed.")
        }

        @Test
        fun `return empty list of the json array is empty`() {
            runBlocking {
                // given
                val deserializer = DeadEntriesFromInputStreamDeserializer()
                val json = createExpectedDeadEntriesPrettyPrint()
                val inputStream = LifecycleAwareInputStream(TestReadOnceInputStream(json.byteInputStream()))

                // when
                val result = deserializer.deserialize(inputStream)

                // then
                assertThat(result.deadEntries).isEmpty()
            }
        }

        @Test
        fun `correctly deserialize list of String`() {
            runBlocking {
                // given
                val deserializer = DeadEntriesFromInputStreamDeserializer()
                val json = createExpectedDeadEntriesPrettyPrint(
                    """
                    "kj42fc5--",
                    "lkn6--k44",
                    "l2ht33--1",
                    "1kj5g--41",
                    "3jl253vv9"
                """.trimIndent()
                )
                val inputStream = LifecycleAwareInputStream(TestReadOnceInputStream(json.byteInputStream()))

                // when
                val result = deserializer.deserialize(inputStream)

                // then
                assertThat(result.deadEntries).containsExactlyInAnyOrder(
                    "kj42fc5--",
                    "lkn6--k44",
                    "l2ht33--1",
                    "1kj5g--41",
                    "3jl253vv9",
                )
            }
        }

        @Test
        fun `correctly deserialize minified dead entries file`() {
            runBlocking {
                // given
                val deserializer = DeadEntriesFromInputStreamDeserializer()
                val json =
                    createExpectedDeadEntriesMinified(""""kj42fc5--","lkn6--k44","l2ht33--1","1kj5g--41","3jl253vv9"""")
                val inputStream = LifecycleAwareInputStream(TestReadOnceInputStream(json.byteInputStream()))

                // when
                val result = deserializer.deserialize(inputStream)

                // then
                assertThat(result.deadEntries).containsExactlyInAnyOrder(
                    "kj42fc5--",
                    "lkn6--k44",
                    "l2ht33--1",
                    "1kj5g--41",
                    "3jl253vv9",
                )
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = DeadEntriesFromInputStreamDeserializer.instance

            // when
            val result = DeadEntriesFromInputStreamDeserializer.instance

            // then
            assertThat(result).isExactlyInstanceOf(DeadEntriesFromInputStreamDeserializer::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}