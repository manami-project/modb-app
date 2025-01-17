package io.github.manamiproject.modb.core.extractor

import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.Test

internal class ExtractionResultTest {

    @Nested
    inner class NotFoundTests {

        @Test
        fun `returns true if key doesn't exist`() {
            // given
            val obj = ExtractionResult(emptyMap())

            // when
            val result = obj.notFound("test")

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `returns true if value is NotFound`() {
            // given
            val obj = ExtractionResult(mapOf("test" to NotFound))

            // when
            val result = obj.notFound("test")

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `returns true if value is present`() {
            // given
            val obj = ExtractionResult(mapOf("test" to "string"))

            // when
            val result = obj.notFound("test")

            // then
            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class IsOfTypeTests {

        @Test
        fun `throws exception if identifier doesn't exist in result`() {
            // given
            val obj = ExtractionResult(mapOf("result" to "test"))

            // when
            val result = exceptionExpected<IllegalStateException> {
                obj.isOfType("unknown", String::class)
            }

            // then
            assertThat(result).hasMessage("Result doesn't contain entry [unknown]")
        }

        @Test
        fun `returns true if type is identical`() {
            // given
            val obj = ExtractionResult(mapOf("result" to 4))

            // when
            val result = obj.isOfType("result", Int::class)

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `returns false if object is subtype of class`() {
            // given
            val obj = ExtractionResult(mapOf("result" to 4))

            // when
            val result = obj.isOfType("result", Number::class)

            // then
            assertThat(result).isFalse()
        }

        @Test
        fun `returns false if object is of different type`() {
            // given
            val obj = ExtractionResult(mapOf("result" to "test"))

            // when
            val result = obj.isOfType("result", Int::class)

            // then
            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class StringTests {

        @Test
        fun `throws exception if identifier doesn't exist in result`() {
            // given
            val obj = ExtractionResult(mapOf("result" to "test"))

            // when
            val result = exceptionExpected<IllegalStateException> {
                obj.string("unknown")
            }

            // then
            assertThat(result).hasMessage("Result doesn't contain entry [unknown]")
        }

        @Test
        fun `returns string as-is`() {
            // given
            val obj = ExtractionResult(mapOf("result" to "test"))

            // when
            val result = obj.string("result")

            // then
            assertThat(result).isEqualTo("test")
        }

        @Test
        fun `returns NotFound`() {
            // given
            val obj = ExtractionResult(mapOf("result" to NotFound))

            // when
            val result = obj.string("result")

            // then
            assertThat(result).isEqualTo("NotFound")
        }

        @Test
        fun `casts it to string`() {
            // given
            val obj = ExtractionResult(mapOf("result" to 4))

            // when
            val result = obj.string("result")

            // then
            assertThat(result).isEqualTo("4")
        }
    }

    @Nested
    inner class StringorDefaultTests {

        @Test
        fun `throws exception if identifier doesn't exist in result`() {
            // given
            val obj = ExtractionResult(mapOf("result" to "test"))

            // when
            val result = exceptionExpected<IllegalStateException> {
                obj.stringOrDefault("unknown")
            }

            // then
            assertThat(result).hasMessage("Result doesn't contain entry [unknown]")
        }

        @Test
        fun `returns string as-is`() {
            // given
            val obj = ExtractionResult(mapOf("result" to "test"))

            // when
            val result = obj.stringOrDefault("result")

            // then
            assertThat(result).isEqualTo("test")
        }

        @Test
        fun `returns default if the value is NotFound`() {
            // given
            val obj = ExtractionResult(mapOf("result" to NotFound))

            // when
            val result = obj.stringOrDefault("result")

            // then
            assertThat(result).isEqualTo(EMPTY)
        }

        @Test
        fun `returns custom default if the value is NotFound`() {
            // given
            val obj = ExtractionResult(mapOf("result" to NotFound))

            // when
            val result = obj.stringOrDefault("result", "custom-default")

            // then
            assertThat(result).isEqualTo("custom-default")
        }
    }

    @Nested
    inner class IntTests {

        @Test
        fun `throws exception if identifier doesn't exist in result`() {
            // given
            val obj = ExtractionResult(mapOf("result" to "test"))

            // when
            val result = exceptionExpected<IllegalStateException> {
                obj.int("unknown")
            }

            // then
            assertThat(result).hasMessage("Result doesn't contain entry [unknown]")
        }

        @Test
        fun `returns integer as-is`() {
            // given
            val obj = ExtractionResult(mapOf("result" to 4))

            // when
            val result = obj.int("result")

            // then
            assertThat(result).isEqualTo(4)
        }

        @Test
        fun `returns double as int`() {
            // given
            val obj = ExtractionResult(mapOf("result" to 4.2))

            // when
            val result = obj.int("result")

            // then
            assertThat(result).isEqualTo(4)
        }

        @Test
        fun `casts string to int`() {
            // given
            val obj = ExtractionResult(mapOf("result" to "4"))

            // when
            val result = obj.int("result")

            // then
            assertThat(result).isEqualTo(4)
        }

        @Test
        fun `casting values can result in alternating values`() {
            // given
            val obj = ExtractionResult(mapOf("result" to "054"))

            // when
            val result = obj.int("result")

            // then
            assertThat(result).isEqualTo(54)
        }

        @Test
        fun `throws exception if casting is not possible`() {
            // given
            val obj = ExtractionResult(mapOf("result" to "test"))

            // when
            val result = exceptionExpected<IllegalStateException> {
                obj.int("result")
            }

            // then
            assertThat(result).hasMessage("Unable to return value [test] as Int.")
        }
    }

    @Nested
    inner class IntOrDefaultTests {

        @Test
        fun `throws exception if identifier doesn't exist in result`() {
            // given
            val obj = ExtractionResult(mapOf("result" to "test"))

            // when
            val result = exceptionExpected<IllegalStateException> {
                obj.intOrDefault("unknown")
            }

            // then
            assertThat(result).hasMessage("Result doesn't contain entry [unknown]")
        }

        @Test
        fun `returns integer as-is`() {
            // given
            val obj = ExtractionResult(mapOf("result" to 4))

            // when
            val result = obj.intOrDefault("result")

            // then
            assertThat(result).isEqualTo(4)
        }

        @Test
        fun `returns default if the value is NotFound`() {
            // given
            val obj = ExtractionResult(mapOf("result" to NotFound))

            // when
            val result = obj.intOrDefault("result")

            // then
            assertThat(result).isZero()
        }

        @Test
        fun `returns custom default if the value is NotFound`() {
            // given
            val obj = ExtractionResult(mapOf("result" to NotFound))

            // when
            val result = obj.intOrDefault("result", 7)

            // then
            assertThat(result).isEqualTo(7)
        }
    }

    @Nested
    inner class DoubleTests {

        @Test
        fun `throws exception if identifier doesn't exist in result`() {
            // given
            val obj = ExtractionResult(mapOf("result" to "test"))

            // when
            val result = exceptionExpected<IllegalStateException> {
                obj.double("unknown")
            }

            // then
            assertThat(result).hasMessage("Result doesn't contain entry [unknown]")
        }

        @Test
        fun `returns double as-is`() {
            // given
            val obj = ExtractionResult(mapOf("result" to 4.2))

            // when
            val result = obj.double("result")

            // then
            assertThat(result).isEqualTo(4.2)
        }

        @Test
        fun `returns int as double`() {
            // given
            val obj = ExtractionResult(mapOf("result" to 4))

            // when
            val result = obj.double("result")

            // then
            assertThat(result).isEqualTo(4.0)
        }

        @Test
        fun `casts string to double`() {
            // given
            val obj = ExtractionResult(mapOf("result" to "4.2"))

            // when
            val result = obj.double("result")

            // then
            assertThat(result).isEqualTo(4.2)
        }

        @Test
        fun `casting values can result in alternating values`() {
            // given
            val obj = ExtractionResult(mapOf("result" to "054"))

            // when
            val result = obj.double("result")

            // then
            assertThat(result).isEqualTo(54.0)
        }

        @Test
        fun `throws exception if casting is not possible`() {
            // given
            val obj = ExtractionResult(mapOf("result" to "test"))

            // when
            val result = exceptionExpected<IllegalStateException> {
                obj.double("result")
            }

            // then
            assertThat(result).hasMessage("Unable to return value [test] as Double.")
        }
    }

    @Nested
    inner class DoubleOrDefaultTests {

        @Test
        fun `throws exception if identifier doesn't exist in result`() {
            // given
            val obj = ExtractionResult(mapOf("result" to "test"))

            // when
            val result = exceptionExpected<IllegalStateException> {
                obj.doubleOrDefault("unknown")
            }

            // then
            assertThat(result).hasMessage("Result doesn't contain entry [unknown]")
        }

        @Test
        fun `returns double as-is`() {
            // given
            val obj = ExtractionResult(mapOf("result" to 4.2))

            // when
            val result = obj.doubleOrDefault("result")

            // then
            assertThat(result).isEqualTo(4.2)
        }

        @Test
        fun `returns default if the value is NotFound`() {
            // given
            val obj = ExtractionResult(mapOf("result" to NotFound))

            // when
            val result = obj.doubleOrDefault("result")

            // then
            assertThat(result).isZero()
        }

        @Test
        fun `returns custom default if the value is NotFound`() {
            // given
            val obj = ExtractionResult(mapOf("result" to NotFound))

            // when
            val result = obj.doubleOrDefault("result", 9.0)

            // then
            assertThat(result).isEqualTo(9.0)
        }
    }

    @Nested
    inner class ListNotNullTests {

        @Test
        fun `throws exception if identifier doesn't exist in result`() {
            // given
            val obj = ExtractionResult(mapOf("result" to "test"))

            // when
            val result = exceptionExpected<IllegalStateException> {
                obj.listNotNull<String>("unknown")
            }

            // then
            assertThat(result).hasMessage("Result doesn't contain entry [unknown]")
        }

        @Test
        fun `returns list as-is`() {
            // given
            val obj = ExtractionResult(mapOf("result" to listOf("one", "two")))

            // when
            val result = obj.listNotNull<String>("result")

            // then
            assertThat(result).containsExactlyInAnyOrder(
                "one",
                "two",
            )
        }

        @Test
        fun `returns set as list`() {
            // given
            val obj = ExtractionResult(mapOf("result" to setOf("one", "two")))

            // when
            val result = obj.listNotNull<String>("result")

            // then
            assertThat(result).containsExactlyInAnyOrder(
                "one",
                "two",
            )
        }

        @Test
        fun `returns empty list as-is`() {
            // given
            val obj = ExtractionResult(mapOf("result" to emptyList<String>()))

            // when
            val result = obj.listNotNull<String>("result")

            // then
            assertThat(result).isEmpty()
        }

        @Test
        fun `excludes null`() {
            // given
            val obj = ExtractionResult(mapOf("result" to listOf("one", null, "two")))

            // when
            val result = obj.listNotNull<String>("result")

            // then
            assertThat(result).containsExactlyInAnyOrder("one", "two")
        }

        @Test
        fun `if the result is only a single element it will be encapsulated in a list`() {
            // given
            val obj = ExtractionResult(mapOf("result" to 5))

            // when
            val result = obj.listNotNull<Int>("result")

            // then
            assertThat(result).containsExactly(5)
        }

        @Test
        fun `throws exception if all list elements are of different type`() {
            // given
            val obj = ExtractionResult(mapOf("result" to listOf(1, 3)))

            // when
            val result = exceptionExpected<IllegalStateException> {
                obj.listNotNull<String>("result")
            }

            // then
            assertThat(result).hasMessage("List not all elements are of type [kotlin.String].")
        }

        @Test
        fun `throws exception if not all list elements are of expected type`() {
            // given
            val obj = ExtractionResult(mapOf("result" to listOf("test", 3)))

            // when
            val result = exceptionExpected<IllegalStateException> {
                obj.listNotNull<String>("result")
            }

            // then
            assertThat(result).hasMessage("List not all elements are of type [kotlin.String].")
        }
    }

    @Nested
    inner class ListWithTransformTests {

        @Test
        fun `throws exception if identifier doesn't exist in result`() {
            // given
            val obj = ExtractionResult(mapOf("result" to "https://example.org"))

            // when
            val result = exceptionExpected<IllegalStateException> {
                obj.listNotNull<URI>("unknown") { URI(it) }
            }

            // then
            assertThat(result).hasMessage("Result doesn't contain entry [unknown]")
        }

        @Test
        fun `correctly transforms elements`() {
            // given
            val obj = ExtractionResult(mapOf("result" to listOf("http://example.org", "http://localhost")))

            // when
            val result = obj.listNotNull<URI>("result") { URI(it) }

            // then
            assertThat(result).containsExactlyInAnyOrder(
                URI("http://example.org"),
                URI("http://localhost"),
            )
        }

        @Test
        fun `excludes null`() {
            // given
            val obj = ExtractionResult(mapOf("result" to listOf("http://example.org", null, "http://localhost")))

            // when
            val result = obj.listNotNull<URI>("result") { URI(it) }

            // then
            assertThat(result).containsExactlyInAnyOrder(
                URI("http://example.org"),
                URI("http://localhost"),
            )
        }

        @Test
        fun `returns set as list`() {
            // given
            val obj = ExtractionResult(mapOf("result" to listOf("http://example.org", "http://localhost")))

            // when
            val result = obj.listNotNull<URI>("result") { URI(it) }

            // then
            assertThat(result).containsExactlyInAnyOrder(
                URI("http://example.org"),
                URI("http://localhost"),
            )
        }

        @Test
        fun `returns empty list as-is`() {
            // given
            val obj = ExtractionResult(mapOf("result" to emptyList<String>()))

            // when
            val result = obj.listNotNull<String>("result") { it }

            // then
            assertThat(result).isEmpty()
        }

        @Test
        fun `if the result is only a single element it will be encapsulated in a list`() {
            // given
            val obj = ExtractionResult(mapOf("result" to 5))

            // when
            val result = obj.listNotNull<Int>("result") { it.toInt() }

            // then
            assertThat(result).containsExactly(5)
        }
    }

    @Nested
    inner class ToStringTests {

        @Test
        fun `correctly create output`() {
            // given
            val extractionResult = ExtractionResult(mapOf(
                "string" to "test",
                "int" to 5,
                "double" to 2.6,
                "list" to listOf("one", "two"),
            ))

            // when
            val result = extractionResult.toString()

            // then
            assertThat(result).isEqualTo("""
                string => test
                int => 5
                double => 2.6
                list => [one, two]
            """.trimIndent())
        }
    }
}