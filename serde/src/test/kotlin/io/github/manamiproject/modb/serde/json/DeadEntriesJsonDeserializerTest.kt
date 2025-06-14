package io.github.manamiproject.modb.serde.json

import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import io.github.manamiproject.modb.serde.TestReadOnceInputStream
import io.github.manamiproject.modb.serde.createExpectedDeadEntriesMinified
import io.github.manamiproject.modb.serde.createExpectedDeadEntriesPrettyPrint
import io.github.manamiproject.modb.test.exceptionExpected
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

internal class DeadEntriesJsonDeserializerTest {

    @Nested
    inner class DeserializeStringTests {

        @ParameterizedTest
        @ValueSource(strings = [
            "",
            "   ",
            "\u00A0",
            "\u202F",
            "\u200A",
            "\u205F",
            "\u2000",
            "\u2001",
            "\u2002",
            "\u2003",
            "\u2004",
            "\u2005",
            "\u2006",
            "\u2007",
            "\u2008",
            "\u2009",
            "\uFEFF",
            "\u180E",
            "\u2060",
            "\u200D",
            "\u0090",
            "\u200C",
            "\u200B",
            "\u00AD",
            "\u000C",
            "\u2028",
            "\r",
            "\n",
            "\t",
        ])
        fun `throws exception if the json string is blank`(input: String) {
            // given
            val deserializer = DeadEntriesJsonDeserializer()

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                deserializer.deserialize(input)
            }

            // then
            assertThat(result).hasMessage("Given JSON string must not be blank.")
        }

        @Test
        fun `return empty list of the json array is empty`() {
            runBlocking {
                // given
                val deserializer = DeadEntriesJsonDeserializer()
                val json = createExpectedDeadEntriesPrettyPrint()

                // when
                val result = deserializer.deserialize(json)

                // then
                assertThat(result.deadEntries).isEmpty()
            }
        }

        @Test
        fun `correctly deserialize list of String`() {
            runBlocking {
                // given
                val deserializer = DeadEntriesJsonDeserializer()
                val json = createExpectedDeadEntriesPrettyPrint("""
                    "kj42fc5--",
                    "lkn6--k44",
                    "l2ht33--1",
                    "1kj5g--41",
                    "3jl253vv9"
                """.trimIndent())

                // when
                val result = deserializer.deserialize(json)

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
                val deserializer = DeadEntriesJsonDeserializer()
                val json = createExpectedDeadEntriesMinified(""""kj42fc5--","lkn6--k44","l2ht33--1","1kj5g--41","3jl253vv9"""")

                // when
                val result = deserializer.deserialize(json)

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
    inner class DeserializeLifecycleAwareInputStreamTests {

        @Test
        fun `throws exception if is closed`() {
            // given
            val deserializer = DeadEntriesJsonDeserializer()
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
                val deserializer = DeadEntriesJsonDeserializer()
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
                val deserializer = DeadEntriesJsonDeserializer()
                val json = createExpectedDeadEntriesPrettyPrint("""
                    "kj42fc5--",
                    "lkn6--k44",
                    "l2ht33--1",
                    "1kj5g--41",
                    "3jl253vv9"
                """.trimIndent())
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
                val deserializer = DeadEntriesJsonDeserializer()
                val json = createExpectedDeadEntriesMinified(""""kj42fc5--","lkn6--k44","l2ht33--1","1kj5g--41","3jl253vv9"""")
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
            val previous = DeadEntriesJsonDeserializer.instance

            // when
            val result = DeadEntriesJsonDeserializer.instance

            // then
            assertThat(result).isExactlyInstanceOf(DeadEntriesJsonDeserializer::class.java)
            assertThat(result===previous).isTrue()
        }
    }
}