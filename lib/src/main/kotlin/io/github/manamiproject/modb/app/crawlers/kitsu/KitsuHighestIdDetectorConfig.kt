package io.github.manamiproject.modb.app.crawlers.kitsu

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.kitsu.KitsuConfig
import java.net.URI

/**
 * Configuration for detecting the highest id on kitsu.app
 * @since 1.0.0
 */
object KitsuHighestIdDetectorConfig: MetaDataProviderConfig by KitsuConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}/api/edge/anime?fields[anime]=slug,canonicalTitle,titles,posterImage,description,averageRating,startDate,popularityRank,ratingRank,youtubeVideoId&page[offset]=0&page[limit]=20&sort=-created_at")
}