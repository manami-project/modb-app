package io.github.manamiproject.modb.core.anime

/**
 * Distribution status of an anime.
 * @since 1.0.0
 */
public enum class AnimeStatus {
    /**
     * Finished airing or has been released completely.
     * @since 1.0.0
     */
    FINISHED,
    /**
     * Currently airing or releasing.
     * @since 5.0.0
     */
    ONGOING,
    /**
     * Not yet released or aired.
     * @since 1.0.0
     */
    UPCOMING,
    /**
     * Status is unknown.
     * @since 1.0.0
     */
    UNKNOWN;

    public companion object {
        /**
         * Creates [AnimeStatus] from a [String]. Tolerant by ignoreing leading and trailing whitespaces as well as case.
         * @since 11.0.0
         * @param value The value being mapped to a [AnimeStatus]
         */
        public fun of(value: String): AnimeStatus {
            return AnimeStatus.entries.find { it.toString().equals(value.trim(), ignoreCase = true) } ?: UNKNOWN
        }
    }
}
