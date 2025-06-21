package io.github.manamiproject.modb.core.config

import io.github.manamiproject.modb.core.extensions.copyTo
import io.github.manamiproject.modb.test.tempDirectory
import io.github.manamiproject.modb.test.testResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZoneOffset.UTC
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.moveTo
import kotlin.test.Test

internal class DefaultConfigRegistryTest {

    @Nested
    inner class InitializationTests {

        @Test
        fun `correctly returns property from config file in classpath`() {
            // given
            val key = "string.classpath.exclusive"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/classpath/config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isEqualTo("exists only in classpath")
        }

        @Test
        fun `correctly returns property from config file in same directory`() {
            // given
            val key = "string.same.directory.exclusive"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "non-existent.toml",
                localFile = testResource("DefaultConfigRegistryTest/same-directory/config.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isEqualTo("exists only in same directory file")
        }

        @Test
        fun `correctly returns property from config file in custom location set by environment variable`() {
            // given
            val key = "string.custom.location.exclusive"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "non-existent.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = mapOf(
                    "modb.core.config.location" to testResource("DefaultConfigRegistryTest/custom-location-env-var/config.toml").toAbsolutePath().toString(),
                ),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isEqualTo("exists only in custom location config set by environment variable")
        }

        @Test
        fun `correctly returns property from config file in custom location set by system property`() {
            // given
            val key = "string.custom.location.exclusive"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "non-existent.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = mapOf(
                    "modb.core.config.location" to testResource("DefaultConfigRegistryTest/custom-location-system-property/config.toml").toAbsolutePath().toString(),
                ),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isEqualTo("exists only in custom location config set by system property")
        }

        @Test
        fun `returns null if the config file in custom location doesn't exist`() {
            // given
            val key = "string.custom.location.exclusive"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "non-existent.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = mapOf(
                    "modb.core.config.location" to Path("non-existent-config.toml").toAbsolutePath().toString(),
                ),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `correctly returns property from environment variable`() {
            // given
            val key = "string.environment.variables.exclusive"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "non-existent.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = mapOf(
                    key to "exists only in environment variables",
                ),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isEqualTo("exists only in environment variables")
        }

        @Test
        fun `correctly returns property from system properties`() {
            // given
            val key = "string.custom.location.exclusive"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "non-existent.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = mapOf(
                    key to "exists only in system properties",
                ),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isEqualTo("exists only in system properties")
        }

        @Test
        fun `same directory config file overrides classpath`() {
            // given
            val key = "string.override.test"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/classpath/config.toml",
                localFile = testResource("DefaultConfigRegistryTest/same-directory/config.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isEqualTo("same directory")
        }

        @Test
        fun `custom location config by environment variable is used instead of same directory config and overrides classpath`() {
            // given
            val key = "string.override.test"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/classpath/config.toml",
                localFile = testResource("DefaultConfigRegistryTest/same-directory/config.toml"),
                environmentVariables = mapOf(
                    "modb.core.config.location" to testResource("DefaultConfigRegistryTest/custom-location-env-var/config.toml").toAbsolutePath().toString(),
                ),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isEqualTo("custom location by environment variable")
        }

        @Test
        fun `custom location config by system property is used instead of file provided by env var or same directory config and overrides classpath`() {
            // given
            val key = "string.override.test"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/classpath/config.toml",
                localFile = testResource("DefaultConfigRegistryTest/same-directory/config.toml"),
                environmentVariables = mapOf(
                    "modb.core.config.location" to testResource("DefaultConfigRegistryTest/custom-location-env-var/config.toml").toAbsolutePath().toString(),
                ),
                systemProperties = mapOf(
                    "modb.core.config.location" to testResource("DefaultConfigRegistryTest/custom-location-system-property/config.toml").toAbsolutePath().toString(),
                ),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isEqualTo("custom location by system property")
        }

        @Test
        fun `environment variable overrides any value of any file`() {
            // given
            val key = "string.override.test"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/classpath/config.toml",
                localFile = testResource("DefaultConfigRegistryTest/same-directory/config.toml"),
                environmentVariables = mapOf(
                    "modb.core.config.location" to testResource("DefaultConfigRegistryTest/custom-location-env-var/config.toml").toAbsolutePath().toString(),
                    key to "environment variable",
                ),
                systemProperties = mapOf(
                    "modb.core.config.location" to testResource("DefaultConfigRegistryTest/custom-location-system-property/config.toml").toAbsolutePath().toString(),
                ),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isEqualTo("environment variable")
        }

        @Test
        fun `system property overrides anything else`() {
            // given
            val key = "string.override.test"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/classpath/config.toml",
                localFile = testResource("DefaultConfigRegistryTest/same-directory/config.toml"),
                environmentVariables = mapOf(
                    "modb.core.config.location" to testResource("DefaultConfigRegistryTest/custom-location-env-var/config.toml").toAbsolutePath().toString(),
                    key to "environment variable",
                ),
                systemProperties = mapOf(
                    "modb.core.config.location" to testResource("DefaultConfigRegistryTest/custom-location-system-property/config.toml").toAbsolutePath().toString(),
                    key to "system property",
                ),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isEqualTo("system property")
        }

        @Test
        fun `ignores existing config file in classpath without the correct name`() {
            // given
            val key = "string.test"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/file-name-tests/other.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `ignores existing config file in same directory without the correct name`() {
            // given
            val key = "string.test"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "non-existent.toml",
                localFile = testResource("DefaultConfigRegistryTest/file-name-tests/other.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `ignores existing config file in custom location set by environment variable without the correct name`() {
            // given
            val key = "string.test"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "non-existent.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = mapOf(
                    "modb.core.config.location" to testResource("DefaultConfigRegistryTest/file-name-tests/other.toml").toAbsolutePath().toString(),
                ),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `ignores existing config file in custom location set by system property without the correct name`() {
            // given
            val key = "string.test"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "non-existent.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = mapOf(
                    "modb.core.config.location" to testResource("DefaultConfigRegistryTest/file-name-tests/other.toml").toAbsolutePath().toString(),
                ),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `accepts config file in classpath which at least ends with 'config(dot)toml'`() {
            // given
            val key = "string.test"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/file-name-tests/other-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isEqualTo("exists")
        }

        @Test
        fun `accepts config file in same directory which at least ends with 'config(dot)toml'`() {
            // given
            val key = "string.test"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "non-existent.toml",
                localFile = testResource("DefaultConfigRegistryTest/file-name-tests/other-config.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isEqualTo("exists")
        }

        @Test
        fun `accepts config file in custom location set by environment variable which at least ends with 'config(dot)toml'`() {
            // given
            val key = "string.test"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "non-existent.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = mapOf(
                    "modb.core.config.location" to testResource("DefaultConfigRegistryTest/file-name-tests/other-config.toml").toAbsolutePath().toString(),
                ),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isEqualTo("exists")
        }

        @Test
        fun `accepts config file in custom location set by system property which at least ends with 'config(dot)toml'`() {
            // given
            val key = "string.test"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "non-existent.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = mapOf(
                    "modb.core.config.location" to testResource("DefaultConfigRegistryTest/file-name-tests/other-config.toml").toAbsolutePath().toString(),
                ),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isEqualTo("exists")
        }
    }

    @Nested
    inner class StringTests {

        @Test
        fun `correctly returns string`() {
            // given
            val key = "string.valid.value"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/string-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isEqualTo("test value")
        }

        @Test
        fun `returns null if key doesn't exist`() {
            // given
            val key = "string.key.not.exists"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/string-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `can cast different types`() {
            // given
            val key = "string.different.type"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/string-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.string(key)

            // then
            assertThat(result).isEqualTo("8")
        }

        @Test
        fun `initialization is done only once`() {
            tempDirectory {
                // given
                val key = "string.valid.value"

                val file = testResource("DefaultConfigRegistryTest/type-tests/string-config.toml")
                    .copyTo(tempDir)
                    .moveTo(tempDir.resolve("config.toml"))

                val configRegistry = DefaultConfigRegistry(
                    classPathFile = "non-existent.toml",
                    localFile = file,
                    environmentVariables = emptyMap(),
                    systemProperties = emptyMap(),
                )

                configRegistry.string(key)
                file.deleteIfExists()
                testResource("DefaultConfigRegistryTest/initialization-test/config.toml").copyTo(tempDir)

                // when
                val result = configRegistry.string(key)

                // then
                assertThat(result).isEqualTo("test value")
            }
        }
    }

    @Nested
    inner class LongTests {

        @Test
        fun `correctly returns long`() {
            // given
            val key = "long.valid.value"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/long-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.long(key)

            // then
            assertThat(result).isEqualTo(21L)
        }

        @Test
        fun `returns null if key doesn't exist`() {
            // given
            val key = "long.key.not.exists"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/long-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.long(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `can cast different types`() {
            // given
            val key = "long.different.type"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/long-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.long(key)

            // then
            assertThat(result).isEqualTo(5L)
        }

        @Test
        fun `returns null if type cannot be cast`() {
            // given
            val key = "long.null.on.wrong.type"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/long-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.long(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `initialization is done only once`() {
            tempDirectory {
                // given
                val key = "long.valid.value"

                val file = testResource("DefaultConfigRegistryTest/type-tests/long-config.toml")
                    .copyTo(tempDir)
                    .moveTo(tempDir.resolve("config.toml"))

                val configRegistry = DefaultConfigRegistry(
                    classPathFile = "non-existent.toml",
                    localFile = file,
                    environmentVariables = emptyMap(),
                    systemProperties = emptyMap(),
                )

                configRegistry.long(key)
                file.deleteIfExists()
                testResource("DefaultConfigRegistryTest/initialization-test/config.toml").copyTo(tempDir)

                // when
                val result = configRegistry.long(key)

                // then
                assertThat(result).isEqualTo(21L)
            }
        }
    }

    @Nested
    inner class IntTests {

        @Test
        fun `correctly returns int`() {
            // given
            val key = "int.valid.value"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/int-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.int(key)

            // then
            assertThat(result).isEqualTo(42)
        }

        @Test
        fun `returns null if key doesn't exist`() {
            // given
            val key = "int.key.not.exists"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/int-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.int(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `can cast different types`() {
            // given
            val key = "int.different.type"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/int-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.int(key)

            // then
            assertThat(result).isEqualTo(7)
        }

        @Test
        fun `returns null if type cannot be cast`() {
            // given
            val key = "int.null.on.wrong.type"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/int-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.int(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `initialization is done only once`() {
            tempDirectory {
                // given
                val key = "int.valid.value"

                val file = testResource("DefaultConfigRegistryTest/type-tests/int-config.toml")
                    .copyTo(tempDir)
                    .moveTo(tempDir.resolve("config.toml"))

                val configRegistry = DefaultConfigRegistry(
                    classPathFile = "non-existent.toml",
                    localFile = file,
                    environmentVariables = emptyMap(),
                    systemProperties = emptyMap(),
                )

                configRegistry.int(key)
                file.deleteIfExists()
                testResource("DefaultConfigRegistryTest/initialization-test/config.toml").copyTo(tempDir)

                // when
                val result = configRegistry.int(key)

                // then
                assertThat(result).isEqualTo(42)
            }
        }
    }

    @Nested
    inner class DoubleTests {

        @Test
        fun `correctly returns double`() {
            // given
            val key = "double.valid.value"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/double-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.double(key)

            // then
            assertThat(result).isEqualTo(64.5)
        }

        @Test
        fun `returns null if key doesn't exist`() {
            // given
            val key = "double.key.not.exists"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/double-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.double(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `can cast different types`() {
            // given
            val key = "double.different.type"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/double-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.double(key)

            // then
            assertThat(result).isEqualTo(9.0)
        }

        @Test
        fun `returns null if type cannot be cast`() {
            // given
            val key = "double.null.on.wrong.type"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/double-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.double(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `initialization is done only once`() {
            tempDirectory {
                // given
                val key = "double.valid.value"

                val file = testResource("DefaultConfigRegistryTest/type-tests/double-config.toml")
                    .copyTo(tempDir)
                    .moveTo(tempDir.resolve("config.toml"))

                val configRegistry = DefaultConfigRegistry(
                    classPathFile = "non-existent.toml",
                    localFile = file,
                    environmentVariables = emptyMap(),
                    systemProperties = emptyMap(),
                )

                configRegistry.double(key)
                file.deleteIfExists()
                testResource("DefaultConfigRegistryTest/initialization-test/config.toml").copyTo(tempDir)

                // when
                val result = configRegistry.double(key)

                // then
                assertThat(result).isEqualTo(64.5)
            }
        }
    }

    @Nested
    inner class BooleanTests {

        @Test
        fun `correctly returns boolean`() {
            // given
            val key = "boolean.valid.value"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/boolean-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.boolean(key)

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `returns null if key doesn't exist`() {
            // given
            val key = "boolean.key.not.exists"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/boolean-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.boolean(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `can cast different types`() {
            // given
            val key = "boolean.different.type"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/boolean-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.boolean(key)

            // then
            assertThat(result).isFalse()
        }

        @Test
        fun `returns null if type is wrong`() {
            // given
            val key = "boolean.null.on.wrong.type"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/boolean-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.boolean(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `returns null if value is not strictly boolean format`() {
            // given
            val key = "null.on.not.strictly.boolean"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/boolean-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.boolean(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `initialization is done only once`() {
            tempDirectory {
                // given
                val key = "boolean.valid.value"

                val file = testResource("DefaultConfigRegistryTest/type-tests/boolean-config.toml")
                    .copyTo(tempDir)
                    .moveTo(tempDir.resolve("config.toml"))

                val configRegistry = DefaultConfigRegistry(
                    classPathFile = "non-existent.toml",
                    localFile = file,
                    environmentVariables = emptyMap(),
                    systemProperties = emptyMap(),
                )

                configRegistry.boolean(key)
                file.deleteIfExists()
                testResource("DefaultConfigRegistryTest/initialization-test/config.toml").copyTo(tempDir)

                // when
                val result = configRegistry.boolean(key)

                // then
                assertThat(result).isTrue()
            }
        }
    }

    @Nested
    inner class LocalDateTests {

        @Test
        fun `correctly returns localDate`() {
            // given
            val key = "localDate.valid.value"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/localDate-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.localDate(key)

            // then
            assertThat(result).isEqualTo(LocalDate.of(2022, 10, 31))
        }

        @Test
        fun `returns null if key doesn't exist`() {
            // given
            val key = "localDate.key.not.exists"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/localDate-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.localDate(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `can cast different types`() {
            // given
            val key = "localDate.different.type"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/localDate-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.localDate(key)

            // then
            assertThat(result).isEqualTo(LocalDate.of(2023, 12, 6))
        }

        @Test
        fun `returns null if value cannot be parsed`() {
            // given
            val key = "localDate.null.on.wrong.format"
            val configRegistry = DefaultConfigRegistry()

            // when
            val result = configRegistry.localDate(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `initialization is done only once`() {
            tempDirectory {
                // given
                val key = "localDate.valid.value"

                val file = testResource("DefaultConfigRegistryTest/type-tests/localDate-config.toml")
                    .copyTo(tempDir)
                    .moveTo(tempDir.resolve("config.toml"))

                val configRegistry = DefaultConfigRegistry(
                    classPathFile = "non-existent.toml",
                    localFile = file,
                    environmentVariables = emptyMap(),
                    systemProperties = emptyMap(),
                )

                configRegistry.localDate(key)
                file.deleteIfExists()
                testResource("DefaultConfigRegistryTest/initialization-test/config.toml").copyTo(tempDir)

                // when
                val result = configRegistry.localDate(key)

                // then
                assertThat(result).isEqualTo(LocalDate.of(2022, 10, 31))
            }
        }
    }

    @Nested
    inner class LocalDateTimeTests {

        @Test
        fun `correctly returns localDateTime`() {
            // given
            val key = "localDateTime.valid.value"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/localDateTime-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.localDateTime(key)

            // then
            assertThat(result).isEqualTo(LocalDateTime.of(2023, 10, 31, 6, 32, 9))
        }

        @Test
        fun `returns null if key doesn't exist`() {
            // given
            val key = "localDateTime.key.not.exists"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/localDateTime-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.localDateTime(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `can cast different types`() {
            // given
            val key = "localDateTime.different.type"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/localDateTime-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.localDateTime(key)

            // then
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 12, 6, 9, 55, 37))
        }

        @Test
        fun `returns null if value cannot be parsed`() {
            // given
            val key = "localDateTime.null.on.wrong.format"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/localDateTime-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.localDateTime(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `initialization is done only once`() {
            tempDirectory {
                // given
                val key = "localDateTime.valid.value"

                val file = testResource("DefaultConfigRegistryTest/type-tests/localDateTime-config.toml")
                    .copyTo(tempDir)
                    .moveTo(tempDir.resolve("config.toml"))

                val configRegistry = DefaultConfigRegistry(
                    classPathFile = "non-existent.toml",
                    localFile = file,
                    environmentVariables = emptyMap(),
                    systemProperties = emptyMap(),
                )

                configRegistry.localDateTime(key)
                file.deleteIfExists()
                testResource("DefaultConfigRegistryTest/initialization-test/config.toml").copyTo(tempDir)

                // when
                val result = configRegistry.localDateTime(key)

                // then
                assertThat(result).isEqualTo(LocalDateTime.of(2023, 10, 31, 6, 32, 9))
            }
        }
    }

    @Nested
    inner class OffsetDateTimeTests {

        @Test
        fun `correctly returns offsetDateTime`() {
            // given
            val key = "offsetDateTime.valid.value"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/offsetDateTime-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.offsetDateTime(key)

            // then
            assertThat(result).isEqualTo(OffsetDateTime.of(LocalDateTime.of(2024, 10, 31, 8, 32, 43), UTC))
        }

        @Test
        fun `returns null if key doesn't exist`() {
            // given
            val key = "offsetDateTime.key.not.exists"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/offsetDateTime-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.offsetDateTime(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `can cast different types`() {
            // given
            val key = "offsetDateTime.different.type"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/offsetDateTime-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.offsetDateTime(key)

            // then
            assertThat(result).isEqualTo(OffsetDateTime.of(LocalDateTime.of(2025, 12, 6, 14, 19, 2), ZoneOffset.ofHours(4)))
        }

        @Test
        fun `returns null if value cannot be parsed`() {
            // given
            val key = "offsetDateTime.null.on.wrong.format"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/offsetDateTime-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.offsetDateTime(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `initialization is done only once`() {
            tempDirectory {
                // given
                val key = "offsetDateTime.valid.value"

                val file = testResource("DefaultConfigRegistryTest/type-tests/offsetDateTime-config.toml")
                    .copyTo(tempDir)
                    .moveTo(tempDir.resolve("config.toml"))

                val configRegistry = DefaultConfigRegistry(
                    classPathFile = "non-existent.toml",
                    localFile = file,
                    environmentVariables = emptyMap(),
                    systemProperties = emptyMap(),
                )

                configRegistry.offsetDateTime(key)
                file.deleteIfExists()
                testResource("DefaultConfigRegistryTest/initialization-test/config.toml").copyTo(tempDir)

                // when
                val result = configRegistry.offsetDateTime(key)

                // then
                assertThat(result).isEqualTo(OffsetDateTime.of(LocalDateTime.of(2024, 10, 31, 8, 32, 43), UTC))
            }
        }
    }

    @Nested
    inner class ListTests {

        @Test
        fun `correctly returns list`() {
            // given
            val key = "list.valid.value"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/list-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.list<String>(key)

            // then
            assertThat(result).containsExactly(
                "one",
                "two",
                "three",
            )
        }

        @Test
        fun `returns null if key doesn't exist`() {
            // given
            val key = "list.key.not.exists"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/list-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.list<String>(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `wraps a single element in a list`() {
            // given
            val key = "list.single.element"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/list-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.list<Long>(key)

            // then
            assertThat(result).containsExactly(
                5L,
            )
        }

        @Test
        fun `initialization is done only once`() {
            tempDirectory {
                // given
                val key = "list.valid.value"

                val file = testResource("DefaultConfigRegistryTest/type-tests/list-config.toml")
                    .copyTo(tempDir)
                    .moveTo(tempDir.resolve("config.toml"))

                val configRegistry = DefaultConfigRegistry(
                    classPathFile = "non-existent.toml",
                    localFile = file,
                    environmentVariables = emptyMap(),
                    systemProperties = emptyMap(),
                )

                configRegistry.list<String>(key)
                file.deleteIfExists()
                testResource("DefaultConfigRegistryTest/initialization-test/config.toml").copyTo(tempDir)

                // when
                val result = configRegistry.list<String>(key)

                // then
                assertThat(result).containsExactly(
                    "one",
                    "two",
                    "three",
                )
            }
        }
    }

    @Nested
    inner class MapTests {

        @Test
        fun `correctly returns map`() {
            // given
            val key = "map.valid.value"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/map-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.map<Long>(key)

            // then
            assertThat(result).containsExactlyEntriesOf(
                mapOf(
                    "key1" to 100L,
                    "key2" to 200L,
                )
            )
        }

        @Test
        fun `returns null if key doesn't exist`() {
            // given
            val key = "map.key.not.exists"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/map-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.map<String>(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `returns null if type cannot be cast`() {
            // given
            val key = "maptest.null.on.wrong.type"

            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/map-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = emptyMap(),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.map<String>(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `environment variables always return null`() {
            // given
            val key = "map.envVar"
            val configRegistry = DefaultConfigRegistry(
                classPathFile = "DefaultConfigRegistryTest/type-tests/map-config.toml",
                localFile = Path("non-existent.toml"),
                environmentVariables = mapOf(
                    key to "something",
                ),
                systemProperties = emptyMap(),
            )

            // when
            val result = configRegistry.map<String>(key)

            // then
            assertThat(result).isNull()
        }

        @Test
        fun `initialization is done only once`() {
            tempDirectory {
                // given
                val key = "map.valid.value"

                val file = testResource("DefaultConfigRegistryTest/type-tests/map-config.toml")
                    .copyTo(tempDir)
                    .moveTo(tempDir.resolve("config.toml"))

                val configRegistry = DefaultConfigRegistry(
                    classPathFile = "non-existent.toml",
                    localFile = file,
                    environmentVariables = emptyMap(),
                    systemProperties = emptyMap(),
                )

                configRegistry.map<Long>(key)
                file.deleteIfExists()
                testResource("DefaultConfigRegistryTest/initialization-test/config.toml").copyTo(tempDir)

                // when
                val result = configRegistry.map<Long>(key)

                // then
                assertThat(result).containsExactlyEntriesOf(
                    mapOf(
                        "key1" to 100L,
                        "key2" to 200L,
                    )
                )
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = DefaultConfigRegistry.instance

            // when
            val result = DefaultConfigRegistry.instance

            // then
            assertThat(result).isExactlyInstanceOf(DefaultConfigRegistry::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}