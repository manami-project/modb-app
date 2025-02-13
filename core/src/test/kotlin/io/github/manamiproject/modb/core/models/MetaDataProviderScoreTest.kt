package io.github.manamiproject.modb.core.models

import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

internal class MetaDataProviderScoreTest {

    @Nested
    inner class NoMetaDataProviderScoreTests {

        @Test
        fun `NoMetaDataProviderScore is a MetaDataProviderScore`() {
            // then
            assertThat(NoMetaDataProviderScore).isInstanceOf(MetaDataProviderScore::class.java)
        }
    }

    @Nested
    inner class MetaDataProviderScoreValueTests {

        @Nested
        inner class ConstructorTests {

            @Test
            fun `throws exception if the value is negative`() {
                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    MetaDataProviderScoreValue(
                        hostname = "example.org",
                        value = -0.1,
                        originalRange = 1.0..10.0,
                    )
                }

                // then
                assertThat(result).hasMessage("value must be >= 0.0")
            }

            @Test
            fun `throws exception if min value is negative`() {
                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    MetaDataProviderScoreValue(
                        hostname = "example.org",
                        value = 5.0,
                        originalRange = -0.1..10.0,
                    )
                }

                // then
                assertThat(result).hasMessage("originalRange start must be >= 0.0")
            }

            @Test
            fun `throws exception if max value is negative`() {
                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    MetaDataProviderScoreValue(
                        hostname = "example.org",
                        value = 5.0,
                        originalRange = 0.0..-10.0,
                    )
                }

                // then
                assertThat(result).hasMessage("originalRange end must be >= 0.0")
            }

            @ParameterizedTest
            @ValueSource(strings = ["", " ", "   "])
            fun `throws exception if hostname is blank`(input: String) {
                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    MetaDataProviderScoreValue(
                        hostname = input,
                        value = 5.0,
                        originalRange = 1.0..5.0,
                    )
                }

                // then
                assertThat(result).hasMessage("hostname must not be blank")
            }

            @ParameterizedTest
            @ValueSource(strings = ["ksljfdskfjs", "adlkfjsdf.124r", "ab-----cd.f-w", "sk dfj.org"])
            fun `throws exception if hostname has an invalid format`(input: String) {
                // when
                val result = exceptionExpected<IllegalArgumentException> {
                    MetaDataProviderScoreValue(
                        hostname = input,
                        value = 5.0,
                        originalRange = 1.0..5.0,
                    )
                }

                // then
                assertThat(result).hasMessage("hostname has invalid format")
            }
        }

        @Nested
        inner class ScaledValueTests {

            @ParameterizedTest
            @ValueSource(doubles = [5.0, 10.0])
            fun `correctly return 0 for ranges beginning with 1`(input: Double) {
                // given
                val metaDataProviderScoreValue = MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 0.0,
                    originalRange = 1.0..input,
                )

                // when
                val result = metaDataProviderScoreValue.scaledValue()

                // then
                assertThat(result).isZero()
            }

            @ParameterizedTest
            @ValueSource(doubles = [5.0, 10.0])
            fun `correctly return 0 for ranges beginning with 0`(input: Double) {
                // given
                val metaDataProviderScoreValue = MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 0.0,
                    originalRange = 0.0..input,
                )

                // when
                val result = metaDataProviderScoreValue.scaledValue()

                // then
                assertThat(result).isZero()
            }

            @ParameterizedTest
            @ValueSource(doubles = [1.0, 5.0, 10.0])
            fun `correctly keep values as is if the range is already as expected`(input: Double) {
                // given
                val metaDataProviderScoreValue = MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = input,
                    originalRange = 1.0..10.0,
                )

                // when
                val result = metaDataProviderScoreValue.scaledValue()

                // then
                assertThat(result).isEqualTo(input)
            }

            @Test
            fun `correctly rescale value 1 from 1 to 100 range`() {
                // given
                val metaDataProviderScoreValue = MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 1.0,
                    originalRange = 1.0..100.0,
                )

                // when
                val result = metaDataProviderScoreValue.scaledValue()

                // then
                assertThat(result).isEqualTo(1.0)
            }

            @Test
            fun `correctly rescale value 5 from 1 to 100 range`() {
                // given
                val metaDataProviderScoreValue = MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 50.0,
                    originalRange = 1.0..100.0,
                )

                // when
                val result = metaDataProviderScoreValue.scaledValue()

                // then
                assertThat(result).isEqualTo(5.454545454545455)
            }

            @Test
            fun `correctly rescale value 100 from 1 to 100 range`() {
                // given
                val metaDataProviderScoreValue = MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 100.0,
                    originalRange = 1.0..100.0,
                )

                // when
                val result = metaDataProviderScoreValue.scaledValue()

                // then
                assertThat(result).isEqualTo(10.0)
            }

            @Test
            fun `correctly rescale value 1 from 1 to 5 range`() {
                // given
                val metaDataProviderScoreValue = MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 1.0,
                    originalRange = 1.0..5.0,
                )

                // when
                val result = metaDataProviderScoreValue.scaledValue()

                // then
                assertThat(result).isEqualTo(1.0)
            }

            @Test
            fun `correctly rescale value 5 from 1 to 5 range`() {
                // given
                val metaDataProviderScoreValue = MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 3.5,
                    originalRange = 1.0..5.0,
                )

                // when
                val result = metaDataProviderScoreValue.scaledValue()

                // then
                assertThat(result).isEqualTo(6.625)
            }

            @Test
            fun `correctly rescale value 100 from 1 to 5 range`() {
                // given
                val metaDataProviderScoreValue = MetaDataProviderScoreValue(
                    hostname = "example.org",
                    value = 5.0,
                    originalRange = 1.0..5.0,
                )

                // when
                val result = metaDataProviderScoreValue.scaledValue()

                // then
                assertThat(result).isEqualTo(10.0)
            }
        }
    }
}