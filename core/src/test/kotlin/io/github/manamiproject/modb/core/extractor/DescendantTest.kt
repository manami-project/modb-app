package io.github.manamiproject.modb.core.extractor

import io.github.manamiproject.modb.core.extensions.EMPTY
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class DescendantTest {

    @Nested
    inner class TerminatingChildTests {

        @ParameterizedTest
        @ValueSource(strings = ["td", "//td", "/td"])
        fun `element without terminating child`(input: String) {
            // given
            val obj = Descendant(input)

            // when
            val hasTerminatingChild = obj.hasTerminatingChild()
            val isTerminatingChild = obj.isTerminatingChild()
            val terminatingChild = obj.terminatingChild

            // when
            assertThat(hasTerminatingChild).isFalse()
            assertThat(isTerminatingChild).isFalse()
            assertThat(terminatingChild).isEqualTo(EMPTY)
        }

        @ParameterizedTest
        @ValueSource(strings = ["@attr", "text()", "node()"])
        fun `only contains terminating child`(input: String) {
            // given
            val obj = Descendant(input)

            // when
            val hasTerminatingChild = obj.hasTerminatingChild()
            val isTerminatingChild = obj.isTerminatingChild()
            val terminatingChild = obj.terminatingChild

            // when
            assertThat(hasTerminatingChild).isFalse()
            assertThat(isTerminatingChild).isTrue()
            assertThat(terminatingChild).isEmpty()
            assertThat(obj.toJsoup()).isEqualTo(input)
        }

        @ParameterizedTest
        @ValueSource(strings = ["@attr", "text()", "node()"])
        fun `single descendant followed by terminating child`(input: String) {
            // given
            val obj = Descendant("//div/$input")

            // when
            val hasTerminatingChild = obj.hasTerminatingChild()
            val isTerminatingChild = obj.isTerminatingChild()
            val terminatingChild = obj.terminatingChild

            // when
            assertThat(hasTerminatingChild).isTrue()
            assertThat(isTerminatingChild).isFalse()
            assertThat(terminatingChild).isEqualTo(input)
            assertThat(obj.toJsoup()).isEqualTo("div")
        }

        @ParameterizedTest
        @ValueSource(strings = ["@attr", "text()", "node()"])
        fun `contains terminating child after additional child and filter`(input: String) {
            // given
            val obj = Descendant("//div/span[@attr='test']/$input")

            // when
            val hasTerminatingChild = obj.hasTerminatingChild()
            val isTerminatingChild = obj.isTerminatingChild()
            val terminatingChild = obj.terminatingChild

            // when
            assertThat(hasTerminatingChild).isTrue()
            assertThat(isTerminatingChild).isFalse()
            assertThat(terminatingChild).isEqualTo(input)
            assertThat(obj.toJsoup()).isEqualTo("div > span[attr=test]")
        }
    }

    @Nested
    inner class ToJsoupTests {

        @ParameterizedTest
        @ValueSource(strings = ["td", "//td", "/td"])
        fun `return element name`(input: String) {
            // given
            val obj = Descendant(input)

            // when
            val result = obj.toJsoup()

            // when
            assertThat(result).isEqualTo("td")
        }

        @ParameterizedTest
        @ValueSource(strings = ["@attr", "text()", "node()"])
        fun `empty if only contains terminating child`(input: String) {
            // given
            val obj = Descendant(input)

            // when
            val result = obj.toJsoup()

            // when
            assertThat(result).isEqualTo(input)
        }

        @Nested
        inner class FilterElementByAttributeEqualsValue {

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by attribute equals value`(input: String) {
                // given
                val obj = Descendant("//div[@class='$input']")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div[class=$input]")
            }

            @ParameterizedTest
            @ValueSource(strings = ["@class=", "@class= ", "@class =", "@class = "])
            fun `filter element by attribute equals value works with whitespaces`(input: String) {
                // given
                val obj = Descendant("//div[$input'test']")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div[class=test]")
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by attribute equals value followed by terminating child`(input: String) {
                // given
                val obj = Descendant("//div[@class='$input']/@itemprop")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div[class=$input]")
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by attribute equals value followed by child`(input: String) {
                // given
                val obj = Descendant("//div[@class='$input']/span")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div[class=$input] > span")
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by attribute equals value on child`(input: String) {
                // given
                val obj = Descendant("//div/span[@class='$input']")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div > span[class=$input]")
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by attribute equals value multiple appearances`(input: String) {
                // given
                val obj = Descendant("//div[@itemprop='test']/span[@class='$input']")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div[itemprop=test] > span[class=$input]")
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by attribute equals value multiple appearances on same element`(input: String) {
                // given
                val obj = Descendant("//div/span[@itemprop='test'][@class='$input']")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div > span[itemprop=test][class=$input]")
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by attribute equals value combined with different filter`(input: String) {
                // given
                val obj = Descendant("//div[contains(@itemprop, 'test')]/span[@class='$input']")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div[itemprop*=test] > span[class=$input]")
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by attribute equals value combined with different filter on same element`(input: String) {
                // given
                val obj = Descendant("//div/span[@class='$input'][contains(@itemprop, 'test')]")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div > span[class=$input][itemprop*=test]")
            }
        }

        @Nested
        inner class FilterElementByAttributeContains {

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by attribute contains value`(input: String) {
                // given
                val obj = Descendant("//div[contains(@class, '$input')]")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div[class*=$input]")
            }

            @ParameterizedTest
            @ValueSource(strings = ["@class,", "@class, ", "@class ,", "@class , "])
            fun `filter element by attribute contains value works with whitespaces`(input: String) {
                // given
                val obj = Descendant("//div[contains($input'test')]")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div[class*=test]")
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by attribute contains value followed by terminating child`(input: String) {
                // given
                val obj = Descendant("//div[contains(@class, '$input')]/@itemprop")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div[class*=$input]")
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by attribute contains value followed by child`(input: String) {
                // given
                val obj = Descendant("//div[contains(@class, '$input')]/span")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div[class*=$input] > span")
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by attribute contains value on child`(input: String) {
                // given
                val obj = Descendant("//div/span[contains(@class, '$input')]")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div > span[class*=$input]")
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by attribute contains value multiple appearances`(input: String) {
                // given
                val obj = Descendant("//div[contains(@itemprop, 'test')]/span[contains(@class, '$input')]")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div[itemprop*=test] > span[class*=$input]")
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by attribute contains value multiple appearances on same element`(input: String) {
                // given
                val obj = Descendant("//div/span[contains(@itemprop, 'test')][contains(@class, '$input')]")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div > span[itemprop*=test][class*=$input]")
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by attribute contains value combined with different filter`(input: String) {
                // given
                val obj = Descendant("//div[@itemprop='test']/span[contains(@class, '$input')]")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div[itemprop=test] > span[class*=$input]")
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by attribute contains value combined with different filter on same element`(input: String) {
                // given
                val obj = Descendant("//div/span[contains(@class, '$input')][@itemprop='test']")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div > span[class*=$input][itemprop=test]")
            }
        }

        @Nested
        inner class FilterElementByTextContains {

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by text contains value`(input: String) {
                // given
                val obj = Descendant("//div[contains(text(), '$input')]")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div:matchesOwn($input)")
            }

            @ParameterizedTest
            @ValueSource(strings = ["text(),", "text(), ", "text() ,", "text() , "])
            fun `filter element by text contains value works with whitespaces`(input: String) {
                // given
                val obj = Descendant("//div[contains($input'test')]")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div:matchesOwn(test)")
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by text contains value followed by terminating child`(input: String) {
                // given
                val obj = Descendant("//div[contains(text(), '$input')]/@itemprop")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div:matchesOwn($input)")
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by text contains value followed by child`(input: String) {
                // given
                val obj = Descendant("//div[contains(text(), '$input')]/span")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div:matchesOwn($input) > span")
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by text contains value on child`(input: String) {
                // given
                val obj = Descendant("//div/span[contains(text(), '$input')]")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div > span:matchesOwn($input)")
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by text contains value multiple appearances`(input: String) {
                // given
                val obj = Descendant("//div[contains(text(), 'test')]/span[contains(text(), '$input')]")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div:matchesOwn(test) > span:matchesOwn($input)")
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by text contains value combined with different filter`(input: String) {
                // given
                val obj = Descendant("//div[@itemprop='test']/span[contains(text(), '$input')]")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div[itemprop=test] > span:matchesOwn($input)")
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "og:url", "mb1 dw3", "http://example.com/test"])
            fun `filter element by text contains value combined with different filter on same element`(input: String) {
                // given
                val obj = Descendant("//div/span[contains(text(), '$input')][@itemprop='test']")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div > span:matchesOwn($input)[itemprop=test]")
            }
        }

        @Nested
        inner class FollowingSiblingTests {

            @ParameterizedTest
            @ValueSource(strings = ["*", "td"])
            fun `select follow-sibling`(input: String) {
                // given
                val obj = Descendant("//div/following-sibling::$input")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div ~ $input")
            }

            @ParameterizedTest
            @ValueSource(strings = ["*", "td"])
            fun `select follow-sibling select text`(input: String) {
                // given
                val obj = Descendant("//div/following-sibling::$input/text()")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div ~ $input")
            }

            @ParameterizedTest
            @ValueSource(strings = ["*", "td"])
            fun `select follow-sibling in between`(input: String) {
                // given
                val obj = Descendant("//div/span/following-sibling::$input/a/@href")

                // when
                val result = obj.toJsoup()

                // when
                assertThat(result).isEqualTo("div > span ~ $input > a")
            }
        }
    }
}