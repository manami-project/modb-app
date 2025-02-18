package io.github.manamiproject.modb.core.anime

/**
 * Calculates the score for an [Anime].
 * @since 17.0.0
 */
public interface ScoreCalculator {

    /**
     * Takes a [Collection] of [MetaDataProviderScore] and calculates a [Score].
     * @since 17.0.0
     * @param scores Scores from meta data providers.
     * @return The score which has been calculated based on the given [scores] or [NoScore] for an empty [Collection].
     */
    public fun calculateScore(scores: Collection<MetaDataProviderScore>): Score
}