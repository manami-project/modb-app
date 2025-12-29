package io.github.manamiproject.modb.core.extractor

import io.github.manamiproject.modb.test.exceptionExpected
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

internal class XmlDataExtractorTest {

    @Test
    fun `successfully uses JsoupCssSelectorDataExtractor`() {
        runTest {
            // given
            val testFileContent = """
                <!DOCTYPE html>
                <html>
                <head>
                <title>Unit test</title>
                <script>
                window.GRECAPTCHA_SITE_KEY = '6Ld_1aIZAAAAAF6bNdR67ICKIaeXLKlbhE7t2Qz4';
                </script>
                </head>
                <body>
                Test
                </body>
                </html>
            """.trimIndent()

            val selectorWorkingForJsoupCssDataExtractor = "//title"
            val cssDataExtractor = JsoupCssSelectorDataExtractor.extract(testFileContent, mapOf(
                "result" to selectorWorkingForJsoupCssDataExtractor
            ))

            // when
            val result = XmlDataExtractor.extract(testFileContent, mapOf(
                "result" to selectorWorkingForJsoupCssDataExtractor
            ))

            // then
            assertThat(result.string("result")).isEqualTo(cssDataExtractor.string("result"))
        }
    }

    @Test
    fun `if JsoupCssSelectorDataExtractor throws an exception fallback to JsoupXPathDataExtractor`() {
        runTest {
            // given
            val testFileContent = """
                <!DOCTYPE html>
                <html>
                <head>
                <title>Unit test</title>
                <script>
                window.GRECAPTCHA_SITE_KEY = '6Ld_1aIZAAAAAF6bNdR67ICKIaeXLKlbhE7t2Qz4';
                </script>
                </head>
                <body>
                Test
                </body>
                </html>
            """.trimIndent()

            val selectorThatForcesExceptionOnJsoupCssDataExtractor = "node()"
            val exceptionWasThrown = exceptionExpected<Throwable> {
                JsoupCssSelectorDataExtractor.extract(testFileContent, mapOf(
                    "result" to selectorThatForcesExceptionOnJsoupCssDataExtractor
                ))
            }

            // when
            val result = XmlDataExtractor.extract(testFileContent, mapOf(
                "result" to selectorThatForcesExceptionOnJsoupCssDataExtractor
            ))

            // then
            assertThat(exceptionWasThrown).hasMessage("Could not parse query 'node()': unexpected token at '()'")
            assertThat(result.string("result")).contains("window.GRECAPTCHA_SITE_KEY")
        }
    }
}