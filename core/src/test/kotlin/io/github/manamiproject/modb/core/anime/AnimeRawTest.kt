package io.github.manamiproject.modb.core.anime

import io.github.manamiproject.modb.core.TestAnimeRawObjects
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

        @ParameterizedTest
        @ValueSource(strings = [
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
        ])
        fun `normalize visible whitespaces to a normale whitespace`(input: String) {
            // given
            val expectedTitle = "Death Note"

            // when
            val result = AnimeRaw("Death${input}Note")

            // then
            assertThat(result.title).isEqualTo(expectedTitle)
        }

        @ParameterizedTest
        @ValueSource(strings = [
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
        ])
        fun `remove non-visible whitespaces`(input: String) {
            // when
            val result = AnimeRaw("Ba${input}ek")

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
        fun `throws exception if title is blank`(value: String) {
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
            fun `must not add blank synonym`(input: String) {
                // when
                val result = AnimeRaw(
                    _title = "Death Note",
                    _synonyms = hashSetOf(
                        "$input $input",
                    ),
                )

                // then
                assertThat(result.synonyms).isEmpty()
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `normalize visible whitespaces to a normale whitespace`(input: String) {
                // when
                val result = AnimeRaw(
                    _title = "Death Note",
                    _synonyms = hashSetOf(
                        "Caderno${input}da${input}Morte",
                    ),
                )

                // then
                assertThat(result.synonyms).containsExactly(
                    "Caderno da Morte",
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `remove non-visible whitespaces`(input: String) {
                // when
                val result = AnimeRaw(
                    _title = "Death Note",
                    _synonyms = hashSetOf(
                        "Cader${input}no da Mor${input}te",
                    ),
                )

                // then
                assertThat(result.synonyms).containsExactly(
                    "Caderno da Morte",
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

            @ParameterizedTest
            @ValueSource(strings = [
                "\u02BB",
                "\u02BC",
                "\u2018",
                "\u275B",
                "\u275C",
                "\u02B9",
                "\u02BE",
                "\u02C8",
                "\u055A",
                "\u07F4",
                "\u07F5",
                "\u1FBF",
                "\u2032",
                "\uA78C",
                "\uFF07",
                "\u0060",
            ])
            fun `correctly normalized usages of apostrophe`(value: String) {
                // when
                val anime = AnimeRaw(
                    _title = "Main title",
                    _synonyms = hashSetOf(
                      "she${value}s, John${value}s, it${value}s",
                      "the girls$value, the dogs${value}",
                      "don${value}t, can${value}t",
                      "I${value}m",
                      "I${value}d, they${value}d",
                      "rock ${value}n$value roll",
                      "he${value}ll, we${value}ll",
                      "your${value}re",
                      "I${value}ve",
                      "${value}ere",
                      "${value}cause",
                      "${value}bout",
                      "${value}fore",
                    ),
                )

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    "she\u2019s, John\u2019s, it\u2019s",
                    "the girls\u2019, the dogs\u2019",
                    "don\u2019t, can\u2019t",
                    "I\u2019m",
                    "I\u2019d, they\u2019d",
                    "rock \u2019n\u2019 roll",
                    "he\u2019ll, we\u2019ll",
                    "your\u2019re",
                    "I\u2019ve",
                    "\u2019ere",
                    "\u2019cause",
                    "\u2019bout",
                    "\u2019fore",
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
                "\u2019",
                "\u0027",
            ])
            fun `doesn't normalize acceptable apostrophes`(value: String) {
                // when
                val anime = AnimeRaw(
                    _title = "Main title",
                    _synonyms = hashSetOf(
                        "she${value}s, John${value}s, it${value}s",
                        "the girls$value, the dogs${value}",
                        "don${value}t, can${value}t",
                        "I${value}m",
                        "I${value}d, they${value}d",
                        "rock ${value}n$value roll",
                        "he${value}ll, we${value}ll",
                        "your${value}re",
                        "I${value}ve",
                        "${value}ere",
                        "${value}cause",
                        "${value}bout",
                        "${value}fore",
                    ),
                )

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    "she${value}s, John${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "I${value}m",
                    "I${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "I${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
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
            fun `must not add blank synonym`(input: String) {
                // given
                val anime = AnimeRaw(
                    _title = "Death Note",
                )

                // when
                anime.addSynonyms(listOf(
                    "$input $input",
                ))

                // then
                assertThat(anime.synonyms).isEmpty()
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `normalize visible whitespaces to a normale whitespace`(input: String) {
                // given
                val expectedTitleOne = "Death Note"
                val expectedTitleTwo = "Caderno da Morte"
                val anime = AnimeRaw("Title")

                // when
                anime.addSynonyms(listOf(
                    "Death${input}Note",
                    "Caderno${input}da${input}Morte",
                ))

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `remove non-visible whitespaces`(input: String) {
                // given
                val expectedTitleOne = "Death Note"
                val expectedTitleTwo = "Caderno da Morte"
                val anime = AnimeRaw("Title")

                // when
                anime.addSynonyms(listOf(
                    "Death Note",
                    "Cader${input}no da Mor${input}te",
                ))

                // then
                assertThat(anime.synonyms).containsExactlyInAnyOrder(
                    expectedTitleOne,
                    expectedTitleTwo,
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

            @ParameterizedTest
            @ValueSource(strings = [
                "\u02BB",
                "\u02BC",
                "\u2018",
                "\u275B",
                "\u275C",
                "\u02B9",
                "\u02BE",
                "\u02C8",
                "\u055A",
                "\u07F4",
                "\u07F5",
                "\u1FBF",
                "\u2032",
                "\uA78C",
                "\uFF07",
                "\u0060",
            ])
            fun `normalizes any combination of preceding apostrophe followed by s or s followed by apostrophe`(value: String) {
                // given
                val anime = AnimeRaw(
                    _title = "Main title",
                )

                // when
                val result = anime.addSynonyms(listOf(
                    "she${value}s, John${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "I${value}m",
                    "I${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "I${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                ))

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "she\u2019s, John\u2019s, it\u2019s",
                    "the girls\u2019, the dogs\u2019",
                    "don\u2019t, can\u2019t",
                    "I\u2019m",
                    "I\u2019d, they\u2019d",
                    "rock \u2019n\u2019 roll",
                    "he\u2019ll, we\u2019ll",
                    "your\u2019re",
                    "I\u2019ve",
                    "\u2019ere",
                    "\u2019cause",
                    "\u2019bout",
                    "\u2019fore",
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
                "\u2019",
                "\u0027",
            ])
            fun `doesn't normalize acceptable apostrophes`(value: String) {
                // given
                val anime = AnimeRaw(
                    _title = "Main title",
                )

                // when
                val result = anime.addSynonyms(listOf(
                    "she${value}s, John${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "I${value}m",
                    "I${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "I${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                ))

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "she${value}s, John${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "I${value}m",
                    "I${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "I${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
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
            fun `must not add blank synonym`(input: String) {
                // given
                val anime = AnimeRaw("Death Note")

                // when
                anime.addSynonyms(
                    "$input $input",
                )

                // then
                assertThat(anime.synonyms).isEmpty()
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `normalize visible whitespaces to a normale whitespace`(input: String) {
                // given
                val anime = AnimeRaw("Death Note")

                // when
                anime.addSynonyms(
                    "Caderno${input}da${input}Morte",
                )

                // then
                assertThat(anime.synonyms).containsExactly(
                    "Caderno da Morte",
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `remove non-visible whitespaces`(input: String) {
                // given
                val anime = AnimeRaw("Death Note")

                // when
                anime.addSynonyms(
                    "Cader${input}no da Mor${input}te",
                )

                // then
                assertThat(anime.synonyms).containsExactly(
                    "Caderno da Morte",
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

            @ParameterizedTest
            @ValueSource(strings = [
                "\u02BB",
                "\u02BC",
                "\u2018",
                "\u275B",
                "\u275C",
                "\u02B9",
                "\u02BE",
                "\u02C8",
                "\u055A",
                "\u07F4",
                "\u07F5",
                "\u1FBF",
                "\u2032",
                "\uA78C",
                "\uFF07",
                "\u0060",
            ])
            fun `normalizes any combination of preceding apostrophe followed by s or s followed by apostrophe`(value: String) {
                // given
                val anime = AnimeRaw(
                    _title = "Main title",
                )

                // when
                val result = anime.addSynonyms(
                    "she${value}s, John${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "I${value}m",
                    "I${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "I${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                )

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "she\u2019s, John\u2019s, it\u2019s",
                    "the girls\u2019, the dogs\u2019",
                    "don\u2019t, can\u2019t",
                    "I\u2019m",
                    "I\u2019d, they\u2019d",
                    "rock \u2019n\u2019 roll",
                    "he\u2019ll, we\u2019ll",
                    "your\u2019re",
                    "I\u2019ve",
                    "\u2019ere",
                    "\u2019cause",
                    "\u2019bout",
                    "\u2019fore",
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
                "\u2019",
                "\u0027",
            ])
            fun `doesn't normalize acceptable apostrophes`(value: String) {
                // given
                val anime = AnimeRaw(
                    _title = "Main title",
                )

                // when
                val result = anime.addSynonyms(
                    "she${value}s, John${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "I${value}m",
                    "I${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "I${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                )

                // then
                assertThat(result.synonyms).containsExactlyInAnyOrder(
                    "she${value}s, John${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "I${value}m",
                    "I${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "I${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                )
            }
        }
    }

    @Nested
    inner class StudiosTest {

        @Test
        fun `ensure that you cannot directly modify the internal hashset`() {
            // given
            val anime = AnimeRaw(
                _title = "test",
                _studios = hashSetOf(
                    "studio 1",
                    "studio 2",
                ),
            )

            // when
            anime.studios.clear()

            // then
            assertThat(anime.studios).isNotEmpty()
        }

        @Nested
        inner class AddStudiosConstructorTests {

            @Test
            fun `studios added by constructor are set to lower case`() {
                // given
                val studio = "EXAMPLE"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        studio,
                    ),
                )

                // then
                assertThat(result.studios).containsExactly(
                    studio.lowercase(),
                )
            }

            @Test
            fun `remove leading whitespace from studios`() {
                // given
                val expectedStudio = "example"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        " $expectedStudio",
                    ),
                )

                // then
                assertThat(result.studios).containsExactly(
                    expectedStudio,
                )
            }

            @Test
            fun `remove tailing whitespace from studios`() {
                // given
                val expectedStudio = "example"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        "$expectedStudio ",
                    ),
                )

                // then
                assertThat(result.studios).containsExactly(
                    expectedStudio,
                )
            }

            @Test
            fun `replace multiple whitespaces with a single whitespace in studios`() {
                // given
                val expectedStudio = "bibury animation studios"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        "bibury       animation     studios",
                    ),
                )

                // then
                assertThat(result.studios).containsExactly(
                    expectedStudio,
                )
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
            fun `don't add studios if it's a blank string`(input: String) {
                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        "$input $input",
                    ),
                )

                // then
                assertThat(result.studios).isEmpty()
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `normalize visible whitespaces to a normale whitespace`(input: String) {
                // given
                val expectedStudio = "bibury animation studios"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        "bibury${input}animation${input}studios",
                    ),
                )

                // then
                assertThat(result.studios).containsExactly(
                    expectedStudio,
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `remove non-visible whitespaces`(input: String) {
                // given
                val expectedStudio = "bibury animation studios"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        "bi${input}bury ani${input}mation stu${input}dios",
                    ),
                )

                // then
                assertThat(result.studios).containsExactly(
                    expectedStudio,
                )
            }

            @Test
            fun `correctly adds a single value`() {
                // given
                val studio = "madhouse"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        studio,
                    ),
                )

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    studio,
                )
            }

            @Test
            fun `does nothing in case of an empty list`() {
                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(),
                )

                // then
                assertThat(result.studios).isEmpty()
            }

            @Test
            fun `prevent duplicates`() {
                // given
                val studio1 = "a studio"
                val studio2 = "before the other"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        studio2,
                        studio1,
                        studio1,
                        studio2,
                    ),
                )

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                )
            }

            @Test
            fun `prevent duplicates between longer and shorter versions of the same studio name - same prefix`() {
                // given
                val studio1 = "satelight"
                val studio2 = "satelight inc."

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        studio2,
                        studio1,
                    ),
                )

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    studio2,
                )
            }

            @Test
            fun `prevent duplicates between longer and shorter versions of the same studio name - same suffix`() {
                // given
                val studio1 = "gallop"
                val studio2 = "studio gallop"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        studio2,
                        studio1,
                    ),
                )

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    studio2,
                )
            }

            @Test
            fun `prevent overlapping duplicates where the last part of a name and the first part of a name identify duplicates - single case`() {
                // given
                val studio1 = "gallop co., ltd."
                val studio2 = "studio gallop"
                val studio3 = "gallop"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        studio2,
                        studio1,
                        studio3,
                    ),
                )

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    studio1,
                )
            }

            @Test
            fun `prevent overlapping duplicates where the last part of a name and the first part of a name identify duplicates - multiple case`() {
                // given
                val studio1 = "gallop co., ltd."
                val studio2 = "studio gallop"
                val studio3 = "tokyo HQ gallop"
                val studio4 = "gallop made up"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        studio2,
                        studio1,
                        studio3,
                        studio4,
                    ),
                )

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    studio1,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - ANIME`() {
                // given
                val studio1 = "anime beans"
                val studio2 = "tate anime"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        studio1,
                        studio2,
                    ),
                )

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - STUDIO`() {
                // given
                val studio1 = "studio durian"
                val studio2 = "wit studio"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        studio1,
                        studio2,
                    ),
                )

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - TV`() {
                // given
                val studio1 = "sotsu"
                val studio2 = "square enix"
                val studio3 = "tv aichi"
                val studio4 = "tv osaka"
                val studio5 =  "tv tokyo"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        studio1,
                        studio2,
                        studio3,
                        studio4,
                        studio5,
                    ),
                )

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - TOKYO`() {
                // given
                val studio1 = "tokyo kids"
                val studio2 = "tv tokyo"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        studio1,
                        studio2,
                    ),
                )

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - GROOVE`() {
                // given
                val studio1 = "groove corporation"
                val studio2 = "group tac"
                val studio3 = "hitsuji no uta production committee"
                val studio4 = "inter communications inc."
                val studio5 = "ken groove"
                val studio6 = "madhouse"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        studio1,
                        studio2,
                        studio3,
                        studio4,
                        studio5,
                        studio6,
                    ),
                )

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                    studio6,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - MEDIA`() {
                // given
                val studio1 = "kitty media"
                val studio2 = "media blasters"
                val studio3 = "on-lead"
                val studio4 = "venet"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        studio1,
                        studio2,
                        studio3,
                        studio4,
                    ),
                )

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - PRODUCTION`() {
                // given
                val studio1 = "009 re:cyborg production committee"
                val studio2 = "amazonlaterna co,ltd."
                val studio3 = "funimation"
                val studio4 = "ishimori production"
                val studio5 = "nippon television network corporation"
                val studio6 = "production i.g"
                val studio7 = "sanzigen"
                val studio8 = "t-joy"
                val studio9 = "vap"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        studio1,
                        studio2,
                        studio3,
                        studio4,
                        studio5,
                        studio6,
                        studio7,
                        studio8,
                        studio9,
                    ),
                )

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                    studio6,
                    studio7,
                    studio8,
                    studio9,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - ANIMATION`() {
                // given
                val studio1 = "animation do"
                val studio2 = "chu-2byo production partners"
                val studio3 = "kyoto animation"
                val studio4 = "lantis"
                val studio5 = "pony canyon"
                val studio6 = "rakuonsha"
                val studio7 = "tbs"
                val studio8 = "tokyo broadcasting system"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        studio1,
                        studio2,
                        studio3,
                        studio4,
                        studio5,
                        studio6,
                        studio7,
                        studio8,
                    ),
                )

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                    studio6,
                    studio7,
                    studio8,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - TELEVISION`() {
                // given
                val studio1 = "chiba television"
                val studio2 = "geneon universal entertainment"
                val studio3 = "kbs kyoto"
                val studio4 = "medil"
                val studio5 = "mie television broadcasting"
                val studio6 = "sun tv"
                val studio7 = "television kanagawa"
                val studio8 = "television saitama co., ltd."
                val studio9 = "thefool"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        studio1,
                        studio2,
                        studio3,
                        studio4,
                        studio5,
                        studio6,
                        studio7,
                        studio8,
                        studio9,
                    ),
                )

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                    studio6,
                    studio7,
                    studio8,
                    studio9,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - GROUP`() {
                // given
                val studio1 = "group tac"
                val studio2 = "nippon herald film group"
                val studio3 = "the asahi shimbun company"
                val studio4 = "tv asahi"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _studios = hashSetOf(
                        studio1,
                        studio2,
                        studio3,
                        studio4,
                    ),
                )

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
                "\u2019",
                "\u02BB",
                "\u02BC",
                "\u2018",
                "\u275B",
                "\u275C",
                "\u02B9",
                "\u02BE",
                "\u02C8",
                "\u055A",
                "\u07F4",
                "\u07F5",
                "\u1FBF",
                "\u2032",
                "\uA78C",
                "\uFF07",
                "\u0060",
            ])
            fun `normalizes any combination of preceding apostrophe followed by s or s followed by apostrophe`(value: String) {
                // when
                val anime = AnimeRaw(
                    _title = "Main title",
                    _studios = hashSetOf(
                        "she${value}s, John${value}s, it${value}s",
                        "the girls$value, the dogs${value}",
                        "don${value}t, can${value}t",
                        "I${value}m",
                        "I${value}d, they${value}d",
                        "rock ${value}n$value roll",
                        "he${value}ll, we${value}ll",
                        "your${value}re",
                        "I${value}ve",
                        "${value}ere",
                        "${value}cause",
                        "${value}bout",
                        "${value}fore",
                    ),
                )

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    "she\u0027s, john\u0027s, it\u0027s",
                    "the girls\u0027, the dogs\u0027",
                    "don\u0027t, can\u0027t",
                    "i\u0027m",
                    "i\u0027d, they\u0027d",
                    "rock \u0027n\u0027 roll",
                    "he\u0027ll, we\u0027ll",
                    "your\u0027re",
                    "i\u0027ve",
                    "\u0027ere",
                    "\u0027cause",
                    "\u0027bout",
                    "\u0027fore",
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
                "\u0027",
            ])
            fun `doesn't normalize acceptable apostrophes`(value: String) {
                // when
                val anime = AnimeRaw(
                    _title = "Main title",
                    _studios = hashSetOf(
                        "she${value}s, John${value}s, it${value}s",
                        "the girls$value, the dogs${value}",
                        "don${value}t, can${value}t",
                        "I${value}m",
                        "I${value}d, they${value}d",
                        "rock ${value}n$value roll",
                        "he${value}ll, we${value}ll",
                        "your${value}re",
                        "I${value}ve",
                        "${value}ere",
                        "${value}cause",
                        "${value}bout",
                        "${value}fore",
                    ),
                )

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    "she${value}s, john${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "i${value}m",
                    "i${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "i${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [" Death Note", "Death Note ", "  Death   Note  ", "DEATH NOTE", "", " ", "    ", "\u200C"])
            fun `doesn't fix studios if activateChecks is false`(value: String) {
                // when
                val obj = AnimeRaw(
                    _title = "デスノート",
                    _studios = hashSetOf(
                        value,
                    ),
                    activateChecks = false,
                )

                // then
                assertThat(obj.studios).containsExactlyInAnyOrder(
                    value,
                )
            }
        }

        @Nested
        inner class AddStudiosTests {

            @Test
            fun `studios added are set to lower case`() {
                // given
                val studio = "EXAMPLE"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    studio,
                ))

                // then
                assertThat(anime.studios).containsExactly(
                    studio.lowercase(),
                )
            }

            @Test
            fun `remove leading whitespace from studios`() {
                // given
                val expectedStudio = "example"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    " $expectedStudio",
                ))

                // then
                assertThat(anime.studios).containsExactly(
                    expectedStudio,
                )
            }

            @Test
            fun `remove tailing whitespace from studios`() {
                // given
                val expectedStudios = "example"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    "$expectedStudios ",
                ))

                // then
                assertThat(anime.studios).containsExactly(
                    expectedStudios,
                )
            }

            @Test
            fun `replace multiple whitespaces with a single whitespace in studios`() {
                // given
                val expectedStudio = "bibury animation studios"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    "bibury    animation     studios",
                ))

                // then
                assertThat(anime.studios).containsExactly(
                    expectedStudio,
                )
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
            fun `don't add studios if it's a blank string`(input: String) {
                // given
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    "$input $input",
                ))

                // then
                assertThat(anime.studios).isEmpty()
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `normalize visible whitespaces to a normale whitespace`(input: String) {
                // given
                val expectedStudio = "bibury animation studios"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    "bibury${input}animation${input}studios",
                ))

                // then
                assertThat(anime.studios).containsExactly(
                    expectedStudio,
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `remove non-visible whitespaces`(input: String) {
                // given
                val expectedStudio = "bibury animation studios"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    "bi${input}bury ani${input}mation stu${input}dios",
                ))

                // then
                assertThat(anime.studios).containsExactly(
                    expectedStudio,
                )
            }

            @Test
            fun `correctly adds a single value`() {
                // given
                val studio = "madhouse"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    studio,
                ))

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio,
                )
            }

            @Test
            fun `does nothing in case of an empty list`() {
                // given
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf())

                // then
                assertThat(anime.studios).isEmpty()
            }

            @Test
            fun `prevent duplicates`() {
                // given
                val studio1 = "a studio"
                val studio2 = "before the other"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    studio2,
                    studio1,
                    studio1,
                    studio2,
                ))

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                )
            }

            @Test
            fun `prevent duplicates between longer and shorter versions of the same studio name - same prefix`() {
                // given
                val studio1 = "satelight"
                val studio2 = "satelight inc."
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    studio2,
                    studio1,
                ))

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio2,
                )
            }

            @Test
            fun `prevent duplicates between longer and shorter versions of the same studio name - same suffix`() {
                // given
                val studio1 = "gallop"
                val studio2 = "studio gallop"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    studio2,
                    studio1,
                ))

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio2,
                )
            }

            @Test
            fun `prevent overlapping duplicates where the last part of a name and the first part of a name identify duplicates - single case`() {
                // given
                val studio1 = "gallop co., ltd."
                val studio2 = "studio gallop"
                val studio3 = "gallop"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    studio2,
                    studio1,
                    studio3,
                ))

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                )
            }

            @Test
            fun `prevent overlapping duplicates where the last part of a name and the first part of a name identify duplicates - multiple cases`() {
                // given
                val studio1 = "gallop co., ltd."
                val studio2 = "studio gallop"
                val studio3 = "tokyo HQ gallop"
                val studio4 = "gallop made up"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                ))

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - ANIME`() {
                // given
                val studio1 = "anime beans"
                val studio2 = "tate anime"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    studio1,
                    studio2,
                ))

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - STUDIO`() {
                // given
                val studio1 = "studio durian"
                val studio2 = "wit studio"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    studio1,
                    studio2,
                ))

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - TV`() {
                // given
                val studio1 = "sotsu"
                val studio2 = "square enix"
                val studio3 = "tv aichi"
                val studio4 = "tv osaka"
                val studio5 = "tv tokyo"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                ))

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - TOKYO`() {
                // given
                val studio1 = "tokyo kids"
                val studio2 = "tv tokyo"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    studio1,
                    studio2,
                ))

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - GROOVE`() {
                // given
                val studio1 = "groove corporation"
                val studio2 = "group tac"
                val studio3 = "hitsuji no uta production committee"
                val studio4 = "inter communications inc."
                val studio5 = "kenmedia"
                val studio6 = "ken groove"
                val studio7 = "madhouse"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                    studio6,
                    studio7,
                ))

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                    studio6,
                    studio7,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - MEDIA`() {
                // given
                val studio1 = "kitty media"
                val studio2 = "media blasters"
                val studio3 = "on-lead"
                val studio4 = "venet"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                ))

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - PRODUCTION`() {
                // given
                val studio1 = "009 re:cyborg production committee"
                val studio2 = "amazonlaterna co,ltd."
                val studio3 = "funimation"
                val studio4 = "ishimori production"
                val studio5 = "nippon television network corporation"
                val studio6 = "production i.g"
                val studio7 = "sanzigen"
                val studio8 = "t-joy"
                val studio9 = "vap"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                    studio6,
                    studio7,
                    studio8,
                    studio9,
                ))

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                    studio6,
                    studio7,
                    studio8,
                    studio9,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - ANIMATION`() {
                // given
                val studio1 = "animation do"
                val studio2 = "chu-2byo production partners"
                val studio3 = "kyoto animation"
                val studio4 = "lantis"
                val studio5 = "pony canyon"
                val studio6 = "rakuonsha"
                val studio7 = "tbs"
                val studio8 = "tokyo broadcasting system"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                    studio6,
                    studio7,
                    studio8,
                ))

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                    studio6,
                    studio7,
                    studio8,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - TELEVISION`() {
                // given
                val studio1 = "chiba television"
                val studio2 = "geneon universal entertainment"
                val studio3 = "kbs kyoto"
                val studio4 = "medil"
                val studio5 = "mie television broadcasting"
                val studio6 = "sun tv"
                val studio7 = "television kanagawa"
                val studio8 = "television saitama co., ltd."
                val studio9 = "thefool"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                    studio6,
                    studio7,
                    studio8,
                    studio9,
                ))

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                    studio6,
                    studio7,
                    studio8,
                    studio9,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - GROUP`() {
                // given
                val studio1 = "group tac"
                val studio2 = "nippon herald film group"
                val studio3 = "the asahi shimbun company"
                val studio4 = "tv asahi"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(listOf(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                ))

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
                "\u2019",
                "\u02BB",
                "\u02BC",
                "\u2018",
                "\u275B",
                "\u275C",
                "\u02B9",
                "\u02BE",
                "\u02C8",
                "\u055A",
                "\u07F4",
                "\u07F5",
                "\u1FBF",
                "\u2032",
                "\uA78C",
                "\uFF07",
                "\u0060",
            ])
            fun `normalizes any combination of preceding apostrophe followed by s or s followed by apostrophe`(value: String) {
                // given
                val anime = AnimeRaw(
                    _title = "Main title",
                )

                // when
                val result = anime.addStudios(listOf(
                    "she${value}s, John${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "I${value}m",
                    "I${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "I${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                ))

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    "she\u0027s, john\u0027s, it\u0027s",
                    "the girls\u0027, the dogs\u0027",
                    "don\u0027t, can\u0027t",
                    "i\u0027m",
                    "i\u0027d, they\u0027d",
                    "rock \u0027n\u0027 roll",
                    "he\u0027ll, we\u0027ll",
                    "your\u0027re",
                    "i\u0027ve",
                    "\u0027ere",
                    "\u0027cause",
                    "\u0027bout",
                    "\u0027fore",
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
                "\u0027",
            ])
            fun `doesn't normalize acceptable apostrophes`(value: String) {
                // given
                val anime = AnimeRaw(
                    _title = "Main title",
                )

                // when
                val result = anime.addStudios(listOf(
                    "she${value}s, john${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "i${value}m",
                    "i${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "i${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                ))

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    "she${value}s, john${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "i${value}m",
                    "i${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "i${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                )
            }
        }

        @Nested
        inner class AddStudiosVarargTests {

            @Test
            fun `studios added are set to lower case`() {
                // given
                val studio = "EXAMPLE"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(studio)

                // then
                assertThat(anime.studios).containsExactly(
                    studio.lowercase(),
                )
            }

            @Test
            fun `remove leading whitespace from studios`() {
                // given
                val expectedStudio = "example"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    " $expectedStudio",
                )

                // then
                assertThat(anime.studios).containsExactly(
                    expectedStudio,
                )
            }

            @Test
            fun `remove tailing whitespace from studios`() {
                // given
                val expectedStudio = "example"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    "$expectedStudio ",
                )

                // then
                assertThat(anime.studios).containsExactly(
                    expectedStudio,
                )
            }

            @Test
            fun `replace multiple whitespaces with a single whitespace in studios`() {
                // given
                val expectedStudio = "bibury animation studios"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    "bibury    animation    studios",
                )

                // then
                assertThat(anime.studios).containsExactly(
                    expectedStudio,
                )
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
            fun `don't add studios if it's a blank string`(input: String) {
                // given
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    "$input $input",
                )

                // then
                assertThat(anime.studios).isEmpty()
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `normalize visible whitespaces to a normale whitespace`(input: String) {
                // given
                val expectedStudio = "bibury animation studios"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    "bibury${input}animation${input}studios",
                )

                // then
                assertThat(anime.studios).containsExactly(
                    expectedStudio,
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `remove non-visible whitespaces`(input: String) {
                // given
                val expectedStudio = "bibury animation studios"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    "bi${input}bury ani${input}mation stu${input}dios",
                )

                // then
                assertThat(anime.studios).containsExactly(
                    expectedStudio,
                )
            }

            @Test
            fun `correctly adds a single value`() {
                // given
                val studio = "madhouse"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    studio,
                )

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio,
                )
            }

            @Test
            fun `does nothing in case of an empty list`() {
                // given
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios()

                // then
                assertThat(anime.studios).isEmpty()
            }

            @Test
            fun `prevent duplicates`() {
                // given
                val studio1 = "a studio"
                val studio2 = "before the other"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    studio2,
                    studio1,
                    studio1,
                    studio2,
                )

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                )
            }

            @Test
            fun `prevent duplicates between longer and shorter versions of the same studio name - same prefix`() {
                // given
                val studio1 = "satelight"
                val studio2 = "satelight inc."
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    studio1,
                    studio2,
                )

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio2,
                )
            }

            @Test
            fun `prevent duplicates between longer and shorter versions of the same studio name - same suffix`() {
                // given
                val studio1 = "gallop"
                val studio2 = "studio gallop"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    studio1,
                    studio2,
                )

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio2,
                )
            }

            @Test
            fun `prevent overlapping duplicates where the last part of a name and the first part of a name identify duplicates - single case`() {
                // given
                val studio1 = "gallop co., ltd."
                val studio2 = "studio gallop"
                val studio3 = "gallop"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    studio3,
                    studio2,
                    studio1,
                )

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                )
            }

            @Test
            fun `prevent overlapping duplicates where the last part of a name and the first part of a name identify duplicates - multiple cases`() {
                // given
                val studio1 = "gallop co., ltd."
                val studio2 = "studio gallop"
                val studio3 = "tokyo HQ gallop"
                val studio4 = "gallop made up"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                )

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - ANIME`() {
                // given
                val studio1 = "anime beans"
                val studio2 = "tate anime"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    studio1,
                    studio2,
                )

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - STUDIO`() {
                // given
                val studio1 = "studio durian"
                val studio2 = "wit studio"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    studio1,
                    studio2,
                )

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - TV`() {
                // given
                val studio1 = "sotsu"
                val studio2 = "square enix"
                val studio3 = "tv aichi"
                val studio4 = "tv osaka"
                val studio5 = "tv tokyo"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                )

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - TOKYO`() {
                // given
                val studio1 = "tokyo kids"
                val studio2 = "tv tokyo"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    studio1,
                    studio2,
                )

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - GROOVE`() {
                // given
                val studio1 = "groove corporation"
                val studio2 = "group tac"
                val studio3 = "hitsuji no uta production committee"
                val studio4 = "inter communications inc."
                val studio5 = "kenmedia"
                val studio6 = "ken groove"
                val studio7 = "madhouse"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                    studio6,
                    studio7,
                )

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                    studio6,
                    studio7,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - MEDIA`() {
                // given
                val studio1 = "kitty media"
                val studio2 = "media blasters"
                val studio3 = "on-lead"
                val studio4 = "venet"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                )

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - PRODUCTION`() {
                // given
                val studio1 = "009 re:cyborg production committee"
                val studio2 = "amazonlaterna co,ltd."
                val studio3 = "funimation"
                val studio4 = "ishimori production"
                val studio5 = "nippon television network corporation"
                val studio6 = "production i.g"
                val studio7 = "sanzigen"
                val studio8 = "t-joy"
                val studio9 = "vap"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                    studio6,
                    studio7,
                    studio8,
                    studio9,
                )

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                    studio6,
                    studio7,
                    studio8,
                    studio9,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - ANIMATION`() {
                // given
                val studio1 = "animation do"
                val studio2 = "chu-2byo production partners"
                val studio3 = "kyoto animation"
                val studio4 = "lantis"
                val studio5 = "pony canyon"
                val studio6 = "rakuonsha"
                val studio7 = "tbs"
                val studio8 = "tokyo broadcasting system"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                    studio6,
                    studio7,
                    studio8,
                )

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                    studio6,
                    studio7,
                    studio8,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - TELEVISION`() {
                // given
                val studio1 = "chiba television"
                val studio2 = "geneon universal entertainment"
                val studio3 = "kbs kyoto"
                val studio4 = "medil"
                val studio5 = "mie television broadcasting"
                val studio6 = "sun tv"
                val studio7 = "television kanagawa"
                val studio8 = "television saitama co., ltd."
                val studio9 = "thefool"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                    studio6,
                    studio7,
                    studio8,
                    studio9,
                )

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                    studio5,
                    studio6,
                    studio7,
                    studio8,
                    studio9,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - GROUP`() {
                // given
                val studio1 = "group tac"
                val studio2 = "nippon herald film group"
                val studio3 = "the asahi shimbun company"
                val studio4 = "tv asahi"
                val anime = AnimeRaw("Test")

                // when
                anime.addStudios(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                )

                // then
                assertThat(anime.studios).containsExactlyInAnyOrder(
                    studio1,
                    studio2,
                    studio3,
                    studio4,
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
                "\u2019",
                "\u02BB",
                "\u02BC",
                "\u2018",
                "\u275B",
                "\u275C",
                "\u02B9",
                "\u02BE",
                "\u02C8",
                "\u055A",
                "\u07F4",
                "\u07F5",
                "\u1FBF",
                "\u2032",
                "\uA78C",
                "\uFF07",
                "\u0060",
            ])
            fun `normalizes any combination of preceding apostrophe followed by s or s followed by apostrophe`(value: String) {
                // given
                val anime = AnimeRaw(
                    _title = "Main title",
                )

                // when
                val result = anime.addStudios(
                    "she${value}s, John${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "I${value}m",
                    "I${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "I${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                )

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    "she\u0027s, john\u0027s, it\u0027s",
                    "the girls\u0027, the dogs\u0027",
                    "don\u0027t, can\u0027t",
                    "i\u0027m",
                    "i\u0027d, they\u0027d",
                    "rock \u0027n\u0027 roll",
                    "he\u0027ll, we\u0027ll",
                    "your\u0027re",
                    "i\u0027ve",
                    "\u0027ere",
                    "\u0027cause",
                    "\u0027bout",
                    "\u0027fore",
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
                "\u0027",
            ])
            fun `doesn't normalize acceptable apostrophes`(value: String) {
                // given
                val anime = AnimeRaw(
                    _title = "Main title",
                )

                // when
                val result = anime.addStudios(
                    "she${value}s, John${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "I${value}m",
                    "I${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "I${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                )

                // then
                assertThat(result.studios).containsExactlyInAnyOrder(
                    "she${value}s, john${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "i${value}m",
                    "i${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "i${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                )
            }
        }
    }

    @Nested
    inner class ProducersTest {

        @Test
        fun `ensure that you cannot directly modify the internal hashset`() {
            // given
            val anime = AnimeRaw(
                _title = "test",
                _producers = hashSetOf(
                    "producer 1",
                    "producer 2",
                ),
            )

            // when
            anime.producers.clear()

            // then
            assertThat(anime.producers).isNotEmpty()
        }

        @Nested
        inner class AddProducersConstructorTests {

            @Test
            fun `producers added by constructor are set to lower case`() {
                // given
                val producer = "EXAMPLE"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        producer,
                    ),
                )

                // then
                assertThat(result.producers).containsExactly(
                    producer.lowercase(),
                )
            }

            @Test
            fun `remove leading whitespace from producers`() {
                // given
                val expectedProducer = "example"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        " $expectedProducer",
                    ),
                )

                // then
                assertThat(result.producers).containsExactly(
                    expectedProducer,
                )
            }

            @Test
            fun `remove tailing whitespace from producers`() {
                // given
                val expectedProducer = "example"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        "$expectedProducer ",
                    ),
                )

                // then
                assertThat(result.producers).containsExactly(
                    expectedProducer,
                )
            }

            @Test
            fun `replace multiple whitespaces with a single whitespace in producers`() {
                // given
                val expectedProducer = "pony canyon"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        "pony         canyon",
                    ),
                )

                // then
                assertThat(result.producers).containsExactly(
                    expectedProducer,
                )
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
            fun `don't add producer if it's a blank string`(input: String) {
                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        "$input $input",
                    ),
                )

                // then
                assertThat(result.producers).isEmpty()
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `normalize visible whitespaces to a normale whitespace`(input: String) {
                // given
                val expectedProducer = "pony canyon"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        "pony${input}canyon",
                    ),
                )

                // then
                assertThat(result.producers).containsExactly(
                    expectedProducer,
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `remove non-visible whitespaces`(input: String) {
                // given
                val expectedProducer = "pony canyon"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        "po${input}ny can${input}yon",
                    ),
                )

                // then
                assertThat(result.producers).containsExactly(
                    expectedProducer,
                )
            }

            @Test
            fun `correctly adds a single value`() {
                // given
                val producer = "madhouse"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        producer,
                    ),
                )

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    producer,
                )
            }

            @Test
            fun `does nothing in case of an empty list`() {
                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(),
                )

                // then
                assertThat(result.producers).isEmpty()
            }

            @Test
            fun `prevent duplicates`() {
                // given
                val producer1 = "a producer"
                val producer2 = "before the other"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        producer2,
                        producer1,
                        producer1,
                        producer2,
                    ),
                )

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                )
            }

            @Test
            fun `prevent duplicates between longer and shorter versions of the same studio name - same prefix`() {
                // given
                val producer1 = "satelight"
                val producer2 = "satelight inc."

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        producer2,
                        producer1,
                    ),
                )

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    producer2,
                )
            }

            @Test
            fun `prevent duplicates between longer and shorter versions of the same studio name - same suffix`() {
                // given
                val producer1 = "gallop"
                val producer2 = "studio gallop"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        producer2,
                        producer1,
                    ),
                )

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    producer2,
                )
            }

            @Test
            fun `prevent overlapping duplicates where the last part of a name and the first part of a name identify duplicates - single case`() {
                // given
                val producer1 = "gallop co., ltd."
                val producer2 = "studio gallop"
                val producer3 = "gallop"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        producer2,
                        producer3,
                        producer1,
                    ),
                )

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    producer1,
                )
            }

            @Test
            fun `prevent overlapping duplicates where the last part of a name and the first part of a name identify duplicates - multiple cases`() {
                // given
                val producer1 = "gallop co., ltd."
                val producer2 = "studio gallop"
                val producer3 = "tokyo HQ gallop"
                val producer4 = "gallop made up"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        producer1,
                        producer2,
                        producer3,
                        producer4,
                    ),
                )

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    producer1,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - ANIME`() {
                // given
                val producer1 = "anime beans"
                val producer2 = "tate anime"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        producer1,
                        producer2,
                    ),
                )

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - STUDIO`() {
                // given
                val producer1 = "studio durian"
                val producer2 = "wit studio"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        producer1,
                        producer2,
                    ),
                )

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - TV`() {
                // given
                val producer1 = "sotsu"
                val producer2 = "square enix"
                val producer3 = "tv aichi"
                val producer4 = "tv osaka"
                val producer5 = "tv tokyo"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        producer1,
                        producer2,
                        producer3,
                        producer4,
                        producer5,
                    ),
                )

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - TOKYO`() {
                // given
                val producer1 = "tokyo kids"
                val producer2 = "tv tokyo"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        producer1,
                        producer2,
                    ),
                )

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - GROOVE`() {
                // given
                val producer1 = "groove corporation"
                val producer2 = "group tac"
                val producer3 = "hitsuji no uta production committee"
                val producer4 = "inter communications inc."
                val producer5 = "kenmedia"
                val producer6 = "ken groove"
                val producer7 = "madhouse"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        producer1,
                        producer2,
                        producer3,
                        producer4,
                        producer5,
                        producer6,
                        producer7,
                    ),
                )

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                    producer6,
                    producer7,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - MEDIA`() {
                // given
                val producer1 = "kitty media"
                val producer2 = "media blasters"
                val producer3 = "on-lead"
                val producer4 = "venet"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        producer1,
                        producer2,
                        producer3,
                        producer4,
                    ),
                )

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - PRODUCTION`() {
                // given
                val producer1 = "009 re:cyborg production committee"
                val producer2 = "amazonlaterna co,ltd."
                val producer3 = "funimation"
                val producer4 = "ishimori production"
                val producer5 = "nippon television network corporation"
                val producer6 = "production i.g"
                val producer7 = "sanzigen"
                val producer8 = "t-joy"
                val producer9 = "vap"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        producer1,
                        producer2,
                        producer3,
                        producer4,
                        producer5,
                        producer6,
                        producer7,
                        producer8,
                        producer9,
                    ),
                )

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                    producer6,
                    producer7,
                    producer8,
                    producer9,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - ANIMATION`() {
                // given
                val producer1 = "animation do"
                val producer2 = "chu-2byo production partners"
                val producer3 = "kyoto animation"
                val producer4 = "lantis"
                val producer5 = "pony canyon"
                val producer6 = "rakuonsha"
                val producer7 = "tbs"
                val producer8 = "tokyo broadcasting system"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        producer1,
                        producer2,
                        producer3,
                        producer4,
                        producer5,
                        producer6,
                        producer7,
                        producer8,
                    ),
                )

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                    producer6,
                    producer7,
                    producer8,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - TELEVISION`() {
                // given
                val producer1 = "chiba television"
                val producer2 = "geneon universal entertainment"
                val producer3 = "kbs kyoto"
                val producer4 = "medil"
                val producer5 = "mie television broadcasting"
                val producer6 = "sun tv"
                val producer7 = "television kanagawa"
                val producer8 = "television saitama co., ltd."
                val producer9 = "thefool"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        producer1,
                        producer2,
                        producer3,
                        producer4,
                        producer5,
                        producer6,
                        producer7,
                        producer8,
                        producer9,
                    ),
                )

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                    producer6,
                    producer7,
                    producer8,
                    producer9,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - GROUP`() {
                // given
                val producer1 = "group tac"
                val producer2 = "nippon herald film group"
                val producer3 = "the asahi shimbun company"
                val producer4 = "tv asahi"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _producers = hashSetOf(
                        producer1,
                        producer2,
                        producer3,
                        producer4,
                    ),
                )

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
                "\u2019",
                "\u02BB",
                "\u02BC",
                "\u2018",
                "\u275B",
                "\u275C",
                "\u02B9",
                "\u02BE",
                "\u02C8",
                "\u055A",
                "\u07F4",
                "\u07F5",
                "\u1FBF",
                "\u2032",
                "\uA78C",
                "\uFF07",
                "\u0060",
            ])
            fun `normalizes any combination of preceding apostrophe followed by s or s followed by apostrophe`(value: String) {
                // when
                val anime = AnimeRaw(
                    _title = "Main title",
                    _producers = hashSetOf(
                        "she${value}s, John${value}s, it${value}s",
                        "the girls$value, the dogs${value}",
                        "don${value}t, can${value}t",
                        "I${value}m",
                        "I${value}d, they${value}d",
                        "rock ${value}n$value roll",
                        "he${value}ll, we${value}ll",
                        "your${value}re",
                        "I${value}ve",
                        "${value}ere",
                        "${value}cause",
                        "${value}bout",
                        "${value}fore",
                    ),
                )

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    "she\u0027s, john\u0027s, it\u0027s",
                    "the girls\u0027, the dogs\u0027",
                    "don\u0027t, can\u0027t",
                    "i\u0027m",
                    "i\u0027d, they\u0027d",
                    "rock \u0027n\u0027 roll",
                    "he\u0027ll, we\u0027ll",
                    "your\u0027re",
                    "i\u0027ve",
                    "\u0027ere",
                    "\u0027cause",
                    "\u0027bout",
                    "\u0027fore",
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
                "\u0027",
            ])
            fun `doesn't normalize acceptable apostrophes`(value: String) {
                // when
                val anime = AnimeRaw(
                    _title = "Main title",
                    _producers = hashSetOf(
                        "she${value}s, John${value}s, it${value}s",
                        "the girls$value, the dogs${value}",
                        "don${value}t, can${value}t",
                        "I${value}m",
                        "I${value}d, they${value}d",
                        "rock ${value}n$value roll",
                        "he${value}ll, we${value}ll",
                        "your${value}re",
                        "I${value}ve",
                        "${value}ere",
                        "${value}cause",
                        "${value}bout",
                        "${value}fore",
                    ),
                )

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    "she${value}s, john${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "i${value}m",
                    "i${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "i${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [" Death Note", "Death Note ", "  Death   Note  ", "DEATH NOTE", "", " ", "    ", "\u200C"])
            fun `doesn't fix producers if activateChecks is false`(value: String) {
                // when
                val obj = AnimeRaw(
                    _title = "デスノート",
                    _producers = hashSetOf(
                        value,
                    ),
                    activateChecks = false,
                )

                // then
                assertThat(obj.producers).containsExactlyInAnyOrder(
                    value,
                )
            }
        }

        @Nested
        inner class AddProducersTests {

            @Test
            fun `producers added are set to lower case`() {
                // given
                val producer = "EXAMPLE"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    producer,
                ))

                // then
                assertThat(anime.producers).containsExactly(
                    producer.lowercase(),
                )
            }

            @Test
            fun `remove leading whitespace from producers`() {
                // given
                val expectedProducer = "example"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    " $expectedProducer",
                ))

                // then
                assertThat(anime.producers).containsExactly(
                    expectedProducer,
                )
            }

            @Test
            fun `remove tailing whitespace from producers`() {
                // given
                val expectedProducer = "example"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    "$expectedProducer ",
                ))

                // then
                assertThat(anime.producers).containsExactly(
                    expectedProducer,
                )
            }

            @Test
            fun `replace multiple whitespaces with a single whitespace in producers`() {
                // given
                val expectedProducer = "pony canyon"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    "pony           canyon",
                ))

                // then
                assertThat(anime.producers).containsExactly(
                    expectedProducer,
                )
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
            fun `don't add studios if it's a blank string`(input: String) {
                // given
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    "$input $input",
                ))

                // then
                assertThat(anime.producers).isEmpty()
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `normalize visible whitespaces to a normale whitespace`(input: String) {
                // given
                val expectedProducer = "pony canyon"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    "pony${input}canyon",
                ))

                // then
                assertThat(anime.producers).containsExactly(
                    expectedProducer,
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `remove non-visible whitespaces`(input: String) {
                // given
                val expectedProducer = "pony canyon"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    "po${input}ny can${input}yon",
                ))

                // then
                assertThat(anime.producers).containsExactly(
                    expectedProducer,
                )
            }

            @Test
            fun `correctly adds a single value`() {
                // given
                val producer = "madhouse"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    producer,
                ))

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer,
                )
            }

            @Test
            fun `does nothing in case of an empty list`() {
                // given
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf())

                // then
                assertThat(anime.producers).isEmpty()
            }

            @Test
            fun `prevent duplicates`() {
                // given
                val producer1 = "a producer"
                val producer2 = "before the other"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    producer2,
                    producer1,
                    producer1,
                    producer2,
                ))

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                )
            }

            @Test
            fun `prevent duplicates between longer and shorter versions of the same studio name - same prefix`() {
                // given
                val producer1 = "satelight"
                val producer2 = "satelight inc."
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    producer1,
                    producer2,
                ))

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer2,
                )
            }

            @Test
            fun `prevent duplicates between longer and shorter versions of the same studio name - same suffix`() {
                // given
                val producer1 = "gallop"
                val producer2 = "studio gallop"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    producer1,
                    producer2,
                ))

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer2,
                )
            }

            @Test
            fun `prevent overlapping duplicates where the last part of a name and the first part of a name identify duplicates - single case`() {
                // given
                val producer1 = "gallop co., ltd."
                val producer2 = "studio gallop"
                val producer3 = "gallop"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    producer2,
                    producer3,
                    producer1,
                ))

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                )
            }

            @Test
            fun `prevent overlapping duplicates where the last part of a name and the first part of a name identify duplicates - multiple cases`() {
                // given
                val producer1 = "gallop co., ltd."
                val producer2 = "studio gallop"
                val producer3 = "tokyo HQ gallop"
                val producer4 = "gallop made up"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                ))

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - ANIME`() {
                // given
                val producer1 = "anime beans"
                val producer2 = "tate anime"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    producer1,
                    producer2,
                ))

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - STUDIO`() {
                // given
                val producer1 = "studio durian"
                val producer2 = "wit studio"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    producer1,
                    producer2,
                ))

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - TV`() {
                // given
                val producer1 = "sotsu"
                val producer2 = "square enix"
                val producer3 = "tv aichi"
                val producer4 = "tv osaka"
                val producer5 = "tv tokyo"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                ))

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - TOKYO`() {
                // given
                val producer1 = "tokyo kids"
                val producer2 = "tv tokyo"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    producer1,
                    producer2,
                ))

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - GROOVE`() {
                // given
                val producer1 = "groove corporation"
                val producer2 = "group tac"
                val producer3 = "hitsuji no uta production committee"
                val producer4 = "inter communications inc."
                val producer5 = "kenmedia"
                val producer6 = "ken groove"
                val producer7 = "madhouse"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                    producer6,
                    producer7,
                ))

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                    producer6,
                    producer7,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - MEDIA`() {
                // given
                val producer1 = "kitty media"
                val producer2 = "media blasters"
                val producer3 = "on-lead"
                val producer4 = "venet"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                ))

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - PRODUCTION`() {
                // given
                val producer1 = "009 re:cyborg production committee"
                val producer2 = "amazonlaterna co,ltd."
                val producer3 = "funimation"
                val producer4 = "ishimori production"
                val producer5 = "nippon television network corporation"
                val producer6 = "production i.g"
                val producer7 = "sanzigen"
                val producer8 = "t-joy"
                val producer9 = "vap"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                    producer6,
                    producer7,
                    producer8,
                    producer9,
                ))

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                    producer6,
                    producer7,
                    producer8,
                    producer9,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - ANIMATION`() {
                // given
                val producer1 = "animation do"
                val producer2 = "chu-2byo production partners"
                val producer3 = "kyoto animation"
                val producer4 = "lantis"
                val producer5 = "pony canyon"
                val producer6 = "rakuonsha"
                val producer7 = "tbs"
                val producer8 = "tokyo broadcasting system"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                    producer6,
                    producer7,
                    producer8,
                ))

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                    producer6,
                    producer7,
                    producer8,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - TELEVISION`() {
                // given
                val producer1 = "chiba television"
                val producer2 = "geneon universal entertainment"
                val producer3 = "kbs kyoto"
                val producer4 = "medil"
                val producer5 = "mie television broadcasting"
                val producer6 = "sun tv"
                val producer7 = "television kanagawa"
                val producer8 = "television saitama co., ltd."
                val producer9 = "thefool"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                    producer6,
                    producer7,
                    producer8,
                    producer9,
                ))

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                    producer6,
                    producer7,
                    producer8,
                    producer9,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - GROUP`() {
                // given
                val producer1 = "group tac"
                val producer2 = "nippon herald film group"
                val producer3 = "the asahi shimbun company"
                val producer4 = "tv asahi"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(listOf(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                ))

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
                "\u2019",
                "\u02BB",
                "\u02BC",
                "\u2018",
                "\u275B",
                "\u275C",
                "\u02B9",
                "\u02BE",
                "\u02C8",
                "\u055A",
                "\u07F4",
                "\u07F5",
                "\u1FBF",
                "\u2032",
                "\uA78C",
                "\uFF07",
                "\u0060",
            ])
            fun `normalizes any combination of preceding apostrophe followed by s or s followed by apostrophe`(value: String) {
                // given
                val anime = AnimeRaw(
                    _title = "Main title",
                )

                // when
                val result = anime.addProducers(listOf(
                    "she${value}s, John${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "I${value}m",
                    "I${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "I${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                ))

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    "she\u0027s, john\u0027s, it\u0027s",
                    "the girls\u0027, the dogs\u0027",
                    "don\u0027t, can\u0027t",
                    "i\u0027m",
                    "i\u0027d, they\u0027d",
                    "rock \u0027n\u0027 roll",
                    "he\u0027ll, we\u0027ll",
                    "your\u0027re",
                    "i\u0027ve",
                    "\u0027ere",
                    "\u0027cause",
                    "\u0027bout",
                    "\u0027fore",
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
                "\u0027",
            ])
            fun `doesn't normalize acceptable apostrophes`(value: String) {
                // given
                val anime = AnimeRaw(
                    _title = "Main title",
                )

                // when
                val result = anime.addProducers(listOf(
                    "she${value}s, John${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "I${value}m",
                    "I${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "I${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                ))

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    "she${value}s, john${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "i${value}m",
                    "i${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "i${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                )
            }
        }

        @Nested
        inner class AddProducersVarargTests {

            @Test
            fun `producers added are set to lower case`() {
                // given
                val producer = "EXAMPLE"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(producer)

                // then
                assertThat(anime.producers).containsExactly(
                    producer.lowercase(),
                )
            }

            @Test
            fun `remove leading whitespace from producers`() {
                // given
                val expectedProducer = "example"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    " $expectedProducer",
                )

                // then
                assertThat(anime.producers).containsExactly(
                    expectedProducer,
                )
            }

            @Test
            fun `remove tailing whitespace from producers`() {
                // given
                val expectedProducer = "example"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    "$expectedProducer ",
                )

                // then
                assertThat(anime.producers).containsExactly(
                    expectedProducer,
                )
            }

            @Test
            fun `replace multiple whitespaces with a single whitespace in producers`() {
                // given
                val expectedProducer = "pony canyon"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    "pony        canyon",
                )

                // then
                assertThat(anime.producers).containsExactly(
                    expectedProducer,
                )
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
            fun `don't add producers if it's a blank string`(input: String) {
                // given
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    "$input $input",
                )

                // then
                assertThat(anime.producers).isEmpty()
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `normalize visible whitespaces to a normale whitespace`(input: String) {
                // given
                val expectedProducer = "pony canyon"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    "pony${input}canyon",
                )

                // then
                assertThat(anime.producers).containsExactly(
                    expectedProducer,
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `remove non-visible whitespaces`(input: String) {
                // given
                val expectedProducer = "pony canyon"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    "po${input}ny can${input}yon",
                )

                // then
                assertThat(anime.producers).containsExactly(
                    expectedProducer,
                )
            }

            @Test
            fun `correctly adds a single value`() {
                // given
                val producer = "madhouse"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    producer,
                )

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer,
                )
            }

            @Test
            fun `does nothing in case of an empty list`() {
                // given
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers()

                // then
                assertThat(anime.producers).isEmpty()
            }

            @Test
            fun `prevent duplicates`() {
                // given
                val producer1 = "a producer"
                val producer2 = "before the other"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    producer2,
                    producer1,
                    producer1,
                    producer2,
                )

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                )
            }

            @Test
            fun `prevent duplicates between longer and shorter versions of the same studio name - same prefix`() {
                // given
                val producer1 = "satelight"
                val producer2 = "satelight inc."
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    producer1,
                    producer2,
                )

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer2,
                )
            }

            @Test
            fun `prevent duplicates between longer and shorter versions of the same studio name - same suffix`() {
                // given
                val producer1 = "gallop"
                val producer2 = "studio gallop"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    producer1,
                    producer2,
                )

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer2,
                )
            }

            @Test
            fun `prevent overlapping duplicates where the last part of a name and the first part of a name identify duplicates - single case`() {
                // given
                val producer1 = "gallop co., ltd."
                val producer2 = "studio gallop"
                val producer3 = "gallop"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    producer1,
                    producer2,
                    producer3,
                )

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                )
            }

            @Test
            fun `prevent overlapping duplicates where the last part of a name and the first part of a name identify duplicates - multiple case`() {
                // given
                val producer1 = "gallop co., ltd."
                val producer2 = "studio gallop"
                val producer3 = "tokyo HQ gallop"
                val producer4 = "gallop made up"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                )

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - ANIME`() {
                // given
                val producer1 = "anime beans"
                val producer2 = "tate anime"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    producer1,
                    producer2,
                )

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - STUDIO`() {
                // given
                val producer1 = "studio durian"
                val producer2 = "wit studio"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    producer1,
                    producer2,
                )

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - TV`() {
                // given
                val producer1 = "sotsu"
                val producer2 = "square enix"
                val producer3 = "tv aichi"
                val producer4 = "tv osaka"
                val producer5 = "tv tokyo"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                )

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - TOKYO`() {
                // given
                val producer1 = "tokyo kids"
                val producer2 = "tv tokyo"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    producer1,
                    producer2,
                )

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - GROOVE`() {
                // given
                val producer1 = "groove corporation"
                val producer2 = "group tac"
                val producer3 = "hitsuji no uta production committee"
                val producer4 = "inter communications inc."
                val producer5 = "kenmedia"
                val producer6 = "ken groove"
                val producer7 = "madhouse"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                    producer6,
                    producer7,
                )

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                    producer6,
                    producer7,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - MEDIA`() {
                // given
                val producer1 = "kitty media"
                val producer2 = "media blasters"
                val producer3 = "on-lead"
                val producer4 = "venet"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                )

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - PRODUCTION`() {
                // given
                val producer1 = "009 re:cyborg production committee"
                val producer2 = "amazonlaterna co,ltd."
                val producer3 = "funimation"
                val producer4 = "ishimori production"
                val producer5 = "nippon television network corporation"
                val producer6 = "production i.g"
                val producer7 = "sanzigen"
                val producer8 = "t-joy"
                val producer9 = "vap"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                    producer6,
                    producer7,
                    producer8,
                    producer9,
                )

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                    producer6,
                    producer7,
                    producer8,
                    producer9,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - ANIMATION`() {
                // given
                val producer1 = "animation do"
                val producer2 = "chu-2byo production partners"
                val producer3 = "kyoto animation"
                val producer4 = "lantis"
                val producer5 = "pony canyon"
                val producer6 = "rakuonsha"
                val producer7 = "tbs"
                val producer8 = "tokyo broadcasting system"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                    producer6,
                    producer7,
                    producer8,
                )

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                    producer6,
                    producer7,
                    producer8,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - TELEVISION`() {
                // given
                val producer1 = "chiba television"
                val producer2 = "geneon universal entertainment"
                val producer3 = "kbs kyoto"
                val producer4 = "medil"
                val producer5 = "mie television broadcasting"
                val producer6 = "sun tv"
                val producer7 = "television kanagawa"
                val producer8 = "television saitama co., ltd."
                val producer9 = "thefool"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                    producer6,
                    producer7,
                    producer8,
                    producer9,
                )

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                    producer5,
                    producer6,
                    producer7,
                    producer8,
                    producer9,
                )
            }

            @Test
            fun `prevent false positive for identifying overlapping duplicates - GROUP`() {
                // given
                val producer1 = "group tac"
                val producer2 = "nippon herald film group"
                val producer3 = "the asahi shimbun company"
                val producer4 = "tv asahi"
                val anime = AnimeRaw("Test")

                // when
                anime.addProducers(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                )

                // then
                assertThat(anime.producers).containsExactlyInAnyOrder(
                    producer1,
                    producer2,
                    producer3,
                    producer4,
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
                "\u2019",
                "\u02BB",
                "\u02BC",
                "\u2018",
                "\u275B",
                "\u275C",
                "\u02B9",
                "\u02BE",
                "\u02C8",
                "\u055A",
                "\u07F4",
                "\u07F5",
                "\u1FBF",
                "\u2032",
                "\uA78C",
                "\uFF07",
                "\u0060",
            ])
            fun `normalizes any combination of preceding apostrophe followed by s or s followed by apostrophe`(value: String) {
                // given
                val anime = AnimeRaw(
                    _title = "Main title",
                )

                // when
                val result = anime.addProducers(
                    "she${value}s, John${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "I${value}m",
                    "I${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "I${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                )

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    "she\u0027s, john\u0027s, it\u0027s",
                    "the girls\u0027, the dogs\u0027",
                    "don\u0027t, can\u0027t",
                    "i\u0027m",
                    "i\u0027d, they\u0027d",
                    "rock \u0027n\u0027 roll",
                    "he\u0027ll, we\u0027ll",
                    "your\u0027re",
                    "i\u0027ve",
                    "\u0027ere",
                    "\u0027cause",
                    "\u0027bout",
                    "\u0027fore",
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
                "\u0027",
            ])
            fun `doesn't normalize acceptable apostrophes`(value: String) {
                // given
                val anime = AnimeRaw(
                    _title = "Main title",
                )

                // when
                val result = anime.addProducers(
                    "she${value}s, John${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "I${value}m",
                    "I${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "I${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                )

                // then
                assertThat(result.producers).containsExactlyInAnyOrder(
                    "she${value}s, john${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "i${value}m",
                    "i${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "i${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
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
            ).addScores(
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 4.97,
                    range = 1.0..5.0,
                ),
            )

            val b = AnimeRaw(
                _title = title,
            ).addScores(
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 4.97,
                    range = 1.0..5.0,
                ),
            )

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
            ).addScores(
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 4.97,
                    range = 1.0..5.0,
                ),
            )

            val b = AnimeRaw(
                _title =  title,
            ).addScores(
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 4.97,
                    range = 1.0..5.0,
                ),
                MetaDataProviderScoreValue(
                    hostname = "other.com",
                    value = 9.50,
                    range = 1.0..10.0,
                )
            )

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
        fun `is equal if studios are the same`() {
            // given
            val title  =  "Death Note"
            val a = AnimeRaw(
                _title =  title,
                _studios = hashSetOf(
                    "studio 1",
                    "studio 2",
                ),
            )

            val b = AnimeRaw(
                _title =  title,
                _studios = hashSetOf(
                    "studio 1",
                    "studio 2",
                ),
            )

            // when
            val result = a == b

            // then
            assertThat(result).isTrue()
            assertThat(a.hashCode()).isEqualTo(b.hashCode())
        }

        @Test
        fun `is not equal if studios are different`() {
            // given
            val title  =  "Death Note"
            val a = AnimeRaw(
                _title =  title,
                _studios = hashSetOf(
                    "studio 2",
                ),
            )

            val b = AnimeRaw(
                _title =  title,
                _studios = hashSetOf(
                    "studio 1",
                    "studio 2",
                ),
            )

            // when
            val result = a == b

            // then
            assertThat(result).isFalse()
            assertThat(a.hashCode()).isNotEqualTo(b.hashCode())
        }

        @Test
        fun `is equal if producers are the same`() {
            // given
            val title  =  "Death Note"
            val a = AnimeRaw(
                _title =  title,
                _producers = hashSetOf(
                    "producer 1",
                    "producer 2",
                ),
            )

            val b = AnimeRaw(
                _title =  title,
                _producers = hashSetOf(
                    "producer 1",
                    "producer 2",
                ),
            )

            // when
            val result = a == b

            // then
            assertThat(result).isTrue()
            assertThat(a.hashCode()).isEqualTo(b.hashCode())
        }

        @Test
        fun `is not equal if producers are different`() {
            // given
            val title  =  "Death Note"
            val a = AnimeRaw(
                _title =  title,
                _producers = hashSetOf(
                    "producer 2",
                ),
            )

            val b = AnimeRaw(
                _title =  title,
                _producers = hashSetOf(
                    "producer 1",
                    "producer 2",
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
        fun `merge studios`() {
            // given
            val anime = AnimeRaw(
                _title =  "Death Note",
                _studios = hashSetOf(
                    "studio 1"
                ),
            )

            val other = AnimeRaw(
                _title =  "Death Note",
                _studios = hashSetOf(
                    "studio 2",
                ),
            )

            // when
            val result = anime.mergeWith(other)

            // then
            assertThat(result.studios).containsExactlyInAnyOrder(
                "studio 1",
                "studio 2",
            )
        }

        @Test
        fun `merge producers`() {
            // given
            val anime = AnimeRaw(
                _title =  "Death Note",
                _producers = hashSetOf(
                    "producer 1"
                ),
            )

            val other = AnimeRaw(
                _title =  "Death Note",
                _producers = hashSetOf(
                    "producer 2",
                ),
            )

            // when
            val result = anime.mergeWith(other)

            // then
            assertThat(result.producers).containsExactlyInAnyOrder(
                "producer 1",
                "producer 2",
            )
        }

        @Test
        fun `merge scores`() {
            // given
            val title =  "Death Note"
            val scoreA = MetaDataProviderScoreValue(
                hostname = "example.org",
                value = 4.97,
                range = 1.0..5.0,
            )
            val a = AnimeRaw(
                _title = title,
            ).addScores(
                scoreA,
            )

            val scoreB = MetaDataProviderScoreValue(
                hostname = "other.com",
                value = 9.5,
                range = 1.0..10.0,
            )
            val b = AnimeRaw(
                _title = title,
            ).addScores(
                scoreB,
            )

            // when
            val result = a.mergeWith(b)

            // then
            assertThat(result.scores).containsExactlyInAnyOrder(
                scoreA,
                scoreB,
            )
        }

        @Test
        fun `overrides score entries with same hostname`() {
            // given
            val title =  "Death Note"
            val scoreA = MetaDataProviderScoreValue(
                hostname = "example.org",
                value = 2.41,
                range = 1.0..5.0,
            )
            val a = AnimeRaw(
                _title = title,
            ).addScores(
                scoreA,
            )

            val scoreB = MetaDataProviderScoreValue(
                hostname = "example.org",
                value = 4.97,
                range = 1.0..5.0,
            )
            val b = AnimeRaw(
                _title = title,
            ).addScores(
                scoreB,
            )

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

        @Nested
        inner class AddTagsConstructorTests {

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
            fun `remove leading whitespace from tags`() {
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
            fun `remove tailing whitespace from tags`() {
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
            fun `replace multiple whitespaces with a single whitespace in tags`() {
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
            fun `must not add blank tag`(input: String) {
                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _tags = hashSetOf(
                        "$input $input",
                    ),
                )

                // then
                assertThat(result.tags).isEmpty()
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `normalize visible whitespaces to a normale whitespace`(input: String) {
                // given
                val expectedTag = "slice of life"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _tags = hashSetOf(
                        "slice${input}of${input}life",
                    ),
                )

                // then
                assertThat(result.tags).containsExactly(
                    expectedTag,
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `remove non-visible whitespaces`(input: String) {
                // given
                val expectedTag = "slice of life"

                // when
                val result = AnimeRaw(
                    _title = "Test",
                    _tags = hashSetOf(
                        "sli${input}ce of li${input}fe",
                    ),
                )

                // then
                assertThat(result.tags).containsExactly(
                    expectedTag,
                )
            }

            @Test
            fun `prevent duplicates`() {
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
                assertThat(result.tags).containsExactlyInAnyOrder(
                    tag1,
                    tag2,
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
                "\u2019",
                "\u02BB",
                "\u02BC",
                "\u2018",
                "\u275B",
                "\u275C",
                "\u02B9",
                "\u02BE",
                "\u02C8",
                "\u055A",
                "\u07F4",
                "\u07F5",
                "\u1FBF",
                "\u2032",
                "\uA78C",
                "\uFF07",
                "\u0060",
            ])
            fun `normalizes any combination of preceding apostrophe followed by s or s followed by apostrophe`(value: String) {
                // when
                val anime = AnimeRaw(
                    _title = "Main title",
                    _tags = hashSetOf(
                        "she${value}s, John${value}s, it${value}s",
                        "the girls$value, the dogs${value}",
                        "don${value}t, can${value}t",
                        "I${value}m",
                        "I${value}d, they${value}d",
                        "rock ${value}n$value roll",
                        "he${value}ll, we${value}ll",
                        "your${value}re",
                        "I${value}ve",
                        "${value}ere",
                        "${value}cause",
                        "${value}bout",
                        "${value}fore",
                    ),
                )

                // then
                assertThat(anime.tags).containsExactlyInAnyOrder(
                    "she\u0027s, john\u0027s, it\u0027s",
                    "the girls\u0027, the dogs\u0027",
                    "don\u0027t, can\u0027t",
                    "i\u0027m",
                    "i\u0027d, they\u0027d",
                    "rock \u0027n\u0027 roll",
                    "he\u0027ll, we\u0027ll",
                    "your\u0027re",
                    "i\u0027ve",
                    "\u0027ere",
                    "\u0027cause",
                    "\u0027bout",
                    "\u0027fore",
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
                "\u0027",
            ])
            fun `doesn't normalize acceptable apostrophes`(value: String) {
                // when
                val anime = AnimeRaw(
                    _title = "Main title",
                    _tags = hashSetOf(
                        "she${value}s, John${value}s, it${value}s",
                        "the girls$value, the dogs${value}",
                        "don${value}t, can${value}t",
                        "I${value}m",
                        "I${value}d, they${value}d",
                        "rock ${value}n$value roll",
                        "he${value}ll, we${value}ll",
                        "your${value}re",
                        "I${value}ve",
                        "${value}ere",
                        "${value}cause",
                        "${value}bout",
                        "${value}fore",
                    ),
                )

                // then
                assertThat(anime.tags).containsExactlyInAnyOrder(
                    "she${value}s, john${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "i${value}m",
                    "i${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "i${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
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
            fun `remove leading whitespace from tags`() {
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
            fun `remove tailing whitespace from tags`() {
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
            fun `replace multiple whitespaces with a single whitespace in tags`() {
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
            fun `must not add blank tag`(input: String) {
                // given
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(listOf(
                    "$input $input",
                ))

                // then
                assertThat(anime.tags).isEmpty()
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `normalize visible whitespaces to a normale whitespace`(input: String) {
                // given
                val expectedTag = "slice of life"
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(listOf(
                    "slice${input}of${input}life",
                ))

                // then
                assertThat(anime.tags).containsExactly(
                    expectedTag,
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `remove non-visible whitespaces`(input: String) {
                // given
                val expectedTag = "slice of life"
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(listOf(
                    "sli${input}ce o${input}f li${input}fe",
                ))

                // then
                assertThat(anime.tags).containsExactly(
                    expectedTag,
                )
            }

            @Test
            fun `prevent duplicates`() {
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
                assertThat(anime.tags).containsExactlyInAnyOrder(
                    tag1,
                    tag2,
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
                "\u2019",
                "\u02BB",
                "\u02BC",
                "\u2018",
                "\u275B",
                "\u275C",
                "\u02B9",
                "\u02BE",
                "\u02C8",
                "\u055A",
                "\u07F4",
                "\u07F5",
                "\u1FBF",
                "\u2032",
                "\uA78C",
                "\uFF07",
                "\u0060",
            ])
            fun `normalizes any combination of preceding apostrophe followed by s or s followed by apostrophe`(value: String) {
                // given
                val anime = AnimeRaw(
                    _title = "Main title",
                )

                // when
                val result = anime.addTags(listOf(
                    "she${value}s, John${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "I${value}m",
                    "I${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "I${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                ))

                // then
                assertThat(result.tags).containsExactlyInAnyOrder(
                    "she\u0027s, john\u0027s, it\u0027s",
                    "the girls\u0027, the dogs\u0027",
                    "don\u0027t, can\u0027t",
                    "i\u0027m",
                    "i\u0027d, they\u0027d",
                    "rock \u0027n\u0027 roll",
                    "he\u0027ll, we\u0027ll",
                    "your\u0027re",
                    "i\u0027ve",
                    "\u0027ere",
                    "\u0027cause",
                    "\u0027bout",
                    "\u0027fore",
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
                "\u0027",
            ])
            fun `doesn't normalize acceptable apostrophes`(value: String) {
                // given
                val anime = AnimeRaw(
                    _title = "Main title",
                )

                // when
                val result = anime.addTags(listOf(
                    "she${value}s, John${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "I${value}m",
                    "I${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "I${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                ))

                // then
                assertThat(result.tags).containsExactlyInAnyOrder(
                    "she${value}s, john${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "i${value}m",
                    "i${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "i${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
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
            fun `remove leading whitespace from tags`() {
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
            fun `remove tailing whitespace from tags`() {
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
            fun `replace multiple whitespaces with a single whitespace in tags`() {
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
            fun `must not add blank tag`(input: String) {
                // given
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(
                    "$input $input",
                )

                // then
                assertThat(anime.tags).isEmpty()
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `normalize visible whitespaces to a normale whitespace`(input: String) {
                // given
                val expectedTag = "slice of life"
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(
                    "slice${input}of${input}life",
                )

                // then
                assertThat(anime.tags).containsExactly(
                    expectedTag,
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
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
            ])
            fun `remove non-visible whitespaces`(input: String) {
                // given
                val expectedTag = "slice of life"
                val anime = AnimeRaw("Test")

                // when
                anime.addTags(
                    "sli${input}ce o${input}f li${input}fe",
                )

                // then
                assertThat(anime.tags).containsExactly(
                    expectedTag,
                )
            }

            @Test
            fun `prevent duplicates`() {
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
                assertThat(anime.tags).containsExactlyInAnyOrder(
                    tag1,
                    tag2,
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
                "\u2019",
                "\u02BB",
                "\u02BC",
                "\u2018",
                "\u275B",
                "\u275C",
                "\u02B9",
                "\u02BE",
                "\u02C8",
                "\u055A",
                "\u07F4",
                "\u07F5",
                "\u1FBF",
                "\u2032",
                "\uA78C",
                "\uFF07",
                "\u0060",
            ])
            fun `normalizes any combination of preceding apostrophe followed by s or s followed by apostrophe`(value: String) {
                // given
                val anime = AnimeRaw(
                    _title = "Main title",
                )

                // when
                val result = anime.addTags(
                    "she${value}s, John${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "I${value}m",
                    "I${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "I${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                )

                // then
                assertThat(result.tags).containsExactlyInAnyOrder(
                    "she\u0027s, john\u0027s, it\u0027s",
                    "the girls\u0027, the dogs\u0027",
                    "don\u0027t, can\u0027t",
                    "i\u0027m",
                    "i\u0027d, they\u0027d",
                    "rock \u0027n\u0027 roll",
                    "he\u0027ll, we\u0027ll",
                    "your\u0027re",
                    "i\u0027ve",
                    "\u0027ere",
                    "\u0027cause",
                    "\u0027bout",
                    "\u0027fore",
                )
            }

            @ParameterizedTest
            @ValueSource(strings = [
                "\u0027",
            ])
            fun `doesn't normalize acceptable apostrophes`(value: String) {
                // given
                val anime = AnimeRaw(
                    _title = "Main title",
                )

                // when
                val result = anime.addTags(
                    "she${value}s, John${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "I${value}m",
                    "I${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "I${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
                )

                // then
                assertThat(result.tags).containsExactlyInAnyOrder(
                    "she${value}s, john${value}s, it${value}s",
                    "the girls$value, the dogs${value}",
                    "don${value}t, can${value}t",
                    "i${value}m",
                    "i${value}d, they${value}d",
                    "rock ${value}n$value roll",
                    "he${value}ll, we${value}ll",
                    "your${value}re",
                    "i${value}ve",
                    "${value}ere",
                    "${value}cause",
                    "${value}bout",
                    "${value}fore",
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
            // when
            val result = TestAnimeRawObjects.AllPropertiesSet.obj.toString()

            // then
            assertThat(result).isEqualTo(
                """
                    AnimeRaw(
                      sources      = [https://anilist.co/anime/177191]
                      title        = Go-toubun no Hanayome *
                      type         = SPECIAL
                      episodes     = 2
                      status       = FINISHED
                      animeSeason  = AnimeSeason(season=FALL, year=2024)
                      picture      = https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx177191-ovNZsq8EbIPY.jpg
                      thumbnail    = https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx177191-ovNZsq8EbIPY.jpg
                      duration     = 1440 seconds
                      scores       = [MetaDataProviderScoreValue(hostname=anilist.co, value=75.0, range=1.0..100.0)]
                      synonyms     = [The Quintessential Quintuplets*, 五等分の花嫁*]
                      studios      = [bibury animation studios]
                      producers    = [dax production, nichion, pony canyon]
                      relatedAnime = [https://anilist.co/anime/131520]
                      tags         = [comedy, drama, ensemble cast, female harem, heterosexual, language barrier, male protagonist, marriage, primarily female cast, romance, school, shounen, twins]
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
                      "studios": [],
                      "producers": [],
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
            ).addScores(
                MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 4.97,
                    range = 1.0..5.0,
                ),
            )

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
                    range = 1.0..5.0,
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
                    range = 1.0..5.0,
                )
                val scoreB = MetaDataProviderScoreValue(
                    hostname = "other.com",
                    value = 6.83,
                    range = 1.0..10.0,
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
                    range = 1.0..5.0,
                )
                val anime = AnimeRaw(
                    _title = "test",
                ).addScores(
                    MetaDataProviderScoreValue(
                        hostname = "example.org",
                        value = 2.54,
                        range = 1.0..5.0,
                    ),
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
                    range = 1.0..5.0,
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
                    range = 1.0..5.0,
                )
                val scoreB = MetaDataProviderScoreValue(
                    hostname = "other.com",
                    value = 6.83,
                    range = 1.0..10.0,
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
                    range = 1.0..5.0,
                )
                val anime = AnimeRaw(
                    _title = "test",
                ).addScores(
                    listOf(
                        MetaDataProviderScoreValue(
                            hostname = "example.org",
                            value = 2.54,
                            range = 1.0..5.0,
                        ),
                    )
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
        }
    }
}