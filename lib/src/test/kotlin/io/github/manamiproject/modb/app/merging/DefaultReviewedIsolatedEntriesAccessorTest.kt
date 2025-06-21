package io.github.manamiproject.modb.app.merging

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.extensions.readFile
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.io.path.appendLines
import kotlin.io.path.createFile
import kotlin.io.path.readLines
import kotlin.test.Test

internal class DefaultReviewedIsolatedEntriesAccessorTest {

    @Nested
    inner class ContainsTests {

        @Test
        fun `returns false if the entry doesn't exist`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultReviewedIsolatedEntriesAccessor = DefaultReviewedIsolatedEntriesAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultReviewedIsolatedEntriesAccessor.contains(URI("https://myanimelist.net/anime/1535"))

                // then
                assertThat(result).isFalse()
            }
        }

        @Test
        fun `returns true if the entry exist`() {
            tempDirectory {
                // given
                val testUri = "https://myanimelist.net/anime/1535"

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val testFile = tempDir.resolve("checked-isolated-entries.txt").createFile()
                testFile.appendLines(setOf(testUri))

                val defaultReviewedIsolatedEntriesAccessor = DefaultReviewedIsolatedEntriesAccessor(
                    appConfig = testAppConfig,
                )

                // when
                val result = defaultReviewedIsolatedEntriesAccessor.contains(URI(testUri))

                // then
                assertThat(result).isTrue()
            }
        }

        @Test
        fun `creates file if it doesn't exist`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultReviewedIsolatedEntriesAccessor = DefaultReviewedIsolatedEntriesAccessor(
                    appConfig = testAppConfig,
                )

                // when
                defaultReviewedIsolatedEntriesAccessor.contains(URI("https://myanimelist.net/anime/1535"))

                // then
                assertThat(tempDir.resolve("checked-isolated-entries.txt").regularFileExists()).isTrue()
            }
        }
    }

    @Nested
    inner class AddCheckedEntryTests {

        @Test
        fun `successfully creates entry`() {
            tempDirectory {
                // given
                val testUri = "https://myanimelist.net/anime/1535"

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultReviewedIsolatedEntriesAccessor = DefaultReviewedIsolatedEntriesAccessor(
                    appConfig = testAppConfig,
                )

                // when
                defaultReviewedIsolatedEntriesAccessor.addCheckedEntry(URI(testUri))

                // then
                assertThat(tempDir.resolve("checked-isolated-entries.txt").readFile()).isEqualTo(testUri)
                assertThat(defaultReviewedIsolatedEntriesAccessor.contains(URI(testUri))).isTrue()
            }
        }

        @Test
        fun `doesn't create duplicates`() {
            tempDirectory {
                // given
                val testUri = "https://myanimelist.net/anime/1535"

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultReviewedIsolatedEntriesAccessor = DefaultReviewedIsolatedEntriesAccessor(
                    appConfig = testAppConfig,
                )
                defaultReviewedIsolatedEntriesAccessor.addCheckedEntry(URI(testUri))

                // when
                defaultReviewedIsolatedEntriesAccessor.addCheckedEntry(URI(testUri))

                // then
                assertThat(tempDir.resolve("checked-isolated-entries.txt").readLines()).containsExactly(
                    testUri,
                )
            }
        }

        @Test
        fun `creates file if it doesn't exist`() {
            tempDirectory {
                // given
                val testUri = "https://myanimelist.net/anime/1535"

                val testAppConfig = object: Config by TestAppConfig {
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val defaultReviewedIsolatedEntriesAccessor = DefaultReviewedIsolatedEntriesAccessor(
                    appConfig = testAppConfig,
                )

                // when
                defaultReviewedIsolatedEntriesAccessor.addCheckedEntry(URI(testUri))

                // then
                assertThat(tempDir.resolve("checked-isolated-entries.txt").regularFileExists()).isTrue()
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = DefaultReviewedIsolatedEntriesAccessor.instance

                // when
                val result = DefaultReviewedIsolatedEntriesAccessor.instance

                // then
                assertThat(result).isExactlyInstanceOf(DefaultReviewedIsolatedEntriesAccessor::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}