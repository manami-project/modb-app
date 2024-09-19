package io.github.manamiproject.modb.app.crawler

import io.github.manamiproject.modb.app.downloadcontrolstate.DefaultDownloadControlStateAccessor
import io.github.manamiproject.modb.app.downloadcontrolstate.DownloadControlStateAccessor
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.logging.LoggerDelegate

/**
 * Default implementation for [HighestIdAlreadyDownloadedDetector].
 * @since 1.0.0
 * @property downloadControlStateAccessor Access to DCS files.
 */
class DefaultHighestIdAlreadyDownloadedDetector(
    private val downloadControlStateAccessor: DownloadControlStateAccessor = DefaultDownloadControlStateAccessor.instance,
): HighestIdAlreadyDownloadedDetector {

    override suspend fun detectHighestIdAlreadyDownloaded(metaDataProviderConfig: MetaDataProviderConfig): Int {
        log.info { "Checking the minimum highest id for ${metaDataProviderConfig.hostname()}" }

        val list = downloadControlStateAccessor.allAnime(metaDataProviderConfig)
            .map { it.sources.first() }
            .map { metaDataProviderConfig.extractAnimeId(it) }

        return if (list.isEmpty()) {
            0
        } else {
            list.maxOf { it.toIntOrNull() ?: 0 }
        }
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DefaultHighestIdAlreadyDownloadedDetector]
         * @since 1.0.0
         */
        val instance: DefaultHighestIdAlreadyDownloadedDetector by lazy { DefaultHighestIdAlreadyDownloadedDetector() }
    }
}