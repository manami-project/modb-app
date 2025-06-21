package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset.UTC
import kotlin.io.path.readText
import kotlin.test.Test

internal class ReleaseInfoFileCreatorPostProcessorTest {

    @Nested
    inner class ProcessTests {

        @Test
        fun `correctly create release info file`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun outputDirectory(): Directory = tempDir
                    override fun clock(): Clock = Clock.fixed(Instant.parse("2020-01-01T16:02:42.00Z"), UTC)
                }

                val postProcessor = ReleaseInfoFileCreatorPostProcessor(
                    appConfig = testAppConfig,
                )

                val expectedOutputFile = tempDir.resolve("week.release")

                // when
                val result = postProcessor.process()

                // then
                assertThat(result).isTrue()
                assertThat(expectedOutputFile).exists()
                assertThat(expectedOutputFile).isRegularFile()
                assertThat(expectedOutputFile.readText()).isEqualTo("2020-01")
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = ReleaseInfoFileCreatorPostProcessor.instance

                // when
                val result = ReleaseInfoFileCreatorPostProcessor.instance

                // then
                assertThat(result).isExactlyInstanceOf(ReleaseInfoFileCreatorPostProcessor::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}