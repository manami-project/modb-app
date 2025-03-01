package io.github.manamiproject.modb.core.json

import com.squareup.moshi.JsonDataException
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class TitleAdapterTest {

    @Nested
    inner class FromJsonTests {

        @Test
        fun `correctly deserialize non-null value`() {
            // given
            val adapter = TitleAdapter()

            // when
            val result = adapter.fromJson("\"Death Note\"")

            // then
            assertThat(result).isEqualTo("Death Note")
        }

        @Test
        fun `throw exception on null value`() {
            // given
            val adapter = TitleAdapter()

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
            val adapter = TitleAdapter()
            val obj = "Death Note"

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo("\"Death Note\"")
        }

        @Test
        fun `throws exception for a null value`() {
            // given
            val adapter = TitleAdapter().serializeNulls()

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                adapter.toJson(null)
            }

            // then
            assertThat(result).hasMessage("TitleAdapter expects non-nullable value, but received null.")
        }
    }
}