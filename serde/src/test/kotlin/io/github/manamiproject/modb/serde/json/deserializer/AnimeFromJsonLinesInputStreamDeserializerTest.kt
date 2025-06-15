package io.github.manamiproject.modb.serde.json.deserializer

import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import io.github.manamiproject.modb.serde.TestAnimeObjects
import io.github.manamiproject.modb.serde.TestReadOnceInputStream
import io.github.manamiproject.modb.test.exceptionExpected
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class AnimeFromJsonLinesInputStreamDeserializerTest {

    @Nested
    inner class DeserializeTests {

        @Test
        fun `throws exception if the stream is closed`() {
            // given
            val deserializer = AnimeFromJsonLinesInputStreamDeserializer()
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
            runBlocking {
                // given
                val deserializer = AnimeFromJsonLinesInputStreamDeserializer()
                // FIXME: schema
                val input = """
                     {"${"$"}schema":"","license":{"name":"Open Data Commons Open Database License (ODbL) v1.0 + Database Contents License (DbCL) v1.0","url":"https://github.com/manami-project/anime-offline-database/blob/2020-01/LICENSE"},"repository":"https://github.com/manami-project/anime-offline-database","scoreRange":{"minInclusive":1.0,"maxInclusive":10.0},"lastUpdate":"2020-01-01"}
                     ${TestAnimeObjects.AllPropertiesSet.serializedMinified}
                     ${TestAnimeObjects.NullableNotSet.serializedMinified}
                     ${TestAnimeObjects.DefaultAnime.serializedMinified}
                 """.trimIndent()

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
            runBlocking {
                // given
                val deserializer = AnimeFromJsonLinesInputStreamDeserializer()

                val input = """
                     {"${"$"}schema":"","license":{"name":"Open Data Commons Open Database License (ODbL) v1.0 + Database Contents License (DbCL) v1.0","url":"https://github.com/manami-project/anime-offline-database/blob/2020-01/LICENSE"},"repository":"https://github.com/manami-project/anime-offline-database","scoreRange":{"minInclusive":1.0,"maxInclusive":10.0},"lastUpdate":"2020-01-01"}
                 """.trimIndent()

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
            val previous = AnimeFromJsonLinesInputStreamDeserializer.instance

            // when
            val result = AnimeFromJsonLinesInputStreamDeserializer.instance

            // then
            assertThat(result).isExactlyInstanceOf(AnimeFromJsonLinesInputStreamDeserializer::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}