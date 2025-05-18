package io.github.manamiproject.modb.core.anime

/**
 * Default implementation for [AnimeRawToAnimeTransformer].
 * @since 17.0.0
 * @property scoreCalculator Implementation for calculating a [Score] based on multiple [MetaDataProviderScore].
 */
public class DefaultAnimeRawToAnimeTransformer(
    private val scoreCalculator: ScoreCalculator = DefaultScoreCalculator.instance,
): AnimeRawToAnimeTransformer {

    override fun transform(obj: AnimeRaw): Anime {

        return Anime(
            title = obj.title,
            sources = obj.sources,
            type = obj.type,
            episodes = obj.episodes,
            status = obj.status,
            animeSeason = obj.animeSeason,
            picture = obj.picture,
            thumbnail = obj.thumbnail,
            duration = obj.duration,
            score = scoreCalculator.calculateScore(obj.scores),
            synonyms = obj.synonyms,
            studios = obj.studios,
            producers = obj.producers,
            relatedAnime = obj.relatedAnime,
            tags = obj.tags,
        )
    }

    public companion object {

        /**
         * Singleton of [DefaultAnimeRawToAnimeTransformer]
         * @since 17.0.0
         */
        public val instance: DefaultAnimeRawToAnimeTransformer by lazy { DefaultAnimeRawToAnimeTransformer() }
    }
}