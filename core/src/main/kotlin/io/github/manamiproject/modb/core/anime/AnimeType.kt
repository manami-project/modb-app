package io.github.manamiproject.modb.core.anime

/**
 * Distribution type of an anime.
 * @since 1.0.0
 */
public enum class AnimeType {
    /**
     * @since 1.0.0
     */
    TV,
    /**
     * @since 1.0.0
     */
    MOVIE,
    /**
     * Original Video Animation. See [Wikipedia](https://en.wikipedia.org/wiki/Original_video_animation).
     * @since 1.0.0
     */
    OVA,
    /**
     * Original Net Animation. See [Wikipedia](https://en.wikipedia.org/wiki/Original_net_animation).
     * @since 1.0.0
     */
    ONA,
    /**
     * Basically anything else. Could be music videos, advertisements, manner movies or actual speical episodes.
     * @since 1.0.0
     */
    SPECIAL,
    /**
     * Type is unknown.
     * @since 5.0.0
     */
    UNKNOWN;

    public companion object {
        /**
         * Creates [AnimeType] from a [String]. Tolerant by ignoreing leading and trailing whitespaces as well as case.
         * @since 11.0.0
         * @param value The value being mapped to a [AnimeType].
         */
        public fun of(value: String): AnimeType {
            return AnimeType.entries.find { it.toString().equals(value.trim(), ignoreCase = true) } ?: UNKNOWN
        }
    }
}