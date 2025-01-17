package io.github.manamiproject.modb.core.json

import com.squareup.moshi.JsonDataException
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class HashSetAdapterTest {

    @Nested
    inner class FromJsonTests {

        @Test
        fun `correctly deserialize non-null value`() {
            // given
            val adapter = HashSetAdapter(TitleAdapter())

            // when
            val result = adapter.fromJson("""["One", "Two"]""")

            // then
            assertThat(result).isInstanceOf(HashSet::class.java)
            assertThat(result).containsExactlyInAnyOrder("One", "Two")
        }

        @Test
        fun `throw exception on null value`() {
            // given
            val adapter = HashSetAdapter(TitleAdapter())

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""null""")
            }

            // then
            assertThat(result).hasMessage("Expected BEGIN_ARRAY but was NULL at path \$")
        }
    }

    @Nested
    inner class ToJsonTests {

        @Test
        fun `correctly serialize non-null value`() {
            // given
            val adapter = HashSetAdapter(TitleAdapter())
            val obj = hashSetOf("One", "Two")

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isIn("""["Two","One"]""", """["One","Two"]""")
        }

        @Test
        fun `throws exception for a null value`() {
            // given
            val adapter = HashSetAdapter(TitleAdapter()).serializeNulls()

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                adapter.toJson(null)
            }

            // then
            assertThat(result).hasMessage("HashSetAdapter is non-nullable, but received null.")
        }
    }
}