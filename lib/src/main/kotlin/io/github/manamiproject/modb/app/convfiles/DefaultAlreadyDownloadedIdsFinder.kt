package io.github.manamiproject.modb.app.convfiles

import io.github.manamiproject.modb.app.config.AppConfig
import io.github.manamiproject.modb.app.config.Config
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import io.github.manamiproject.modb.core.extensions.fileName
import io.github.manamiproject.modb.core.extensions.listRegularFiles
import io.github.manamiproject.modb.core.extensions.remove
import kotlinx.coroutines.withContext

/**
 * Default implementation for [AlreadyDownloadedIdsFinder].
 * @since 1.0.0
 * @property appConfig Application specific configuration. Uses [AppConfig] by default.
 */
class DefaultAlreadyDownloadedIdsFinder(
    private val appConfig: Config = AppConfig.instance,
): AlreadyDownloadedIdsFinder {

    override suspend fun alreadyDownloadedIds(metaDataProviderConfig: MetaDataProviderConfig): Set<AnimeId> = withContext(LIMITED_FS) {
        return@withContext appConfig.workingDir(metaDataProviderConfig)
            .listRegularFiles("*.${metaDataProviderConfig.fileSuffix()}")
            .asSequence()
            .map { it.fileName() }
            .map { it.remove(".${metaDataProviderConfig.fileSuffix()}") }
            .toSet()
    }

    companion object {
        /**
         * Singleton of [DefaultAlreadyDownloadedIdsFinder]
         * @since 1.0.0
         */
        val instance: DefaultAlreadyDownloadedIdsFinder by lazy { DefaultAlreadyDownloadedIdsFinder() }
    }
}