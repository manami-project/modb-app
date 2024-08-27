package io.github.manamiproject.modb.app.downloadcontrolstate

/**
 * Updates the DCS files.
 * @since 1.0.0
 */
interface DownloadControlStateUpdater {

    /**
     * Update all DCS files which need to be updated.
     * @since 1.0.0
     */
    suspend fun updateAll()
}