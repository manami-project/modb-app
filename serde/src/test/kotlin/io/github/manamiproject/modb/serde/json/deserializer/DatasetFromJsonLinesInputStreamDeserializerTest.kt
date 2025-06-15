package io.github.manamiproject.modb.serde.json.deserializer

import io.github.manamiproject.modb.core.io.LifecycleAwareInputStream
import io.github.manamiproject.modb.serde.TestAnimeObjects
import io.github.manamiproject.modb.serde.TestReadOnceInputStream
import io.github.manamiproject.modb.serde.json.models.Dataset
import io.github.manamiproject.modb.serde.json.models.License
import io.github.manamiproject.modb.test.exceptionExpected
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.Test

internal class DatasetFromJsonLinesInputStreamDeserializerTest {

    @Nested
    inner class DeserializeTests {

        @Test
        fun `throws exception if the stream is closed`() {
            // given
            val deserializer = DatasetFromJsonLinesInputStreamDeserializer()
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
                val deserializer = DatasetFromJsonLinesInputStreamDeserializer()

                val expected = Dataset(
                    `$schema` = URI(""), // FIXME
                    license = License(
                        url = URI("https://github.com/manami-project/anime-offline-database/blob/2020-01/LICENSE"),
                    ),
                    lastUpdate = "2020-01-01",
                    data = listOf(
                        TestAnimeObjects.DefaultAnime.obj,
                        TestAnimeObjects.AllPropertiesSet.obj,
                        TestAnimeObjects.NullableNotSet.obj,
                    )
                )

                val input = """
                     {"${"$"}schema":"","license":{"name":"Open Data Commons Open Database License (ODbL) v1.0 + Database Contents License (DbCL) v1.0","url":"https://github.com/manami-project/anime-offline-database/blob/2020-01/LICENSE"},"repository":"https://github.com/manami-project/anime-offline-database","scoreRange":{"minInclusive":1.0,"maxInclusive":10.0},"lastUpdate":"2020-01-01"}
                     ${TestAnimeObjects.DefaultAnime.serializedMinified}
                     ${TestAnimeObjects.AllPropertiesSet.serializedMinified}
                     ${TestAnimeObjects.NullableNotSet.serializedMinified}
                 """.trimIndent()

                val inputStream = LifecycleAwareInputStream(TestReadOnceInputStream(input.byteInputStream()))


                // when
                val result = deserializer.deserialize(inputStream)

                // then
                assertThat(result).isEqualTo(expected)
            }
        }

        @Test
        fun `correctly deserialize empty dataset`() {
            runBlocking {
                // given
                val deserializer = DatasetFromJsonLinesInputStreamDeserializer()

                val expected = Dataset(
                    `$schema` = URI(""), // FIXME
                    license = License(
                        url = URI("https://github.com/manami-project/anime-offline-database/blob/2020-01/LICENSE"),
                    ),
                    lastUpdate = "2020-01-01",
                    data = emptyList()
                )

                val input = """
                     {"${"$"}schema":"","license":{"name":"Open Data Commons Open Database License (ODbL) v1.0 + Database Contents License (DbCL) v1.0","url":"https://github.com/manami-project/anime-offline-database/blob/2020-01/LICENSE"},"repository":"https://github.com/manami-project/anime-offline-database","scoreRange":{"minInclusive":1.0,"maxInclusive":10.0},"lastUpdate":"2020-01-01"}
                 """.trimIndent()

                val inputStream = LifecycleAwareInputStream(TestReadOnceInputStream(input.byteInputStream()))


                // when
                val result = deserializer.deserialize(inputStream)

                // then
                assertThat(result).isEqualTo(expected)
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = DatasetFromJsonLinesInputStreamDeserializer.instance

            // when
            val result = DatasetFromJsonLinesInputStreamDeserializer.instance

            // then
            assertThat(result).isExactlyInstanceOf(DatasetFromJsonLinesInputStreamDeserializer::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}