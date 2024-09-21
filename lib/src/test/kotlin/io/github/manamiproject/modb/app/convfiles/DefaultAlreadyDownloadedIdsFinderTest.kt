package io.github.manamiproject.modb.app.convfiles

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestMetaDataProviderConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.test.Test

internal class DefaultAlreadyDownloadedIdsFinderTest {

    @Nested
    inner class AlreadyDownloadedIdsTests {

        @Test
        fun `correctly returns the IDs`() {
            tempDirectory {
                // given
                val testAppConfig = object: Config by TestAppConfig {
                    override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = tempDir
                }

                val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun fileSuffix(): FileSuffix = "html"
                }

                val defaultAlreadyDownloadedIdsFinder = DefaultAlreadyDownloadedIdsFinder(
                    appConfig = testAppConfig,
                )

                tempDir.resolve("3.html").createFile()
                tempDir.resolve("754.html").createFile()
                tempDir.resolve("19643.html").createFile()
                tempDir.resolve("anime-title.html").createFile()
                tempDir.resolve("200.json").createFile()
                tempDir.resolve("something-else.txt").createFile()
                tempDir.resolve("folder").createDirectory()

                // when
                val result = defaultAlreadyDownloadedIdsFinder.alreadyDownloadedIds(testMetaDataProviderConfig)

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    "3",
                    "754",
                    "19643",
                    "anime-title",
                )
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = DefaultAlreadyDownloadedIdsFinder.instance

                // when
                val result = DefaultAlreadyDownloadedIdsFinder.instance

                // then
                assertThat(result).isExactlyInstanceOf(DefaultAlreadyDownloadedIdsFinder::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}