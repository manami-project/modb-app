package io.github.manamiproject.modb.core.config

import io.github.manamiproject.modb.core.TestConfigRegistry
import io.github.manamiproject.modb.core.TestKProperty
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.test.exceptionExpected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import kotlin.reflect.KProperty
import kotlin.test.Test

internal class PropertiesKtTest {

    @Nested
    inner class StringPropertyDelegateTests {

        @Nested
        inner class RegularConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String? = null
                }

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                val property = StringPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }

            @Test
            fun `throws an exception if the value is not present and there is no default`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String? = null
                }

                val property = StringPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Unable to find property [modb.core.unittest.testProp]. Property doesn't have a default set.")
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String? {
                        lookup = key
                        return null
                    }
                }

                val property = StringPropertyDelegate(
                    configRegistry = testConfigRegistry,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(lookup).isEqualTo("kotlinx.coroutines.StandaloneCoroutine.testProp")
                assertThat(result).hasMessage("Unable to find property [kotlinx.coroutines.StandaloneCoroutine.testProp]. Property doesn't have a default set.")
            }

            @Test
            fun `returns value when no default has been defined`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String {
                        lookup = key
                        return "my-value"
                    }
                }

                val property = StringPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("modb.core.unittest.testProp")
                assertThat(result).isEqualTo("my-value")
            }

            @Test
            fun `correctly returns value when custom validator matches`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = "my-value"
                }

                val property = StringPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> value.endsWith("value") },
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo("my-value")
            }

            @Test
            fun `throws exception if validator returns false`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = "my-value"
                }

                val property = StringPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> value.endsWith("non-existent") },
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [my-value] for property [modb.core.unittest.testProp] is invalid.")
            }
        }

        @Nested
        inner class DefaultValueConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String? = null
                }

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                val property = StringPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                    default = "my-default",
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }

            @Test
            fun `returns default value if value is not set and a default has been defined`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String? = null
                }

                val property = StringPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = "my-default",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo("my-default")
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String? {
                        lookup = key
                        return null
                    }
                }

                val property = StringPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    default = "my-default",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("io.github.manamiproject.modb.core.config.PropertiesKtTest.StringPropertyDelegateTests.DefaultValueConstructorTests.testProp")
            }

            @Test
            fun `returns value when a default has been defined`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String {
                        lookup = key
                        return "my-value"
                    }
                }

                val property = StringPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = "my-default",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("modb.core.unittest.testProp")
                assertThat(result).isEqualTo("my-value")
            }

            @Test
            fun `correctly returns value when custom validator matches`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = "my-value"
                }

                val property = StringPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = "my-default",
                    validator = { value -> value.endsWith("value") },
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo("my-value")
            }

            @Test
            fun `throws exception if validator returns false`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun string(key: String): String = "my-value"
                }

                val property = StringPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = "my-default",
                    validator = { value -> value.endsWith("non-existent") },
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [my-value] for property [modb.core.unittest.testProp] is invalid.")
            }
        }
    }

    @Nested
    inner class LongPropertyDelegateTests {

        @Nested
        inner class RegularConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun long(key: String): Long? = null
                }

                val property = LongPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }

            @Test
            fun `throws an exception if the value is not present and there is no default`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun long(key: String): Long? = null
                }

                val property = LongPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Unable to find property [modb.core.unittest.testProp]. Property doesn't have a default set.")
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun long(key: String): Long? {
                        lookup = key
                        return null
                    }
                }

                val property = LongPropertyDelegate(
                    configRegistry = testConfigRegistry,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(lookup).isEqualTo("kotlinx.coroutines.StandaloneCoroutine.testProp")
                assertThat(result).hasMessage("Unable to find property [kotlinx.coroutines.StandaloneCoroutine.testProp]. Property doesn't have a default set.")
            }

            @Test
            fun `returns value when no default has been defined`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun long(key: String): Long {
                        lookup = key
                        return 128L
                    }
                }

                val property = LongPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("modb.core.unittest.testProp")
                assertThat(result).isEqualTo(128L)
            }

            @Test
            fun `correctly returns value when custom validator matches`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun long(key: String): Long = 128L
                }

                val property = LongPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> value > 100L }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(128L)
            }

            @Test
            fun `throws exception if validator returns false`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun long(key: String): Long = 128L
                }

                val property = LongPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> value < 100L }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [128] for property [modb.core.unittest.testProp] is invalid.")
            }
        }

        @Nested
        inner class DefaultValueConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun long(key: String): Long? = null
                }

                val property = LongPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                    default = 456L,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }

            @Test
            fun `returns default value if value is not set and a default has been defined`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun long(key: String): Long? = null
                }

                val property = LongPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = 456L,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(456L)
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun long(key: String): Long? {
                        lookup = key
                        return null
                    }
                }

                val property = LongPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    default = 456L,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("io.github.manamiproject.modb.core.config.PropertiesKtTest.LongPropertyDelegateTests.DefaultValueConstructorTests.testProp")
            }

            @Test
            fun `returns value when a default has been defined`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun long(key: String): Long = 128L
                }

                val property = LongPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = 456L,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(128L)
            }

            @Test
            fun `correctly returns value when custom validator matches`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun long(key: String): Long = 128L
                }

                val property = LongPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = 456L,
                    validator = { value -> value > 100L }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(128L)
            }

            @Test
            fun `throws exception if validator returns false`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun long(key: String): Long = 128L
                }

                val property = LongPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = 456L,
                    validator = { value -> value < 100L }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [128] for property [modb.core.unittest.testProp] is invalid.")
            }
        }
    }

    @Nested
    inner class IntPropertyDelegateTests {

        @Nested
        inner class RegularConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun int(key: String): Int? = null
                }

                val property = IntPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }

            @Test
            fun `throws an exception if the value is not present and there is no default`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun int(key: String): Int? = null
                }

                val property = IntPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Unable to find property [modb.core.unittest.testProp]. Property doesn't have a default set.")
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun int(key: String): Int? {
                        lookup = key
                        return null
                    }
                }

                val property = IntPropertyDelegate(
                    configRegistry = testConfigRegistry,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(lookup).isEqualTo("kotlinx.coroutines.StandaloneCoroutine.testProp")
                assertThat(result).hasMessage("Unable to find property [kotlinx.coroutines.StandaloneCoroutine.testProp]. Property doesn't have a default set.")
            }

            @Test
            fun `returns value when no default has been defined`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun int(key: String): Int {
                        lookup = key
                        return 128
                    }
                }

                val property = IntPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("modb.core.unittest.testProp")
                assertThat(result).isEqualTo(128L)
            }

            @Test
            fun `correctly returns value when custom validator matches`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun int(key: String): Int = 128
                }

                val property = IntPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> value > 100L }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(128L)
            }

            @Test
            fun `throws exception if validator returns false`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun int(key: String): Int = 128
                }

                val property = IntPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> value < 100L }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [128] for property [modb.core.unittest.testProp] is invalid.")
            }
        }

        @Nested
        inner class DefaultValueConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun int(key: String): Int? = null
                }

                val property = IntPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                    default = 456,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }

            @Test
            fun `returns default value if value is not set and a default has been defined`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun int(key: String): Int? = null
                }

                val property = IntPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = 456,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(456)
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun int(key: String): Int? {
                        lookup = key
                        return null
                    }
                }

                val property = IntPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    default = 456,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("io.github.manamiproject.modb.core.config.PropertiesKtTest.IntPropertyDelegateTests.DefaultValueConstructorTests.testProp")
            }

            @Test
            fun `returns value when a default has been defined`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun int(key: String): Int = 128
                }

                val property = IntPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = 456,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(128)
            }

            @Test
            fun `correctly returns value when custom validator matches`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun int(key: String): Int = 128
                }

                val property = IntPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = 456,
                    validator = { value -> value > 100 }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(128)
            }

            @Test
            fun `throws exception if validator returns false`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun int(key: String): Int = 128
                }

                val property = IntPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = 456,
                    validator = { value -> value < 100 }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [128] for property [modb.core.unittest.testProp] is invalid.")
            }
        }
    }

    @Nested
    inner class DoublePropertyDelegateTests {

        @Nested
        inner class RegularConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun double(key: String): Double? = null
                }

                val property = DoublePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }

            @Test
            fun `throws an exception if the value is not present and there is no default`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun double(key: String): Double? = null
                }

                val property = DoublePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Unable to find property [modb.core.unittest.testProp]. Property doesn't have a default set.")
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun double(key: String): Double? {
                        lookup = key
                        return null
                    }
                }

                val property = DoublePropertyDelegate(
                    configRegistry = testConfigRegistry,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(lookup).isEqualTo("kotlinx.coroutines.StandaloneCoroutine.testProp")
                assertThat(result).hasMessage("Unable to find property [kotlinx.coroutines.StandaloneCoroutine.testProp]. Property doesn't have a default set.")
            }

            @Test
            fun `returns value when no default has been defined`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun double(key: String): Double {
                        lookup = key
                        return 128.43
                    }
                }

                val property = DoublePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("modb.core.unittest.testProp")
                assertThat(result).isEqualTo(128.43)
            }

            @Test
            fun `correctly returns value when custom validator matches`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun double(key: String): Double = 128.43
                }

                val property = DoublePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> value > 100L }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(128.43)
            }

            @Test
            fun `throws exception if validator returns false`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun double(key: String): Double = 128.43
                }

                val property = DoublePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> value < 100L }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [128.43] for property [modb.core.unittest.testProp] is invalid.")
            }
        }

        @Nested
        inner class DefaultValueConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun double(key: String): Double? = null
                }

                val property = DoublePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                    default = 456.12,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }

            @Test
            fun `returns default value if value is not set and a default has been defined`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun double(key: String): Double? = null
                }

                val property = DoublePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = 456.12,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(456.12)
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun double(key: String): Double? {
                        lookup = key
                        return null
                    }
                }

                val property = DoublePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    default = 456.12,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("io.github.manamiproject.modb.core.config.PropertiesKtTest.DoublePropertyDelegateTests.DefaultValueConstructorTests.testProp")
            }

            @Test
            fun `returns value when a default has been defined`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun double(key: String): Double = 128.43
                }

                val property = DoublePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = 456.12,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(128.43)
            }

            @Test
            fun `correctly returns value when custom validator matches`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun double(key: String): Double = 128.43
                }

                val property = DoublePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = 456.12,
                    validator = { value -> value > 100L }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(128.43)
            }

            @Test
            fun `throws exception if validator returns false`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun double(key: String): Double = 128.43
                }

                val property = DoublePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = 456.12,
                    validator = { value -> value < 100L }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [128.43] for property [modb.core.unittest.testProp] is invalid.")
            }
        }
    }

    @Nested
    inner class BooleanPropertyDelegateTests {

        @Nested
        inner class RegularConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun boolean(key: String): Boolean? = null
                }

                val property = BooleanPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }

            @Test
            fun `throws an exception if the value is not present and there is no default`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun boolean(key: String): Boolean? = null
                }

                val property = BooleanPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Unable to find property [modb.core.unittest.testProp]. Property doesn't have a default set.")
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun boolean(key: String): Boolean? {
                        lookup = key
                        return null
                    }
                }

                val property = BooleanPropertyDelegate(
                    configRegistry = testConfigRegistry,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(lookup).isEqualTo("kotlinx.coroutines.StandaloneCoroutine.testProp")
                assertThat(result).hasMessage("Unable to find property [kotlinx.coroutines.StandaloneCoroutine.testProp]. Property doesn't have a default set.")
            }

            @Test
            fun `returns value when no default has been defined`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun boolean(key: String): Boolean {
                        lookup = key
                        return true
                    }
                }

                val property = BooleanPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("modb.core.unittest.testProp")
                assertThat(result).isTrue()
            }

            @Test
            fun `correctly returns value when custom validator matches`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun boolean(key: String): Boolean = true
                }

                val property = BooleanPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> value }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isTrue()
            }

            @Test
            fun `throws exception if validator returns false`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun boolean(key: String): Boolean = true
                }

                val property = BooleanPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> !value }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [true] for property [modb.core.unittest.testProp] is invalid.")
            }
        }

        @Nested
        inner class DefaultValueConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun boolean(key: String): Boolean? = null
                }

                val property = BooleanPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                    default = false,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }
            
            @Test
            fun `returns default value if value is not set and a default has been defined`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun boolean(key: String): Boolean? = null
                }

                val property = BooleanPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = false,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isFalse()
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun boolean(key: String): Boolean? {
                        lookup = key
                        return null
                    }
                }

                val property = BooleanPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    default = false,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("io.github.manamiproject.modb.core.config.PropertiesKtTest.BooleanPropertyDelegateTests.DefaultValueConstructorTests.testProp")
            }

            @Test
            fun `returns value when a default has been defined`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun boolean(key: String): Boolean = true
                }

                val property = BooleanPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = false,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isTrue()
            }

            @Test
            fun `correctly returns value when custom validator matches`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun boolean(key: String): Boolean = true
                }

                val property = BooleanPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = false,
                    validator = { value -> value }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isTrue()
            }

            @Test
            fun `throws exception if validator returns false`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun boolean(key: String): Boolean = true
                }

                val property = BooleanPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = false,
                    validator = { value -> !value }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [true] for property [modb.core.unittest.testProp] is invalid.")
            }
        }
    }

    @Nested
    inner class LocalDatePropertyDelegateTests {

        @Nested
        inner class RegularConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDate(key: String): LocalDate? = null
                }

                val property = LocalDatePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }

            @Test
            fun `throws an exception if the value is not present and there is no default`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDate(key: String): LocalDate? = null
                }

                val property = LocalDatePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Unable to find property [modb.core.unittest.testProp]. Property doesn't have a default set.")
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDate(key: String): LocalDate? {
                        lookup = key
                        return null
                    }
                }

                val property = LocalDatePropertyDelegate(
                    configRegistry = testConfigRegistry,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(lookup).isEqualTo("kotlinx.coroutines.StandaloneCoroutine.testProp")
                assertThat(result).hasMessage("Unable to find property [kotlinx.coroutines.StandaloneCoroutine.testProp]. Property doesn't have a default set.")
            }

            @Test
            fun `returns value when no default has been defined`() {
                // given
                val value = LocalDate.now().minusDays(2)

                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDate(key: String): LocalDate {
                        lookup = key
                        return value
                    }
                }

                val property = LocalDatePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("modb.core.unittest.testProp")
                assertThat(result).isEqualTo(value)
            }

            @Test
            fun `correctly returns value when custom validator matches`() {
                // given
                val date = LocalDate.now().minusDays(2)

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDate(key: String): LocalDate = date
                }

                val property = LocalDatePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> value.isBefore(LocalDate.now()) }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(date)
            }

            @Test
            fun `throws exception if validator returns false`() {
                // given
                val date = LocalDate.now().minusDays(2)

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDate(key: String): LocalDate = date
                }

                val property = LocalDatePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> value.isAfter(LocalDate.now()) }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [${date}] for property [modb.core.unittest.testProp] is invalid.")
            }
        }

        @Nested
        inner class DefaultValueConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val defaultValue = LocalDate.now()

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDate(key: String): LocalDate? = null
                }

                val property = LocalDatePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                    default = defaultValue,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }

            @Test
            fun `returns default value if value is not set and a default has been defined`() {
                // given
                val defaultValue = LocalDate.now()

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDate(key: String): LocalDate? = null
                }

                val property = LocalDatePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = defaultValue,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(defaultValue)
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                val defaultValue = LocalDate.now()

                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDate(key: String): LocalDate? {
                        lookup = key
                        return null
                    }
                }

                val property = LocalDatePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    default = defaultValue,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("io.github.manamiproject.modb.core.config.PropertiesKtTest.LocalDatePropertyDelegateTests.DefaultValueConstructorTests.testProp")
            }

            @Test
            fun `returns value when a default has been defined`() {
                // given
                val defaultValue = LocalDate.now()
                val value = defaultValue.minusDays(2)

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDate(key: String): LocalDate = value
                }

                val property = LocalDatePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = defaultValue,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(value)
            }

            @Test
            fun `correctly returns value when custom validator matches`() {
                // given
                val defaultValue = LocalDate.now()
                val date = LocalDate.now().minusDays(2)

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDate(key: String): LocalDate = date
                }

                val property = LocalDatePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = defaultValue,
                    validator = { value -> value.isBefore(LocalDate.now()) }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(date)
            }

            @Test
            fun `throws exception if validator returns false`() {
                // given
                val defaultValue = LocalDate.now()
                val date = LocalDate.now().minusDays(2)

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDate(key: String): LocalDate = date
                }

                val property = LocalDatePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = defaultValue,
                    validator = { value -> value.isAfter(LocalDate.now()) }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [${date}] for property [modb.core.unittest.testProp] is invalid.")
            }
        }
    }

    @Nested
    inner class LocalDateTimePropertyDelegateTests {

        @Nested
        inner class RegularConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDateTime(key: String): LocalDateTime? = null
                }

                val property = LocalDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }

            @Test
            fun `throws an exception if the value is not present and there is no default`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDateTime(key: String): LocalDateTime? = null
                }

                val property = LocalDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Unable to find property [modb.core.unittest.testProp]. Property doesn't have a default set.")
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDateTime(key: String): LocalDateTime? {
                        lookup = key
                        return null
                    }
                }

                val property = LocalDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(lookup).isEqualTo("kotlinx.coroutines.StandaloneCoroutine.testProp")
                assertThat(result).hasMessage("Unable to find property [kotlinx.coroutines.StandaloneCoroutine.testProp]. Property doesn't have a default set.")
            }

            @Test
            fun `returns value when no default has been defined`() {
                // given
                val value = LocalDateTime.now().minusDays(2)

                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDateTime(key: String): LocalDateTime {
                        lookup = key
                        return value
                    }
                }

                val property = LocalDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("modb.core.unittest.testProp")
                assertThat(result).isEqualTo(value)
            }

            @Test
            fun `correctly returns value when custom validator matches`() {
                // given
                val date = LocalDateTime.now().minusDays(2)

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDateTime(key: String): LocalDateTime = date
                }

                val property = LocalDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> value.isBefore(LocalDateTime.now()) }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(date)
            }

            @Test
            fun `throws exception if validator returns false`() {
                // given
                val date = LocalDateTime.now().minusDays(2)

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDateTime(key: String): LocalDateTime = date
                }

                val property = LocalDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> value.isAfter(LocalDateTime.now()) }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [${date}] for property [modb.core.unittest.testProp] is invalid.")
            }
        }

        @Nested
        inner class DefaultValueConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val defaultValue = LocalDateTime.now()

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDateTime(key: String): LocalDateTime? = null
                }

                val property = LocalDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                    default = defaultValue,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }

            @Test
            fun `returns default value if value is not set and a default has been defined`() {
                // given
                val defaultValue = LocalDateTime.now()

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDateTime(key: String): LocalDateTime? = null
                }

                val property = LocalDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = defaultValue,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(defaultValue)
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                val defaultValue = LocalDateTime.now()

                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDateTime(key: String): LocalDateTime? {
                        lookup = key
                        return null
                    }
                }

                val property = LocalDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    default = defaultValue,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("io.github.manamiproject.modb.core.config.PropertiesKtTest.LocalDateTimePropertyDelegateTests.DefaultValueConstructorTests.testProp")
            }

            @Test
            fun `returns value when a default has been defined`() {
                // given
                val defaultValue = LocalDateTime.now()
                val value = defaultValue.minusDays(2)

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDateTime(key: String): LocalDateTime = value
                }

                val property = LocalDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = defaultValue,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(value)
            }

            @Test
            fun `correctly returns value when custom validator matches`() {
                // given
                val defaultValue = LocalDateTime.now()
                val date = LocalDateTime.now().minusDays(2)

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDateTime(key: String): LocalDateTime = date
                }

                val property = LocalDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = defaultValue,
                    validator = { value -> value.isBefore(LocalDateTime.now()) }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(date)
            }

            @Test
            fun `throws exception if validator returns false`() {
                // given
                val defaultValue = LocalDateTime.now()
                val date = LocalDateTime.now().minusDays(2)

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun localDateTime(key: String): LocalDateTime = date
                }

                val property = LocalDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = defaultValue,
                    validator = { value -> value.isAfter(LocalDateTime.now()) }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [${date}] for property [modb.core.unittest.testProp] is invalid.")
            }
        }
    }

    @Nested
    inner class OffsetDateTimePropertyDelegateTests {

        @Nested
        inner class RegularConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun offsetDateTime(key: String): OffsetDateTime? = null
                }

                val property = OffsetDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }

            @Test
            fun `throws an exception if the value is not present and there is no default`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun offsetDateTime(key: String): OffsetDateTime? = null
                }

                val property = OffsetDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Unable to find property [modb.core.unittest.testProp]. Property doesn't have a default set.")
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun offsetDateTime(key: String): OffsetDateTime? {
                        lookup = key
                        return null
                    }
                }

                val property = OffsetDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(lookup).isEqualTo("kotlinx.coroutines.StandaloneCoroutine.testProp")
                assertThat(result).hasMessage("Unable to find property [kotlinx.coroutines.StandaloneCoroutine.testProp]. Property doesn't have a default set.")
            }

            @Test
            fun `returns value when no default has been defined`() {
                // given
                val value = OffsetDateTime.now().minusDays(2)

                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun offsetDateTime(key: String): OffsetDateTime {
                        lookup = key
                        return value
                    }
                }

                val property = OffsetDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("modb.core.unittest.testProp")
                assertThat(result).isEqualTo(value)
            }

            @Test
            fun `correctly returns value when custom validator matches`() {
                // given
                val date = OffsetDateTime.now().minusDays(2)

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun offsetDateTime(key: String): OffsetDateTime = date
                }

                val property = OffsetDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> value.isBefore(OffsetDateTime.now()) }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(date)
            }

            @Test
            fun `throws exception if validator returns false`() {
                // given
                val date = OffsetDateTime.now().minusDays(2)

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun offsetDateTime(key: String): OffsetDateTime = date
                }

                val property = OffsetDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> value.isAfter(OffsetDateTime.now()) }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [${date}] for property [modb.core.unittest.testProp] is invalid.")
            }
        }

        @Nested
        inner class DefaultValueConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val defaultValue = OffsetDateTime.now()

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun offsetDateTime(key: String): OffsetDateTime? = null
                }

                val property = OffsetDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                    default = defaultValue,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }

            @Test
            fun `returns default value if value is not set and a default has been defined`() {
                // given
                val defaultValue = OffsetDateTime.now()

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun offsetDateTime(key: String): OffsetDateTime? = null
                }

                val property = OffsetDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = defaultValue,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(defaultValue)
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                val defaultValue = OffsetDateTime.now()

                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun offsetDateTime(key: String): OffsetDateTime? {
                        lookup = key
                        return null
                    }
                }

                val property = OffsetDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    default = defaultValue,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("io.github.manamiproject.modb.core.config.PropertiesKtTest.OffsetDateTimePropertyDelegateTests.DefaultValueConstructorTests.testProp")
            }

            @Test
            fun `returns value when a default has been defined`() {
                // given
                val defaultValue = OffsetDateTime.now()
                val value = defaultValue.minusDays(2)

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun offsetDateTime(key: String): OffsetDateTime = value
                }

                val property = OffsetDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = defaultValue,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(value)
            }

            @Test
            fun `correctly returns value when custom validator matches`() {
                // given
                val defaultValue = OffsetDateTime.now()
                val date = OffsetDateTime.now().minusDays(2)

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun offsetDateTime(key: String): OffsetDateTime = date
                }

                val property = OffsetDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = defaultValue,
                    validator = { value -> value.isBefore(OffsetDateTime.now()) }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(date)
            }

            @Test
            fun `throws exception if validator returns false`() {
                // given
                val defaultValue = OffsetDateTime.now()
                val date = OffsetDateTime.now().minusDays(2)

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun offsetDateTime(key: String): OffsetDateTime = date
                }

                val property = OffsetDateTimePropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = defaultValue,
                    validator = { value -> value.isAfter(OffsetDateTime.now()) }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [${date}] for property [modb.core.unittest.testProp] is invalid.")
            }
        }
    }

    @Nested
    inner class ListPropertyDelegateTests {

        @Nested
        inner class RegularConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T>? = null
                }

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                val property = ListPropertyDelegate<String>(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }

            @Test
            fun `throws an exception if the value is not present and there is no default`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T>? = null
                }

                val property = ListPropertyDelegate<String>(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Unable to find property [modb.core.unittest.testProp]. Property doesn't have a default set.")
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T>? {
                        lookup = key
                        return null
                    }
                }

                val property = ListPropertyDelegate<String>(
                    configRegistry = testConfigRegistry,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(lookup).isEqualTo("kotlinx.coroutines.StandaloneCoroutine.testProp")
                assertThat(result).hasMessage("Unable to find property [kotlinx.coroutines.StandaloneCoroutine.testProp]. Property doesn't have a default set.")
            }

            @Test
            @Suppress("UNCHECKED_CAST")
            fun `returns value when no default has been defined`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T> {
                        lookup = key
                        return listOf("my-value") as List<T>
                    }
                }

                val property = ListPropertyDelegate<String>(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("modb.core.unittest.testProp")
                assertThat(result).isEqualTo(listOf("my-value"))
            }

            @Test
            @Suppress("UNCHECKED_CAST")
            fun `correctly returns value when custom validator matches`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T> = listOf("my-value") as List<T>
                }

                val property = ListPropertyDelegate<String>(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> value.isNotEmpty() }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(listOf("my-value"))
            }

            @Test
            @Suppress("UNCHECKED_CAST")
            fun `throws exception if validator returns false`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T> = listOf("my-value") as List<T>
                }

                val property = ListPropertyDelegate<String>(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> value.isEmpty() }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [my-value] for property [modb.core.unittest.testProp] is invalid.")
            }
        }

        @Nested
        inner class DefaultValueConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T>? = null
                }

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                val property = ListPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                    default = listOf("my-default"),
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }

            @Test
            fun `returns default value if value is not set and a default has been defined`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T>? = null
                }

                val property = ListPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = listOf("my-default"),
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(listOf("my-default"))
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T>? {
                        lookup = key
                        return null
                    }
                }

                val property = ListPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    default = listOf("my-default"),
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("io.github.manamiproject.modb.core.config.PropertiesKtTest.ListPropertyDelegateTests.DefaultValueConstructorTests.testProp")
            }

            @Test
            @Suppress("UNCHECKED_CAST")
            fun `returns value when a default has been defined`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T> = listOf("my-value") as List<T>
                }

                val property = ListPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = listOf("my-default"),
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(listOf("my-value"))
            }

            @Test
            @Suppress("UNCHECKED_CAST")
            fun `correctly returns value when custom validator matches`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T> = listOf("my-value") as List<T>
                }

                val property = ListPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = listOf("my-default"),
                    validator = { value -> value.isNotEmpty() }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(listOf("my-value"))
            }

            @Test
            @Suppress("UNCHECKED_CAST")
            fun `throws exception if validator returns false`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T> = listOf("my-value") as List<T>
                }

                val property = ListPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = listOf("my-default"),
                    validator = { value -> value.isEmpty() }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [my-value] for property [modb.core.unittest.testProp] is invalid.")
            }
        }
    }

    @Nested
    inner class SetPropertyDelegateTests {

        @Nested
        inner class RegularConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T>? = null
                }

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                val property = SetPropertyDelegate<String>(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }

            @Test
            fun `throws an exception if the value is not present and there is no default`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T>? = null
                }

                val property = SetPropertyDelegate<String>(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Unable to find property [modb.core.unittest.testProp]. Property doesn't have a default set.")
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T>? {
                        lookup = key
                        return null
                    }
                }

                val property = SetPropertyDelegate<String>(
                    configRegistry = testConfigRegistry,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(lookup).isEqualTo("kotlinx.coroutines.StandaloneCoroutine.testProp")
                assertThat(result).hasMessage("Unable to find property [kotlinx.coroutines.StandaloneCoroutine.testProp]. Property doesn't have a default set.")
            }

            @Test
            @Suppress("UNCHECKED_CAST")
            fun `returns value when no default has been defined`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T> {
                        lookup = key
                        return listOf(
                            "one",
                            "one",
                            "two",
                            "two",
                        ) as List<T>
                    }
                }

                val property = SetPropertyDelegate<String>(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("modb.core.unittest.testProp")
                assertThat(result).isEqualTo(setOf(
                    "one",
                    "two",
                ))
            }

            @Test
            @Suppress("UNCHECKED_CAST")
            fun `correctly returns value when custom validator matches`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T> = listOf(
                        "one",
                        "one",
                        "two",
                        "two",
                    ) as List<T>
                }

                val property = SetPropertyDelegate<String>(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> value.isNotEmpty() }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(setOf(
                    "one",
                    "two",
                ))
            }

            @Test
            @Suppress("UNCHECKED_CAST")
            fun `throws exception if validator returns false`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T> = listOf(
                        "one",
                        "one",
                        "two",
                        "two",
                    ) as List<T>
                }

                val property = SetPropertyDelegate<String>(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> value.isEmpty() }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [one, two] for property [modb.core.unittest.testProp] is invalid.")
            }
        }

        @Nested
        inner class DefaultValueConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T>? = null
                }

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                val property = SetPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                    default = setOf("my-default"),
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }

            @Test
            fun `returns default value if value is not set and a default has been defined`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T>? = null
                }

                val property = SetPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = setOf("my-default"),
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(setOf("my-default"))
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T>? {
                        lookup = key
                        return null
                    }
                }

                val property = ListPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    default = listOf("my-default"),
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("io.github.manamiproject.modb.core.config.PropertiesKtTest.SetPropertyDelegateTests.DefaultValueConstructorTests.testProp")
            }

            @Test
            @Suppress("UNCHECKED_CAST")
            fun `returns value when a default has been defined`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T> = listOf(
                        "one",
                        "one",
                        "two",
                        "two",
                    ) as List<T>
                }

                val property = SetPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = setOf("my-default"),
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(setOf(
                    "one",
                    "two",
                ))
            }

            @Test
            @Suppress("UNCHECKED_CAST")
            fun `correctly returns value when custom validator matches`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T> = listOf(
                        "one",
                        "one",
                        "two",
                        "two",
                    ) as List<T>
                }

                val property = SetPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = setOf("my-default"),
                    validator = { value -> value.isNotEmpty() }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(setOf(
                    "one",
                    "two",
                ))
            }

            @Test
            @Suppress("UNCHECKED_CAST")
            fun `throws exception if validator returns false`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> list(key: String): List<T> = listOf(
                        "one",
                        "one",
                        "two",
                        "two",
                    ) as List<T>
                }

                val property = SetPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = setOf("my-default"),
                    validator = { value -> value.isEmpty() }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [one, two] for property [modb.core.unittest.testProp] is invalid.")
            }
        }
    }

    @Nested
    inner class MapPropertyDelegateTests {

        @Nested
        inner class RegularConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> map(key: String): Map<String, T>? = null
                }

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                val property = MapPropertyDelegate<Long>(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }

            @Test
            fun `throws an exception if the value is not present and there is no default`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> map(key: String): Map<String, T>? = null
                }

                val property = MapPropertyDelegate<Long>(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Unable to find property [modb.core.unittest.testProp]. Property doesn't have a default set.")
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> map(key: String): Map<String, T>? {
                        lookup = key
                        return null
                    }
                }

                val property = MapPropertyDelegate<Long>(
                    configRegistry = testConfigRegistry,
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(lookup).isEqualTo("kotlinx.coroutines.StandaloneCoroutine.testProp")
                assertThat(result).hasMessage("Unable to find property [kotlinx.coroutines.StandaloneCoroutine.testProp]. Property doesn't have a default set.")
            }

            @Test
            @Suppress("UNCHECKED_CAST")
            fun `returns value when no default has been defined`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> map(key: String): Map<String, T> {
                        lookup = key
                        return mapOf("my-value" to 8080L) as Map<String, T>
                    }
                }

                val property = MapPropertyDelegate<String>(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("modb.core.unittest.testProp")
                assertThat(result).isEqualTo(mapOf("my-value" to 8080L))
            }

            @Test
            @Suppress("UNCHECKED_CAST")
            fun `correctly returns value when custom validator matches`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> map(key: String): Map<String, T> = mapOf("my-value" to 8080L) as Map<String, T>
                }

                val property = MapPropertyDelegate<String>(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> value.containsValue<String, Any>(8080L) }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(mapOf("my-value" to 8080L))
            }

            @Test
            @Suppress("UNCHECKED_CAST")
            fun `throws exception if validator returns false`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> map(key: String): Map<String, T> = mapOf("my-value" to 8080L) as Map<String, T>
                }

                val property = MapPropertyDelegate<String>(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    validator = { value -> value.containsValue<String, Any>(443L) }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [{my-value=8080}] for property [modb.core.unittest.testProp] is invalid.")
            }
        }

        @Nested
        inner class DefaultValueConstructorTests {

            @ParameterizedTest
            @ValueSource(strings = [" ", "!abv", "abc-def"])
            fun `throws an exception if the namespace is not valid`(input: String) {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> map(key: String): Map<String, T>? = null
                }

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                val property = MapPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = input,
                    default = mapOf("my-default" to 5432L),
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Config parameter can only consist of alphanumeric chars and dots. Adjust the namespace of [$input.testProp].")
            }

            @Test
            fun `returns default value if value is not set and a default has been defined`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> map(key: String): Map<String, T>? = null
                }

                val property = MapPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = mapOf("my-default" to 5432L),
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(mapOf(
                    "my-default" to 5432L,
                ))
            }

            @Test
            fun `default namespace is qualified class name of followed by property name`() {
                // given
                var lookup = EMPTY
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> map(key: String): Map<String, T>? {
                        lookup = key
                        return null
                    }
                }

                val property = MapPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    default = mapOf("my-default" to 5432L),
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                property.getValue(this, testKProperty)

                // then
                assertThat(lookup).isEqualTo("io.github.manamiproject.modb.core.config.PropertiesKtTest.MapPropertyDelegateTests.DefaultValueConstructorTests.testProp")
            }

            @Test
            @Suppress("UNCHECKED_CAST")
            fun `returns value when a default has been defined`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> map(key: String): Map<String, T> = mapOf("my-value" to 8080L) as Map<String, T>
                }

                val property = MapPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = mapOf("my-default" to 5432L),
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(mapOf("my-value" to 8080L))
            }

            @Test
            @Suppress("UNCHECKED_CAST")
            fun `correctly returns value when custom validator matches`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> map(key: String): Map<String, T> = mapOf("my-value" to 8080L) as Map<String, T>
                }

                val property = MapPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = mapOf("my-default" to 5432L),
                    validator = { value -> value.containsValue<String, Any>(8080L) }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = property.getValue(this, testKProperty)

                // then
                assertThat(result).isEqualTo(mapOf("my-value" to 8080L))
            }

            @Test
            @Suppress("UNCHECKED_CAST")
            fun `throws exception if validator returns false`() {
                // given
                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun <T: Any> map(key: String): Map<String, T> = mapOf("my-value" to 8080L) as Map<String, T>
                }

                val property = MapPropertyDelegate(
                    configRegistry = testConfigRegistry,
                    namespace = "modb.core.unittest",
                    default = mapOf("my-default" to 5432L),
                    validator = { value -> value.containsValue<String, Any>(443L) }
                )

                val testKProperty = object: KProperty<String> by TestKProperty() {
                    override val name: String = "testProp"
                }

                // when
                val result = exceptionExpected<IllegalStateException> {
                    property.getValue(this, testKProperty)
                }

                // then
                assertThat(result).hasMessage("Value [{my-value=8080}] for property [modb.core.unittest.testProp] is invalid.")
            }
        }
    }
}