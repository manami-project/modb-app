package io.github.manamiproject.modb.core.json

import com.squareup.moshi.JsonDataException
import io.github.manamiproject.modb.core.anime.AnimeStatus.FINISHED
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class AnimeStatusAdapterTest {

    @Nested
    inner class FromJsonTests {

        @Test
        fun `correctly deserialize non-null value`() {
            // given
            val adapter = AnimeStatusAdapter()

            // when
            val result = adapter.fromJson("\"FINISHED\"")

            // then
            assertThat(result).isEqualTo(FINISHED)
        }

        @Test
        fun `throw exception on null value`() {
            // given
            val adapter = AnimeStatusAdapter()

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
            val adapter = AnimeStatusAdapter()
            val obj = FINISHED

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo("\"FINISHED\"")
        }

        @Test
        fun `throws exception for a null value`() {
            // given
            val adapter = AnimeStatusAdapter().serializeNulls()

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                adapter.toJson(null)
            }

            // then
            assertThat(result).hasMessage("AnimeStatusAdapter expects non-nullable value, but received null.")
        }
    }
}