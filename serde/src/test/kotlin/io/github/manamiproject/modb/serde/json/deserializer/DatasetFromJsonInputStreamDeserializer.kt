package io.github.manamiproject.modb.serde.json.deserializer

import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import io.github.manamiproject.modb.serde.TestAnimeObjects
import io.github.manamiproject.modb.serde.TestReadOnceInputStream
import io.github.manamiproject.modb.serde.createExpectedDatasetMinified
import io.github.manamiproject.modb.serde.createExpectedDatasetPrettyPrint
import io.github.manamiproject.modb.test.exceptionExpected
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class DatasetFromJsonInputStreamDeserializerTest {

    @Nested
    inner class DeserializeTests {

        @Test
        fun `throws exception if the stream is closed`() {
            // given
            val deserializer = DatasetFromJsonInputStreamDeserializer()
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
                val deserializer = DatasetFromJsonInputStreamDeserializer()

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
                val deserializer = DatasetFromJsonInputStreamDeserializer()

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
            val previous = DatasetFromJsonInputStreamDeserializer.instance

            // when
            val result = DatasetFromJsonInputStreamDeserializer.instance

            // then
            assertThat(result).isExactlyInstanceOf(DatasetFromJsonInputStreamDeserializer::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}