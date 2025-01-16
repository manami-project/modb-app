package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank

/**
 * Central place to store the current anilist token so that the same token is always being used.
 * @since 1.0.0
 */
public object AnilistDefaultTokenRepository : AnilistTokenRepository {

    private var currentToken = AnilistToken(EMPTY, EMPTY)

    override var token: AnilistToken
        get() = currentToken
        set(value) {
            require(value.cookie.neitherNullNorBlank()) { "Cookie must not blank" }
            require(value.csrfToken.neitherNullNorBlank()) { "CSRF token must not blank" }
            currentToken = value
        }
}