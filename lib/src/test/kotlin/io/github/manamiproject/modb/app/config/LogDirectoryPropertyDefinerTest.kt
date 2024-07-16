package io.github.manamiproject.modb.app.config

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestConfigRegistry
import io.github.manamiproject.modb.core.config.ConfigRegistry
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import kotlin.io.path.createDirectory
import kotlin.test.Test

internal class LogDirectoryPropertyDefinerTest {

    @Test
    fun `correctly returns the default`() {
        tempDirectory {
            // given
            val testAppConfig = object: Config by TestAppConfig {
                override fun currentWeekWorkingDir(): Directory = tempDir
            }

            val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                override fun string(key: String): String? = null
            }

            val logDirectoryPropertyDefiner = LogDirectoryPropertyDefiner(
                appConfig = testAppConfig,
                configRegistry = testConfigRegistry,
            )

            // when
            val result = logDirectoryPropertyDefiner.propertyValue

            // then
            assertThat(result).isEqualTo(tempDir.resolve("logs").toAbsolutePath().toString())
        }
    }

    @Test
    fun `correctly returns user config`() {
        tempDirectory {
            // given
            val testAppConfig = object: Config by TestAppConfig {
                override fun currentWeekWorkingDir(): Directory = tempDir
            }

            val testDir = tempDir.resolve("test").createDirectory()

            val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                override fun string(key: String): String = testDir.toAbsolutePath().toString()
            }

            val logDirectoryPropertyDefiner = LogDirectoryPropertyDefiner(
                appConfig = testAppConfig,
                configRegistry = testConfigRegistry,
            )

            // when
            val result = logDirectoryPropertyDefiner.propertyValue

            // then
            assertThat(result).isEqualTo(testDir.toAbsolutePath().toString())
        }
    }
}