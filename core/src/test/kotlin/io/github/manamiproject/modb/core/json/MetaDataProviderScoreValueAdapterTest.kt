package io.github.manamiproject.modb.core.json

import com.squareup.moshi.JsonDataException
import io.github.manamiproject.modb.core.anime.MetaDataProviderScoreValue
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

internal class MetaDataProviderScoreValueAdapterTest {

    @Nested
    inner class FromJsonTests {

        @Test
        fun `correctly deserialze valid MetaDataProviderScoreValue`() {
            // given
            val adapter = MetaDataProviderScoreValueAdapter()
            val expected = MetaDataProviderScoreValue(
                hostname = "example.org",
                value = 4.62,
                originalRange = 1.0..5.0,
            )

            // when
            val result = adapter.fromJson("""{"hostname":"example.org","value":4.62,"range":{"minInclusive":1.0,"maxInclusive":5.0}}""")

            // then
            assertThat(result).isEqualTo(expected)
        }

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
        fun `throws exception if title is blank`(value: String) {
            // given
            val adapter = MetaDataProviderScoreValueAdapter()

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                adapter.fromJson("""{"hostname":"$value","value":4.62,"range":{"minInclusive":1.0,"maxInclusive":5.0}}""")
            }

            // then
            assertThat(result).hasMessage("hostname must not be blank")
        }

        @Test
        fun `throws exception on null`() {
            // given
            val adapter = MetaDataProviderScoreValueAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""null""")
            }

            // then
            assertThat(result).hasMessage("""Expected BEGIN_OBJECT but was NULL at path $""")
        }

        @Test
        fun `throws exception if hostname is null`() {
            // given
            val adapter = MetaDataProviderScoreValueAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""{"value":4.62,"range":{"minInclusive":1.0,"maxInclusive":5.0}}""")
            }

            // then
            assertThat(result).hasMessage("Property 'hostname' is either missing or null.")
        }

        @Test
        fun `throws exception if value is null`() {
            // given
            val adapter = MetaDataProviderScoreValueAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""{"hostname":"example.org","range":{"minInclusive":1.0,"maxInclusive":5.0}}""")
            }

            // then
            assertThat(result).hasMessage("Property 'value' is either missing or null.")
        }

        @Test
        fun `throws exception if range is null`() {
            // given
            val adapter = MetaDataProviderScoreValueAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""{"hostname":"example.org","value":4.62}""")
            }

            // then
            assertThat(result).hasMessage("Property 'range.minInclusive' is either missing or null.")
        }

        @Test
        fun `throws exception if minInclusive is null`() {
            // given
            val adapter = MetaDataProviderScoreValueAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""{"hostname":"example.org","value":4.62,"range":{"maxInclusive":5.0}}""")
            }

            // then
            assertThat(result).hasMessage("Property 'range.minInclusive' is either missing or null.")
        }

        @Test
        fun `throws exception if maxInclusive is null`() {
            // given
            val adapter = MetaDataProviderScoreValueAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""{"hostname":"example.org","value":4.62,"range":{"minInclusive":1.0}}""")
            }

            // then
            assertThat(result).hasMessage("Property 'range.maxInclusive' is either missing or null.")
        }

        @Test
        fun `ignore other properties`() {
            // given
            val adapter = MetaDataProviderScoreValueAdapter()
            val expected = MetaDataProviderScoreValue(
                hostname = "example.org",
                value = 4.62,
                originalRange = 1.0..5.0,
            )

            // when
            val result = adapter.fromJson("""{"hostname":"example.org","unmapped": true,"value":4.62,"range":{"minInclusive":1.0,"maxInclusive":5.0}}""")

            // then
            assertThat(result).isEqualTo(expected)
        }
    }

    @Nested
    inner class ToJsonTests {

        @Test
        fun `throws exception if value is null`() {
            // given
            val adapter = MetaDataProviderScoreValueAdapter()

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                adapter.toJson(null)
            }

            // then
            assertThat(result).hasMessage("MetaDataProviderScoreAdapter expects non-nullable value, but received null.")
        }

        @Test
        fun `correctly serializes MetaDataProviderScoreValue`() {
            // given
            val adapter = MetaDataProviderScoreValueAdapter()
            val score = MetaDataProviderScoreValue(
                hostname = "example.org",
                value = 4.62,
                originalRange = 1.0..5.0,
            )

            // when
            val result = adapter.toJson(score)

            // then
            assertThat(result).isEqualTo("""{"hostname":"example.org","value":4.62,"range":{"minInclusive":1.0,"maxInclusive":5.0}}""")
        }
    }
}