package io.github.manamiproject.modb.core.downloader

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.anime.AnimeRaw

/**
 * Downloads raw content containing data which describes an anime.
 * @since 1.0.0
 */
public interface Downloader {

    /**
     * Downloads raw data for a specific anime which then can be converted into an [AnimeRaw] using an [AnimeConverter].
     * @since 8.0.0
     * @param id The id of the anime for which you want to download the raw data.
     * @param onDeadEntry Function that is executed if the anime in question is found to be a dead entry on the website.
     * of the metadata provider when it is downloaded. **Default:** is no action.
     * @return Raw data.
     */
    public suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit = {}): String
}