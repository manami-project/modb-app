package io.github.manamiproject.modb.core.json

import com.squareup.moshi.JsonDataException
import io.github.manamiproject.modb.core.models.AnimeSeason
import io.github.manamiproject.modb.core.models.AnimeSeason.Companion.UNKNOWN_YEAR
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class AnimeSeasonAdapterTest {

    @Nested
    inner class FromJsonTests {

        @Test
        fun `correctly deserialize non-null value`() {
            // given
            val adapter = AnimeSeasonAdapter()
            val expected = AnimeSeason(
                season = AnimeSeason.Season.WINTER,
                year = 2024,
            )

            // when
            val result = adapter.fromJson("""{"season":"WINTER","year":2024}""")

            // then
            assertThat(result).isEqualTo(expected)
        }

        @Test
        fun `correctly deserialize with year null`() {
            // given
            val adapter = AnimeSeasonAdapter()
            val expected = AnimeSeason(
                season = AnimeSeason.Season.WINTER,
                year = UNKNOWN_YEAR,
            )

            // when
            val result = adapter.fromJson("""{"season":"WINTER","year":null}""")

            // then
            assertThat(result).isEqualTo(expected)
        }

        @Test
        fun `correctly deserialize with year missing`() {
            // given
            val adapter = AnimeSeasonAdapter()
            val expected = AnimeSeason(
                season = AnimeSeason.Season.WINTER,
                year = UNKNOWN_YEAR,
            )

            // when
            val result = adapter.fromJson("""{"season":"WINTER"}""")

            // then
            assertThat(result).isEqualTo(expected)
        }

        @Test
        fun `throws exception if season is null`() {
            // given
            val adapter = AnimeSeasonAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""{"season":null,"year":2024}""")
            }

            // then
            assertThat(result).hasMessage("Expected a string but was NULL at path \$.season")
        }

        @Test
        fun `throws exception if season is missing`() {
            // given
            val adapter = AnimeSeasonAdapter()

            // when
            val result = exceptionExpected<IllegalStateException> {
                adapter.fromJson("""{"year":2024}""")
            }

            // then
            assertThat(result).hasMessage("Property 'season' is either missing or null.")
        }

        @Test
        fun `throw exception on null value`() {
            // given
            val adapter = AnimeSeasonAdapter()

            // when
            val result = exceptionExpected<JsonDataException> {
                adapter.fromJson("""null""")
            }

            // then
            assertThat(result).hasMessage("Expected BEGIN_OBJECT but was NULL at path \$")
        }
    }

    @Nested
    inner class ToJsonTests {

        @Test
        fun `correctly serialize non-null value`() {
            // given
            val adapter = AnimeSeasonAdapter()
            val obj = AnimeSeason(
                season = AnimeSeason.Season.WINTER,
                year = 2024,
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo("""{"season":"WINTER","year":2024}""")
        }

        @Test
        fun `correctly serialize setting year to null if it's unknown and serializeNull is set`() {
            // given
            val adapter = AnimeSeasonAdapter().serializeNulls()
            val obj = AnimeSeason(
                season = AnimeSeason.Season.WINTER,
                year = UNKNOWN_YEAR,
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo("""{"season":"WINTER","year":null}""")
        }

        @Test
        fun `correctly serialize omitting year if it's unknown`() {
            // given
            val adapter = AnimeSeasonAdapter()
            val obj = AnimeSeason(
                season = AnimeSeason.Season.WINTER,
                year = UNKNOWN_YEAR,
            )

            // when
            val result = adapter.toJson(obj)

            // then
            assertThat(result).isEqualTo("""{"season":"WINTER"}""")
        }

        @Test
        fun `throws exception for a null value`() {
            // given
            val adapter = AnimeSeasonAdapter().serializeNulls()

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                adapter.toJson(null)
            }

            // then
            assertThat(result).hasMessage("AnimeSeasonAdapter is non-nullable, but received null.")
        }
    }
}