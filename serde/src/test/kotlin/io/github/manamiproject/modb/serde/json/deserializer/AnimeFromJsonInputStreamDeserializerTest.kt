package io.github.manamiproject.modb.serde.json.deserializer

import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import io.github.manamiproject.modb.serde.TestAnimeObjects
import io.github.manamiproject.modb.serde.TestReadOnceInputStream
import io.github.manamiproject.modb.serde.createExpectedDatasetMinified
import io.github.manamiproject.modb.test.exceptionExpected
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class AnimeFromJsonInputStreamDeserializerTest {

    @Nested
    inner class DeserializeTests {

        @Test
        fun `throws exception if the stream is closed`() {
            // given
            val deserializer = AnimeFromJsonInputStreamDeserializer()
            val inputStream = LifecycleAwareInputStream(TestReadOnceInputStream("test".byteInputStream()))
            inputStream.close()

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                deserializer.deserialize(inputStream).count()
            }

            // then
            assertThat(result).hasMessage("Stream must not be closed.")
        }

        @Test
        fun `correctly deserializes JSON lines`() {
            runTest {
                // given
                val deserializer = AnimeFromJsonInputStreamDeserializer()

                val input = createExpectedDatasetMinified(
                    TestAnimeObjects.DefaultAnime.serializedMinified,
                    TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    TestAnimeObjects.NullableNotSet.serializedMinified,
                )

                val expectedEntries = listOf(
                    TestAnimeObjects.DefaultAnime.obj,
                    TestAnimeObjects.AllPropertiesSet.obj,
                    TestAnimeObjects.NullableNotSet.obj,
                )

                val inputStream = LifecycleAwareInputStream(TestReadOnceInputStream(input.byteInputStream()))

                // when
                val result = deserializer.deserialize(inputStream).toList()

                // then
                assertThat(result).containsAll(expectedEntries)
            }
        }

        @Test
        fun `returns empty list if there is not data`() {
            runTest {
                // given
                val deserializer = AnimeFromJsonInputStreamDeserializer()

                val input = createExpectedDatasetMinified()

                val inputStream = LifecycleAwareInputStream(TestReadOnceInputStream(input.byteInputStream()))

                // when
                val result = deserializer.deserialize(inputStream).toList()

                // then
                assertThat(result).isEmpty()
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = AnimeFromJsonInputStreamDeserializer.instance

            // when
            val result = AnimeFromJsonInputStreamDeserializer.instance

            // then
            assertThat(result).isExactlyInstanceOf(AnimeFromJsonInputStreamDeserializer::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}