package io.github.manamiproject.modb.core.coroutines

import io.github.manamiproject.modb.core.coroutines.CoroutineManager.runCoroutine
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

internal class CoroutineManagerTest {

    @Nested
    inner class RunCoroutineTests {

        @Test
        fun `successfully execute action`() {
            // given
            val testFunction: suspend () -> String = {
                delay(1)
                "successful"
            }

            // when
            val result = runCoroutine(isTestContext = true) {
                testFunction()
            }

            // then
            assertThat(result).isEqualTo("successful")
        }

        @Test
        fun `throws an exception up to parent`() {
            // given
            val testFunction: suspend () -> String = {
                delay(1)
                throw IllegalStateException("test")
            }

            // when
            val result = assertThrows<IllegalStateException> {
                runCoroutine(isTestContext = true) {
                    testFunction()
                }
            }

            // then
            assertThat(result).hasMessage("test")
        }

        @Test
        fun `throws an exception up to parent from nested function call`() {
            // given
            val nestedFunction : suspend () -> String = {
                delay(1)
                throw IllegalStateException("test")
            }

            val testFunction: suspend () -> String = {
                delay(1)
                nestedFunction()
            }


            // when
            val result = assertThrows<IllegalStateException> {
                runCoroutine(isTestContext = true) {
                    testFunction()
                }
            }

            // then
            assertThat(result).hasMessage("test")
        }

        @Test
        fun `correctly throws an exception up to parent with nested call if itself`() {
            // given
            val nestedFunction : suspend () -> String = {
                delay(1)
                throw IllegalStateException("test")
            }

            val testFunction: () -> String = {
                runCoroutine(isTestContext = true) {
                    nestedFunction()
                }
            }


            // when
            val result = assertThrows<IllegalStateException> {
                runCoroutine(isTestContext = true) {
                    testFunction()
                }
            }

            // then
            assertThat(result).hasMessage("test")
        }

        @Test
        fun `correctly throws an exception up to parent with nested runBlocking`() {
            // given
            val nestedFunction : suspend () -> String = {
                delay(1)
                throw IllegalStateException("test")
            }

            val testFunction: () -> String = {
                runBlocking {
                    nestedFunction()
                }
            }


            // when
            val result = assertThrows<IllegalStateException> {
                runCoroutine(isTestContext = true) {
                    testFunction()
                }
            }

            // then
            assertThat(result).hasMessage("test")
        }
    }
}