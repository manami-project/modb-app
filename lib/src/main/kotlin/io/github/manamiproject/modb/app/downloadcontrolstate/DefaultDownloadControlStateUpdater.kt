package io.github.manamiproject.modb.app.downloadcontrolstate

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.app.convfiles.CONVERTED_FILE_SUFFIX
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import io.github.manamiproject.modb.core.extensions.fileName
import io.github.manamiproject.modb.core.extensions.listRegularFiles
import io.github.manamiproject.modb.core.extensions.readFile
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.anime.AnimeRaw
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext


/**
 * This implementation of [DownloadControlStateUpdater] checks and updates DCS files.
 * There are some important key factors here.
 *
 * This class can only be executed once in a meaningful way per weekly update.
 * The reason is that before actually updating the DCS entries it also checks the newly created conv filed against
 * the already existing DCS files for possible problems in the converter classes. This is the only point of time where
 * it is possible and only before the update took place. That's way it is baked into the [updateAll] function instead of
 * being offered as a public function and then orchestrated elsewhere.
 * This is also the where the anime are checked for IDs being updated. Conv files are already accessed here and it's
 * supposed to be done prior to updating the DCS files. That's why it's also part of the [updateAll].
 *
 * Therefore this class is mostly a quality gate and process orchestrator for [DownloadControlStateAccessor].
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 * @property downloadControlStateAccessor Access to DCS files.
 */
class DefaultDownloadControlStateUpdater(
    private val appConfig: Config = AppConfig.instance,
    private val downloadControlStateAccessor: DownloadControlStateAccessor = DefaultDownloadControlStateAccessor.instance,
): DownloadControlStateUpdater {

    override suspend fun updateAll() = withContext(LIMITED_FS) {
        val convFileAnimeToFilename = fetchAnimeFromConvFiles()
        val animeToProvider = convFileAnimeToFilename.map { it.first to appConfig.findMetaDataProviderConfig(it.first.sources.first().host) }

        checkForExtractionProblems(animeToProvider)
        updateChangedIds(convFileAnimeToFilename)

        animeToProvider.forEach { (anime, metaDataProviderConfig) ->
            handleUpdate(anime, metaDataProviderConfig)
        }
    }

    private suspend fun fetchAnimeFromConvFiles(): List<Pair<AnimeRaw, String>> = withContext(LIMITED_FS) {
        log.info { "Loading [*.$CONVERTED_FILE_SUFFIX] files." }

        val jobs = appConfig.metaDataProviderConfigurations()
            .map { metaDataProviderConfig ->
                appConfig.workingDir(metaDataProviderConfig)
            }
            .map { workDir ->
                workDir.listRegularFiles("*.$CONVERTED_FILE_SUFFIX").map { file ->
                    async {
                        Json.parseJson<AnimeRaw>(file.readFile())!! to file.fileName()
                    }
                }
            }.flatten()

        return@withContext awaitAll(*jobs.toTypedArray())
    }

    private suspend fun checkForExtractionProblems(convFileAnime: List<Pair<AnimeRaw, MetaDataProviderConfig>>) {
        log.info { "Checking for possible extraction problems in the converter classes." }

        val counter = mutableMapOf<MetaDataProviderConfig, UInt>()
        val score = mutableMapOf<MetaDataProviderConfig, UInt>()

        appConfig.metaDataProviderConfigurations().forEach { metaDataProviderConfig ->
            counter[metaDataProviderConfig] = 0u
            score[metaDataProviderConfig] = 0u
        }

        convFileAnime.forEach { (anime, metaDataProviderConfig) ->
            val animeId = metaDataProviderConfig.extractAnimeId(anime.sources.first())

            if (downloadControlStateAccessor.dcsEntryExists(metaDataProviderConfig, animeId)) {
                val dcsEntry = downloadControlStateAccessor.dcsEntry(metaDataProviderConfig, animeId)
                val currentScore = dcsEntry.calculateQualityScore(anime)
                counter[metaDataProviderConfig] = counter[metaDataProviderConfig]!!.inc()
                score[metaDataProviderConfig] = score[metaDataProviderConfig]!! + currentScore
            }
        }

        val faulty = mutableListOf<String>()

        score.forEach { (metaDataProviderConfig, score) ->
            val numberOfFiles = counter[metaDataProviderConfig] ?: 0u
            val percentage = when(score) {
                0u -> 0u
                else -> (score.toDouble() / numberOfFiles.toDouble() * 100.0).toUInt()
            }

            if (percentage >= 25u) {
                faulty.add("${metaDataProviderConfig.hostname()} with a percentage of $percentage")
            }
        }

        check(faulty.isEmpty()) {
            val msg = StringBuilder("Possibly found a problem in the extraction of data:")
            faulty.forEach { msg.append("\n  * $it") }
            msg.toString()
        }
    }

    private suspend fun updateChangedIds(convFileAnimeToFilename: List<Pair<AnimeRaw, String>>) {
        log.info { "Checking if IDs have changed." }

        convFileAnimeToFilename.forEach { (anime, fileName) ->
            val metaDataProviderConfig = appConfig.findMetaDataProviderConfig(anime.sources.first().host)
            val fileId = fileName.removeSuffix(".$CONVERTED_FILE_SUFFIX")
            val animeId = metaDataProviderConfig.extractAnimeId(anime.sources.first())

            if (animeId != fileId) {
                check(appConfig.canChangeAnimeIds(metaDataProviderConfig)) { "Detected ID change from [$fileId] to [$animeId] although [${metaDataProviderConfig.hostname()}] doesn't support changing IDs." }
                downloadControlStateAccessor.changeId(fileId, animeId, metaDataProviderConfig)
            }
        }
    }

    private suspend fun handleUpdate(anime: AnimeRaw, metaDataProviderConfig: MetaDataProviderConfig) {
        val animeId = metaDataProviderConfig.extractAnimeId(anime.sources.first())

        val dcsEntry = when(downloadControlStateAccessor.dcsEntryExists(metaDataProviderConfig, animeId)) {
            false -> DownloadControlStateEntry(
                _weeksWihoutChange = 0,
                _lastDownloaded = WeekOfYear.currentWeek(),
                _nextDownload = WeekOfYear.currentWeek().plusWeeks(1),
                _anime = anime,
            )
            else -> {
                val downloadControlStateEntry = downloadControlStateAccessor.dcsEntry(metaDataProviderConfig, animeId)
                downloadControlStateEntry.update(anime)
            }
        }

        downloadControlStateAccessor.createOrUpdate(metaDataProviderConfig, animeId, dcsEntry)
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DefaultDownloadControlStateUpdater]
         * @since 1.0.0
         */
        val instance: DefaultDownloadControlStateUpdater by lazy { DefaultDownloadControlStateUpdater() }
    }
}