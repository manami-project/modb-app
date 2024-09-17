package io.github.manamiproject.modb.app.crawler.kitsu

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.kitsu.KitsuConfig
import java.net.URI

object KitsuHighestIdDetectorConfig: MetaDataProviderConfig by KitsuConfig {

    override fun buildDataDownloadLink(id: String): URI = URI("https://${hostname()}/api/edge/anime?fields[anime]=slug,canonicalTitle,titles,posterImage,description,averageRating,startDate,popularityRank,ratingRank,youtubeVideoId&page[offset]=0&page[limit]=20&sort=-created_at")
}