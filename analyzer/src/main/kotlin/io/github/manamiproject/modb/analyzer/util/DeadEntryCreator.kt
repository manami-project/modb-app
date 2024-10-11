package io.github.manamiproject.modb.analyzer.util

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.convfiles.CONVERTED_FILE_SUFFIX
import io.github.manamiproject.modb.app.dataset.DefaultDeadEntriesAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateAccessor
import io.github.manamiproject.modb.core.coverage.KoverIgnore
import java.net.URI
import kotlin.io.path.deleteIfExists

@KoverIgnore
internal object DeadEntryCreator {

    suspend fun markAsDeadEntry(uri: URI) {
        val config = AppConfig.instance.findMetaDataProviderConfig(uri.toURL().host)
        val animeId = config.extractAnimeId(uri)

        DefaultDownloadControlStateAccessor.instance.removeDeadEntry(config, animeId)
        DefaultDeadEntriesAccessor.instance.addDeadEntry(animeId, config)

        AppConfig.instance.workingDir(config).resolve("$animeId.${config.fileSuffix()}").deleteIfExists()
        AppConfig.instance.workingDir(config).resolve("$animeId.$CONVERTED_FILE_SUFFIX").deleteIfExists()
    }
}