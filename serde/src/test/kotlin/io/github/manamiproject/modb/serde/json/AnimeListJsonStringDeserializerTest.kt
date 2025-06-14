package io.github.manamiproject.modb.serde.json

import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import io.github.manamiproject.modb.serde.TestAnimeObjects
import io.github.manamiproject.modb.serde.TestReadOnceInputStream
import io.github.manamiproject.modb.serde.createExpectedDatasetMinified
import io.github.manamiproject.modb.serde.createExpectedDatasetPrettyPrint
import io.github.manamiproject.modb.test.exceptionExpected
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

internal class AnimeListJsonStringDeserializerTest {

    @Nested
    inner class DeserializeStringTests {

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
        fun `throws exception if the given string is blank`(input: String) {
            // given
            val deserializer = AnimeListJsonStringDeserializer()

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                deserializer.deserialize(input)
            }

            // then
            assertThat(result).hasMessage("Given JSON string must not be blank.")
        }

        @Test
        fun `correctly deserialize dataset string`() {
            runBlocking {
                // given
                val deserializer = AnimeListJsonStringDeserializer()

                val expectedEntries = listOf(
                    TestAnimeObjects.DefaultAnime.obj,
                    TestAnimeObjects.AllPropertiesSet.obj,
                    TestAnimeObjects.FullyMergedAllPropertiesSet.obj,
                    TestAnimeObjects.NullableNotSet.obj,
                )

                val prettyPrintDataset = createExpectedDatasetPrettyPrint(
                    TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                    TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint,
                    TestAnimeObjects.FullyMergedAllPropertiesSet.serializedPrettyPrint,
                    TestAnimeObjects.NullableNotSet.serializedPrettyPrint,
                )

                // when
                val result = deserializer.deserialize(prettyPrintDataset)

                // then
                assertThat(result.data).containsAll(expectedEntries)
            }
        }

        @Test
        fun `correctly deserialize minified dataset string`() {
            runBlocking {
                // given
                val deserializer = AnimeListJsonStringDeserializer()

                val expectedEntries = listOf(
                    TestAnimeObjects.DefaultAnime.obj,
                    TestAnimeObjects.AllPropertiesSet.obj,
                    TestAnimeObjects.FullyMergedAllPropertiesSet.obj,
                    TestAnimeObjects.NullableNotSet.obj,
                )

                val minifiedDataset = createExpectedDatasetMinified(
                    TestAnimeObjects.DefaultAnime.serializedMinified,
                    TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    TestAnimeObjects.FullyMergedAllPropertiesSet.serializedMinified,
                    TestAnimeObjects.NullableNotSet.serializedMinified,
                )

                // when
                val result = deserializer.deserialize(minifiedDataset)

                // then
                assertThat(result.data).containsAll(expectedEntries)
            }
        }
    }

    @Nested
    inner class DeserializeLifecycleAwareInputStreamTests {

        @Test
        fun `throws exception if the stream is closed`() {
            // given
            val deserializer = AnimeListJsonStringDeserializer()
            val inputStream = LifecycleAwareInputStream(TestReadOnceInputStream("test".byteInputStream()))
            inputStream.close()

            // when
            val result = exceptionExpected<IllegalArgumentException> {
                deserializer.deserialize(inputStream)
            }

            // then
            assertThat(result).hasMessage("Stream must not be closed.")
        }

        @Test
        fun `correctly deserialize dataset string`() {
            runBlocking {
                // given
                val deserializer = AnimeListJsonStringDeserializer()

                val expectedEntries = listOf(
                    TestAnimeObjects.DefaultAnime.obj,
                    TestAnimeObjects.AllPropertiesSet.obj,
                    TestAnimeObjects.FullyMergedAllPropertiesSet.obj,
                    TestAnimeObjects.NullableNotSet.obj,
                )

                val prettyPrintDataset = createExpectedDatasetPrettyPrint(
                    TestAnimeObjects.DefaultAnime.serializedPrettyPrint,
                    TestAnimeObjects.AllPropertiesSet.serializedPrettyPrint,
                    TestAnimeObjects.FullyMergedAllPropertiesSet.serializedPrettyPrint,
                    TestAnimeObjects.NullableNotSet.serializedPrettyPrint,
                )

                val inputStream = LifecycleAwareInputStream(TestReadOnceInputStream(prettyPrintDataset.byteInputStream()))


                // when
                val result = deserializer.deserialize(inputStream)

                // then
                assertThat(result.data).containsAll(expectedEntries)
            }
        }

        @Test
        fun `correctly deserialize minified dataset string`() {
            runBlocking {
                // given
                val deserializer = AnimeListJsonStringDeserializer()

                val expectedEntries = listOf(
                    TestAnimeObjects.DefaultAnime.obj,
                    TestAnimeObjects.AllPropertiesSet.obj,
                    TestAnimeObjects.FullyMergedAllPropertiesSet.obj,
                    TestAnimeObjects.NullableNotSet.obj,
                )

                val minifiedDataset = createExpectedDatasetMinified(
                    TestAnimeObjects.DefaultAnime.serializedMinified,
                    TestAnimeObjects.AllPropertiesSet.serializedMinified,
                    TestAnimeObjects.FullyMergedAllPropertiesSet.serializedMinified,
                    TestAnimeObjects.NullableNotSet.serializedMinified,
                )

                val inputStream = LifecycleAwareInputStream(TestReadOnceInputStream(minifiedDataset.byteInputStream()))

                // when
                val result = deserializer.deserialize(inputStream)

                // then
                assertThat(result.data).containsAll(expectedEntries)
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = AnimeListJsonStringDeserializer.instance

            // when
            val result = AnimeListJsonStringDeserializer.instance

            // then
            assertThat(result).isExactlyInstanceOf(AnimeListJsonStringDeserializer::class.java)
            assertThat(result===previous).isTrue()
        }
    }
}