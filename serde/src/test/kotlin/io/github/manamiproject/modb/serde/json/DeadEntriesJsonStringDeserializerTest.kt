package io.github.manamiproject.modb.serde.json

import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.test.exceptionExpected
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class DeadEntriesJsonStringDeserializerTest {

    @Test
    fun `throws exception if the json string is empty`() {
        // given
        val deserializer = DeadEntriesJsonStringDeserializer()

        // when
        val result = exceptionExpected<IllegalArgumentException> {
            deserializer.deserialize(EMPTY)
        }

        // then
        assertThat(result).hasMessage("Given JSON string must not be blank.")
    }

    @Test
    fun `throws exception if the json string is blank`() {
        // given
        val deserializer = DeadEntriesJsonStringDeserializer()

        // when
        val result = exceptionExpected<IllegalArgumentException> {
            deserializer.deserialize("    ")
        }

        // then
        assertThat(result).hasMessage("Given JSON string must not be blank.")
    }

    @Test
    fun `return empty list of the json array is empty`() {
        runBlocking {
            // given
            val deserializer = DeadEntriesJsonStringDeserializer()
            val json = """
                {
                  "license": {
                    "name": "GNU Affero General Public License v3.0",
                    "url": "https://github.com/manami-project/anime-offline-database/blob/master/LICENSE"
                  },
                  "repository": "https://github.com/manami-project/anime-offline-database",
                  "lastUpdate": "2020-01-01",
                  "deadEntries": []
                }
            """.trimIndent()

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
            val deserializer = DeadEntriesJsonStringDeserializer()
            val json = """
                {
                  "license": {
                    "name": "GNU Affero General Public License v3.0",
                    "url": "https://github.com/manami-project/anime-offline-database/blob/master/LICENSE"
                  },
                  "repository": "https://github.com/manami-project/anime-offline-database",
                  "lastUpdate": "2020-01-01",
                  "deadEntries": [
                    "kj42fc5--",
                    "lkn6--k44",
                    "l2ht33--1",
                    "1kj5g--41",
                    "3jl253vv9"
                  ]
                }
            """.trimIndent()

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

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = DeadEntriesJsonStringDeserializer.instance

            // when
            val result = DeadEntriesJsonStringDeserializer.instance

            // then
            assertThat(result).isExactlyInstanceOf(DeadEntriesJsonStringDeserializer::class.java)
            assertThat(result===previous).isTrue()
        }
    }
}