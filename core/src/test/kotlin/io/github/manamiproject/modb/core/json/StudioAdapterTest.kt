package io.github.manamiproject.modb.core.json

import com.squareup.moshi.JsonDataException
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class StudioAdapterTest {

    @Nested
    inner class FromJsonTests {

        @Test
        fun `correctly deserialize non-null value`() {
            // given
            val adapter = ProducerAdapter()

            // when
            val result = adapter.fromJson("\"madhouse\"")

            // then
            assertThat(result).isEqualTo("madhouse")
        }

        @Test
        fun `throw exception on null value`() {
            // given
            val adapter = ProducerAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""null""")
            }

            // then
            assertThat(result).hasMessage("Expected a string but was NULL at path \$")
        }
    }

    @Nested
    inner class ToJsonTests {

        @Test
        fun `correctly serialize non-null value`() {
            // given
            val adapter = ProducerAdapter()
            val obj = "madhouse"

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo("\"madhouse\"")
        }

        @Test
        fun `throws exception for a null value`() {
            // given
            val adapter = ProducerAdapter().serializeNulls()

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                adapter.toJson(null)
            }

            // then
            assertThat(result).hasMessage("ProducerAdapter expects non-nullable value, but received null.")
        }
    }
}