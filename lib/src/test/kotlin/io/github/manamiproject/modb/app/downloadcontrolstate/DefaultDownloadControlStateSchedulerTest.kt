package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.app.TestAppConfig
import io.github.manamiproject.modb.app.TestDownloadControlStateAccessor
import io.github.manamiproject.modb.app.TestMetaDataProviderConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.test.tempDirectory
import io.github.manamiproject.modb.test.testResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset.UTC
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectory
import kotlin.test.Test

internal class DefaultDownloadControlStateSchedulerTest {

    @Nested
    inner class FindEntriesNotScheduledForCurrentWeekTests {

        @Test
        fun `find entries not scheduled for current week`() {
            tempDirectory {
                // given
                val testAppConfig = object : Config by TestAppConfig {
                    override fun clock(): Clock = Clock.fixed(Instant.parse("2021-01-31T16:02:42.00Z"), UTC)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val testConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun buildAnimeLink(id: AnimeId): URI = super.buildAnimeLink(id)
                    override fun extractAnimeId(uri: URI): AnimeId = super.extractAnimeId(uri)
                }

                val metaDataProviderDir = tempDir.resolve(testConfig.hostname()).createDirectory()
                testResource("downloadcontrolstate/DefaultDownloadControlStateScheduleTest/32.dcs").copyTo(
                    tempDir.resolve(testConfig.hostname()).resolve("32.dcs")
                )
                testResource("downloadcontrolstate/DefaultDownloadControlStateScheduleTest/99.dcs").copyTo(
                    tempDir.resolve(testConfig.hostname()).resolve("99.dcs")
                )

                val testDownloadControlStateAccessor: DownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = metaDataProviderDir
                }

                val defaultDownloadControlStateScheduler = DefaultDownloadControlStateScheduler(
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                val result = defaultDownloadControlStateScheduler.findEntriesNotScheduledForCurrentWeek(testConfig)

                // then
                assertThat(result).containsExactlyInAnyOrder("99")
            }
        }
    }

    @Nested
    inner class FindEntriesScheduledForCurrentWeekTests {

        @Test
        fun `find entries scheduled for current week`() {
            tempDirectory {
                // given
                val testAppConfig = object : Config by TestAppConfig {
                    override fun clock(): Clock = Clock.fixed(Instant.parse("2021-01-31T16:02:42.00Z"), UTC)
                    override fun downloadControlStateDirectory(): Directory = tempDir
                }

                val testConfig = object : MetaDataProviderConfig by TestMetaDataProviderConfig {
                    override fun hostname(): Hostname = "example.org"
                    override fun buildAnimeLink(id: AnimeId): URI = super.buildAnimeLink(id)
                    override fun extractAnimeId(uri: URI): AnimeId = super.extractAnimeId(uri)
                }

                val metaDataProviderDir = tempDir.resolve(testConfig.hostname()).createDirectory()
                testResource("downloadcontrolstate/DefaultDownloadControlStateScheduleTest/32.dcs").copyTo(
                    tempDir.resolve(testConfig.hostname()).resolve("32.dcs")
                )
                testResource("downloadcontrolstate/DefaultDownloadControlStateScheduleTest/99.dcs").copyTo(
                    tempDir.resolve(testConfig.hostname()).resolve("99.dcs")
                )

                val testDownloadControlStateAccessor: DownloadControlStateAccessor = object: DownloadControlStateAccessor by TestDownloadControlStateAccessor {
                    override fun downloadControlStateDirectory(metaDataProviderConfig: MetaDataProviderConfig): Directory = metaDataProviderDir
                }

                val defaultDownloadControlStateScheduler = DefaultDownloadControlStateScheduler(
                    appConfig = testAppConfig,
                    downloadControlStateAccessor = testDownloadControlStateAccessor,
                )

                // when
                val result = defaultDownloadControlStateScheduler.findEntriesScheduledForCurrentWeek(testConfig)

                // then
                assertThat(result).containsExactlyInAnyOrder("32")
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            tempDirectory {
                // given
                val previous = DefaultDownloadControlStateScheduler.instance

                // when
                val result = DefaultDownloadControlStateScheduler.instance

                // then
                assertThat(result).isExactlyInstanceOf(DefaultDownloadControlStateScheduler::class.java)
                assertThat(result===previous).isTrue()
            }
        }
    }
}