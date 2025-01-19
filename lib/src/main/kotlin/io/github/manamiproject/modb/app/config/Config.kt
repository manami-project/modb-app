package io.github.manamiproject.modb.app.config

import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import io.github.manamiproject.modb.anisearch.AnisearchConfig
import io.github.manamiproject.modb.core.config.ContextAware
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.livechart.LivechartConfig
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import io.github.manamiproject.modb.notify.NotifyConfig
import io.github.manamiproject.modb.simkl.SimklConfig
import java.time.Clock

/**
 * Configuration options that are specifc for the application.
 * @since 1.0.0
 */
interface Config: ContextAware {

    /**
     * Determines the directory in which the raw data and the converted files are stored.
     * The is just the root directory. It doesn't contain files. It contains directories for each week.
     * @since 1.0.0
     * @see currentWeekWorkingDir
     * @return Directory containing a directory for every week.
     */
    fun downloadsDirectory(): Directory

    /**
     * Subdirectory of [downloadsDirectory]. Contains raw data and the converted files for the current week of the year.
     * This directory doesn't contain files. It contains directories for each meta data provider.
     * @since 1.0.0
     * @see downloadsDirectory
     * @see workingDir
     * @return Directory containing a directory for every meta data provider.
     */
    fun currentWeekWorkingDir(): Directory

    /**
     * Retrieve the current working directory for a specific meta data provider.
     * This direcory contains the raw and converted files.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific meta data provider.
     * @see currentWeekWorkingDir
     * @return
     */
    fun workingDir(metaDataProviderConfig: MetaDataProviderConfig): Directory

    /**
     * List of all configurations for all meta data provider.
     * @since 1.0.0
     * @return Duplicate free list of all meta data provider configs.
     */
    fun metaDataProviderConfigurations(): Set<MetaDataProviderConfig> = setOf(
        AnidbConfig,
        AnilistConfig,
        AnimePlanetConfig,
        AnisearchConfig,
        KitsuConfig,
        LivechartConfig,
        MyanimelistConfig,
        NotifyConfig,
        SimklConfig,
    )

    /**
     * Finds a specific [MetaDataProviderConfig] for a given hostname.
     * @since 1.0.0
     * @param host Hostname of a meta data provider
     * @return The corresponding [MetaDataProviderConfig]
     * @throws IllegalArgumentException if no [MetaDataProviderConfig] can be found for the given [host].
     */
    fun findMetaDataProviderConfig(host: Hostname) = metaDataProviderConfigurations().find { it.hostname() == host } ?: throw IllegalArgumentException("No config found for [$host]")

    /**
     * Directory for the git repository of anime-offline-database.
     * This is the target directory in which the finalized files are being written to.
     * @since 1.0.0
     * @return The directory in which the JSON files and the `README.md` are saved..
     */
    fun outputDirectory(): Directory

    /**
     * Root directory of download control state files.
     * @since 1.0.0
     * @return Directory which contains subdirectories for all download control state files.
     */
    fun downloadControlStateDirectory(): Directory

    /**
     * Determines if anime of a specific meta data provider can change their Ids.
     * This is most likely to happen if the ID is a SEO optimized title.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific meta data provider.
     * @return `true` if the anime ids can change for the given meta data provider.
     */
    fun canChangeAnimeIds(metaDataProviderConfig: MetaDataProviderConfig): Boolean = setOf(
        AnimePlanetConfig
    ).contains(metaDataProviderConfig)

    /**
     * Checks if tracking IDs for anime which cannot be downloaded is supported for a specific meta data provider.
     * @since 1.0.0
     * @param metaDataProviderConfig Configuration for a specific meta data provider.
     * @return `true` if the meta data provider has its own dead entries file.
     */
    fun deadEntriesSupported(metaDataProviderConfig: MetaDataProviderConfig): Boolean = setOf(
        AnidbConfig,
        AnilistConfig,
        KitsuConfig,
        MyanimelistConfig,
    ).contains(metaDataProviderConfig)

    /**
     * Clock being used whenever dates and timestamps are created.
     * Default is system default zone.
     * @since 1.0.0
     * @return Instance of [Clock] as basis for any type of date instances.
     */
    fun clock(): Clock = Clock.systemDefaultZone()
}