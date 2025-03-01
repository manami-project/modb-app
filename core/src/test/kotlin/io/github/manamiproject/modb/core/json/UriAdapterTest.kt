package io.github.manamiproject.modb.core.json

import com.squareup.moshi.JsonDataException
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import java.net.URI

internal class UriAdapterTest {

    @Nested
    inner class FromJsonTests {

        @Test
        fun `correctly deserialize non-null value`() {
            // given
            val adapter = UriAdapter()

            // when
            val result = adapter.fromJson("\"https://example.org\"")

            // then
            assertThat(result).isEqualTo(URI("https://example.org"))
        }

        @Test
        fun `throw exception on null value`() {
            // given
            val adapter = UriAdapter()

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
            val adapter = UriAdapter()
            val obj = URI("https://example.org")

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo("\"https://example.org\"")
        }

        @Test
        fun `throws exception for a null value`() {
            // given
            val adapter = UriAdapter().serializeNulls()

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                adapter.toJson(null)
            }

            // then
            assertThat(result).hasMessage("UriAdapter expects non-nullable value, but received null.")
        }
    }
}