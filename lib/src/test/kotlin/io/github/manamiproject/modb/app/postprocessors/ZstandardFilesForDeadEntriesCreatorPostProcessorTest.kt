package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestDeadEntriesAccessor
import io.github.manamiproject.modb.app.TestMetaDataProviderConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.dataset.DatasetFileType
import io.github.manamiproject.modb.app.dataset.DeadEntriesAccessor
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

internal class ZstandardFilesForDeadEntriesCreatorPostProcessorTest {

    @Test
    fun `correctly create Zstandard files`() {
        tempDirectory {
            // given
            val testConfig1 = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = "not-supported.io"
            }

            val testConfig2 = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = "example.org"
            }

            val testAppConfig = object: Config by TestAppConfig {
                override fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(
                    testConfig1,
                    testConfig2,
                )
                override fun deadEntriesSupported(metaDataProviderConfig: MetaDataProviderConfig): Boolean = metaDataProviderConfig.hostname() == "example.org"
            }

            val testFile = tempDir.resolve("json-minified.txt")

            val testDeadEntriesAccessor = object: DeadEntriesAccessor by TestDeadEntriesAccessor {
                override fun deadEntriesFile(metaDataProviderConfig: MetaDataProviderConfig, type: DatasetFileType): RegularFile = testFile
            }

            val postProcessor = ZstandardFilesForDeadEntriesCreatorPostProcessor(
                appConfig = testAppConfig,
                deadEntriesAccessor = testDeadEntriesAccessor,
            )

            """Here is
               some text.
            """.trimMargin().writeToFile(testFile)

            // when
            val result = postProcessor.process()

            // then
            assertThat(result).isTrue()
            assertThat(testFile).exists()
            assertThat(tempDir.resolve("json-minified.json.zst")).exists()
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = ZstandardFilesForDeadEntriesCreatorPostProcessor.instance

                // when
                val result = ZstandardFilesForDeadEntriesCreatorPostProcessor.instance

                // then
                assertThat(result).isExactlyInstanceOf(ZstandardFilesForDeadEntriesCreatorPostProcessor::class.java)
                assertThat(result === previous).isTrue()
            }
        }
    }
}