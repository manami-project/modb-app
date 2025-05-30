package io.github.manamiproject.modb.anilist

/**
 * Contains the CSRF token and the corresponding cookie value.
 * @since 1.0.0
 * @property cookie Value for cookies header parameter
 * @property csrfToken Value for CSRF token header parameter
 */
public data class AnilistToken (
    val cookie: String,
    val csrfToken: String,
)