package io.github.manamiproject.modb.core.anime

import java.net.URI

/**
 * @since 18.0.0
 */
public object AnimeMedia {

    /**
     * URL to a default picture.
     * @since 18.0.0
     */
    public val NO_PICTURE: URI = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png")

    /**
     * URL to a default thumbnail.
     * @since 18.0.0
     */
    public val NO_PICTURE_THUMBNAIL: URI = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png")
}