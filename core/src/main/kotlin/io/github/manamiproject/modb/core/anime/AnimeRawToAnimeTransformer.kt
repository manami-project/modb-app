package io.github.manamiproject.modb.core.anime

/**
 * Transforms anime objects from between the raw data version which is used as the target of a conversion from raw data
 * and the finalized object which is used for the dataset.
 * @since 17.0.0
 */
public interface AnimeRawToAnimeTransformer {

    /**
     * Converts an [AnimeRaw] to an [Anime]. This also includes calcuation of the score.
     * @since 17.0.0
     * @param obj The [AnimeRaw] after merging and any additional adjustments have been done.
     * @return The finalized version which used for the dataset.
     */
    public fun transform(obj: AnimeRaw): Anime
}