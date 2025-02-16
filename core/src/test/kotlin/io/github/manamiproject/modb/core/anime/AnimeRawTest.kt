package io.github.manamiproject.modb.core.anime

import io.github.manamiproject.modb.core.anime.AnimeSeason.Companion.UNKNOWN_YEAR
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.FINISHED
import io.github.manamiproject.modb.core.anime.AnimeStatus.ONGOING
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.*
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.json.AnimeRawAdapter
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI
import kotlin.test.Test
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN as UNKNOWN_STATUS
import io.github.manamiproject.modb.core.anime.AnimeType.UNKNOWN as UNKNOWN_TYPE
import io.github.manamiproject.modb.core.anime.Duration.Companion.UNKNOWN as UNKNOWN_DURATION

internal class AnimeRawTest {

    @Nested
    inner class TitleTests {

        @Test
        fun `remove leading whitespace from title`() {
            // given
            val expectedTitle = "Death Note"

            // when
            val result = AnimeRaw(" $expectedTitle")

            // then
            assertThat(result.title).isEqualTo(expectedTitle)
        }

        @Test
        fun `remove tailing whitespace from title`() {
            // given
            val expectedTitle = "Death Note"

            // when
            val result = AnimeRaw("$expectedTitle ")

            // then
            assertThat(result.title).isEqualTo(expectedTitle)
        }

        @Test
        fun `replace multiple whitespaces with a single whitespace in title`() {
            // given
            val expectedTitle = "Death Note"

            // when
            val result = AnimeRaw("Death    Note")

            // then
            assertThat(result.title).isEqualTo(expectedTitle)
        }

        @Test
        fun `replace tab character with whitespace in title`() {
            // given
            val expectedTitle = "Death Note"

            // when
            val result = AnimeRaw("Death\tNote")

            // then
            assertThat(result.title).isEqualTo(expectedTitle)
        }

        @Test
        fun `replace line feed character with whitespace in title`() {
            // given
            val expectedTitle = "Death Note"

            // when
            val result = AnimeRaw("Death\nNote")

            // then
            assertThat(result.title).isEqualTo(expectedTitle)
        }

        @Test
        fun `replace carriage return line feed with whitespace in title`() {
            // given
            val expectedTitle = "Death Note"

            // when
            val result = AnimeRaw("Death\r\nNote")

            // then
            assertThat(result.title).isEqualTo(expectedTitle)
        }

        @Test
        fun `remove zero-width non-joiner`() {
            // when
            val result = AnimeRaw("Ba\u200Cek")

            // then
            assertThat(result.title).isEqualTo("Baek")
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
        fun `throws exception if title is empty or blank or zero-width non-joiner`(value: String) {
            // when
            val result = assertThrows<IllegalArgumentException> {
                AnimeRaw(value)
            }

            // then
            assertThat(result).hasMessage("Title cannot be blank.")
        }

        @Test
        fun `don't change title if activateChecks is set to false`() {
            // given
            val expectedTitle = " Death Note "

            // when
            val result = AnimeRaw(
                _title = expectedTitle,
                activateChecks = false,
            )

            // then
            assertThat(result.title).isEqualTo(expectedTitle)
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
        fun `throws an exception if title is blank even if activateChecks is set to false`(value: String) {
            // when
            val result = exceptionExpected<IllegalArgumentException> {
                AnimeRaw(
                    _title = value,
                    activateChecks = false,
                )
            }

            // then
            assertThat(result).hasMessage("Title cannot be blank.")
        }
    }

    @Nested
    inner class SynonymsTests   {

        @Test
        fun `ensure that you cannot directly modify the internal hashset`() {
            // given
            val anime = AnimeRaw(
                _title = "test",
                _synonyms = hashSetOf(
                    "other title1",
                    "other title2",
                ),
            )

            // when
            anime.synonyms.clear()

            // then
            assertThat(anime.synonyms).isNotEmpty()
        }

        @Nested
        inner class AddSynonymsConstructorTests {

            @Test
            fun `must not add a synonym if it equals the title`() {
                // given
                val title = "Death Note"

                // when
                val result = AnimeRaw(
                    _title =  title,
                    _synonyms = hashSetOf(
                        title,
                    ),
                )

                // then
                assertThat(result.synonyms).isEmpty()
            }

            @Test
            fun `must not add blank synonym`() {
                // when
                val result = AnimeRaw(
                    _title = "Death Note",
                    _synonyms = hashSetOf(
                        "         ",
                    ),
                )

                // then
                assertThat(result.synonyms).isEmpty()
            }

            @Test
            fun `must not add zero-width non-joiner synonym`() {
                // when
                val result = AnimeRaw(
                    _title = "Death Note",
                    _synonyms = hashSetOf(
                        "\u200C",
                    ),
                )

                // then
                assertThat(result.synonyms).isEmpty()
            }

            @Test
            fun `successfully add a synonym`() {
                // given
                val synonym = "Caderno da Morte"

                // when
                val result = AnimeRaw(
                    _title = "Death Note",
                    _synonyms = hashSetOf(
                        synonym,
                    ),
                )

                // then
                assertThat(result.synonyms).containsExactly(
                    synonym,
                )
            }

            @Test
            fun `must not add a duplicated synonym`() {
                // given
                val synonym = "Caderno da Morte"

                // when
                val result = AnimeRaw(
                    _title = "Death Note",
                    _synonyms = hashSetOf(
                        synonym,
                        synonym,
                    ),
                )

                // then
                assertThat(result.synonyms).containsExactly(
                    synonym,
                )
            }

            @Test
            fun `synonym comparison to title is not case sensitive`() {
                // given
                val title  =  "Death Note"

                // when
                val result = AnimeRaw(
                    _title = title,
                    _synonyms = hashSetOf(
                        title.uppercase(),
                        title.lowercase(),
                    ),
                )

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    title.uppercase(),
                    title.lowercase(),
                )
            }

            @Test
            fun `synonym comparison is not case sensitive`() {
                // given
                val title  =  "Death Note"

                // when
                val result = AnimeRaw(
                    _title = "デスノート",
                    _synonyms = hashSetOf(
                        title,
                        title.lowercase(),
                        title.uppercase(),
                    ),
                )

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    title,
                    title.uppercase(),
                    title.lowercase(),
                )
            }

            @Test
            fun `remove leading whitespace from synonyms`() {
                // given
                val expectedTitleOne = "Death Note"
                val expectedTitleTwo = "Made in Abyss"

                // when
                val result = AnimeRaw(
                    _title = "Title",
                    _synonyms = hashSetOf(
                        " $expectedTitleOne",
                        " $expectedTitleTwo",
                    ),
                )

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }

            @Test
            fun `remove tailing whitespace from synonyms`() {
                // given
                val expectedTitleOne = "Death Note"
                val expectedTitleTwo = "Made in Abyss"

                // when
                val result = AnimeRaw(
                    _title = "Title",
                    _synonyms = hashSetOf(
                        "$expectedTitleOne ",
                        "$expectedTitleTwo ",
                    ),
                )

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }

            @Test
            fun `replace multiple whitespaces with a single whitespace in synonyms`() {
                // given
                val expectedTitleOne = "Death Note"
                val expectedTitleTwo = "Made in Abyss"

                // when
                val result = AnimeRaw(
                    _title = "Title",
                    _synonyms = hashSetOf(
                        "Death        Note",
                        "Made      in        Abyss",
                    ),
                )

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }

            @Test
            fun `replace tab character with whitespace in synonyms`() {
                // given
                val expectedTitleOne = "Death Note"
                val expectedTitleTwo = "Made in Abyss"

                // when
                val result = AnimeRaw(
                    _title = "Title",
                    _synonyms = hashSetOf(
                        "Death\tNote",
                        "Made\tin\tAbyss",
                    ),
                )

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }

            @Test
            fun `replace line feed character with whitespace in synonyms`() {
                // given
                val expectedTitleOne = "Death Note"
                val expectedTitleTwo = "Made in Abyss"

                // when
                val result = AnimeRaw(
                    _title = "Title",
                    _synonyms = hashSetOf(
                        "Death\nNote",
                        "Made\nin\nAbyss",
                    ),
                )

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }

            @Test
            fun `replace carriage return line feed character with whitespace in synonyms`() {
                // given
                val expectedTitleOne = "Death Note"
                val expectedTitleTwo = "Made in Abyss"

                // when
                val result = AnimeRaw(
                    _title = "Title",
                    _synonyms = hashSetOf(
                        "Death\r\nNote",
                        "Made\r\nin\r\nAbyss",
                    ),
                )

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }

            @Test
            fun `replace zero-width non-joiner character in synonyms`() {
                // given
                val expectedTitleOne = "DeathNote"
                val expectedTitleTwo = "MadeinAbyss"

                // when
                val result = AnimeRaw(
                    _title = "Title",
                    _synonyms = hashSetOf(
                        "Death\u200CNote",
                        "Made\u200Cin\u200CAbyss",
                    ),
                )

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [" Death Note", "Death Note ", "  Death   Note  ", "", " ", "    ", "\u200C"])
            fun `doesn't fix synonyms if activateChecks is false`(value: String) {
                // when
                val obj = AnimeRaw(
                    _title = "デスノート",
                    _synonyms = hashSetOf(value),
                    activateChecks = false,
                )

                // then
                assertThat(obj.synonyms).containsExactlyInAnyOrder(
                    value,
                )
            }
        }

        @Nested
        inner class AddSynonymsTests {

            @Test
            fun `must not add a synonym if it equals the title`() {
                // given
                val anime = AnimeRaw("Death Note")

                // when
                anime.addSynonyms(listOf(
                    "Death Note",
                ))

                // then
                assertThat(anime.synonyms).isEmpty()
            }

            @Test
            fun `must not add blank synonym`() {
                // given
                val anime = AnimeRaw("Death Note")

                // when
                anime.addSynonyms(listOf(
                    "         ",
                ))

                // then
                assertThat(anime.synonyms).isEmpty()
            }

            @Test
            fun `must not add zero-width non-joiner synonym`() {
                // given
                val anime = AnimeRaw("Death Note")

                // when
                anime.addSynonyms(listOf(
                    "\u200C",
                ))

                // then
                assertThat(anime.synonyms).isEmpty()
            }

            @Test
            fun `successfully add a list of synonyms`() {
                // given
                val one = "Caderno da Morte"
                val two = "DN"
                val three = "Quaderno della Morte"
                val four = "Sveska Smrti"

                val anime = AnimeRaw("Death Note")

                // when
                anime.addSynonyms(listOf(
                    four,
                    two,
                    three,
                    one,
                ))

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    one,
                    two,
                    three,
                    four,
                )
            }

            @Test
            fun `must not add a duplicated synonym`() {
                // given
                val one = "Caderno da Morte"
                val two = "DN"

                val anime = AnimeRaw(
                    _title = "Death Note",
                    _synonyms = hashSetOf(
                        two,
                    ),
                )

                // when
                anime.addSynonyms(listOf(
                    two,
                    one,
                ))

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    one,
                    two,
                )
            }

            @Test
            fun `synonym comparison to title is not case sensitive`() {
                // given
                val title  =  "Death Note"
                val anime = AnimeRaw(title)

                // when
                anime.addSynonyms(listOf(
                    title.uppercase(),
                    title.lowercase(),
                ))

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    title.uppercase(),
                    title.lowercase(),
                )
            }

            @Test
            fun `synonym comparison is not case sensitive`() {
                // given
                val title  =  "Death Note"
                val anime = AnimeRaw(
                    _title = "デスノート",
                    _synonyms = hashSetOf(
                        title.lowercase(),
                        title.uppercase(),
                    ),
                )

                // when
                anime.addSynonyms(title)

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    title,
                    title.uppercase(),
                    title.lowercase(),
                )
            }

            @Test
            fun `remove leading whitespace from synonyms`() {
                // given
                val expectedTitleOne = "Death Note"
                val expectedTitleTwo = "Made in Abyss"
                val anime = AnimeRaw("Title")

                // when
                anime.addSynonyms(listOf(
                    " $expectedTitleOne",
                    " $expectedTitleTwo",
                ))

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }

            @Test
            fun `remove tailing whitespace from synonyms`() {
                // given
                val expectedTitleOne = "Death Note"
                val expectedTitleTwo = "Made in Abyss"
                val anime = AnimeRaw("Title")

                // when
                anime.addSynonyms(listOf(
                    "$expectedTitleOne ",
                    "$expectedTitleTwo ",
                ))

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }

            @Test
            fun `replace multiple whitespaces with a single whitespace in synonyms`() {
                // given
                val expectedTitleOne = "Death Note"
                val expectedTitleTwo = "Made in Abyss"
                val anime = AnimeRaw("Title")

                // when
                anime.addSynonyms(listOf(
                    "Death        Note",
                    "Made      in        Abyss",
                ))

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }

            @Test
            fun `replace tab character with whitespace in synonyms`() {
                // given
                val expectedTitleOne = "Death Note"
                val expectedTitleTwo = "Made in Abyss"
                val anime = AnimeRaw("Title")

                // when
                anime.addSynonyms(listOf(
                    "Death\tNote",
                    "Made\tin\tAbyss",
                ))

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }

            @Test
            fun `replace line feed character with whitespace in synonyms`() {
                // given
                val expectedTitleOne = "Death Note"
                val expectedTitleTwo = "Made in Abyss"
                val anime = AnimeRaw("Title")

                // when
                anime.addSynonyms(listOf(
                    "Death\nNote",
                    "Made\nin\nAbyss",
                ))

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }

            @Test
            fun `replace carriage return line feed character with whitespace in synonyms`() {
                // given
                val expectedTitleOne = "Death Note"
                val expectedTitleTwo = "Made in Abyss"
                val anime = AnimeRaw("Title")

                // when
                anime.addSynonyms(listOf(
                    "Death\r\nNote",
                    "Made\r\nin\r\nAbyss",
                ))

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }

            @Test
            fun `replace zero-width non-joiner character in synonyms`() {
                // given
                val expectedTitleOne = "DeathNote"
                val expectedTitleTwo = "MadeinAbyss"
                val anime = AnimeRaw("Title")

                // when
                anime.addSynonyms(listOf(
                    "Death\u200CNote",
                    "Made\u200Cin\u200CAbyss",
                ))

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }
        }

        @Nested
        inner class AddSynonymsVarargTests {

            @Test
            fun `must not add a synonym if it equals the title`() {
                // given
                val anime = AnimeRaw("Death Note")

                // when
                anime.addSynonyms(
                    "Death Note",
                )

                // then
                assertThat(anime.synonyms).isEmpty()
            }

            @Test
            fun `must not add blank synonym`() {
                // given
                val anime = AnimeRaw("Death Note")

                // when
                anime.addSynonyms(
                    "         ",
                )

                // then
                assertThat(anime.synonyms).isEmpty()
            }

            @Test
            fun `must not add zero-width non-joiner synonym`() {
                // given
                val anime = AnimeRaw("Death Note")

                // when
                anime.addSynonyms(
                    "\u200C",
                )

                // then
                assertThat(anime.synonyms).isEmpty()
            }

            @Test
            fun `successfully add synonyms`() {
                // given
                val one = "Caderno da Morte"
                val two = "DN"
                val three = "Quaderno della Morte"
                val four = "Sveska Smrti"

                val anime = AnimeRaw("Death Note")

                // when
                anime.addSynonyms(
                    four,
                    two,
                    three,
                    one,
                )

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    one,
                    two,
                    three,
                    four,
                )
            }

            @Test
            fun `must not add a duplicated synonym`() {
                // given
                val one = "Caderno da Morte"
                val two = "DN"

                val anime = AnimeRaw(
                    _title = "Death Note",
                    _synonyms = hashSetOf(
                        two,
                    ),
                )

                // when
                anime.addSynonyms(
                    two,
                    one,
                )

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    one,
                    two,
                )
            }

            @Test
            fun `synonym comparison to title is case sensitive`() {
                // given
                val title  =  "Death Note"
                val anime = AnimeRaw(title)

                // when
                anime.addSynonyms(
                    title.uppercase(),
                    title.lowercase(),
                )

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    title.uppercase(),
                    title.lowercase(),
                )
            }

            @Test
            fun `synonym comparison is not case sensitive`() {
                // given
                val title  =  "Death Note"
                val anime = AnimeRaw("デスノート")

                // when
                anime.addSynonyms(
                    title,
                    title.lowercase(),
                    title.uppercase(),
                )

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    title,
                    title.uppercase(),
                    title.lowercase(),
                )
            }

            @Test
            fun `remove leading whitespace from synonyms`() {
                // given
                val expectedTitleOne = "Death Note"
                val expectedTitleTwo = "Made in Abyss"
                val anime = AnimeRaw("Title")

                // when
                anime.addSynonyms(
                    " $expectedTitleOne",
                    " $expectedTitleTwo",
                )

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }

            @Test
            fun `remove tailing whitespace from synonyms`() {
                // given
                val expectedTitleOne = "Death Note"
                val expectedTitleTwo = "Made in Abyss"
                val anime = AnimeRaw("Title")

                // when
                anime.addSynonyms(
                    "$expectedTitleOne ",
                    "$expectedTitleTwo ",
                )

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }

            @Test
            fun `replace multiple whitespaces with a single whitespace in synonyms`() {
                // given
                val expectedTitleOne = "Death Note"
                val expectedTitleTwo = "Made in Abyss"
                val anime = AnimeRaw("Title")

                // when
                anime.addSynonyms(
                    "Death        Note",
                    "Made      in        Abyss",
                )

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }

            @Test
            fun `replace tab character with whitespace in synonyms`() {
                // given
                val expectedTitleOne = "Death Note"
                val expectedTitleTwo = "Made in Abyss"
                val anime = AnimeRaw("Title")

                // when
                anime.addSynonyms(
                    "Death\tNote",
                    "Made\tin\tAbyss",
                )

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }

            @Test
            fun `replace line feed character with whitespace in synonyms`() {
                // given
                val expectedTitleOne = "Death Note"
                val expectedTitleTwo = "Made in Abyss"
                val anime = AnimeRaw("Title")

                // when
                anime.addSynonyms(
                    "Death\nNote",
                    "Made\nin\nAbyss",
                )

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }

            @Test
            fun `replace carriage return line feed character with whitespace in synonyms`() {
                // given
                val expectedTitleOne = "Death Note"
                val expectedTitleTwo = "Made in Abyss"
                val anime = AnimeRaw("Title")

                // when
                anime.addSynonyms(
                    "Death\r\nNote",
                    "Made\r\nin\r\nAbyss",
                )

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }

            @Test
            fun `replace zero-width non-joiner character in synonyms`() {
                // given
                val expectedTitleOne = "DeathNote"
                val expectedTitleTwo = "MadeinAbyss"
                val anime = AnimeRaw("Title")

                // when
                anime.addSynonyms(
                    "Death\u200CNote",
                    "Made\u200Cin\u200CAbyss",
                )

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }
        }
    }

    @Nested
    inner class SourcesTests {

        @Test
        fun `ensure that you cannot directly modify the internal hashset`() {
            // given
            val anime = AnimeRaw(
                _title = "test",
                _sources = hashSetOf(
                    URI("https://example.org/anime/1535"),
                    URI("https://myanimelist.net/anime/1535"),
                ),
            )

            // when
            anime.sources.clear()

            // then
            assertThat(anime.sources).isNotEmpty()
        }

        @Nested
        inner class AddSourcesConstructorTests {

            @Test
            fun `add source`() {
                // given
                val source = URI("https://myanimelist.net/anime/1535")

                // when
                val anime = AnimeRaw(
                    _title =  "Death Note",
                    _sources = hashSetOf(
                        source,
                    ),
                )

                // then
                assertThat(anime.sources).containsExactly(source)
            }

            @Test
            fun `cannot add duplicated source link`() {
                // given
                val source = URI("https://myanimelist.net/anime/1535")

                // when
                val result = AnimeRaw(
                    _title =  "Death Note",
                    _sources = hashSetOf(
                        source,
                        source,
                    ),
                )

                // then
                assertThat(result.sources).containsExactly(source)
            }

            @Test
            fun `remove related anime if the same uri has been added to sources`() {
                // given
                val source = URI("https://myanimelist.net/anime/1535")

                // when
                val result = AnimeRaw(
                    _title =  "Death Note",
                    _sources = hashSetOf(
                        source,
                    ),
                    _relatedAnime = hashSetOf(
                        source,
                    ),
                )

                // then
                assertThat(result.sources).containsExactly(
                    source,
                )
                assertThat(result.relatedAnime).isEmpty()
            }

            @Test
            fun `don't remove related anime if activateChecks is false`() {
                // given
                val source = URI("https://myanimelist.net/anime/1535")

                // when
                val result = AnimeRaw(
                    _title =  "Death Note",
                    _sources = hashSetOf(
                        source,
                    ),
                    _relatedAnime = hashSetOf(
                        source,
                    ),
                    activateChecks = false,
                )

                // then
                assertThat(result.sources).containsExactly(
                    source,
                )
                assertThat(result.relatedAnime).containsExactly(
                    source,
                )
            }
        }

        @Nested
        inner class AddSourcesTests {

            @Test
            fun `add source`() {
                // given
                val source = URI("https://myanimelist.net/anime/1535")
                val anime = AnimeRaw(
                    _title =  "Death Note",
                )

                // when
                anime.addSources(listOf(
                    source,
                ))

                // then
                assertThat(anime.sources).containsExactly(
                    source,
                )
            }

            @Test
            fun `cannot add duplicated source link`() {
                // given
                val source = URI("https://myanimelist.net/anime/1535")
                val anime = AnimeRaw(
                    _title =  "Death Note",
                    _sources = hashSetOf(
                        source,
                    ),
                )

                // when
                anime.addSources(listOf(
                    source,
                ))

                // then
                assertThat(anime.sources).containsExactly(
                    source,
                )
            }

            @Test
            fun `remove related anime if the same uri has been added to sources`() {
                // given
                val source = URI("https://myanimelist.net/anime/1535")
                val anime = AnimeRaw(
                    _title =  "Death Note",
                    _relatedAnime = hashSetOf(
                        source,
                    ),
                )

                // when
                anime.addSources(listOf(
                    source,
                ))

                // then
                assertThat(anime.sources).containsExactly(
                    source,
                )
                assertThat(anime.relatedAnime).isEmpty()
            }
        }

        @Nested
        inner class AddSourcesVarargTests {

            @Test
            fun `add source`() {
                // given
                val source = URI("https://myanimelist.net/anime/1535")
                val anime = AnimeRaw(
                    _title =  "Death Note",
                )

                // when
                anime.addSources(
                    source,
                )

                // then
                assertThat(anime.sources).containsExactly(
                    source,
                )
            }

            @Test
            fun `cannot add duplicated source link`() {
                // given
                val source = URI("https://myanimelist.net/anime/1535")
                val anime = AnimeRaw(
                    _title =  "Death Note",
                    _sources = hashSetOf(
                        source,
                    ),
                )

                // when
                anime.addSources(source)

                // then
                assertThat(anime.sources).containsExactly(
                    source,
                )
            }

            @Test
            fun `remove related anime if the same uri has been added to sources`() {
                // given
                val source = URI("https://myanimelist.net/anime/1535")
                val anime = AnimeRaw(
                    _title =  "Death Note",
                    _relatedAnime = hashSetOf(
                        source,
                    ),
                )

                // when
                anime.addSources(source)

                // then
                assertThat(anime.sources).containsExactly(
                    source,
                )
                assertThat(anime.relatedAnime).isEmpty()
            }
        }

        @Nested
        inner class RemoveSourceIfTests {

            @Test
            fun `successfully remove source`() {
                // given
                val source = URI("https://myanimelist.net/anime/2994")
                val anime = AnimeRaw(
                    _title =  "Death Note",
                    _sources = hashSetOf(
                        source,
                    ),
                )

                // when
                anime.removeSourceIf { it.toString() == "https://myanimelist.net/anime/2994" }

                // then
                assertThat(anime.sources).isEmpty()
            }

            @Test
            fun `don't remove anything if the condition doesn't match`() {
                // given
                val source = URI("https://myanimelist.net/anime/2994")
                val anime = AnimeRaw(
                    _title =  "Death Note",
                    _sources = hashSetOf(
                        source,
                    ),
                )

                // when
                anime.removeSourceIf { it.toString().contains("anidb.net") }

                // then
                assertThat(anime.sources).containsExactly(
                    source,
                )
            }
        }
    }

    @Nested
    inner class RelatedAnimeTests {

        @Nested
        inner class AddRelatedAnimeConstructorTests {

            @Test
            fun `ensure that you cannot directly modify the internal hashset`() {
                // given
                val anime = AnimeRaw(
                    _title = "test",
                    _relatedAnime = hashSetOf(
                        URI("https://example.org/anime/1535"),
                        URI("https://myanimelist.net/anime/1535"),
                    ),
                )

                // when
                anime.relatedAnime.clear()

                // then
                assertThat(anime.relatedAnime).isNotEmpty()
            }

            @Test
            fun `add related anime`() {
                // given
                val relatedAnime = URI("https://myanimelist.net/anime/2994")

                // when
                val result = AnimeRaw(
                    _title =  "Death Note",
                    _relatedAnime = hashSetOf(
                        relatedAnime,
                    ),
                )

                // then
                assertThat(result.relatedAnime).containsExactly(
                    relatedAnime,
                )
            }

            @Test
            fun `cannot add duplicated link for related anime`() {
                // given
                val relatedAnime = URI("https://myanimelist.net/anime/2994")

                // when
                val result = AnimeRaw(
                    _title =  "Death Note",
                    _relatedAnime = hashSetOf(
                        relatedAnime,
                        relatedAnime,
                    ),
                )

                // then
                assertThat(result.relatedAnime).containsExactly(
                    relatedAnime,
                )
            }

            @Test
            fun `cannot add a related anime if the links is already part of the sources`() {
                // given
                val link = URI("https://myanimelist.net/anime/1535")

                // when
                val result = AnimeRaw(
                    _title =  "Death Note",
                    _sources = hashSetOf(
                        link,
                    ),
                    _relatedAnime = hashSetOf(
                        link,
                    ),
                )

                // then
                assertThat(result.relatedAnime).isEmpty()
            }

            @Test
            fun `doesn't remove sources from relatedAnimeif activateChecks is false`() {
                // when
                val obj = AnimeRaw(
                    _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                    _sources = hashSetOf(
                        URI("https://myanimelist.net/anime/6351"),
                    ),
                    _relatedAnime = hashSetOf(
                        URI("https://myanimelist.net/anime/2167"),
                        URI("https://myanimelist.net/anime/6351"),
                    ),
                    activateChecks = false,
                )

                // then
                assertThat(obj.relatedAnime).hasSize(2)
            }
        }

        @Nested
        inner class AddRelatedAnimeTests {

            @Test
            fun `add related anime`() {
                // given
                val relatedAnime = URI("https://myanimelist.net/anime/2994")
                val anime = AnimeRaw(
                    _title =  "Death Note",
                )

                // when
                anime.addRelatedAnime(listOf(
                    relatedAnime,
                ))

                // then
                assertThat(anime.relatedAnime).containsExactly(
                    relatedAnime,
                )
            }

            @Test
            fun `cannot add duplicated link for related anime`() {
                // given
                val relatedAnime = URI("https://myanimelist.net/anime/2994")
                val anime = AnimeRaw(
                    _title =  "Death Note",
                    _relatedAnime = hashSetOf(
                        relatedAnime,
                    ),
                )

                // when
                anime.addRelatedAnime(listOf(
                    relatedAnime,
                ))

                // then
                assertThat(anime.relatedAnime).containsExactly(
                    relatedAnime,
                )
            }

            @Test
            fun `cannot add a related anime if the links is already part of the sources`() {
                // given
                val link = URI("https://myanimelist.net/anime/1535")
                val anime = AnimeRaw(
                    _title =  "Death Note",
                    _sources = hashSetOf(
                        link,
                    ),
                )

                // when
                anime.addRelatedAnime(listOf(
                    link,
                ))

                // then
                assertThat(anime.relatedAnime).isEmpty()
            }
        }

        @Nested
        inner class AddRelatedAnimeVarargTests {

            @Test
            fun `add related anime`() {
                // given
                val relatedAnime = URI("https://myanimelist.net/anime/2994")
                val anime = AnimeRaw(
                    _title =  "Death Note",
                )

                // when
                anime.addRelatedAnime(
                    relatedAnime,
                )

                // then
                assertThat(anime.relatedAnime).containsExactly(
                    relatedAnime,
                )
            }

            @Test
            fun `cannot add duplicated link for related anime`() {
                // given
                val relatedAnime = URI("https://myanimelist.net/anime/2994")
                val anime = AnimeRaw(
                    _title =  "Death Note",
                    _relatedAnime = hashSetOf(
                        relatedAnime,
                    ),
                )

                // when
                anime.addRelatedAnime(
                    relatedAnime,
                )

                // then
                assertThat(anime.relatedAnime).containsExactly(
                    relatedAnime,
                )
            }

            @Test
            fun `cannot add a related anime if the links is already part of the sources`() {
                // given
                val link = URI("https://myanimelist.net/anime/1535")
                val anime = AnimeRaw(
                    _title =  "Death Note",
                    _sources = hashSetOf(
                        link,
                    ),
                )

                // when
                anime.addRelatedAnime(link)

                // then
                assertThat(anime.relatedAnime).isEmpty()
            }
        }

        @Nested
        inner class RemoveRelationIfTests {

            @Test
            fun `successfully remove related anime`() {
                // given
                val relatedAnime = URI("https://myanimelist.net/anime/2994")
                val anime = AnimeRaw(
                    _title =  "Death Note",
                    _relatedAnime = hashSetOf(
                        relatedAnime,
                    ),
                )

                // when
                anime.removeRelatedAnimeIf { it.toString() == "https://myanimelist.net/anime/2994" }

                // then
                assertThat(anime.relatedAnime).isEmpty()
            }

            @Test
            fun `don't remove anything if condition doesn't match`() {
                // given
                val relatedAnime = URI("https://myanimelist.net/anime/2994")
                val anime = AnimeRaw(
                    _title =  "Death Note",
                    _relatedAnime = hashSetOf(
                        relatedAnime,
                    ),
                )

                // when
                anime.removeRelatedAnimeIf { it.toString().contains("anidb.net") }

                // then
                assertThat(anime.relatedAnime).containsExactly(
                    relatedAnime,
                )
            }
        }
    }

    @Nested
    inner class EqualityTests {

        @Test
        fun `is equal if titles are the same`() {
            // given
            val title =  "Death Note"
            val a = AnimeRaw(
                _title = title,
            )

            val b = AnimeRaw(
                _title = title,
            )

            // when
            val result = a == b

            // then
            assertThat(result).isTrue()
            assertThat(a.hashCode()).isEqualTo(b.hashCode())
        }

        @Test
        fun `is not equal if titles are different`() {
            // given
            val a = AnimeRaw(
                _title =  "Death Note",
            )

            val b = AnimeRaw(
                _title =  "デスノート",
            )

            // when
            val result = a == b

            // then
            assertThat(result).isFalse()
            assertThat(a.hashCode()).isNotEqualTo(b.hashCode())
        }

        @Test
        fun `is equal if source links are the same`() {
            // given
            val title =  "Death Note"
            val a = AnimeRaw(
                _title = title,
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/1535"),
                ),
            )

            val b = AnimeRaw(
                _title = title,
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/1535"),
                ),
            )

            // when
            val result = a == b

            // then
            assertThat(result).isTrue()
            assertThat(a.hashCode()).isEqualTo(b.hashCode())
        }

        @Test
        fun `is not equal if source links are different`() {
            // given
            val title  =  "Death Note"
            val a = AnimeRaw(
                _title =  title,
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/1535"),
                ),
            )

            val b = AnimeRaw(
                _title =  title,
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/1535"),
                    URI("https://anidb.net/anime/4563"),
                ),
            )

            // when
            val result = a == b

            // then
            assertThat(result).isFalse()
            assertThat(a.hashCode()).isNotEqualTo(b.hashCode())
        }

        @Test
        fun `is equal if synonyms are the same`() {
            // given
            val title  =  "Death Note"
            val a = AnimeRaw(
                _title =  title,
                _synonyms = hashSetOf(
                    "Caderno da Morte",
                ),
            )

            val b = AnimeRaw(
                _title =  title,
                _synonyms = hashSetOf(
                    "Caderno da Morte",
                ),
            )

            // when
            val result = a == b

            // then
            assertThat(result).isTrue()
            assertThat(a.hashCode()).isEqualTo(b.hashCode())
        }

        @Test
        fun `is not equal if synonyms are different`() {
            // given
            val title  =  "Death Note"
            val a = AnimeRaw(
                _title =  title,
                _synonyms = hashSetOf(
                    "Caderno da Morte",
                ),
            )

            val b = AnimeRaw(
                _title =  title,
                _synonyms = hashSetOf(
                    "Caderno da Morte",
                    "Quaderno della Morte",
                ),
            )

            // when
            val result = a == b

            // then
            assertThat(result).isFalse()
            assertThat(a.hashCode()).isNotEqualTo(b.hashCode())
        }

        @Test
        fun `is equal if types are the same`() {
            // given
            val title =  "Death Note"
            val a = AnimeRaw(
                _title = title,
                type = TV,
            )

            val b = AnimeRaw(
                _title = title,
                type = TV,
            )

            // when
            val result = a == b

            // then
            assertThat(result).isTrue()
            assertThat(a.hashCode()).isEqualTo(b.hashCode())
        }

        @Test
        fun `is not equal if types are different`() {
            // given
            val title  =  "Death Note"
            val a = AnimeRaw(
                _title =  title,
                type = TV,
            )

            val b = AnimeRaw(
                _title =  title,
                type = MOVIE,
            )

            // when
            val result = a == b

            // then
            assertThat(result).isFalse()
            assertThat(a.hashCode()).isNotEqualTo(b.hashCode())
        }

        @Test
        fun `is equal if episodes are the same`() {
            // given
            val title =  "Death Note"
            val a = AnimeRaw(
                _title = title,
                episodes = 37,
            )

            val b = AnimeRaw(
                _title = title,
                episodes = 37,
            )

            // when
            val result = a == b

            // then
            assertThat(result).isTrue()
            assertThat(a.hashCode()).isEqualTo(b.hashCode())
        }

        @Test
        fun `is not equal if episodes are different`() {
            // given
            val title  =  "Death Note"
            val a = AnimeRaw(
                _title =  title,
                episodes = 37,
            )

            val b = AnimeRaw(
                _title =  title,
                episodes = 1,
            )

            // when
            val result = a == b

            // then
            assertThat(result).isFalse()
            assertThat(a.hashCode()).isNotEqualTo(b.hashCode())
        }

        @Test
        fun `is equal if status is the same`() {
            // given
            val title =  "Death Note"
            val a = AnimeRaw(
                _title = title,
                status = FINISHED,
            )

            val b = AnimeRaw(
                _title = title,
                status = FINISHED,
            )

            // when
            val result = a == b

            // then
            assertThat(result).isTrue()
            assertThat(a.hashCode()).isEqualTo(b.hashCode())
        }

        @Test
        fun `is not equal if status is different`() {
            // given
            val title  =  "Death Note"
            val a = AnimeRaw(
                _title =  title,
                status = FINISHED,
            )

            val b = AnimeRaw(
                _title =  title,
                status = UNKNOWN_STATUS,
            )

            // when
            val result = a == b

            // then
            assertThat(result).isFalse()
            assertThat(a.hashCode()).isNotEqualTo(b.hashCode())
        }

        @Test
        fun `is equal if animeSeasons are the same`() {
            // given
            val title =  "Death Note"
            val a = AnimeRaw(
                _title = title,
                animeSeason = AnimeSeason(
                    season = FALL,
                    year = 2006,
                ),
            )

            val b = AnimeRaw(
                _title = title,
                animeSeason = AnimeSeason(
                    season = FALL,
                    year = 2006,
                ),
            )

            // when
            val result = a == b

            // then
            assertThat(result).isTrue()
            assertThat(a.hashCode()).isEqualTo(b.hashCode())
        }

        @Test
        fun `is not equal if animeSeasons are different`() {
            // given
            val title  =  "Death Note"
            val a = AnimeRaw(
                _title =  title,
                animeSeason = AnimeSeason(
                    season = UNDEFINED,
                    year = 2006,
                ),
            )

            val b = AnimeRaw(
                _title =  title,
                animeSeason = AnimeSeason(
                    season = FALL,
                    year = 2006,
                ),
            )

            // when
            val result = a == b

            // then
            assertThat(result).isFalse()
            assertThat(a.hashCode()).isNotEqualTo(b.hashCode())
        }

        @Test
        fun `is equal if pictures are the same`() {
            // given
            val title =  "Death Note"
            val a = AnimeRaw(
                _title = title,
                picture = URI("https://example.org/pic/1.png"),
            )

            val b = AnimeRaw(
                _title = title,
                picture = URI("https://example.org/pic/1.png"),
            )

            // when
            val result = a == b

            // then
            assertThat(result).isTrue()
            assertThat(a.hashCode()).isEqualTo(b.hashCode())
        }

        @Test
        fun `is not equal if pictures are different`() {
            // given
            val title  =  "Death Note"
            val a = AnimeRaw(
                _title =  title,
                picture = URI("https://example.org/pictures/1.png"),
            )

            val b = AnimeRaw(
                _title =  title,
                picture = URI("https://example.org/pic/1.png"),
            )

            // when
            val result = a == b

            // then
            assertThat(result).isFalse()
            assertThat(a.hashCode()).isNotEqualTo(b.hashCode())
        }

        @Test
        fun `is equal if thumbnail are the same`() {
            // given
            val title =  "Death Note"
            val a = AnimeRaw(
                _title = title,
                thumbnail = URI("https://example.org/thumbnail/1.png"),
            )

            val b = AnimeRaw(
                _title = title,
                thumbnail = URI("https://example.org/thumbnail/1.png"),
            )

            // when
            val result = a == b

            // then
            assertThat(result).isTrue()
            assertThat(a.hashCode()).isEqualTo(b.hashCode())
        }

        @Test
        fun `is not equal if thumbnail are different`() {
            // given
            val title  =  "Death Note"
            val a = AnimeRaw(
                _title =  title,
                thumbnail = URI("https://example.org/thumbnails/1.png"),
            )

            val b = AnimeRaw(
                _title =  title,
                thumbnail = URI("https://example.org/thumbnail/1.png"),
            )

            // when
            val result = a == b

            // then
            assertThat(result).isFalse()
            assertThat(a.hashCode()).isNotEqualTo(b.hashCode())
        }

        @Test
        fun `is equal if durations are the same`() {
            // given
            val title =  "Death Note"
            val a = AnimeRaw(
                _title = title,
                duration = Duration(20, MINUTES),
            )

            val b = AnimeRaw(
                _title = title,
                duration = Duration(20, MINUTES),
            )

            // when
            val result = a == b

            // then
            assertThat(result).isTrue()
            assertThat(a.hashCode()).isEqualTo(b.hashCode())
        }

        @Test
        fun `is not equal if durations are different`() {
            // given
            val title  =  "Death Note"
            val a = AnimeRaw(
                _title =  title,
                duration = Duration(21, MINUTES),
            )

            val b = AnimeRaw(
                _title =  title,
                duration = Duration(20, MINUTES),
            )

            // when
            val result = a == b

            // then
            assertThat(result).isFalse()
            assertThat(a.hashCode()).isNotEqualTo(b.hashCode())
        }

        @Test
        fun `is equal if scores are the same`() {
            // given
            val title =  "Death Note"
            val a = AnimeRaw(
                _title = title,
            ).apply {
                addScores(
                    MetaDataProviderScoreValue(
                        hostname = "example.org",
                        value = 4.97,
                        originalRange = 1.0..5.0,
                    ),
                )
            }

            val b = AnimeRaw(
                _title = title,
            ).apply {
                addScores(
                    MetaDataProviderScoreValue(
                        hostname = "example.org",
                        value = 4.97,
                        originalRange = 1.0..5.0,
                    ),
                )
            }

            // when
            val result = a == b

            // then
            assertThat(result).isTrue()
            assertThat(a.hashCode()).isEqualTo(b.hashCode())
        }

        @Test
        fun `is not equal if scores are different`() {
            // given
            val title  =  "Death Note"
            val a = AnimeRaw(
                _title =  title,
            ).apply {
                addScores(
                    MetaDataProviderScoreValue(
                        hostname = "example.org",
                        value = 4.97,
                        originalRange = 1.0..5.0,
                    ),
                )
            }

            val b = AnimeRaw(
                _title =  title,
            ).apply {
                addScores(
                    MetaDataProviderScoreValue(
                        hostname = "example.org",
                        value = 4.97,
                        originalRange = 1.0..5.0,
                    ),
                    MetaDataProviderScoreValue(
                        hostname = "other.com",
                        value = 9.50,
                        originalRange = 1.0..10.0,
                    )
                )
            }

            // when
            val result = a == b

            // then
            assertThat(result).isFalse()
            assertThat(a.hashCode()).isNotEqualTo(b.hashCode())
        }

        @Test
        fun `is equal if related anime are the same`() {
            // given
            val title  =  "Death Note"
            val a = AnimeRaw(
                _title =  title,
                _relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/2994"),
                ),
            )

            val b = AnimeRaw(
                _title =  title,
                _relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/2994"),
                ),
            )

            // when
            val result = a == b

            // then
            assertThat(result).isTrue()
            assertThat(a.hashCode()).isEqualTo(b.hashCode())
        }

        @Test
        fun `is not equal if related anime are different`() {
            // given
            val title  =  "Death Note"
            val a = AnimeRaw(
                _title =  title,
                _relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/2994"),
                ),
            )

            val b = AnimeRaw(
                _title =  title,
                _relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/2994"),
                    URI("http://anilist.co/anime/2994"),
                ),
            )

            // when
            val result = a == b

            // then
            assertThat(result).isFalse()
            assertThat(a.hashCode()).isNotEqualTo(b.hashCode())
        }

        @Test
        fun `is equal if tags are the same`() {
            // given
            val title  =  "Death Note"
            val a = AnimeRaw(
                _title =  title,
                _tags = hashSetOf(
                    "comedy",
                    "slice of life",
                ),
            )

            val b = AnimeRaw(
                _title =  title,
                _tags = hashSetOf(
                    "slice of life",
                    "comedy",
                ),
            )

            // when
            val result = a == b

            // then
            assertThat(result).isTrue()
            assertThat(a.hashCode()).isEqualTo(b.hashCode())
        }

        @Test
        fun `is not equal if tags are different`() {
            // given
            val title  =  "Death Note"
            val a = AnimeRaw(
                _title =  title,
                _tags = hashSetOf(
                    "slice of life",
                ),
            )

            val b = AnimeRaw(
                _title =  title,
                _tags = hashSetOf(
                    "slice of life",
                    "comedy",
                ),
            )

            // when
            val result = a == b

            // then
            assertThat(result).isFalse()
            assertThat(a.hashCode()).isNotEqualTo(b.hashCode())
        }

        @Test
        fun `is not equal if the other object is of a different type`() {
            // given
            val title  =  "Death Note"
            val a = AnimeRaw(
                _title =  title,
                _tags = hashSetOf(
                    "slice of life",
                ),
            )

            // when
            val result = a.equals(1)

            // then
            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class MergeTests {

        @Test
        fun `use this number of episodes` () {
            // given
            val anime = AnimeRaw(
                _title = "this",
                episodes = 12,
            )
            val other = AnimeRaw(
                _title = "other",
                episodes = 13,
            )

            // when
            val result = anime.mergeWith(other)

            // then
            assertThat(result.episodes).isEqualTo(12)
        }

        @Test
        fun `use other's number of episodes if this number of episodes is 0` () {
            // given
            val anime = AnimeRaw(
                _title = "this",
                episodes = 0,
            )
            val other = AnimeRaw(
                _title = "other",
                episodes = 13,
            )

            // when
            val result = anime.mergeWith(other)

            // then
            assertThat(result.episodes).isEqualTo(13)
        }

        @Test
        fun `use this type` () {
            // given
            val anime = AnimeRaw(
                _title = "this",
                type = MOVIE,
            )
            val other = AnimeRaw(
                _title = "other",
                type = SPECIAL,
            )

            // when
            val result = anime.mergeWith(other)

            // then
            assertThat(result.type).isEqualTo(MOVIE)
        }

        @Test
        fun `use other's type if this type is UNKNOWN` () {
            // given
            val anime = AnimeRaw(
                _title = "this",
                type = UNKNOWN_TYPE,
            )
            val other = AnimeRaw(
                _title = "other",
                type = SPECIAL,
            )

            // when
            val result = anime.mergeWith(other)

            // then
            assertThat(result.type).isEqualTo(SPECIAL)
        }

        @Test
        fun `use this status` () {
            // given
            val anime = AnimeRaw(
                _title = "this",
                status = FINISHED,
            )
            val other = AnimeRaw(
                _title = "other",
                status = ONGOING,
            )

            // when
            val result = anime.mergeWith(other)

            // then
            assertThat(result.status).isEqualTo(FINISHED)
        }

        @Test
        fun `use other's status if this status is UNKNOWN` () {
            // given
            val anime = AnimeRaw(
                _title = "this",
                status = UNKNOWN_STATUS,
            )
            val other = AnimeRaw(
                _title = "other",
                status = ONGOING,
            )

            // when
            val result = anime.mergeWith(other)

            // then
            assertThat(result.status).isEqualTo(ONGOING)
        }

        @Test
        fun `use this duration` () {
            // given
            val anime = AnimeRaw(
                _title = "this",
                duration = Duration(120, MINUTES),
            )
            val other = AnimeRaw(
                _title = "other",
                duration = Duration(125, MINUTES),
            )

            // when
            val result = anime.mergeWith(other)

            // then
            assertThat(result.duration).isEqualTo(Duration(120, MINUTES))
        }

        @Test
        fun `use other's duration if this duration is UNKNOWN` () {
            // given
            val anime = AnimeRaw(
                _title = "this",
                duration = UNKNOWN_DURATION,
            )
            val other = AnimeRaw(
                _title = "other",
                duration = Duration(125, MINUTES),
            )

            // when
            val result = anime.mergeWith(other)

            // then
            assertThat(result.duration).isEqualTo(Duration(125, MINUTES))
        }

        @Test
        fun `use this season` () {
            // given
            val anime = AnimeRaw(
                _title = "this",
                animeSeason = AnimeSeason(
                    season = FALL,
                    year = 2010,
                ),
            )
            val other = AnimeRaw(
                _title = "other",
                animeSeason = AnimeSeason(
                    season = WINTER,
                    year = 2011,
                ),
            )

            // when
            val result = anime.mergeWith(other)

            // then
            assertThat(result.animeSeason.season).isEqualTo(FALL)
            assertThat(result.animeSeason.year).isEqualTo(2010)
        }

        @Test
        fun `use other's season if this season is UNDEFINED` () {
            // given
            val anime = AnimeRaw(
                _title = "this",
                animeSeason = AnimeSeason(
                    season = UNDEFINED,
                    year = 2010,
                ),
            )
            val other = AnimeRaw(
                _title = "other",
                animeSeason = AnimeSeason(
                    season = WINTER,
                    year = 2011,
                ),
            )

            // when
            val result = anime.mergeWith(other)

            // then
            assertThat(result.animeSeason.season).isEqualTo(WINTER)
            assertThat(result.animeSeason.year).isEqualTo(2010)
        }

        @Test
        fun `use this year` () {
            // given
            val anime = AnimeRaw(
                _title = "this",
                animeSeason = AnimeSeason(
                    season = FALL,
                    year = 2010,
                ),
            )
            val other = AnimeRaw(
                _title = "other",
                animeSeason = AnimeSeason(
                    season = WINTER,
                    year = 2011,
                ),
            )

            // when
            val result = anime.mergeWith(other)

            // then
            assertThat(result.animeSeason.season).isEqualTo(FALL)
            assertThat(result.animeSeason.year).isEqualTo(2010)
        }

        @Test
        fun `use other's year if this year is UNKNOWN` () {
            // given
            val anime = AnimeRaw(
                _title = "this",
                animeSeason = AnimeSeason(
                    season = FALL,
                    year = UNKNOWN_YEAR,
                ),
            )
            val other = AnimeRaw(
                _title = "other",
                animeSeason = AnimeSeason(
                    season = WINTER,
                    year = 2011,
                ),
            )

            // when
            val result = anime.mergeWith(other)

            // then
            assertThat(result.animeSeason.season).isEqualTo(FALL)
            assertThat(result.animeSeason.year).isEqualTo(2011)
        }

        @Test
        fun `add title and synonyms of the other anime to this anime's synonyms`() {
            // given
            val anime = AnimeRaw(
                _title =  "Death Note",
                _synonyms = hashSetOf(
                    "Caderno da Morte",
                ),
            )

            val other = AnimeRaw(
                _title =  "DEATH NOTE",
                _synonyms = hashSetOf(
                    "Caderno da Morte",
                    "Quaderno della Morte",
                ),
            )

            // when
            val result = anime.mergeWith(other)

            // then
            assertThat(result.title).isEqualTo(anime.title)
            assertThat(result.synonyms).containsExactlyInAnyOrder(
                "Caderno da Morte",
                "DEATH NOTE",
                "Quaderno della Morte",
            )
        }

        @Test
        fun `merge related anime and source links`() {
            // given
            val anime = AnimeRaw(
                _title =  "Death Note",
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/1535"),
                ),
                _relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/2994"),
                ),
            )

            val other = AnimeRaw(
                _title =  "Death Note",
                _sources = hashSetOf(
                    URI("https://anidb.net/anime/4563"),
                ),
                _relatedAnime = hashSetOf(
                    URI("https://anidb.net/anime/8146"),
                    URI("https://anidb.net/anime/8147"),
                ),
            )

            // when
            val result = anime.mergeWith(other)

            // then
            assertThat(result.sources).containsExactlyInAnyOrder(
                URI("https://anidb.net/anime/4563"),
                URI("https://myanimelist.net/anime/1535"),
            )
            assertThat(result.relatedAnime).containsExactlyInAnyOrder(
                URI("https://anidb.net/anime/8146"),
                URI("https://anidb.net/anime/8147"),
                URI("https://myanimelist.net/anime/2994"),
            )
        }

        @Test
        fun `merge tags`() {
            // given
            val anime = AnimeRaw(
                _title =  "Death Note",
                _tags = hashSetOf(
                    "Psychological",
                    "Thriller",
                    "Shounen",
                ),
            )

            val other = AnimeRaw(
                _title =  "Death Note",
                _tags = hashSetOf(
                    "Mystery",
                    "Police",
                    "Psychological",
                    "Supernatural",
                    "Thriller",
                ),
            )

            // when
            val result = anime.mergeWith(other)

            // then
            assertThat(result.tags).containsExactlyInAnyOrder(
                "mystery",
                "police",
                "psychological",
                "shounen",
                "supernatural",
                "thriller",
            )
        }

        @Test
        fun `use other's score if hostname of a score entry has the same hostname`() {
            // given
            val title =  "Death Note"
            val scoreA = MetaDataProviderScoreValue(
                hostname = "example.org",
                value = 4.97,
                originalRange = 1.0..5.0,
            )
            val a = AnimeRaw(
                _title = title,
            ).apply {
                addScores(
                    scoreA,
                )
            }

            val scoreB = MetaDataProviderScoreValue(
                hostname = "other.com",
                value = 9.5,
                originalRange = 1.0..10.0,
            )
            val b = AnimeRaw(
                _title = title,
            ).apply {
                addScores(
                    scoreB,
                )
            }

            // when
            val result = a.mergeWith(b)

            // then
            assertThat(result.scores).containsExactlyInAnyOrder(
                scoreA,
                scoreB,
            )
        }

        @Test
        fun `overrides entries with same hostname`() {
            // given
            val title =  "Death Note"
            val scoreA = MetaDataProviderScoreValue(
                hostname = "example.org",
                value = 2.41,
                originalRange = 1.0..5.0,
            )
            val a = AnimeRaw(
                _title = title,
            ).apply {
                addScores(
                    scoreA,
                )
            }

            val scoreB = MetaDataProviderScoreValue(
                hostname = "example.org",
                value = 4.97,
                originalRange = 1.0..5.0,
            )
            val b = AnimeRaw(
                _title = title,
            ).apply {
                addScores(
                    scoreB,
                )
            }

            // when
            val result = a.mergeWith(b)

            // then
            assertThat(result.scores).containsExactlyInAnyOrder(
                scoreB,
            )
        }
    }

    @Nested
    inner class TagsTest {

        @Nested
        inner class AddTagsConstructorTests {

            @Test
            fun `ensure that you cannot directly modify the internal hashset`() {
                // given
                val anime = AnimeRaw(
                    _title = "test",
                    _tags = hashSetOf(
                        "thriller",
                        "acion",
                    ),
                )

                // when
                anime.tags.clear()

                // then
                assertThat(anime.tags).isNotEmpty()
            }

            @Test
            fun `tags added by constructor are set to lower case`() {
                // given
                val tag = "EXAMPLE"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _tags = hashSetOf(
                        tag,
                    ),
                )

                // then
                assertThat(result.tags).containsExactly(
                    tag.lowercase(),
                )
            }

            @Test
            fun `remove leading whitespace from title`() {
                // given
                val expectedTag = "example"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _tags = hashSetOf(
                        " $expectedTag",
                    ),
                )

                // then
                assertThat(result.tags).containsExactly(
                    expectedTag,
                )
            }

            @Test
            fun `remove tailing whitespace from title`() {
                // given
                val expectedTag = "example"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _tags = hashSetOf(
                        "$expectedTag ",
                    ),
                )

                // then
                assertThat(result.tags).containsExactly(
                    expectedTag,
                )
            }

            @Test
            fun `replace multiple whitespaces with a single whitespace in title`() {
                // given
                val expectedTag = "slice of life"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _tags = hashSetOf(
                        "slice     of      life",
                    ),
                )

                // then
                assertThat(result.tags).containsExactly(
                    expectedTag,
                )
            }

            @Test
            fun `replace tab character with whitespace in title`() {
                // given
                val expectedTag = "slice of life"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _tags = hashSetOf(
                        "slice\tof\tlife",
                    ),
                )

                // then
                assertThat(result.tags).containsExactly(
                    expectedTag,
                )
            }

            @Test
            fun `replace line feed character with whitespace in title`() {
                // given
                val expectedTag = "slice of life"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _tags = hashSetOf(
                        "slice\nof\nlife",
                    ),
                )

                // then
                assertThat(result.tags).containsExactly(
                    expectedTag,
                )
            }

            @Test
            fun `replace carriage return line feed with whitespace in title`() {
                // given
                val expectedTag = "slice of life"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _tags = hashSetOf(
                        "slice\r\nof\r\nlife",
                    ),
                )

                // then
                assertThat(result.tags).containsExactly(
                    expectedTag,
                )
            }

            @Test
            fun `don't add tag if it's an empty string`() {
                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _tags = hashSetOf(
                        EMPTY,
                    ),
                )

                // then
                assertThat(result.tags).isEmpty()
            }

            @Test
            fun `don't add tag if it's a blank string`() {
                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _tags = hashSetOf(
                        "     ",
                    ),
                )

                // then
                assertThat(result.tags).isEmpty()
            }

            @Test
            fun `tags is a distinct list`() {
                // given
                val tag1 = "a tag"
                val tag2 = "before the other"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _tags = hashSetOf(
                        tag2,
                        tag1,
                        tag1,
                        tag2,
                    ),
                )

                // then
                assertThat(result.tags).hasSize(2)
                assertThat(result.tags).containsExactlyInAnyOrder(
                    tag1,
                    tag2,
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [" Death Note", "Death Note ", "  Death   Note  ", "DEATH NOTE", "", " ", "    ", "\u200C"])
            fun `doesn't fix tags if activateChecks is false`(value: String) {
                // when
                val obj = AnimeRaw(
                    _title = "デスノート",
                    _tags = hashSetOf(
                        value,
                    ),
                    activateChecks = false,
                )

                // then
                assertThat(obj.tags).containsExactlyInAnyOrder(
                    value,
                )
            }
        }

        @Nested
        inner class AddTagsTests {

            @Test
            fun `tags added are set to lower case`() {
                // given
                val tag = "EXAMPLE"
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(listOf(
                    tag,
                ))

                // then
                assertThat(anime.tags).containsExactly(
                    tag.lowercase(),
                )
            }

            @Test
            fun `remove leading whitespace from title`() {
                // given
                val expectedTag = "example"
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(listOf(
                    " $expectedTag",
                ))

                // then
                assertThat(anime.tags).containsExactly(
                    expectedTag,
                )
            }

            @Test
            fun `remove tailing whitespace from title`() {
                // given
                val expectedTag = "example"
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(listOf(
                    "$expectedTag ",
                ))

                // then
                assertThat(anime.tags).containsExactly(
                    expectedTag,
                )
            }

            @Test
            fun `replace multiple whitespaces with a single whitespace in title`() {
                // given
                val expectedTag = "slice of life"
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(listOf(
                    "slice     of      life",
                ))

                // then
                assertThat(anime.tags).containsExactly(
                    expectedTag,
                )
            }

            @Test
            fun `replace tab character with whitespace in title`() {
                // given
                val expectedTag = "slice of life"
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(listOf(
                    "slice\tof\tlife",
                ))

                // then
                assertThat(anime.tags).containsExactly(
                    expectedTag,
                )
            }

            @Test
            fun `replace line feed character with whitespace in title`() {
                // given
                val expectedTag = "slice of life"
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(listOf(
                    "slice\nof\nlife",
                ))

                // then
                assertThat(anime.tags).containsExactly(
                    expectedTag,
                )
            }

            @Test
            fun `replace carriage return line feed with whitespace in title`() {
                // given
                val expectedTag = "slice of life"
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(listOf(
                    "slice\r\nof\r\nlife",
                ))

                // then
                assertThat(anime.tags).containsExactly(
                    expectedTag,
                )
            }

            @Test
            fun `don't add tag if it's an empty string`() {
                // given
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(listOf(
                    EMPTY,
                ))

                // then
                assertThat(anime.tags).isEmpty()
            }

            @Test
            fun `don't add tag if it's a blank string`() {
                // given
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(listOf(
                    "     ",
                ))

                // then
                assertThat(anime.tags).isEmpty()
            }

            @Test
            fun `tags is a distinct list`() {
                // given
                val tag1 = "a tag"
                val tag2 = "before the other"
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(listOf(
                    tag2,
                    tag1,
                    tag1,
                    tag2,
                ))

                // then
                assertThat(anime.tags).hasSize(2)
                assertThat(anime.tags).containsExactlyInAnyOrder(
                    tag1,
                    tag2,
                )
            }
        }

        @Nested
        inner class AddTagsVarargTests {

            @Test
            fun `tags added are set to lower case`() {
                // given
                val tag = "EXAMPLE"
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(tag)

                // then
                assertThat(anime.tags).containsExactly(
                    tag.lowercase(),
                )
            }

            @Test
            fun `remove leading whitespace from title`() {
                // given
                val expectedTag = "example"
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(
                    " $expectedTag",
                )

                // then
                assertThat(anime.tags).containsExactly(
                    expectedTag,
                )
            }

            @Test
            fun `remove tailing whitespace from title`() {
                // given
                val expectedTag = "example"
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(
                    "$expectedTag ",
                )

                // then
                assertThat(anime.tags).containsExactly(
                    expectedTag,
                )
            }

            @Test
            fun `replace multiple whitespaces with a single whitespace in title`() {
                // given
                val expectedTag = "slice of life"
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(
                    "slice     of      life",
                )

                // then
                assertThat(anime.tags).containsExactly(
                    expectedTag,
                )
            }

            @Test
            fun `replace tab character with whitespace in title`() {
                // given
                val expectedTag = "slice of life"
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(
                    "slice\tof\tlife",
                )

                // then
                assertThat(anime.tags).containsExactly(
                    expectedTag,
                )
            }

            @Test
            fun `replace line feed character with whitespace in title`() {
                // given
                val expectedTag = "slice of life"
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(
                    "slice\nof\nlife",
                )

                // then
                assertThat(anime.tags).containsExactly(
                    expectedTag,
                )
            }

            @Test
            fun `replace carriage return line feed with whitespace in title`() {
                // given
                val expectedTag = "slice of life"
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(
                    "slice\r\nof\r\nlife",
                )

                // then
                assertThat(anime.tags).containsExactly(
                    expectedTag,
                )
            }

            @Test
            fun `don't add tag if it's an empty string`() {
                // given
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(
                    EMPTY,
                )

                // then
                assertThat(anime.tags).isEmpty()
            }

            @Test
            fun `don't add tag if it's a blank string`() {
                // given
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(
                    "     ",
                )

                // then
                assertThat(anime.tags).isEmpty()
            }

            @Test
            fun `tags is a distinct list`() {
                // given
                val tag1 = "a tag"
                val tag2 = "before the other"
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(
                    tag2,
                    tag1,
                    tag1,
                    tag2,
                )

                // then
                assertThat(anime.tags).hasSize(2)
                assertThat(anime.tags).containsExactlyInAnyOrder(
                    tag1,
                    tag2,
                )
            }
        }
    }

    @Nested
    inner class DurationTests {

        @Test
        fun `default duration is 0 seconds`() {
            // given
            val expectedDuration = Duration(0, SECONDS)

            // when
            val result = AnimeRaw("Death Note")

            // then
            assertThat(result.duration).isEqualTo(expectedDuration)
        }

        @Test
        fun `setting a duration of 10 seconds`() {
            // when
            val result = Duration(10, SECONDS)

            // then
            assertThat(result.duration).isEqualTo(10)
            assertThat(result.toString()).isEqualTo("10 seconds")
        }

        @Test
        fun `duration of a minute is equal to a duration of 60 seconds`() {
            // given
            val durationInSeconds = Duration(60, SECONDS)

            // when
            val result = Duration(1, MINUTES)

            // then
            assertThat(result).isEqualTo(durationInSeconds)
            assertThat(result.duration).isEqualTo(60)
            assertThat(result.toString()).isEqualTo("60 seconds")
        }

        @Test
        fun `duration of an hour is equal to a duration of 60 minutes`() {
            // given
            val durationInMinutes = Duration(60, MINUTES)

            // when
            val result = Duration(1, HOURS)

            // then
            assertThat(result).isEqualTo(durationInMinutes)
            assertThat(result.duration).isEqualTo(3600)
            assertThat(result.toString()).isEqualTo("3600 seconds")
        }

        @Test
        fun `returns false on equals check if other object is not of the same type`() {
            // given
            val duration = Duration(60, MINUTES)

            // when
            val result = duration.equals(EMPTY)

            // then
            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class AnimeSeasonTests {

        @Test
        fun `default year is 0 indicating unknown and season is undefined`() {
            // when
            val result = AnimeRaw("Death Note")

            // then
            assertThat(result.animeSeason.year).isZero()
            assertThat(result.animeSeason.isYearOfPremiereUnknown()).isTrue()
            assertThat(result.animeSeason.season).isEqualTo(UNDEFINED)
        }
    }

    @Nested
    inner class SeasonTests {

        @Test
        fun `default season if nothing has been set`() {
            // when
            val result = AnimeRaw("test")

            // then
            assertThat(result.animeSeason.season).isEqualTo(UNDEFINED)
            assertThat(result.animeSeason.year).isZero()
        }
    }

    @Nested
    inner class ToStringTests {

        @Test
        fun `create formatted string listing all properties`() {
            // given
            val anime = AnimeRaw(
                _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                _sources = hashSetOf(
                    URI("https://myanimelist.net/anime/6351"),
                ),
                _relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/2167"),
                ),
                type = SPECIAL,
                episodes = 1,
                status = FINISHED,
                animeSeason = AnimeSeason(
                    season = SUMMER,
                    year = 2009
                ),
                picture = URI("https://cdn.myanimelist.net/images/anime/10/19621.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/19621t.jpg"),
                duration = Duration(2, MINUTES),
                _synonyms = hashSetOf(
                    "Clannad ~After Story~: Another World, Kyou Chapter",
                    "Clannad: After Story OVA",
                    "クラナド　アフターストーリー　もうひとつの世界　杏編",
                ),
                _tags = hashSetOf(
                    "comedy",
                    "drama",
                    "romance",
                    "school",
                    "slice of life",
                    "supernatural",
                ),
            ).apply {
                addScores(
                    MetaDataProviderScoreValue(
                        hostname = "myanimelist.net",
                        value = 7.77,
                        originalRange = 1.0..10.0,
                    ),
                )
            }

            // when
            val result = anime.toString()

            // then
            assertThat(result).isEqualTo(
                """
                    Anime(
                      sources = [https://myanimelist.net/anime/6351]
                      title = Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen
                      type = SPECIAL
                      episodes = 1
                      status = FINISHED
                      animeSeason = AnimeSeason(season=SUMMER, year=2009)
                      picture = https://cdn.myanimelist.net/images/anime/10/19621.jpg
                      thumbnail = https://cdn.myanimelist.net/images/anime/10/19621t.jpg
                      duration = 120 seconds
                      scores = [MetaDataProviderScoreValue(hostname=myanimelist.net, value=7.77, originalRange=1.0..10.0)]
                      synonyms = [Clannad ~After Story~: Another World, Kyou Chapter, Clannad: After Story OVA, クラナド　アフターストーリー　もうひとつの世界　杏編]
                      relatedAnime = [https://myanimelist.net/anime/2167]
                      tags = [comedy, drama, romance, school, slice of life, supernatural]
                    )
                """.trimIndent()
            )
        }
    }

    @Nested
    inner class EpisodesTests {

        @Test
        fun `default value is 0`() {
            // when
            val result = AnimeRaw("test")

            // then
            assertThat(result.episodes).isZero()
        }

        @Test
        fun `throws exception if number of episodes is negative`() {
            // when
            val result = assertThrows<IllegalArgumentException> {
                AnimeRaw(
                    _title = "test",
                    episodes = -1,
                )
            }

            // then
            assertThat(result).hasMessage("Episodes cannot have a negative value.")
        }

        @Test
        fun `doesn't throw an exception if number of episodes is negative and activateChecks is false`() {
            // when
            val result = AnimeRaw(
                _title = "test",
                episodes = -1,
                activateChecks = false,
            )

            // then
            assertThat(result.episodes).isEqualTo(-1)
        }
    }

    @Nested
    inner class PerformChecksTests {

        @Nested
        inner class TitleTests {

            @ParameterizedTest
            @ValueSource(strings = [" Death Note", "Death Note ", "  Death   Note  "])
            fun `fixes title`(value: String) {
                // given
                val obj = AnimeRaw(
                    _title = value,
                    activateChecks = false,
                )

                val expected = AnimeRaw(
                    _title = "Death Note",
                    activateChecks = false,
                )

                // when
                obj.performChecks()

                // then
                assertThat(obj).isEqualTo(expected)
            }
        }

        @Nested
        inner class EpisodeTests {

            @Test
            fun `throws exception if number of episodes is negative`() {
                val anime = AnimeRaw(
                    _title = "test",
                    episodes = -1,
                    activateChecks = false,
                )

                // when
                val result = assertThrows<IllegalArgumentException> {
                    anime.performChecks()
                }

                // then
                assertThat(result).hasMessage("Episodes cannot have a negative value.")
            }
        }

        @Nested
        inner class RelatedAnimeTests {

            @Test
            fun `removes sources from relatedAnime`() {
                // given
                val adapter = AnimeRawAdapter().indent("  ")
                val obj = AnimeRaw(
                    _title = "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                    _sources = hashSetOf(
                        URI("https://myanimelist.net/anime/6351"),
                    ),
                    _relatedAnime = hashSetOf(
                        URI("https://myanimelist.net/anime/2167"),
                        URI("https://myanimelist.net/anime/6351"),
                    ),
                    activateChecks = false,
                )

                // when
                val result = adapter.toJson(obj)

                // then
                assertThat(result).isEqualTo("""
                    {
                      "sources": [
                        "https://myanimelist.net/anime/6351"
                      ],
                      "title": "Clannad: After Story - Mou Hitotsu no Sekai, Kyou-hen",
                      "type": "UNKNOWN",
                      "episodes": 0,
                      "status": "UNKNOWN",
                      "animeSeason": {
                        "season": "UNDEFINED"
                      },
                      "picture": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png",
                      "thumbnail": "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png",
                      "scores": [],
                      "synonyms": [],
                      "relatedAnime": [
                        "https://myanimelist.net/anime/2167"
                      ],
                      "tags": []
                    }
                """.trimIndent())
            }
        }

        @Nested
        inner class SynonymsTests {

            @ParameterizedTest
            @ValueSource(strings = [" Death Note", "Death Note ", "  Death   Note  "])
            fun `fixes synonyms`(value: String) {
                // given
                val obj = AnimeRaw(
                    _title = "デスノート",
                    _synonyms = hashSetOf(
                        value,
                    ),
                    activateChecks = false,
                )

                val expected = AnimeRaw(
                    _title = "デスノート",
                    _synonyms = hashSetOf(
                        "Death Note",
                    ),
                    activateChecks = false,
                )

                // when
                obj.performChecks()

                // then
                assertThat(obj).isEqualTo(expected)
            }

            @ParameterizedTest
            @ValueSource(strings = ["", " ", "    ", "\u200C"])
            fun `removes blank entries from synonyms`(value: String) {
                // given
                val obj = AnimeRaw(
                    _title = "デスノート",
                    _synonyms = hashSetOf(
                        value,
                    ),
                    activateChecks = false,
                )

                val expected = AnimeRaw(
                    _title = "デスノート",
                    _synonyms = HashSet(),
                    activateChecks = false,
                )

                // when
                obj.performChecks()

                // then
                assertThat(obj).isEqualTo(expected)
            }
        }

        @Nested
        inner class TagsTests {

            @ParameterizedTest
            @ValueSource(strings = [" Death Note", "Death Note ", "  Death   Note  ", "DEATH NOTE"])
            fun `fixes tags`(value: String) {
                // given
                val obj = AnimeRaw(
                    _title = "デスノート",
                    _tags = hashSetOf(
                        value,
                    ),
                    activateChecks = false,
                )

                val expected = AnimeRaw(
                    _title = "デスノート",
                    _tags = hashSetOf(
                        "death note",
                    ),
                    activateChecks = false,
                )

                // when
                obj.performChecks()

                // then
                assertThat(obj).isEqualTo(expected)
            }

            @ParameterizedTest
            @ValueSource(strings = ["", " ", "    ", "\u200C"])
            fun `removes blank entries from tags`(value: String) {
                // given
                val obj = AnimeRaw(
                    _title = "デスノート",
                    _tags = hashSetOf(
                        value,
                    ),
                    activateChecks = false,
                )

                val expected = AnimeRaw(
                    _title = "デスノート",
                    _tags = HashSet(),
                    activateChecks = false,
                )

                // when
                obj.performChecks()

                // then
                assertThat(obj).isEqualTo(expected)
            }
        }

        @Nested
        inner class SourcesTests {

            @Test
            fun `remove related anime if the same uri has been added to sources`() {
                // given
                val source = URI("https://myanimelist.net/anime/1535")
                val anime = AnimeRaw(
                    _title =  "Death Note",
                    _sources = hashSetOf(
                        source,
                    ),
                    _relatedAnime = hashSetOf(
                        source,
                    ),
                    activateChecks = false,
                )

                // when
                anime.performChecks()

                // then
                assertThat(anime.sources).containsExactly(source)
                assertThat(anime.relatedAnime).isEmpty()
            }
        }
    }

    @Nested
    inner class ScoresTests {

        @Test
        fun `default is empty`() {
            // when
            val result = AnimeRaw("Death Note")

            // then
            assertThat(result.scores).isEmpty()
        }

        @Test
        fun `ensure that you cannot directly modify the internal hashset`() {
            // given
            val anime = AnimeRaw(
                _title = "test",
            ).apply {
                addScores(
                    MetaDataProviderScoreValue(
                        hostname = "example.org",
                        value = 4.97,
                        originalRange = 1.0..5.0,
                    ),
                )
            }

            // when
            anime.scores.clear()

            // then
            assertThat(anime.scores).isNotEmpty()
        }

        @Nested
        inner class AddTagsTests {

            @Test
            fun `ignores NoMetaDataProviderScore`() {
                // given
                val anime = AnimeRaw(
                    _title = "Death Note",
                )

                // when
                val result = anime.addScores(
                    NoMetaDataProviderScore,
                )

                // then
                assertThat(result.scores).isEmpty()
            }

            @Test
            fun `correctly adds score`() {
                // given
                val score = MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 4.97,
                    originalRange = 1.0..5.0,
                )
                val anime = AnimeRaw(
                    _title = "test",
                )

                // when
                val result = anime.addScores(
                    score,
                )

                // then
                assertThat(result.scores).containsExactlyInAnyOrder(
                    score,
                )
            }

            @Test
            fun `correctly adds multiple scores`() {
                // given
                val scoreA = MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 4.97,
                    originalRange = 1.0..5.0,
                )
                val scoreB = MetaDataProviderScoreValue(
                    hostname = "other.com",
                    value = 6.83,
                    originalRange = 1.0..10.0,
                )
                val anime = AnimeRaw(
                    _title = "test",
                )

                // when
                val result = anime.addScores(
                    scoreA,
                    scoreB,
                )

                // then
                assertThat(result.scores).containsExactlyInAnyOrder(
                    scoreA,
                    scoreB,
                )
            }

            @Test
            fun `overrides existing score if the hostname is identical`() {
                // given
                val score = MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 4.97,
                    originalRange = 1.0..5.0,
                )
                val anime = AnimeRaw(
                    _title = "test",
                ).apply {
                    addScores(
                        MetaDataProviderScoreValue(
                            hostname = "example.org",
                            value = 2.54,
                            originalRange = 1.0..5.0,
                        ),
                    )
                }

                // when
                val result = anime.addScores(
                    score,
                )

                // then
                assertThat(result.scores).containsExactlyInAnyOrder(
                    score,
                )
            }
        }

        @Nested
        inner class AddTagsVarargTests {

            @Test
            fun `ignores NoMetaDataProviderScore`() {
                // given
                val anime = AnimeRaw(
                    _title = "Death Note",
                )

                // when
                val result = anime.addScores(
                    listOf(
                        NoMetaDataProviderScore,
                    )
                )

                // then
                assertThat(result.scores).isEmpty()
            }

            @Test
            fun `correctly adds score`() {
                // given
                val score = MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 4.97,
                    originalRange = 1.0..5.0,
                )
                val anime = AnimeRaw(
                    _title = "test",
                )

                // when
                val result = anime.addScores(
                    listOf(
                        score,
                    )
                )

                // then
                assertThat(result.scores).containsExactlyInAnyOrder(
                    score,
                )
            }

            @Test
            fun `correctly adds multiple scores`() {
                // given
                val scoreA = MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 4.97,
                    originalRange = 1.0..5.0,
                )
                val scoreB = MetaDataProviderScoreValue(
                    hostname = "other.com",
                    value = 6.83,
                    originalRange = 1.0..10.0,
                )
                val anime = AnimeRaw(
                    _title = "test",
                )

                // when
                val result = anime.addScores(
                    listOf(
                        scoreA,
                        scoreB,
                    )
                )

                // then
                assertThat(result.scores).containsExactlyInAnyOrder(
                    scoreA,
                    scoreB,
                )
            }

            @Test
            fun `overrides existing score if the hostname is identical`() {
                // given
                val score = MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 4.97,
                    originalRange = 1.0..5.0,
                )
                val anime = AnimeRaw(
                    _title = "test",
                ).apply {
                    addScores(
                        listOf(
                            MetaDataProviderScoreValue(
                                hostname = "example.org",
                                value = 2.54,
                                originalRange = 1.0..5.0,
                            ),
                        )
                    )
                }

                // when
                val result = anime.addScores(
                    score,
                )

                // then
                assertThat(result.scores).containsExactlyInAnyOrder(
                    score,
                )
            }
        }
    }
}