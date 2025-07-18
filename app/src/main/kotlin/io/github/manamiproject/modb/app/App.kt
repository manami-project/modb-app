package io.github.manamiproject.modb.app

import io.github.manamiproject.modb.anisearch.AnisearchConfig
import io.github.manamiproject.modb.anisearch.AnisearchRelationsConfig
import io.github.manamiproject.modb.app.convfiles.DefaultRawFileConversionService
import io.github.manamiproject.modb.app.crawlers.anidb.AnidbCrawler
import io.github.manamiproject.modb.app.crawlers.anilist.AnilistCrawler
import io.github.manamiproject.modb.app.crawlers.animenewsnetwork.AnimenewsnetworkCrawler
import io.github.manamiproject.modb.app.crawlers.animeplanet.AnimePlanetCrawler
import io.github.manamiproject.modb.app.crawlers.anisearch.AnisearchCrawler
import io.github.manamiproject.modb.app.crawlers.kitsu.KitsuCrawler
import io.github.manamiproject.modb.app.crawlers.livechart.LivechartCrawler
import io.github.manamiproject.modb.app.crawlers.myanimelist.MyanimelistCrawler
import io.github.manamiproject.modb.app.crawlers.notify.NotifyDatasetDownloadCrawler
import io.github.manamiproject.modb.app.crawlers.simkl.SimklCrawler
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateUpdater
import io.github.manamiproject.modb.app.extensions.alertDeletedAnimeByTitle
import io.github.manamiproject.modb.app.fluentapi.*
import io.github.manamiproject.modb.app.network.LinuxNetworkController
import io.github.manamiproject.modb.app.postprocessors.*
import io.github.manamiproject.modb.core.coroutines.CoroutineManager.runCoroutine
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_NETWORK
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import io.github.manamiproject.modb.core.extensions.EMPTY
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.swing.JOptionPane.*
import javax.swing.JPasswordField
import javax.swing.SwingUtilities
import kotlin.system.exitProcess

@KoverIgnore
fun main() = runCoroutine {
    LinuxNetworkController.instance.sudoPasswordValue = passwordPrompt()

    val rawFileConversionService = DefaultRawFileConversionService.instance
    rawFileConversionService.start()

    withContext(LIMITED_NETWORK) {
        launch { AnidbCrawler.instance.start() }
        launch { AnilistCrawler.instance.start() }
        launch { AnimePlanetCrawler.instance.start() }
        launch { AnimenewsnetworkCrawler.instance.start() }
        launch { AnisearchCrawler(metaDataProviderConfig = AnisearchConfig).start() }
        launch { AnisearchCrawler(metaDataProviderConfig = AnisearchRelationsConfig).start() }
        launch { KitsuCrawler.instance.start() }
        launch { LivechartCrawler.instance.start() }
        launch { MyanimelistCrawler.instance.start() }
        launch { NotifyDatasetDownloadCrawler.instance.start() }
        launch { SimklCrawler.instance.start() }
    }.join()

    rawFileConversionService.waitForAllRawFilesToBeConverted()
    rawFileConversionService.shutdown()

    DefaultDownloadControlStateUpdater.instance.updateAll()
    DefaultDownloadControlStateAccessor.instance.allAnime()
        .alertDeletedAnimeByTitle()
        .mergeAnime()
        .removeUnknownEntriesFromRelatedAnime()
        .addAnimeCountdown()
        .transformToDatasetEntries()
        .saveToDataset()
        .updateStatistics()

    listOf(
        NoLockFilesLeftValidationPostProcessor.instance,
        DownloadControlStateWeeksValidationPostProcessor.instance,
        StudiosAndProducersExtractionChecker.instance,
        DuplicatesValidationPostProcessor.instance,
        ZstandardFilesForDeadEntriesCreatorPostProcessor.instance,
        DeadEntriesValidationPostProcessor.instance,
        SourcesConsistencyValidationPostProcessor.instance,
        NumberOfEntriesValidationPostProcessor.instance,
        FileSizePlausibilityValidationPostProcessor.instance,
        DeleteOldDownloadDirectoriesPostProcessor.instance,
        ReleaseInfoFileCreatorPostProcessor.instance
    ).forEach { it.process() }
}

@KoverIgnore
private fun passwordPrompt(): String {
    val console = System.console()
    if (console != null) {
        return String(console.readPassword("sudo password:"))
    }

    return try {
        var ret = EMPTY
        SwingUtilities.invokeAndWait {
            val passwordField = JPasswordField()
            val options = arrayOf<Any>("OK", "Cancel")
            val option = showOptionDialog(
                null,
                passwordField,
                "sudo password:",
                NO_OPTION, PLAIN_MESSAGE,
                null,
                options,
                options[0],
            )
            when (option) {
                0 -> {
                    val passwordArray = passwordField.password
                    ret = String(passwordArray)
                }
                else -> exitProcess(0)
            }
        }
        ret
    } catch (_: Exception) {
        println("sudo password:")
        readlnOrNull() ?: EMPTY
    }
}