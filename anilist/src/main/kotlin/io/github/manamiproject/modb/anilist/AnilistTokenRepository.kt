package io.github.manamiproject.modb.anilist

/**
 * Stores the current anilist token.
 * @since 2.0.0
 */
public interface AnilistTokenRepository {

    /**
     * Currently valid token or a token with empty fields after initialization.
     * The token is only accepted if both cookie and csrf field are not blank.
     * @since 1.0.0
     * @throws IllegalArgumentException if the given token contains blank fields
     */
    public var token: AnilistToken
}