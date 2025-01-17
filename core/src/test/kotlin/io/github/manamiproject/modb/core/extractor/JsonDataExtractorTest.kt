package io.github.manamiproject.modb.core.extractor

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

internal class JsonDataExtractorTest {

    @ParameterizedTest
    @ValueSource(strings = ["", "    "])
    fun `contains NotFound for the OutputKey if raw content is blank`(input: String) {
        runBlocking {
            // when
            val result = JsonDataExtractor.extract(input, mapOf("result" to "$.propertyName"))

            // then
            assertThat(result).containsEntry("result", NotFound)
        }
    }

    @Test
    fun `contains NotFound for the OutputKey if selector cannot be found`() {
        runBlocking {
            // given
            val raw = """
                {
                    "testKey": "testValue"
                }
            """.trimIndent()

            // when
            val result = JsonDataExtractor.extract(raw, mapOf("result" to "$.propertyName"))

            // then
            assertThat(result).containsEntry("result", NotFound)
        }
    }

    @Test
    fun `returns empty map if selector is empty`() {
        runBlocking {
            // given
            val raw = """
                {
                    "testKey": "testValue"
                }
            """.trimIndent()

            // when
            val result = JsonDataExtractor.extract(raw, emptyMap())

            // then
            assertThat(result).isEmpty()
        }
    }

    @Test
    fun `correctly extracts strings`() {
        runBlocking {
            // given
            val raw = """
                {
                    "string": "testValue"
                }
            """.trimIndent()

            // when
            val result = JsonDataExtractor.extract(raw, mapOf("result" to "$.string"))

            // then
            assertThat(result).containsEntry("result", "testValue")
        }
    }

    @Test
    fun `correctly extracts integer`() {
        runBlocking {
            // given
            val raw = """
                {
                    "integer": 5
                }
            """.trimIndent()

            // when
            val result = JsonDataExtractor.extract(raw, mapOf("result" to "$.integer"))

            // then
            assertThat(result).containsEntry("result", 5)
        }
    }

    @Test
    fun `correctly extracts double`() {
        runBlocking {
            // given
            val raw = """
                {
                    "double": 5.2
                }
            """.trimIndent()

            // when
            val result = JsonDataExtractor.extract(raw, mapOf("result" to "$.double"))

            // then
            assertThat(result).containsEntry("result", 5.2)
        }
    }

    @Test
    fun `correctly extracts array of strings`() {
        runBlocking {
            // given
            val raw = """
                {
                    "array_strings": [
                        "one",
                        "two"
                    ]
                }
            """.trimIndent()

            // when
            val result = JsonDataExtractor.extract(raw, mapOf("result" to "$.array_strings"))

            // then
            assertThat(result).containsEntry("result", listOf("one", "two"))
        }
    }

    @Test
    fun `correctly extracts array of integers`() {
        runBlocking {
            // given
            val raw = """
                {
                    "array_integers": [
                        2,
                        3,
                        5
                    ]
                }
            """.trimIndent()

            // when
            val result = JsonDataExtractor.extract(raw, mapOf("result" to "$.array_integers"))

            // then
            assertThat(result).containsEntry("result", listOf(2, 3, 5))
        }
    }

    @Test
    fun `correctly extracts objects`() {
        runBlocking {
            // given
            val raw = """
                {
                    "object": {
                        "inner_string": "otherValue",
                        "inner_integer": 7,
                        "inner_double": 4.3
                    }
                }
            """.trimIndent()

            // when
            val result = JsonDataExtractor.extract(raw, mapOf("result" to "$.object"))

            // then
            assertThat(result).containsEntry("result", mapOf(
                "inner_string" to "otherValue",
                "inner_integer" to 7,
                "inner_double" to 4.3,
            ))
        }
    }

    @Test
    fun `correctly extracts nested string`() {
        runBlocking {
            // given
            val raw = """
                {
                    "object": {
                        "inner_string": "otherValue",
                        "inner_integer": 7,
                        "inner_double": 4.3
                    }
                }
            """.trimIndent()

            // when
            val result = JsonDataExtractor.extract(raw, mapOf("result" to "$.object.inner_string"))

            // then
            assertThat(result).containsEntry("result", "otherValue")
        }
    }

    @Test
    fun `correctly extracts nested integer`() {
        runBlocking {
            // given
            val raw = """
                {
                    "object": {
                        "inner_string": "otherValue",
                        "inner_integer": 7,
                        "inner_double": 4.3
                    }
                }
            """.trimIndent()

            // when
            val result = JsonDataExtractor.extract(raw, mapOf("result" to "$.object.inner_integer"))

            // then
            assertThat(result).containsEntry("result", 7)
        }
    }

    @Test
    fun `correctly extracts nested double`() {
        runBlocking {
            // given
            val raw = """
                {
                    "object": {
                        "inner_string": "otherValue",
                        "inner_integer": 7,
                        "inner_double": 4.3
                    }
                }
            """.trimIndent()

            // when
            val result = JsonDataExtractor.extract(raw, mapOf("result" to "$.object.inner_double"))

            // then
            assertThat(result).containsEntry("result", 4.3)
        }
    }

    @Test
    fun `correctly extracts specific array element`() {
        runBlocking {
            // given
            val raw = """
                {
                    "array_integers": [
                        2,
                        3,
                        5
                    ]
                }
            """.trimIndent()

            // when
            val result = JsonDataExtractor.extract(raw, mapOf("result" to "$.array_integers[1]"))

            // then
            assertThat(result).containsEntry("result", 3)
        }
    }
}