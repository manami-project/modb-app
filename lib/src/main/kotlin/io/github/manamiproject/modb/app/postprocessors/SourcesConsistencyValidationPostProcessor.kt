package io.github.manamiproject.modb.app.postprocessors

import io.github.manamiproject.modb.animecountdown.AnimeCountdownConfig
import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.convfiles.CONVERTED_FILE_SUFFIX
import io.github.manamiproject.modb.app.dataset.DatasetFileAccessor
import io.github.manamiproject.modb.app.dataset.DefaultDatasetFileAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DOWNLOAD_CONTROL_STATE_FILE_SUFFIX
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import io.github.manamiproject.modb.core.extensions.listRegularFiles
import io.github.manamiproject.modb.core.extensions.readFile
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.anime.AnimeRaw
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.net.URI

/**
 * Verifies that source exists consistently over files of the intermediate format (`*.conv`), download control state
 * files (`*.dcs`) as well as dataset files.
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property datasetFileAccessor Access to dataset files.
 * @property downloadControlStateAccessor Access to DCS files.
 * @throws IllegalStateException if sources are empty or there is a mismatch.
 */
class SourcesConsistencyValidationPostProcessor(
    private val appConfig: Config = AppConfig.instance,
    private val datasetFileAccessor: DatasetFileAccessor = DefaultDatasetFileAccessor.instance,
    private val downloadControlStateAccessor: DownloadControlStateAccessor = DefaultDownloadControlStateAccessor.instance,
    private val ignoreMetaDataConfiguration: Set<MetaDataProviderConfig> = setOf(AnimeCountdownConfig),
): PostProcessor {

    override suspend fun process(): Boolean {
        log.info { "Checking if all downloaded sources in [*.$CONVERTED_FILE_SUFFIX] exist in [*.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX] files." }

        val sourcesInConvFiles = sourcesInConvFiles()
        val sourcesInDcsEntries = downloadControlStateAccessor.allAnime().map { it.sources }.flatten().toSet()
        var result = sourcesInConvFiles - sourcesInDcsEntries

        check(sourcesInConvFiles.isNotEmpty()) { "No sources in [*.$CONVERTED_FILE_SUFFIX] files." }
        check(sourcesInDcsEntries.isNotEmpty()) { "No sources in [*.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX] files." }
        check(result.isEmpty()) { "There are entries existing in [*.$CONVERTED_FILE_SUFFIX] files, but not in [*.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX]. Affected sources: [${result.joinToString(", ")}]" }

        log.info { "Checking if all sources in [*.$DOWNLOAD_CONTROL_STATE_FILE_SUFFIX] files exist in dataset and the other way round." }

        val sourcesInDataset = datasetFileAccessor.fetchEntries()
            .map { it.sources }
            .flatten()
            .filterNot { source -> ignoreMetaDataConfiguration.any { it.hostname() == source.host } }
            .toSet()
        check(sourcesInDataset.isNotEmpty()) { "No sources in dataset found." }

        result = (sourcesInDataset - sourcesInDcsEntries).union(sourcesInDcsEntries - sourcesInDataset)

        check(result.isEmpty()) { "Sources in dataset and sources in DCS entries differ: [${result.joinToString(", ")}]" }

        return true
    }

    private suspend fun sourcesInConvFiles(): Set<URI> = withContext(LIMITED_FS) {
        val jobs = appConfig.metaDataProviderConfigurations()
            .map { config -> appConfig.workingDir(config) }
            .map { directory ->
                directory.listRegularFiles("*.$CONVERTED_FILE_SUFFIX").map {
                    async {
                        Json.parseJson<AnimeRaw>(it.readFile())!!.sources
                    }
                }
            }.flatten()

        return@withContext awaitAll(*jobs.toTypedArray()).flatten().toSet()
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [SourcesConsistencyValidationPostProcessor]
         * @since 1.0.0
         */
        val instance: SourcesConsistencyValidationPostProcessor by lazy { SourcesConsistencyValidationPostProcessor() }
    }
}




