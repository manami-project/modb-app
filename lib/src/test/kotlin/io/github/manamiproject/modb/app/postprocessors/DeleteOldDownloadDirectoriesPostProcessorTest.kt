package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestConfigRegistry
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.date.weekOfYear
import io.github.manamiproject.modb.core.config.ConfigRegistry
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.fileName
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.test.exceptionExpected
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.listDirectoryEntries
import kotlin.test.Test

internal class DeleteOldDownloadDirectoriesPostProcessorTest {

    @Nested
    inner class ProcessTests {

        @Test
        fun `remove download directories except for the last`() {
            tempDirectory {
                // given
                val fixedClock = Clock.fixed(Instant.parse("2019-01-12T17:55:43.035Z"), ZoneId.systemDefault())
                val fixedLocalDate = LocalDate.now(fixedClock)

                val timestamps = mutableListOf<LocalDate>()

                for (index in 1 .. 5 step 1) {
                    timestamps.add(fixedLocalDate.minusWeeks(index.toLong()))
                }

                timestamps.forEach {
                    val weekOfYear = it.weekOfYear()
                    val dir = tempDir.resolve("downloads").resolve("$weekOfYear").createDirectories()

                    "test".writeToFile(dir.resolve("test.txt"))
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadsDirectory(): Directory = tempDir.resolve("downloads")
                    override fun currentWeekWorkingDir(): Directory = downloadsDirectory().resolve("2019-45")
                }

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun int(key: String): Int = 1
                }

                val deleteOldDownloadDirectoriesPostProcessor = DeleteOldDownloadDirectoriesPostProcessor(
                    appConfig = testAppConfig,
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = deleteOldDownloadDirectoriesPostProcessor.process()

                // then
                val files = testAppConfig.downloadsDirectory()
                    .listDirectoryEntries()
                    .map { it.fileName() }

                assertThat(files).containsExactlyInAnyOrder(
                    "2019-01",
                )
                assertThat(result).isTrue()
            }
        }

        @Test
        fun `ignore files and and directories not matching the expected file name format`() {
            tempDirectory {
                // given
                val fixedClock = Clock.fixed(Instant.parse("2019-01-12T17:55:43.035Z"), ZoneId.systemDefault())
                val fixedLocalDate = LocalDate.now(fixedClock)

                val timestamps = mutableListOf<LocalDate>()

                for (index in 1 .. 5 step 1) {
                    timestamps.add(fixedLocalDate.minusWeeks(index.toLong()))
                }

                timestamps.forEach {
                    val weekOfYear = it.weekOfYear()
                    val dir = tempDir.resolve("downloads").resolve("$weekOfYear").createDirectories()

                    "test".writeToFile(dir.resolve("test.txt"))
                }

                tempDir.resolve("downloads").resolve("somethingElse").createDirectories()
                tempDir.resolve("downloads").resolve("testfile.txt").createFile()

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadsDirectory(): Directory = tempDir.resolve("downloads")
                    override fun currentWeekWorkingDir(): Directory = downloadsDirectory().resolve("2019-45")
                }

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun int(key: String): Int = 1
                }

                val deleteOldDownloadDirectoriesPostProcessor = DeleteOldDownloadDirectoriesPostProcessor(
                    appConfig = testAppConfig,
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = deleteOldDownloadDirectoriesPostProcessor.process()

                // then
                val files = testAppConfig.downloadsDirectory()
                    .listDirectoryEntries()
                    .map { it.fileName() }

                assertThat(files).containsExactlyInAnyOrder(
                    "2019-01",
                    "somethingElse",
                    "testfile.txt",
                )
                assertThat(result).isTrue()
            }
        }

        @Test
        fun `don't remove anything if there is only the amount of directories to keep`() {
            tempDirectory {
                // given
                val fixedClock = Clock.fixed(Instant.parse("2019-11-17T17:55:43.035Z"), ZoneId.systemDefault())
                val fixedLocalDate = LocalDate.now(fixedClock)

                val timestamps = mutableListOf<LocalDate>()

                for (index in 1 .. 3 step 1) {
                    timestamps.add(fixedLocalDate.minusWeeks(index.toLong()))
                }

                timestamps.forEach {
                    val weekOfYear = it.weekOfYear()

                    val dir = tempDir.resolve("downloads").resolve("$weekOfYear").createDirectories()

                    "test".writeToFile(dir.resolve("test.txt"))
                }

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadsDirectory(): Directory = tempDir.resolve("downloads")
                    override fun currentWeekWorkingDir(): Directory = downloadsDirectory().resolve("2019-45")
                }

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun int(key: String): Int = 3
                }

                val deleteOldDownloadDirectoriesPostProcessor = DeleteOldDownloadDirectoriesPostProcessor(
                    appConfig = testAppConfig,
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = deleteOldDownloadDirectoriesPostProcessor.process()

                // then
                val files = testAppConfig.downloadsDirectory()
                    .listDirectoryEntries()
                    .map { it.fileName() }

                assertThat(files).containsExactlyInAnyOrder(
                    "2019-45",
                    "2019-44",
                    "2019-43",
                )
                assertThat(result).isTrue()
            }
        }

        @ParameterizedTest
        @ValueSource(ints = [0, -1])
        fun `throws exception if keepDownloadDirectories is less than 1`(value: Int) {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadsDirectory(): Directory = tempDir
                }

                val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
                    override fun int(key: String): Int = value
                }

                val deleteOldDownloadDirectoriesPostProcessor = DeleteOldDownloadDirectoriesPostProcessor(
                    appConfig = testAppConfig,
                    configRegistry = testConfigRegistry,
                )

                // when
                val result = exceptionExpected<IllegalStateException> {
                    deleteOldDownloadDirectoriesPostProcessor.process()
                }

                // then
                assertThat(result).hasMessage("Value [$value] for property [modb.app.keepDownloadDirectories] is invalid.")
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = DeleteOldDownloadDirectoriesPostProcessor.instance

                // when
                val result = DeleteOldDownloadDirectoriesPostProcessor.instance

                // then
                assertThat(result).isExactlyInstanceOf(DeleteOldDownloadDirectoriesPostProcessor::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}