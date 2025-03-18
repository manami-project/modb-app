package io.github.manamiproject.modb.app.config

import io.github.manamiproject.AnimenewsnetworkConfig
import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import io.github.manamiproject.modb.anisearch.AnisearchConfig
import io.github.manamiproject.modb.app.TestMetaDataProviderConfig
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.livechart.LivechartConfig
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import io.github.manamiproject.modb.notify.NotifyConfig
import io.github.manamiproject.modb.simkl.SimklConfig
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Clock
import kotlin.test.Test

internal class ConfigTest {

    @Nested
    inner class IsTestContextTests {

        @Test
        fun `default for isTestContext is false`() {
            // given
            val testConfig = object: MetaDataProviderConfig {
                override fun hostname(): Hostname = shouldNotBeInvoked()
                override fun fileSuffix(): FileSuffix = shouldNotBeInvoked()
            }

            // when
            val result = testConfig.isTestContext()

            // then
            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class MetaDataProviderConfigurationsTests {

        @Test
        fun `metaDataProviderConfigurations returns all supported main MetaDataProviderConfigurations by default`() {
            // given
            val expectedList = setOf(
                AnidbConfig,
                AnilistConfig,
                AnimePlanetConfig,
                AnimenewsnetworkConfig,
                AnisearchConfig,
                KitsuConfig,
                LivechartConfig,
                MyanimelistConfig,
                NotifyConfig,
                SimklConfig,
            )
            val config = object : Config {
                override fun downloadsDirectory(): Directory = shouldNotBeInvoked()
                override fun currentWeekWorkingDir(): Directory = shouldNotBeInvoked()
                override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = shouldNotBeInvoked()
                override fun outputDirectory(): Directory = shouldNotBeInvoked()
                override fun downloadControlStateDirectory(): Directory = shouldNotBeInvoked()
            }

            // when
            val result = config.metaDataProviderConfigurations()

            // then
            assertThat(result).containsAll(expectedList)
        }
    }

    @Nested
    inner class FindMetaDataProviderConfigTests {

        @Test
        fun `throws exception if config for given host does not exist`() {
            // given
            val nonExistentHost = "example.org"

            val testConfig = object: Config {
                override fun downloadsDirectory(): Directory = shouldNotBeInvoked()
                override fun currentWeekWorkingDir(): Directory = shouldNotBeInvoked()
                override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = shouldNotBeInvoked()
                override fun outputDirectory(): Directory = shouldNotBeInvoked()
                override fun downloadControlStateDirectory(): Directory = shouldNotBeInvoked()
            }

            // when
            val result = assertThrows<IllegalArgumentException> {
                testConfig.findMetaDataProviderConfig(nonExistentHost)
            }

            // then
            assertThat(result).hasMessage("No config found for [example.org]")
        }

        @Test
        fun `find correct config`() {
            // given
            val existingHost = "myanimelist.net"

            val testConfig = object: Config {
                override fun downloadsDirectory(): Directory = shouldNotBeInvoked()
                override fun currentWeekWorkingDir(): Directory = shouldNotBeInvoked()
                override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = shouldNotBeInvoked()
                override fun outputDirectory(): Directory = shouldNotBeInvoked()
                override fun downloadControlStateDirectory(): Directory = shouldNotBeInvoked()
            }

            // when
            val result = testConfig.findMetaDataProviderConfig(existingHost)

            // then
            assertThat(result).isInstanceOf(MyanimelistConfig::class.java)
        }
    }

    @Nested
    inner class CanChangeAnimeIdsTests {

        @Test
        fun `returns true if IDs of a meta data provider can change`() {
            // given
            val testConfig = object: Config {
                override fun downloadsDirectory(): Directory = shouldNotBeInvoked()
                override fun currentWeekWorkingDir(): Directory = shouldNotBeInvoked()
                override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = shouldNotBeInvoked()
                override fun outputDirectory(): Directory = shouldNotBeInvoked()
                override fun downloadControlStateDirectory(): Directory = shouldNotBeInvoked()
            }

            // when
            val result = testConfig.canChangeAnimeIds(AnimePlanetConfig)

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `returns false if IDs of a meta data provider cannot change`() {
            // given
            val testMetaDataProviderConfig = object: MetaDataProviderConfig by TestMetaDataProviderConfig {
                override fun hostname(): Hostname = "example.org"
            }

            val testConfig = object: Config {
                override fun downloadsDirectory(): Directory = shouldNotBeInvoked()
                override fun currentWeekWorkingDir(): Directory = shouldNotBeInvoked()
                override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = shouldNotBeInvoked()
                override fun outputDirectory(): Directory = shouldNotBeInvoked()
                override fun downloadControlStateDirectory(): Directory = shouldNotBeInvoked()
            }

            // when
            val result = testConfig.canChangeAnimeIds(testMetaDataProviderConfig)

            // then
            assertThat(result).isFalse()
        }

    }

    @Nested
    inner class DeadEntriesSupportedTests {

        @ParameterizedTest
        @ValueSource(classes = [AnidbConfig::class, AnilistConfig::class, KitsuConfig::class, MyanimelistConfig::class])
        fun `returns true for all supported meta data provider`(configClass: Class<*>) {
            // given
            val testMetaDataProviderConfig = configClass.kotlin.objectInstance as MetaDataProviderConfig

            val testConfig = object: Config {
                override fun downloadsDirectory(): Directory = shouldNotBeInvoked()
                override fun currentWeekWorkingDir(): Directory = shouldNotBeInvoked()
                override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = shouldNotBeInvoked()
                override fun outputDirectory(): Directory = shouldNotBeInvoked()
                override fun downloadControlStateDirectory(): Directory = shouldNotBeInvoked()
            }

            // when
            val result = testConfig.deadEntriesSupported(testMetaDataProviderConfig)

            // then
            assertThat(result).isTrue()
        }

        @ParameterizedTest
        @ValueSource(classes = [
            AnisearchConfig::class,
            AnimePlanetConfig::class,
            LivechartConfig::class,
            NotifyConfig::class,
            SimklConfig::class,
        ])
        fun `returns false for all meta data provider which are not supported`(configClass: Class<*>) {
            // given
            val testMetaDataProviderConfig = configClass.kotlin.objectInstance as MetaDataProviderConfig

            val testConfig = object: Config {
                override fun downloadsDirectory(): Directory = shouldNotBeInvoked()
                override fun currentWeekWorkingDir(): Directory = shouldNotBeInvoked()
                override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = shouldNotBeInvoked()
                override fun outputDirectory(): Directory = shouldNotBeInvoked()
                override fun downloadControlStateDirectory(): Directory = shouldNotBeInvoked()
            }

            // when
            val result = testConfig.deadEntriesSupported(testMetaDataProviderConfig)

            // then
            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class ClockTests {

        @Test
        fun `default value is current system local zone`() {
            // given
            val systemDefaultZone = Clock.systemDefaultZone()
            val config = object : Config {
                override fun downloadsDirectory(): Directory = shouldNotBeInvoked()
                override fun currentWeekWorkingDir(): Directory = shouldNotBeInvoked()
                override fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory = shouldNotBeInvoked()
                override fun outputDirectory(): Directory = shouldNotBeInvoked()
                override fun downloadControlStateDirectory(): Directory = shouldNotBeInvoked()
            }

            // when
            val result = config.clock()

            // then
            assertThat(result).isEqualTo(systemDefaultZone)
        }
    }
}