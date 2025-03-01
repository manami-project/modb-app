package io.github.manamiproject.modb.core.json

import com.squareup.moshi.JsonDataException
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.anime.Duration
import io.github.manamiproject.modb.core.anime.Duration.Companion.UNKNOWN
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class DurationAdapterTest {

    @Nested
    inner class FromJsonTests {

        @Test
        fun `correctly deserialize non-null value`() {
            // given
            val adapter = DurationAdapter()

            // when
            val result = adapter.fromJson("""{"value":1440,"unit":"SECONDS"}""")

            // then
            assertThat(result).isEqualTo(Duration(24, MINUTES))
        }

        @Test
        fun `throws exception if unit is null`() {
            // given
            val adapter = DurationAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""{"value":1440,"unit":null}""")
            }

            // then
            assertThat(result).hasMessage("Expected a string but was NULL at path \$.unit")
        }

        @Test
        fun `throws exception if unit is missing`() {
            // given
            val adapter = DurationAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""{"value":1440}""")
            }

            // then
            assertThat(result).hasMessage("Property 'unit' is either missing or null.")
        }

        @Test
        fun `throws exception if value is null`() {
            // given
            val adapter = DurationAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""{"value":null,"unit":"SECONDS"}""")
            }

            // then
            assertThat(result).hasMessage("Expected an int but was NULL at path \$.value")
        }

        @Test
        fun `throws exception if value is missing`() {
            // given
            val adapter = DurationAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""{"unit":"SECONDS"}""")
            }

            // then
            assertThat(result).hasMessage("Property 'value' is either missing or null.")
        }

        @Test
        fun `returns unknown duration if value is null`() {
            // given
            val adapter = DurationAdapter()

            // when
            val result = adapter.fromJson("""null""")

            // then
            assertThat(result).isEqualTo(UNKNOWN)
        }
    }

    @Nested
    inner class ToJsonTests {

        @Test
        fun `correctly serialize non-null value`() {
            // given
            val adapter = DurationAdapter()
            val obj = Duration(24, MINUTES)

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo("""{"value":1440,"unit":"SECONDS"}""")
        }

        @Test
        fun `throws exception for a null value`() {
            // given
            val adapter = DurationAdapter().serializeNulls()

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                adapter.toJson(null)
            }

            // then
            assertThat(result).hasMessage("DurationAdapter expects non-nullable value, but received null.")
        }

        @Test
        fun `correctly serialize unknown value`() {
            // given
            val adapter = DurationAdapter()
            val obj = UNKNOWN

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo(EMPTY)
        }
    }
}